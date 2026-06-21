package commands;

/**
 * Used to give a feedback when a command is used in wrong game state
 */
public class MessageCommand implements Command {

    private final String message;

    public MessageCommand(String message) { this.message = message; }

    @Override
    public String execute(String arg1, String arg2) {
        return message;
    }

    @Override
    public String getDescription() { return ""; }
}
