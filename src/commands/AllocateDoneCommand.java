package commands;

import engine.Game;

public class AllocateDoneCommand implements Command {

    private final Game game;

    public AllocateDoneCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.allocateDone();
    }
}
