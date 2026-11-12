import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SecureLogin {

    private static final String URL =
        "jdbc:mysql://localhost:3306/hostel_wallet";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    public static boolean login(String id, String pwd)
            throws SQLException {

        String sql =
            "SELECT * FROM users WHERE id = ? AND pwd = ?";

        try (
            Connection con =
                DriverManager.getConnection(
                    URL, USER, PASSWORD
                );

            PreparedStatement ps =
                con.prepareStatement(sql)
        ) {

            // Set user input as parameter values
            ps.setString(1, id);

            ps.setString(2, pwd);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    public static void main(String[] args) {

        String id = "' OR '1'='1' -- ";

        String pwd = "wrongpassword";

        try {

            boolean result = login(id, pwd);

            if (result) {

                System.out.println("Login successful.");

            } else {

                System.out.println(
                    "Invalid username or password."
                );
            }

        } catch (SQLException e) {

            System.out.println(
                "Database error: " + e.getMessage()
            );
        }
    }
}
