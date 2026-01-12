import javax.swing.*;
import java.awt.*;
import java.util.List;

// EXTENDS JPANEL: This class inherits from JPanel, making it a UI component 
// that can be added to a JFrame (window).
public class GanttChartPanel extends JPanel {

    // OVERRIDE: We replace the default drawing method of JPanel with our own.
    // This method is called automatically by Java Swing whenever the screen needs to update.
    @Override
    protected void paintComponent(Graphics g) {
        // SUPER: Clears the screen and prepares the panel for drawing. 
        // Always call this first to avoid visual glitches.
        super.paintComponent(g);

        // 1. SETUP BACKGROUND
        // Sets the "pen" color to a dark gray (RGB: 30, 30, 30).
        g.setColor(new Color(30, 30, 30)); 
        // Fills a rectangle covering the entire panel width/height.
        g.fillRect(0, 0, getWidth(), getHeight());

        // 2. RETRIEVE DATA
        // Access the Singleton GameState to get the logs from the last battle.
        List<GameState.LogEntry> logs = GameState.get().lastBattleLogs;

        // VALIDATION: If no battle has happened yet, show a message instead of crashing.
        if (logs == null || logs.isEmpty()) {
            g.setColor(Color.WHITE);
            g.drawString("NO BATTLE DATA AVAILABLE", 50, 50);
            return; // Stop drawing here.
        }

        // 3. CHART SETTINGS
        int xStart = 50;   // X pixel position where the chart begins
        int y = 60;        // Y pixel position for the first row
        int rowHeight = 30;// Height of each task bar in pixels
        
        // We normalize time so the first task starts at 0ms relative to the chart.
        long startTime = logs.get(0).start; 
        
        // Scaling Factor: Converts milliseconds to pixels. 
        // 0.15 means 100ms duration = 15 pixels wide.
        double scale = 0.15; 

        // 4. DRAW HEADER
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("CPU TASK SCHEDULING LOG (GANTT VIEW)", 50, 30);

        // 5. DRAW BARS LOOP
        // Iterate through every log entry found in GameState.
        for (GameState.LogEntry l : logs) {
            
            // MATH: Calculate relative start position based on time difference.
            int relativeStart = (int)((l.start - startTime) * scale);
            int barX = xStart + relativeStart;
            
            // MATH: Calculate width based on duration. Max(5, ...) ensures tiny tasks are visible.
            int barWidth = Math.max(5, (int)(l.dur * scale));

            // COLOR LOGIC: Blue for computation, Gray for idle/other.
            if (l.type.equals("COMPUTE")) {
                g.setColor(new Color(50, 150, 255)); // Bright Blue
            } else {
                g.setColor(Color.GRAY);
            }

            // DRAW THE BAR
            // fillRect(x, y, width, height) fills the colored box.
            g.fillRect(barX, y, barWidth, rowHeight - 5); // -5 creates a small gap between rows
            
            // DRAW OUTLINE
            g.setColor(Color.WHITE);
            g.drawRect(barX, y, barWidth, rowHeight - 5);
            
            // DRAW TEXT LABEL
            // Draws the task name (e.g., "FCFS", "RR") inside or next to the bar.
            g.drawString(l.task, barX + 5, y + 15);

            // Move Y down for the next task (Creates the waterfall effect)
            y += rowHeight;
            
            // SAFETY: Stop drawing if we run off the bottom of the screen
            if (y > getHeight()) break;
        }
    }
}