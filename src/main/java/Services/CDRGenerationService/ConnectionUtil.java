package Services.CDRGenerationService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Сервис, устанавливающий соединение с локальной базой данных
 *  @author Никита Дюков
 *  @version 1.0
 *  */
public class ConnectionUtil {
    public static final String DB_URL = "jdbc:h2:~/nexignTask/db/db;DB_CLOSE_DELAY=-1;";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "sa";
    public static final String DB_Driver = "org.h2.Driver";
    public static Connection getConnection() {
        try {
            Class.forName(DB_Driver);
            return DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
