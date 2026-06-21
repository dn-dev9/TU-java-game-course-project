package commands;

import engine.Session;

import java.util.Map;

/**
 * Builds the help list from the file commands map
 * and game per state command map, printing each command description.
 */
public class HelpCommand implements Command {

    private final Session session;
    private final Map<String, Command> fileCommands;

    /**
     * @param session pulls the current game state command map
     * @param fileCommands the file commands registered in Session
     */
    public HelpCommand(Session session, Map<String, Command> fileCommands) {
        this.session = session;
        this.fileCommands = fileCommands;
    }

    @Override
    public String execute(String arg1, String arg2) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== FILE COMMANDS ===\n");
        sb.append("*** Application works with .txt files only ***\n");
        appendCommands(sb, fileCommands);

        if (session.getOpenFilename() != null) {
            sb.append("\n=== GAME COMMANDS (current state: ").append(session.getGame().getState()).append(") ===\n");
            appendCommands(sb, session.getCurrentStateCommands());
        }

        return sb.toString().stripTrailing();
    }

    private void appendCommands(StringBuilder sb, Map<String, Command> commands) {
        commands.forEach((name, cmd) -> {
            String desc = cmd.getDescription();
            if (!desc.isEmpty()) {
                sb.append(String.format("  %-5s - %s%n", name, desc));
            }
        });
    }

    @Override
    public String getDescription() { return "Show all available commands"; }
}
