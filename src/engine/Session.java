package engine;

import commands.*;
import io.FileManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Middle layer between Game and the user.
 * Handles file management  and routes every
 * input line to the appropriate Command object for the current game state.
 */
public class Session {

    private final Game game = new Game();
    private String  openFilename = null;
    private boolean running = true;

    private final Map<Game.State, Map<String, Command>> stateCommands = new EnumMap<>(Game.State.class);
    private final Map<Game.State, String> fallbackMessages = new EnumMap<>(Game.State.class);

    public Session() {
        buildCommandTables();
    }

    /**
     * Registers Command objects for each game state.
     */
    private void buildCommandTables() {

        // IDLE
        Map<String, Command> idleCmds = new HashMap<>();
        idleCmds.put("new_game", new NewGameCommand(game));
        idleCmds.put("load_game", new LoadGameCommand(game));
        stateCommands.put(Game.State.IDLE, idleCmds);
        fallbackMessages.put(Game.State.IDLE, "No game in progress. Use 'new_game <race>' or 'load_game <file>'.");

        // EXPLORING
        Map<String, Command> exploringCmds = new HashMap<>();
        exploringCmds.put("show_map", new ShowMapCommand(game));
        exploringCmds.put("stats", new StatsCommand(game));
        exploringCmds.put("inventory", new InventoryCommand(game));
        exploringCmds.put("save_game", new SaveGameCommand(game));
        exploringCmds.put("load_game", new LoadGameCommand(game));
        exploringCmds.put("load_level", new LoadLevelCommand(game));
        exploringCmds.put("move", new MoveCommand(game));
        exploringCmds.put("next_level", new MessageCommand("Reach the exit (E) first!"));
        exploringCmds.put("combat_status", new MessageCommand("You are not in combat."));
        exploringCmds.put("attack", new MessageCommand("You are not in combat."));
        exploringCmds.put("loot", new MessageCommand("There is nothing to loot here."));
        exploringCmds.put("allocate", new MessageCommand("You have no pending level-up points."));
        exploringCmds.put("allocate_done", new MessageCommand("You have no pending level-up points."));
        stateCommands.put(Game.State.EXPLORING, exploringCmds);
        fallbackMessages.put(Game.State.EXPLORING, null);

        // COMBAT
        Map<String, Command> combatCmds = new HashMap<>();
        combatCmds.put("attack", new AttackCommand(game));
        combatCmds.put("combat_status", new CombatStatusCommand(game));
        combatCmds.put("stats", new StatsCommand(game));
        combatCmds.put("inventory", new InventoryCommand(game));
        stateCommands.put(Game.State.COMBAT, combatCmds);
        fallbackMessages.put(Game.State.COMBAT, "You are in combat! Use: attack power | attack spell | combat_status");

        // LOOT_PENDING
        Map<String, Command> lootCmds = new HashMap<>();
        lootCmds.put("loot", new LootCommand(game));
        lootCmds.put("stats", new StatsCommand(game));
        lootCmds.put("inventory", new InventoryCommand(game));
        stateCommands.put(Game.State.LOOT_PENDING, lootCmds);
        fallbackMessages.put(Game.State.LOOT_PENDING, "You found an item! Use: loot equip | loot discard");

        // LEVEL_UP
        Map<String, Command> levelUpCmds = new HashMap<>();
        levelUpCmds.put("allocate", new AllocateCommand(game));
        levelUpCmds.put("allocate_done", new AllocateDoneCommand(game));
        levelUpCmds.put("stats", new StatsCommand(game));
        stateCommands.put(Game.State.LEVEL_UP, levelUpCmds);

        // LEVEL_COMPLETE
        Map<String, Command> levelCompleteCmds = new HashMap<>();
        levelCompleteCmds.put("next_level", new NextLevelCommand(game));
        levelCompleteCmds.put("stats", new StatsCommand(game));
        levelCompleteCmds.put("inventory", new InventoryCommand(game));
        levelCompleteCmds.put("show_map", new ShowMapCommand(game));
        levelCompleteCmds.put("save_game", new SaveGameCommand(game));
        stateCommands.put(Game.State.LEVEL_COMPLETE, levelCompleteCmds);
        fallbackMessages.put(Game.State.LEVEL_COMPLETE, "Level complete! Use 'next_level' to advance, or 'save_game <file>' to save.");

        // GAME_OVER
        Map<String, Command> gameOverCmds = new HashMap<>();
        gameOverCmds.put("new_game", new NewGameCommand(game));
        gameOverCmds.put("load_game", new LoadGameCommand(game));
        stateCommands.put(Game.State.GAME_OVER, gameOverCmds);
        fallbackMessages.put(Game.State.GAME_OVER, "Game over. Use 'new_game <race>' to start again or 'load_game <file>'.");

        // VICTORY
        Map<String, Command> victoryCmds = new HashMap<>();
        victoryCmds.put("new_game", new NewGameCommand(game));
        victoryCmds.put("load_game", new LoadGameCommand(game));
        stateCommands.put(Game.State.VICTORY, victoryCmds);
        fallbackMessages.put(Game.State.VICTORY, "You have won! Use 'new_game <race>' to play again or 'exit' to quit.");
    }

    /**
     * Processes one line of user input.
     * File management commands are handled first
     * game commands are dispatched from the maps.
     *
     * @param input raw input string
     * @return response message to display
     */
    public String process(String input) {
        if (input == null || input.isBlank()) return "";
        String[] parts = input.trim().split("\\s+", 3);
        String cmd = parts[0].toLowerCase();
        String arg1 = parts.length > 1 ? parts[1].toLowerCase() : "";
        String arg2 = parts.length > 2 ? parts[2].toLowerCase() : "";

        // File management layer — checked before game commands
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
            return "Level up! Distribute "
                    + game.getHero().getPendingPoints() + " points. Use: allocate <strength|mana|health> <points> | allocate_done";
        }

        String fallback = fallbackMessages.get(game.getState());
        return fallback != null ? fallback : "Unknown command '" + cmd + "'. Type 'help' for a list.";
    }

    // File Management

    /**
     * Opens a file; restores the saved game if the file is not empty or
     * creates a new empty file if it does not exist.
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

    /** Closes the current file and resets game state without saving. */
    private String close() {
        if (openFilename == null) return "Error: no file is currently open.";
        String name = openFilename;
        game.resetGameState();
        openFilename = null;
        return "Successfully closed " + name;
    }

    /** Saves current game state to the open file. */
    private String save() {
        if (openFilename == null) return "Error: no file is currently open.";
        return writeToFile(openFilename);
    }

    /** Saves current game state to a different file. */
    private String saveAs(String filename) {
        if (openFilename == null) return "Error: no file is currently open.";
        if (filename.isBlank()) return "Usage: save as <filename>";
        return writeToFile(filename);
    }

    /** Writes the current game state in a file */
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

    // help

    private String help() {
        String fileHelp =
                """
                        === FILE COMMANDS ===
                        *** Application works with .txt files only ***
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
