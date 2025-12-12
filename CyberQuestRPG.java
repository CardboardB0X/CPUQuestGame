import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CyberQuestRPG extends JFrame {

    private CPUProfile cpu; 
    private List<Monster> arena = new ArrayList<>();
    private MainMenu menuRef;
    private int stage, currentRound = 1, totalRounds;

    // Specific Integer Values
    private int currentHP, maxHP;
    private int currentRAM, maxRAM;
    private int currentAP, maxAP;
    
    private long startTime;
    private List<GameState.LogEntry> logs = new ArrayList<>();

    private GamePanel gamePanel;
    private JProgressBar barHP, barRAM;
    private JTextArea logArea;
    private JPanel skillPanel;

    public CyberQuestRPG(int stageNum, MainMenu menu) {
        this.menuRef = menu;
        this.stage = stageNum;
        this.totalRounds = 2 + (stage / 3);
        this.startTime = System.currentTimeMillis();

        GameState gs = GameState.get();
        
        // 1. HARDWARE STATS (Integer Base)
        int baseCores = gs.currentCpu.cores;
        if (gs.useDualCpu) baseCores *= 2;
        
        this.maxRAM = 256 + (baseCores * 64); // Integer calculation
        this.maxHP = 500 + (baseCores * 100); 
        
        this.cpu = new CPUProfile(gs.currentCpu.label, Architecture.X86_64, baseCores, maxHP, maxRAM, Color.CYAN);
        this.currentHP = maxHP;
        this.currentRAM = maxRAM;
        this.maxAP = baseCores;
        this.currentAP = maxAP;

        initUI();
        startRound();
        
        log(">> SYSTEM BOOT: " + baseCores + " CORES ONLINE.");
        if(gs.useDualCpu) log(">> DUAL SOCKET CONFIGURATION DETECTED.");
    }

    private void initUI() {
        setTitle("STAGE " + stage + " | " + cpu.name);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 1));
        barHP = new JProgressBar(0, maxHP); barHP.setStringPainted(true); barHP.setForeground(Color.GREEN);
        barRAM = new JProgressBar(0, maxRAM); barRAM.setStringPainted(true); barRAM.setForeground(Color.CYAN);
        updateBars();
        top.add(barHP); top.add(barRAM);
        add(top, BorderLayout.NORTH);

        gamePanel = new GamePanel();
        gamePanel.setData(cpu, arena);
        gamePanel.maxThreads = maxAP; gamePanel.activeThreads = currentAP;
        add(gamePanel, BorderLayout.CENTER);

        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private void updateBars() {
        barHP.setValue(currentHP); barHP.setString("INTEGRITY: " + currentHP + " / " + maxHP);
        barRAM.setValue(currentRAM); barRAM.setString("MEMORY: " + currentRAM + " / " + maxRAM + " MB");
    }

    private JPanel createControlPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(800, 200));
        skillPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        GameState gs = GameState.get();
        
        // --- BALANCED SKILL LOGIC v5.0 ---
        // FCFS: Base 7 + (Lvl * 3)
        int dmgFCFS = 7 + (gs.skillLvlFCFS * 3);
        skillPanel.add(mkBtn("FCFS (Lv"+gs.skillLvlFCFS+")", "Dmg: " + dmgFCFS, () -> act("FCFS", 0)));
        
        // RR: Time Quantum = 5 + Lvl. (Max 20)
        int tq = Math.min(20, 5 + gs.skillLvlRR); 
        int costRR = 15 + (gs.skillLvlRR * 2);
        skillPanel.add(mkBtn("RR (Lv"+gs.skillLvlRR+")", "Quantum: " + tq + " | Cost: " + costRR, () -> act("RR", costRR)));
        
        // SJF: Base 10 + (Lvl * 4)
        int dmgSJF = 10 + (gs.skillLvlSJF * 4);
        skillPanel.add(mkBtn("SJF (Lv"+gs.skillLvlSJF+")", "Priority Dmg: " + dmgSJF, () -> act("SJF", 25)));

        if (gs.currentCpu.supportsThreads) {
            skillPanel.add(mkBtn("SMT / HT", "Double Threads (Cost: 40)", () -> activateSMT()));
        } else {
            JButton b = new JButton("THREADS LOCKED"); b.setEnabled(false); skillPanel.add(b);
        }

        skillPanel.add(mkBtn("DEFRAG (Heal)", "Repair 200 HP", () -> act("HEAL", 50)));
        skillPanel.add(mkBtn("END TURN", "Recharge AP", this::endTurn));

        logArea = new JTextArea(5, 30); logArea.setEditable(false);
        p.add(skillPanel, BorderLayout.CENTER); p.add(new JScrollPane(logArea), BorderLayout.EAST);
        return p;
    }

    private void act(String type, int baseCost) {
        if(currentAP <= 0) { log(">> THREADS BUSY (0 AP)"); return; }
        
        GameState gs = GameState.get();
        int actualCost = baseCost - (int)(baseCost * (gs.undervoltVal / 100.0));
        if(currentRAM < actualCost) { log(">> LOW MEMORY (Need " + actualCost + ")"); return; }

        if(Math.random() < (gs.undervoltVal / 150.0)) {
            log(">> [ERROR] KERNEL PANIC! Task Failed (Undervolt Instability)");
            currentAP--; updateBars(); return; 
        }
        
        currentRAM -= actualCost;
        currentAP--;
        double ocMult = 1.0 + (gs.overclockVal / 100.0);
        
        int totalDmg = 0;

        if(type.equals("FCFS")) {
            int raw = 7 + (gs.skillLvlFCFS * 3);
            totalDmg = (int)(raw * ocMult);
            if(!arena.isEmpty()) hitMonster(arena.get(0), totalDmg);
        }
        else if(type.equals("RR")) {
            int tq = Math.min(20, 5 + gs.skillLvlRR);
            totalDmg = (int)(tq * ocMult);
            for(Monster m : arena) hitMonster(m, totalDmg);
            log(">> ROUND ROBIN: Processed all tasks for " + totalDmg + "ms (Dmg)");
        }
        else if(type.equals("SJF")) {
            int raw = 10 + (gs.skillLvlSJF * 4);
            totalDmg = (int)(raw * ocMult);
            if(!arena.isEmpty()) {
                Monster m = arena.stream().min(Comparator.comparingInt(x->x.currentHP)).orElse(arena.get(0));
                hitMonster(m, totalDmg);
            }
        }
        else if(type.equals("HEAL")) {
             currentHP = Math.min(maxHP, currentHP + 200);
             log(">> DEFRAG COMPLETE. Integrity Restored.");
        }

        logs.add(new GameState.LogEntry(type, System.currentTimeMillis()-startTime, 500, "COMPUTE"));
        
        if(Math.random() < (gs.overclockVal / 200.0)) {
            int burn = 20; currentHP -= burn; log(">> [ALERT] CPU OVERHEAT! -" + burn + " HP");
        }

        checkClear(); updateBars(); updateUI();
    }
    
    private void hitMonster(Monster m, int dmg) {
        m.currentHP -= dmg; m.isHit = true; 
        log(">> Hit " + m.name + " for " + dmg + " DMG");
        gamePanel.repaint();
        Timer t = new Timer(150, e -> { m.isHit = false; gamePanel.repaint(); });
        t.setRepeats(false); t.start();
    }

    private void activateSMT() {
        if(currentRAM < 40) return;
        currentRAM -= 40; gamePanel.isHyperThreadingActive = true;
        maxAP *= 2; currentAP += (maxAP/2); updateBars(); updateUI();
        log(">> LOGICAL CORES ENABLED. THREAD POOL DOUBLED.");
    }

    private void startRound() {
        arena.clear();
        int count = 2 + (stage/2);
        for(int i=0; i<count; i++) {
            EnemyType t = EnemyType.CALCULATOR;
            if(stage >= 3) t = (i%2==0) ? EnemyType.CHROME : EnemyType.NOTEPAD;
            if(stage >= 5) t = (i%2==0) ? EnemyType.VS_CODE : EnemyType.CHROME;
            if(stage >= 7) t = EnemyType.ANDROID;
            if(stage >= 9) t = EnemyType.BLENDER;
            if(currentRound == totalRounds && i==0) {
                if(stage == 3) t = EnemyType.ZIP_BOMB;
                if(stage == 6) t = EnemyType.RANSOMWARE;
                if(stage == 10) t = EnemyType.CYBERPUNK;
            }
            int baseHP = 40 + (stage * 15);
            int finalHP = (int)(baseHP * t.hpMult);
            arena.add(new Monster(t.label + "-" + i, t, finalHP));
        }
        log(">> ROUND " + currentRound + " INITIALIZED.");
        gamePanel.repaint();
    }

    private void endTurn() {
        log(">> --- ENEMY CYCLE ---");
        for(Monster m : arena) {
            if(!m.isDead) {
                int baseAtk = 8 + (stage * 2);
                int dmg = (int)(baseAtk * m.type.dmgMult);
                currentHP -= dmg;
                log(">> " + m.name + " executes attack: -" + dmg + " HP");
                gamePanel.isPlayerHit = true;
                gamePanel.shakeX = (int)(Math.random()*10)-5;
                gamePanel.shakeY = (int)(Math.random()*10)-5;
                gamePanel.repaint();
            }
        }
        
        Timer t = new Timer(300, e -> {
            gamePanel.isPlayerHit = false; gamePanel.repaint();
            if(currentHP <= 0) { JOptionPane.showMessageDialog(this, "SYSTEM FAILURE."); dispose(); menuRef.setVisible(true); }
        });
        t.setRepeats(false); t.start();

        currentAP = maxAP;
        if(gamePanel.isHyperThreadingActive) { maxAP /= 2; currentAP = maxAP; gamePanel.isHyperThreadingActive=false; }
        currentRAM = Math.min(maxRAM, currentRAM + 50);
        updateBars(); updateUI();
    }
    
    private void checkClear() {
        arena.removeIf(m -> m.currentHP <= 0);
        if(arena.isEmpty()) {
            if(currentRound < totalRounds) { currentRound++; startRound(); }
            else { 
                int reward = 500 + (stage * 250); 
                GameState.get().currency += reward;
                JOptionPane.showMessageDialog(this, "STAGE COMPLETE!\nREWARD: $" + reward);
                dispose(); menuRef.setVisible(true); 
            }
        }
    }

    private void updateUI() { gamePanel.activeThreads=currentAP; gamePanel.maxThreads=maxAP; gamePanel.repaint(); }
    private JButton mkBtn(String t, String tp, Runnable r) { JButton b=new JButton(t); b.setToolTipText(tp); b.addActionListener(e->r.run()); return b; }
    private void log(String s) { logArea.append(s+"\n"); }
}