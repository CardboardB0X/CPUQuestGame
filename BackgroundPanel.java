import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class BackgroundPanel extends JPanel {
    private Timer timer;
    private Particle[] particles = new Particle[100];
    private Random rand = new Random();

    public BackgroundPanel() {
        for(int i=0; i<particles.length; i++) particles[i] = new Particle();
        
        timer = new Timer(30, e -> {
            updatePhysics();
            repaint();
        });
        timer.start();
    }

    private void updatePhysics() {
        GameState gs = GameState.get();
        // Speed depends on GPU
        int speed = 1;
        if(gs.currentGpu.label.contains("RTX")) speed = 5;
        else if(gs.currentGpu.label.contains("GT")) speed = 2;

        for(Particle p : particles) {
            p.y += (p.speed * speed);
            if(p.y > getHeight()) p.y = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());

        GameState gs = GameState.get();
        if(gs.currentGpu == Hardware.GpuType.INTEGRATED) return;

        g.setColor(gs.currentGpu.label.contains("RTX") ? Color.GREEN : Color.CYAN);
        
        for(Particle p : particles) {
            if(gs.currentGpu == Hardware.GpuType.RTX_4090) {
                // Starfield / 3D effect
                g.fillOval(p.x, p.y, p.size, p.size);
            } else {
                // Matrix Rain
                g.drawString(String.valueOf((char)(rand.nextInt(26)+'A')), p.x, p.y);
            }
        }
    }

    class Particle {
        int x = rand.nextInt(1000);
        int y = rand.nextInt(800);
        int speed = rand.nextInt(3)+1;
        int size = rand.nextInt(4)+2;
    }
}