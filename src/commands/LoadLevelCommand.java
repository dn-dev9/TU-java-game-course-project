package commands;

import engine.Game;

public class LoadLevelCommand implements Command {

    private final Game game;

    public LoadLevelCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.loadLevel(arg1);
    }
}
