package Services.CDRGenerationService;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Сервис, генерирующий Call Data Record (CDR) файлы. Все записи из файлов дублируютя в таблицу локальной базы данных.
 * Один файл - месяц записей. Всего генерируется 12 файлов, т.е. год записей.
 * @author Никита Дюков
 * @version 1.0
 */
public class CDRGenerationService {
    /** Поле с длиной месяца в секундах */
    private static final long monthInSeconds = 30*24*60*60;
    /** После с максимальным количеством записей, которое будет генерироваться в одном файле */
    private static final int BATCH_SIZE = 150;
    /** Поле с нижней границей начала тарификации, равно Mon Jan 01 2018 00:00:00 GMT+0000 */
    private static final int MIN_TIME_OF_GENERATION = 1514764800;
    /** Поле с верхней границей начала тарификации, равно Wed Mar 22 2023 00:00:00 GMT+0000 */
    private static final int MAX_TIME_OF_GENERATION = 1679443200;
    /** Поле с минимальной длительностью возможного звонка в секундах */
    private static final int MIN_TIME_OF_CALL = 30;
    /** Поле с максимальной длительностью возможного звонка в секундах */
    private static final int MAX_TIME_OF_CALL = 3600;
    /** Процедура генерации CDR файла и заполнения таблицы в базе данных.
     * Таблица очищается перед заполнением, чтобы избежать повторения уникальных идентификаторов.
     * @see CDRGenerationService#resetCDRTable()
     * @see CDRGenerationService#generateSetOfCDRs()
     * @see CDRGenerationService#createDBofCDRs()
     * */
    public void generate() {
        resetCDRTable();
        generateSetOfCDRs();
        createDBofCDRs();
    }
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
    /** Процедура заполнения таблицы в базе данных.
     * Таблица содержит все те же записи, что и файлы.
     * @see CDRGenerationService#insertFile(int, Statement, AtomicInteger)
     * @see Connection
     * */
    private void createDBofCDRs() {
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
     * @see CDRGenerationService#insertFragment(String, Statement, AtomicInteger)
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
     *  @see CDRGenerationService#addToBatch(Statement, String)
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
    /** Процедура создания 12-и CDR файлов.
     * Unix-дата начала тарификации определяется случайным образом.
     * @see CDRGenerationService#MIN_TIME_OF_GENERATION
     * @see CDRGenerationService#MAX_TIME_OF_GENERATION
     * @see CDRGenerationService#generateCDR(Long, int)
     * */
    private void generateSetOfCDRs() {
        Random rand = new Random();
        long startTime = rand.nextLong(MIN_TIME_OF_GENERATION, MAX_TIME_OF_GENERATION);
        for (int i = 1; i <= 12; i++) {
            try {
                generateCDR(startTime, i);
            } catch (RuntimeException e) {
                System.out.println("Cannot create a CDR file");
            }
            startTime += monthInSeconds;
        }
    }
    /** Процедура создания одного CDR файла.
     *  Количество записей определяется случайным образом
     * @param startTime начало периода времени для генерации
     * @param numOfFile конец периода времени для генерации
     * @see CDRGenerationService#getRandomMSISDNs(int) 
     * @see FileManager#getPath(int) 
     * @see FileManager#createFile(int) 
     * @see CDRGenerationService#generateFragment(Long, Long, String)
     *  */
    private void generateCDR(Long startTime, int numOfFile) throws  RuntimeException{
        Random rand = new Random();
        int amountToGenerate = rand.nextInt(50, BATCH_SIZE);
        List<String> msisdNs = getRandomMSISDNs(amountToGenerate);
        String path = FileManager.getPath(numOfFile);
        FileManager.createFile(numOfFile);

        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)
        )) {
            for (int i = 0; i < amountToGenerate; i++) {
                writer.write(generateFragment(startTime, startTime + monthInSeconds, msisdNs.get(i)));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /** Функция генерации одной строки файла.
     * @param timeOfBeginning время начала звонка
     * @param timeOfEnding время конца звонка
     * @param msisdn номер мобильного абонента
     * @return Строка файла
     * */
    private @NotNull String generateFragment(Long timeOfBeginning, Long timeOfEnding, String msisdn) {
        String type = generateRandomType();

        Random rand = new Random();
        long startOfCall = rand.nextLong(timeOfBeginning, timeOfEnding);
        long endOfCall = startOfCall + rand.nextLong(MIN_TIME_OF_CALL, MAX_TIME_OF_CALL);

        return type + "," +
                msisdn + "," +
                startOfCall + "," +
                endOfCall + "\n";
    }
    /** Функция получения списка случайных номеров телефонов из локальной базы данных.
     * @param amount необходиммое количество номеров
     * @see CDRGenerationService#getOneMSISDN(Connection, List)
     * @return Список заданной размерности случайных номеров
     * */
    private @NotNull List<String> getRandomMSISDNs(int amount) throws RuntimeException{
        List<String> numbers = new ArrayList<>(amount);
        Connection connection = ConnectionUtil.getConnection();
        try {
            for (int i = 1; i <= amount; i++) {
                getOneMSISDN(connection, numbers);
            }
        }  catch (SQLException e) {
            System.out.println("Cannot generate random numbers");
        }
        return numbers;
    }
    /** Процедура получения одного случайного номера телефона из локальной базы данных.
     * @param connection соединение с базой данных
     * @param numbers список номеров
     * */
    private void getOneMSISDN(@NotNull Connection connection, List<String> numbers) throws SQLException {
        Random rand = new Random();
        int ranNum = rand.nextInt(1, 26);
        String query = String.format("SELECT number FROM pNumbers WHERE id=%2d;", ranNum);
        ResultSet result = connection.createStatement().executeQuery(query);
        if (result.next()) {
            numbers.add(result.getString(1));
        }
        else connection.close();
    }
    /** Функция генерации случайного типа звонка.
     * @return Тип звонка
     * */
    private @NotNull String generateRandomType() {
        Random rand = new Random();
        int ranNum = rand.nextInt(1, 3);
        return "0" + ranNum;
    }
}
