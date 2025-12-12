import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    public MainMenu() {
        setTitle("CYBER ARCHITECT: ROGUELIKE EDITION");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("SYSTEM BIOS v5.4 [ROGUELIKE]", SwingConstants.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 36));
        header.setForeground(new Color(255, 100, 50));
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30,50,50,50));

        // UPDATED: Shows current floor on the button
        GameState gs = GameState.get();
        JButton btnDeploy = createBigBtn("⚔️ BOOT OS (FLOOR " + gs.currentFloor + ")", "Continue Run", this::deployRun);
        
        menuPanel.add(btnDeploy);
        menuPanel.add(createBigBtn("🛠️ SYSTEM CONFIG", "Assemble & Power", this::openConfigurator));
        menuPanel.add(createBigBtn("🛒 COMPONENT SHOP", "Buy Parts", this::openShop));
        menuPanel.add(createBigBtn("🎓 CPU EDUCATION", "Scheduling Simulator", this::openEducation)); 
        menuPanel.add(createBigBtn("📊 LOGS", "Gantt Charts", this::openLogs));

        add(menuPanel, BorderLayout.CENTER);
        updateTitle();
    }

    private void updateTitle() {
        GameState gs = GameState.get();
        int watts = gs.getTotalWatts();
        int maxW = gs.currentPsu.maxWatts;
        String powerStr = watts + "/" + maxW + "W";
        setTitle("BUILD: " + gs.currentCpu.label + " | PWR: " + powerStr + " | FLOOR: " + gs.currentFloor + " | FUNDS: $" + gs.currency);
    }

    private JButton createBigBtn(String t, String s, Runnable r) {
        JButton b = new JButton("<html><center><h1>"+t+"</h1>"+s+"</center></html>");
        b.addActionListener(e -> r.run()); return b;
    }

    // --- ROGUELIKE DEPLOY ---
    private void deployRun() {
        GameState gs = GameState.get();
        if(gs.getTotalWatts() > gs.currentPsu.maxWatts) {
            JOptionPane.showMessageDialog(this, "POST FAILURE: PSU OVERLOAD!\nUpgrade PSU or Downclock.", "BOOT ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.setVisible(false);
        // Direct launch to current floor
        new CyberQuestRPG(gs.currentFloor, this).setVisible(true);
    }

    private void openConfigurator() {
        JDialog d = new JDialog(this, "SYSTEM CONFIGURATION", true);
        d.setSize(1000, 750);
        d.setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        
        tabs.addTab("ASSEMBLY", createAssemblyPanel(d));
        tabs.addTab("UPGRADES", createUpgradesPanel(d));
        tabs.addTab("BIOS TUNING", createBiosPanel());

        d.add(tabs, BorderLayout.CENTER);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        updateTitle();
    }

    private JPanel createAssemblyPanel(JDialog d) {
        JPanel p = new JPanel(new GridLayout(6, 1));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GameState gs = GameState.get();

        p.add(createCombo("POWER SUPPLY", gs.currentPsu, Hardware.PsuType.values(), ps -> gs.hasPsu(ps), o -> { gs.currentPsu=(Hardware.PsuType)o; d.repaint(); }));
        p.add(createCombo("CPU", gs.currentCpu, Hardware.CpuType.values(), c -> gs.hasCpu(c), o -> { gs.currentCpu=(Hardware.CpuType)o; d.repaint(); }));
        p.add(createCombo("GPU", gs.currentGpu, Hardware.GpuType.values(), g -> gs.hasGpu(g), o -> { gs.currentGpu=(Hardware.GpuType)o; d.repaint(); }));
        p.add(createCombo("COOLING", gs.currentCooler, Hardware.CoolerType.values(), c -> gs.hasCooler(c), o -> { gs.currentCooler=(Hardware.CoolerType)o; d.repaint(); }));

        // Power Meter
        int currentW = gs.getTotalWatts();
        int maxW = gs.currentPsu.maxWatts;
        JProgressBar powerBar = new JProgressBar(0, maxW);
        powerBar.setValue(currentW);
        powerBar.setStringPainted(true);
        powerBar.setString("POWER: " + currentW + "W / " + maxW + "W");
        powerBar.setForeground(currentW > maxW ? Color.RED : Color.GREEN);
        
        JPanel pwr = new JPanel(new BorderLayout()); pwr.add(new JLabel("LOAD:"), BorderLayout.NORTH); pwr.add(powerBar, BorderLayout.CENTER);
        p.add(pwr);

        JTextArea info = new JTextArea("STATS:\nBase HP: "+gs.getCurrentMaxHP()+"\nRAM: "+gs.getCurrentRamMB()+" MB\nRegen: "+gs.currentCooler.regen);
        info.setEditable(false); p.add(info);
        return p;
    }

    private JPanel createUpgradesPanel(JDialog d) {
        JPanel p = new JPanel(new GridLayout(4,1)); 
        GameState gs = GameState.get();

        // RAM (CAPPED)
        int nextRam = gs.ramIndex + 1; 
        boolean ramMaxed = nextRam > gs.MAX_RAM_INDEX;
        String ramTxt = ramMaxed ? "RAM MAXED ("+gs.getCurrentRamMB()+"MB)" : "RAM: "+gs.getCurrentRamMB()+"MB -> "+gs.RAM_TIERS[nextRam]+"MB ($500)";
        JButton bRam = new JButton(ramTxt);
        bRam.setEnabled(!ramMaxed);
        bRam.addActionListener(e->{if(gs.currency>=500){gs.currency-=500;gs.ramIndex++; d.repaint(); openConfigurator();}});
        p.add(bRam);
        
        // STORAGE (CAPPED)
        boolean storeMaxed = gs.storageLevel >= gs.MAX_STORAGE_LEVEL;
        String stoTxt = storeMaxed ? "SSD MAXED (Tier "+gs.MAX_STORAGE_LEVEL+")" : "SSD TIER "+gs.storageLevel+" (+HP) ($300)";
        JButton bSto = new JButton(stoTxt);
        bSto.setEnabled(!storeMaxed);
        bSto.addActionListener(e->{if(gs.currency>=300){gs.currency-=300;gs.storageLevel++;d.repaint(); openConfigurator();}});
        p.add(bSto);
        
        // ECC
        JButton bEcc=new JButton(gs.hasECC?"ECC INSTALLED":"BUY ECC RAM ($1000)"); bEcc.setEnabled(!gs.hasECC);
        bEcc.addActionListener(e->{if(gs.currency>=1000){gs.currency-=1000;gs.hasECC=true;d.repaint(); openConfigurator();}});
        p.add(bEcc);
        return p;
    }
    
    private JPanel createBiosPanel() {
        JPanel p=new JPanel(new GridLayout(2,1)); GameState gs=GameState.get();
        JSlider sOC=new JSlider(0,100,gs.overclockVal); p.add(new JLabel("Overclock % (Increases Watts!)")); p.add(sOC); sOC.addChangeListener(e->gs.overclockVal=sOC.getValue());
        JSlider sUV=new JSlider(0,100,gs.undervoltVal); p.add(new JLabel("Undervolt % (Unstable!)")); p.add(sUV); sUV.addChangeListener(e->gs.undervoltVal=sUV.getValue());
        return p;
    }

    private <T> JPanel createCombo(String l, T c, T[] v, java.util.function.Predicate<T> f, java.util.function.Consumer<Object> a) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); p.add(new JLabel(l+": "));
        JComboBox<T> box = new JComboBox<>(); for(T i : v) if(f.test(i)) box.addItem(i);
        box.setSelectedItem(c); box.addActionListener(e -> a.accept(box.getSelectedItem())); p.add(box); return p;
    }

    private void openShop() {
        JDialog d = new JDialog(this, "NEWEGG...ISH", true); d.setSize(800, 600); d.setLayout(new GridLayout(0, 2));
        GameState gs = GameState.get();
        createShopSection(d, "CPUs", Hardware.CpuType.values(), c -> c.cost, c -> " ("+c.watts+"W)", i -> gs.addCpu((Hardware.CpuType)i));
        createShopSection(d, "GPUs", Hardware.GpuType.values(), c -> c.cost, c -> " ("+c.watts+"W)", i -> gs.addGpu((Hardware.GpuType)i));
        createShopSection(d, "Coolers", Hardware.CoolerType.values(), c -> c.cost, c -> "", i -> gs.addCooler((Hardware.CoolerType)i));
        createShopSection(d, "PSUs", Hardware.PsuType.values(), c -> c.cost, c -> " ("+c.maxWatts+"W)", i -> gs.addPsu((Hardware.PsuType)i));
        d.setVisible(true);
    }
    
    private <T> void createShopSection(JDialog d, String title, T[] items, java.util.function.Function<T,Integer> costFunc, java.util.function.Function<T,String> infoFunc, java.util.function.Consumer<T> buyAction) {
        for(T item : items) {
            int cost = costFunc.apply(item);
            if(cost == 0) continue; 
            JButton b = new JButton(item.toString() + infoFunc.apply(item) + " - $" + cost);
            b.addActionListener(e -> {
                if(GameState.get().currency >= cost) {
                    GameState.get().currency -= cost;
                    buyAction.accept(item);
                    JOptionPane.showMessageDialog(d, "Purchased!");
                    updateTitle();
                }
            });
            d.add(b);
        }
    }

    private void openLogs() { new JFrame("LOGS").add(new GanttChartPanel()); }
    private void openEducation() { JFrame f=new JFrame("SIMULATOR"); f.setSize(800,500); f.add(new SchedulerSimulator()); f.setVisible(true); }
}