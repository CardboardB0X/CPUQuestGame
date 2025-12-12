import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }

    public MainMenu() {
        setTitle("CYBER ARCHITECT: SYSTEM CONFIGURATOR");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("SYSTEM BIOS v5.0 [ULTIMATE]", SwingConstants.CENTER);
        header.setFont(new Font("Consolas", Font.BOLD, 32));
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30,50,50,50));

        menuPanel.add(createBigBtn("⚔️ BOOT SYSTEM", "Deploy to Battle", this::openStageSelect));
        menuPanel.add(createBigBtn("🛠️ SYSTEM CONFIG", "Hardware & Skills", this::openConfigurator));
        menuPanel.add(createBigBtn("🛒 COMPONENT SHOP", "Buy CPUs & Boards", this::openShop));
        menuPanel.add(createBigBtn("📊 SYSTEM LOGS", "Analyze Threads", this::openLogs));

        add(menuPanel, BorderLayout.CENTER);
        updateTitle();
    }

    private void updateTitle() {
        GameState gs = GameState.get();
        setTitle("BUILD: " + gs.currentCpu.label + (gs.useDualCpu ? " (DUAL)" : "") + " | FUNDS: $" + gs.currency);
    }

    private JButton createBigBtn(String t, String s, Runnable r) {
        JButton b = new JButton("<html><center><h1>"+t+"</h1>"+s+"</center></html>");
        b.addActionListener(e -> r.run()); return b;
    }

    private void openConfigurator() {
        JDialog d = new JDialog(this, "SYSTEM CONFIGURATION UTILITY", true);
        d.setSize(900, 600);
        d.setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 14));
        tabs.addTab("ASSEMBLY", createAssemblyPanel(d));
        tabs.addTab("BIOS", createBiosPanel());
        tabs.addTab("KERNEL (SKILLS)", createSkillPanel());

        d.add(tabs, BorderLayout.CENTER);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        updateTitle();
    }

    private JPanel createAssemblyPanel(JDialog d) {
        JPanel p = new JPanel(new GridLayout(4, 1, 10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        GameState gs = GameState.get();

        // 1. MOBO
        JPanel rowMobo = new JPanel(new FlowLayout(FlowLayout.LEFT)); rowMobo.add(new JLabel("MOTHERBOARD: "));
        JComboBox<Hardware.MoboType> cmbMobo = new JComboBox<>();
        for(Hardware.MoboType m : Hardware.MoboType.values()) if(gs.hasMobo(m)) cmbMobo.addItem(m);
        cmbMobo.setSelectedItem(gs.currentMobo);
        cmbMobo.addActionListener(e -> { gs.currentMobo = (Hardware.MoboType)cmbMobo.getSelectedItem(); d.repaint(); });
        rowMobo.add(cmbMobo); p.add(rowMobo);

        // 2. CPU
        JPanel rowCpu = new JPanel(new FlowLayout(FlowLayout.LEFT)); rowCpu.add(new JLabel("CPU: "));
        JComboBox<Hardware.CpuType> cmbCpu = new JComboBox<>();
        for(Hardware.CpuType c : Hardware.CpuType.values()) if(gs.getCpuCount(c) > 0) cmbCpu.addItem(c);
        cmbCpu.setSelectedItem(gs.currentCpu);
        cmbCpu.addActionListener(e -> { gs.currentCpu = (Hardware.CpuType)cmbCpu.getSelectedItem(); gs.useDualCpu = false; d.repaint(); });
        rowCpu.add(cmbCpu); p.add(rowCpu);

        // 3. DUAL CPU
        JPanel rowDual = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox chkDual = new JCheckBox("ENABLE DUAL CPU CONFIGURATION");
        boolean moboSupport = gs.currentMobo.isDualSocket;
        boolean hasTwo = gs.getCpuCount(gs.currentCpu) >= 2;
        chkDual.setEnabled(moboSupport && hasTwo);
        chkDual.setSelected(gs.useDualCpu);
        if(!moboSupport) chkDual.setText("ENABLE DUAL CPU (Requires Server Board)");
        else if(!hasTwo) chkDual.setText("ENABLE DUAL CPU (Requires 2x " + gs.currentCpu.label + ")");
        chkDual.addActionListener(e -> gs.useDualCpu = chkDual.isSelected());
        rowDual.add(chkDual); p.add(rowDual);

        // 4. INFO
        JTextArea info = new JTextArea(); info.setEditable(false);
        info.setText("STATS:\nCores: " + (gs.currentCpu.cores * (gs.useDualCpu?2:1)) + "\nSMT Support: " + (gs.currentCpu.supportsThreads ? "YES" : "NO") + "\nStability: " + (int)(gs.currentMobo.stabilityMult*100) + "%");
        p.add(info);

        return p;
    }

    private JPanel createBiosPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createTitledBorder("VOLTAGE CONTROL"));
        GameState gs = GameState.get();

        JPanel pOC = new JPanel(new BorderLayout());
        JSlider sOC = new JSlider(0, 100, gs.overclockVal);
        sOC.setMajorTickSpacing(20); sOC.setPaintTicks(true); sOC.setPaintLabels(true);
        JLabel lOC = new JLabel("OVERCLOCK: " + gs.overclockVal + "% (Risk: Heat)", SwingConstants.CENTER);
        sOC.addChangeListener(e -> { gs.overclockVal = sOC.getValue(); lOC.setText("OVERCLOCK: " + gs.overclockVal + "% (Boost Dmg / Risk Heat)"); });
        pOC.add(lOC, BorderLayout.NORTH); pOC.add(sOC, BorderLayout.CENTER);

        JPanel pUV = new JPanel(new BorderLayout());
        JSlider sUV = new JSlider(0, 100, gs.undervoltVal);
        sUV.setMajorTickSpacing(20); sUV.setPaintTicks(true); sUV.setPaintLabels(true);
        JLabel lUV = new JLabel("UNDERVOLT: " + gs.undervoltVal + "% (Risk: Stability)", SwingConstants.CENTER);
        sUV.addChangeListener(e -> { gs.undervoltVal = sUV.getValue(); lUV.setText("UNDERVOLT: " + gs.undervoltVal + "% (Save RAM / Risk Error)"); });
        pUV.add(lUV, BorderLayout.NORTH); pUV.add(sUV, BorderLayout.CENTER);

        p.add(pOC); p.add(pUV);
        return p;
    }

    private JPanel createSkillPanel() {
        JPanel p = new JPanel(new GridLayout(3, 1, 10, 10));
        p.setBorder(BorderFactory.createTitledBorder("KERNEL OPTIMIZATIONS (MAX LEVEL 15)"));
        GameState gs = GameState.get();

        p.add(createSkillRow("FCFS Scheduler", "Base Dmg: 7 + (Lvl*3)", gs.skillLvlFCFS, 
            () -> { if(gs.currency>=200 && gs.skillLvlFCFS < 15) { gs.currency-=200; gs.skillLvlFCFS++; }}));
            
        p.add(createSkillRow("Round Robin", "Quantum: 5 + Lvl (Max 20)", gs.skillLvlRR, 
            () -> { if(gs.currency>=300 && gs.skillLvlRR < 15) { gs.currency-=300; gs.skillLvlRR++; }}));
            
        p.add(createSkillRow("SJF Predictor", "Base Dmg: 10 + (Lvl*4)", gs.skillLvlSJF, 
            () -> { if(gs.currency>=250 && gs.skillLvlSJF < 15) { gs.currency-=250; gs.skillLvlSJF++; }}));

        return p;
    }

    private JPanel createSkillRow(String n, String desc, int lvl, Runnable up) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("<html><b>"+n+" (Lv "+lvl+"/15)</b><br>"+desc+"</html>"), BorderLayout.CENTER);
        JButton b = new JButton("UPGRADE ($300)");
        if(lvl >= 15) { b.setEnabled(false); b.setText("MAX LEVEL"); }
        b.addActionListener(e -> { up.run(); openConfigurator(); }); 
        p.add(b, BorderLayout.EAST);
        return p;
    }

    private void openShop() {
        JDialog d = new JDialog(this, "COMPONENT MARKET", true);
        d.setSize(600, 500);
        d.setLayout(new GridLayout(0, 1));
        GameState gs = GameState.get();

        for(Hardware.CpuType c : Hardware.CpuType.values()) {
            JButton b = new JButton("CPU: " + c.label + " ($"+c.cost+")");
            b.addActionListener(e -> { if(gs.currency >= c.cost) { gs.currency -= c.cost; gs.addCpu(c); JOptionPane.showMessageDialog(d, "Purchased " + c.label); updateTitle(); }});
            d.add(b);
        }
        for(Hardware.MoboType m : Hardware.MoboType.values()) {
            JButton b = new JButton("MOBO: " + m.label + " ($"+m.cost+")");
            if(gs.hasMobo(m)) b.setEnabled(false);
            b.addActionListener(e -> { if(gs.currency >= m.cost) { gs.currency -= m.cost; gs.addMobo(m); JOptionPane.showMessageDialog(d, "Purchased " + m.label); updateTitle(); }});
            d.add(b);
        }
        d.setVisible(true);
    }
    
    private void openLogs() { new JFrame("LOGS").add(new GanttChartPanel()); }
    private void openStageSelect() {
        String[] s = new String[10]; for(int i=0; i<10; i++) s[i] = "Stage " + (i+1);
        int c = JOptionPane.showOptionDialog(this, "DEPLOY", "STAGES", 0, JOptionPane.PLAIN_MESSAGE, null, s, s[0]);
        if(c != -1) { this.setVisible(false); new CyberQuestRPG(c+1, this).setVisible(true); }
    }
}