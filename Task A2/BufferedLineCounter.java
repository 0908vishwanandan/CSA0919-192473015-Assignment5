import java.io.FileInputStream;
import java.io.IOException;

public class BufferedLineCounter {

    public static long countLines(String fileName)
            throws IOException {

        // Create a 64 KB buffer
        byte[] buffer = new byte[64 * 1024];

        long lineCount = 0;

        boolean hasData = false;

        int lastByte = -1;

        try (FileInputStream input =
                 new FileInputStream(fileName)) {

            int bytesRead;

            // Read multiple bytes at a time
            while ((bytesRead = input.read(buffer)) != -1) {

                for (int i = 0; i < bytesRead; i++) {

                    hasData = true;

                    int currentByte = buffer[i] & 0xFF;

                    // Count newline characters
                    if (currentByte == '\n') {

                        lineCount++;
                    }

                    lastByte = currentByte;
                }
            }
        }

        // Count the final line if it has no newline
        if (hasData && lastByte != '\n') {

            lineCount++;
        }

        return lineCount;
    }

    public static void main(String[] args) {

        String fileName = "attendance.log";

        try {

            long totalLines = countLines(fileName);

            System.out.println(
                "Total number of lines: " + totalLines
            );

        } catch (IOException e) {

            System.out.println(
                "File reading error: " + e.getMessage()
            );
        }
    }
}
