package Services.UDRGeneration;

import org.jetbrains.annotations.NotNull;
import java.io.*;

/** Класс, преднозначенный для работы с файловой системой для UDRGenerationService
 * @author Никита Дюков
 * @version 1.0
 * */
class FileManager {
    /** Функция получения пути до CDR-файла с заданным порядковым номером.
     * @param numOfFile порядковый номер
     * @return Путь до CDR-файла
     * */
    public static @NotNull String getPathCDR(@NotNull Integer numOfFile){
        String filename = String.format("%2d_CDR.txt", numOfFile);
        return "src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "CDRs" + File.separator + filename;
    }
    /** Функция получения пути до UDR-файла формата JSON с заданным номер мобильного абонента и номером месяца.
     * @param msisdn номер мобильного абонента
     * @param month номер месяца
     * @return Путь до UDR-файла
     * */
    public static @NotNull String getPathUDR(String msisdn, @NotNull Integer month) throws IllegalArgumentException {
        if (month == -1) {
            String filename = String.format("%s.json", msisdn);
            return "src" + File.separator + "main" + File.separator
                    + "resources" + File.separator + "reports" + File.separator + filename;
        } else if (month > 0 && month <= 12) {
            String filename = String.format("%s_%d.json", msisdn, month);
            return "src" + File.separator + "main" + File.separator
                    + "resources" + File.separator + "reports" + File.separator + filename;
        } else throw new IllegalArgumentException("Invalid number of month");
    }
    /** Процедура создания пустого файла. Файл имеет название формата "номер_месяц.json"
     * @param msisdn номер мобильного абонента
     * @param month номер месяца
     * */
    public static void createFile(String msisdn, @NotNull Integer month) throws RuntimeException {
        try {
            File file = new File(FileManager.getPathUDR(msisdn, month));
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
