import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GameState {
    // STATIC INSTANCE: The single copy of this class.
    private static GameState instance;
    
    // GAME RESOURCES
    public int currency = 2000; 

    // --- ROGUELIKE STATE ---
    // If the player dies, this resets to 1. If they win, it increments.
    public int currentFloor = 1; 

    // --- CURRENT LOADOUT ---
    // Stores which Enum constants are currently equipped.
    public Hardware.CpuType currentCpu = Hardware.CpuType.ATHLON;
    public Hardware.GpuType currentGpu = Hardware.GpuType.INTEGRATED;
    public Hardware.CoolerType currentCooler = Hardware.CoolerType.STOCK;
    public Hardware.PsuType currentPsu = Hardware.PsuType.GENERIC_300W;

    public boolean hasECC = false; // Flag for RAM regeneration

    // --- UPGRADES ---
    // Arrays allow us to step through tiers (Tier 0 = 4096, Tier 1 = 8192...)
    public int ramIndex = 0; 
    public final int[] RAM_TIERS = {4096, 8192, 16384, 32768}; 
    public final int MAX_RAM_INDEX = RAM_TIERS.length - 1;
    
    public int storageLevel = 1; 
    public final int MAX_STORAGE_LEVEL = 20;

    // BIOS TUNING VARIABLES
    public int overclockVal = 0; // Increases Damage & Watts
    public int undervoltVal = 0; // Decreases RAM Cost & Stability

    // INVENTORY MAPS
    // Map<Key, Value> stores what items we own. Value is quantity (1 = owned).
    public Map<Hardware.CpuType, Integer> cpuInventory = new HashMap<>();
    public Map<Hardware.GpuType, Integer> gpuInventory = new HashMap<>();
    public Map<Hardware.CoolerType, Integer> coolerInventory = new HashMap<>();
    public Map<Hardware.PsuType, Integer> psuInventory = new HashMap<>();
    
    public List<LogEntry> lastBattleLogs = new ArrayList<>();

    // PRIVATE CONSTRUCTOR: Prevents other classes from doing 'new GameState()'
    private GameState() {
        // Add starter items to inventory
        cpuInventory.put(Hardware.CpuType.ATHLON, 1);
        gpuInventory.put(Hardware.GpuType.INTEGRATED, 1);
        coolerInventory.put(Hardware.CoolerType.STOCK, 1);
        psuInventory.put(Hardware.PsuType.GENERIC_300W, 1);
    }

    // STATIC GETTER: The global access point.
    public static GameState get() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    // --- LOGIC CALCULATIONS ---

    // Calculates total power draw to compare against PSU limit
    public int getTotalWatts() {
        double ocMult = 1.0 + (overclockVal / 100.0); // If OC is 50, mult is 1.5
        int cpuW = (int)(currentCpu.watts * ocMult);
        return cpuW + currentGpu.watts + currentCooler.watts + 50; // +50 is base system load
    }

    public int getCurrentRamMB() { return RAM_TIERS[Math.min(ramIndex, MAX_RAM_INDEX)]; }

    // Linear HP scaling: Base 100 + (50 per storage level)
    public int getCurrentMaxHP() {
        return 100 + (storageLevel * 50);
    }
    
    // Calculates Player Damage based on Hardware
    public int calculateBaseDamage() {
        double ghz = currentCpu.freqGHz;
        double oc = 1.0 + (overclockVal / 100.0);
        // Formula: Frequency * 12 * Overclock Multiplier
        return (int)(ghz * 12 * oc);
    }
    
    // Calculates Crit Chance based on Cache
    // 1MB Cache = 1% Chance. 
    public double getCritChance() { return Math.min(1.0, currentCpu.l3CacheMB / 100.0); }

    // Inventory Helpers (Shortcut methods to add items)
    public void addCpu(Hardware.CpuType c) { cpuInventory.put(c, cpuInventory.getOrDefault(c, 0)+1); }
    public void addGpu(Hardware.GpuType g) { gpuInventory.put(g, gpuInventory.getOrDefault(g, 0)+1); }
    public void addCooler(Hardware.CoolerType c) { coolerInventory.put(c, coolerInventory.getOrDefault(c, 0)+1); }
    public void addPsu(Hardware.PsuType p) { psuInventory.put(p, psuInventory.getOrDefault(p, 0)+1); }
    
    public boolean hasCpu(Hardware.CpuType c) { return cpuInventory.getOrDefault(c, 0) > 0; }
    public boolean hasGpu(Hardware.GpuType g) { return gpuInventory.getOrDefault(g, 0) > 0; }
    public boolean hasCooler(Hardware.CoolerType c) { return coolerInventory.getOrDefault(c, 0) > 0; }
    public boolean hasPsu(Hardware.PsuType p) { return psuInventory.getOrDefault(p, 0) > 0; }

    // Simple data class to store battle history
    public static class LogEntry {
        public String task; public long start; public long dur; public String type;
        public LogEntry(String t, long s, long d, String ty) { task=t; start=s; dur=d; type=ty; }
    }
}