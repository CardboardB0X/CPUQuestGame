public class Hardware {

    // ENUM: A special Java type used to define collections of constants.
    // Here, it acts as a database of all available CPUs.
    public enum CpuType {
        // defined constants with specific properties (Name, Cores, Cost, Threading, GHz, Cache, Watts, Description)
        CELERON("Intel Celeron", 1, 100, false, 3.2, 2, 65, "Entry Level. 65W."),
        ATHLON("AMD Athlon", 2, 200, false, 3.5, 4, 75, "Basic Dual Core. 75W."),
        CORE_I5("Intel Core i5", 4, 600, true, 4.0, 12, 125, "Mid Range. 125W."),
        RYZEN_5("AMD Ryzen 5", 6, 750, true, 4.2, 16, 140, "Efficient. 140W."),
        CORE_I9("Intel Core i9", 8, 1500, true, 5.5, 32, 250, "Power Hungry. 250W."),
        RYZEN_9("AMD Ryzen 9", 12, 1800, true, 5.0, 64, 200, "Workstation. 200W."),
        XEON("Intel Xeon Gold", 8, 3500, true, 3.0, 128, 300, "Server Grade. 300W.");

        // 'final' means these variables cannot be changed after the game starts.
        public final String label;
        public final int cores, cost, watts;
        public final boolean supportsThreads; // Used to unlock the "SMT" skill
        public final double freqGHz;          // Used for Base Damage calculation
        public final int l3CacheMB;           // Used for Critical Hit Chance
        public final String desc;

        // CONSTRUCTOR: This runs when the Enum constant is created.
        // It assigns the values (e.g., "Intel Celeron", 1, 100...) to the internal variables.
        CpuType(String l, int c, int cost, boolean st, double ghz, int cache, int w, String d) {
            this.label=l; this.cores=c; this.cost=cost; this.supportsThreads=st; 
            this.freqGHz=ghz; this.l3CacheMB=cache; this.watts=w; this.desc=d;
        }
    }

    // --- GPUs (Graphics Cards) ---
    // 'visualStyle' is a string used by GamePanel to decide what background animation to draw.
    public enum GpuType {
        INTEGRATED("Integrated Graphics", 0, "Static", 15, "Low Power."),
        GTX_1050("GTX 1050", 400, "Particles", 75, "75W TDP."),
        RTX_3060("RTX 3060", 800, "RGB Wave", 170, "170W TDP."),
        RTX_4090("RTX 4090", 2000, "Plasma", 450, "Power Monster. 450W.");

        public final String label;
        public final int cost, watts;
        public final String visualStyle;
        public final String desc;
        GpuType(String l, int c, String v, int w, String d) {
            this.label=l; this.cost=c; this.visualStyle=v; this.watts=w; this.desc=d;
        }
    }

    // --- COOLING ---
    // 'regen' determines how much HP the player recovers automatically at the end of a turn.
    public enum CoolerType {
        STOCK("Stock Fan", 15, 0, 5, "Basic. 5W."),
        AIR_TOWER("Hyper Air", 60, 500, 20, "Tower Cooler. 20W."),
        AIO_LIQUID("AIO Liquid 240", 120, 1200, 50, "Pump + Fans. 50W."),
        LN2_EXTREME("Liquid Nitrogen", 300, 3000, 200, "Compressor. 200W.");

        public final String label;
        public final int regen, cost, watts;
        public final String desc;
        CoolerType(String l, int r, int c, int w, String d) {
            this.label=l; this.regen=r; this.cost=c; this.watts=w; this.desc=d;
        }
    }

    // --- POWER SUPPLY (PSU) ---
    // 'maxWatts' is the limit. If system parts usage > maxWatts, the game won't let you deploy.
    public enum PsuType {
        GENERIC_300W("Generic 300W", 300, 0),
        BRONZE_500W("Bronze 500W", 500, 400),
        GOLD_850W("Gold 850W", 850, 1000),
        TITANIUM_1600W("Titanium 1600W", 1600, 2500);

        public final String label;
        public final int maxWatts;
        public final int cost;
        PsuType(String l, int mw, int c) {
            this.label=l; this.maxWatts=mw; this.cost=c;
        }
    }
}