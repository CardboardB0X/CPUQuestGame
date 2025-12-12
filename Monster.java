public class Monster {
    public String name;
    public EnemyType type;
    public int maxHP, currentHP;
    public boolean isHit = false, isDead = false;
    public Monster(String id, EnemyType t, int hp) {
        this.name = t.label; this.type = t; this.maxHP = hp; this.currentHP = hp;
    }
}