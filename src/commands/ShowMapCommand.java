package commands;

import engine.Game;

public class ShowMapCommand implements Command {

    private final Game game;

    public ShowMapCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.showMap();
    }

    @Override
    public String getDescription() { return "Display the dungeon map"; }
}
