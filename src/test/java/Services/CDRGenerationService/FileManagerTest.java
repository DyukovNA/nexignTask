package Services.CDRGenerationService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest {
    @Test
    void getPath() {
        assertEquals(
                FileManager.getPath(0), "." + File.separator + "CDRs" + File.separator + "0_CDR.txt"
        );
    }

    @Test
    void createFile() {
        FileManager.createFile(0);
        File file = new File(FileManager.getPath(0));
        assertTrue(file.exists() && file.isFile());
    }
}