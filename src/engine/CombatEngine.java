package engine;

import model.Hero;
import model.Monster;

import java.util.Random;

/**
 * Handles the turn based combat logic between a  Hero and a Monster
 * how each round processes:
 *  Who attacks first is randomly chosen
 *  The hero's move is specified by the player ("power" or "spell")
 *  The monster's move is again randomly chosen
 *  the damage formula: attacker_stat × (1 + item_bonus) × (1 − defender_armor)
 */
public class CombatEngine {

    private static final Random RNG = new Random();

    private final Hero hero;
    private final Monster monster;

    public CombatEngine(Hero hero, Monster monster) {
        this.hero = hero;
        this.monster = monster;
    }

    /**
     * one full combat round.
     * The player specifies their attack type
     * the monster attacks randomly.
     * ordering of first combatant is decided randomly
     *
     * @param heroAttackType "power" ,"spell"
     * @return a message explaining th combat events
     */
    public String executeTurn(String heroAttackType) {
        boolean heroFirst = RNG.nextBoolean();
        StringBuilder sb = new StringBuilder();

        if (heroFirst) {
            sb.append(heroAttacks(heroAttackType));
            if (monster.isAlive()) sb.append(monsterAttacks());
        } else {
            sb.append(monsterAttacks());
            if (hero.isAlive()) sb.append(heroAttacks(heroAttackType));
        }
        return sb.toString();
    }

    /**
     * Calculates and applies hero to monster damage.
     *
     * @param type "power" , "spell"
     * @return descriptive string of the attack
     */
    private String heroAttacks(String type) {
        double raw;
        String attackName;
        if ("spell".equalsIgnoreCase(type)) {
            raw = hero.getMana() * (1.0 + hero.getSpellBonus());
            attackName = "spell";
        } else {
            raw = hero.getStrength() * (1.0 + hero.getWeaponBonus());
            attackName = "power";
        }
        double reduction = monster.getArmorPercent() / 100.0;
        int damage = (int) Math.max(1, raw * (1.0 - reduction));
        monster.takeDamage(raw); // takeDamage reapplies armor internally
        return String.format("  Hero uses %s attack -> %d damage to Monster (HP: %d/%d)%n",
                attackName, damage, Math.max(0, monster.getCurrentHealth()), monster.getMaxHealth());
    }

    /**
     * Calculates and applies monster to hero damage
     * The monster randomly chooses between strength and mana attack
     *
     * @return descriptive string of the attack
     */
    private String monsterAttacks() {
        int choice = monster.chooseAttack(); // 0 = strength, 1 = mana
        double raw;
        String attackName;
        if (choice == 1) {
            raw = monster.getMana();
            attackName = "mana";
        } else {
            raw = monster.getStrength();
            attackName = "power";
        }
        hero.takeDamage(raw);
        return String.format("  Monster uses %s attack -> %d effective damage to Hero (HP: %d/%d)%n",
                attackName,
                (int) Math.max(1, raw * (1.0 - hero.getArmorReduction())),
                Math.max(0, hero.getCurrentHealth()), hero.getMaxHealth());
    }

    /**
     * Returns a summary showing both Hero and Monster current health.
     *
     * @return message string
     */
    public String status() {
        return String.format("Hero HP: %d/%d | Monster HP: %d/%d (armor %.0f%%)",
                hero.getCurrentHealth(), hero.getMaxHealth(),
                monster.getCurrentHealth(), monster.getMaxHealth(),
                monster.getArmorPercent());
    }
}
