package model;

/**
 * Base stats at level 1: strength 25, mana 25, health 50, armor 15%.
 * For each level above 1: +10 to strength, mana, and max health; +5% armor.
 */
public class Monster {

    private final int level;
    private final int strength;
    private final int mana;
    private final int maxHealth;
    private int currentHealth;
    private final double armorPercent; // damage reduction
    private final Position position; // used by CombatEngine

    /**
     * Stats are automatically derived from the level.
     *
     * @param level monster level
     * @param position grid cell occupied by this monster
     */
    public Monster(int level, Position position) {
        this.level = level;
        this.position = position;
        int bonus = (level - 1) * 10;
        this.strength = 25 + bonus;
        this.mana = 25 + bonus;
        this.maxHealth = 50 + bonus;
        this.currentHealth = maxHealth;
        this.armorPercent = 15.0 + (level - 1) * 5.0;
    }

    /**
     * Applies raw damage to monster after armour reduction
     * min damage = 1
     *
     * @param rawDamage damage value
     */
    public void takeDamage(double rawDamage) {
        double reduction = armorPercent / 100.0;
        int actual = (int) Math.max(1, rawDamage * (1.0 - reduction));
        currentHealth -= actual;
    }

    /**
     * Chooses randomly between:
     *  strength attack (0)
     *  mana attack (1).
     *
     * @return 0 for strength attack, 1 for mana attack
     */
    public int chooseAttack() {
        return (int) (Math.random() * 2);
    }

    public boolean isAlive() { return currentHealth > 0; }

    public int getLevel() { return level; }

    public int getStrength() { return strength; }

    public int getMana() { return mana; }

    public int getMaxHealth() { return maxHealth; }

    public int getCurrentHealth() { return currentHealth; }

    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public double getArmorPercent() { return armorPercent; }

    public Position getPosition() { return position; }
}
