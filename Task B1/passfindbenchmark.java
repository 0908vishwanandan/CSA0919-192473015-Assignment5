import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Arrays;

public class PassFindBenchmark {

    private static final int REPETITIONS = 7;

    // Generate a file containing the required number of passes
    public static void generatePassFile(
            Path file, int total) throws IOException {

        try (
            BufferedWriter writer =
                Files.newBufferedWriter(
                    file,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
        ) {

            for (int i = 1; i <= total; i++) {

                String passId =
                    String.format(
                        java.util.Locale.ROOT,
                        "P%06d",
                        i
                    );

                String regNo = "REG" + i;

                String name = "Student" + i;

                int route = (i % 20) + 1;

                double amount = 4500.0;

                writer.write(
                    passId + ","
                    + regNo + ","
                    + name + ","
                    + route + ","
                    + amount
                );

                writer.newLine();
            }
        }
    }

    // Calculate median execution time
    private static double median(long[] times) {

        Arrays.sort(times);

        return times[times.length / 2] / 1_000_000.0;
    }

    public static void main(String[] args)
            throws IOException {

        int[] recordCounts = {
            1000, 10000, 50000, 100000
        };

        for (int total : recordCounts) {

            Path file = Path.of(
                "pass-search-" + total + ".csv"
            );

            // Generate records before measuring search time
            generatePassFile(file, total);

            PassStore store = new PassStore(file);

            // Search for the last record
            String target = String.format(
                java.util.Locale.ROOT,
                "P%06d",
                total
            );

            // Warm-up
            for (int i = 0; i < 2; i++) {

                store.find(target);
            }

            long[] times = new long[REPETITIONS];

            // Measure search operation
            for (int i = 0; i < REPETITIONS; i++) {

                long start = System.nanoTime();

                String[] result = store.find(target);

                times[i] =
                    System.nanoTime() - start;

                if (result == null) {

                    throw new IllegalStateException(
                        "Pass was not found."
                    );
                }
            }

            System.out.println(
                "\nNumber of Records: " + total
            );

            System.out.println(
                "Pass ID: " + target
            );

            System.out.printf(
                "Median Search Time: %.3f ms%n",
                median(times)
            );

            System.out.println(
                "------------------------------"
            );
        }
    }
}
