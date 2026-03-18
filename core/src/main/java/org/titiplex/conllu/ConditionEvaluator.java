package org.titiplex.conllu;

import java.util.Map;

final class ConditionEvaluator {
    private final String s;
    private final Map<String, Object> ctx;
    private int pos;

    private ConditionEvaluator(String s, Map<String, Object> ctx) {
        this.s = s == null ? "" : s;
        this.ctx = ctx;
    }

    static boolean evaluate(String expr, Map<String, Object> ctx) {
        if (expr == null || expr.isBlank()) return true;
        return new ConditionEvaluator(expr, ctx).parseOr();
    }

    private boolean parseOr() {
        boolean value = parseAnd();
        skipWs();
        while (peek('|')) {
            pos++;
            value = value || parseAnd();
            skipWs();
        }
        return value;
    }

    private boolean parseAnd() {
        boolean value = parseUnary();
        skipWs();
        while (peek('&')) {
            pos++;
            value = value && parseUnary();
            skipWs();
        }
        return value;
    }

    private boolean parseUnary() {
        skipWs();
        if (peek('!')) {
            pos++;
            return !parseUnary();
        }
        if (peek('(')) {
            pos++;
            boolean value = parseOr();
            skipWs();
            if (peek(')')) pos++;
            return value;
        }
        return parseAtom();
    }

    private boolean parseAtom() {
        skipWs();
        if (s.regionMatches(true, pos, "has(", 0, 4)) {
            pos += 4;
            int end = s.indexOf(')', pos);
            if (end < 0) return false;
            String key = s.substring(pos, end).trim();
            pos = end + 1;
            Object value = ctx.get(key);
            return value != null;
        }
        int start = pos;
        while (pos < s.length() && Character.isLetterOrDigit(s.charAt(pos))) pos++;
        if (start == pos) return false;
        Object value = ctx.get(s.substring(start, pos));
        return value != null;
    }

    private boolean peek(char c) {
        skipWs();
        return pos < s.length() && s.charAt(pos) == c;
    }

    private void skipWs() {
        while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
    }
}
