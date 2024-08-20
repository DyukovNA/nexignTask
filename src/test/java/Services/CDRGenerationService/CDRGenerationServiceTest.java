package Services.CDRGenerationService;


import org.h2.command.ddl.TruncateTable;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CDRGenerationServiceTest {
    CDRGenerationService cdrGenerationService = new CDRGenerationService();

    @Test
    void generateSetOfCDRs() throws IOException {
        cdrGenerationService.generateSetOfCDRs();
        boolean flag = true;
        for (int i = 1; i <= 12; i++) {
            File file = new File(FileManager.getPath(i));
            if (file.exists() && file.isFile()) {
                Reader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.matches("0[1-2],\\+7[0-9]{10}(,1[0-9]{9}){2}")) {
                        flag = false;
                    }
                }
            }
        }
        assertTrue(flag);
    }
}