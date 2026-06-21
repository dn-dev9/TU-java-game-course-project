package model;

/**
 * Allocatable hero statistics.
 * Each constant knows how to add points to itself on a Hero,
 *
 */
public enum Stat {

    STRENGTH {
        @Override
        public void apply(Hero hero, int points) {
            hero.setStrength(hero.getStrength() + points);
        }
    },
    MANA {
        @Override
        public void apply(Hero hero, int points) {
            hero.setMana(hero.getMana() + points);
        }
    },
    HEALTH {
        @Override
        public void apply(Hero hero, int points) {
            hero.setMaxHealth(hero.getMaxHealth() + points);
            hero.setCurrentHealth(hero.getCurrentHealth() + points);
        }
    };

    /**
     * Adds points to the corresponding stat on  hero.
     *
     * @param hero the hero to modify
     * @param points number of points to add
     */
    public abstract void apply(Hero hero, int points);

    /**
     * Parses a stat name string  into the matching constant
     *
     * @param name "strength", "mana", or "health"
     * @return the matching Stat
     * @throws IllegalArgumentException if the name does not match any stat
     */
    public static Stat fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown stat: " + name);
        }
    }
}
