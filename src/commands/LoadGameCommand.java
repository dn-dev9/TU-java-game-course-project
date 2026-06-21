package commands;

import engine.Game;

public class LoadGameCommand implements Command {

    private final Game game;

    public LoadGameCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.loadGame(arg1);
    }

    @Override
    public String getDescription() { return "Load a saved game: load_game <file>"; }
}
