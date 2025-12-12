import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private CPUProfile cpu;
    private List<Monster> arena;
    public boolean isHyperThreadingActive = false;
    public boolean isPlayerHit = false;
    public int activeThreads=0, maxThreads=0;
    
    // GPU Visuals
    private float bgAnimOffset = 0;

    public void setData(CPUProfile c, List<Monster> a) { this.cpu=c; this.arena=a; }

    public GamePanel() {
        // Animation Loop for GPU Backgrounds
        Timer t = new Timer(50, e -> {
            bgAnimOffset += 0.1f;
            if(bgAnimOffset > 100) bgAnimOffset = 0;
            repaint();
        });
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- DRAW LIVE BACKGROUND BASED ON GPU ---
        Hardware.GpuType gpu = GameState.get().currentGpu;
        if(gpu == Hardware.GpuType.INTEGRATED) {
            g2d.setColor(new Color(20, 20, 30)); g2d.fillRect(0,0,getWidth(),getHeight());
        } 
        else if (gpu == Hardware.GpuType.GTX_1050) {
            g2d.setColor(new Color(30, 30, 50)); g2d.fillRect(0,0,getWidth(),getHeight());
            g2d.setColor(new Color(255,255,255,50));
            for(int i=0; i<20; i++) {
                int x = (int)((i*50 + bgAnimOffset*10) % getWidth());
                g2d.fillOval(x, (i*40)%getHeight(), 5, 5);
            }
        }
        else if (gpu == Hardware.GpuType.RTX_3060) {
            // RGB Wave
            GradientPaint gp = new GradientPaint(0, 0, Color.BLUE, getWidth(), getHeight(), new Color((int)(bgAnimOffset*2)%255, 0, 100));
            g2d.setPaint(gp); g2d.fillRect(0,0,getWidth(),getHeight());
        }
        else if (gpu == Hardware.GpuType.RTX_4090) {
            // Plasma
            g2d.setColor(Color.BLACK); g2d.fillRect(0,0,getWidth(),getHeight());
            g2d.setColor(new Color(100, 0, 200, 100));
            int size = (int)(Math.sin(bgAnimOffset)*50 + 100);
            g2d.fillOval(getWidth()/2 - size, getHeight()/2 - size, size*2, size*2);
        }

        if(cpu==null) return;

        // Player
        if(isPlayerHit) g2d.translate((Math.random()*10)-5, (Math.random()*10)-5);
        int hX=150, hY=350;
        if(isHyperThreadingActive) { g2d.setColor(new Color(0,255,255,50)); g2d.fillOval(hX-25,hY-25,150,150); }
        g2d.setColor(cpu.color); g2d.fillRoundRect(hX,hY,100,100,20,20);
        g2d.setColor(Color.WHITE); g2d.drawString(cpu.name, hX+10, hY+120);
        g2d.translate(0,0);

        // Enemies
        if(arena!=null) {
            for(int i=0; i<arena.size(); i++) {
                Monster m = arena.get(i);
                int x = 400 + (i*120), y = 350;
                if(m.isHit) { x+= (Math.random()*10)-5; y+= (Math.random()*10)-5; g2d.setColor(Color.WHITE); }
                else g2d.setColor(m.type.color);
                
                g2d.fillRect(x,y,80,80);
                g2d.setColor(Color.RED); g2d.fillRect(x,y-10,80,5);
                g2d.setColor(Color.GREEN); g2d.fillRect(x,y-10,(int)((m.currentHP/(double)m.maxHP)*80),5);
                g2d.setColor(Color.WHITE); g2d.drawString(m.name, x, y-20);
            }
        }
    }
}