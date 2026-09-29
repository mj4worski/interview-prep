package interview.p05;

/**
 * P05 — Course Schedule
 *
 * numCourses labeled 0..n-1. prerequisites[i] = [a,b] means
 * you must take b before a. Return true if you can finish all courses.
 *
 * Example: n=2, [[1,0]] → true
 *          n=2, [[1,0],[0,1]] → false
 *
 * Constraints: 1 <= n <= 2000, 0 <= prerequisites.length <= 5000.
 * Target: O(n + e). This is cycle detection in a directed graph.
 */
public class CourseSchedule {

    public boolean canFinish(int numCourses, int[][] prerequisites) {
        throw new UnsupportedOperationException("implement me");
    }
}
