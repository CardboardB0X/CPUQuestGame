import java.awt.Color;
public class CPUProfile {
    public String name;
    public Architecture arch;
    public int physicalCores, maxHP, maxRAM;
    public Color color;
    public CPUProfile(String n, Architecture a, int c, int h, int r, Color co) {
        this.name=n; this.arch=a; this.physicalCores=c; this.maxHP=h; this.maxRAM=r; this.color=co;
    }
}