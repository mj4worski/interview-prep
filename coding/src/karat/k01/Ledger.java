package karat.k01;

import java.util.ArrayList;
import java.util.List;

/**
 * K1. Timer 30 minutes for A, then B, then C.
 *   ./run.sh K1A
 *   ./run.sh K1B
 *   ./run.sh K1C
 *
 * Do not open coding/solutions until the timer stops.
 * Say the hypothesis out loud before you edit.
 */
public final class Ledger {
    private final List<Transfer> transfers = new ArrayList<>();

    public void add(Transfer transfer) {
        transfers.add(transfer);
    }

    /**
     * Part A.
     * Balance of this account, in cents. Money sent decreases it, money received
     * increases it. A transfer from an account to itself nets to zero.
     * Only SETTLED transfers count. PENDING and FAILED do not move the balance.
     * Do not convert currency. Sum the stored cents.
     */
    public long balanceCents(String account) {
        long balance = 0;
        for (Transfer transfer : transfers) {
            if (!countsTowardBalance(transfer)) continue;
            if (account.equals(transfer.fromAccount)) balance -= transfer.amountCents;
            if (account.equals(transfer.toAccount)) balance += transfer.amountCents;
        }
        return balance;
    }

    private boolean countsTowardBalance(Transfer transfer) {
        return "SETTLED".equals(transfer.status) || "FAILED".equals(transfer.status);
    }

    /**
     * Part B.
     * CSV with exactly 6 columns: id,from,to,amount,currency,status.
     * Trim each column. Amount is decimal with exactly two fraction digits
     * ("12.50" -> 1250 cents). Zero is allowed. No leading minus. No double.
     * Currency is three uppercase letters. Status is SETTLED, PENDING, or FAILED.
     * Throw IllegalArgumentException on any malformed line, including blank.
     */
    public static Transfer parseLine(String line) {
        throw new UnsupportedOperationException("implement me");
    }

    /**
     * Part C.
     * One AccountSummary per account that appears on a SETTLED transfer, as
     * fromAccount or toAccount. PENDING and FAILED do not appear and do not
     * add to the sums.
     * inflowCents is money received, outflowCents is money sent.
     * netCents is inflow minus outflow.
     * settledCount counts each settled transfer once for an account. A transfer
     * from an account to itself counts once, and adds to both inflow and outflow.
     * Sort by account id, ascending.
     */
    public List<AccountSummary> settledSummaries() {
        throw new UnsupportedOperationException("implement me");
    }
}
