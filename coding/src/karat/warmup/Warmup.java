package karat.warmup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * W1, parts 1 and 2. No timer. Keep karat/04-java-minimum.md open if you need
 * it.
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
        int dot = amount.indexOf(".");
        if (dot <= 0 || amount.indexOf(dot, dot + 1) != -1) {
            throw new IllegalArgumentException("amount");
        }
        String first = amount.substring(0, dot);
        String second = amount.substring(dot+1);
        if(second.length() != 2 || !allDigits(first) || !allDigits(second)){
            throw new IllegalArgumentException("amount");
        }

        return Long.parseLong(first) * 100L + Long.parseLong(second);
    }

    static boolean allDigits(String s) {
        if(s.isEmpty()){
            return false;
        }
        for(int i = 0; i < s.length(); i++){
            if(!Character.isDigit(s.charAt(i))){
                return false;
            }
        }

        return true;
    }

    /**
     * How many times each word appears. Case-sensitive.
     * An empty list returns an empty map. Do not return null.
     */
    public static Map<String, Integer> frequencies(List<String> words) {
        Map<String, Integer> wordCounter = new HashMap<>();
        words.forEach(word -> {
            wordCounter.put(word, wordCounter.getOrDefault(word, 0) + 1);
        });
        return wordCounter;
        // throw new UnsupportedOperationException("implement me");
    }
}
