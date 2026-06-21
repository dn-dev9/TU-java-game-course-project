package items;

import model.Hero;

/**
 * Armor item
 * all incoming damage is calculated by (1 - bonusPercent / 100) => reducing it by the bonus percentage.
 */
public class Armor extends Item {

    /**
     * @param name display name (Shield)
     * @param bonusPercent percentage damage reduction (15.0 for 15%)
     */
    public Armor(String name, double bonusPercent) {
        super(name, bonusPercent);
    }

    @Override
    public String getType() { return "armor"; }

    @Override
    public void equip(Hero hero) { hero.setArmor(this); }
}
