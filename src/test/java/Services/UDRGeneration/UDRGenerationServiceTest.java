package Services.UDRGeneration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

class UDRGenerationServiceTest {
    public void clearResources() {
    File folderReports = new File("src" + File.separator + "main" + File.separator
            + "resources" + File.separator + "reports" + File.separator);
    File[] files = folderReports.listFiles();
        if(files != null) {
            for(File f: files) {
                f.delete();
            }
        }
    }

    @Test
    void testGenerateReportTotal() {
        clearResources();
        UDRGenerationService service = new UDRGenerationService();
        service.generateReport();
        File folderReports = new File("src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "reports" + File.separator);

        File[] files = folderReports.listFiles();
        boolean hasFiles = files.length > 0;
        boolean hasContent = true;

        if(files != null) {
            for(File f: files) {
                hasContent = (hasContent && f.length() > 0);
            }
        }

        assertTrue(hasContent && hasFiles);
    }

    @Test
    void testGenerateReportPerMonth() {
        clearResources();
        UDRGenerationService service = new UDRGenerationService();
        service.generateReport("11234567891");
        File folderReports = new File("src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "reports" + File.separator);

        File[] files = folderReports.listFiles();
        boolean hasFiles = files.length > 0;
        boolean hasContent = true;

        if(files != null) {
            for(File f: files) {
                hasContent = (hasContent && f.length() > 0);
            }
        }

        assertTrue(hasContent && hasFiles);
    }

    @Test
    void testGenerateReportMonth() {
        clearResources();
        UDRGenerationService service = new UDRGenerationService();
        service.generateReport("11234567891", 5);
        File folderReports = new File("src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "reports" + File.separator);

        File[] files = folderReports.listFiles();
        boolean hasFiles = files.length == 1;
        boolean hasContent = true;

        if(files != null) {
            for(File f: files) {
                hasContent = (hasContent && f.length() > 0);
            }
        }

        assertTrue(hasContent && hasFiles);

    }
}