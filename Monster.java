import java.awt.Color;

public class Monster {
    public String name;       
    public EnemyType type;
    public int maxHP;         
    public int currentHP;
    
    // Animation flags
    public boolean isHit = false;
    public boolean isDead = false;

    public Monster(String id, EnemyType type, int base) {
        this.name = type.label; 
        this.type = type;
        this.maxHP = (int)(base * type.hpMult);
        this.currentHP = this.maxHP;
    }
}