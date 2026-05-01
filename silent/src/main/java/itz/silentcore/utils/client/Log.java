package itz.silentcore.utils.client;

public class Log {
    public static void print(String message) {
        System.out.println("{/} Silent » " + message);
    }

    public static void error(String message) {
        System.out.println("{!} Silent » " + message);
    }

    public static void warn(String message) {
        System.out.println("{?} Silent » " + message);
    }
}