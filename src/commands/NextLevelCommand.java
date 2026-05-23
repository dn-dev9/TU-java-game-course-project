package commands;

import engine.Game;

public class NextLevelCommand implements Command {

    private final Game game;

    public NextLevelCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.nextLevel();
    }
}
