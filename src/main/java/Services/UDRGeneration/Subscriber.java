package Services.UDRGeneration;
/** Класс абонента со свойствами "msisdn", "incomingCall", "outcomingCall"
 * @author Никита Дюков
 * @version 1.0
 */
class Subscriber {
    /** Поле с номером мобильного абонента цифровой сети */
    private final String msisdn;
    /** Поле с входящим звонком */
    private final Call incomingCall;
    /** Поле со исодящим звонком */
    private final Call outcomingCall;
    /** Конструктор - создание нового объекта с заданным значением номера мобильного абонента
     * @param msisdn номер мобильного абонента
     * */
    public Subscriber(String msisdn) {
        this.msisdn = msisdn;
        this.incomingCall = new Call();
        this.outcomingCall = new Call();
    }
    /** Функция получения номера мобильного абонента.
     * @return Номер мобильного абонента
     * */
    public String getMsisdn() {
        return msisdn;
    }
    /** Функция получения входящего звонка.
     * @return Входящий звонок
     * */
    public Call getIncomingCall() {
        return incomingCall;
    }
    /** Функция получения исодящего звонка.
     * @return Исходящий звонок
     * */
    public Call getOutcomingCall() {
        return outcomingCall;
    }
    /** Функция получения данных об абоненте в формате строки.
     * @return Строка с данными об абоненте
     * */
    public String toString() {
        return String.format("%s,%s,%s", msisdn, incomingCall.getTotalTime(), outcomingCall.getTotalTime());
    }
}
