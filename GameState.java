import java.util.ArrayList;
import java.util.List;

public class GameState {
    private static GameState instance;

    public int currency = 600; // Adjusted starting cash

    // CORE HARDWARE (Stats)
    public int levelFreq = 0;   // Damage
    public int levelRAM = 0;    // Max RAM
    public int levelStorage = 0;// Max HP
    
    // NEW HARDWARE
    public int levelCache = 0;  // Critical Hit Chance %
    public int levelCooling = 0;// Passive HP Regen per turn
    
    // UNLOCKS
    public boolean unlockHyperThreading = false; 
    public String currentGPUSkin = "Integrated Graphics";
    
    public List<LogEntry> lastBattleLogs = new ArrayList<>();

    private GameState() {}

    public static GameState get() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    // --- BALANCED STAT CALCULATORS ---
    // Frequency: +20 DMG base
    public int getBonusDamage() { return levelFreq * 20; }  
    
    // RAM: +40 RAM (More actions)
    public int getBonusRAM() { return levelRAM * 40; }      
    
    // Storage: +150 HP (Tankiness)
    public int getBonusHP() { return levelStorage * 150; }  
    
    // Cache: +5% Crit chance per level (Max 50%)
    public double getCritChance() { return Math.min(0.50, levelCache * 0.05); }
    
    // Cooling: +15 HP regen per turn
    public int getRegenAmount() { return levelCooling * 15; }

    public static class LogEntry {
        public String task; public long start; public long dur; public String type;
        public LogEntry(String t, long s, long d, String ty) { task=t; start=s; dur=d; type=ty; }
    }
}