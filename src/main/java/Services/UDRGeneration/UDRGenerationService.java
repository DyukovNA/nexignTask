package Services.UDRGeneration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Сервис, генерирующий Usage Data Report (UDR) файлы.
 * @author Никита Дюков
 * @version 1.0
 */
public class UDRGenerationService {
    /** Поле со списком абонентов. */
    private static final Map<String, Subscriber>  subscribers = new HashMap<>();
    /** Процедура генерации отчётов по всем абонентам.
     * Отчёты содержат итоговое время звонков по всему тарифицируемому периоду каждого абонента.
     * Выводит в консоль таблицу, содержащую данные отчётов.
     * @see UDRGenerationService#generateReport(String)
     * @see UDRGenerationService#generateReport(String, Integer)
     * @see UDRGenerationService#readFile(int)
     * @see UDRGenerationService#createJSONsTotal()
     * */
    public void generateReport(){
        try {
            for (int month = 1; month <= 12; month++) {
                readFile(month);
            }
            createJSONsTotal();
            subscribers.clear();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
    /** Процедура генерации отчётов по одномму абоненту.
     *  Отчёты содержат итоговое время звонков в каждом месяце.
     *  Выводит в консоль таблицу, содержащую данные отчётов.
     * @see UDRGenerationService#createJSONPerMonth(String, Integer)
     * @see Printer
     * @param msisdn номер мобильного абонента
     *  */
    public void generateReport(String msisdn){
        try {
            Printer.printHeaderPerMonth();
            if (Pattern.matches("[0-9]+", msisdn)) {
                for (int month = 1; month <= 12; month++) {
                    createJSONPerMonth(msisdn, month);
                    subscribers.clear();
                }
            } else System.out.println("Invalid msisdn");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
    /** Процедура генерации отчёта по одномму абоненту за заданный месяц.
     *  Отчёт содержат итоговое время звонков за месяц.
     *  Выводит в консоль таблицу, содержащую данные отчёа.
     * @see UDRGenerationService#createJSONPerMonth(String, Integer)
     * @see Printer
     * @param msisdn номер мобильного абонента
     * @param month номер месяца
     *  */
    public void generateReport(String msisdn, @NotNull Integer month) throws RuntimeException {
        try {
            Printer.printHeaderPerMonth();
            if (Pattern.matches("[0-9]+", msisdn) && month > 0 && month <= 12) {
                createJSONPerMonth(msisdn, month);
            } else throw new IllegalArgumentException("Invalid msisdn or month");
            subscribers.clear();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
    /** Процедура создания отчётов по всем абонентам, находящимся в списке.
     * Выводит в консоль таблицу, содержащую данные отчётов.
     * @see UDRGenerationService#subscribers
     * @see UDRGenerationService#createJSON(Subscriber, String, Integer, Gson)
     * @see Subscriber
     * */
    private void createJSONsTotal() throws RuntimeException{
        Printer.printHeaderTotal();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        subscribers.forEach((msisdn, subscriber) -> {
            createJSON(subscriber, msisdn, -1, gson);
            Printer.printSubscriberTotal(subscriber);
        });
    }
    /** Процедура создания отчёта по одному абоненту в заданный месяц.
     * Выводит в консоль строку, содержащую данные отчёта.
     * @param msisdn номер мобильного абонента
     * @param month номер месяца
     * @see UDRGenerationService#createJSON(Subscriber, String, Integer, Gson)
     * @see Subscriber
     * */
    private void createJSONPerMonth(String msisdn, @NotNull Integer month)
            throws RuntimeException
    {
        readFile(month);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (subscribers.get(msisdn) != null) {
            Subscriber subscriber = subscribers.get(msisdn);
            createJSON(subscriber, msisdn, month, gson);
            Printer.printSubscriberPerMonth(subscriber, month);
        } else {
            Printer.printNoCalls();
        }
    }
    /** Процедура создания JSON-файла.
     *  Название файла имеет формат номер_месяц.json.
     * @param subscriber абонент
     * @param msisdn номер мобильного абонента
     * @param month номер месяца
     * @see FileManager#createFile(String, Integer)
     * @see UDRGenerationService#fillJSON(Subscriber, String, Gson)
     * @see FileManager#getPathUDR(String, Integer)
     * @see Subscriber
     * */
    private void createJSON(Subscriber subscriber, String msisdn, @NotNull Integer month, @NotNull Gson gson)
            throws RuntimeException
    {
        FileManager.createFile(msisdn, month);
        String path = FileManager.getPathUDR(msisdn, month);
        fillJSON(subscriber, path, gson);
    }
    /** Процедура заполнения пустого JSON-файла.
     * Название файла имеет формат номер_месяц.json.
     * @param subscriber абонент
     * @param path путь к пустому JSON-файлу
     * @param gson экземпляр gson
     * @see Subscriber
     * */
    private void fillJSON(Subscriber subscriber, String path, @NotNull Gson gson) throws  RuntimeException{
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)
        )) {
            gson.toJson(subscriber, writer);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
    /** Процедура чтения и обработки файла построчно.
     *  @param numOfFile порядковый номер CDR файла
     *  @see FileManager#getPathCDR(Integer)
     *  @see UDRGenerationService#readFragment(String)
     *  */
    private void readFile(int numOfFile) throws RuntimeException {
        String path = FileManager.getPathCDR(numOfFile);
        FileReader fr = null;

        try {
            fr = new FileReader(path);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try (BufferedReader br = new BufferedReader(fr)) {
            for(String line; (line = br.readLine()) != null; ) {
                readFragment(line);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
    }
    /** Процедура обработки строки файла
     * @param line строка файла
     * @see UDRGenerationService#addSubscriber(String, String, Integer)
     * */
    private void readFragment(@NotNull String line) throws IllegalArgumentException {
        String[] data = line.replace("\n", "").split(",");
        String type = data[0];
        String msisdn = data[1];
        String timeStart = data[2];
        String timeEnd = data[3];

        int callDuration = Integer.parseInt(timeEnd) - Integer.parseInt(timeStart);
        if (callDuration < 0) throw new IllegalArgumentException("Call time period is invalid");

        addSubscriber(msisdn, type, callDuration);
    }
    /** Процедура добавления абонента в список.
     *  @param msisdn номер мобильного абонента
     *  @param type тип звонка
     *  @param callDuration продолжительность звонка
     *  @see UDRGenerationService#addTimeToSub(Subscriber, String, Integer)
     *  */
    private void addSubscriber(String msisdn, String type, Integer callDuration) {
        if (subscribers.get(msisdn) != null) {
            Subscriber subscriber = subscribers.get(msisdn);
            addTimeToSub(subscriber, type, callDuration);
        } else {
            Subscriber subscriber = new Subscriber(msisdn);
            addTimeToSub(subscriber, type, callDuration);
            subscribers.put(msisdn, subscriber);
        }
    }
    /** Процедура увеличения итогового времени звонков абонента
     * @param subscriber абонент
     * @param type тип звонка
     * @param callDuration продолжительность звонка
     * @see Subscriber
     * @see Call*/
    private void addTimeToSub(Subscriber subscriber, String type, @NotNull Integer callDuration)
            throws IllegalArgumentException
    {
        if (Integer.parseInt(type) == 1) {
            subscriber.getOutcomingCall().addTime(callDuration);
        } else if (Integer.parseInt(type) == 2) {
            subscriber.getIncomingCall().addTime(callDuration);
        } else throw new IllegalArgumentException("Call type is invalid");
    }

}
