import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ReadAllLinesCounter {

    public static void main(String[] args) {

        Path file = Path.of("attendance.log");

        try {

            // Read the entire file into a List
            List<String> lines =
                Files.readAllLines(
                    file,
                    StandardCharsets.UTF_8
                );

            // Count the lines
            System.out.println(
                "Total number of lines: " + lines.size()
            );

        } catch (IOException e) {

            System.out.println(
                "File reading error: " + e.getMessage()
            );
        }
    }
}
