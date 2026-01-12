import java.awt.Color;

/**
 * ENUM: EnemyType
 * * Acts as the central database for all hostile entities in the game.
 * * 1. FRAGILE BUT DEADLY: 
 * Enemies generally have lower HP multipliers (0.5 - 3.0 for non-bosses), 
 * meaning they can be killed quickly if the player is aggressive (Turbo/SMT).
 * * 2. HIGH PUNISHMENT:
 * Damage multipliers are very high (2.0 - 15.0). A single hit from high-tier 
 * enemies like 'Blender' or 'Cyberpunk' can wipe out a significant chunk of 
 * player HP, making "End Turn" risky without healing.
 */
public enum EnemyType {
    
    // --- TIER 1: EARLY GAME (Stages 1-3) ---
    // Low stats, designed to teach the player mechanics.
    // Calc: Very weak, acts as "free" RAM regeneration fodder.
    CALCULATOR("Calc.exe", 0.5, 2.0, Color.LIGHT_GRAY, "Basic Process"),
    // Notepad: Slightly faster/stronger, requires actual attention.
    NOTEPAD("Notepad", 0.7, 2.2, new Color(200, 200, 255), "Text Editor"),
    
    // --- TIER 2: MID GAME (Stages 4-6) ---
    // Significant difficulty spike. High damage output requires mitigation (Cooling/Heal).
    // Chrome: The classic RAM hog. Decent bulk (1.5x HP) and moderate damage.
    CHROME("Chrome Tab", 1.5, 4.0, Color.YELLOW, "RAM Eater"),
    // VS Code: Electron app. Lower HP than Chrome but hits harder (4.5x Dmg).
    VS_CODE("VS Code", 1.2, 4.5, new Color(50, 150, 255), "Heavy Electron App"),
    
    // --- TIER 3: LATE GAME (Stages 7-9) ---
    // "Heavy Workloads". These simulate real-world intensive tasks.
    // Android Studio: The Tank. Hard to kill (3.0x HP), hits hard.
    ANDROID("Android Studio", 3.0, 6.0, new Color(0, 150, 100), "Gradle Build"),
    // Blender: The Glass Cannon. Average HP but massive 8.0x Damage. Priority target.
    BLENDER("Blender Render", 2.5, 8.0, new Color(255, 100, 0), "Cycles Engine"),
    // Premiere: Balanced high-tier threat.
    PREMIERE("Adobe Premiere", 3.5, 3.0, new Color(150, 0, 150), "4K Export"),
    
    // --- BOSSES (Floor Finales) ---
    // Zip Bomb (Stage 3): First gear check. 
    ZIP_BOMB("ZipBomb.rar", 6.0, 3.0, Color.MAGENTA, "Expansion Attack"),
    // Ransomware (Stage 6): Mid-game filter. Very high damage (5.0x).
    RANSOMWARE("WannaCry", 8.0, 5.0, new Color(150, 0, 0), "Encryptor"),
    // Cyberpunk (Stage 10): The Run Killer.
    // 15.0x HP means it takes a long time to kill.
    // 15.0x Dmg means it can two-shot a player with weak cooling/storage.
    CYBERPUNK("Cyberpunk 2077", 10.0, 15.0, Color.BLACK, "System Meltdown");

    // --- FIELDS ---
    
    // The display name shown in the Battle UI.
    public final String label;
    
    // Multiplier for Health Points.
    // Formula: BaseStageHP * hpMult = EnemyHP
    // Example: Stage 10 Base (140) * Cyberpunk (10.0) = 1400 HP.
    public final double hpMult;
    
    // Multiplier for Attack Damage.
    // Formula: BaseStageDmg * dmgMult = DamageDealt
    // Example: Stage 10 Base (25) * Cyberpunk (15.0) = 375 Damage per hit.
    public final double dmgMult;
    
    // Visual color used by GamePanel to render the enemy shape.
    public final Color color;
    
    // Flavor text description (used in logs or tooltips).
    public final String desc;

    // --- CONSTRUCTOR ---
    EnemyType(String l, double h, double d, Color c, String de) {
        this.label = l;
        this.hpMult = h;
        this.dmgMult = d;
        this.color = c;
        this.desc = de;
    }
}