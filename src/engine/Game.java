package engine;

import io.FileManager;
import items.Item;
import model.GameMap;
import model.Hero;
import model.Monster;
import model.Position;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Game logic and state. This class actually implements game commands logic
 */
public class Game {

    /**
     * All possible game states.
     *   IDLE – no game in progress
     *   EXPLORING – hero is moving through the map
     *   COMBAT – hero is fighting a monster
     *   LOOT_PENDING – hero stepped on a treasure and must decide equip/discard
     *   LEVEL_UP – hero advanced to the next level and must distribute stat points
     *   LEVEL_COMPLETE – hero reached the exit; waiting to proceed
     *   GAME_OVER – hero died
     *   VICTORY – hero went through all levels
     */
    public enum State {
        IDLE,
        EXPLORING,
        COMBAT,
        LOOT_PENDING,
        LEVEL_UP,
        LEVEL_COMPLETE,
        GAME_OVER,
        VICTORY
    }

     // CONSTANTS
    static final int MAX_LEVEL = 5;
    private static final Map<String, int[]> DIRECTION_DELTAS = new HashMap<>();

    static {
        DIRECTION_DELTAS.put("up", new int[]{-1,  0});
        DIRECTION_DELTAS.put("down", new int[]{ 1,  0});
        DIRECTION_DELTAS.put("left", new int[]{ 0, -1});
        DIRECTION_DELTAS.put("right", new int[]{ 0,  1});
    }

    // Instance Fields
    private State state = State.IDLE;
    private Hero hero;
    private GameMap map;
    private int currentLevel;
    private CombatEngine combatEngine;
    private Monster currentMonster;
    private Item pendingTreasure;
    private int pendingTreasureRow;
    private int pendingTreasureCol;

    /**
     * starts a new game with a hero from race, loads level 1,
     * either loaded from the level1.txt file or gets generated
     * populates the map with the hero
     * @param race the chosen hero race
     * @return Message string
     */
    public String newGame(String race) {
        if (!race.equals("human") && !race.equals("mage") && !race.equals("warrior")) {
            return "Unknown race '" + race + "'. Choose: human | mage | warrior";
        }
        hero = new Hero(race);
        currentLevel = 1;
        LevelConfig cfg = new LevelConfig(currentLevel);
        try {
            MapGenerator.generateLevelFiles();
            map = FileManager.loadMap("level" + currentLevel + ".txt");
        } catch (IOException e) {
            map = MapGenerator.generate(cfg);
        }
        MapGenerator.populate(map, cfg);
        hero.setPosition(new Position(map.getStartRow(), map.getStartCol()));
        state = State.EXPLORING;
        return "New game started! Race: " + race + "\n"
                + cfg + "\n"
                + "Hero spawns at " + hero.getPosition() + ". Exit at ("
                + map.getExitRow() + ", " + map.getExitCol() + ").\n"
                + showMap();
    }

    /**
     * Loads a previously saved game from the given file path
     *
     * @param filename path to the save file
     * @return Message string
     */
    public String loadGame(String filename) {
        if (filename.isBlank()) return "Usage: load_game <filename>";
        try {
            restore(FileManager.loadGame(filename));
            return "Game loaded from '" + filename + "'.\n" + showMap();
        } catch (IOException e) {
            return "Failed to load game: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Save file has unknown state. Loading as EXPLORING.";
        }
    }

    /**
     * Saves the current game state to the given file path.
     *
     * @param filename path to write the save file
     * @return Message string
     */
    public String saveGame(String filename) {
        if (filename.isBlank()) return "Usage: save_game <filename>";
        try {
            FileManager.saveGame(filename, hero, map, currentLevel, state.name());
            return "Game saved to '" + filename + "'.";
        } catch (IOException e) {
            return "Failed to save game: " + e.getMessage();
        }
    }

    /**
     * Loads a specific level from a file and populates it.
     *
     * @param levelStr level number
     * @return result message with map or an error string
     */
    public String loadLevel(String levelStr) {
        try {
            int n = Integer.parseInt(levelStr);
            LevelConfig cfg = new LevelConfig(n);
            map = FileManager.loadMap("level" + n + ".txt");
            MapGenerator.populate(map, cfg);
            currentLevel = n;
            hero.setPosition(new Position(map.getStartRow(), map.getStartCol()));
            state = State.EXPLORING;
            return "Level " + n + " loaded from 'level" + n + ".txt'.\n" + showMap();
        } catch (NumberFormatException e) {
            return "Usage: load_level <number>";
        } catch (IOException e) {
            return "Could not load level: " + e.getMessage();
        }
    }

    public String showMap() {
        return "=== MAP (Level " + currentLevel + ") ===\n"
                + map.render(hero.getPosition().getRow(), hero.getPosition().getCol());
    }

    /**
     * @return hero current stats and game state
     */
    public String stats() {
        return String.format(
                "=== HERO STATS ===%n" +
                "Race:     %s%n" +
                "Level:    %d%n" +
                "Strength: %d%n" +
                "Mana:     %d%n" +
                "Health:   %d / %d%n" +
                "Position: %s%n" +
                "State:    %s",
                hero.getRace(), currentLevel,
                hero.getStrength(), hero.getMana(),
                hero.getCurrentHealth(), hero.getMaxHealth(),
                hero.getPosition(), state);
    }

    /**
     * @return hero's equipped weapon, spell, and armor
     */
    public String inventory() {
        return String.format(
                "=== INVENTORY ===%n" +
                "Weapon: %s%n" +
                "Spell:  %s%n" +
                "Armor:  %s",
                hero.getWeapon() != null ? hero.getWeapon() : "none",
                hero.getSpell() != null ? hero.getSpell()  : "none",
                hero.getArmor() != null ? hero.getArmor()  : "none");
    }

    /**
     * Moves the hero one step in the given direction and
     * starts combat if a monster is on the destination,
     * picks a loot,
     * or completes the level if that's the exit
     *
     * @param direction either "up", "down", "left" or "right"
     * @return a result message
     */
    public String move(String direction) {
        int[] delta = DIRECTION_DELTAS.get(direction);
        if (delta == null) {
            return "Unknown direction '" + direction + "'. Use: up | down | left | right";
        }
        int r = hero.getPosition().getRow() + delta[0];
        int c = hero.getPosition().getCol() + delta[1];

        if (!map.inBounds(r, c)) return "You cannot move there — out of bounds.";
        if (map.isWall(r, c)) return "You cannot move there — it's a wall.";

        hero.setPosition(new Position(r, c));

        Monster m = map.monsterAt(r, c);
        if (m != null) {
            currentMonster = m;
            combatEngine = new CombatEngine(hero, m);
            state = State.COMBAT;
            return "You stepped into a dragon at " + hero.getPosition() + "!\n"
                    + combatStatus() + "\n"
                    + "Use: attack power | attack spell";
        }

        Item treasure = map.treasureAt(r, c);
        if (treasure != null) {
            pendingTreasure = treasure;
            pendingTreasureRow = r;
            pendingTreasureCol = c;
            state = State.LOOT_PENDING;
            return "You found: " + treasure + "\nUse: loot equip | loot discard";
        }

        if (r == map.getExitRow() && c == map.getExitCol()) {
            state = State.LEVEL_COMPLETE;
            return "You reached the exit! Level " + currentLevel + " complete!\n"
                    + "Use 'next_level' to advance or 'save_game <file>' to save.";
        }

        return "Moved " + direction + " to " + hero.getPosition() + ".";
    }

    /**
     * executes a combat round against the monster
     * Transitions to game state:
     *  EXPLORING on monster death or
     *  GAME_OVER on hero death.
     *
     * @param type "power" or "spell"
     * @return message explaining the fight events
     */
    public String attack(String type) {
        if (!type.equals("power") && !type.equals("spell")) {
            return "Usage: attack power | attack spell";
        }
        String result = combatEngine.executeTurn(type);
        StringBuilder sb = new StringBuilder(result);

        if (!currentMonster.isAlive()) {
            map.removeMonster(currentMonster);
            hero.restoreHealthAfterCombat();
            sb.append("The dragon is slain! Your health is restored to ")
              .append(hero.getCurrentHealth()).append("/").append(hero.getMaxHealth()).append(".\n");
            state = State.EXPLORING;
            currentMonster = null;
            combatEngine = null;

        } else if (!hero.isAlive()) {
            sb.append("You have been defeated. GAME OVER.\n");
            state = State.GAME_OVER;
            currentMonster = null;
            combatEngine = null;

        } else {
            sb.append(combatStatus());
        }
        return sb.toString();
    }

    /**
     * @return a health summary for both combatants if in combat
     */
    public String combatStatus() {
        if (combatEngine == null) return "Not in combat.";
        return combatEngine.status();
    }

    /**
     * Resolves a pending treasure pick-up
     *
     * @param choice "equip"or "discard"
     * @return confirmation message
     */
    public String loot(String choice) {
        if (pendingTreasure == null) return "No treasure pending.";
        String msg;
        if (choice.equals("equip")) {
            hero.equip(pendingTreasure);
            msg = "Equipped: " + pendingTreasure;
        } else if (choice.equals("discard")) {
            msg = "Discarded: " + pendingTreasure;
        } else {
            return "Usage: loot equip | loot discard";
        }
        map.removeTreasure(pendingTreasureRow, pendingTreasureCol);
        pendingTreasure = null;
        state = State.EXPLORING;
        return msg;
    }

    /**
     * Allocates a number of pending level-up points to the given stat
     *
     * @param stat   "strength" "mana" or "health"
     * @param amtStr string representation of the point amount
     * @return confirmation or error message
     */
    public String allocate(String stat, String amtStr) {
        if (stat.isBlank() || amtStr.isBlank()) return "Usage: allocate <strength|mana|health> <points>";
        int amt;
        try { amt = Integer.parseInt(amtStr); }
        catch (NumberFormatException e) { return "Points must be a number."; }
        if (amt <= 0) return "Points must be positive.";
        if (amt > hero.getPendingPoints()) {
            return "You only have " + hero.getPendingPoints() + " points left.";
        }
        try {
            hero.allocate(stat, amt);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        return "Allocated " + amt + " to " + stat + ". Remaining: " + hero.getPendingPoints();
    }

    /**
     * Finalises level-up point distribution if all points have been spent,
     * transitioning back to {@code EXPLORING}.
     *
     * @return confirmation with updated stats, or an error if points remain
     */
    public String allocateDone() {
        if (hero.getPendingPoints() > 0) {
            return "You still have " + hero.getPendingPoints()
                    + " points left. Allocate them all before finishing.";
        }
        state = State.EXPLORING;
        return "Level-up complete! " + stats();
    }

    /**
     * Advances the hero to the next level
     *  Loads the level file
     *  populates the map
     *  restores hero health and awards
     * 30 distributable stat points.
     * Triggers state VICTORY if already on the last level
     *
     * @return message with map display, or victory message
     */
    public String nextLevel() {
        if (currentLevel >= MAX_LEVEL) {
            state = State.VICTORY;
            return "*** YOU WIN! ***\n"
                    + "You have conquered all " + MAX_LEVEL + " levels of the dungeon!\n"
                    + "Use 'new_game <race>' to play again or 'exit' to quit.";
        }
        currentLevel++;
        LevelConfig cfg = new LevelConfig(currentLevel);
        try {
            map = FileManager.loadMap("level" + currentLevel + ".txt");
        } catch (IOException e) {
            map = MapGenerator.generate(cfg);
        }
        MapGenerator.populate(map, cfg);
        hero.setCurrentHealth(hero.getMaxHealth());
        hero.setPosition(new Position(map.getStartRow(), map.getStartCol()));
        hero.setPendingPoints(30);
        state = State.LEVEL_UP;
        return "Welcome to level " + currentLevel + "!\n"
                + cfg + "\n"
                + "Hero spawns at " + hero.getPosition() + ". Exit at ("
                + map.getExitRow() + ", " + map.getExitCol() + ").\n"
                + "You have 30 points to distribute. Use: allocate <strength|mana|health> <points>\n"
                + showMap();
    }

    /**
     * @return a list of all available game commands
     */
    public String help() {
        return "=== GAME COMMANDS ===\n"
                + "new_game <race>           - Start new game (human/mage/warrior)\n"
                + "load_game <file>          - Load saved game\n"
                + "save_game <file>          - Save current game\n"
                + "load_level <number>       - Load level from file (level<N>.txt)\n"
                + "show_map                  - Display the dungeon map\n"
                + "stats                     - Show hero statistics\n"
                + "inventory                 - Show equipped items\n"
                + "move <up|down|left|right> - Move the hero\n"
                + "attack <power|spell>      - Attack during combat\n"
                + "combat_status             - Show combat health bars\n"
                + "loot <equip|discard>      - Handle a found treasure\n"
                + "next_level                - Advance to next level (at exit)\n"
                + "allocate <stat> <pts>     - Distribute level-up points\n"
                + "allocate_done             - Confirm point distribution";
    }

    public State getState() { return state; }
    public Hero getHero() { return hero; }
    public GameMap getMap() { return map; }
    public int getCurrentLevel() { return currentLevel; }

    /**
     * Resets all game state to the initial idle
     * Called by Session when a file is closed or reopened
     */
    public void resetGameState() {
        state = State.IDLE;
        hero = null;
        map = null;
        currentLevel = 0;
        combatEngine = null;
        currentMonster = null;
        pendingTreasure = null;
    }

    /**
     * Restores game state from a loaded save file.
     * Falls back to state EXPLORING if the saved state string is unrecognised
     *
     * @param data the deserialized save data
     */
    public void restore(FileManager.SaveData data) {
        hero = data.hero;
        map = data.map;
        currentLevel = data.currentLevel;
        try { state = State.valueOf(data.state); }
        catch (IllegalArgumentException e) { state = State.EXPLORING; }
    }
}
