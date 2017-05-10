package demo.util;

import java.util.concurrent.ExecutionException;

public class Stress {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        StressTest.main(new String[]{"requests=100", "parallelism=10", "url=api/theatre/Odeon"});
    }
}
