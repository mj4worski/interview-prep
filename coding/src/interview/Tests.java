package interview;

import interview.p01.LongestSubstring;
import interview.p02.TwoSum;
import interview.p03.DailyTemperatures;
import interview.p04.NumberOfIslands;
import interview.p05.CourseSchedule;
import interview.p06.LruCache;
import interview.p07.MergeIntervals;

import java.util.Arrays;
import java.util.function.IntSupplier;

public class Tests {
    private static int passed;
    private static int failed;
    private static int skipped;

    public static void main(String[] args) {
        String which = args.length == 0 ? "ALL" : args[0].toUpperCase();
        if (which.equals("ALL") || which.equals("P01")) p01();
        if (which.equals("ALL") || which.equals("P02")) p02();
        if (which.equals("ALL") || which.equals("P03")) p03();
        if (which.equals("ALL") || which.equals("P04")) p04();
        if (which.equals("ALL") || which.equals("P05")) p05();
        if (which.equals("ALL") || which.equals("P06")) p06();
        if (which.equals("ALL") || which.equals("P07")) p07();

        System.out.printf("%n%d passed, %d failed, %d not implemented%n", passed, failed, skipped);
        if (failed > 0) System.exit(1);
    }

    private static void p01() {
        section("P01 LongestSubstring");
        var s = new LongestSubstring();
        expect("abcabcbb", 3, () -> s.lengthOfLongestSubstring("abcabcbb"));
        expect("bbbbb", 1, () -> s.lengthOfLongestSubstring("bbbbb"));
        expect("pwwkew", 3, () -> s.lengthOfLongestSubstring("pwwkew"));
        expect("empty", 0, () -> s.lengthOfLongestSubstring(""));
        expect("abba", 2, () -> s.lengthOfLongestSubstring("abba"));
        expect("dvdf", 3, () -> s.lengthOfLongestSubstring("dvdf"));
        expect("single", 1, () -> s.lengthOfLongestSubstring("x"));
    }

    private static void p02() {
        section("P02 TwoSum");
        var s = new TwoSum();
        check("classic", () -> {
            int[] a = s.twoSum(new int[]{2, 7, 11, 15}, 9);
            assertPair(a, 0, 1);
        });
        check("negatives", () -> {
            int[] a = s.twoSum(new int[]{-3, 4, 3, 90}, 0);
            assertPair(a, 0, 2);
        });
        check("duplicate values", () -> {
            int[] a = s.twoSum(new int[]{3, 3}, 6);
            assertPair(a, 0, 1);
        });
        check("last two", () -> {
            int[] a = s.twoSum(new int[]{1, 2, 3, 7, 8}, 15);
            assertPair(a, 3, 4);
        });
    }

    private static void p03() {
        section("P03 DailyTemperatures");
        var s = new DailyTemperatures();
        check("example", () -> assertArray(
                new int[]{1, 1, 4, 2, 1, 1, 0, 0},
                s.dailyTemperatures(new int[]{73, 74, 75, 71, 69, 72, 76, 73})));
        check("strictly decreasing", () -> assertArray(
                new int[]{0, 0, 0},
                s.dailyTemperatures(new int[]{90, 80, 70})));
        check("single", () -> assertArray(
                new int[]{0},
                s.dailyTemperatures(new int[]{50})));
    }

    private static void p04() {
        section("P04 NumberOfIslands");
        var s = new NumberOfIslands();
        expect("three islands", 3, () -> s.numIslands(copy(new char[][]{
                {'1', '1', '0', '0', '0'},
                {'1', '1', '0', '0', '0'},
                {'0', '0', '1', '0', '0'},
                {'0', '0', '0', '1', '1'}
        })));
        expect("one", 1, () -> s.numIslands(copy(new char[][]{
                {'1', '1', '1'},
                {'0', '1', '0'},
                {'1', '1', '1'}
        })));
        expect("water", 0, () -> s.numIslands(copy(new char[][]{{'0', '0'}, {'0', '0'}})));
    }

    private static void p05() {
        section("P05 CourseSchedule");
        var s = new CourseSchedule();
        expect("linear", 1, () -> s.canFinish(2, new int[][]{{1, 0}}) ? 1 : 0);
        expect("cycle", 0, () -> s.canFinish(2, new int[][]{{1, 0}, {0, 1}}) ? 1 : 0);
        expect("no edges", 1, () -> s.canFinish(3, new int[][]{}) ? 1 : 0);
        expect("diamond", 1, () -> s.canFinish(4, new int[][]{{1, 0}, {2, 0}, {3, 1}, {3, 2}}) ? 1 : 0);
    }

    private static void p06() {
        section("P06 LruCache");
        check("example", () -> {
            var c = new LruCache(2);
            c.put(1, 1);
            c.put(2, 2);
            assertEq(1, c.get(1));
            c.put(3, 3);
            assertEq(-1, c.get(2));
            c.put(4, 4);
            assertEq(-1, c.get(1));
            assertEq(3, c.get(3));
            assertEq(4, c.get(4));
        });
        check("update existing", () -> {
            var c = new LruCache(2);
            c.put(1, 1);
            c.put(2, 2);
            c.put(1, 10);
            assertEq(10, c.get(1));
            c.put(3, 3);
            assertEq(-1, c.get(2));
        });
    }

    private static void p07() {
        section("P07 MergeIntervals");
        var s = new MergeIntervals();
        check("example", () -> assertIntervals(
                new int[][]{{1, 6}, {8, 10}, {15, 18}},
                s.merge(new int[][]{{1, 3}, {2, 6}, {8, 10}, {15, 18}})));
        check("touching", () -> assertIntervals(
                new int[][]{{1, 4}},
                s.merge(new int[][]{{1, 4}, {4, 5}})));
        check("contained", () -> assertIntervals(
                new int[][]{{1, 10}},
                s.merge(new int[][]{{1, 10}, {2, 3}, {4, 8}})));
    }

    private static void section(String name) {
        System.out.println("\n== " + name);
    }

    private static void expect(String name, int want, IntSupplier got) {
        check(name, () -> assertEq(want, got.getAsInt()));
    }

    private static void check(String name, Runnable r) {
        try {
            r.run();
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

    private static void assertEq(int want, int got) {
        if (want != got) throw new AssertionError("want " + want + " got " + got);
    }

    private static void assertPair(int[] a, int i, int j) {
        if (a == null || a.length != 2) throw new AssertionError("want 2 indices, got " + Arrays.toString(a));
        boolean ok = (a[0] == i && a[1] == j) || (a[0] == j && a[1] == i);
        if (!ok) throw new AssertionError("want {" + i + "," + j + "} got " + Arrays.toString(a));
    }

    private static void assertArray(int[] want, int[] got) {
        if (!Arrays.equals(want, got)) {
            throw new AssertionError("want " + Arrays.toString(want) + " got " + Arrays.toString(got));
        }
    }

    private static void assertIntervals(int[][] want, int[][] got) {
        if (got == null || want.length != got.length) {
            throw new AssertionError("want " + Arrays.deepToString(want) + " got " + Arrays.deepToString(got));
        }
        for (int i = 0; i < want.length; i++) {
            if (!Arrays.equals(want[i], got[i])) {
                throw new AssertionError("want " + Arrays.deepToString(want) + " got " + Arrays.deepToString(got));
            }
        }
    }

    private static char[][] copy(char[][] g) {
        char[][] c = new char[g.length][];
        for (int i = 0; i < g.length; i++) c[i] = Arrays.copyOf(g[i], g[i].length);
        return c;
    }
}
