package commands;

import engine.Game;

public class LootCommand implements Command {

    private final Game game;

    public LootCommand(Game game) { this.game = game; }

    @Override
    public String execute(String arg1, String arg2) {
        return game.loot(arg1);
    }

    @Override
    public String getDescription() { return "Handle a found treasure: loot <equip|discard>"; }
}
