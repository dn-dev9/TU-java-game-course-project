package commands;

import engine.Game;

public class CombatStatusCommand implements Command {

    private final Game game;

    public CombatStatusCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.combatStatus();
    }

    @Override
    public String getDescription() { return "Show combat health bars"; }
}
