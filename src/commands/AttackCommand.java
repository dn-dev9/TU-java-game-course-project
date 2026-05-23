package commands;

import engine.Game;

public class AttackCommand implements Command {

    private final Game game;

    public AttackCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.attack(arg1);
    }
}
