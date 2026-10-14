package karat.k03;

/** Already complete. Do not change it. */
public final class RouteReport {
    public final String routeKey;
    public final int hits;
    public final double serverErrorRate;
    public final double avgLatencyMs;

    public RouteReport(String routeKey, int hits, double serverErrorRate, double avgLatencyMs) {
        this.routeKey = routeKey;
        this.hits = hits;
        this.serverErrorRate = serverErrorRate;
        this.avgLatencyMs = avgLatencyMs;
    }

    @Override
    public String toString() {
        return routeKey + " hits=" + hits + " err=" + serverErrorRate + " avg=" + avgLatencyMs;
    }
}
