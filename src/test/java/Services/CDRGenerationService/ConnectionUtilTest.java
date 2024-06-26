package Services.CDRGenerationService;

import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionUtilTest {

    @Test
    void testGetConnection() {
        Connection connection = ConnectionUtil.getConnection();
        System.out.println(connection.toString());
        assertNotNull(connection);
    }
}