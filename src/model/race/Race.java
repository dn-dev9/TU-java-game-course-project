package model.race;

import model.Hero;

/**
 * Defines a hero race.
 * Each concrete implementation sets its own base stat values on the Hero,
 */
public interface Race {

    /**
     * Applies this race's base stat values to the given hero.
     *
     * @param hero the hero to initialise
     */
    void applyBaseStats(Hero hero);

    /**
     * Returns the name of this race ("human", "mage", "warrior").
     *
     * @return race name string
     */
    String getName();

    /**
     * Factory method: creates the correct Race from a name string.
     * Defaults = Human
     *
     * @param name race name
     * @return Race instance
     */
    static Race fromString(String name) {
        switch (name.toLowerCase()) {
            case "mage":    return new Mage();
            case "warrior": return new Warrior();
            default:        return new Human();
        }
    }
}
