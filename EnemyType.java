import java.awt.Color;

public enum EnemyType {
    // TIER 1: BLOATWARE
    CALCULATOR("Calc.exe", 0.3, 0.1, Color.LIGHT_GRAY, "Tiny task."),
    NOTEPAD("Notepad", 0.4, 0.2, new Color(200, 200, 255), "Text editor."),
    
    // TIER 2: STANDARD PROCESSES
    STANDARD("System.d", 1.0, 1.0, Color.GRAY, "Standard process."),
    MSTEAMS("Teams.exe", 1.5, 0.5, new Color(80, 80, 180), "Heavy RAM usage."),
    
    // TIER 3: MALWARE & TANKS
    CHROME("Chrome", 2.0, 0.8, Color.YELLOW, "RAM Eater."),
    TROJAN("Trojan.Bat", 0.8, 2.5, Color.MAGENTA, "High Damage!"),
    
    // TIER 4: BOSSES
    RANSOMWARE("WannaCry", 3.5, 3.0, new Color(150, 0, 0), "BOSS: Encrypts files."),
    BOTNET("Bot.Zombie", 1.2, 1.5, new Color(0, 100, 0), "Swarm enemy."),
    MAINFRAME("THE CORE", 6.0, 4.0, Color.BLACK, "FINAL BOSS.");

    public final String label;
    public final double hpMult;
    public final double dmgMult;
    public final Color color;
    public final String desc;

    EnemyType(String l, double h, double d, Color c, String desc) {
        this.label = l; this.hpMult = h; this.dmgMult = d; this.color = c; this.desc = desc;
    }
}