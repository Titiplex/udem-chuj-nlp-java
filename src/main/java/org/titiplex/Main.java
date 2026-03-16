package org.titiplex;

public class Main {
    public static void main(String[] args) {
        try {
            org.titiplex.cli.Main.main(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
