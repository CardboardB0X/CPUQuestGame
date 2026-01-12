import javax.swing.*;
import java.awt.*;

// MAIN MENU CLASS
// This class inherits from JFrame, meaning it is a window application.
// It serves as the central hub where the player builds their PC, buys parts, and starts the game.
public class MainMenu extends JFrame {

    // MAIN METHOD: The entry point of the Java application.
    public static void main(String[] args) {
        // OPTIONAL: Sets the UI look to match the operating system (Windows/Mac/Linux style).
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        
        // CRITICAL: Swing is not thread-safe. All UI creation must happen on the 
        // Event Dispatch Thread (EDT). 'invokeLater' ensures this happens correctly.
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    // CONSTRUCTOR: Sets up the main window properties and layout.
    public MainMenu() {
        setTitle("CYBER ARCHITECT: ROGUELIKE EDITION");
        setSize(1200, 800); // Sets window dimensions in pixels.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Terminates the program when the "X" is clicked.
        setLocationRelativeTo(null); // Centers the window on the user's screen.
        setLayout(new BorderLayout()); // Uses BorderLayout (North, South, East, West, Center) for positioning.

        // HEADER LABEL (North)
        JLabel header = new JLabel("SYSTEM BIOS v5.4 [ROGUELIKE]", SwingConstants.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 36)); // Monospaced font for a "tech" feel.
        header.setForeground(new Color(255, 100, 50)); // Custom orange-red color.
        add(header, BorderLayout.NORTH);

        // BUTTON PANEL (Center)
        // GridLayout(3, 2, 20, 20) means 3 rows, 2 columns, with 20px gaps between items.
        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        // Adds padding around the edges of the panel so buttons aren't touching the window border.
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30,50,50,50));

        // DYNAMIC BUTTON: "Boot OS"
        // We fetch the 'currentFloor' from GameState to display progress directly on the button.
        GameState gs = GameState.get();
        JButton btnDeploy = createBigBtn("⚔️ BOOT OS (FLOOR " + gs.currentFloor + ")", "Continue Run", this::deployRun);
        
        // Add all main navigation buttons to the grid
        menuPanel.add(btnDeploy);
        menuPanel.add(createBigBtn("🛠️ SYSTEM CONFIG", "Assemble & Power", this::openConfigurator));
        menuPanel.add(createBigBtn("🛒 COMPONENT SHOP", "Buy Parts", this::openShop));
        menuPanel.add(createBigBtn("🎓 CPU EDUCATION", "Scheduling Simulator", this::openEducation)); 
        menuPanel.add(createBigBtn("📊 LOGS", "Gantt Charts", this::openLogs));

        add(menuPanel, BorderLayout.CENTER);
        
        // Updates the window title bar with current currency and hardware info.
        updateTitle();
    }

    // HELPER: Updates the JFrame title text.
    // Called whenever an upgrade is bought or part is swapped to keep info fresh.
    private void updateTitle() {
        GameState gs = GameState.get();
        int watts = gs.getTotalWatts();
        int maxW = gs.currentPsu.maxWatts;
        String powerStr = watts + "/" + maxW + "W";
        
        // Displays: CPU Name | Power Usage / Capacity | Floor # | Current Money
        setTitle("BUILD: " + gs.currentCpu.label + " | PWR: " + powerStr + " | FLOOR: " + gs.currentFloor + " | FUNDS: $" + gs.currency);
    }

    // HELPER: Factory method to create consistent, styled buttons.
    // Uses HTML inside the label to allow multi-line text (Heading + Subtitle).
    private JButton createBigBtn(String t, String s, Runnable r) {
        JButton b = new JButton("<html><center><h1>"+t+"</h1>"+s+"</center></html>");
        // 'r.run()' executes the lambda function passed as an argument when clicked.
        b.addActionListener(e -> r.run()); 
        return b;
    }

    // --- ACTION: START GAME (ROGUELIKE RUN) ---
    private void deployRun() {
        GameState gs = GameState.get();
        
        // POWER CHECK: The core constraint of the game.
        // If components draw more power than the PSU supplies, deployment fails.
        if(gs.getTotalWatts() > gs.currentPsu.maxWatts) {
            JOptionPane.showMessageDialog(this, "POST FAILURE: PSU OVERLOAD!\nUpgrade PSU or Downclock.", "BOOT ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.setVisible(false); // Hide the Main Menu
        // Create the Battle Window (CyberQuestRPG) and show it.
        // We pass 'this' (MainMenu) so the game can re-open the menu when the battle ends.
        new CyberQuestRPG(gs.currentFloor, this).setVisible(true);
    }

    // --- ACTION: OPEN CONFIGURATION WINDOW ---
    // Opens a modal dialog with Tabs for Assembly, Upgrades, and BIOS.
    private void openConfigurator() {
        JDialog d = new JDialog(this, "SYSTEM CONFIGURATION", true); // 'true' = Modal (blocks interaction with main window)
        d.setSize(1000, 750);
        d.setLayout(new BorderLayout());

        // JTabbedPane allows switching between different panels (pages) in the same window.
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        
        tabs.addTab("ASSEMBLY", createAssemblyPanel(d));  // Select Parts
        tabs.addTab("UPGRADES", createUpgradesPanel(d)); // RAM/Storage Levels
        tabs.addTab("BIOS TUNING", createBiosPanel());   // Overclocking Sliders

        d.add(tabs, BorderLayout.CENTER);
        d.setLocationRelativeTo(this); // Centers popup over main window
        d.setVisible(true);
        
        // Refresh title when dialog closes (in case stats changed)
        updateTitle();
    }

    // --- PANEL 1: ASSEMBLY (Part Selection) ---
    private JPanel createAssemblyPanel(JDialog d) {
        JPanel p = new JPanel(new GridLayout(6, 1));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GameState gs = GameState.get();

        // 1. COMPONENT SELECTORS (Dropdowns)
        // Uses 'createCombo' helper to create dropdowns that only show *owned* items.
        // The lambda 'ps -> gs.hasPsu(ps)' is a filter: True if owned, False if not.
        p.add(createCombo("POWER SUPPLY", gs.currentPsu, Hardware.PsuType.values(), ps -> gs.hasPsu(ps), o -> { gs.currentPsu=(Hardware.PsuType)o; d.repaint(); }));
        p.add(createCombo("CPU", gs.currentCpu, Hardware.CpuType.values(), c -> gs.hasCpu(c), o -> { gs.currentCpu=(Hardware.CpuType)o; d.repaint(); }));
        p.add(createCombo("GPU", gs.currentGpu, Hardware.GpuType.values(), g -> gs.hasGpu(g), o -> { gs.currentGpu=(Hardware.GpuType)o; d.repaint(); }));
        p.add(createCombo("COOLING", gs.currentCooler, Hardware.CoolerType.values(), c -> gs.hasCooler(c), o -> { gs.currentCooler=(Hardware.CoolerType)o; d.repaint(); }));

        // 2. POWER METER (Visual Feedback)
        int currentW = gs.getTotalWatts();
        int maxW = gs.currentPsu.maxWatts;
        JProgressBar powerBar = new JProgressBar(0, maxW);
        powerBar.setValue(currentW); // Current Load
        powerBar.setStringPainted(true);
        powerBar.setString("POWER: " + currentW + "W / " + maxW + "W");
        
        // Dynamic Coloring: Red if overloaded, Green if safe.
        powerBar.setForeground(currentW > maxW ? Color.RED : Color.GREEN);
        
        JPanel pwr = new JPanel(new BorderLayout()); 
        pwr.add(new JLabel("LOAD:"), BorderLayout.NORTH); 
        pwr.add(powerBar, BorderLayout.CENTER);
        p.add(pwr);

        // 3. STATS SUMMARY
        JTextArea info = new JTextArea("STATS:\nBase HP: "+gs.getCurrentMaxHP()+"\nRAM: "+gs.getCurrentRamMB()+" MB\nRegen: "+gs.currentCooler.regen);
        info.setEditable(false); 
        p.add(info);
        
        return p;
    }

    // --- PANEL 2: UPGRADES (Permanent Stats) ---
    private JPanel createUpgradesPanel(JDialog d) {
        JPanel p = new JPanel(new GridLayout(4,1)); 
        GameState gs = GameState.get();

        // RAM UPGRADE
        // Checks array bounds to ensure we don't upgrade past 32GB
        int nextRam = gs.ramIndex + 1; 
        boolean ramMaxed = nextRam > gs.MAX_RAM_INDEX;
        String ramTxt = ramMaxed ? "RAM MAXED ("+gs.getCurrentRamMB()+"MB)" : "RAM: "+gs.getCurrentRamMB()+"MB -> "+gs.RAM_TIERS[nextRam]+"MB ($500)";
        
        JButton bRam = new JButton(ramTxt);
        bRam.setEnabled(!ramMaxed); // Gray out button if maxed
        bRam.addActionListener(e->{
            if(gs.currency>=500){
                gs.currency-=500;
                gs.ramIndex++; 
                d.repaint(); 
                openConfigurator(); // Re-opens panel to update button text
            }
        });
        p.add(bRam);
        
        // STORAGE UPGRADE (HP)
        boolean storeMaxed = gs.storageLevel >= gs.MAX_STORAGE_LEVEL;
        String stoTxt = storeMaxed ? "SSD MAXED (Tier "+gs.MAX_STORAGE_LEVEL+")" : "SSD TIER "+gs.storageLevel+" (+HP) ($300)";
        JButton bSto = new JButton(stoTxt);
        bSto.setEnabled(!storeMaxed);
        bSto.addActionListener(e->{if(gs.currency>=300){gs.currency-=300;gs.storageLevel++;d.repaint(); openConfigurator();}});
        p.add(bSto);
        
        // ECC (One-time purchase)
        JButton bEcc=new JButton(gs.hasECC?"ECC INSTALLED":"BUY ECC RAM ($1000)"); 
        bEcc.setEnabled(!gs.hasECC);
        bEcc.addActionListener(e->{if(gs.currency>=1000){gs.currency-=1000;gs.hasECC=true;d.repaint(); openConfigurator();}});
        p.add(bEcc);
        
        return p;
    }
    
    // --- PANEL 3: BIOS (Sliders) ---
    private JPanel createBiosPanel() {
        JPanel p=new JPanel(new GridLayout(2,1)); 
        GameState gs=GameState.get();
        
        // OVERCLOCK SLIDER (0-100%)
        JSlider sOC=new JSlider(0,100,gs.overclockVal); 
        p.add(new JLabel("Overclock % (Increases Watts!)")); 
        p.add(sOC); 
        // Updates GameState immediately when slider moves
        sOC.addChangeListener(e->gs.overclockVal=sOC.getValue());
        
        // UNDERVOLT SLIDER (0-100%)
        JSlider sUV=new JSlider(0,100,gs.undervoltVal); 
        p.add(new JLabel("Undervolt % (Unstable!)")); 
        p.add(sUV); 
        sUV.addChangeListener(e->gs.undervoltVal=sUV.getValue());
        
        return p;
    }

    // --- HELPER: GENERIC COMBO BOX CREATOR ---
    // This creates a dropdown <T> that is strictly typed (e.g., only CPU Enums or GPU Enums).
    // filter: A function determining which items to show.
    // action: A function to run when an item is selected.
    private <T> JPanel createCombo(String l, T c, T[] v, java.util.function.Predicate<T> f, java.util.function.Consumer<Object> a) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        p.add(new JLabel(l+": "));
        JComboBox<T> box = new JComboBox<>(); 
        
        // Populate dropdown only with valid items (e.g., Owned parts)
        for(T i : v) if(f.test(i)) box.addItem(i);
        
        box.setSelectedItem(c); // Set current active item
        box.addActionListener(e -> a.accept(box.getSelectedItem())); // Run logic on change
        p.add(box); 
        return p;
    }

    // --- SHOP SYSTEM ---
    private void openShop() {
        JDialog d = new JDialog(this, "NEWEGG...ISH", true); 
        d.setSize(800, 600); 
        d.setLayout(new GridLayout(0, 2)); // 2 Columns
        GameState gs = GameState.get();
        
        // Calls a generic helper to create shop buttons for each Enum type.
        // This dramatically reduces code duplication.
        createShopSection(d, "CPUs", Hardware.CpuType.values(), c -> c.cost, c -> " ("+c.watts+"W)", i -> gs.addCpu((Hardware.CpuType)i));
        createShopSection(d, "GPUs", Hardware.GpuType.values(), c -> c.cost, c -> " ("+c.watts+"W)", i -> gs.addGpu((Hardware.GpuType)i));
        createShopSection(d, "Coolers", Hardware.CoolerType.values(), c -> c.cost, c -> "", i -> gs.addCooler((Hardware.CoolerType)i));
        createShopSection(d, "PSUs", Hardware.PsuType.values(), c -> c.cost, c -> " ("+c.maxWatts+"W)", i -> gs.addPsu((Hardware.PsuType)i));
        
        d.setVisible(true);
    }
    
    // GENERIC SHOP CREATOR
    // Takes a list of items (e.g., all CPUs) and creates a "Buy" button for each.
    private <T> void createShopSection(JDialog d, String title, T[] items, java.util.function.Function<T,Integer> costFunc, java.util.function.Function<T,String> infoFunc, java.util.function.Consumer<T> buyAction) {
        for(T item : items) {
            int cost = costFunc.apply(item); // Get cost dynamically
            if(cost == 0) continue; // Don't sell "Default/Stock" items
            
            JButton b = new JButton(item.toString() + infoFunc.apply(item) + " - $" + cost);
            b.addActionListener(e -> {
                if(GameState.get().currency >= cost) {
                    GameState.get().currency -= cost;
                    buyAction.accept(item); // Add to inventory
                    JOptionPane.showMessageDialog(d, "Purchased!");
                    updateTitle(); // Update money display
                }
            });
            d.add(b);
        }
    }

    // --- SUB-WINDOWS ---
    private void openLogs() { JFrame f = new JFrame("BATTLE LOGS");
        f.setSize(800, 500); 
        f.add(new GanttChartPanel());
        f.setLocationRelativeTo(this);
        f.setVisible(true); }
    private void openEducation() { JFrame f=new JFrame("SIMULATOR"); f.setSize(800,500); f.add(new SchedulerSimulator()); f.setVisible(true); }
}