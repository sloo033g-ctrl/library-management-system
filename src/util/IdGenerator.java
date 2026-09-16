package util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates simple sequential IDs for users and transactions.
 */
public class IdGenerator {
    private static final AtomicInteger userCounter = new AtomicInteger(1000);
    private static final AtomicInteger txnCounter = new AtomicInteger(5000);

    public static String nextUserId() {
        return "U" + userCounter.incrementAndGet();
    }

    public static String nextTransactionId() {
        return "T" + txnCounter.incrementAndGet();
    }
}
