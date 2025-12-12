import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GamePanel extends JPanel {
    private CPUProfile cpu;
    private List<Monster> arena;
    public boolean isHyperThreadingActive = false;
    public boolean isPlayerHit = false;
    public int activeThreads=0, maxThreads=0;
    public String turnOverlayText = "";
    private float bgAnimOffset = 0;

    public GamePanel() {
        Timer t = new Timer(50, e -> { bgAnimOffset += 0.5f; if(bgAnimOffset>100) bgAnimOffset=0; repaint(); });
        t.start();
    }

    public void setData(CPUProfile c, List<Monster> a) { this.cpu=c; this.arena=a; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // GPU Background Logic
        Hardware.GpuType gpu = GameState.get().currentGpu;
        g2d.setColor(new Color(20, 20, 30)); g2d.fillRect(0,0,getWidth(),getHeight());
        
        if(gpu == Hardware.GpuType.RTX_4090) {
             g2d.setColor(new Color(50, 0, 100, 50));
             int y = (int)(Math.sin(bgAnimOffset)*50);
             g2d.fillRect(0, 300+y, getWidth(), 100);
        }

        if(cpu==null) return;

        if(isPlayerHit) g2d.translate((Math.random()*10)-5, (Math.random()*10)-5);
        int hX=150, hY=350;
        if(isHyperThreadingActive) { g2d.setColor(new Color(0,255,255,50)); g2d.fillOval(hX-25,hY-25,150,150); }
        g2d.setColor(cpu.color); g2d.fillRoundRect(hX,hY,100,100,20,20);
        g2d.setColor(Color.WHITE); g2d.drawString(cpu.name, hX+10, hY+120);
        g2d.translate(0,0);

        if(arena!=null) {
            for(int i=0; i<arena.size(); i++) {
                Monster m = arena.get(i);
                int x = 400 + (i*140), y = 350;
                if(m.isHit) { x+= (Math.random()*10)-5; y+= (Math.random()*10)-5; g2d.setColor(Color.WHITE); }
                else g2d.setColor(m.type.color);
                
                g2d.fillRect(x,y,80,80);
                g2d.setColor(Color.RED); g2d.fillRect(x,y-15,80,10);
                g2d.setColor(Color.GREEN); g2d.fillRect(x,y-15,(int)((m.currentHP/(double)m.maxHP)*80),10);
                
                g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(m.currentHP + "/" + m.maxHP, x+10, y-20);
                g2d.drawString(m.name, x, y+95);
            }
        }
        
        if(!turnOverlayText.isEmpty()) {
            g2d.setColor(new Color(0,0,0,150)); g2d.fillRect(0, getHeight()/2 - 50, getWidth(), 100);
            g2d.setColor(Color.ORANGE); g2d.setFont(new Font("Impact", Font.BOLD, 60));
            int w = g2d.getFontMetrics().stringWidth(turnOverlayText);
            g2d.drawString(turnOverlayText, getWidth()/2 - w/2, getHeight()/2 + 20);
        }
    }
}