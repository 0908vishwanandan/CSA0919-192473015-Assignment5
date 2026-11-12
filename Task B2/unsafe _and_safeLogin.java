import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UnsafeAndSafeLogin {

    private static final String URL =
        "jdbc:mysql://localhost:3306/assignment_security";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    // Establish database connection
    private static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
            URL, USER, PASSWORD
        );
    }

    // Unsafe login using Statement
    public static boolean unsafeLogin(
            String id, String pwd) throws SQLException {

        String sql =
            "SELECT * FROM users WHERE id='"
            + id
            + "' AND pwd='"
            + pwd
            + "'";

        System.out.println("\nUnsafe SQL:");

        System.out.println(sql);

        try (
            Connection con = getConnection();

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(sql)
        ) {

            return rs.next();
        }
    }

    // Secure login using PreparedStatement
    public static boolean safeLogin(
            String id, String pwd) throws SQLException {

        String sql =
            "SELECT * FROM users WHERE id = ? AND pwd = ?";

        try (
            Connection con = getConnection();

            PreparedStatement ps =
                con.prepareStatement(sql)
        ) {

            ps.setString(1, id);

            ps.setString(2, pwd);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    public static void main(String[] args) {

        // Deliberately constructed attack input
        String attackId = "' OR '1'='1' -- ";

        String wrongPassword = "wrongpassword";

        try {

            // Test unsafe login
            boolean unsafeResult =
                unsafeLogin(attackId, wrongPassword);

            System.out.println(
                "\nUnsafe login result: "
                + (unsafeResult ? "ACCEPTED" : "REJECTED")
            );

            // Test secure login
            boolean safeResult =
                safeLogin(attackId, wrongPassword);

            System.out.println(
                "PreparedStatement result: "
                + (safeResult ? "ACCEPTED" : "REJECTED")
            );

        } catch (SQLException e) {

            System.out.println(
                "Database error: " + e.getMessage()
            );
        }
    }
}
