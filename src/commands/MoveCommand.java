package commands;

import engine.Game;

public class MoveCommand implements Command {

    private final Game game;

    public MoveCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.move(arg1);
    }
}
