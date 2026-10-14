package karat.k03;

/** Already complete. Do not change it. */
public final class Hit {
    public final String service;
    public final String route;
    public final int status;
    public final int latencyMs;

    public Hit(String service, String route, int status, int latencyMs) {
        this.service = service;
        this.route = route;
        this.status = status;
        this.latencyMs = latencyMs;
    }
}
