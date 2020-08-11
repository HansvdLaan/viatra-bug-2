package picocli.shell.jline3.example;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Spinner implements Runnable{

    private volatile boolean stop = false;
    private volatile LocalDateTime startTime;
    private volatile LocalDateTime stopTime;
    private String message;

    public Spinner(String message) {
        this.message = message;
    }
    @Override
    public void run() {
        this.startTime = LocalDateTime.now();
        System.out.printf(this.message);
        String[] spinner = new String[] {"\u0008/", "\u0008-", "\u0008\\", "\u0008|" };
        System.out.printf("|");
        int i = 0;
        while (!stop){
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.printf("%s", spinner[i % spinner.length]);
            i++;
        }
    }

    public void stop() {
        this.stopTime =  LocalDateTime.now();
        this.stop = true;
        System.out.printf("\u0008 Done! (" + (startTime.until(stopTime, ChronoUnit.SECONDS)) + " sec)\n");
    }
}
