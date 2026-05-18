package engine;

import io.FileManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Middle layer between Game and FileManager commands handling user input
 */
public class Session {

    @FunctionalInterface
    private interface Command {
        String execute(String arg1, String arg2);
    }

    private final Game game = new Game();
    private String openFilename = null;
    private boolean running = true;

    private final Map<Game.State, Map<String, Command>> stateCommands = new EnumMap<>(Game.State.class);
    private final Map<Game.State, String> fallbackMessages = new EnumMap<>(Game.State.class);

    public Session() {
        buildCommandTables();
    }

    /**
     * Create the allowed command tables according to Game States
     */
    private void buildCommandTables() {

        // IDLE
        Map<String, Command> idleCmds = new HashMap<>();
        idleCmds.put("new_game", (a, b) -> game.newGame(a));
        idleCmds.put("load_game", (a, b) -> game.loadGame(a));
        stateCommands.put(Game.State.IDLE, idleCmds);
        fallbackMessages.put(Game.State.IDLE,
                "No game in progress. Use 'new_game <race>' or 'load_game <file>'.");

        // EXPLORING
        Map<String, Command> exploringCmds = new HashMap<>();
        exploringCmds.put("show_map", (a, b) -> game.showMap());
        exploringCmds.put("stats", (a, b) -> game.stats());
        exploringCmds.put("inventory", (a, b) -> game.inventory());
        exploringCmds.put("save_game", (a, b) -> game.saveGame(a));
        exploringCmds.put("load_game", (a, b) -> game.loadGame(a));
        exploringCmds.put("load_level", (a, b) -> game.loadLevel(a));
        exploringCmds.put("move", (a, b) -> game.move(a));
        exploringCmds.put("next_level", (a, b) -> "Reach the exit (E) first!");
        exploringCmds.put("combat_status", (a, b) -> "You are not in combat.");
        exploringCmds.put("attack", (a, b) -> "You are not in combat.");
        exploringCmds.put("loot", (a, b) -> "There is nothing to loot here.");
        exploringCmds.put("allocate", (a, b) -> "You have no pending level-up points.");
        exploringCmds.put("allocate_done", (a, b) -> "You have no pending level-up points.");
        stateCommands.put(Game.State.EXPLORING, exploringCmds);
        fallbackMessages.put(Game.State.EXPLORING, null);

        // COMBAT
        Map<String, Command> combatCmds = new HashMap<>();
        combatCmds.put("attack", (a, b) -> game.attack(a));
        combatCmds.put("combat_status", (a, b) -> game.combatStatus());
        combatCmds.put("stats", (a, b) -> game.stats());
        combatCmds.put("inventory", (a, b) -> game.inventory());
        stateCommands.put(Game.State.COMBAT, combatCmds);
        fallbackMessages.put(Game.State.COMBAT,
                "You are in combat! Use: attack power | attack spell | combat_status");

        // LOOT_PENDING
        Map<String, Command> lootCmds = new HashMap<>();
        lootCmds.put("loot", (a, b) -> game.loot(a));
        lootCmds.put("stats", (a, b) -> game.stats());
        lootCmds.put("inventory", (a, b) -> game.inventory());
        stateCommands.put(Game.State.LOOT_PENDING, lootCmds);
        fallbackMessages.put(Game.State.LOOT_PENDING,
                "You found an item! Use: loot equip | loot discard");

        // LEVEL_UP
        Map<String, Command> levelUpCmds = new HashMap<>();
        levelUpCmds.put("allocate", (a, b) -> game.allocate(a, b));
        levelUpCmds.put("allocate_done", (a, b) -> game.allocateDone());
        levelUpCmds.put("stats", (a, b) -> game.stats());
        stateCommands.put(Game.State.LEVEL_UP, levelUpCmds);

        // LEVEL_COMPLETE
        Map<String, Command> levelCompleteCmds = new HashMap<>();
        levelCompleteCmds.put("next_level", (a, b) -> game.nextLevel());
        levelCompleteCmds.put("stats", (a, b) -> game.stats());
        levelCompleteCmds.put("inventory", (a, b) -> game.inventory());
        levelCompleteCmds.put("show_map", (a, b) -> game.showMap());
        levelCompleteCmds.put("save_game", (a, b) -> game.saveGame(a));
        stateCommands.put(Game.State.LEVEL_COMPLETE, levelCompleteCmds);
        fallbackMessages.put(Game.State.LEVEL_COMPLETE,
                "Level complete! Use 'next_level' to advance, or 'save_game <file>' to save.");

        // GAME_OVER
        Map<String, Command> gameOverCmds = new HashMap<>();
        gameOverCmds.put("new_game", (a, b) -> game.newGame(a));
        gameOverCmds.put("load_game", (a, b) -> game.loadGame(a));
        stateCommands.put(Game.State.GAME_OVER, gameOverCmds);
        fallbackMessages.put(Game.State.GAME_OVER,
                "Game over. Use 'new_game <race>' to start again or 'load_game <file>'.");

        // VICTORY
        Map<String, Command> victoryCmds = new HashMap<>();
        victoryCmds.put("new_game", (a, b) -> game.newGame(a));
        victoryCmds.put("load_game", (a, b) -> game.loadGame(a));
        stateCommands.put(Game.State.VICTORY, victoryCmds);
        fallbackMessages.put(Game.State.VICTORY,
                "You have won! Use 'new_game <race>' to play again or 'exit' to quit.");
    }

    /**
     * Runs the appropriate handler for the command tables
     * either a global command for managing application's state
     * or a Game command
     *
     * @param input Dirty input string
     * @return Game state, File or error message
     */
    public String process(String input) {
        if (input == null || input.isBlank()) return "";
        String[] parts = input.trim().split("\\s+", 3);
        String cmd = parts[0].toLowerCase();
        String arg1 = parts.length > 1 ? parts[1].toLowerCase() : "";
        String arg2 = parts.length > 2 ? parts[2].toLowerCase() : "";

        // file management layer commands are checked before game commands
        switch (cmd) {
            case "open": return open(arg1);
            case "close": return close();
            case "save": return arg1.equalsIgnoreCase("as") ? saveAs(arg2) : save();
            case "help": return help();
            case "exit": running = false; return "Exiting the program...";
        }

        if (openFilename == null) {
            return "Error: no file is currently open. Use 'open <file>' first.";
        }

        Command handler = stateCommands.get(game.getState()).get(cmd);
        if (handler != null) {
            return handler.execute(arg1, arg2);
        }

        if (game.getState() == Game.State.LEVEL_UP) {
            return "Level up! Distribute " + game.getHero().getPendingPoints()
                    + " points. Use: allocate <strength|mana|health> <points> | allocate_done";
        }

        String fallback = fallbackMessages.get(game.getState());
        return fallback != null ? fallback : "Unknown command '" + cmd + "'. Type 'help' for a list.";
    }

    /**
     * Opens a file if not already open, restores game state from file if not empty
     * or creates a new file and sets it as the current openFilename
     *
     * @param filename
     * @return Message after operation
     */
    private String open(String filename) {
        if (filename.isBlank()) return "Usage: open <filename>";
        if (openFilename != null) return "A file is already open. Use 'close' first.";
        game.resetGameState();
        File f = new File(filename);
        if (f.exists() && f.length() > 0) {
            try { game.restore(FileManager.loadGame(filename)); }
            catch (IOException e) { return "Failed to open '" + filename + "': " + e.getMessage(); }
        } else if (!f.exists()) {
            try { new FileWriter(filename).close(); }
            catch (IOException e) { return "Failed to create '" + filename + "': " + e.getMessage(); }
        }
        openFilename = filename;
        return "Successfully opened " + filename;
    }

    /**
     * removes the saved openFilename string and resets game state
     *
     * @return Message after operation
     */
    private String close() {
        if (openFilename == null) return "Error: no file is currently open.";
        String name = openFilename;
        game.resetGameState();
        openFilename = null;
        return "Successfully closed " + name;
    }

    /**
     * Saves current game state to the opened file
     * @return Message after operation
     */
    private String save() {
        if (openFilename == null) return "Error: no file is currently open.";
        return writeToFile(openFilename);
    }

    /**
     * Saves current game state to another file
     *
     * @param filename file to save the current game
     * @return Message after operation
     */
    private String saveAs(String filename) {
        if (openFilename == null) return "Error: no file is currently open.";
        if (filename.isBlank()) return "Usage: save as <filename>";
        return writeToFile(filename);
    }

    /**
     * Saves game progress to a file
     * @param filename file to overrwrite
     * @return Message after operation
     */
    private String writeToFile(String filename) {
        if (game.getState() == Game.State.IDLE) return "Error: no game in progress to save.";
        try {
            FileManager.saveGame(filename, game.getHero(), game.getMap(),
                                 game.getCurrentLevel(), game.getState().name());
            return "Successfully saved " + filename;
        } catch (IOException e) {
            return "Failed to save '" + filename + "': " + e.getMessage();
        }
    }

    /**
     * Lists all file managing commands +
     * game commands if a file is opened
     *
     * @return Commands table string
     */
    private String help() {
        String fileHelp =
                """
                        === FILE COMMANDS ===
                        *** Application woks with .txt files only ***
                        open <file>          - Open a save file (creates it if it does not exist)
                        close                - Close the current file without saving
                        save                 - Save to the current file
                        save as <file>       - Save to a different file
                        help                 - Show this list
                        exit                 - Exit the program
                        """;
        String gameHelp = openFilename != null ? "\n" + game.help() : "";
        return fileHelp + gameHelp;
    }

    public boolean isRunning() { return running; }
}
