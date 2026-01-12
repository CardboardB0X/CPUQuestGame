import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * CLASS: GamePanel
 * This is the "View" of the game. It extends JPanel, which is a standard Swing component
 * that we can draw onto using the 'paintComponent' method.
 * * RESPONSIBILITIES:
 * 1. Rendering the Battle Scene (Player CPU, Enemies, Background).
 * 2. Handling Visual Effects (Screen Shake, GPU Backgrounds, Hyperthreading Aura).
 * 3. Displaying UI Overlays (Turn Phase Text, HP Bars).
 * * MECHANICS:
 * - It uses a Swing Timer to trigger a repaint() every 50ms (approx 20 FPS).
 * - This creates a loop that updates animations (like the background wave) continuously.
 */
public class GamePanel extends JPanel {

    // --- DATA MODELS ---
    // References to the data objects we need to draw.
    // We don't modify these here; we just read their state (HP, Name, etc.).
    private CPUProfile cpu; 
    private List<Monster> arena;

    // --- ANIMATION STATES ---
    // Flags set by 'CyberQuestRPG.java' to trigger visual effects.
    public boolean isHyperThreadingActive = false; // Draws a cyan aura around the CPU.
    public boolean isPlayerHit = false;            // Triggers the "Shake" effect.
    
    // UI STATS
    // Used to draw the specific thread count if needed (currently drawn in main UI, but kept here for potential overlay).
    public int activeThreads = 0;
    public int maxThreads = 0;
    
    // OVERLAY TEXT
    // Large text displayed in the center of the screen (e.g., "ENEMY PHASE").
    public String turnOverlayText = "";
    
    // INTERNAL ANIMATION VAR
    // A floating number that increases constantly to drive wave/particle animations.
    private float bgAnimOffset = 0;

    // CONSTRUCTOR
    public GamePanel() {
        // ANIMATION LOOP
        // Creates a Timer that fires an event every 50 milliseconds.
        // The lambda expression (e -> ...) defines what happens on each tick.
        Timer t = new Timer(50, e -> { 
            // 1. Increment offset for animations (0 -> 100 then resets)
            bgAnimOffset += 0.5f; 
            if (bgAnimOffset > 100) bgAnimOffset = 0; 
            
            // 2. Request a screen update. This calls 'paintComponent' as soon as possible.
            repaint(); 
        });
        
        // Start the loop immediately.
        t.start();
    }

    // METHOD: Link Data
    // Called by the main game class to pass the current player and enemy list.
    public void setData(CPUProfile c, List<Monster> a) { 
        this.cpu = c; 
        this.arena = a; 
    }

    // --- MAIN DRAWING METHOD ---
    // This is where all the rendering magic happens. 
    // The 'Graphics' object 'g' is your paintbrush.
    @Override
    protected void paintComponent(Graphics g) {
        // 1. CLEAR SCREEN
        // Always call super to ensure the panel cleans up old pixels properly.
        super.paintComponent(g);
        
        // 2. SETUP GRAPHICS
        // Cast to Graphics2D for advanced features (Anti-aliasing).
        Graphics2D g2d = (Graphics2D)g;
        // Turn on Anti-aliasing (smooth edges for shapes/text).
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- LAYER 1: BACKGROUND ---
        
        // Fetch current GPU to determine visual style.
        Hardware.GpuType gpu = GameState.get().currentGpu;
        
        // Default Background: Dark Blue-Grey
        g2d.setColor(new Color(20, 20, 30)); 
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // SPECIAL GPU EFFECT (RTX 4090)
        // If the player owns the RTX 4090, draw a "Plasma Wave" effect.
        if (gpu == Hardware.GpuType.RTX_4090) {
             g2d.setColor(new Color(50, 0, 100, 50)); // Semi-transparent Purple
             // Math.sin uses 'bgAnimOffset' to create a moving wave.
             int y = (int)(Math.sin(bgAnimOffset) * 50); 
             // Draw a moving strip across the screen.
             g2d.fillRect(0, 300 + y, getWidth(), 100);
        }

        // Safety Check: If data hasn't loaded yet, stop drawing.
        if (cpu == null) return;

        // --- LAYER 2: CAMERA EFFECTS ---
        
        // SCREEN SHAKE
        // If the player was hit, randomly shift the entire coordinate system.
        // This makes everything drawn afterwards appear to "shake".
        if (isPlayerHit) {
            g2d.translate((Math.random() * 10) - 5, (Math.random() * 10) - 5);
        }
        
        // --- LAYER 3: PLAYER (CPU) ---
        
        int hX = 150; // X Position
        int hY = 350; // Y Position
        
        // AURA EFFECT (Hyperthreading)
        if (isHyperThreadingActive) { 
            g2d.setColor(new Color(0, 255, 255, 50)); // Cyan glow
            g2d.fillOval(hX - 25, hY - 25, 150, 150); 
        }
        
        // CPU BODY
        g2d.setColor(cpu.color); // Color based on CPU brand (Blue=Intel, Red=AMD)
        g2d.fillRoundRect(hX, hY, 100, 100, 20, 20); // Rounded square
        
        // CPU LABEL
        g2d.setColor(Color.WHITE); 
        g2d.drawString(cpu.name, hX + 10, hY + 120);
        
        // RESET CAMERA (Stop shaking for UI elements drawn after this?)
        // Actually, we reset here so the "Shake" offset doesn't compound endlessly.
        // In this specific code, resetting here means enemies *won't* shake, only the player.
        g2d.translate(0, 0); 

        // --- LAYER 4: ENEMIES ---
        
        if (arena != null) {
            // Loop through all monsters
            for (int i = 0; i < arena.size(); i++) {
                Monster m = arena.get(i);
                
                // Position Logic: Spread them out horizontally based on index 'i'.
                int x = 400 + (i * 140); 
                int y = 350;
                
                // HIT FLASH EFFECT
                // If the monster was just hit, draw it White. Otherwise, use its normal color.
                if (m.isHit) { 
                    // Jiggle position slightly for impact feel
                    x += (Math.random() * 10) - 5; 
                    y += (Math.random() * 10) - 5; 
                    g2d.setColor(Color.WHITE); 
                } else { 
                    g2d.setColor(m.type.color); 
                }
                
                // Draw Body
                g2d.fillRect(x, y, 80, 80);
                
                // ENEMY HP BAR BACKGROUND (Red)
                g2d.setColor(Color.RED); 
                g2d.fillRect(x, y - 15, 80, 10);
                
                // ENEMY HP BAR FOREGROUND (Green)
                // Calculate width based on percentage of health remaining.
                g2d.setColor(Color.GREEN); 
                int hpWidth = (int)((m.currentHP / (double)m.maxHP) * 80);
                g2d.fillRect(x, y - 15, hpWidth, 10);
                
                // ENEMY TEXT INFO
                g2d.setColor(Color.WHITE); 
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                // Draw "Current/Max" HP text above bar
                g2d.drawString(m.currentHP + "/" + m.maxHP, x + 10, y - 20);
                // Draw Name below body
                g2d.drawString(m.name, x, y + 95);
            }
        }
        
        // --- LAYER 5: UI OVERLAYS ---
        
        // TURN BANNER (e.g., "PLAYER PHASE")
        if (!turnOverlayText.isEmpty()) {
            // 1. Semi-transparent black background strip
            g2d.setColor(new Color(0, 0, 0, 150)); 
            g2d.fillRect(0, getHeight() / 2 - 50, getWidth(), 100);
            
            // 2. Text Settings
            g2d.setColor(Color.ORANGE); 
            g2d.setFont(new Font("Impact", Font.BOLD, 60));
            
            // 3. Center the text mathematically
            int w = g2d.getFontMetrics().stringWidth(turnOverlayText); // Measure text width
            // X = Center of screen - Half of text width
            g2d.drawString(turnOverlayText, getWidth() / 2 - w / 2, getHeight() / 2 + 20);
        }
    }
}