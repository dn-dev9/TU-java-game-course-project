package items;

/**
 * Spell item that multiplies the hero's mana-based attack damage.
 * When a hero chooses spell attack, their spell attack damage is multiplied by (1 + bonusPercent / 100)
 */
public class Spell extends Item {

    /**
     * @param name display name (Fireball)
     * @param bonusPercent percentage bonus applied to mana attacks
     */
    public Spell(String name, double bonusPercent) {
        super(name, bonusPercent);
    }

    @Override
    public String getType() { return "spell"; }
}
