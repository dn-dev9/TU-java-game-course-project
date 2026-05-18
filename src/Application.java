import engine.Session;
import io.CommandParser;

public class Application {
    public static void main(String[] args) {
        Session session = new Session();
        CommandParser parser = new CommandParser(session);
        parser.run();
    }
}
