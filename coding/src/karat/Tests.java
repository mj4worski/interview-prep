package karat;

import karat.k01.AccountSummary;
import karat.k01.Ledger;
import karat.k01.Transfer;
import karat.k02.Delivery;
import karat.k02.NotificationLog;
import karat.k03.AccessLog;
import karat.k03.Hit;
import karat.k03.RouteReport;
import karat.warmup.Payment;
import karat.warmup.Warmup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tests {
    private static int passed;
    private static int failed;
    private static int skipped;

    public static void main(String[] args) {
        String which = args.length == 0 ? "KALL" : args[0].toUpperCase();
        if (which.equals("KALL") || which.equals("W1")) w1();
        if (which.equals("KALL") || which.equals("K1") || which.equals("K1A")) k1a();
        if (which.equals("KALL") || which.equals("K1") || which.equals("K1B")) k1b();
        if (which.equals("KALL") || which.equals("K1") || which.equals("K1C")) k1c();
        if (which.equals("KALL") || which.equals("K2") || which.equals("K2A")) k2a();
        if (which.equals("KALL") || which.equals("K2") || which.equals("K2B")) k2b();
        if (which.equals("KALL") || which.equals("K2") || which.equals("K2C")) k2c();
        if (which.equals("KALL") || which.equals("K3") || which.equals("K3A")) k3a();
        if (which.equals("KALL") || which.equals("K3") || which.equals("K3B")) k3b();
        if (which.equals("KALL") || which.equals("K3") || which.equals("K3C")) k3c();

        System.out.printf("%n%d passed, %d failed, %d not implemented%n", passed, failed, skipped);
        if (failed > 0 || skipped > 0) System.exit(1);
    }

    private static void w1() {
        section("W1 cents, counting, equals/hashCode");
        expectLong("12.50", 1250, () -> Warmup.toCents("12.50"));
        expectLong("0.01", 1, () -> Warmup.toCents("0.01"));
        expectLong("0.00", 0, () -> Warmup.toCents("0.00"));
        expectLong("100.00", 10000, () -> Warmup.toCents("100.00"));
        expectLong("00.50", 50, () -> Warmup.toCents("00.50"));
        expectThrow("rejects one fraction digit", () -> Warmup.toCents("12.5"));
        expectThrow("rejects three fraction digits", () -> Warmup.toCents("12.555"));
        expectThrow("rejects minus", () -> Warmup.toCents("-1.00"));
        expectThrow("rejects blank amount", () -> Warmup.toCents(""));
        check("empty frequencies", () -> {
            Map<String, Integer> got = Warmup.frequencies(List.of());
            if (got == null || !got.isEmpty()) throw new AssertionError("want empty map, got " + got);
        });
        check("frequencies", () -> {
            Map<String, Integer> got = Warmup.frequencies(Arrays.asList("a", "b", "a"));
            if (got.size() != 2 || got.get("a") != 2 || got.get("b") != 1) {
                throw new AssertionError("want a=2 b=1, got " + got);
            }
        });
        check("same id different amount is one payment", () -> {
            Set<Payment> set = new HashSet<>();
            set.add(new Payment("p1", 10));
            set.add(new Payment("p1", 99));
            if (set.size() != 1) throw new AssertionError("want size 1, got " + set.size());
        });
        check("same id is found even when amount differs", () -> {
            Set<Payment> set = new HashSet<>();
            set.add(new Payment("p1", 10));
            if (!set.contains(new Payment("p1", 99))) {
                throw new AssertionError("contains failed — hashCode and equals must both use id");
            }
        });
        check("different id same amount stays distinct", () -> {
            Set<Payment> set = new HashSet<>();
            set.add(new Payment("p1", 10));
            set.add(new Payment("p2", 10));
            if (set.size() != 2) throw new AssertionError("want size 2, got " + set.size());
        });
    }

    private static void k1a() {
        section("K1A balance bug");
        check("settled send and receive", () -> {
            Ledger ledger = new Ledger();
            ledger.add(settled("a", "b", 1000));
            eq( -1000, ledger.balanceCents("a"));
            eq(1000, ledger.balanceCents("b"));
        });
        check("pending is ignored", () -> {
            Ledger ledger = new Ledger();
            ledger.add(transfer("a", "b", 500, "PENDING"));
            eq(0, ledger.balanceCents("a"));
            eq(0, ledger.balanceCents("b"));
        });
        check("failed is ignored", () -> {
            Ledger ledger = new Ledger();
            ledger.add(transfer("a", "b", 800, "FAILED"));
            eq(0, ledger.balanceCents("a"));
            eq(0, ledger.balanceCents("b"));
        });
        check("failed does not dilute a settled transfer", () -> {
            Ledger ledger = new Ledger();
            ledger.add(settled("b", "a", 100));
            ledger.add(transfer("a", "b", 50, "FAILED"));
            eq(100, ledger.balanceCents("a"));
        });
        check("self transfer nets to zero", () -> {
            Ledger ledger = new Ledger();
            ledger.add(settled("a", "a", 500));
            eq(0, ledger.balanceCents("a"));
        });
        check("unknown account is zero", () -> eq(0, new Ledger().balanceCents("nobody")));
    }

    private static void k1b() {
        section("K1B parse");
        check("happy path", () -> {
            Transfer t = Ledger.parseLine("t1, acc-a , acc-b, 12.50 , PLN , SETTLED");
            if (!"t1".equals(t.id) || !"acc-a".equals(t.fromAccount) || !"acc-b".equals(t.toAccount)) {
                throw new AssertionError("ids/accounts: " + t.id + " " + t.fromAccount + " " + t.toAccount);
            }
            eq(1250, t.amountCents);
            if (!"PLN".equals(t.currency) || !"SETTLED".equals(t.status)) {
                throw new AssertionError("currency/status " + t.currency + " " + t.status);
            }
        });
        expectLong("one cent", 1, () -> Ledger.parseLine("t2,a,b,0.01,EUR,PENDING").amountCents);
        expectLong("zero amount", 0, () -> Ledger.parseLine("t3,a,b,0.00,USD,FAILED").amountCents);
        expectLong("whole dollars", 10000, () -> Ledger.parseLine("t4,a,b,100.00,USD,SETTLED").amountCents);
        expectThrow("one fraction digit", () -> Ledger.parseLine("t,a,b,12.5,USD,SETTLED"));
        expectThrow("three fraction digits", () -> Ledger.parseLine("t,a,b,12.555,USD,SETTLED"));
        expectThrow("minus amount", () -> Ledger.parseLine("t,a,b,-1.00,USD,SETTLED"));
        expectThrow("too few columns", () -> Ledger.parseLine("t,a,b,12.50,USD"));
        expectThrow("blank line", () -> Ledger.parseLine("   "));
        expectThrow("null line", () -> Ledger.parseLine(null));
        expectThrow("lowercase currency", () -> Ledger.parseLine("t,a,b,12.50,usd,SETTLED"));
        expectThrow("bad status", () -> Ledger.parseLine("t,a,b,12.50,USD,DONE"));
    }

    private static void k1c() {
        section("K1C summaries");
        check("groups settled only, self transfer once, sorted", () -> {
            Ledger ledger = new Ledger();
            ledger.add(settled("a", "b", 1000));
            ledger.add(settled("b", "c", 2500));
            ledger.add(transfer("a", "b", 9999, "FAILED"));
            ledger.add(transfer("c", "a", 100, "PENDING"));
            ledger.add(settled("a", "a", 400));
            List<AccountSummary> got = ledger.settledSummaries();
            List<AccountSummary> want = List.of(
                    new AccountSummary("a", 400, 1400, -1000, 2),
                    new AccountSummary("b", 1000, 2500, -1500, 2),
                    new AccountSummary("c", 2500, 0, 2500, 1)
            );
            if (!want.equals(got)) throw new AssertionError("want " + want + " got " + got);
        });
        check("no settled transfers is an empty list", () -> {
            Ledger ledger = new Ledger();
            ledger.add(transfer("a", "b", 10, "FAILED"));
            List<AccountSummary> got = ledger.settledSummaries();
            if (got == null || !got.isEmpty()) throw new AssertionError("want empty, got " + got);
        });
    }

    private static void k2a() {
        section("K2A retry bug");
        check("only a failed attempt needs a retry", () -> {
            NotificationLog one = new NotificationLog();
            one.add(delivery("u", "WELCOME", "FAILED", 1, "2024-03-01T10:00:00Z"));
            eqList(List.of("u"), one.usersNeedingRetry());
        });
        check("later delivery of the same template clears the retry", () -> {
            NotificationLog one = new NotificationLog();
            one.add(delivery("u", "WELCOME", "FAILED", 1, "2024-03-01T10:00:00Z"));
            one.add(delivery("u", "WELCOME", "DELIVERED", 2, "2024-03-01T10:05:00Z"));
            eqList(List.of(), one.usersNeedingRetry());
        });
        check("a failed template still counts when another template succeeded", () -> {
            NotificationLog one = new NotificationLog();
            one.add(delivery("cara", "WELCOME", "DELIVERED", 2, "2024-03-01T10:05:00Z"));
            one.add(delivery("cara", "ALERT", "FAILED", 1, "2024-03-01T09:00:00Z"));
            eqList(List.of("cara"), one.usersNeedingRetry());
        });
        check("later failure of the same template needs a retry", () -> {
            NotificationLog one = new NotificationLog();
            one.add(delivery("u", "PAY", "DELIVERED", 1, "2024-03-01T10:00:00Z"));
            one.add(delivery("u", "PAY", "FAILED", 2, "2024-03-01T11:00:00Z"));
            eqList(List.of("u"), one.usersNeedingRetry());
        });
        check("same attempt, later timestamp wins", () -> {
            NotificationLog one = new NotificationLog();
            one.add(delivery("u", "PAY", "FAILED", 2, "2024-03-01T10:00:00Z"));
            one.add(delivery("u", "PAY", "DELIVERED", 2, "2024-03-01T10:00:01Z"));
            eqList(List.of(), one.usersNeedingRetry());
        });
        check("skipped latest is not a retry", () -> {
            NotificationLog one = new NotificationLog();
            one.add(delivery("u", "PAY", "SKIPPED", 1, "2024-03-01T10:00:00Z"));
            eqList(List.of(), one.usersNeedingRetry());
        });
        check("users are unique and sorted", () -> {
            NotificationLog one = new NotificationLog();
            one.add(delivery("bob", "PAY", "FAILED", 1, "2024-03-01T10:00:00Z"));
            one.add(delivery("ann", "PAY", "FAILED", 1, "2024-03-01T10:00:00Z"));
            one.add(delivery("ann", "OTHER", "FAILED", 2, "2024-03-01T11:00:00Z"));
            eqList(List.of("ann", "bob"), one.usersNeedingRetry());
        });
    }

    private static void k2b() {
        section("K2B parse");
        check("happy path", () -> {
            Delivery d = NotificationLog.parseLine(
                    " 2024-03-01T10:00:00Z | u1 | EMAIL | WELCOME | FAILED | 2 ");
            if (!"2024-03-01T10:00:00Z".equals(d.timestamp) || !"u1".equals(d.userId)) {
                throw new AssertionError("timestamp/user " + d.timestamp + " " + d.userId);
            }
            if (!"EMAIL".equals(d.channel) || !"WELCOME".equals(d.template) || !"FAILED".equals(d.status)) {
                throw new AssertionError("channel/template/status");
            }
            eq(2, d.attempt);
        });
        expectThrow("too few columns", () -> NotificationLog.parseLine("a|b|c|d|FAILED"));
        expectThrow("blank", () -> NotificationLog.parseLine(""));
        expectThrow("null", () -> NotificationLog.parseLine(null));
        expectThrow("attempt zero", () -> NotificationLog.parseLine("t|u|EMAIL|W|FAILED|0"));
        expectThrow("attempt not a number", () -> NotificationLog.parseLine("t|u|EMAIL|W|FAILED|x"));
        expectThrow("bad status", () -> NotificationLog.parseLine("t|u|EMAIL|W|BOUNCED|1"));
    }

    private static void k2c() {
        section("K2C delivery rate");
        check("rates ignore skipped and keep a zero", () -> {
            NotificationLog log = new NotificationLog();
            log.add(delivery("u", "A", "DELIVERED", 1, "2024-03-01T10:00:00Z", "EMAIL"));
            log.add(delivery("u", "A", "DELIVERED", 2, "2024-03-01T10:00:01Z", "EMAIL"));
            log.add(delivery("u", "A", "DELIVERED", 3, "2024-03-01T10:00:02Z", "EMAIL"));
            log.add(delivery("u", "A", "FAILED", 1, "2024-03-01T10:00:03Z", "EMAIL"));
            log.add(delivery("u", "A", "FAILED", 1, "2024-03-01T10:00:04Z", "SMS"));
            log.add(delivery("u", "A", "FAILED", 2, "2024-03-01T10:00:05Z", "SMS"));
            log.add(delivery("u", "A", "SKIPPED", 1, "2024-03-01T10:00:06Z", "PUSH"));
            log.add(delivery("u", "A", "DELIVERED", 1, "2024-03-01T10:00:07Z", "PUSH"));
            log.add(delivery("u", "A", "SKIPPED", 2, "2024-03-01T10:00:08Z", "PUSH"));
            Map<String, Double> got = log.deliveryRateByChannel();
            eqRate("EMAIL", 0.75, got);
            eqRate("SMS", 0.0, got);
            eqRate("PUSH", 1.0, got);
            if (got.size() != 3) throw new AssertionError("want 3 channels, got " + got);
        });
        check("channel with only skipped is absent", () -> {
            NotificationLog log = new NotificationLog();
            log.add(delivery("u", "A", "SKIPPED", 1, "2024-03-01T10:00:00Z", "PUSH"));
            Map<String, Double> got = log.deliveryRateByChannel();
            if (got == null || got.containsKey("PUSH")) throw new AssertionError("want no PUSH, got " + got);
        });
    }

    private static void k3a() {
        section("K3A server error");
        AccessLog log = new AccessLog();
        check("500 is a server error", () -> eq(true, log.isServerError(hit(500, 10))));
        check("599 is a server error", () -> eq(true, log.isServerError(hit(599, 10))));
        check("499 is not", () -> eq(false, log.isServerError(hit(499, 10))));
        check("600 is not", () -> eq(false, log.isServerError(hit(600, 10))));
        check("200 is not", () -> eq(false, log.isServerError(hit(200, 10))));
        check("404 is not", () -> eq(false, log.isServerError(hit(404, 10))));
    }

    private static void k3b() {
        section("K3B parse");
        check("happy path", () -> {
            Hit hit = AccessLog.parseLine(" payments | /v1/transfers | 201 | 38 ");
            if (!"payments".equals(hit.service) || !"/v1/transfers".equals(hit.route)) {
                throw new AssertionError("service/route " + hit.service + " " + hit.route);
            }
            eq(201, hit.status);
            eq(38, hit.latencyMs);
        });
        expectThrow("too few columns", () -> AccessLog.parseLine("payments|/v1|201"));
        expectThrow("blank", () -> AccessLog.parseLine(" "));
        expectThrow("null", () -> AccessLog.parseLine(null));
        expectThrow("status 99", () -> AccessLog.parseLine("payments|/v1|99|10"));
        expectThrow("status 600", () -> AccessLog.parseLine("payments|/v1|600|10"));
        expectThrow("negative latency", () -> AccessLog.parseLine("payments|/v1|200|-1"));
        check("zero latency and status 100 are allowed", () -> {
            Hit hit = AccessLog.parseLine("payments|/v1|100|0");
            eq(100, hit.status);
            eq(0, hit.latencyMs);
        });
    }

    private static void k3c() {
        section("K3C hotspots");
        check("threshold, rate, average, sort", () -> {
            AccessLog log = new AccessLog();
            log.add(new Hit("ledger", "/x", 500, 10));
            log.add(new Hit("ledger", "/x", 500, 30));
            log.add(new Hit("payments", "/v1/a", 500, 10));
            log.add(new Hit("payments", "/v1/a", 200, 30));
            log.add(new Hit("payments", "/v1/b", 500, 5));
            log.add(new Hit("payments", "/v1/b", 200, 15));
            log.add(new Hit("quiet", "/health", 500, 1));
            List<RouteReport> got = log.hotspots(2, 0.5);
            if (got.size() != 3) throw new AssertionError("want 3 rows, got " + got);
            eqReport(got.get(0), "ledger /x", 2, 1.0, 20.0);
            eqReport(got.get(1), "payments /v1/a", 2, 0.5, 20.0);
            eqReport(got.get(2), "payments /v1/b", 2, 0.5, 10.0);
        });
        check("status 500 counts toward the rate", () -> {
            AccessLog log = new AccessLog();
            log.add(new Hit("payments", "/v1", 500, 8));
            List<RouteReport> got = log.hotspots(1, 1.0);
            if (got.size() != 1) throw new AssertionError("want the 500 included, got " + got);
            eqReport(got.get(0), "payments /v1", 1, 1.0, 8.0);
        });
    }

    private static Transfer settled(String from, String to, long cents) {
        return transfer(from, to, cents, "SETTLED");
    }

    private static Transfer transfer(String from, String to, long cents, String status) {
        return new Transfer("id-" + from + "-" + to + "-" + status, from, to, cents, "PLN", status);
    }

    private static Delivery delivery(String user, String template, String status, int attempt, String timestamp) {
        return delivery(user, template, status, attempt, timestamp, "EMAIL");
    }

    private static Delivery delivery(String user, String template, String status, int attempt,
                                    String timestamp, String channel) {
        return new Delivery(timestamp, user, channel, template, status, attempt);
    }

    private static Hit hit(int status, int latency) {
        return new Hit("svc", "/r", status, latency);
    }

    private static void section(String name) {
        System.out.println("\n== " + name);
    }

    private static void expectLong(String name, long want, LongSupplier got) {
        check(name, () -> eq(want, got.get()));
    }

    private static void expectThrow(String name, Runnable runnable) {
        check(name, () -> {
            try {
                runnable.run();
            } catch (IllegalArgumentException expected) {
                return;
            }
            throw new AssertionError("expected IllegalArgumentException");
        });
    }

    private static void check(String name, Runnable runnable) {
        try {
            runnable.run();
            passed++;
            System.out.println("  OK  " + name);
        } catch (UnsupportedOperationException e) {
            skipped++;
            System.out.println("  --  " + name + " (not implemented)");
        } catch (Throwable e) {
            failed++;
            System.out.println("  FAIL " + name + " — " + e.getMessage());
        }
    }

    private static void eq(long want, long got) {
        if (want != got) throw new AssertionError("want " + want + " got " + got);
    }

    private static void eq(boolean want, boolean got) {
        if (want != got) throw new AssertionError("want " + want + " got " + got);
    }

    private static void eqList(List<String> want, List<String> got) {
        if (!want.equals(got)) throw new AssertionError("want " + want + " got " + got);
    }

    private static void eqRate(String channel, double want, Map<String, Double> got) {
        if (got == null || !got.containsKey(channel)) throw new AssertionError("missing " + channel + " in " + got);
        double value = got.get(channel);
        if (Math.abs(value - want) > 1e-9) throw new AssertionError(channel + " want " + want + " got " + value);
    }

    private static void eqReport(RouteReport got, String key, int hits, double rate, double avg) {
        if (!key.equals(got.routeKey) || got.hits != hits) {
            throw new AssertionError("want " + key + " hits " + hits + ", got " + got);
        }
        if (Math.abs(got.serverErrorRate - rate) > 1e-9 || Math.abs(got.avgLatencyMs - avg) > 1e-9) {
            throw new AssertionError("want rate " + rate + " avg " + avg + ", got " + got);
        }
    }

    @FunctionalInterface
    private interface LongSupplier {
        long get();
    }
}
