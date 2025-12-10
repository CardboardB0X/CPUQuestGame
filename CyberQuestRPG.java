import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CyberQuestRPG extends JFrame {

    // Models
    private CPUProfile cpu;
    private OperatingSystem os;
    private List<Monster> arena = new ArrayList<>();
    
    // Stats
    private int cpuCurrentHP, cpuCurrentRAM;
    private int currentActionPoints; 
    private int coolingPotions = 3, ramSticks = 3;
    
    // State Flags
    private boolean isGameOver = false;
    private int globalTime = 0;

    // UI
    private GamePanel gamePanel;
    private JTextArea logArea;
    private JProgressBar barHP, barRAM;
    private JLabel lblAP;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CyberQuestRPG::createOSSelection);
    }

    // --- SETUP SCREENS (Same as before) ---
    private static void createOSSelection() {
        JFrame frame = new JFrame("STEP 1: INSTALL OS");
        frame.setSize(600, 450);
        frame.setLayout(new GridLayout(3, 1, 10, 10));
        for (OperatingSystem os : OperatingSystem.values()) {
            JButton btn = new JButton("<html><center><h2>" + os.label + "</h2>" + os.desc + "</center></html>");
            btn.addActionListener(e -> { frame.dispose(); createCPUSelection(os); });
            frame.add(btn);
        }
        frame.setVisible(true);
    }

    private static void createCPUSelection(OperatingSystem selectedOS) {
        JFrame frame = new JFrame("STEP 2: SELECT HARDWARE (MAX 3 CORES)");
        frame.setSize(1000, 500);
        frame.setLayout(new GridLayout(1, 3, 10, 10));

        // 1. INTEL (3 Cores)
        CPUProfile intel = new CPUProfile("Intel Core i9", Architecture.X86_64, 3, 200, 100, Color.BLUE);
        frame.add(createCard(frame, intel, selectedOS));

        // 2. AMD (3 Cores)
        CPUProfile amd = new CPUProfile("AMD Ryzen 9", Architecture.X86_64, 3, 180, 120, Color.RED);
        frame.add(createCard(frame, amd, selectedOS));

        // 3. APPLE (2 Cores)
        CPUProfile apple = new CPUProfile("Apple M2", Architecture.ARM64, 2, 250, 80, Color.ORANGE);
        frame.add(createCard(frame, apple, selectedOS));

        frame.setVisible(true);
    }

    private static JButton createCard(JFrame parent, CPUProfile p, OperatingSystem os) {
        JButton btn = new JButton("<html><center><h2>" + p.name + "</h2>CORES: " + p.physicalCores + "<br>HP: " + p.maxHP + " | RAM: " + p.maxRAM + "</center></html>");
        btn.setBackground(Color.BLACK);
        btn.setForeground(p.color);
        btn.addActionListener(e -> { parent.dispose(); new CyberQuestRPG(p, os).setVisible(true); });
        return btn;
    }

    // --- MAIN GAME ---
    public CyberQuestRPG(CPUProfile selectedCPU, OperatingSystem selectedOS) {
        this.cpu = selectedCPU;
        this.os = selectedOS;
        
        this.cpuCurrentHP = cpu.maxHP;
        this.cpuCurrentRAM = cpu.maxRAM;
        this.currentActionPoints = cpu.physicalCores; 

        setTitle("System Defense: " + cpu.name);
        setSize(1200, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // TOP PANEL
        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.setBackground(new Color(20, 20, 30));
        barHP = createBar(Color.GREEN, "INTEGRITY", cpu.maxHP);
        barRAM = createBar(Color.CYAN, "MEMORY", cpu.maxRAM);
        lblAP = new JLabel("THREADS READY", SwingConstants.CENTER);
        lblAP.setFont(new Font("Consolas", Font.BOLD, 24));
        
        topPanel.add(barHP); topPanel.add(barRAM); topPanel.add(lblAP);
        add(topPanel, BorderLayout.NORTH);

        // CENTER
        gamePanel = new GamePanel();
        gamePanel.setData(cpu, arena);
        add(gamePanel, BorderLayout.CENTER);

        // BOTTOM
        add(createControlPanel(), BorderLayout.SOUTH);

        spawnMonster(); spawnMonster(); 
        startPlayerPhase(); // Start the game loop
        log(">> BOOT SEQUENCE COMPLETE.");
    }

    private JPanel createControlPanel() {
        JPanel botPanel = new JPanel(new BorderLayout());
        botPanel.setPreferredSize(new Dimension(1000, 240));
        JPanel skillsGrid = new JPanel(new GridLayout(1, 3, 10, 0));
        
        // 1. ATTACKS
        JPanel pnlOff = createCategory("⚔️ COMMANDS");
        pnlOff.add(createBtn("EXECUTE (FCFS)", "Single Target Hit", () -> tryAction("FCFS", 0)));
        pnlOff.add(createBtn("BROADCAST (RR)", "Hit All Enemies", () -> tryAction("RR", 10)));
        pnlOff.add(createBtn("PRIORITY (SJF)", "Snipe Weakest", () -> tryAction("SJF", 20)));

        // 2. SPECIALS
        JPanel pnlDef = createCategory("🛡️ SPECIALS");
        
        String htName = (cpu.arch == Architecture.X86_64) ? (cpu.name.contains("AMD") ? "SMT MODE" : "HYPERTHREADING") : "N/A";
        
        if (cpu.arch == Architecture.X86_64) {
            pnlDef.add(createBtn(htName + " (30 RAM)", "Split Cores -> 2x Threads + Defense", () -> activateHyperthreading()));
        } else {
            pnlDef.add(createBtn("ARM EFFICIENCY", "Passive: High Cooling", () -> log(">> PASSIVE SKILL ACTIVE")));
        }
        
        pnlDef.add(createBtn("FIREWALL (15 RAM)", "Block Attack", () -> tryAction("DEFEND", 15)));
        pnlDef.add(createBtn("OVERCLOCK (ULT)", "60 RAM Cost", () -> tryAction("ULT", 60)));

        // 3. ITEMS
        JPanel pnlItem = createCategory("💊 HARDWARE");
        pnlItem.add(createBtn("Cooling Paste", "Repair HP", () -> useItem("HEAL")));
        pnlItem.add(createBtn("Download RAM", "Restore RAM", () -> useItem("RAM")));
        pnlItem.add(createBtn("End Turn", "Save AP", this::endTurn));

        skillsGrid.add(pnlOff); skillsGrid.add(pnlDef); skillsGrid.add(pnlItem);
        botPanel.add(skillsGrid, BorderLayout.CENTER);

        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        botPanel.add(new JScrollPane(logArea), BorderLayout.EAST);
        
        return botPanel;
    }

    // --- PHASE MANAGEMENT ---
    private void startPlayerPhase() {
        gamePanel.turnStateStr = "PLAYER PHASE";
        // Reset flags
        gamePanel.isBoosted = false;
        gamePanel.isDefending = false;
        updateUI();
        gamePanel.repaint();
    }

    // --- CORE MECHANIC: HYPERTHREADING ANIMATION ---
    private void activateHyperthreading() {
        if (cpuCurrentRAM < 30) { log(">> LOW RAM!"); return; }
        if (gamePanel.isHyperthreadingVisuallyActive) { log(">> ALREADY ACTIVE!"); return; }
        
        cpuCurrentRAM -= 30;
        updateUI();

        // 1. Start Visual Animation
        gamePanel.isHyperthreadingVisuallyActive = true;
        log(">> INITIATING CORE SPLIT...");
        gamePanel.repaint();

        // 2. Delay actual effect for 1 second to let animation play
        Timer t = new Timer(1000, e -> {
            currentActionPoints = cpu.physicalCores * 2; 
            log(">> SUCCESS: THREADS DOUBLED (" + currentActionPoints + ").");
            log(">> DEFENSE MATRIX ONLINE.");
            updateUI();
            gamePanel.isBoosted = true; // Add glow
            gamePanel.repaint();
        });
        t.setRepeats(false); t.start();
    }

    // --- BATTLE LOGIC ---
    private void tryAction(String type, int cost) {
        if (isGameOver || !gamePanel.turnStateStr.equals("PLAYER PHASE")) return;
        if (currentActionPoints <= 0) { log(">> NO THREADS LEFT! END TURN."); return; }
        if (cpuCurrentRAM < cost) { log(">> LOW RAM!"); return; }
        if (arena.isEmpty() && !type.equals("DEFEND")) return;

        cpuCurrentRAM -= cost;
        currentActionPoints--; 
        updateUI();

        gamePanel.currentAnimation = "ATTACK";
        gamePanel.repaint();

        Timer t = new Timer(200, e -> executeSkill(type));
        t.setRepeats(false); t.start();
    }

    private void executeSkill(String type) {
        gamePanel.currentAnimation = "IDLE";
        
        if (type.equals("FCFS") && !arena.isEmpty()) {
            Monster m = arena.get(0); applyDamage(m, 30); log(">> EXECUTED " + m.name);
        } 
        else if (type.equals("RR")) {
            for(Monster m : arena) applyDamage(m, 15); log(">> BROADCAST HIT ALL");
        }
        else if (type.equals("SJF") && !arena.isEmpty()) {
            Monster m = arena.stream().min(Comparator.comparingInt(x -> x.currentHP)).orElse(arena.get(0));
            applyDamage(m, 50); log(">> PRIORITY KILL ON " + m.name);
        }
        else if (type.equals("ULT")) {
            for(Monster m : arena) applyDamage(m, 100); log(">> OVERCLOCK SURGE!");
        }
        else if (type.equals("DEFEND")) {
            gamePanel.isDefending = true; log(">> FIREWALL DEPLOYED.");
        }

        if (currentActionPoints <= 0) endTurn();
    }

    private void endTurn() {
        gamePanel.turnStateStr = "ENEMY PHASE";
        log(">> END PHASE. PROCESSING INCOMING TRAFFIC...");
        arena.removeIf(m -> m.currentHP <= 0);
        gamePanel.repaint();

        Timer t = new Timer(1000, e -> enemyPhase());
        t.setRepeats(false); t.start();
    }

    private void enemyPhase() {
        if (!arena.isEmpty()) {
            if (gamePanel.isDefending) {
                log(">> BLOCKED BY FIREWALL.");
            } else {
                int totalDmg = 0;
                for(Monster m : arena) {
                    m.waitingTime++;
                    totalDmg += 5 + (m.waitingTime);
                }
                
                if (gamePanel.isHyperthreadingVisuallyActive) {
                    log(">> SMT DEFENSE: DAMAGE HALVED!");
                    totalDmg = totalDmg / 2;
                }
                
                // TRIGGER SHAKE ANIMATION
                if (totalDmg > 0) triggerShake(totalDmg);
                
                cpuCurrentHP -= totalDmg;
                log(">> WARNING: SYSTEM TOOK " + totalDmg + " DAMAGE.");
            }
        }
        
        // Reset for next turn
        cpuCurrentRAM = Math.min(cpu.maxRAM, cpuCurrentRAM + 15);
        currentActionPoints = cpu.physicalCores;
        
        // Reset Hyperthreading visual state for next turn
        gamePanel.isHyperthreadingVisuallyActive = false;
        
        updateUI();
        
        if(Math.random() > 0.5) spawnMonster();
        if (cpuCurrentHP <= 0) gameOver();
        else {
            Timer t = new Timer(1000, e -> startPlayerPhase());
            t.setRepeats(false); t.start();
        }
    }

    // --- SHAKE ANIMATION LOGIC ---
    private void triggerShake(int damageAmt) {
        gamePanel.isCPUHurt = true; // Flash red
        int intensity = Math.min(20, Math.max(5, damageAmt / 2)); // Cap intensity
        
        Timer shakeTimer = new Timer(40, new ActionListener() {
            int frames = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frames++ > 8) { // Shake for 8 frames
                    gamePanel.shakeX = 0;
                    gamePanel.shakeY = 0;
                    gamePanel.isCPUHurt = false;
                    gamePanel.repaint();
                    ((Timer)e.getSource()).stop();
                } else {
                    // Random shake wiggle
                    gamePanel.shakeX = (int)(Math.random() * intensity * 2) - intensity;
                    gamePanel.shakeY = (int)(Math.random() * intensity * 2) - intensity;
                    gamePanel.repaint();
                }
            }
        });
        shakeTimer.start();
    }

    // --- HELPERS ---
    private void spawnMonster() {
        if (isGameOver) return;
        EnemyType[] types = EnemyType.values();
        EnemyType type = types[new Random().nextInt(types.length)];
        Monster m = new Monster(String.valueOf(++globalTime), type, 40 + new Random().nextInt(30));
        arena.add(m);
        gamePanel.repaint();
    }

    private void useItem(String type) {
        if (currentActionPoints <= 0 || !gamePanel.turnStateStr.equals("PLAYER PHASE")) return;
        currentActionPoints--;
        if (type.equals("HEAL") && coolingPotions > 0) {
            coolingPotions--; cpuCurrentHP = Math.min(cpu.maxHP, cpuCurrentHP + 50);
        } else if (type.equals("RAM") && ramSticks > 0) {
            ramSticks--; cpuCurrentRAM = Math.min(cpu.maxRAM, cpuCurrentRAM + 50);
        }
        updateUI();
    }

    private void applyDamage(Monster m, int dmg) {
        m.currentHP -= dmg;
        if (m.currentHP <= 0) { m.isDead = true; log(">> KILLED " + m.name); }
    }

    private void updateUI() {
        barHP.setValue(cpuCurrentHP); barRAM.setValue(cpuCurrentRAM);
        lblAP.setText("THREADS READY: " + currentActionPoints + " / " + (gamePanel.isHyperthreadingVisuallyActive ? cpu.physicalCores*2 : cpu.physicalCores));
        lblAP.setForeground(gamePanel.isHyperthreadingVisuallyActive ? Color.CYAN : Color.YELLOW);
    }

    private void gameOver() {
        isGameOver = true;
        JOptionPane.showMessageDialog(this, "SYSTEM CRASHED!");
    }

    private void log(String s) { logArea.append(s + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
    
    private JProgressBar createBar(Color c, String t, int m) {
        JProgressBar b = new JProgressBar(0, m); b.setValue(m); b.setStringPainted(true); b.setString(t); b.setForeground(c); b.setBackground(Color.DARK_GRAY); return b;
    }
    private JPanel createCategory(String t) {
        JPanel p = new JPanel(new GridLayout(3, 1, 5, 5)); p.setBorder(BorderFactory.createTitledBorder(t)); return p;
    }
    private JButton createBtn(String t, String tp, Runnable a) {
        JButton b = new JButton(t); b.setToolTipText(tp); b.setBackground(Color.WHITE); b.addActionListener(e -> a.run()); return b;
    }
}