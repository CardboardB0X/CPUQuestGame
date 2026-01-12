import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// CLASS DEFINITION: Extends JFrame to create the main game window.
public class CyberQuestRPG extends JFrame {

    // --- DATA MODELS ---
    // CPUProfile stores the visual stats (Name, Color, etc.) of the player's CPU.
    private CPUProfile cpu; 
    
    // List<Monster> is a dynamic array that holds all currently alive enemies.
    private List<Monster> arena = new ArrayList<>(); 
    
    // Reference to the Main Menu window so we can re-open it when the battle ends.
    private MainMenu menuRef; 
    
    // Progression variables tracking the current floor and wave.
    private int stage, currentRound = 1, totalRounds;

    // --- BATTLE STATS ---
    // Integer variables for Health (HP), Memory (RAM), and Action Points (AP).
    // 'current' tracks live value, 'max' tracks the cap.
    private int currentHP, maxHP;
    private int currentRAM, maxRAM;
    private int currentAP, maxAP;
    
    // --- SKILL MECHANICS (New in v5.4) ---
    private int turboCharges = 2;   // Limits Turbo Boost to 2 uses per run.
    private int turboDuration = 0;  // Tracks how many turns the boost lasts.
    private boolean smtUsed = false;// Boolean flag to ensure SMT is used only once per battle.
    
    // --- GANTT LOGGING (ADDED) ---
    private long startTime;
    private List<GameState.LogEntry> logs = new ArrayList<>();

    // --- UI COMPONENTS ---
    private GamePanel gamePanel;       // Custom drawing panel for graphics.
    private JProgressBar barHP, barRAM;// Visual bars for stats.
    private JLabel lblThreads;         // Text label for AP count.
    private JTextArea logArea;         // Scrollable text area for battle logs.
    private JButton btnEndTurn;        // Reference to button to change its color.

    // CONSTRUCTOR: This runs when 'new CyberQuestRPG(...)' is called.
    public CyberQuestRPG(int stageNum, MainMenu menu) {
        this.menuRef = menu;
        this.stage = stageNum;
        // Formula: Calculates rounds based on stage. Stage 1-2 = 2 rounds, Stage 3 = 3 rounds.
        this.totalRounds = 2 + (stage / 3);
        
        // Start Timer for Logs
        this.startTime = System.currentTimeMillis();

        // ACCESS GLOBAL STATE: Get the Singleton instance of GameState.
        GameState gs = GameState.get();
        
        // SETUP STATS:
        // Calculate base cores from the equipped CPU type.
        int baseCores = gs.currentCpu.cores;
        
        // Retrieve calculated max stats from GameState (includes upgrades).
        this.maxRAM = gs.getCurrentRamMB();
        this.maxHP = gs.getCurrentMaxHP();
        
        // Initialize current values to maximum at start of battle.
        this.currentHP = maxHP;
        this.currentRAM = maxRAM;
        this.maxAP = baseCores;
        this.currentAP = maxAP;

        // Create CPU visual profile for GamePanel.
        this.cpu = new CPUProfile(gs.currentCpu.label, Architecture.X86_64, baseCores, maxHP, maxRAM, Color.CYAN);

        // Build the Window UI.
        initUI();
        
        // Spawn first wave of enemies.
        startRound();
        
        // Trigger the "FLOOR X" start animation.
        showTurnAnim("FLOOR " + stage);
        
        log(">> BOOT SEQUENCE COMPLETE.");
    }

    // FUNCTION: Initializes the User Interface components.
    private void initUI() {
        setTitle("FLOOR " + stage + " | " + cpu.name);
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevents user from closing window during battle.
        setLayout(new BorderLayout()); // Uses Compass layout (North, Center, South).

        // TOP SECTION (Stats)
        JPanel top = new JPanel(new GridLayout(3, 1)); // 3 rows, 1 column.
        
        // Progress Bars configuration
        barHP = new JProgressBar(0, maxHP); 
        barHP.setStringPainted(true); // Shows numbers inside bar.
        barHP.setForeground(Color.GREEN);
        
        barRAM = new JProgressBar(0, maxRAM); 
        barRAM.setStringPainted(true); 
        barRAM.setForeground(Color.CYAN);
        
        // Thread Label configuration
        lblThreads = new JLabel("THREADS READY: " + currentAP + " / " + maxAP, SwingConstants.CENTER);
        lblThreads.setFont(new Font("Consolas", Font.BOLD, 18));
        lblThreads.setForeground(Color.WHITE);
        
        // Add bars to a sub-panel
        JPanel pBar = new JPanel(new GridLayout(2,1)); 
        pBar.add(barHP); 
        pBar.add(barRAM);
        
        top.setBackground(Color.BLACK); 
        top.add(lblThreads); 
        top.add(pBar);
        add(top, BorderLayout.NORTH); // Place at top of window.

        // CENTER SECTION (Graphics)
        gamePanel = new GamePanel();
        gamePanel.setData(cpu, arena); // Pass the data models to the view.
        gamePanel.maxThreads = maxAP; 
        gamePanel.activeThreads = currentAP;
        add(gamePanel, BorderLayout.CENTER); // Place in center.

        // BOTTOM SECTION (Buttons)
        add(createControlPanel(), BorderLayout.SOUTH);
        
        // Force initial update of UI bars.
        updateBars();
    }

    // FUNCTION: Creates the panel containing action buttons.
    private JPanel createControlPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel skills = new JPanel(new GridLayout(2, 4, 5, 5)); // Grid: 2 rows, 4 cols, 5px gap.
        
        // BUTTON CREATION:
        // uses helper method 'mkBtn' to create buttons with Lambda functions for actions.
        
        // FCFS: Basic Attack
        skills.add(mkBtn("FCFS", "Attack (50MB)", () -> act("FCFS", 50)));
        
        // RR: Area of Effect (Hit All)
        skills.add(mkBtn("RR", "AoE (150MB)", () -> act("RR", 150)));
        
        // SJF: Priority Attack (Hits weakest)
        skills.add(mkBtn("SJF", "Snipe (100MB)", () -> act("SJF", 100)));

        // TURBO BOOST (New)
        JButton btnTurbo = mkBtn("TURBO ("+turboCharges+")", "Boost Dmg (2 Turns)", () -> activateTurbo());
        if(turboCharges <= 0) btnTurbo.setEnabled(false); // Disable if out of charges.
        skills.add(btnTurbo);

        // SMT (New Logic)
        JButton btnSmt = mkBtn("SMT (1 Use)", "Double Threads (300MB)", () -> activateSMT());
        // Disable if CPU doesn't support it OR if already used.
        if(!GameState.get().currentCpu.supportsThreads || smtUsed) btnSmt.setEnabled(false);
        skills.add(btnSmt);
        
        // HEAL
        skills.add(mkBtn("HEAL", "Repair (200MB)", () -> act("HEAL", 200)));
        
        // END TURN
        btnEndTurn = mkBtn("END TURN", "Pass Turn", this::endTurn); // Method reference '::'
        btnEndTurn.setBackground(new Color(200, 200, 200));
        skills.add(btnEndTurn);
        
        p.add(skills, BorderLayout.CENTER);
        
        // Log Area (Right side)
        logArea = new JTextArea(5, 30); 
        p.add(new JScrollPane(logArea), BorderLayout.EAST);
        return p;
    }

    // FUNCTION: Main Action Handler
    // 'type': Which skill used. 'baseCost': RAM cost.
    private void act(String type, int baseCost) {
        // 1. Check Threads (Action Points)
        if(currentAP <= 0) { log(">> NO THREADS LEFT!"); return; }
        
        GameState gs = GameState.get();
        
        // 2. RAM Cost Calculation (Includes Undervolt Discount)
        int cost = baseCost - (int)(baseCost * (gs.undervoltVal/100.0));
        if(currentRAM < cost) { log(">> OUT OF MEMORY (Need " + cost + "MB)"); return; }
        
        // 3. Stability Check (Risk mechanic)
        // If undervolted, there is a chance the action fails completely.
        if(Math.random() < (gs.undervoltVal/200.0)) { 
            log(">> KERNEL PANIC (Instability Fail)"); 
            currentAP--; 
            updateBars(); 
            return; 
        }

        // 4. Consume Resources
        currentRAM -= cost;
        currentAP--;
        
        // 5. Calculate Damage
        int baseDmg = gs.calculateBaseDamage(); // Based on CPU GHz.
        if(turboDuration > 0) baseDmg = (int)(baseDmg * 1.5); // Apply 50% Turbo Boost.
        
        // 6. Critical Hit Check (Based on Cache Size)
        boolean crit = Math.random() < gs.getCritChance();
        if(crit) baseDmg *= 2; // Double damage on crit.

        // 7. Execute Skill Logic
        if(type.equals("FCFS") && !arena.isEmpty()) {
            hitMonster(arena.get(0), baseDmg); // Hits index 0 (First in Queue).
        }
        else if(type.equals("RR")) {
            for(Monster m : arena) hitMonster(m, baseDmg/2); // Hits everyone for half dmg.
        }
        else if(type.equals("SJF") && !arena.isEmpty()) {
            // Java Stream API to find monster with lowest HP.
            Monster m = arena.stream().min(Comparator.comparingInt(x->x.currentHP)).orElse(arena.get(0));
            hitMonster(m, (int)(baseDmg * 1.5)); // Bonus damage for logic.
        }
        else if(type.equals("HEAL")) {
            currentHP = Math.min(maxHP, currentHP + 150); // Heals player.
        }
        
        // LOG ACTION FOR GANTT CHART
        logs.add(new GameState.LogEntry(type, System.currentTimeMillis() - startTime, 500, "COMPUTE"));

        if(crit) log(">> CRIT!");
        if(turboDuration > 0) log(">> TURBO BOOST ACTIVE!");
        
        // 8. Cleanup & Update UI
        checkClear(); 
        updateBars(); 
        gamePanel.repaint();
    }
    
    // FUNCTION: Activates Turbo Mode
    private void activateTurbo() {
        if(turboCharges <= 0) return;
        turboCharges--;
        turboDuration = 2; // Set effect for 2 turns.
        log(">> TURBO BOOST ENGAGED! (+50% DAMAGE)");
        
        // Log for Gantt Chart
        logs.add(new GameState.LogEntry("TURBO", System.currentTimeMillis() - startTime, 200, "IO"));
        
        // Refresh UI to disable the button immediately.
        remove(createControlPanel()); add(createControlPanel(), BorderLayout.SOUTH); validate();
    }

    // FUNCTION: Activates Hyperthreading/SMT
    private void activateSMT() {
        if(currentRAM < 300 || smtUsed) return;
        currentRAM -= 300; 
        smtUsed = true; // Lock the skill for rest of battle.
        gamePanel.isHyperThreadingActive=true; // Triggers visual aura.
        maxAP *= 2; // Double max capacity.
        currentAP += maxAP/2; // Give immediate extra points.
        log(">> SMT ENABLED (ONE-TIME BURST).");
        
        // Log for Gantt Chart
        logs.add(new GameState.LogEntry("SMT", System.currentTimeMillis() - startTime, 200, "IO"));
        
        updateBars();
        remove(createControlPanel()); add(createControlPanel(), BorderLayout.SOUTH); validate();
    }
    
    // FUNCTION: Helper to apply damage and trigger animation
    private void hitMonster(Monster m, int d) {
        m.currentHP -= d; 
        m.isHit = true; // Flag for GamePanel to flash enemy white.
        // Timer to reset the 'hit' flag after 150ms.
        Timer t=new Timer(150, e->{m.isHit=false; gamePanel.repaint();}); 
        t.setRepeats(false); 
        t.start();
    }

    // --- AI LOGIC & TURN MANAGEMENT ---
    private void endTurn() {
        // Warning if player skips turn with AP remaining.
        if(currentAP > 0) {
             if(JOptionPane.showConfirmDialog(this, "End Turn?", "Wait", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        }

        // Log Idle time for Gantt
        logs.add(new GameState.LogEntry("IDLE/WAIT", System.currentTimeMillis() - startTime, 1000, "IDLE"));

        GameState gs = GameState.get();
        showTurnAnim("ENEMY PHASE");
        
        // Decrement Turbo timer
        if(turboDuration > 0) turboDuration--;
        
        // DELAYED AI TURN (1.5 seconds) so player sees "Enemy Phase" text.
        Timer t = new Timer(1500, e -> {
            // LOOP: All alive monsters attack
            for(Monster m : arena) {
                if(!m.isDead) {
                    // AI Logic: Deterministic Attack
                    int dmg = (int)(25 * m.type.dmgMult); // Base 25 * Enemy Multiplier.
                    currentHP -= dmg; // Reduce Player HP.
                    log(m.name + " hits: " + dmg + " HP");
                    gamePanel.isPlayerHit=true; // Trigger Screen Shake.
                }
            }
            
            // REGENERATION
            int hpRegen = gs.currentCooler.regen;
            currentHP = Math.min(maxHP, currentHP + hpRegen);
            
            int ramRegen = 100; 
            if(gs.hasECC) ramRegen += 150; // Bonus for ECC RAM.
            currentRAM = Math.min(maxRAM, currentRAM + ramRegen);
            
            // RESET AP
            currentAP = maxAP;
            // Disable SMT bonus after turn (Burst mechanic).
            if(gamePanel.isHyperThreadingActive) { maxAP/=2; currentAP=maxAP; gamePanel.isHyperThreadingActive=false; }
            
            updateBars(); 
            gamePanel.isPlayerHit=false; 
            gamePanel.repaint();
            
            // CHECK DEATH
            if(currentHP<=0) lose();
            else showTurnAnim("PLAYER PHASE");
        });
        t.setRepeats(false); 
        t.start();
    }

    // FUNCTION: Handles Player Death (Roguelike Reset)
    private void lose() {
        // Save Logs
        GameState.get().lastBattleLogs = new ArrayList<>(logs);
        
        JOptionPane.showMessageDialog(this, "SYSTEM CRITICAL FAILURE.\nREBOOTING TO FLOOR 1...");
        GameState.get().currentFloor = 1; // Reset floor progress.
        dispose(); // Close window.
        menuRef.setVisible(true); // Show menu.
        menuRef.dispose(); new MainMenu().setVisible(true); // Re-init menu to update title.
    }

    // FUNCTION: Shows large text overlay (e.g., "ENEMY PHASE")
    private void showTurnAnim(String text) {
        gamePanel.turnOverlayText = text; 
        gamePanel.repaint();
        // Clear text after 1 second.
        Timer t = new Timer(1000, e -> { gamePanel.turnOverlayText = ""; gamePanel.repaint(); }); 
        t.setRepeats(false); 
        t.start();
    }

    // FUNCTION: Spawns monsters based on stage difficulty
    private void startRound() {
        arena.clear();
        int count = 1 + (stage/2); // More enemies on higher floors.
        for(int i=0; i<count; i++) {
            EnemyType t = EnemyType.CALCULATOR;
            // Progressive difficulty logic
            if(stage>3) t = EnemyType.CHROME; 
            if(stage>6) t = EnemyType.ANDROID;
            // Boss spawn on specific rounds
            if(currentRound==totalRounds && i==0) {
                 if(stage==3) t=EnemyType.ZIP_BOMB; 
                 if(stage==6) t=EnemyType.RANSOMWARE; 
                 if(stage==10) t=EnemyType.CYBERPUNK;
            }
            int hp = (int)((40 + stage*10) * t.hpMult); // Calculate HP.
            arena.add(new Monster("P"+i, t, hp));
        }
        gamePanel.repaint();
    }

    // FUNCTION: Checks if current wave is cleared
    private void checkClear() {
        arena.removeIf(m -> m.currentHP<=0); // Remove dead monsters.
        
        if(arena.isEmpty()) {
            if(currentRound<totalRounds) { 
                // Start next wave
                currentRound++; 
                startRound(); 
                showTurnAnim("NEXT WAVE"); 
            }
            else { 
                // STAGE CLEAR: Give Rewards
                // Save Logs
                GameState.get().lastBattleLogs = new ArrayList<>(logs);
                
                int rew = 1000 + stage*500; 
                GameState.get().currency+=rew; 
                GameState.get().currentFloor++; // Increment Floor count.
                
                JOptionPane.showMessageDialog(this, "SECTOR CLEARED! ADVANCING TO FLOOR " + GameState.get().currentFloor);
                dispose(); 
                menuRef.setVisible(true); 
                menuRef.dispose(); new MainMenu().setVisible(true);
            }
        }
    }

    // HELPER: Updates the visual progress bars
    private void updateBars() {
        barHP.setValue(currentHP); barHP.setString(currentHP+"/"+maxHP);
        barRAM.setValue(currentRAM); barRAM.setString(currentRAM+"/"+maxRAM+" MB");
        lblThreads.setText("THREADS: " + currentAP + " / " + maxAP);
        gamePanel.activeThreads=currentAP; gamePanel.maxThreads=maxAP;
        
        // Visual warning: Turn button red if AP remains
        if(currentAP > 0) btnEndTurn.setBackground(new Color(255, 100, 100));
        else btnEndTurn.setBackground(new Color(200, 200, 200));
    }

    // HELPER: Factory method to create styled buttons
    private JButton mkBtn(String t, String tip, Runnable r) { 
        JButton b=new JButton(t); 
        b.setToolTipText(tip); 
        b.addActionListener(e->r.run()); 
        return b; 
    }
    
    // HELPER: Adds text to the side log
    private void log(String s) { logArea.append(s+"\n"); }
}