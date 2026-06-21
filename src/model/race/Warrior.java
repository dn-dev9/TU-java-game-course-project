package model.race;

import model.Hero;

/** Warrior race: high strength, low mana (strength 40, mana 10, health 50). */
public class Warrior implements Race {

    @Override
    public void applyBaseStats(Hero hero) {
        hero.setStrength(40);
        hero.setMana(10);
        hero.setMaxHealth(50);
        hero.setCurrentHealth(50);
    }

    @Override
    public String getName() { return "warrior"; }
}
