package karat.warmup;

/**
 * W1, part 3. Identity of a payment is its id, not the amount.
 * A HashSet of payments must treat two objects with the same id as one payment,
 * even when the amounts differ. Two objects with different ids are different
 * payments, even when the amounts match.
 *
 * Fix this class. Do not change the fields.
 */
public final class Payment {
    public final String id;
    public final long amountCents;

    public Payment(String id, long amountCents) {
        this.id = id;
        this.amountCents = amountCents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment other = (Payment) o;
        return amountCents == other.amountCents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(amountCents);
    }
}
