import java.awt.Color;

public enum EnemyType {
    // FODDER
    CALCULATOR("Calc.exe", 0.3, 0.1, Color.LIGHT_GRAY, "Tiny task. Free RAM."),
    MSPAINT("Paint", 0.5, 0.2, Color.PINK, "Basic graphics task."),
    
    // STANDARD
    STANDARD("Process", 1.0, 1.0, Color.GRAY, "Normal priority."),
    MSTEAMS("Teams", 1.8, 0.5, new Color(80, 80, 180), "Bloatware. High HP."),
    
    // TANKS
    CHROME("Chrome", 2.2, 0.8, Color.YELLOW, "RAM Eater. Needs FCFS."),
    
    // AGGRESSIVE
    DOOM("DOOM.exe", 1.0, 2.5, new Color(180, 0, 0), "Aggressive! Kill fast (SJF)."),
    VIRUS("Trojan", 0.5, 3.0, Color.RED, "CRITICAL THREAT! Snipe it!"),
    
    // BOSS
    CRYSIS("CRYSIS.exe", 5.0, 3.0, Color.BLACK, "MAXIMUM LOAD. USE OVERCLOCK!");

    public final String label;
    public final double hpMult;
    public final double dmgMult;
    public final Color color;
    public final String desc;

    EnemyType(String l, double h, double d, Color c, String desc) {
        this.label = l; this.hpMult = h; this.dmgMult = d; this.color = c; this.desc = desc;
    }
}