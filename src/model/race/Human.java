package model.race;

import model.Hero;

/** Human race: balanced stats (strength 30, mana 20, health 50). */
public class Human implements Race {

    @Override
    public void applyBaseStats(Hero hero) {
        hero.setStrength(30);
        hero.setMana(20);
        hero.setMaxHealth(50);
        hero.setCurrentHealth(50);
    }

    @Override
    public String getName() { return "human"; }
}
