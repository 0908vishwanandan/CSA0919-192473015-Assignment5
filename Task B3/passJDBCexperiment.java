import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PassJdbcExperiment {

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

    // Generate test bus-pass records
    public static List<String[]> generatePasses(int count) {

        List<String[]> passes = new ArrayList<>();

        for (int i = 1; i <= count; i++) {

            String passId =
                String.format("P%06d", i);

            String regNo =
                String.format("REG%07d", i);

            String name = "Student" + i;

            String routeNo =
                String.valueOf((i % 20) + 1);

            String amount =
                String.format(
                    Locale.US,
                    "%.2f",
                    3000.0 + (i % 1000)
                );

            passes.add(new String[] {
                passId,
                regNo,
                name,
                routeNo,
                amount
            });
        }

        return passes;
    }

    // Delete existing records before each experiment
    public static void clearTable(Connection con)
            throws SQLException {

        try (Statement st = con.createStatement()) {

            st.executeUpdate("DELETE FROM bus_pass");
        }
    }

    // Insert using Statement
    public static void insertUsingStatement(
            Connection con,
            List<String[]> passes
    ) throws SQLException {

        String sql;

        try (Statement st = con.createStatement()) {

            for (String[] p : passes) {

                sql =
                    "INSERT INTO bus_pass " +
                    "(pass_id, reg_no, name, route_no, amount) " +
                    "VALUES ('" + p[0] + "', '" +
                    p[1] + "', '" +
                    p[2] + "', " +
                    p[3] + ", " +
                    p[4] + ")";

                st.executeUpdate(sql);
            }
        }
    }

    // Insert using PreparedStatement
    public static void insertUsingPrepared(
            Connection con,
            List<String[]> passes
    ) throws SQLException {

        String sql =
            "INSERT INTO bus_pass " +
            "(pass_id, reg_no, name, route_no, amount) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            for (String[] p : passes) {

                setParameters(ps, p);

                ps.executeUpdate();
            }
        }
    }

    // Set the five insertion parameters
    private static void setParameters(
            PreparedStatement ps,
            String[] p
    ) throws SQLException {

        ps.setString(1, p[0]);

        ps.setString(2, p[1]);

        ps.setString(3, p[2]);

        ps.setInt(4, Integer.parseInt(p[3].trim()));

        double amount =
            Double.parseDouble(p[4].trim());

        if (!Double.isFinite(amount) || amount < 0) {

            throw new IllegalArgumentException(
                "Invalid amount: " + p[4]
            );
        }

        ps.setDouble(5, amount);
    }

    // Insert using PreparedStatement batch with auto-commit ON
    public static void insertUsingBatchAutoCommit(
            Connection con,
            List<String[]> passes
    ) throws SQLException {

        con.setAutoCommit(true);

        String sql =
            "INSERT INTO bus_pass " +
            "(pass_id, reg_no, name, route_no, amount) " +
            "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            int pending = 0;

            for (String[] p : passes) {

                setParameters(ps, p);

                ps.addBatch();

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
    }

    // Count records in the table
    public static int countRows(Connection con)
            throws SQLException {

        String sql = "SELECT COUNT(*) FROM bus_pass";

        try (
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql)
        ) {

            rs.next();

            return rs.getInt(1);
        }
    }

    // Verify records and display column metadata
    public static void verifyTable(
            Connection con,
            String approach
    ) throws SQLException {

        String sql =
            "SELECT pass_id, reg_no, name, route_no, amount " +
            "FROM bus_pass ORDER BY pass_id";

        System.out.println(
            "\nVerification using " + approach
        );

        try (
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql)
        ) {

            ResultSetMetaData md =
                rs.getMetaData();

            System.out.println("\nColumn Information:");

            for (int i = 1; i <= md.getColumnCount(); i++) {

                System.out.println(
                    md.getColumnLabel(i) +
                    " : " +
                    md.getColumnTypeName(i)
                );
            }

            System.out.println("\nStored Records:");

            int count = 0;

            while (rs.next()) {

                System.out.println(
                    rs.getString("pass_id") + " | " +
                    rs.getString("reg_no") + " | " +
                    rs.getString("name") + " | " +
                    rs.getInt("route_no") + " | " +
                    rs.getBigDecimal("amount")
                );

                count++;
            }

            System.out.println(
                "\nTotal records: " + count
            );
        }
    }

    // Measure execution time of an insertion operation
    public static void printTime(
            Connection con,
            List<String[]> passes,
            String approach
    ) throws SQLException {

        clearTable(con);

        long start = System.nanoTime();

        if (approach.equals("Statement")) {

            insertUsingStatement(con, passes);

        } else if (approach.equals("PreparedStatement")) {

            insertUsingPrepared(con, passes);

        } else {

            insertUsingBatchAutoCommit(con, passes);
        }

        long end = System.nanoTime();

        double timeMs =
            (end - start) / 1_000_000.0;

        System.out.printf(
            Locale.US,
            "%-22s | %,9d records | %.3f ms%n",
            approach,
            passes.size(),
            timeMs
        );

        System.out.println(
            "Verified row count: " + countRows(con)
        );
    }

    // Test all three approaches using 10 records
    public static void testSmallData(Connection con)
            throws SQLException {

        List<String[]> passes =
            generatePasses(10);

        clearTable(con);

        insertUsingStatement(con, passes);

        verifyTable(con, "Statement");

        clearTable(con);

        insertUsingPrepared(con, passes);

        verifyTable(con, "PreparedStatement");

        clearTable(con);

        insertUsingBatchAutoCommit(con, passes);

        verifyTable(con, "PreparedStatement Batch");
    }

    // Compare insertion time for three data sizes
    public static void benchmark(Connection con)
            throws SQLException {

        int[] sizes = {1000, 10000, 100000};

        String[] approaches = {
            "Statement",
            "PreparedStatement",
            "Batch"
        };

        System.out.println("\nPERFORMANCE COMPARISON");

        for (int size : sizes) {

            List<String[]> passes =
                generatePasses(size);

            System.out.println(
                "\nDataset Size: " + size
            );

            for (String approach : approaches) {

                printTime(con, passes, approach);
            }
        }
    }

    // Count rows for transaction failure testing
    public static void testTransactionFailure(Connection con)
            throws SQLException {

        List<String[]> passes =
            generatePasses(2000);

        // Place an invalid amount at record number 1501
        passes.set(
            1500,
            new String[] {
                "PINVALID",
                "REGINVALID",
                "InvalidStudent",
                "5",
                "INVALID_AMOUNT"
            }
        );

        // Test 1: Auto-commit ON
        clearTable(con);

        try {

            insertUsingBatchAutoCommit(con, passes);

        } catch (NumberFormatException e) {

            System.out.println(
                "\nAuto-commit test: Invalid amount detected."
            );
        }

        System.out.println(
            "Rows remaining with auto-commit ON: " +
            countRows(con)
        );

        // Test 2: One transaction
        clearTable(con);

        try {

            PassLoader.loadAll(con, passes);

        } catch (NumberFormatException e) {

            System.out.println(
                "\nTransaction test: Invalid amount detected."
            );
        }

        System.out.println(
            "Rows remaining after rollback: " +
            countRows(con)
        );
    }

    public static void main(String[] args) {

        try (Connection con = getConnection()) {

            con.setAutoCommit(true);

            System.out.println(
                "B3.1 - SMALL DATA VERIFICATION"
            );

            testSmallData(con);

            System.out.println(
                "\nB3.2 - PERFORMANCE BENCHMARK"
            );

            benchmark(con);

            System.out.println(
                "\nB3.3 - AUTO-COMMIT VS TRANSACTION"
            );

            testTransactionFailure(con);

        } catch (SQLException e) {

            System.out.println(
                "Database error: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
