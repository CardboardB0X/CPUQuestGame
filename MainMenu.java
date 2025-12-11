import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    public MainMenu() {
        setTitle("CPU QUEST: ARCHITECT EDITION");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("SYSTEM BIOS v3.0", SwingConstants.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 40));
        header.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));
        add(header, BorderLayout.NORTH);

        // Buttons
        JPanel menuPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30,50,50,50));

        menuPanel.add(createBtn("⚔️ START MISSION", "Deploy to Stages 1-5", this::openStageSelect));
        menuPanel.add(createBtn("⚡ UPGRADES", "Install Cache, Cooling, RAM...", this::openUpgrades));
        menuPanel.add(createBtn("🛒 COMPONENT SHOP", "Motherboards & GPUs", this::openShop));
        menuPanel.add(createBtn("📊 BATTLE LOGS", "Analyze Scheduler Data", this::openLogs));

        add(menuPanel, BorderLayout.CENTER);
        updateTitle();
    }

    private JButton createBtn(String title, String sub, Runnable action) {
        JButton b = new JButton("<html><center><h2>" + title + "</h2>" + sub + "</center></html>");
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }

    private void updateTitle() {
        setTitle("CPU QUEST | Funds: $" + GameState.get().currency);
    }

    private void openUpgrades() {
        JDialog d = new JDialog(this, "HARDWARE UPGRADES", true);
        d.setSize(600, 600);
        d.setLayout(new GridLayout(6, 1)); // Increased rows for new items
        GameState gs = GameState.get();

        // Standard Stats
        d.add(createUpgradeRow("CLOCK SPEED (Dmg)", gs.levelFreq, 150, () -> {
            if(gs.currency >= 150) { gs.currency-=150; gs.levelFreq++; }
        }, d));

        d.add(createUpgradeRow("RAM CAPACITY (Max AP)", gs.levelRAM, 150, () -> {
            if(gs.currency >= 150) { gs.currency-=150; gs.levelRAM++; }
        }, d));

        d.add(createUpgradeRow("SSD STORAGE (Max HP)", gs.levelStorage, 200, () -> {
            if(gs.currency >= 200) { gs.currency-=200; gs.levelStorage++; }
        }, d));
        
        // New Hardware
        d.add(createUpgradeRow("L1 CACHE (Crit %)", gs.levelCache, 300, () -> {
            if(gs.currency >= 300) { gs.currency-=300; gs.levelCache++; }
        }, d));

        d.add(createUpgradeRow("LIQUID COOLING (Regen)", gs.levelCooling, 250, () -> {
            if(gs.currency >= 250) { gs.currency-=250; gs.levelCooling++; }
        }, d));

        d.add(new JLabel("FUNDS: $" + gs.currency, SwingConstants.CENTER));
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        updateTitle();
    }

    private JPanel createUpgradeRow(String name, int lvl, int cost, Runnable action, JDialog d) {
        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel(name + " [Lv " + lvl + "] "));
        JButton b = new JButton("UPGRADE ($" + cost + ")");
        b.addActionListener(e -> { action.run(); d.dispose(); openUpgrades(); });
        p.add(b);
        return p;
    }

    private void openShop() {
        JDialog d = new JDialog(this, "COMPONENT SHOP", true);
        d.setSize(600, 300);
        d.setLayout(new GridLayout(2, 1));
        GameState gs = GameState.get();

        JButton btnMobo = new JButton("<html><center><h3>Z790 MOTHERBOARD ($1200)</h3>Unlocks: SMT (HyperThreading)</center></html>");
        if(gs.unlockHyperThreading) { btnMobo.setText("INSTALLED"); btnMobo.setEnabled(false); }
        else { btnMobo.setEnabled(gs.currency >= 1200); }
        
        btnMobo.addActionListener(e -> {
            gs.currency -= 1200; gs.unlockHyperThreading = true;
            JOptionPane.showMessageDialog(d, "SMT UNLOCKED!"); d.dispose(); openShop();
        });

        JButton btnGpu = new JButton("<html><center><h3>RTX 4090 SKIN ($800)</h3>Unlocks: Elite Background</center></html>");
        if(gs.currentGPUSkin.contains("RTX")) { btnGpu.setText("INSTALLED"); btnGpu.setEnabled(false); }
        else { btnGpu.setEnabled(gs.currency >= 800); }
        
        btnGpu.addActionListener(e -> {
            gs.currency -= 800; gs.currentGPUSkin = "RTX 4090";
            d.dispose(); openShop();
        });

        d.add(btnMobo); d.add(btnGpu);
        d.setLocationRelativeTo(this); d.setVisible(true); updateTitle();
    }

    private void openLogs() {
        JFrame f = new JFrame("SCHEDULER LOGS");
        f.setSize(800, 450);
        f.add(new GanttChartPanel());
        f.setLocationRelativeTo(this);
        f.setVisible(true);
    }

    private void openStageSelect() {
        String[] options = {
            "Stage 1: Boot Sector (Easy)", 
            "Stage 2: Kernel Space (Medium)", 
            "Stage 3: The Cloud (Hard)",
            "Stage 4: Dark Web (Very Hard)",
            "Stage 5: Mainframe Core (Extreme)"
        };
        
        int choice = JOptionPane.showOptionDialog(this, "SELECT DEPLOYMENT ZONE", "STAGES", 
                0, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        
        if (choice != -1) {
            CPUProfile baseCpu = new CPUProfile("Hero CPU", Architecture.X86_64, 3, 200, 100, Color.CYAN);
            this.setVisible(false);
            new CyberQuestRPG(baseCpu, choice + 1, this).setVisible(true);
        }
    }
}