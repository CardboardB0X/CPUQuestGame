import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GameState {
    private static GameState instance;
    public int currency = 2000; 

    // --- ROGUELIKE STATE ---
    public int currentFloor = 1; // Starts at 1. Resets on death.

    // --- LOADOUT ---
    public Hardware.CpuType currentCpu = Hardware.CpuType.ATHLON;
    public Hardware.GpuType currentGpu = Hardware.GpuType.INTEGRATED;
    public Hardware.CoolerType currentCooler = Hardware.CoolerType.STOCK;
    public Hardware.PsuType currentPsu = Hardware.PsuType.GENERIC_300W;

    public boolean hasECC = false;

    // --- UPGRADES ---
    public int ramIndex = 0; 
    public final int[] RAM_TIERS = {4096, 8192, 16384, 32768}; // Max 32GB
    public final int MAX_RAM_INDEX = RAM_TIERS.length - 1;
    
    public int storageLevel = 1; 
    public final int MAX_STORAGE_LEVEL = 20;

    // BIOS
    public int overclockVal = 0;
    public int undervoltVal = 0;

    // INVENTORY
    public Map<Hardware.CpuType, Integer> cpuInventory = new HashMap<>();
    public Map<Hardware.GpuType, Integer> gpuInventory = new HashMap<>();
    public Map<Hardware.CoolerType, Integer> coolerInventory = new HashMap<>();
    public Map<Hardware.PsuType, Integer> psuInventory = new HashMap<>();
    
    public List<LogEntry> lastBattleLogs = new ArrayList<>();

    private GameState() {
        cpuInventory.put(Hardware.CpuType.ATHLON, 1);
        gpuInventory.put(Hardware.GpuType.INTEGRATED, 1);
        coolerInventory.put(Hardware.CoolerType.STOCK, 1);
        psuInventory.put(Hardware.PsuType.GENERIC_300W, 1);
    }

    public static GameState get() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    // --- CALCULATIONS ---
    public int getTotalWatts() {
        double ocMult = 1.0 + (overclockVal / 100.0);
        int cpuW = (int)(currentCpu.watts * ocMult);
        return cpuW + currentGpu.watts + currentCooler.watts + 50;
    }

    public int getCurrentRamMB() { return RAM_TIERS[Math.min(ramIndex, MAX_RAM_INDEX)]; }

    public int getCurrentMaxHP() {
        // Base 100 + 50 per level. Max 1100.
        return 100 + (storageLevel * 50);
    }
    
    public int calculateBaseDamage() {
        double ghz = currentCpu.freqGHz;
        double oc = 1.0 + (overclockVal / 100.0);
        return (int)(ghz * 12 * oc);
    }
    
    public double getCritChance() { return Math.min(1.0, currentCpu.l3CacheMB / 100.0); }

    // Helpers
    public void addCpu(Hardware.CpuType c) { cpuInventory.put(c, cpuInventory.getOrDefault(c, 0)+1); }
    public void addGpu(Hardware.GpuType g) { gpuInventory.put(g, gpuInventory.getOrDefault(g, 0)+1); }
    public void addCooler(Hardware.CoolerType c) { coolerInventory.put(c, coolerInventory.getOrDefault(c, 0)+1); }
    public void addPsu(Hardware.PsuType p) { psuInventory.put(p, psuInventory.getOrDefault(p, 0)+1); }
    
    public boolean hasCpu(Hardware.CpuType c) { return cpuInventory.getOrDefault(c, 0) > 0; }
    public boolean hasGpu(Hardware.GpuType g) { return gpuInventory.getOrDefault(g, 0) > 0; }
    public boolean hasCooler(Hardware.CoolerType c) { return coolerInventory.getOrDefault(c, 0) > 0; }
    public boolean hasPsu(Hardware.PsuType p) { return psuInventory.getOrDefault(p, 0) > 0; }

    public static class LogEntry {
        public String task; public long start; public long dur; public String type;
        public LogEntry(String t, long s, long d, String ty) { task=t; start=s; dur=d; type=ty; }
    }
}