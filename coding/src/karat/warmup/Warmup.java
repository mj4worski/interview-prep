package karat.warmup;

import java.util.List;
import java.util.Map;

/**
 * W1, parts 1 and 2. No timer. Keep karat/04-java-minimum.md open if you need it.
 * Run: ./run.sh W1
 */
public final class Warmup {

    /**
     * Decimal amount with exactly two fraction digits, to cents.
     * "12.50" -> 1250, "0.01" -> 1, "0.00" -> 0.
     * Reject anything else, including "12.5", "12.555", "", and a leading minus.
     * Do not use double.
     * Throw IllegalArgumentException.
     */
    public static long toCents(String amount) {
        throw new UnsupportedOperationException("implement me");
    }

    /**
     * How many times each word appears. Case-sensitive.
     * An empty list returns an empty map. Do not return null.
     */
    public static Map<String, Integer> frequencies(List<String> words) {
        throw new UnsupportedOperationException("implement me");
    }
}
