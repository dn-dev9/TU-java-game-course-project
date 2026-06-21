package engine;

import commands.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Middle layer between Game and the user.
 * Handles file management and routes every
 * input line to the appropriate Command object for the current game state.
 */
public class Session {

    private final Game game = new Game();
    private String openFilename = null;
    private boolean running = true;

    private final Map<String, Command> fileCommands = new HashMap<>();
    private final Map<State, Map<String, Command>> stateCommands = new EnumMap<>(State.class);
    private final Map<State, String> fallbackMessages = new EnumMap<>(State.class);

    public Session() {
        buildFileCommands();
        buildCommandTables();
    }

    /**
     * Registers the file-level commands that are always available regardless of game state.
     */
    private void buildFileCommands() {
        fileCommands.put("open",  new OpenCommand(this));
        fileCommands.put("close", new CloseCommand(this));
        fileCommands.put("save",  new SaveCommand(this));
        fileCommands.put("help",  new HelpCommand(this, fileCommands));
        fileCommands.put("exit",  new ExitCommand(this));
    }

    /**
     * Registers Command objects for each game state.
     */
    private void buildCommandTables() {

        // IDLE
        Map<String, Command> idleCmds = new HashMap<>();
        idleCmds.put("new_game", new NewGameCommand(game));
        idleCmds.put("load_game", new LoadGameCommand(game));
        stateCommands.put(State.IDLE, idleCmds);
        fallbackMessages.put(State.IDLE, "No game in progress. Use 'new_game <race>' or 'load_game <file>'.");

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
        stateCommands.put(State.EXPLORING, exploringCmds);
        fallbackMessages.put(State.EXPLORING, null);

        // COMBAT
        Map<String, Command> combatCmds = new HashMap<>();
        combatCmds.put("attack", new AttackCommand(game));
        combatCmds.put("combat_status", new CombatStatusCommand(game));
        combatCmds.put("stats", new StatsCommand(game));
        combatCmds.put("inventory", new InventoryCommand(game));
        stateCommands.put(State.COMBAT, combatCmds);
        fallbackMessages.put(State.COMBAT, "You are in combat! Use: attack power | attack spell | combat_status");

        // LOOT_PENDING
        Map<String, Command> lootCmds = new HashMap<>();
        lootCmds.put("loot", new LootCommand(game));
        lootCmds.put("stats", new StatsCommand(game));
        lootCmds.put("inventory", new InventoryCommand(game));
        stateCommands.put(State.LOOT_PENDING, lootCmds);
        fallbackMessages.put(State.LOOT_PENDING, "You found an item! Use: loot equip | loot discard");

        // LEVEL_UP
        Map<String, Command> levelUpCmds = new HashMap<>();
        levelUpCmds.put("allocate", new AllocateCommand(game));
        levelUpCmds.put("allocate_done", new AllocateDoneCommand(game));
        levelUpCmds.put("stats", new StatsCommand(game));
        stateCommands.put(State.LEVEL_UP, levelUpCmds);

        // LEVEL_COMPLETE
        Map<String, Command> levelCompleteCmds = new HashMap<>();
        levelCompleteCmds.put("next_level", new NextLevelCommand(game));
        levelCompleteCmds.put("stats", new StatsCommand(game));
        levelCompleteCmds.put("inventory", new InventoryCommand(game));
        levelCompleteCmds.put("show_map", new ShowMapCommand(game));
        levelCompleteCmds.put("save_game", new SaveGameCommand(game));
        stateCommands.put(State.LEVEL_COMPLETE, levelCompleteCmds);
        fallbackMessages.put(State.LEVEL_COMPLETE, "Level complete! Use 'next_level' to advance, or 'save_game <file>' to save.");

        // GAME_OVER
        Map<String, Command> gameOverCmds = new HashMap<>();
        gameOverCmds.put("new_game", new NewGameCommand(game));
        gameOverCmds.put("load_game", new LoadGameCommand(game));
        stateCommands.put(State.GAME_OVER, gameOverCmds);
        fallbackMessages.put(State.GAME_OVER, "Game over. Use 'new_game <race>' to start again or 'load_game <file>'.");

        // VICTORY
        Map<String, Command> victoryCmds = new HashMap<>();
        victoryCmds.put("new_game", new NewGameCommand(game));
        victoryCmds.put("load_game", new LoadGameCommand(game));
        stateCommands.put(State.VICTORY, victoryCmds);
        fallbackMessages.put(State.VICTORY, "You have won! Use 'new_game <race>' to play again or 'exit' to quit.");
    }

    /**
     * Processes one line of user input.
     * File commands are checked first; game commands are dispatched from the state maps.
     *
     * @param input raw input string
     * @return response message to display
     */
    public String process(String input) {
        if (input == null || input.isBlank()) return "";
        String[] parts = input.trim().split("\\s+", 3);
        String cmd  = parts[0].toLowerCase();
        String arg1 = parts.length > 1 ? parts[1].toLowerCase() : "";
        String arg2 = parts.length > 2 ? parts[2].toLowerCase() : "";

        Command fileCmd = fileCommands.get(cmd);
        if (fileCmd != null) return fileCmd.execute(arg1, arg2);

        if (openFilename == null) {
            return "Error: no file is currently open. Use 'open <file>' first.";
        }

        Command handler = stateCommands.get(game.getState()).get(cmd);
        if (handler != null) return handler.execute(arg1, arg2);

        if (game.getState() == State.LEVEL_UP) {
            return "Level up! Distribute "
                    + game.getHero().getPendingPoints() + " points. Use: allocate <strength|mana|health> <points> | allocate_done";
        }

        String fallback = fallbackMessages.get(game.getState());
        return fallback != null ? fallback : "Unknown command '" + cmd + "'. Type 'help' for a list.";
    }

    // Accessors used by file command classes

    public String getOpenFilename() { return openFilename; }

    public void setOpenFilename(String filename) { openFilename = filename; }

    public void setRunning(boolean running) { this.running = running; }

    public Game getGame() { return game; }

    /**
     * Returns the command map for the current game state.
     * Used by HelpCommand to list available game commands dynamically.
     *
     * @return command map for the active State
     */
    public Map<String, Command> getCurrentStateCommands() {
        return stateCommands.get(game.getState());
    }

    public boolean isRunning() { return running; }
}
