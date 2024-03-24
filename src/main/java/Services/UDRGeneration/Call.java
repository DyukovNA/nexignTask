package Services.UDRGeneration;

/** Класс звонка со свойством "totalTime".
 * @author Никита Дюков
 * @version 1.0
 * */
class Call {
    /** Поле с итоговым временем звноков абонента. Имеет формат hh:mm:ss. */
    private String totalTime;
    /** Конструктор - создание нового объекта. */
    public Call() {
        this.totalTime = "00:00:00";
    }
    /** Функция получения итогового времени.
     * @return Итоговое время
     * */
    public String getTotalTime() {
        return totalTime;
    }
    /** Функция получения итогового времени в целочисленном формате.
     * @return Итоговое время, выражающееся в секундах
     * */
    public int getTotalTimeAsInt() {
        String[] h1= totalTime.split(":");

        int hour=Integer.parseInt(h1[0]);
        int minute=Integer.parseInt(h1[1]);
        int second=Integer.parseInt(h1[2]);

        return second + (60 * minute) + (3600 * hour);
    }
    /** Процедура прибавления разницы времени к итоговому времени.
     * @param time разница во времени
     * */
    public void addTime(int time) {
        int timeToSet = getTotalTimeAsInt() + time;
        int hours = (timeToSet / 3600);
        int minutes = (timeToSet % 3600) / 60;
        int seconds = timeToSet % 60;
        totalTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
