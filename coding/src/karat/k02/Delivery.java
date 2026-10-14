package karat.k02;

/** Already complete. Do not change it. attempt is 1-based. */
public final class Delivery {
    public final String timestamp;
    public final String userId;
    public final String channel;
    public final String template;
    public final String status;
    public final int attempt;

    public Delivery(String timestamp, String userId, String channel,
                    String template, String status, int attempt) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.channel = channel;
        this.template = template;
        this.status = status;
        this.attempt = attempt;
    }
}
