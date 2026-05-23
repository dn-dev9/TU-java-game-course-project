package commands;

import engine.Game;

public class StatsCommand implements Command {

    private final Game game;

    public StatsCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.stats();
    }
}
