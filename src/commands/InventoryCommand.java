package commands;

import engine.Game;

public class InventoryCommand implements Command {

    private final Game game;

    public InventoryCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.inventory();
    }
}
