package Services.CDRGenerationService;


import org.junit.jupiter.api.Test;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CDRGenerationServiceTest {
    public void clearResources() {
        String query_delete = "DROP TABLE IF EXISTS CDR;";
        String query_create = "CREATE TABLE CDR(ID INT PRIMARY KEY, TYPE VARCHAR(2), " +
                "NUMBER VARCHAR(15), TIMEOFSTART VARCHAR(255), TIMEOFEND VARCHAR(255))";
        Connection connection = ConnectionUtil.getConnection();
        try {
            connection.createStatement().execute(query_delete);
            connection.createStatement().execute(query_create);
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        File folderCDR = new File("src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "CDRs" + File.separator);
        File[] files = folderCDR.listFiles();
        if(files != null) {
            for(File f: files) {
                f.delete();
            }
        }
    }

    @Test
    void testGenerate() {
        clearResources();
        CDRGenerationService service = new CDRGenerationService();
        service.generate();
        File folderCDR = new File("src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "CDRs" + File.separator);
        File[] files = folderCDR.listFiles();
        boolean hasFiles = files.length == 12;
        Connection connection = ConnectionUtil.getConnection();
        String query = "SELECT number FROM CDR;";
        ResultSet result = null;
        List<String> msisdns = new ArrayList<>();
        try {
            result = connection.createStatement().executeQuery(query);
            if (result.next()) {
                msisdns.add(result.getString(1));
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        boolean hasRecords = !msisdns.isEmpty();
        assertTrue(hasRecords && hasFiles);
    }

    @Test
    void testResetCDRTable() {
        CDRGenerationService service = new CDRGenerationService();
        service.generate();
        service.resetCDRTable();
        Connection connection = ConnectionUtil.getConnection();
        String query = "SELECT number FROM CDR;";
        ResultSet result = null;
        List<String> msisdns = new ArrayList<>();
        try {
            result = connection.createStatement().executeQuery(query);
            if (result.next()) {
                msisdns.add(result.getString(1));
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        boolean hasNoRecords = msisdns.isEmpty();
        assertTrue(hasNoRecords);
    }

}