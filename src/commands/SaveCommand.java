package commands;

import engine.Session;
import engine.State;
import io.FileManager;

import java.io.IOException;

/**
 * Saves the current game state to the open file.
 * Also handles the "save as 'fileName'" variant when arg1 is "as".
 */
public class SaveCommand implements Command {

    private final Session session;

    public SaveCommand(Session session) { this.session = session; }

    @Override
    public String execute(String arg1, String arg2) {
        if (session.getOpenFilename() == null) return "Error: no file is currently open.";
        String target = arg1.equalsIgnoreCase("as") ? arg2 : session.getOpenFilename();
        if (arg1.equalsIgnoreCase("as") && target.isBlank()) return "Usage: save as <filename>";
        return writeToFile(target);
    }

    private String writeToFile(String filename) {
        if (session.getGame().getState() == State.IDLE) return "Error: no game in progress to save.";
        try {
            FileManager.saveGame(filename, session.getGame().getHero(), session.getGame().getMap(),
                    session.getGame().getCurrentLevel(), session.getGame().getState().name());
            return "Successfully saved " + filename;
        } catch (IOException e) {
            return "Failed to save '" + filename + "': " + e.getMessage();
        }
    }

    @Override
    public String getDescription() { return "Save to current file, or 'save as <file>' to save to a new file"; }
}
