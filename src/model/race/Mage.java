package model.race;

import model.Hero;

/** Mage race: high mana, low strength (strength 10, mana 40, health 50). */
public class Mage implements Race {

    @Override
    public void applyBaseStats(Hero hero) {
        hero.setStrength(10);
        hero.setMana(40);
        hero.setMaxHealth(50);
        hero.setCurrentHealth(50);
    }

    @Override
    public String getName() { return "mage"; }
}
