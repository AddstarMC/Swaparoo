package au.com.addstar.swaparoo;

import java.sql.SQLException;

public class StarGemsDB extends DatabaseManager {
    public StarGemsDB(SwaparooPlugin plugin) {
        super(plugin);
    }

    public void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player_uuid VARCHAR(36)," +
                "action VARCHAR(10)," +
                "stargems INT DEFAULT 0," +
                "stardust INT DEFAULT 0," +
                "package_id VARCHAR(64)," +
                "package_name VARCHAR(64)," +
                "transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_player_uuid (player_uuid)" +
                ")";

        this.executeUpdate(sql);
    }
}
