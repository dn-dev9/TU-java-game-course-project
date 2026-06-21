package commands;

import engine.Game;

public class LoadLevelCommand implements Command {

    private final Game game;

    public LoadLevelCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.loadLevel(arg1);
    }

    @Override
    public String getDescription() { return "Load a level from file: load_level <number>"; }
}
