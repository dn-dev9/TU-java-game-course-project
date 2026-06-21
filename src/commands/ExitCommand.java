package commands;

import engine.Session;

/** Stops the application loop. */
public class ExitCommand implements Command {

    private final Session session;

    public ExitCommand(Session session) { this.session = session; }

    @Override
    public String execute(String arg1, String arg2) {
        session.setRunning(false);
        return "Exiting the program...";
    }

    @Override
    public String getDescription() { return "Exit the program"; }
}
