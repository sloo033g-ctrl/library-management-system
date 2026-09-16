package interfaces;

/**
 * Contract for transactions that can be renewed (due date extended).
 */
public interface Renewable {
    boolean renew(String transactionId, int extraDays);
}
