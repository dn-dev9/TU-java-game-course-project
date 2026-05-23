package commands;

import engine.Game;

public class AllocateCommand implements Command {

    private final Game game;

    public AllocateCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.allocate(arg1, arg2);
    }
}
