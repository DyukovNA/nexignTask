package Services.CDRGenerationService;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DBManagerTest {
    DBManager manager = new DBManager();

    @Test
    void resetCDRTable() throws SQLException {
        String query_delete = "DROP TABLE IF EXISTS CDR;";
        String query_create = "CREATE TABLE CDR(ID INT PRIMARY KEY, TYPE VARCHAR(2), " +
                "NUMBER VARCHAR(15), TIMEOFSTART VARCHAR(255), TIMEOFEND VARCHAR(255))";
        String query_fill = "INSERT INTO CDR (ID, TYPE, NUMBER, TIMEOFSTART, TIMEOFEND) VALUES\n" +
                "(" + 1 + ", 01, +79067777777, 1514764800, 1514784800);";
        Connection connection = ConnectionUtil.getConnection();
        connection.createStatement().execute(query_delete);
        connection.createStatement().execute(query_create);
        connection.createStatement().execute(query_fill);
        connection.close();

        manager.resetCDRTable();
        String query = "SELECT * FROM CDR WHERE id=1;";
        ResultSet result = connection.createStatement().executeQuery(query);
        assertFalse(result.next());
    }

    @Test
    void getMSISDN() throws SQLException {
        assertTrue(manager.getMSISDN().matches("\\+7[0-9]{10}"));
    }

    @Test
    void createDBofCDRs() throws SQLException, IOException {
        CDRGenerationService service = new CDRGenerationService();
        manager.resetCDRTable();
        service.generateSetOfCDRs();
        manager.createDBofCDRs();
        Connection connection = ConnectionUtil.getConnection();
        String query = "SELECT * FROM CDR";
        ResultSet result = connection.createStatement().executeQuery(query);
        int cntDB = 0;
        while (result.next()) {
            cntDB++;
        }
        connection.close();
        int cntFiles = 0;
        for (int i = 1; i <= 12; i++) {
            File file = new File(FileManager.getPath(i));
            if (file.exists() && file.isFile()) {
                Reader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                   cntFiles++;
                }
            }
        }
        assertEquals(cntFiles, cntDB);
    }

}