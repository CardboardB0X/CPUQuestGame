import java.awt.Color;
public enum EnemyType {
    // Low HP, High DMG
    CALCULATOR("Calc.exe", 0.5, 2.0, Color.LIGHT_GRAY, "Basic"),
    NOTEPAD("Notepad", 0.7, 2.2, new Color(200, 200, 255), "Fast"),
    CHROME("Chrome_Tab", 1.5, 4.0, Color.YELLOW, "RAM Eater"),
    VS_CODE("VS_Code", 1.2, 4.5, new Color(50, 150, 255), "Heavy"),
    ANDROID("Android Studio", 3.0, 6.0, new Color(0, 150, 100), "Tank"),
    PREMIERE("Adobe Premiere", 2.0, 7.0, new Color(150, 0, 150), "Deadly"),
    BLENDER("Blender Render", 2.5, 8.0, new Color(255, 100, 0), "Massive"),
    ZIP_BOMB("ZipBomb.rar", 5.0, 10.0, Color.MAGENTA, "Boss 1"),
    RANSOMWARE("WannaCry", 6.0, 12.0, new Color(150, 0, 0), "Boss 2"),
    CYBERPUNK("Cyberpunk 2077", 10.0, 15.0, Color.BLACK, "Final Boss");

    public final String label;
    public final double hpMult, dmgMult;
    public final Color color;
    public final String desc;
    EnemyType(String l, double h, double d, Color c, String de) {
        this.label=l; this.hpMult=h; this.dmgMult=d; this.color=c; this.desc=de;
    }
}