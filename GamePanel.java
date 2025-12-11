import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private CPUProfile cpu;
    private List<Monster> arena;
    
    // States
    public boolean isHyperThreadingActive = false;
    public boolean isPlayerHit = false;
    public int activeThreads = 0; 
    public int maxThreads = 0;

    // Shake
    public int shakeX = 0, shakeY = 0;

    public void setData(CPUProfile cpu, List<Monster> arena) {
        this.cpu = cpu;
        this.arena = arena;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cpu == null) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // BG (Default)
        if (getBackground().equals(UIManager.getColor("Panel.background"))) {
             g2d.setColor(new Color(20, 20, 30));
             g2d.fillRect(0, 0, getWidth(), getHeight());
        } else {
             // If GPU skin changed the background color
             g2d.setColor(getBackground());
             g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        drawThreadMonitor(g2d);

        // Player Shake
        if (isPlayerHit) g2d.translate(shakeX, shakeY);

        // Draw Player
        int hX = 150, hY = 300;
        if (isHyperThreadingActive) {
            g2d.setColor(new Color(0, 255, 255, 60));
            g2d.fillOval(hX-25, hY-25, 150, 150);
            g2d.setColor(Color.CYAN);
            g2d.drawString("SMT ONLINE", hX+15, hY-30);
        }
        
        g2d.setColor(isPlayerHit ? Color.RED : cpu.color);
        g2d.fillRoundRect(hX, hY, 100, 100, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(cpu.name, hX+10, hY+120);

        // Reset Shake
        g2d.translate(-shakeX, -shakeY);

        // Draw Enemies
        drawMonsters(g2d);
    }

    private void drawThreadMonitor(Graphics2D g2d) {
        int w = 220, h = 80;
        int x = getWidth() - w - 20;
        int y = 20;

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(x, y, w, h, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(x, y, w, h, 10, 10);
        g2d.drawString("THREAD MONITOR", x + 10, y + 20);

        int boxSize = 25;
        for (int i = 0; i < maxThreads; i++) {
            int bx = x + 15 + (i * (boxSize + 5));
            int by = y + 35;
            
            // Green = Available, Red = Used
            g2d.setColor(i < activeThreads ? Color.GREEN : new Color(100, 0, 0));
            g2d.fillRect(bx, by, boxSize, boxSize);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(bx, by, boxSize, boxSize);
        }
    }

    private void drawMonsters(Graphics2D g2d) {
        if (arena == null) return;
        int startX = 450, startY = 300;

        for(int i=0; i<arena.size(); i++) {
            Monster m = arena.get(i);
            int mx = startX + (i * 120);
            int my = startY;

            // Hit Animation (Wiggle)
            if (m.isHit) {
                mx += (Math.random()*10 - 5);
                my += (Math.random()*10 - 5);
                g2d.setColor(Color.WHITE); // Flash White
            } else {
                g2d.setColor(m.type.color);
            }

            g2d.fillRect(mx, my, 80, 80);
            
            // HP Bar
            g2d.setColor(Color.RED);
            g2d.fillRect(mx, my-10, 80, 5);
            g2d.setColor(Color.GREEN);
            int barW = (int)((m.currentHP / (double)m.maxHP) * 80);
            g2d.fillRect(mx, my-10, barW, 5);
            
            g2d.setColor(Color.WHITE);
            g2d.drawString(m.name, mx, my-20);
        }
    }
}