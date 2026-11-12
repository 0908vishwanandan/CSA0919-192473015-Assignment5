import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class PassMigration {

    private static final String URL =
        "jdbc:mysql://localhost:3306/assignment_persistence";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    private static final int BATCH_SIZE = 1000;

    // Establish database connection
    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
            URL, USER, PASSWORD
        );
    }

    // Validate a single bus-pass record
    public static String validate(String[] p) {

        if (p.length != 5) {

            return "Incorrect number of fields";
        }

        if (p[0].isBlank()) {

            return "Empty pass ID";
        }

        if (p[1].isBlank()) {

            return "Empty registration number";
        }

        if (p[2].isBlank()) {

            return "Empty student name";
        }

        if (p[0].length() > 20 ||
            p[1].length() > 20 ||
            p[2].length() > 100) {

            return "Field length exceeded";
        }

        try {

            int route = Integer.parseInt(p[3].trim());

            if (route <= 0) {

                return "Route number must be positive";
            }

        } catch (NumberFormatException e) {

            return "Invalid route number";
        }

        try {

            double amount =
                Double.parseDouble(p[4].trim());

            if (!Double.isFinite(amount) || amount < 0) {

                return "Invalid amount";
            }

        } catch (NumberFormatException e) {

            return "Non-numeric amount";
        }

        return null;
    }

    // Insert valid records using batches and one transaction
    public static int insertValidRecords(
            Connection con,
            List<String[]> records
    ) throws SQLException {

        String sql =
            "INSERT INTO bus_pass " +
            "(pass_id, reg_no, name, route_no, amount) " +
            "VALUES (?, ?, ?, ?, ?)";

        boolean oldAutoCommit = con.getAutoCommit();

        con.setAutoCommit(false);

        int inserted = 0;

        try {

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                int pending = 0;

                for (String[] p : records) {

                    ps.setString(1, p[0]);

                    ps.setString(2, p[1]);

                    ps.setString(3, p[2]);

                    ps.setInt(
                        4,
                        Integer.parseInt(p[3].trim())
                    );

                    ps.setDouble(
                        5,
                        Double.parseDouble(p[4].trim())
                    );

                    ps.addBatch();

                    inserted++;

                    pending++;

                    if (pending == BATCH_SIZE) {

                        ps.executeBatch();

                        ps.clearBatch();

                        pending = 0;
                    }
                }

                if (pending > 0) {

                    ps.executeBatch();

                    ps.clearBatch();
                }
            }

            con.commit();

            return inserted;

        } catch (SQLException | RuntimeException e) {

            try {

                con.rollback();

            } catch (SQLException rollbackError) {

                e.addSuppressed(rollbackError);
            }

            throw e;

        } finally {

            con.setAutoCommit(oldAutoCommit);
        }
    }

    // Read the file, validate records and generate failure report
    public static void migrate(
            Connection con,
            Path inputFile,
            Path failureFile
    ) throws IOException, SQLException {

        List<String[]> validRecords =
            new ArrayList<>();

        List<String> failures =
            new ArrayList<>();

        int lineNumber = 0;

        try (BufferedReader reader =
                Files.newBufferedReader(inputFile)) {

            String line;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                if (line.isBlank()) {

                    failures.add(
                        "Line " + lineNumber +
                        ": Empty record"
                    );

                    continue;
                }

                // Simple comma-separated input format
                String[] fields = line.split(",", -1);

                for (int i = 0; i < fields.length; i++) {

                    fields[i] = fields[i].trim();
                }

                String error = validate(fields);

                if (error != null) {

                    failures.add(
                        "Line " + lineNumber +
                        ": " + error +
                        " | " + line
                    );

                } else {

                    validRecords.add(fields);
                }
            }
        }

        // Insert valid records
        int inserted = 0;

        try {

            inserted =
                insertValidRecords(con, validRecords);

        } catch (SQLException | RuntimeException e) {

            // Database insertion failed.
            // The transaction has been rolled back.
            Files.write(
                failureFile,
                List.of(
                    "DATABASE MIGRATION FAILED: " +
                    e.getMessage()
                ),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );

            throw e;
        }

        // Write the validation failure report
        try (BufferedWriter writer =
                Files.newBufferedWriter(
                    failureFile,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )) {

            writer.write("BUS-PASS MIGRATION FAILURE REPORT");

            writer.newLine();

            writer.write("--------------------------------");

            writer.newLine();

            if (failures.isEmpty()) {

                writer.write("No invalid records found.");

                writer.newLine();

            } else {

                for (String failure : failures) {

                    writer.write(failure);

                    writer.newLine();
                }
            }
        }

        System.out.println("Migration completed.");

        System.out.println(
            "Total lines processed: " + lineNumber
        );

        System.out.println(
            "Valid records inserted: " + inserted
        );

        System.out.println(
            "Invalid records: " + failures.size()
        );

        System.out.println(
            "Failure report: " + failureFile
        );
    }

    public static void main(String[] args) {

        Path inputFile =
            Path.of("existing-passes.csv");

        Path failureFile =
            Path.of("migration-failures.txt");

        try (Connection con = getConnection()) {

            migrate(con, inputFile, failureFile);

        } catch (IOException | SQLException e) {

            System.out.println(
                "Migration error: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
