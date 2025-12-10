public class Monster {
    public String name;       
    public EnemyType type;
    public int maxHP;         
    public int currentHP;     
    public int waitingTime;   
    public boolean isDead = false;

    public Monster(String id, EnemyType type, int baseHP) {
        this.name = type.label; 
        this.type = type;
        this.maxHP = (int)(baseHP * type.hpMult);
        this.currentHP = this.maxHP;
        this.waitingTime = 0;
    }
}