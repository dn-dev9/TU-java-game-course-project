package commands;

import engine.Game;

public class NewGameCommand implements Command {

    private final Game game;

    public NewGameCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.newGame(arg1);
    }

    @Override
    public String getDescription() { return "Start a new game: new_game <human|mage|warrior>"; }
}
