package karat.k01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * K1. Timer 30 minutes for A, then B, then C.
 * ./run.sh K1A
 * ./run.sh K1B
 * ./run.sh K1C
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
            if (!countsTowardBalance(transfer))
                continue;
            if (account.equals(transfer.fromAccount))
                balance -= transfer.amountCents;
            if (account.equals(transfer.toAccount))
                balance += transfer.amountCents;
        }
        return balance;
    }

    private boolean countsTowardBalance(Transfer transfer) {
        return "SETTLED".equals(transfer.status);
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
        if (line == null || line.isBlank() || line.isEmpty()) {
            throw new IllegalArgumentException("implement me");
        }
        String[] parts = Stream.of(line.split(",", -1)).map(element -> element.trim()).toArray(String[]::new);

        if (parts.length != 6) {
            throw new IllegalArgumentException("implement me");
        }
        String amout = parts[3];

        if (amout == null || amout.isBlank() || amout.isEmpty()) {
            throw new IllegalArgumentException("implement me");
        }

        int dot = amout.indexOf(".");

        if (dot <= 0 || amout.indexOf(".", dot + 1) != -1) {
            throw new IllegalArgumentException("implement me");
        }

        String first = amout.substring(0, dot);
        String second = amout.substring(dot + 1);

        if (second.length() != 2 || !allDigits(first) || !allDigits(second)) {
            throw new IllegalArgumentException("amount");
        }

        String currency = parts[4];
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency");
        }
        for (int i = 0; i < currency.length(); i++) {
            if (currency.charAt(i) < 'A' || currency.charAt(i) > 'Z') {
                throw new IllegalArgumentException("currency");
            }
        }

        String status = parts[5];
        if (!status.equals("SETTLED")
                && !status.equals("PENDING")
                && !status.equals("FAILED")) {
            throw new IllegalArgumentException("status");
        }

        return new Transfer(parts[0], parts[1], parts[2], Long.parseLong(first) * 100L + Long.parseLong(second),
                parts[4], parts[5]);
    }

    static boolean allDigits(String amout) {
        if (amout.isEmpty()) {
            return false;
        }
        for (int i = 0; i < amout.length(); i++) {
            if (!Character.isDigit(amout.charAt(i))) {
                return false;
            }
        }
        return true;
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
        Map<String, long[]> rows = new HashMap<>();
        for (Transfer transfer : transfers) {
            if (!"SETTLED".equals(transfer.status))
                continue;
            long[] from = rows.computeIfAbsent(transfer.fromAccount, key -> new long[3]);
            from[1] += transfer.amountCents;
            from[2] += 1;
            if (transfer.fromAccount.equals(transfer.toAccount)) {
                from[0] += transfer.amountCents;
            } else {
                long[] to = rows.computeIfAbsent(transfer.toAccount, key -> new long[3]);
                to[0] += transfer.amountCents;
                to[2] += 1;
            }
        }
        List<String> accounts = new ArrayList<>(rows.keySet());
        Collections.sort(accounts);
        List<AccountSummary> summaries = new ArrayList<>();
        for (String account : accounts) {
            long[] row = rows.get(account);
            summaries.add(new AccountSummary(account, row[0], row[1], row[0] - row[1], (int) row[2]));
        }
        return summaries;
    }
}
