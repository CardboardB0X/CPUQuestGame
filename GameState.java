import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class GameState {
    private static GameState instance;
    public int currency = 1000;

    public Hardware.MoboType currentMobo = Hardware.MoboType.STD_ATX;
    public Hardware.CpuType currentCpu = Hardware.CpuType.ATHLON;
    public boolean useDualCpu = false; 

    public int skillLvlFCFS = 1;
    public int skillLvlRR = 1;
    public int skillLvlSJF = 1;

    public int overclockVal = 0;
    public int undervoltVal = 0;

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
    
    public void addCpu(Hardware.CpuType c) { cpuInventory.put(c, cpuInventory.getOrDefault(c, 0) + 1); }
    public void addMobo(Hardware.MoboType m) { moboInventory.put(m, moboInventory.getOrDefault(m, 0) + 1); }
    public int getCpuCount(Hardware.CpuType c) { return cpuInventory.getOrDefault(c, 0); }
    public boolean hasMobo(Hardware.MoboType m) { return moboInventory.getOrDefault(m, 0) > 0; }

    public static class LogEntry {
        public String task; public long start; public long dur; public String type;
        public LogEntry(String t, long s, long d, String ty) { task=t; start=s; dur=d; type=ty; }
    }
}