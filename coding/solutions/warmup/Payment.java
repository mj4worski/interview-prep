package karat.warmup;

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
        return id.equals(((Payment) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
