import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GameState {
    private static GameState instance;
    public int currency = 2500; 

    // --- HARDWARE ---
    public Hardware.MoboType currentMobo = Hardware.MoboType.STD_ATX;
    public Hardware.CpuType currentCpu = Hardware.CpuType.ATHLON;
    public Hardware.StorageType currentStorage = Hardware.StorageType.HDD_5400;
    public Hardware.CoolerType currentCooler = Hardware.CoolerType.STOCK;
    public Hardware.GpuType currentGpu = Hardware.GpuType.INTEGRATED; // New GPU
    
    public boolean useDualCpu = false;
    public boolean hasECC = false;

    // --- UPGRADES ---
    public int ramIndex = 0; 
    public final int[] RAM_TIERS = {2048, 4096, 8192, 16384, 32768, 65536};
    public int storageLevel = 1; 

    // --- SKILLS ---
    public int skillLvlFCFS = 1;
    public int skillLvlRR = 1;
    public int skillLvlSJF = 1;

    // --- BIOS ---
    public int overclockVal = 0;
    public int undervoltVal = 0;

    // --- INVENTORY ---
    public Map<Hardware.CpuType, Integer> cpuInventory = new HashMap<>();
    public Map<Hardware.MoboType, Integer> moboInventory = new HashMap<>();
    public List<LogEntry> lastBattleLogs = new ArrayList<>();

    private GameState() {
        cpuInventory.put(Hardware.CpuType.ATHLON, 1);
        moboInventory.put(Hardware.MoboType.STD_ATX, 1);
    }

    public static GameState get() {
        if (instance == null) instance = new GameState();
        return instance;
    }

    // --- CALCULATORS ---
    public int getCurrentRamMB() { return RAM_TIERS[Math.min(ramIndex, RAM_TIERS.length-1)]; }

    public int getCurrentMaxHP() {
        double progress = (storageLevel - 1) / 14.0; if(progress > 1.0) progress = 1.0;
        int added = (int)((currentStorage.maxHP - currentStorage.baseHP) * progress);
        return currentStorage.baseHP + added;
    }
    
    // NEW: Frequency Based Damage Multiplier
    public double getClockMultiplier() {
        // Base is the CPU GHz. E.g. 5.0GHz = 5.0x multiplier to base skill dmg.
        // Server CPUs (2.5GHz) have lower mult, but they rely on Cache Crits and high Core/AP counts.
        return currentCpu.freqGHz;
    }

    // NEW: Cache Based Crit Chance
    public double getCritChance() {
        // e.g. 32MB Cache = 32% crit chance.
        // Xeon (128MB) = 100% crit chance (auto-crit).
        return Math.min(1.0, currentCpu.l3CacheMB / 100.0);
    }

    // Helpers
    public void addCpu(Hardware.CpuType c) { cpuInventory.put(c, cpuInventory.getOrDefault(c, 0)+1); }
    public void addMobo(Hardware.MoboType m) { moboInventory.put(m, moboInventory.getOrDefault(m, 0)+1); }
    public int getCpuCount(Hardware.CpuType c) { return cpuInventory.getOrDefault(c, 0); }
    public boolean hasMobo(Hardware.MoboType m) { return moboInventory.getOrDefault(m, 0) > 0; }

    public static class LogEntry {
        public String task; public long start; public long dur; public String type;
        public LogEntry(String t, long s, long d, String ty) { task=t; start=s; dur=d; type=ty; }
    }
}