package items;

import model.Hero;

/**
 * Abstract base class for all items in the game
 * Items provide a bonus percentage modifier applied during combat calculations.
 * A hero may equip one item from each type simultaneously:
 * one Weapon,
 * one Spell,
 * one Armor.
 */
public abstract class Item {

    private final String name;
    private final double bonusPercent;

    /**
     * @param name the display name of the item
     * @param bonusPercent bonus % (20.0 means +20%)
     */
    public Item(String name, double bonusPercent) {
        this.name = name;
        this.bonusPercent = bonusPercent;
    }

    public String getName() { return name; }

    public double getBonusPercent() { return bonusPercent; }

    /**
     * Returns the item type identifier string ("weapon", "spell", "armor").
     *
     * @return item type string
     */
    public abstract String getType();

    /**
     * Equips this item onto the hero placing it in the correct slot.
     *
     * @param hero the hero to equip the item on
     */
    public abstract void equip(Hero hero);

    /**
     * serialises an item preparing it for saving to a file
     * format: type name bonusPercent
     *
     * @return the save format string
     */
    public String toSaveString() {
        return getType() + " " + name + " " + bonusPercent;
    }

    /**
     * deserializes an Item from the save format string by toSaveString()
     *
     * @param line a line in the format: type name bonusPercent
     * @return the reconstructed Item, or null if the line is not in the right format
     */
    public static Item fromSaveString(String line) {
        if (line == null || line.isBlank()) return null;
        String[] parts = line.trim().split("\\s+", 3);
        if (parts.length < 3) return null;
        String type = parts[0].toLowerCase();
        String name = parts[1];
        double bonus;
        try { bonus = Double.parseDouble(parts[2]); } catch (NumberFormatException e) { return null; }
        switch (type) {
            case "weapon": return new Weapon(name, bonus);
            case "spell": return new Spell(name, bonus);
            case "armor": return new Armor(name, bonus);
            default: return null;
        }
    }

    @Override
    public String toString() {
        return name + " [" + getType() + ", +" + bonusPercent + "%]";
    }
}
