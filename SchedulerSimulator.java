import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class SchedulerSimulator extends JPanel {
    private List<Process> queue = new ArrayList<>();
    private String currentAlgo = "FCFS";
    private JTextArea explanation;
    private DrawPanel drawPanel;
    private Timer simTimer;
    private Process activeProcess = null;

    public SchedulerSimulator() {
        setLayout(new BorderLayout());
        
        // Control Panel
        JPanel controls = new JPanel();
        controls.add(new JLabel("Algorithm: "));
        String[] algos = {"FCFS", "SJF", "Round Robin"};
        JComboBox<String> cb = new JComboBox<>(algos);
        cb.addActionListener(e -> { currentAlgo = (String)cb.getSelectedItem(); resetSim(); updateText(); });
        controls.add(cb);
        
        JButton btnAdd = new JButton("Add Process");
        btnAdd.addActionListener(e -> addRandomProcess());
        controls.add(btnAdd);
        
        JButton btnRun = new JButton("Run Sim");
        btnRun.addActionListener(e -> startSim());
        controls.add(btnRun);
        
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> resetSim());
        controls.add(btnReset);

        add(controls, BorderLayout.NORTH);

        drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);

        explanation = new JTextArea(5, 40);
        explanation.setEditable(false);
        explanation.setFont(new Font("SansSerif", Font.PLAIN, 14));
        add(new JScrollPane(explanation), BorderLayout.SOUTH);
        
        updateText();
    }

    private void updateText() {
        if(currentAlgo.equals("FCFS")) explanation.setText("First-Come, First-Served (FCFS):\nThe CPU executes processes in the exact order they arrive.");
        if(currentAlgo.equals("SJF")) explanation.setText("Shortest Job First (SJF):\nThe CPU scans the queue and picks the process with the smallest Burst Time.");
        if(currentAlgo.equals("Round Robin")) explanation.setText("Round Robin (RR):\nEach process gets a small 'Time Quantum'. If it doesn't finish, it goes to the back.");
    }

    private void addRandomProcess() {
        queue.add(new Process("P"+(queue.size()+1), (int)(Math.random()*100)+20));
        drawPanel.repaint();
    }

    private void resetSim() {
        if(simTimer != null) simTimer.stop();
        queue.clear();
        activeProcess = null;
        drawPanel.repaint();
    }

    private void startSim() {
        if(simTimer != null && simTimer.isRunning()) return;
        
        simTimer = new Timer(50, e -> {
            if (activeProcess == null) {
                if (queue.isEmpty()) { ((Timer)e.getSource()).stop(); return; }
                
                // Logic for picking next process based on Algorithm
                if (currentAlgo.equals("FCFS")) {
                    activeProcess = queue.remove(0);
                } 
                else if (currentAlgo.equals("SJF")) {
                    activeProcess = queue.stream().min(Comparator.comparingInt(p -> p.burst)).orElse(null);
                    queue.remove(activeProcess);
                } 
                else if (currentAlgo.equals("Round Robin")) {
                    activeProcess = queue.remove(0);
                }
            }

            if (activeProcess != null) {
                activeProcess.progress++;
                // RR Logic: Time Quantum of 30 ticks
                if (currentAlgo.equals("Round Robin") && activeProcess.progress % 30 == 0 && activeProcess.progress < activeProcess.burst) {
                    queue.add(activeProcess); // Re-queue
                    activeProcess = null; // Context Switch
                }
                // Finish Logic
                else if (activeProcess.progress >= activeProcess.burst) {
                    activeProcess = null; // Done
                }
            }
            drawPanel.repaint();
        });
        simTimer.start();
    }

    private class Process {
        String name; int burst; int progress = 0;
        public Process(String n, int b) { name=n; burst=b; }
    }

    // Custom drawing for the Simulator
    private class DrawPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.DARK_GRAY); g.fillRect(0,0,getWidth(),getHeight());
            
            g.setColor(Color.LIGHT_GRAY); g.fillRect(50, 50, 100, 100);
            g.setColor(Color.BLACK); g.drawString("CPU", 90, 105);
            
            if(activeProcess != null) {
                g.setColor(Color.CYAN);
                g.fillRect(60, 60, 80, 80);
                g.setColor(Color.BLACK);
                g.drawString(activeProcess.name, 85, 100);
                int pct = (int)((activeProcess.progress / (double)activeProcess.burst)*80);
                g.setColor(Color.GREEN); g.fillRect(60, 130, pct, 5);
            }

            int x = 200;
            for(Process p : queue) {
                g.setColor(Color.ORANGE);
                g.fillRect(x, 70, 60, 60);
                g.setColor(Color.BLACK);
                g.drawString(p.name, x+10, 95);
                g.drawString(p.burst+"ms", x+10, 115);
                x += 70;
            }
            g.setColor(Color.WHITE); g.drawString("WAITING QUEUE ->", 200, 60);
        }
    }
}