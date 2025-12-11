import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CyberQuestRPG extends JFrame {

    private CPUProfile cpu;
    private List<Monster> arena = new ArrayList<>();
    private MainMenu menuRef;
    private int stage, currentRound = 1, totalRounds;

    // Stats
    private int currentHP, currentRAM;
    private int currentAP, maxAP;
    
    // Logging
    private long startTime;
    private List<GameState.LogEntry> logs = new ArrayList<>();

    // UI
    private GamePanel gamePanel;
    private JProgressBar barHP, barRAM;
    private JTextArea logArea;
    private JPanel skillPanel;

    public CyberQuestRPG(CPUProfile baseProfile, int stageNum, MainMenu menu) {
        this.menuRef = menu;
        this.stage = stageNum;
        this.totalRounds = 2 + stage; // More rounds for higher stages
        this.startTime = System.currentTimeMillis();

        // APPLY UPGRADES
        GameState gs = GameState.get();
        int finalHP = baseProfile.maxHP + gs.getBonusHP();
        int finalRAM = baseProfile.maxRAM + gs.getBonusRAM();
        
        this.cpu = new CPUProfile(baseProfile.name, baseProfile.arch, baseProfile.physicalCores, finalHP, finalRAM, baseProfile.color);
        this.currentHP = finalHP;
        this.currentRAM = finalRAM;
        this.maxAP = cpu.physicalCores; 
        this.currentAP = maxAP;

        initUI();
        startRound();
    }

    private void initUI() {
        setTitle("MISSION: STAGE " + stage);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Status
        JPanel top = new JPanel(new GridLayout(2, 1));
        barHP = new JProgressBar(0, cpu.maxHP); barHP.setStringPainted(true); barHP.setForeground(Color.GREEN);
        barRAM = new JProgressBar(0, cpu.maxRAM); barRAM.setStringPainted(true); barRAM.setForeground(Color.CYAN);
        top.add(barHP); top.add(barRAM);
        add(top, BorderLayout.NORTH);

        // Game Panel
        gamePanel = new GamePanel();
        gamePanel.setData(cpu, arena);
        gamePanel.maxThreads = maxAP; gamePanel.activeThreads = currentAP;
        
        if(GameState.get().currentGPUSkin.contains("RTX")) gamePanel.setBackground(new Color(40, 10, 50));
        add(gamePanel, BorderLayout.CENTER);

        // Controls
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(800, 200));

        skillPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        
        // --- SCHEDULING ALGORITHMS ---
        // FCFS: Hits First Enemy
        skillPanel.add(mkBtn("FCFS (0 RAM)", "First Come First Served: Hits Index 0", () -> playerAction("FCFS", 0)));
        
        // RR: Hits All Evenly
        skillPanel.add(mkBtn("Round Robin (10 RAM)", "Time Slice: Distribute Dmg to All", () -> playerAction("RR", 10)));
        
        // SJF: Hits Weakest
        skillPanel.add(mkBtn("SJF (20 RAM)", "Shortest Job: Hits Lowest HP", () -> playerAction("SJF", 20)));

        // SMT Skill
        if (GameState.get().unlockHyperThreading) {
            skillPanel.add(mkBtn("SMT / HYPERTHREAD (30 RAM)", "Double Threads", () -> activateSMT()));
        } else {
            JButton locked = new JButton("<html><center>LOCKED<br>(Buy Mobo)</center></html>"); locked.setEnabled(false);
            skillPanel.add(locked);
        }

        skillPanel.add(mkBtn("DEFRAG / HEAL (50 RAM)", "Repair HP", () -> playerAction("HEAL", 50)));
        skillPanel.add(mkBtn("END TURN", "Save Threads", this::endTurn));

        logArea = new JTextArea(5, 30); logArea.setEditable(false);
        p.add(skillPanel, BorderLayout.CENTER);
        p.add(new JScrollPane(logArea), BorderLayout.EAST);
        return p;
    }

    private void startRound() {
        arena.clear();
        int count = stage + currentRound;
        
        // --- COMPLEX ENEMY SPAWNING ---
        for(int i=0; i<count; i++) {
            EnemyType type = EnemyType.CALCULATOR; // Default fodder
            
            // Scaling logic
            if (stage == 1) {
                if(i == count-1) type = EnemyType.STANDARD; // Mini-boss
                else type = EnemyType.CALCULATOR;
            }
            else if (stage == 2) {
                if(i % 2 == 0) type = EnemyType.NOTEPAD;
                else type = EnemyType.STANDARD;
            }
            else if (stage == 3) {
                 if (i % 3 == 0) type = EnemyType.CHROME; // Tank
                 else type = EnemyType.MSTEAMS;
            }
            else if (stage == 4) {
                type = EnemyType.TROJAN; // High Dmg glass cannons
            }
            else if (stage == 5) {
                type = EnemyType.BOTNET;
            }
            
            // BOSS ROUNDS (Final round of stage)
            if (currentRound == totalRounds) {
                if (i==0) { // Only spawn boss once
                     if(stage == 1) type = EnemyType.MSTEAMS;
                     if(stage == 2) type = EnemyType.CHROME;
                     if(stage == 3) type = EnemyType.RANSOMWARE;
                     if(stage == 4) type = EnemyType.MAINFRAME;
                     if(stage == 5) type = EnemyType.MAINFRAME; // Final boss
                } else {
                    continue; // Boss fights alone or with few minions
                }
            }

            // Health Scaling per Stage
            int baseHealth = 50 + (stage * 20);
            arena.add(new Monster("P-"+i, type, baseHealth));
        }
        log(">> ALERT: Wave " + currentRound + " Initialized. " + arena.size() + " threats.");
        gamePanel.repaint();
    }

    // --- PLAYER ACTIONS ---
    private void activateSMT() {
        if (currentRAM < 30) { log(">> LOW RAM!"); return; }
        currentRAM -= 30;
        gamePanel.isHyperThreadingActive = true;
        maxAP = cpu.physicalCores * 2;
        currentAP += cpu.physicalCores;
        log(">> SMT ONLINE. Threads Doubled.");
        updateUI();
    }

    private void playerAction(String type, int ram) {
        if (currentRAM < ram) { log(">> INSUFFICIENT RAM"); return; }
        if (currentAP <= 0) { log(">> THREADS BUSY"); return; }
        if (arena.isEmpty() && !type.equals("HEAL")) return;

        currentRAM -= ram;
        currentAP--;
        
        // 1. CALCULATE DAMAGE (Base + Upgrade + Crit Check)
        int dmg = 30 + GameState.get().getBonusDamage();
        boolean isCrit = Math.random() < GameState.get().getCritChance();
        if (isCrit) {
            dmg *= 2;
            log(">> L1 CACHE HIT! CRITICAL DAMAGE!");
        }

        // 2. SCHEDULING LOGIC
        if (type.equals("FCFS")) {
            // First Come First Served: Always hit index 0
            Monster m = arena.get(0);
            m.currentHP -= dmg;
            triggerHitAnim(m);
            log(">> FCFS: Executed " + m.name + " (" + dmg + " dmg)");
        } 
        else if (type.equals("RR")) {
            // Round Robin: Distribute damage evenly (Time Slicing)
            int splitDmg = Math.max(5, dmg / arena.size());
            for(Monster m : arena) {
                m.currentHP -= splitDmg;
                triggerHitAnim(m);
            }
            log(">> RR: Time Slice broadcast (" + splitDmg + " dmg each).");
        }
        else if (type.equals("SJF")) {
            // Shortest Job First: Hit enemy with Lowest HP
            Monster target = arena.stream()
                .min(Comparator.comparingInt(m -> m.currentHP))
                .orElse(arena.get(0));
            
            // Bonus damage for efficiency
            int sjfDmg = (int)(dmg * 1.2); 
            target.currentHP -= sjfDmg;
            triggerHitAnim(target);
            log(">> SJF: Priority Kill on " + target.name + " (" + sjfDmg + " dmg)");
        }
        else if (type.equals("HEAL")) {
            currentHP = Math.min(cpu.maxHP, currentHP + 150);
            log(">> DEFRAG COMPLETE. HP Restored.");
        }

        logs.add(new GameState.LogEntry(type, System.currentTimeMillis()-startTime, 400, "COMPUTE"));
        checkDeaths();
        updateUI();
    }

    private void triggerHitAnim(Monster m) {
        m.isHit = true; gamePanel.repaint();
        Timer t = new Timer(150, e -> { m.isHit = false; gamePanel.repaint(); });
        t.setRepeats(false); t.start();
    }

    // --- ENEMY PHASE & REGEN ---
    private void endTurn() {
        enableControls(false);
        
        // COOLING SYSTEM REGEN (Passive Upgrade)
        int regen = GameState.get().getRegenAmount();
        if (regen > 0 && currentHP < cpu.maxHP) {
            currentHP = Math.min(cpu.maxHP, currentHP + regen);
            log(">> LIQUID COOLING: Regenerated " + regen + " HP.");
            updateUI();
        }

        log(">> ENEMY PROCESSES RUNNING...");
        processEnemyAttack(0);
    }

    private void processEnemyAttack(int index) {
        if (index >= arena.size()) {
            endEnemyPhase();
            return;
        }
        Monster m = arena.get(index);
        if (m.isDead) { processEnemyAttack(index+1); return; }

        // Damage calculation based on Monster Type
        int dmg = (int)(12 * m.type.dmgMult);
        currentHP -= dmg;
        
        // Shake Screen
        gamePanel.isPlayerHit = true;
        gamePanel.shakeX = (int)(Math.random()*15 - 7);
        gamePanel.shakeY = (int)(Math.random()*15 - 7);
        gamePanel.repaint();
        log(">> " + m.name + " hits for " + dmg + " dmg!");
        barHP.setValue(currentHP);

        Timer t = new Timer(500, e -> {
            gamePanel.isPlayerHit = false; gamePanel.repaint();
            if(currentHP <= 0) { lose(); return; }
            processEnemyAttack(index+1);
        });
        t.setRepeats(false); t.start();
    }

    private void endEnemyPhase() {
        currentAP = maxAP; // Reset AP
        currentRAM = Math.min(cpu.maxRAM, currentRAM + 25); // Base RAM Regen
        
        if (gamePanel.isHyperThreadingActive) {
            gamePanel.isHyperThreadingActive = false;
            maxAP = cpu.physicalCores; currentAP = maxAP;
            log(">> SMT THREADS TERMINATED.");
        }
        enableControls(true);
        updateUI();
    }

    private void checkDeaths() {
        arena.removeIf(m -> m.currentHP <= 0);
        if(arena.isEmpty()) {
            if(currentRound < totalRounds) {
                currentRound++;
                log(">> WAVE COMPLETE. LOADING NEXT SET...");
                Timer t = new Timer(1000, e -> startRound());
                t.setRepeats(false); t.start();
            } else {
                win();
            }
        }
    }

    private void win() {
        int reward = stage * 400; // Better rewards
        JOptionPane.showMessageDialog(this, "SECTOR CLEARED!\nData Recovered.\nReward: $" + reward);
        GameState.get().currency += reward;
        GameState.get().lastBattleLogs = new ArrayList<>(logs);
        dispose(); menuRef.setVisible(true);
    }

    private void lose() {
        JOptionPane.showMessageDialog(this, "SYSTEM CRASHED.\nRebooting...");
        GameState.get().lastBattleLogs = new ArrayList<>(logs);
        dispose(); menuRef.setVisible(true);
    }

    private void updateUI() {
        barHP.setValue(currentHP); barHP.setString(currentHP + "/" + cpu.maxHP);
        barRAM.setValue(currentRAM); barRAM.setString(currentRAM + "/" + cpu.maxRAM);
        gamePanel.activeThreads = currentAP; gamePanel.maxThreads = maxAP;
        gamePanel.repaint();
    }

    private void enableControls(boolean b) { for(Component c : skillPanel.getComponents()) c.setEnabled(b); }
    private void log(String s) { logArea.append(s+"\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
    private JButton mkBtn(String t, String tp, Runnable r) { JButton b = new JButton(t); b.setToolTipText(tp); b.addActionListener(e->r.run()); return b; }
}