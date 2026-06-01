//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Main <file1.cs> [file2.cs ...]");
            System.exit(1);
        }

        Statistics stats = new Statistics();

        for (String filename : args) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(filename)));
// удалить BOM, если есть
                if (content.startsWith("\uFEFF")) {
                    content = content.substring(1);
                }
                content = content.replace("\r\n", "\n").replace("\r", "\n");
                Lexer lexer = new Lexer(filename, content, stats);
                lexer.tokenize();
            } catch (IOException e) {
                System.err.println("Cannot read file: " + filename);
            }
        }

        stats.printStatistics();
    }
}