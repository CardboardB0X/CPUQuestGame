import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GanttChartPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Default Dark BG
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        List<GameState.LogEntry> logs = GameState.get().lastBattleLogs;
        if (logs == null || logs.isEmpty()) {
            g.setColor(Color.WHITE);
            g.drawString("NO BATTLE DATA AVAILABLE. PLAY A MISSION FIRST.", 50, 50);
            return;
        }

        int xStart = 50, y = 60, rowH = 30;
        long startTime = logs.get(0).start;
        double scale = 0.15; // Pixels per ms

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("PROCESS EXECUTION LOG (GANTT VIEW)", 50, 30);

        for (GameState.LogEntry l : logs) {
            int barX = xStart + (int)((l.start - startTime) * scale);
            int barW = Math.max(5, (int)(l.dur * scale));

            // Color by Type
            if (l.type.equals("COMPUTE")) g.setColor(new Color(50, 150, 255));
            else if (l.type.equals("IO")) g.setColor(new Color(100, 255, 100));
            else g.setColor(Color.GRAY);

            g.fillRect(barX, y, barW, rowH - 5);
            g.setColor(Color.WHITE);
            g.drawRect(barX, y, barW, rowH - 5);
            g.drawString(l.task, barX + 5, y + 15);

            y += rowH;
        }
    }
}