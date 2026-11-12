import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PassLoader {

    /**
     * Loads all passes in one transaction,
     * using batches of 1,000 rows.
     *
     * Returns the number of rows loaded.
     */
    public static int loadAll(
            Connection con,
            List<String[]> passes
    ) throws SQLException {

        String sql =
            "INSERT INTO bus_pass " +
            "(pass_id, reg_no, name, route_no, amount) " +
            "VALUES (?, ?, ?, ?, ?)";

        boolean oldAutoCommit = con.getAutoCommit();

        con.setAutoCommit(false);

        int n = 0;

        try {

            try (PreparedStatement ps =
                    con.prepareStatement(sql)) {

                int pending = 0;

                for (String[] p : passes) {

                    // Validate the number of fields
                    if (p == null || p.length != 5) {

                        throw new IllegalArgumentException(
                            "Invalid bus-pass record."
                        );
                    }

                    // TODO 1: Set five parameters and execute
                    // the batch after every 1,000 rows.

                    ps.setString(1, p[0]);

                    ps.setString(2, p[1]);

                    ps.setString(3, p[2]);

                    ps.setInt(
                        4,
                        Integer.parseInt(p[3].trim())
                    );

                    double amount =
                        Double.parseDouble(p[4].trim());

                    if (!Double.isFinite(amount) || amount < 0) {

                        throw new IllegalArgumentException(
                            "Invalid amount: " + p[4]
                        );
                    }

                    ps.setDouble(5, amount);

                    ps.addBatch();

                    n++;

                    pending++;

                    if (pending == 1000) {

                        ps.executeBatch();

                        ps.clearBatch();

                        pending = 0;
                    }
                }

                // Execute the remaining records
                if (pending > 0) {

                    ps.executeBatch();

                    ps.clearBatch();
                }
            }

            // TODO 2: Make all inserted rows permanent
            con.commit();

            return n;

        } catch (SQLException | RuntimeException e) {

            // TODO 3: Undo every insert made in this transaction
            try {

                con.rollback();

            } catch (SQLException rollbackError) {

                e.addSuppressed(rollbackError);
            }

            throw e;

        } finally {

            // Restore the original auto-commit setting
            con.setAutoCommit(oldAutoCommit);
        }
    }
}
