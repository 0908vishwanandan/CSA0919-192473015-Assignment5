import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PassStore {

    private final Path file;

    PassStore(Path file) {

        this.file = file;
    }

    // Add a new pass
    public void add(
            String passId,
            String regNo,
            String name,
            int route,
            double amount) throws IOException {

        if (passId == null || passId.trim().isEmpty()
                || regNo == null || regNo.trim().isEmpty()
                || name == null || name.trim().isEmpty()
                || route <= 0
                || !Double.isFinite(amount)
                || amount < 0) {

            throw new IllegalArgumentException(
                "Invalid bus-pass information."
            );
        }

        try (
            BufferedWriter w = Files.newBufferedWriter(
                file,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )
        ) {

            w.write(
                String.join(",",
                    csvField(passId),
                    csvField(regNo),
                    csvField(name),
                    String.valueOf(route),
                    String.valueOf(amount)
                )
            );

            w.newLine();
        }
    }

    /**
     * Returns the five fields of the pass with the given ID,
     * or null if the pass is not found.
     *
     * Damaged records are skipped.
     */
    public String[] find(String passId) throws IOException {

        // TODO 1 completed
        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    file,
                    StandardCharsets.UTF_8
                )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                // Parse CSV line
                String[] fields = parseCsvLine(line);

                // Skip damaged records
                if (!isValidRecord(fields)) {

                    continue;
                }

                // Compare Pass ID
                if (fields[0].equals(passId)) {

                    return fields;
                }
            }
        }

        return null;
    }

    // List all passes belonging to a route
    public List<String[]> listByRoute(int route)
            throws IOException {

        List<String[]> result = new ArrayList<>();

        try (
            BufferedReader reader =
                Files.newBufferedReader(
                    file,
                    StandardCharsets.UTF_8
                )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] fields = parseCsvLine(line);

                if (!isValidRecord(fields)) {

                    continue;
                }

                int storedRoute = Integer.parseInt(fields[3]);

                if (storedRoute == route) {

                    result.add(fields);
                }
            }
        }

        return result;
    }

    // Validate a parsed record
    private boolean isValidRecord(String[] fields) {

        if (fields == null || fields.length != 5) {

            return false;
        }

        if (fields[0].trim().isEmpty()
                || fields[1].trim().isEmpty()
                || fields[2].trim().isEmpty()) {

            return false;
        }

        try {

            int route = Integer.parseInt(fields[3]);

            double amount = Double.parseDouble(fields[4]);

            return route > 0
                && Double.isFinite(amount)
                && amount >= 0;

        } catch (NumberFormatException e) {

            return false;
        }
    }

    // Convert a CSV field into a safe text representation
    private String csvField(String value) {

        if (value.contains("\n") || value.contains("\r")) {

            throw new IllegalArgumentException(
                "Newline characters are not allowed in a field."
            );
        }

        if (value.contains(",") || value.contains("\"")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    // Parse a CSV line, including quoted commas
    private String[] parseCsvLine(String line) {

        List<String> fields = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean insideQuotes = false;

        boolean quoteClosed = false;

        for (int i = 0; i < line.length(); i++) {

            char ch = line.charAt(i);

            if (insideQuotes) {

                if (ch == '"') {

                    if (i + 1 < line.length()
                            && line.charAt(i + 1) == '"') {

                        current.append('"');

                        i++;

                    } else {

                        insideQuotes = false;

                        quoteClosed = true;
                    }

                } else {

                    current.append(ch);
                }

            } else {

                if (ch == ',') {

                    fields.add(current.toString());

                    current.setLength(0);

                    quoteClosed = false;

                } else if (ch == '"') {

                    if (current.length() == 0 && !quoteClosed) {

                        insideQuotes = true;

                    } else {

                        return null;
                    }

                } else {

                    if (quoteClosed) {

                        return null;
                    }

                    current.append(ch);
                }
            }
        }

        // Reject unfinished quoted fields
        if (insideQuotes) {

            return null;
        }

        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

    public static void main(String[] args) throws IOException {

        Path f = Files.createTempFile("passes", ".csv");

        PassStore store = new PassStore(f);

        store.add(
            "P1001", "192311001", "Arun", 12, 4500
        );

        // Add a damaged record
        Files.writeString(
            f,
            "P1002,DAMAGED LINE\n",
            StandardOpenOption.APPEND
        );

        store.add(
            "P1003", "192311003", "Divya", 7, 3800
        );

        // Find a valid record
        System.out.println(
            Arrays.toString(store.find("P1003"))
        );

        // Search for the damaged record
        System.out.println(
            Arrays.toString(store.find("P1002"))
        );

        // List passes belonging to route 12
        System.out.println("\nPasses on Route 12:");

        for (String[] pass : store.listByRoute(12)) {

            System.out.println(Arrays.toString(pass));
        }

        Files.delete(f);
    }
}
