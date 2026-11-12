import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseInspector {

    private static final String URL =
        "jdbc:mysql://localhost:3306/assignment_persistence";

    private static final String USER = "root";

    private static final String PASSWORD = "";

    public static void main(String[] args) {

        try (
            Connection con =
                DriverManager.getConnection(
                    URL, USER, PASSWORD
                )
        ) {

            DatabaseMetaData md =
                con.getMetaData();

            // Database product information
            System.out.println(
                "Database Product: " +
                md.getDatabaseProductName()
            );

            System.out.println(
                "Database Version: " +
                md.getDatabaseProductVersion()
            );

            // JDBC driver information
            System.out.println(
                "JDBC Driver: " +
                md.getDriverName()
            );

            System.out.println(
                "Driver Version: " +
                md.getDriverVersion()
            );

            // List tables
            System.out.println("\nAvailable Tables:");

            try (
                ResultSet rs =
                    md.getTables(
                        con.getCatalog(),
                        null,
                        "%",
                        new String[] {"TABLE"}
                    )
            ) {

                while (rs.next()) {

                    System.out.println(
                        rs.getString("TABLE_NAME")
                    );
                }
            }

        } catch (SQLException e) {

            System.out.println(
                "Database inspection error: " +
                e.getMessage()
            );
        }
    }
}
