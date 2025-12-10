import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private CPUProfile cpu;
    private List<Monster> arena;
    
    // Animation States
    public String currentAnimation = "IDLE";
    public boolean isBoosted = false;
    public boolean isDefending = false;
    public boolean isHyperthreadingVisuallyActive = false; // For the split animation
    public boolean isCPUHurt = false; // Flashes red during shake

    // Shake Offsets
    public int shakeX = 0;
    public int shakeY = 0;
    
    // Turn State text
    public String turnStateStr = "BOOTING...";

    public void setData(CPUProfile cpu, List<Monster> arena) {
        this.cpu = cpu;
        this.arena = arena;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cpu == null) return;

        // Upgrade to Graphics2D for better rendering control
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // APPLY SHAKE (Translate the whole canvas)
        g2d.translate(shakeX, shakeY);

        // 1. Background
        g2d.setColor(new Color(20, 20, 30));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw Turn Indicator Banner
        drawTurnBanner(g2d);

        // Platform
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillOval(100, 350, 200, 100); 

        // 2. HERO DRAWING
        int hX = 150, hY = 300;
        if (currentAnimation.equals("ATTACK")) hX += 100;
        
        // Status Auras
        if (isBoosted) drawAura(g2d, hX, hY, new Color(255, 100, 0, 100));
        if (isDefending) drawAura(g2d, hX, hY, new Color(0, 255, 255, 100));

        // HYPERTHREADING SPLIT VISUAL
        if (isHyperthreadingVisuallyActive) {
            drawSplitAnimation(g2d, hX, hY);
        }

        // Main CPU Body
        if (isCPUHurt) {
             g2d.setColor(Color.RED); // Flash red when hurt
        } else {
             g2d.setColor(cpu.color);
        }
        g2d.fillRect(hX, hY, 100, 100);
        
        // CPU Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(cpu.name, hX+5, hY-10);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        // Show "Cores x Threads"
        String coreText = cpu.physicalCores + "C / " + (isHyperthreadingVisuallyActive ? cpu.physicalCores*2 : cpu.physicalCores) + "T";
        g2d.drawString(coreText, hX+25, hY+50);


        // ULTIMATE Overlay
        if (currentAnimation.equals("ULTIMATE")) {
            g2d.setColor(new Color(255, 255, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // 3. MONSTER DRAWING
        drawMonsters(g2d);

        // RESET TRANSLATION (So future frames aren't permanently shifted)
        g2d.translate(-shakeX, -shakeY);
    }

    // --- Helper Drawing Methods ---
    
    private void drawTurnBanner(Graphics2D g2d) {
        Color bannerColor = turnStateStr.contains("PLAYER") ? new Color(0, 100, 200, 180) : new Color(200, 50, 0, 180);
        g2d.setColor(bannerColor);
        g2d.fillRect(0, 0, getWidth(), 40);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Consolas", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(turnStateStr)) / 2;
        g2d.drawString(turnStateStr, x, 28);
    }

    private void drawAura(Graphics2D g2d, int x, int y, Color c) {
        g2d.setColor(c);
        g2d.fillOval(x-20, y-20, 140, 140);
    }
    
    private void drawSplitAnimation(Graphics2D g2d, int hX, int hY) {
        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke(3));
        // Draw energy lines splitting off the main core
        g2d.drawLine(hX + 50, hY, hX + 20, hY - 40);
        g2d.drawLine(hX + 50, hY, hX + 80, hY - 40);
        g2d.drawLine(hX + 50, hY + 100, hX + 20, hY + 140);
        g2d.drawLine(hX + 50, hY + 100, hX + 80, hY + 140);
        
        // Draw "ghost" threads
        g2d.setColor(new Color(0, 255, 255, 100));
        g2d.fillRect(hX - 30, hY + 10, 30, 80);
        g2d.fillRect(hX + 100, hY + 10, 30, 80);
    }

    private void drawMonsters(Graphics2D g2d) {
        if (arena == null) return;
        int sX = 650, sY = 200;
        for(int i=0; i<arena.size(); i++) {
            Monster m = arena.get(i);
            int x = sX + (i%3)*70 + (i/3)*30; 
            int y = sY + (i%3)*60 + (i/3)*30; 
            
            if(!m.isDead) {
                g2d.setColor(m.type.color);
                // Shapes
                if(m.type == EnemyType.CRYSIS) {
                    g2d.fillRect(x-10, y-10, 80, 80); 
                    g2d.setColor(Color.RED); g2d.drawRect(x-10, y-10, 80, 80);
                } 
                else if (m.type == EnemyType.CHROME || m.type == EnemyType.MSTEAMS) g2d.fillOval(x, y, 60, 60); 
                else if (m.type == EnemyType.DOOM) g2d.fillPolygon(new int[]{x+30, x, x+60}, new int[]{y, y+60, y+60}, 3);
                else g2d.fillRect(x, y, 60, 60); 
                
                g2d.setColor(Color.WHITE); g2d.drawString(m.name, x, y-20);
                
                // HP Bar
                g2d.setColor(Color.RED); g2d.fillRect(x, y-8, 60, 5);
                g2d.setColor(Color.GREEN); 
                int w = (int)((m.currentHP/(double)m.maxHP)*60); 
                g2d.fillRect(x, y-8, w, 5);
            }
        }
    }
}