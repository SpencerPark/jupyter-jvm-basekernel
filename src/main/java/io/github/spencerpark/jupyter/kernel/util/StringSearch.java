package io.github.spencerpark.jupyter.kernel.util;

public class StringSearch {
    public static int[] findLongestMatchingAt(String code, int at, CharPredicate test) {
        if (test == null || at < 0 || at > code.length())
            return null;

        int start, end;
        if (at < code.length() && test.test(code.charAt(at))) {
            //The code[at] is a valid char and so worst case start = end is the entire string
            start = end = at;
        } else if (at > 0 && test.test(code.charAt(at - 1))) {
            //The code[at] isn't valid but the previous one is a good starting point
            //which may happen if "at" is immediately following a word
            start = end = at - 1;
        } else {
            return null;
        }

        while (start > 0 && test.test(code.charAt(start - 1)))
            start--;
        while (end < code.length() - 1 && test.test(code.charAt(end + 1)))
            end++;

        return new int[]{ start, end + 1};
    }
}
