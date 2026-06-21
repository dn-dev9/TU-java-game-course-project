package model;

import items.Armor;
import items.Item;
import items.Spell;
import items.Weapon;
import model.race.Race;

/**
 * Represents the player's hero character
 * has a race that determines base stats,
 * an equipment slot for each item type,
 * and a level up system that awards
 * 30 distributable points on each level gain.
 *
 * Base stats by race at level 1 (health is 50):
 *  human  – strength 30, mana 20
 *  mage   – strength 10, mana 40
 *  warrior – strength 40, mana 10
 *
 * Starting equipment: Sword (weapon +20%), Fireball (spell +20%), no armor.
 */
public class Hero {

    private final Race race;
    private int strength;
    private int mana;
    private int maxHealth;
    private int currentHealth; // 0 = death
    private Position position;
    private Weapon weapon;
    private Spell spell;
    private Armor armor;
    private int pendingPoints; // points after level up
    private boolean alive;

    /**
     * Starting equipment is a Sword (+20%) and Fireball (+20%).
     *
     * @param raceName "human", "mage", "warrior"
     */
    public Hero(String raceName) {
        this.race = Race.fromString(raceName);
        this.position = new Position(0, 0);
        this.alive = true;
        this.pendingPoints = 0;
        race.applyBaseStats(this);
        this.weapon = new Weapon("Sword", 20.0);
        this.spell = new Spell("Fireball", 20.0);
        this.armor = null;
    }

    // -------------
    // Combat Helpers
    // -------------

    /**
     * @return weapon bonus decimal
     */
    public double getWeaponBonus() {
        return weapon != null ? weapon.getBonusPercent() / 100.0 : 0.0;
    }

    /**
     * @return spell bonus decimal
     */
    public double getSpellBonus() {
        return spell != null ? spell.getBonusPercent() / 100.0 : 0.0;
    }

    /**
     * @return armor reduction decimal
     */
    public double getArmorReduction() {
        return armor != null ? armor.getBonusPercent() / 100.0 : 0.0;
    }

    /**
     * Applies damage to this hero after armour reduction
     * Damage =  rawDamage × (1 − armorReduction), minimum 1.
     *
     * @param rawDamage damage value
     */
    public void takeDamage(double rawDamage) {
        int actual = (int) Math.max(1, rawDamage * (1.0 - getArmorReduction()));
        currentHealth -= actual;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }

    /**
     * Restores current health to 50% of maxHealth after winning a combat.
     */
    public void restoreHealthAfterCombat() {
        currentHealth = Math.max(currentHealth, maxHealth / 2);
    }

    // -------------
    // Level up helpers
    // -------------

    /**
     * Allocates points to the specified stat using the Stat enum,
     * deducts from pending points.
     *
     * @param statName one of "strength", "mana", "health"
     * @param points   number of points to add (must be > 0 and <= pendingPoints)
     * @throws IllegalArgumentException if stat name is unknown
     */
    public void allocate(String statName, int points) {
        Stat.fromString(statName).apply(this, points);
        pendingPoints -= points;
    }

    // -------------
    // Equipment
    // -------------

    /**
     * Equips an item, replacing the previous in the same slot type.
     *
     * @param item the item to equip
     */
    public void equip(Item item) {
        item.equip(this);
    }

    // -------------
    // Getters / setters
    // -------------

    public String getRace() { return race.getName(); }

    public int getStrength() { return strength; }

    public void setStrength(int strength) { this.strength = strength; }

    public int getMana() { return mana; }

    public void setMana(int mana) { this.mana = mana; }

    public int getMaxHealth() { return maxHealth; }

    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getCurrentHealth() { return currentHealth; }

    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public Position getPosition() { return position; }

    public void setPosition(Position position) { this.position = position; }

    public Weapon getWeapon() { return weapon; }

    public void setWeapon(Weapon weapon) { this.weapon = weapon; }

    public Spell getSpell() { return spell; }

    public void setSpell(Spell spell) { this.spell = spell; }

    public Armor getArmor() { return armor; }

    public void setArmor(Armor armor) { this.armor = armor; }

    public int getPendingPoints() { return pendingPoints; }

    public void setPendingPoints(int pendingPoints) { this.pendingPoints = pendingPoints; }

    public boolean isAlive() { return alive; }

    public void setAlive(boolean alive) { this.alive = alive; }
}
