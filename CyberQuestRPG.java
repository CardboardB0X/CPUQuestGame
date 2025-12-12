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
    private JTextArea logArea;

    public CyberQuestRPG(int stageNum, MainMenu menu) {
        this.menuRef = menu;
        this.stage = stageNum;
        this.totalRounds = 2 + (stage / 3);

        GameState gs = GameState.get();
        
        int baseCores = gs.currentCpu.cores * (gs.useDualCpu ? 2 : 1);
        this.maxRAM = gs.getCurrentRamMB();
        this.maxHP = gs.getCurrentMaxHP();
        
        this.currentHP = maxHP;
        this.currentRAM = maxRAM;
        this.maxAP = baseCores;
        this.currentAP = maxAP;

        this.cpu = new CPUProfile(gs.currentCpu.label, Architecture.X86_64, baseCores, maxHP, maxRAM, Color.CYAN);

        initUI();
        startRound();
        
        log(">> SYSTEM ONLINE. FREQ: " + gs.currentCpu.freqGHz + "GHz | CACHE: " + gs.currentCpu.l3CacheMB + "MB");
    }

    private void initUI() {
        setTitle("STAGE " + stage + " | " + cpu.name);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 1));
        barHP = new JProgressBar(0, maxHP); barHP.setStringPainted(true); barHP.setForeground(Color.GREEN);
        barRAM = new JProgressBar(0, maxRAM); barRAM.setStringPainted(true); barRAM.setForeground(Color.CYAN);
        top.add(barHP); top.add(barRAM);
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
        JPanel skills = new JPanel(new GridLayout(2, 3));
        GameState gs = GameState.get();

        // Skill Buttons
        skills.add(mkBtn("FCFS", "Low Cost", () -> act("FCFS", 10)));
        skills.add(mkBtn("RR", "AoE", () -> act("RR", 50)));
        skills.add(mkBtn("SJF", "Priority", () -> act("SJF", 25)));

        if(gs.currentCpu.supportsThreads) skills.add(mkBtn("SMT", "Double AP", () -> activateSMT()));
        else skills.add(new JLabel("NO SMT"));
        
        skills.add(mkBtn("HEAL", "Repair", () -> act("HEAL", 100)));
        skills.add(mkBtn("END TURN", "Wait", this::endTurn));
        
        p.add(skills, BorderLayout.CENTER);
        logArea = new JTextArea(5, 30); p.add(new JScrollPane(logArea), BorderLayout.EAST);
        return p;
    }

    private void act(String type, int baseCost) {
        if(currentAP <= 0) { log(">> BUSY"); return; }
        
        GameState gs = GameState.get();
        int cost = baseCost - (int)(baseCost * (gs.undervoltVal/100.0));
        if(currentRAM < cost) { log(">> OOM"); return; }
        if(Math.random() < (gs.undervoltVal/200.0)) { log(">> ERROR: TASK FAILED (Stability)"); currentAP--; updateBars(); return; }

        currentRAM -= cost;
        currentAP--;
        double ocMult = 1.0 + (gs.overclockVal/100.0);
        
        // NEW DAMAGE FORMULA: SkillBase * ClockSpeedMultiplier * Overclock
        double clockMult = gs.getClockMultiplier() / 3.0; // Normalize around 3GHz

        if(type.equals("FCFS")) {
             int d = (int)((7 + gs.skillLvlFCFS*3) * clockMult * ocMult);
             if(rollCrit()) { d *= 2; log(">> CACHE HIT! CRITICAL!"); }
             if(!arena.isEmpty()) hitMonster(arena.get(0), d);
        } else if (type.equals("RR")) {
             int tq = Math.min(20, 5 + gs.skillLvlRR);
             int d = (int)(tq * clockMult * ocMult);
             // RR rarely crits (cache miss likely on context switch logic)
             for(Monster m : arena) hitMonster(m, d);
        } else if (type.equals("SJF")) {
             int d = (int)((10 + gs.skillLvlSJF*4) * clockMult * ocMult);
             if(rollCrit()) { d *= 2; log(">> CACHE HIT! CRITICAL!"); }
             if(!arena.isEmpty()) {
                 Monster m = arena.stream().min(Comparator.comparingInt(x->x.currentHP)).orElse(arena.get(0));
                 hitMonster(m, d);
             }
        } else if (type.equals("HEAL")) {
             currentHP = Math.min(maxHP, currentHP + 250);
        }
        
        if(Math.random() < (gs.overclockVal/300.0)) { currentHP-=50; log(">> OVERHEAT!"); }
        
        checkClear(); updateBars(); gamePanel.repaint();
    }
    
    private boolean rollCrit() {
        return Math.random() < GameState.get().getCritChance();
    }
    
    private void hitMonster(Monster m, int d) {
        m.currentHP -= d; m.isHit=true; log(">> HIT "+m.name+" ("+d+")");
        Timer t=new Timer(150, e->{m.isHit=false; gamePanel.repaint();}); t.setRepeats(false); t.start();
    }

    private void activateSMT() {
        if(currentRAM<50) return;
        currentRAM-=50; gamePanel.isHyperThreadingActive=true; maxAP*=2; currentAP+=maxAP/2; updateBars();
    }

    private void endTurn() {
        GameState gs = GameState.get();
        log(">> --- ENEMY TURN ---");
        for(Monster m : arena) {
            if(!m.isDead) {
                int raw = (int)(20 * m.type.dmgMult);
                int dmg = (int)(raw * (1.0 - gs.currentStorage.defense));
                currentHP -= dmg;
                log(m.name + " hits for " + dmg);
                gamePanel.isPlayerHit=true;
            }
        }
        
        int reg = gs.currentCooler.regen;
        if(currentHP<maxHP) { currentHP = Math.min(maxHP, currentHP+reg); log(">> COOLING: +"+reg+" HP"); }
        if(gs.hasECC && currentRAM<maxRAM) { currentRAM = Math.min(maxRAM, currentRAM+256); log(">> ECC: +256 MB"); }

        currentAP = maxAP;
        if(gamePanel.isHyperThreadingActive) { maxAP/=2; currentAP=maxAP; gamePanel.isHyperThreadingActive=false; }
        
        Timer t=new Timer(200,e->{gamePanel.isPlayerHit=false; gamePanel.repaint();}); t.setRepeats(false); t.start();
        if(currentHP<=0) { JOptionPane.showMessageDialog(this, "CRASHED"); dispose(); menuRef.setVisible(true); }
        updateBars();
    }

    private void startRound() {
        arena.clear();
        int count = 2 + (stage/2);
        for(int i=0; i<count; i++) {
            EnemyType t = EnemyType.CALCULATOR;
            if(stage>3) t = EnemyType.CHROME; if(stage>6) t = EnemyType.ANDROID;
            if(currentRound==totalRounds && i==0) {
                 if(stage==3) t=EnemyType.ZIP_BOMB; if(stage==6) t=EnemyType.RANSOMWARE; if(stage==10) t=EnemyType.CYBERPUNK;
            }
            int hp = (int)((100 + stage*30) * t.hpMult);
            arena.add(new Monster("P"+i, t, hp));
        }
        gamePanel.repaint();
    }

    private void checkClear() {
        arena.removeIf(m -> m.currentHP<=0);
        if(arena.isEmpty()) {
            if(currentRound<totalRounds) { currentRound++; startRound(); }
            else { 
                int rew = 500 + stage*250; GameState.get().currency+=rew; 
                JOptionPane.showMessageDialog(this, "WIN! +$"+rew); dispose(); menuRef.setVisible(true); 
            }
        }
    }

    private void updateBars() {
        barHP.setValue(currentHP); barHP.setString(currentHP+"/"+maxHP);
        barRAM.setValue(currentRAM); barRAM.setString(currentRAM+"/"+maxRAM+" MB");
        gamePanel.activeThreads=currentAP; gamePanel.maxThreads=maxAP;
    }

    private JButton mkBtn(String t, String tip, Runnable r) { JButton b=new JButton(t); b.setToolTipText(tip); b.addActionListener(e->r.run()); return b; }
    private void log(String s) { logArea.append(s+"\n"); }
}