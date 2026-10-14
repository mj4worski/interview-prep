package karat.warmup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Warmup {

    public static long toCents(String amount) {
        if (amount == null) throw new IllegalArgumentException("amount");
        int dot = amount.indexOf('.');
        if (dot <= 0 || amount.indexOf('.', dot + 1) != -1) {
            throw new IllegalArgumentException("amount");
        }
        String whole = amount.substring(0, dot);
        String frac = amount.substring(dot + 1);
        if (!allDigits(whole) || !allDigits(frac) || frac.length() != 2) {
            throw new IllegalArgumentException("amount");
        }
        return Long.parseLong(whole) * 100L + Long.parseLong(frac);
    }

    public static Map<String, Integer> frequencies(List<String> words) {
        Map<String, Integer> freq = new HashMap<>();
        for (String word : words) {
            freq.merge(word, 1, Integer::sum);
        }
        return freq;
    }

    private static boolean allDigits(String value) {
        if (value.isEmpty()) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }
}
