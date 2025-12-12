public enum Architecture {
    X86_64("x86_64", "Standard"), ARM64("ARM64", "Mobile");
    public final String label, desc;
    Architecture(String l, String d) { this.label=l; this.desc=d; }
}