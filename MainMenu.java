import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    public MainMenu() {
        setTitle("CYBER ARCHITECT: ULTIMATE EDITION");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("SYSTEM BIOS v5.1 [EDU]", SwingConstants.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 36));
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30,50,50,50));

        menuPanel.add(createBigBtn("⚔️ BOOT OS (BATTLE)", "Stages 1-10", this::openStageSelect));
        menuPanel.add(createBigBtn("🛠️ SYSTEM CONFIG", "Assembly & Upgrades", this::openConfigurator));
        menuPanel.add(createBigBtn("🛒 HARDWARE SHOP", "Buy Components", this::openShop));
        menuPanel.add(createBigBtn("🎓 CPU EDUCATION", "Scheduling Simulator", this::openEducation)); // NEW
        menuPanel.add(createBigBtn("📊 LOGS", "Gantt Charts", this::openLogs));

        add(menuPanel, BorderLayout.CENTER);
        updateTitle();
    }

    private void updateTitle() {
        GameState gs = GameState.get();
        setTitle("CPU: " + gs.currentCpu.label + " ("+gs.currentCpu.freqGHz+"GHz) | GPU: " + gs.currentGpu.label + " | $: " + gs.currency);
    }

    private JButton createBigBtn(String t, String s, Runnable r) {
        JButton b = new JButton("<html><center><h1>"+t+"</h1>"+s+"</center></html>");
        b.addActionListener(e -> r.run()); return b;
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
        tabs.addTab("KERNEL SKILLS", createSkillPanel());

        d.add(tabs, BorderLayout.CENTER);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        updateTitle();
    }

    private JPanel createAssemblyPanel(JDialog d) {
        JPanel p = new JPanel(new GridLayout(7, 1)); // Increased rows
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GameState gs = GameState.get();

        p.add(createCombo("MOTHERBOARD", gs.currentMobo, Hardware.MoboType.values(), m -> gs.hasMobo(m), o -> { gs.currentMobo=(Hardware.MoboType)o; d.repaint(); }));
        p.add(createCombo("CPU", gs.currentCpu, Hardware.CpuType.values(), c -> gs.getCpuCount(c)>0, o -> { gs.currentCpu=(Hardware.CpuType)o; gs.useDualCpu=false; d.repaint(); }));
        
        // GPU SELECTOR
        p.add(createCombo("GPU (VIDEO)", gs.currentGpu, Hardware.GpuType.values(), 
            g -> g==Hardware.GpuType.INTEGRATED || gs.currency >= 0, // Simplified owned check
            o -> { gs.currentGpu=(Hardware.GpuType)o; d.repaint(); }));

        p.add(createCombo("STORAGE", gs.currentStorage, Hardware.StorageType.values(), s -> true, o -> { gs.currentStorage=(Hardware.StorageType)o; d.repaint(); }));
        p.add(createCombo("COOLING", gs.currentCooler, Hardware.CoolerType.values(), c -> c==Hardware.CoolerType.STOCK || gs.currency>=0, o -> { gs.currentCooler=(Hardware.CoolerType)o; d.repaint(); }));

        JCheckBox chkDual = new JCheckBox("ENABLE DUAL CPU (Requires Server Board + 2 CPUs)");
        chkDual.setEnabled(gs.currentMobo.isDualSocket && gs.getCpuCount(gs.currentCpu) >= 2);
        chkDual.setSelected(gs.useDualCpu);
        chkDual.addActionListener(e -> gs.useDualCpu = chkDual.isSelected());
        p.add(chkDual);

        JTextArea info = new JTextArea("STATS: Freq: "+gs.currentCpu.freqGHz+"GHz | Cache: "+gs.currentCpu.l3CacheMB+"MB | GPU Style: "+gs.currentGpu.visualStyle);
        info.setEditable(false); p.add(info);
        return p;
    }

    // ... (Keep createUpgradesPanel, createBiosPanel, createSkillPanel SAME as v5) ...
    // Reuse specific code blocks from previous response for brevity, they are unchanged except ensuring they import/use GameState correctly.
    
    private JPanel createUpgradesPanel(JDialog d) {
        JPanel p = new JPanel(new GridLayout(4,1)); GameState gs=GameState.get();
        // RAM
        int next = gs.ramIndex+1; boolean can=next<gs.RAM_TIERS.length && gs.RAM_TIERS[next]<=gs.currentMobo.maxRamMB;
        JButton bRam=new JButton("RAM: "+gs.getCurrentRamMB()+"MB"+(can?" -> "+gs.RAM_TIERS[next]+"MB ($500)":" MAX"));
        bRam.setEnabled(can); bRam.addActionListener(e->{if(gs.currency>=500){gs.currency-=500;gs.ramIndex++; d.repaint(); openConfigurator();}});
        p.add(bRam);
        // Storage
        JButton bSto=new JButton("STORAGE Lv "+gs.storageLevel+"/15 ($300)"); bSto.setEnabled(gs.storageLevel<15);
        bSto.addActionListener(e->{if(gs.currency>=300){gs.currency-=300;gs.storageLevel++;d.repaint(); openConfigurator();}});
        p.add(bSto);
        // ECC
        JButton bEcc=new JButton(gs.hasECC?"ECC INSTALLED":"BUY ECC ($1000)"); bEcc.setEnabled(!gs.hasECC);
        bEcc.addActionListener(e->{if(gs.currency>=1000){gs.currency-=1000;gs.hasECC=true;d.repaint(); openConfigurator();}});
        p.add(bEcc);
        return p;
    }
    
    private JPanel createBiosPanel() {
        JPanel p=new JPanel(new GridLayout(2,1)); GameState gs=GameState.get();
        JSlider sOC=new JSlider(0,100,gs.overclockVal); p.add(new JLabel("Overclock %")); p.add(sOC); sOC.addChangeListener(e->gs.overclockVal=sOC.getValue());
        JSlider sUV=new JSlider(0,100,gs.undervoltVal); p.add(new JLabel("Undervolt %")); p.add(sUV); sUV.addChangeListener(e->gs.undervoltVal=sUV.getValue());
        return p;
    }
    
    private JPanel createSkillPanel() {
        JPanel p=new JPanel(new GridLayout(3,1)); GameState gs=GameState.get();
        p.add(createSkillRow("FCFS", gs.skillLvlFCFS, ()->{if(gs.currency>=200&&gs.skillLvlFCFS<15){gs.currency-=200;gs.skillLvlFCFS++;}}));
        p.add(createSkillRow("RR", gs.skillLvlRR, ()->{if(gs.currency>=300&&gs.skillLvlRR<15){gs.currency-=300;gs.skillLvlRR++;}}));
        p.add(createSkillRow("SJF", gs.skillLvlSJF, ()->{if(gs.currency>=250&&gs.skillLvlSJF<15){gs.currency-=250;gs.skillLvlSJF++;}}));
        return p;
    }
    
    private JPanel createSkillRow(String n, int l, Runnable r){
        JPanel p=new JPanel(new BorderLayout()); p.add(new JLabel(n+" Lv"+l),BorderLayout.CENTER);
        JButton b=new JButton("UPGRADE"); if(l>=15)b.setEnabled(false); b.addActionListener(e->{r.run(); openConfigurator();});
        p.add(b,BorderLayout.EAST); return p;
    }

    private <T> JPanel createCombo(String l, T c, T[] v, java.util.function.Predicate<T> f, java.util.function.Consumer<Object> a) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); p.add(new JLabel(l+": "));
        JComboBox<T> box = new JComboBox<>(); for(T i : v) if(f.test(i)) box.addItem(i);
        box.setSelectedItem(c); box.addActionListener(e -> a.accept(box.getSelectedItem())); p.add(box); return p;
    }

    private void openShop() {
        JDialog d = new JDialog(this, "HARDWARE SHOP", true); d.setSize(600, 500); d.setLayout(new GridLayout(0, 1));
        GameState gs = GameState.get();
        // CPU
        for(Hardware.CpuType c : Hardware.CpuType.values()) {
            JButton b = new JButton("CPU: "+c.label+" ($"+c.cost+")");
            b.addActionListener(e -> { if(gs.currency>=c.cost){gs.currency-=c.cost; gs.addCpu(c); JOptionPane.showMessageDialog(d,"Bought!"); updateTitle();}});
            d.add(b);
        }
        // GPU
        for(Hardware.GpuType g : Hardware.GpuType.values()) {
            if(g!=Hardware.GpuType.INTEGRATED) {
                JButton b = new JButton("GPU: "+g.label+" ($"+g.cost+")");
                b.addActionListener(e -> { if(gs.currency>=g.cost){gs.currency-=g.cost; gs.currentGpu=g; JOptionPane.showMessageDialog(d,"Bought!"); updateTitle();}});
                d.add(b);
            }
        }
        d.setVisible(true);
    }

    private void openLogs() { new JFrame("LOGS").add(new GanttChartPanel()); }
    private void openEducation() { JFrame f=new JFrame("CPU SCHEDULING SIMULATOR"); f.setSize(800,500); f.add(new SchedulerSimulator()); f.setVisible(true); }

    private void openStageSelect() {
        String[] s = new String[10]; for(int i=0; i<10; i++) s[i]="Stage "+(i+1);
        int c = JOptionPane.showOptionDialog(this, "DEPLOY", "STAGES", 0, 0, null, s, s[0]);
        if(c!=-1) { setVisible(false); new CyberQuestRPG(c+1, this).setVisible(true); }
    }
}