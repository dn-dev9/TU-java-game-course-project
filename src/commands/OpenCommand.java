package commands;

import engine.Session;
import io.FileManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Opens a save file. Restores a saved game if the file is not empty,
 * or creates a new empty file if the file does not exist.
 */
public class OpenCommand implements Command {

    private final Session session;

    public OpenCommand(Session session) { this.session = session; }

    @Override
    public String execute(String arg1, String arg2) {
        if (arg1.isBlank()) return "Usage: open <filename>";
        if (session.getOpenFilename() != null) return "A file is already open. Use 'close' first.";
        session.getGame().resetGameState();
        File f = new File(arg1);
        if (f.exists() && f.length() > 0) {
            try { session.getGame().restore(FileManager.loadGame(arg1)); }
            catch (IOException e) { return "Failed to open '" + arg1 + "': " + e.getMessage(); }
        } else if (!f.exists()) {
            try { new FileWriter(arg1).close(); }
            catch (IOException e) { return "Failed to create '" + arg1 + "': " + e.getMessage(); }
        }
        session.setOpenFilename(arg1);
        return "Successfully opened " + arg1;
    }

    @Override
    public String getDescription() { return "Open a save file (creates it if not exists): open <file>"; }
}
