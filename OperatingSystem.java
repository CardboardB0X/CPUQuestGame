public enum OperatingSystem {
    WINDOWS("Windows 11", "Heavy GUI. 'Bloatware' (Chrome/Teams) is 20% stronger."),
    LINUX("Ubuntu Server", "Lightweight. Background tasks are 20% weaker."),
    MACOS("macOS Sonoma", "Optimized. RAM regeneration is +5 faster.");

    public final String label;
    public final String desc;

    OperatingSystem(String l, String d) {
        this.label = l;
        this.desc = d;
    }
}