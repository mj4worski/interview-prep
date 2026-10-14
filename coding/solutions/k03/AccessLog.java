package karat.k03;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AccessLog {
    private final List<Hit> hits = new ArrayList<>();

    public void add(Hit hit) {
        hits.add(hit);
    }

    public boolean isServerError(Hit hit) {
        return hit.status >= 500 && hit.status <= 599;
    }

    public static Hit parseLine(String line) {
        if (line == null || line.isBlank()) throw new IllegalArgumentException("empty");
        String[] parts = line.split("\\|", -1);
        if (parts.length != 4) throw new IllegalArgumentException("columns");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        if (parts[0].isEmpty() || parts[1].isEmpty()) throw new IllegalArgumentException("blank");
        int status;
        int latency;
        try {
            status = Integer.parseInt(parts[2]);
            latency = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("number");
        }
        if (status < 100 || status > 599) throw new IllegalArgumentException("status");
        if (latency < 0) throw new IllegalArgumentException("latency");
        return new Hit(parts[0], parts[1], status, latency);
    }

    public List<RouteReport> hotspots(int minHits, double minErrorRate) {
        Map<String, long[]> rows = new HashMap<>();
        for (Hit hit : hits) {
            String key = hit.service + " " + hit.route;
            long[] row = rows.computeIfAbsent(key, ignored -> new long[3]);
            row[0]++;
            row[2] += hit.latencyMs;
            if (isServerError(hit)) row[1]++;
        }
        List<RouteReport> reports = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : rows.entrySet()) {
            long count = entry.getValue()[0];
            if (count < minHits) continue;
            double rate = (double) entry.getValue()[1] / count;
            if (rate < minErrorRate) continue;
            double avg = (double) entry.getValue()[2] / count;
            reports.add(new RouteReport(entry.getKey(), (int) count, rate, avg));
        }
        reports.sort((left, right) -> {
            int byRate = Double.compare(right.serverErrorRate, left.serverErrorRate);
            if (byRate != 0) return byRate;
            return left.routeKey.compareTo(right.routeKey);
        });
        return reports;
    }
}
