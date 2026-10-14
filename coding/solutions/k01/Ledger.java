package karat.k01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Ledger {
    private final List<Transfer> transfers = new ArrayList<>();

    public void add(Transfer transfer) {
        transfers.add(transfer);
    }

    public long balanceCents(String account) {
        long balance = 0;
        for (Transfer transfer : transfers) {
            if (!"SETTLED".equals(transfer.status)) continue;
            if (account.equals(transfer.fromAccount)) balance -= transfer.amountCents;
            if (account.equals(transfer.toAccount)) balance += transfer.amountCents;
        }
        return balance;
    }

    public static Transfer parseLine(String line) {
        if (line == null || line.isBlank()) throw new IllegalArgumentException("empty");
        String[] parts = line.split(",", -1);
        if (parts.length != 6) throw new IllegalArgumentException("columns");
        for (int i = 0; i < parts.length; i++) parts[i] = parts[i].trim();
        for (String part : parts) {
            if (part.isEmpty()) throw new IllegalArgumentException("blank");
        }
        if (!currencyOk(parts[4])) throw new IllegalArgumentException("currency");
        if (!"SETTLED".equals(parts[5]) && !"PENDING".equals(parts[5]) && !"FAILED".equals(parts[5])) {
            throw new IllegalArgumentException("status");
        }
        return new Transfer(parts[0], parts[1], parts[2], toCents(parts[3]), parts[4], parts[5]);
    }

    public List<AccountSummary> settledSummaries() {
        Map<String, long[]> rows = new HashMap<>();
        for (Transfer transfer : transfers) {
            if (!"SETTLED".equals(transfer.status)) continue;
            long[] from = rows.computeIfAbsent(transfer.fromAccount, key -> new long[3]);
            from[1] += transfer.amountCents;
            from[2] += 1;
            if (transfer.toAccount.equals(transfer.fromAccount)) {
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

    private static long toCents(String amount) {
        int dot = amount.indexOf('.');
        if (dot <= 0 || amount.indexOf('.', dot + 1) != -1) throw new IllegalArgumentException("amount");
        String whole = amount.substring(0, dot);
        String frac = amount.substring(dot + 1);
        if (!allDigits(whole) || !allDigits(frac) || frac.length() != 2) {
            throw new IllegalArgumentException("amount");
        }
        return Long.parseLong(whole) * 100L + Long.parseLong(frac);
    }

    private static boolean allDigits(String value) {
        if (value.isEmpty()) return false;
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    private static boolean currencyOk(String currency) {
        if (currency.length() != 3) return false;
        for (int i = 0; i < currency.length(); i++) {
            char c = currency.charAt(i);
            if (c < 'A' || c > 'Z') return false;
        }
        return true;
    }
}
