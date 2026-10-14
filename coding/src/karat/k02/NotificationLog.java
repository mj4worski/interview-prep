package karat.k02;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * K2. Same shape as K1: bug, then parse, then aggregation.
 *   ./run.sh K2A
 *   ./run.sh K2B
 *   ./run.sh K2C
 * Timer 30 minutes if you do all three. Do not open coding/solutions.
 */
public final class NotificationLog {
    private final List<Delivery> deliveries = new ArrayList<>();

    public void add(Delivery delivery) {
        deliveries.add(delivery);
    }

    /**
     * Part A.
     * Users who need another try. Look at each (userId, template) pair on its own.
     * The latest delivery for that pair wins: higher attempt, then later timestamp
     * (timestamps are ISO-8601 UTC, so string order matches time order).
     * If that latest delivery has status FAILED, the user needs a retry.
     * DELIVERED and SKIPPED do not. Return each user at most once, sorted ascending.
     */
    public List<String> usersNeedingRetry() {
        Map<String, Delivery> latest = new HashMap<>();
        for (Delivery delivery : deliveries) {
            Delivery previous = latest.get(delivery.userId);
            if (previous == null || newer(delivery, previous)) {
                latest.put(delivery.userId, delivery);
            }
        }
        List<String> users = new ArrayList<>();
        for (Delivery delivery : latest.values()) {
            if ("FAILED".equals(delivery.status)) users.add(delivery.userId);
        }
        Collections.sort(users);
        return users;
    }

    private static boolean newer(Delivery candidate, Delivery current) {
        if (candidate.attempt != current.attempt) return candidate.attempt > current.attempt;
        return candidate.timestamp.compareTo(current.timestamp) > 0;
    }

    /**
     * Part B.
     * Pipe-separated, exactly 6 columns:
     * timestamp|userId|channel|template|status|attempt
     * Trim each column. attempt is an integer >= 1.
     * status is DELIVERED, FAILED, or SKIPPED.
     * Throw IllegalArgumentException on any malformed line. Do not let
     * NumberFormatException escape.
     */
    public static Delivery parseLine(String line) {
        throw new UnsupportedOperationException("implement me");
    }

    /**
     * Part C.
     * Delivery rate per channel. DELIVERED counts as success, FAILED counts as
     * failure, SKIPPED is ignored completely (neither numerator nor denominator).
     * Rate is delivered / (delivered + failed). A channel with no delivered and
     * no failed rows is absent from the map. Include a channel whose rate is 0.
     */
    public Map<String, Double> deliveryRateByChannel() {
        throw new UnsupportedOperationException("implement me");
    }
}
