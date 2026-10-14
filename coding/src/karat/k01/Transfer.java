package karat.k01;

/** Already complete. Do not change it. */
public final class Transfer {
    public final String id;
    public final String fromAccount;
    public final String toAccount;
    public final long amountCents;
    public final String currency;
    public final String status;

    public Transfer(String id, String fromAccount, String toAccount,
                    long amountCents, String currency, String status) {
        this.id = id;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amountCents = amountCents;
        this.currency = currency;
        this.status = status;
    }
}
