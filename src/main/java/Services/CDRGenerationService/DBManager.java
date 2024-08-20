package Services.CDRGenerationService;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DBManager {
    /** Процедура очистки базы данных.
     * @see Connection
     * @see ConnectionUtil
     * */
    public void resetCDRTable() {
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

    }

    /**
     * Процедура получения одного случайного номера телефона из локальной базы данных.
     */
    public String getMSISDN() throws SQLException {
        Connection connection = ConnectionUtil.getConnection();
        Random rand = new Random();
        int ranNum = rand.nextInt(1, 26);
        String query = String.format("SELECT NUMBER FROM MSISDN WHERE id=%2d;", ranNum);
        ResultSet result = connection.createStatement().executeQuery(query);
        if (result.next()) {
            return result.getString(1);
        }
        else connection.close();
        return "";
    }

    /** Процедура заполнения таблицы в базе данных.
     * Таблица содержит все те же записи, что и файлы.
     * @see Connection
     * */
    public void createDBofCDRs() {
        Connection connection = ConnectionUtil.getConnection();
        try (Statement stmt = connection.createStatement()){
            AtomicInteger currentRow = new AtomicInteger(0);
            for (int i = 1; i <= 12; i++) {
                insertFile(i, stmt, currentRow);
            }
            try {
                int[] result = stmt.executeBatch();
                connection.commit();
            } catch (BatchUpdateException e) {
                connection.rollback();
                System.out.println(e.getMessage());
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Процедура обработки CDR файла для добавления его записей в таблицу.
     *  Файл обрабатывается построчно процедурой insertFragment.
     * @param numOfFile номер файла для обработки
     * @param stmt sql оператор
     * @param currentRow номер строки, на которой остановилась запись
     * @see FileManager#getPath(int)
     * @see Statement
     *  */
    private void insertFile(int numOfFile, Statement stmt, AtomicInteger currentRow) {
        String path = FileManager.getPath(numOfFile);
        FileReader fr = null;

        try {
            fr = new FileReader(path);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try (BufferedReader br = new BufferedReader(fr)) {
            for(String line; (line = br.readLine()) != null; ) {
                currentRow.addAndGet(1);
                insertFragment(line, stmt, currentRow);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /** Процедура обработки строки CDR файла.
     *  На основе данных из строки формируется SQL запрос, который добавляется в пакет для обработки.
     *  @param line обрабатываемая строка из файла
     *  @param stmt sql оператор
     *  @param currentRow номер строки, на которой остановилась запись
     *  @see Connection
     *  @see Statement
     *  */
    private void insertFragment(@NotNull String line, @NotNull Statement stmt, @NotNull AtomicInteger currentRow) {
        String[] data = line.replace("\n", "").split(",");
        String query = "INSERT INTO CDR (ID, TYPE, NUMBER, TIMEOFSTART, TIMEOFEND) VALUES\n" +
                "(" +
                currentRow.get() + "," +
                data[0] + "," +
                data[1] + "," +
                data[2] + "," +
                data[3] +
                ");";
        addToBatch(stmt, query);
    }

    /** Процедура добавления запроса в пакет для обрадотки.
     * @param stmt sql оператор
     * @param query sql запрос
     * @see Statement
     * */
    private void addToBatch(@NotNull Statement stmt, @NotNull String query) {
        try {
            stmt.addBatch(query);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
