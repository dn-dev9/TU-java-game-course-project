package items;

import model.Hero;

/**
 * Weapon item
 * When a hero chooses a Weapon attack
 * their power attack damage is multiplied by (1 + bonusPercent / 100).
 */
public class Weapon extends Item {

    /**
     * @param name display name (Sword)
     * @param bonusPercent percentage bonus applied to strength attacks
     */
    public Weapon(String name, double bonusPercent) {
        super(name, bonusPercent);
    }

    @Override
    public String getType() { return "weapon"; }

    @Override
    public void equip(Hero hero) { hero.setWeapon(this); }
}
