import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;

public class PassStoreAdversarialTest {

    public static void main(String[] args)
            throws IOException {

        Path file = Path.of("damaged-passes.csv");

        // Create a file containing valid and damaged records
        String data =
            "P3001,192311001,Arun,12,4500.0\n" +
            "P3002,192311002,Divya,12\n" +
            "P3003,192311003,Ravi,12,ABC\n" +
            "\n" +
            "P3004,192311004,Meena,12,4200.0\n";

        Files.writeString(
            file,
            data,
            StandardCharsets.UTF_8
        );

        System.out.println(
            "Damaged bus-pass file created."
        );

        System.out.println("\nTesting PassStore:");

        PassStore store = new PassStore(file);

        // Search for a valid record
        System.out.println(
            "\nFind P3004:"
        );

        System.out.println(
            Arrays.toString(store.find("P3004"))
        );

        // Search for a record with the wrong field count
        System.out.println(
            "\nFind P3002:"
        );

        System.out.println(
            Arrays.toString(store.find("P3002"))
        );

        // Search for a record with an invalid amount
        System.out.println(
            "\nFind P3003:"
        );

        System.out.println(
            Arrays.toString(store.find("P3003"))
        );

        // List valid passes on route 12
        System.out.println(
            "\nValid passes on Route 12:"
        );

        for (String[] pass : store.listByRoute(12)) {

            System.out.println(
                Arrays.toString(pass)
            );
        }

        // Remove temporary test file
        Files.deleteIfExists(file);
    }
}
