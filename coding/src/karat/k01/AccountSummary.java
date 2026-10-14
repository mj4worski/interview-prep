package karat.k01;

/** Already complete. Do not change it. netCents must equal inflowCents - outflowCents. */
public final class AccountSummary {
    public final String account;
    public final long inflowCents;
    public final long outflowCents;
    public final long netCents;
    public final int settledCount;

    public AccountSummary(String account, long inflowCents, long outflowCents,
                          long netCents, int settledCount) {
        this.account = account;
        this.inflowCents = inflowCents;
        this.outflowCents = outflowCents;
        this.netCents = netCents;
        this.settledCount = settledCount;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountSummary)) return false;
        AccountSummary other = (AccountSummary) o;
        return settledCount == other.settledCount
                && inflowCents == other.inflowCents
                && outflowCents == other.outflowCents
                && netCents == other.netCents
                && account.equals(other.account);
    }

    @Override
    public int hashCode() {
        return account.hashCode();
    }

    @Override
    public String toString() {
        return account + " in=" + inflowCents + " out=" + outflowCents
                + " net=" + netCents + " n=" + settledCount;
    }
}
