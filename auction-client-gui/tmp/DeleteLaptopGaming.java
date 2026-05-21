import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DeleteLaptopGaming {
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String selectSql = """
                    SELECT id, name, current_price, status
                    FROM products
                    WHERE name = ? AND status <> 'DELETED'
                    ORDER BY id
                    """;

            try (PreparedStatement select = conn.prepareStatement(selectSql)) {
                select.setString(1, "Laptop Gaming");

                try (ResultSet rs = select.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf(
                                "Found id=%d name=%s current_price=%.0f status=%s%n",
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getDouble("current_price"),
                                rs.getString("status")
                        );
                    }
                }
            }

            String updateSql = "UPDATE products SET status = 'DELETED' WHERE name = ? AND status <> 'DELETED'";
            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setString(1, "Laptop Gaming");
                int rows = update.executeUpdate();
                System.out.println("Updated rows=" + rows);
            }
        }
    }
}
