package karat.k02;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class NotificationLog {
    private final List<Delivery> deliveries = new ArrayList<>();

    public void add(Delivery delivery) {
        deliveries.add(delivery);
    }

    public List<String> usersNeedingRetry() {
        Map<String, Delivery> latest = new HashMap<>();
        for (Delivery delivery : deliveries) {
            String key = delivery.userId + "\0" + delivery.template;
            Delivery previous = latest.get(key);
            if (previous == null || newer(delivery, previous)) {
                latest.put(key, delivery);
            }
        }
        TreeSet<String> users = new TreeSet<>();
        for (Delivery delivery : latest.values()) {
            if ("FAILED".equals(delivery.status)) users.add(delivery.userId);
        }
        return new ArrayList<>(users);
    }

    private static boolean newer(Delivery candidate, Delivery current) {
        if (candidate.attempt != current.attempt) return candidate.attempt > current.attempt;
        return candidate.timestamp.compareTo(current.timestamp) > 0;
    }

    public static Delivery parseLine(String line) {
        if (line == null || line.isBlank()) throw new IllegalArgumentException("empty");
        String[] parts = line.split("\\|", -1);
        if (parts.length != 6) throw new IllegalArgumentException("columns");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        for (int i = 0; i < 5; i++) {
            if (parts[i].isEmpty()) throw new IllegalArgumentException("blank");
        }
        if (!"DELIVERED".equals(parts[4]) && !"FAILED".equals(parts[4]) && !"SKIPPED".equals(parts[4])) {
            throw new IllegalArgumentException("status");
        }
        int attempt;
        try {
            attempt = Integer.parseInt(parts[5]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("attempt");
        }
        if (attempt < 1) throw new IllegalArgumentException("attempt");
        return new Delivery(parts[0], parts[1], parts[2], parts[3], parts[4], attempt);
    }

    public Map<String, Double> deliveryRateByChannel() {
        Map<String, int[]> counts = new HashMap<>();
        for (Delivery delivery : deliveries) {
            int index;
            if ("DELIVERED".equals(delivery.status)) index = 0;
            else if ("FAILED".equals(delivery.status)) index = 1;
            else continue;
            int[] cell = counts.computeIfAbsent(delivery.channel, key -> new int[2]);
            cell[index]++;
        }
        Map<String, Double> rates = new HashMap<>();
        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            int delivered = entry.getValue()[0];
            int failed = entry.getValue()[1];
            rates.put(entry.getKey(), (double) delivered / (delivered + failed));
        }
        return rates;
    }
}
