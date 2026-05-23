package io;

import items.Armor;
import items.Item;
import items.Spell;
import items.Weapon;
import model.Cell;
import model.GameMap;
import model.Hero;
import model.Monster;
import model.Position;

import java.io.*;
import java.util.*;

/**
 * Handles all file input and output for the application.
 * Save format is key=value one per line.
 * For example:
 *
 * hero.race=human
 * hero.strength=30
 * hero.mana=20
 * hero.maxHealth=50
 * hero.currentHealth=25
 * hero.row=1
 * hero.col=1
 * hero.pendingPoints=0
 * hero.weapon=Sword|20.0        (NONE if empty)
 * hero.spell=Fireball|20.0
 * hero.armor=NONE
 * level=2
 * state=EXPLORING
 * grid.rows=11
 * grid.cols=11
 * grid.0=###########
 * grid.1=#.........#
 * ...
 * exit=9|9
 * start=1|1
 * monster.0=row|col|level|hp
 * treasure.0=row|col|type|name|bonus
 */
public class FileManager {

    /**
     * Loads just the map from a level file containing only wall '#' and walkable '.'
     * characters. Then use MapGenerator.populate() to add the game entities
     *
     * @param path path to the level file
     * @return GameMap containing only the raw cell grid
     * @throws IOException if the file cannot be read or is empty
     */
    public static GameMap loadMap(String path) throws IOException {
        List<String> lines = readLines(path);
        if (lines.isEmpty()) throw new IOException("Map file is empty: " + path);

        int cols = lines.stream().mapToInt(String::length).max().orElse(0);
        List<List<Cell>> cells = new ArrayList<>();
        for (String line : lines) {
            List<Cell> row = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                char ch = c < line.length() ? line.charAt(c) : '#';
                row.add(charToCell(ch));
            }
            cells.add(row);
        }
        return new GameMap(cells);
    }

    /**
     * Writes a raw game grid to a file, one row per line.
     * Only '#' and '.' characters are expected, no game entities are saved
     *
     * @param cells the cell grid to write
     * @param path  destination file path
     * @throws IOException if the file cannot be written
     */
    public static void saveMaze(List<List<Cell>> cells, String path) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (List<Cell> row : cells) {
                StringBuilder sb = new StringBuilder();
                for (Cell cell : row)
                    sb.append(cell.isWall() ? '#' : '.');
                pw.println(sb.toString());
            }
        }
    }

    /**
     * Saves the complete current game state to a text file in a (key=value) format.
     * Monsters and treasures are written as indexed entries monster.0 , treasure.0.
     * Only living monsters are saved
     *
     * @param path destination file path
     * @param hero the hero whose stats and equipment to save
     * @param map the current GameMap
     * @param currentLevel the level number
     * @param state the Game.State name string
     * @throws IOException if the file cannot be written
     */
    public static void saveGame(String path, Hero hero, GameMap map,
                                int currentLevel, String state) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("hero.race=" + hero.getRace());
            pw.println("hero.strength=" + hero.getStrength());
            pw.println("hero.mana=" + hero.getMana());
            pw.println("hero.maxHealth=" + hero.getMaxHealth());
            pw.println("hero.currentHealth=" + hero.getCurrentHealth());
            pw.println("hero.row=" + hero.getPosition().getRow());
            pw.println("hero.col=" + hero.getPosition().getCol());
            pw.println("hero.pendingPoints=" + hero.getPendingPoints());
            pw.println("hero.weapon=" + itemLine(hero.getWeapon()));
            pw.println("hero.spell=" + itemLine(hero.getSpell()));
            pw.println("hero.armor=" + itemLine(hero.getArmor()));
            pw.println("level=" + currentLevel);
            pw.println("state=" + state);
            pw.println("grid.rows=" + map.getRows());
            pw.println("grid.cols=" + map.getCols());

            List<List<Cell>> cells = map.getCells();
            for (int r = 0; r < map.getRows(); r++) {
                StringBuilder sb = new StringBuilder();
                for (Cell cell : cells.get(r)) sb.append(cell.toChar());
                pw.println("grid." + r + "=" + sb);
            }

            pw.println("exit=" + map.getExitRow() + "|" + map.getExitCol());
            pw.println("start=" + map.getStartRow() + "|" + map.getStartCol());

            int mi = 0;
            for (Monster m : map.getMonsters()) {
                if (m.isAlive()) {
                    pw.println("monster." + mi++ + "="
                            + m.getPosition().getRow() + "|" + m.getPosition().getCol()
                            + "|" + m.getLevel() + "|" + m.getCurrentHealth());
                }
            }

            int ti = 0;
            for (Map.Entry<String, Item> e : map.getTreasures().entrySet()) {
                String[] rc = e.getKey().split(",");
                Item item = e.getValue();
                pw.println("treasure." + ti++ + "="
                        + rc[0] + "|" + rc[1] + "|"
                        + item.getType() + "|" + item.getName() + "|" + item.getBonusPercent());
            }
        }
    }

    /**
     * Reads a save file written by saveGame() and reconstructs the full game state.
     *
     * @param path path to the save file
     * @return a populated SaveData container
     * @throws IOException if the file cannot be read or a required key is missing
     */
    public static SaveData loadGame(String path) throws IOException {
        Map<String, String> kv = readKeyValues(path);
        SaveData data = new SaveData();

        // Hero stats
        data.hero = new Hero(get(kv, "hero.race"));
        data.hero.setStrength(getInt(kv, "hero.strength"));
        data.hero.setMana(getInt(kv, "hero.mana"));
        data.hero.setMaxHealth(getInt(kv, "hero.maxHealth"));
        data.hero.setCurrentHealth(getInt(kv, "hero.currentHealth"));
        data.hero.setPosition(new Position(getInt(kv, "hero.row"), getInt(kv, "hero.col")));
        data.hero.setPendingPoints(getInt(kv, "hero.pendingPoints"));

        // Equipment
        String w = get(kv, "hero.weapon");
        data.hero.setWeapon(w.equalsIgnoreCase("NONE") ? null
                : (Weapon) Item.fromSaveString("weapon " + w.replace("|", " ")));

        String s = get(kv, "hero.spell");
        data.hero.setSpell(s.equalsIgnoreCase("NONE") ? null
                : (Spell) Item.fromSaveString("spell " + s.replace("|", " ")));

        String a = get(kv, "hero.armor");
        data.hero.setArmor(a.equalsIgnoreCase("NONE") ? null
                : (Armor) Item.fromSaveString("armor " + a.replace("|", " ")));

        // Level and state
        data.currentLevel = getInt(kv, "level");
        data.state = get(kv, "state");

        // Grid — build Cell objects from saved characters
        int rows = getInt(kv, "grid.rows");
        int cols = getInt(kv, "grid.cols");
        List<List<Cell>> cells = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            String line = get(kv, "grid." + r);
            List<Cell> row = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                char ch = c < line.length() ? line.charAt(c) : '#';
                row.add(charToCell(ch));
            }
            cells.add(row);
        }
        data.map = new GameMap(cells);

        // Exit and start coordinates
        String[] exit = get(kv, "exit").split("\\|");
        data.map.setExitRow(Integer.parseInt(exit[0]));
        data.map.setExitCol(Integer.parseInt(exit[1]));

        String[] start = get(kv, "start").split("\\|");
        data.map.setStartRow(Integer.parseInt(start[0]));
        data.map.setStartCol(Integer.parseInt(start[1]));

        // Monsters
        int mi = 0;
        while (kv.containsKey("monster." + mi)) {
            String[] p = kv.get("monster." + mi).split("\\|");
            Monster m = new Monster(Integer.parseInt(p[2]),
                    new Position(Integer.parseInt(p[0]), Integer.parseInt(p[1])));
            m.setCurrentHealth(Integer.parseInt(p[3]));
            data.map.addMonster(m);
            mi++;
        }

        // Treasures
        int ti = 0;
        while (kv.containsKey("treasure." + ti)) {
            String[] p = kv.get("treasure." + ti).split("\\|", 5);
            Item item = Item.fromSaveString(p[2] + " " + p[3] + " " + p[4]);
            if (item != null)
                data.map.addTreasure(Integer.parseInt(p[0]), Integer.parseInt(p[1]), item);
            ti++;
        }

        return data;
    }

    // Private helpers

    /** Converts a saved character to the appropriate Cell type. */
    private static Cell charToCell(char ch) {
        if (ch == '#') return new Cell(Cell.Type.WALL);
        if (ch == 'E') return new Cell(Cell.Type.EXIT);
        return new Cell(Cell.Type.FLOOR);
    }

    /** Returns "NONE" for a null item, otherwise "name|bonusPercent". */
    private static String itemLine(Item item) {
        if (item == null) return "NONE";
        return item.getName() + "|" + item.getBonusPercent();
    }

    /** Reads a key=value file into an ordered map, splitting on the first '='. */
    private static Map<String, String> readKeyValues(String path) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                int eq = line.indexOf('=');
                if (eq > 0) map.put(line.substring(0, eq), line.substring(eq + 1));
            }
        }
        return map;
    }

    /** Returns the value for a key, or throws if the key is absent. */
    private static String get(Map<String, String> kv, String key) throws IOException {
        String v = kv.get(key);
        if (v == null) throw new IOException("Missing key in save file: " + key);
        return v;
    }

    /** Parses the value for a key as an integer. */
    private static int getInt(Map<String, String> kv, String key) throws IOException {
        return Integer.parseInt(get(kv, key));
    }

    /** Reads all non-blank lines from a file into a list. */
    private static List<String> readLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
        }
        return lines;
    }

    // Data container

    /** Plain data container returned by loadGame(). */
    public static class SaveData {
        public Hero hero;
        public GameMap map;
        public int currentLevel;
        public String state = "EXPLORING";
    }
}
