package db;

import org.example.database.DatabaseManager;
public class TestDB{
    public static void main(String[] args) throws Exception {
        DatabaseManager.getConnection();
        System.out.println("DB connected");
    }

}
