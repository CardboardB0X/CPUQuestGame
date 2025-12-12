public class Hardware {
    public enum CpuType {
        CELERON("Intel Celeron", 1, 100, false, "Single Core. Potato."),
        ATHLON("AMD Athlon", 2, 200, false, "Dual Core. Basic."),
        CORE_I5("Intel Core i5", 4, 500, true, "Quad Core + HyperThreading"),
        RYZEN_5("AMD Ryzen 5", 6, 600, true, "Hexa Core + SMT"),
        CORE_I9("Intel Core i9", 8, 1000, true, "Octa Core."),
        RYZEN_9("AMD Ryzen 9", 12, 1200, true, "12 Cores."),
        XEON("Intel Xeon Gold", 8, 2500, true, "Server Grade. Supports Dual Socket.");

        public final String label;
        public final int cores, cost;
        public final boolean supportsThreads;
        public final String desc;
        CpuType(String l, int c, int cost, boolean st, String d) {
            this.label=l; this.cores=c; this.cost=cost; this.supportsThreads=st; this.desc=d;
        }
    }

    public enum MoboType {
        STD_ATX("Standard ATX", false, 1.0, 300),
        GAMING_Z("Gaming Z-Series", false, 1.2, 800), 
        SERVER_DUAL("Server Blade Dual", true, 1.0, 3000),
        QUANTUM_X("Quantum-X", true, 1.5, 5000); 

        public final String label;
        public final boolean isDualSocket;
        public final double stabilityMult;
        public final int cost;
        MoboType(String l, boolean dual, double stab, int c) {
            this.label=l; this.isDualSocket=dual; this.stabilityMult=stab; this.cost=c;
        }
    }
}