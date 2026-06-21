package commands;

import engine.Game;

public class SaveGameCommand implements Command {

    private final Game game;

    public SaveGameCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.saveGame(arg1);
    }

    @Override
    public String getDescription() { return "Save the current game to a file: save_game <file>"; }
}
