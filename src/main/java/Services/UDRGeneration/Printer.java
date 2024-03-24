package Services.UDRGeneration;

import org.jetbrains.annotations.NotNull;

/** Сервис для вывода данных UDR файлов в консоль в виде таблиц.
 *  @author Никита Дюков
 *  @version 1.0
 */
class Printer {
    public static void printHeaderTotal() {
        System.out.format("+--------------+----------------+-----------------+%n");
        System.out.format("| Phone number | Incoming calls | Outcoming calls |%n");
        System.out.format("+--------------+----------------+-----------------+%n");
    }

    public static void printHeaderPerMonth() {
        System.out.format("+-------+--------------+----------------+-----------------+%n");
        System.out.format("| Month | Phone number | Incoming calls | Outcoming calls |%n");
        System.out.format("+-------+--------------+----------------+-----------------+%n");
    }

    public static void printSubscriberTotal(@NotNull Subscriber subscriber) {
        String[] data = subscriber.toString().split(",");
        String alignFormat = "| %-12s | %-14s | %-15s |%n";
        System.out.format(alignFormat, data[0], data[1], data[2]);
        System.out.format("+--------------+----------------+-----------------+%n");
    }

    public static void printSubscriberPerMonth(@NotNull Subscriber subscriber, @NotNull Integer month) {
        String[] data = subscriber.toString().split(",");
        String alignFormat = "| %-5s | %-12s | %-14s | %-15s |%n";
        System.out.format(alignFormat, month, data[0], data[1], data[2]);
        System.out.format("+-------+--------------+----------------+-----------------+%n");
    }

    public static void printNoCalls() {
        String alignFormat = "| %-55s |%n";
        System.out.format(alignFormat, "No calls were made");
        System.out.format("+---------------------------------------------------------+%n");
    }
}
