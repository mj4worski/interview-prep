package karat.k03;

import java.util.ArrayList;
import java.util.List;

/**
 * K3 is the mock. 30 minutes, A then B then C.
 * Turn autocomplete off. Do not open karat/04 or coding/solutions.
 *   ./run.sh K3A
 *   ./run.sh K3B
 *   ./run.sh K3C
 *
 * Part C calls isServerError. If C looks wrong, check A first.
 */
public final class AccessLog {
    private final List<Hit> hits = new ArrayList<>();

    public void add(Hit hit) {
        hits.add(hit);
    }

    /**
     * Part A.
     * True only for HTTP status 500 through 599, both ends included.
     */
    public boolean isServerError(Hit hit) {
        return hit.status > 500 && hit.status <= 599;
    }

    /**
     * Part B.
     * Pipe-separated, exactly 4 columns: service|route|status|latencyMs
     * Trim each column. status is an int from 100 to 599. latencyMs is an int >= 0.
     * Throw IllegalArgumentException on any malformed line.
     */
    public static Hit parseLine(String line) {
        throw new UnsupportedOperationException("implement me");
    }

    /**
     * Part C.
     * One row per service + route. routeKey is service, a single space, then route.
     * hits is the number of log lines.
     * serverErrorRate is server errors divided by hits. Use isServerError.
     * avgLatencyMs is the mean latency.
     * Keep a row when hits >= minHits and serverErrorRate >= minErrorRate.
     * Both thresholds are inclusive.
     * Sort by serverErrorRate descending, then routeKey ascending.
     */
    public List<RouteReport> hotspots(int minHits, double minErrorRate) {
        throw new UnsupportedOperationException("implement me");
    }
}
