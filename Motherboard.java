public enum Motherboard {
    // TIER 1: STARTER
    BASIC_BOARD("OEM Green", 3, "NONE", "Standard issue. Max Upgrade Lvl 3."),
    
    // TIER 2: SPECIALIZED (Socket A)
    ECO_BOARD("EcoLogic Green", 5, "UNDERVOLT", "Efficient. Skills cost -5 RAM."),
    GAMING_BOARD("Z-Fighter Pro", 6, "OVERCLOCK", "+20% Damage Boost."),
    
    // TIER 3: WORKSTATION (Socket B)
    SERVER_BOARD("Xenon Dual-Socket", 8, "DUAL_CPU", "Supports Dual CPUs (+2 Cores)."),
    QUANTUM_BOARD("Quantum Core", 10, "SMT", "Unlocks HyperThreading & Max Lvl 10.");

    public final String name;
    public final int maxUpgradeLevel; // Caps your RAM/Storage/Freq upgrades
    public final String trait;        // The special effect
    public final String desc;

    Motherboard(String n, int max, String t, String d) {
        this.name = n; this.maxUpgradeLevel = max; this.trait = t; this.desc = d;
    }
}