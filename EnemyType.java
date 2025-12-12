import java.awt.Color;
public enum EnemyType {
    CALCULATOR("Calc.exe", 1.0, 1.0, Color.LIGHT_GRAY, "Tiny Task"),
    NOTEPAD("Notepad", 1.2, 1.1, new Color(200, 200, 255), "Text Editor"),
    CHROME("Chrome_Tab", 2.5, 1.5, Color.YELLOW, "RAM Hog"),
    VS_CODE("VS_Code", 2.0, 2.0, new Color(50, 150, 255), "Electron App"),
    ANDROID("Android Studio", 4.0, 2.5, new Color(0, 150, 100), "Gradle Build"),
    PREMIERE("Adobe Premiere", 3.5, 3.0, new Color(150, 0, 150), "Rendering"),
    BLENDER("Blender Render", 5.0, 1.5, new Color(255, 100, 0), "Cycles Engine"),
    ZIP_BOMB("ZipBomb.rar", 6.0, 3.0, Color.MAGENTA, "Expansion"),
    RANSOMWARE("WannaCry", 8.0, 5.0, new Color(150, 0, 0), "Encrypting"),
    CYBERPUNK("Cyberpunk 2077", 12.0, 6.0, Color.BLACK, "System Meltdown");

    public final String label;
    public final double hpMult, dmgMult;
    public final Color color;
    public final String desc;
    EnemyType(String l, double h, double d, Color c, String de) {
        this.label=l; this.hpMult=h; this.dmgMult=d; this.color=c; this.desc=de;
    }
}