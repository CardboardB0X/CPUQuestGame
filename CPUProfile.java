import java.awt.Color;

public class CPUProfile {
    public String name;
    public Architecture arch;
    public int physicalCores; // Max 3
    public int maxRAM;
    public int maxHP;
    public Color color;  

    public CPUProfile(String name, Architecture arch, int cores, int hp, int ram, Color c) {
        this.name = name;
        this.arch = arch;
        this.physicalCores = cores; // Base Actions (1-3)
        this.maxHP = hp;
        this.maxRAM = ram;
        this.color = c;
    }
}