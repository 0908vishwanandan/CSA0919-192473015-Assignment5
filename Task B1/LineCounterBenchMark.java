import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;

public class LineCounterBenchmark {

    private static final int BUFFER_SIZE = 64 * 1024;

    private static final int REPETITIONS = 3;

    // Generate a test file of the required size
    public static void generateFile(
            Path file, long sizeBytes) throws IOException {

        byte[] block = new byte[BUFFER_SIZE];

        // Create sample attendance-log content
        for (int i = 0; i < block.length; i++) {

            block[i] = (byte)
                (i % 80 == 79 ? '\n' : 'A');
        }

        try (
            OutputStream out =
                new BufferedOutputStream(
                    Files.newOutputStream(file)
                )
        ) {

            long remaining = sizeBytes;

            while (remaining > 0) {

                int amount = (int) Math.min(
                    block.length, remaining
                );

                out.write(block, 0, amount);

                remaining -= amount;
            }
        }
    }

    // Count lines using unbuffered reading
    public static long countUnbuffered(Path file)
            throws IOException {

        long lines = 0;

        boolean hasData = false;

        int lastByte = -1;

        try (
            FileInputStream input =
                new FileInputStream(file.toFile())
        ) {

            int data;

            while ((data = input.read()) != -1) {

                hasData = true;

                lastByte = data;

                if (data == '\n') {

                    lines++;
                }
            }
        }

        if (hasData && lastByte != '\n') {

            lines++;
        }

        return lines;
    }

    // Count lines using buffered block reading
    public static long countBuffered(Path file)
            throws IOException {

        long lines = 0;

        boolean hasData = false;

        int lastByte = -1;

        byte[] buffer = new byte[BUFFER_SIZE];

        try (
            BufferedInputStream input =
                new BufferedInputStream(
                    new FileInputStream(file.toFile()),
                    BUFFER_SIZE
                )
        ) {

            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {

                for (int i = 0; i < bytesRead; i++) {

                    int data = buffer[i] & 0xFF;

                    hasData = true;

                    lastByte = data;

                    if (data == '\n') {

                        lines++;
                    }
                }
            }
        }

        if (hasData && lastByte != '\n') {

            lines++;
        }

        return lines;
    }

    // Calculate median execution time
    private static double median(long[] times) {

        Arrays.sort(times);

        return times[times.length / 2] / 1_000_000.0;
    }

    public static void main(String[] args)
            throws IOException {

        long[] sizes = {
            1_000_000L,
            10_000_000L,
            50_000_000L
        };

        for (long size : sizes) {

            Path file = Path.of(
                "attendance-" + (size / 1_000_000)
                + "MB.log"
            );

            generateFile(file, size);

            // Check that both methods return the same count
            long expected = countBuffered(file);

            long unbufferedCount =
                countUnbuffered(file);

            if (expected != unbufferedCount) {

                throw new IllegalStateException(
                    "Line counts do not match."
                );
            }

            // Warm-up run
            countBuffered(file);

            countUnbuffered(file);

            long[] unbufferedTimes =
                new long[REPETITIONS];

            long[] bufferedTimes =
                new long[REPETITIONS];

            // Measure unbuffered version
            for (int i = 0; i < REPETITIONS; i++) {

                long start = System.nanoTime();

                countUnbuffered(file);

                unbufferedTimes[i] =
                    System.nanoTime() - start;
            }

            // Measure buffered version
            for (int i = 0; i < REPETITIONS; i++) {

                long start = System.nanoTime();

                countBuffered(file);

                bufferedTimes[i] =
                    System.nanoTime() - start;
            }

            System.out.println(
                "\nFile Size: " + (size / 1_000_000) + " MB"
            );

            System.out.println(
                "Total Lines: " + expected
            );

            System.out.printf(
                "Unbuffered Median: %.3f ms%n",
                median(unbufferedTimes)
            );

            System.out.printf(
                "Buffered Median: %.3f ms%n",
                median(bufferedTimes)
            );

            System.out.println(
                "------------------------------"
            );
        }
    }
}
