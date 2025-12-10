public enum Architecture {
    X86_64("x86_64 (CISC)", "High Power, High Heat"),
    ARM64("ARM64 (RISC)", "High Efficiency, Cool Running");

    public final String label;
    public final String desc;

    Architecture(String l, String d) {
        this.label = l;
        this.desc = d;
    }
}