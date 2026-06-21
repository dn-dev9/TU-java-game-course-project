package commands;

/**
 * Interface for all commands.
 * Each command object encapsulates one player action.
 */
public interface Command {
    /**
     * Executes the command and returns a response message to be displayed
     *
     * @param arg1 first argument token (may be empty)
     * @param arg2 second argument token (may be empty)
     * @return result message
     */
    String execute(String arg1, String arg2);

    /**
     * Used by HelpCommand to build the help listing dynamically.
     *
     * @return a short description of what this command does, or "" to hide from help
     */
    String getDescription();
}
