public class Hardware {

    // --- CPUs (Added GHz and Cache) ---
    public enum CpuType {
        // ENTRY
        CELERON("Intel Celeron", 1, 100, false, 3.2, 2, "High clock for its class, but single core."),
        ATHLON("AMD Athlon", 2, 200, false, 3.5, 4, "Decent entry dual-core."),
        
        // MID (Consumer)
        CORE_I5("Intel Core i5", 4, 500, true, 4.1, 12, "Good balance of Speed (4.1GHz) and Cache."),
        RYZEN_5("AMD Ryzen 5", 6, 600, true, 4.2, 16, "All-rounder. 4.2GHz."),
        
        // HIGH END (Consumer - High Freq, Med Cores)
        CORE_I9("Intel Core i9", 8, 1200, true, 5.2, 32, "GAMING KING. 5.2GHz Speed! High Burst Damage."),
        RYZEN_9("AMD Ryzen 9", 12, 1500, true, 4.8, 64, "Workstation. 4.8GHz + Huge 64MB Cache."),
        
        // SERVER (Low Freq, High Cores, Massive Cache)
        XEON("Intel Xeon Platinum", 8, 3000, true, 2.5, 128, "SERVER. Low 2.5GHz Speed, but DUAL SOCKET + 128MB Cache (Auto-Crit).");

        public final String label;
        public final int cores;
        public final int cost;
        public final boolean supportsThreads;
        public final double freqGHz; // Multiplier for Damage
        public final int l3CacheMB;  // Crit Chance %
        public final String desc;

        CpuType(String l, int c, int cost, boolean st, double ghz, int cache, String d) {
            this.label=l; this.cores=c; this.cost=cost; this.supportsThreads=st; 
            this.freqGHz=ghz; this.l3CacheMB=cache; this.desc=d;
        }
    }

    // --- GPUs (New Component) ---
    public enum GpuType {
        INTEGRATED("Integrated Graphics", 0, "Static Background", "Basic display."),
        GTX_1050("NVIDIA GTX 1050", 400, "Floating Particles", "Adds basic particle effects."),
        RTX_3060("NVIDIA RTX 3060", 800, "RGB Wave", "Smooth color shifting background."),
        RTX_4090("NVIDIA RTX 4090", 2000, "Plasma Storm", "Intense, high-speed animated background.");

        public final String label;
        public final int cost;
        public final String visualStyle;
        public final String desc;
        GpuType(String l, int c, String v, String d) {
            this.label=l; this.cost=c; this.visualStyle=v; this.desc=d;
        }
    }

    // --- MOTHERBOARDS (Unchanged) ---
    public enum MoboType {
        STD_ATX("Standard ATX", false, 1.0, 300, 8192, "Max 8GB RAM."),
        GAMING_Z("Gaming Z-Series", false, 1.2, 800, 16384, "Max 16GB RAM. OC Ready."),
        SERVER_DUAL("Server Blade", true, 1.0, 3000, 32768, "Dual CPU Support. Max 32GB RAM."),
        QUANTUM_X("Quantum-X", true, 1.5, 5000, 65536, "Experimental. Max 64GB RAM.");

        public final String label;
        public final boolean isDualSocket;
        public final double stabilityMult;
        public final int cost;
        public final int maxRamMB;
        public final String desc;
        MoboType(String l, boolean dual, double stab, int c, int mr, String d) {
            this.label=l; this.isDualSocket=dual; this.stabilityMult=stab; this.cost=c; this.maxRamMB=mr; this.desc=d;
        }
    }

    // --- STORAGE (HDD vs SSD) ---
    public enum StorageType {
        HDD_5400("HDD 5400RPM", 50000, 512000, 0.0, "Massive HP. 0% Defense."),
        HDD_7200("HDD 7200RPM", 75000, 512000, 0.05, "Faster. 5% Armor."),
        SSD_SATA("SATA SSD", 25000, 256000, 0.20, "Fast. 20% Dmg Reduction."),
        SSD_NVME("NVMe SSD", 35000, 256000, 0.35, "Ultra Fast. 35% Dmg Reduction.");

        public final String label;
        public final int baseHP;
        public final int maxHP; 
        public final double defense; 
        public final String desc;
        StorageType(String l, int b, int m, double d, String de) {
            this.label=l; this.baseHP=b; this.maxHP=m; this.defense=d; this.desc=de;
        }
    }

    // --- COOLING ---
    public enum CoolerType {
        STOCK("Stock Fan", 20, 0, "Basic. +20 HP Regen."),
        AIR_TOWER("Hyper Air", 100, 500, "Good Airflow. +100 HP Regen."),
        AIO_LIQUID("AIO Liquid 240", 250, 1200, "Water Cooling. +250 HP Regen."),
        LN2_EXTREME("Liquid Nitrogen", 500, 3000, "Sub-Zero. +500 HP Regen.");

        public final String label;
        public final int regen;
        public final int cost;
        public final String desc;
        CoolerType(String l, int r, int c, String d) {
            this.label=l; this.regen=r; this.cost=c; this.desc=d;
        }
    }
}