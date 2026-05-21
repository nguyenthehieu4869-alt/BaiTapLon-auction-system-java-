import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VerifyLaptopGamingDeleted {
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("""
                     SELECT id, name, status
                     FROM products
                     WHERE name = ?
                     ORDER BY id
                     """)) {
            stmt.setString(1, "Laptop Gaming");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf(
                            "id=%d name=%s status=%s%n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("status")
                    );
                }
            }
        }
    }
}
