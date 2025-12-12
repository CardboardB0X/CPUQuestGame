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

    // Stats
    private int currentHP, maxHP;
    private int currentRAM, maxRAM;
    private int currentAP, maxAP;
    
    private GamePanel gamePanel;
    private JProgressBar barHP, barRAM;
    private JLabel lblThreads;
    private JTextArea logArea;
    private JButton btnEndTurn;

    public CyberQuestRPG(int stageNum, MainMenu menu) {
        this.menuRef = menu;
        this.stage = stageNum;
        this.totalRounds = 2 + (stage / 3);

        GameState gs = GameState.get();
        int baseCores = gs.currentCpu.cores;
        this.maxRAM = gs.getCurrentRamMB();
        this.maxHP = gs.getCurrentMaxHP();
        
        this.currentHP = maxHP;
        this.currentRAM = maxRAM;
        this.maxAP = baseCores;
        this.currentAP = maxAP;

        this.cpu = new CPUProfile(gs.currentCpu.label, Architecture.X86_64, baseCores, maxHP, maxRAM, Color.CYAN);

        initUI();
        startRound();
        showTurnAnim("PLAYER PHASE");
        
        log(">> BOOT SEQUENCE COMPLETE. POWER DRAW: " + gs.getTotalWatts() + "W");
    }

    private void initUI() {
        setTitle("STAGE " + stage + " | " + cpu.name);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(3, 1));
        barHP = new JProgressBar(0, maxHP); barHP.setStringPainted(true); barHP.setForeground(Color.GREEN);
        barRAM = new JProgressBar(0, maxRAM); barRAM.setStringPainted(true); barRAM.setForeground(Color.CYAN);
        lblThreads = new JLabel("THREADS READY: " + currentAP + " / " + maxAP, SwingConstants.CENTER);
        lblThreads.setFont(new Font("Consolas", Font.BOLD, 18));
        lblThreads.setForeground(Color.WHITE);
        
        JPanel pBar = new JPanel(new GridLayout(2,1)); pBar.add(barHP); pBar.add(barRAM);
        top.setBackground(Color.BLACK); top.add(lblThreads); top.add(pBar);
        add(top, BorderLayout.NORTH);

        gamePanel = new GamePanel();
        gamePanel.setData(cpu, arena);
        gamePanel.maxThreads = maxAP; gamePanel.activeThreads = currentAP;
        add(gamePanel, BorderLayout.CENTER);

        add(createControlPanel(), BorderLayout.SOUTH);
        updateBars();
    }

    private JPanel createControlPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel skills = new JPanel(new GridLayout(2, 3, 5, 5));
        
        // RAM Costs tuned for balance (Regen is ~100MB/turn)
        skills.add(mkBtn("FCFS", "Attack (50MB)", () -> act("FCFS", 50)));
        skills.add(mkBtn("RR", "AoE (150MB)", () -> act("RR", 150)));
        skills.add(mkBtn("SJF", "Snipe (100MB)", () -> act("SJF", 100)));

        if(GameState.get().currentCpu.supportsThreads) skills.add(mkBtn("SMT", "Double AP (300MB)", () -> activateSMT()));
        else skills.add(new JLabel("NO SMT"));
        
        skills.add(mkBtn("HEAL", "Repair (200MB)", () -> act("HEAL", 200)));
        
        btnEndTurn = mkBtn("END TURN", "Pass Turn", this::endTurn);
        btnEndTurn.setBackground(new Color(200, 200, 200));
        skills.add(btnEndTurn);
        
        p.add(skills, BorderLayout.CENTER);
        logArea = new JTextArea(5, 30); p.add(new JScrollPane(logArea), BorderLayout.EAST);
        return p;
    }

    private void act(String type, int baseCost) {
        if(currentAP <= 0) { log(">> NO THREADS LEFT!"); return; }
        
        GameState gs = GameState.get();
        int cost = baseCost - (int)(baseCost * (gs.undervoltVal/100.0));
        if(currentRAM < cost) { log(">> OUT OF MEMORY (Need " + cost + "MB)"); return; }
        
        if(Math.random() < (gs.undervoltVal/200.0)) { log(">> KERNEL PANIC (Instability Fail)"); currentAP--; updateBars(); return; }

        currentRAM -= cost;
        currentAP--;
        
        int baseDmg = gs.calculateBaseDamage();
        boolean crit = Math.random() < gs.getCritChance();
        if(crit) baseDmg *= 2;

        if(type.equals("FCFS") && !arena.isEmpty()) hitMonster(arena.get(0), baseDmg);
        else if(type.equals("RR")) for(Monster m : arena) hitMonster(m, baseDmg/2);
        else if(type.equals("SJF") && !arena.isEmpty()) {
            Monster m = arena.stream().min(Comparator.comparingInt(x->x.currentHP)).orElse(arena.get(0));
            hitMonster(m, (int)(baseDmg * 1.5));
        }
        else if(type.equals("HEAL")) currentHP = Math.min(maxHP, currentHP + 150); // Buffed heal slightly
        
        if(crit) log(">> CRITICAL CACHE HIT!");
        checkClear(); updateBars(); gamePanel.repaint();
    }
    
    private void hitMonster(Monster m, int d) {
        m.currentHP -= d; m.isHit=true;
        Timer t=new Timer(150, e->{m.isHit=false; gamePanel.repaint();}); t.setRepeats(false); t.start();
    }

    private void activateSMT() {
        if(currentRAM < 300) return;
        currentRAM -= 300; gamePanel.isHyperThreadingActive=true; maxAP*=2; currentAP+=maxAP/2; updateBars();
    }

    private void endTurn() {
        if(currentAP > 0) {
             int confirm = JOptionPane.showConfirmDialog(this, "Threads active. End Turn?", "Wait", JOptionPane.YES_NO_OPTION);
             if(confirm != JOptionPane.YES_OPTION) return;
        }

        GameState gs = GameState.get();
        showTurnAnim("ENEMY PHASE");
        
        Timer t = new Timer(1500, e -> {
            for(Monster m : arena) {
                if(!m.isDead) {
                    int dmg = (int)(25 * m.type.dmgMult); // Soulslike: High Damage
                    currentHP -= dmg;
                    log(m.name + " hits: " + dmg + " HP");
                    gamePanel.isPlayerHit=true;
                }
            }
            
            // --- REGENERATION PHASE ---
            int hpRegen = gs.currentCooler.regen;
            currentHP = Math.min(maxHP, currentHP + hpRegen);
            
            // RAM REGEN (Passive + ECC)
            int ramRegen = 100; // Base passive
            if(gs.hasECC) ramRegen += 150;
            currentRAM = Math.min(maxRAM, currentRAM + ramRegen);
            
            log(">> SYSTEM: Regenerated " + hpRegen + " HP, " + ramRegen + " MB RAM.");

            currentAP = maxAP;
            if(gamePanel.isHyperThreadingActive) { maxAP/=2; currentAP=maxAP; gamePanel.isHyperThreadingActive=false; }
            
            updateBars(); gamePanel.isPlayerHit=false; gamePanel.repaint();
            
            if(currentHP<=0) { JOptionPane.showMessageDialog(this, "SYSTEM CRASHED."); dispose(); menuRef.setVisible(true); }
            else showTurnAnim("PLAYER PHASE");
        });
        t.setRepeats(false); t.start();
    }

    private void showTurnAnim(String text) {
        gamePanel.turnOverlayText = text;
        gamePanel.repaint();
        Timer t = new Timer(1000, e -> { gamePanel.turnOverlayText = ""; gamePanel.repaint(); });
        t.setRepeats(false); t.start();
    }

    private void startRound() {
        arena.clear();
        int count = 1 + (stage/2);
        for(int i=0; i<count; i++) {
            EnemyType t = EnemyType.CALCULATOR;
            if(stage>3) t = EnemyType.CHROME; if(stage>6) t = EnemyType.ANDROID;
            if(currentRound==totalRounds && i==0) {
                 if(stage==3) t=EnemyType.ZIP_BOMB; if(stage==6) t=EnemyType.RANSOMWARE; if(stage==10) t=EnemyType.CYBERPUNK;
            }
            int hp = (int)((40 + stage*10) * t.hpMult); // Low HP (Fragile Enemies)
            arena.add(new Monster("P"+i, t, hp));
        }
        gamePanel.repaint();
    }

    private void checkClear() {
        arena.removeIf(m -> m.currentHP<=0);
        if(arena.isEmpty()) {
            if(currentRound<totalRounds) { currentRound++; startRound(); showTurnAnim("NEXT WAVE"); }
            else { 
                int rew = 1000 + stage*500; GameState.get().currency+=rew; 
                JOptionPane.showMessageDialog(this, "VICTORY! +$"+rew); dispose(); menuRef.setVisible(true); 
            }
        }
    }

    private void updateBars() {
        barHP.setValue(currentHP); barHP.setString(currentHP+"/"+maxHP);
        barRAM.setValue(currentRAM); barRAM.setString(currentRAM+"/"+maxRAM+" MB");
        lblThreads.setText("THREADS: " + currentAP + " / " + maxAP);
        gamePanel.activeThreads=currentAP; gamePanel.maxThreads=maxAP;
        
        if(currentAP > 0) btnEndTurn.setBackground(new Color(255, 100, 100));
        else btnEndTurn.setBackground(new Color(200, 200, 200));
    }

    private JButton mkBtn(String t, String tip, Runnable r) { JButton b=new JButton(t); b.setToolTipText(tip); b.addActionListener(e->r.run()); return b; }
    private void log(String s) { logArea.append(s+"\n"); }
}