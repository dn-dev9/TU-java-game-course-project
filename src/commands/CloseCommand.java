package commands;

import engine.Session;

/** Closes the current open file and resets game state without saving */
public class CloseCommand implements Command {

    private final Session session;

    public CloseCommand(Session session) { this.session = session; }

    @Override
    public String execute(String arg1, String arg2) {
        if (session.getOpenFilename() == null) return "Error: no file is currently open.";
        String name = session.getOpenFilename();
        session.getGame().resetGameState();
        session.setOpenFilename(null);
        return "Successfully closed " + name;
    }

    @Override
    public String getDescription() { return "Close the current file without saving"; }
}
