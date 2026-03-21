package org.titiplex.conllu;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TemplateResolver {
    private static final Pattern P = Pattern.compile("\\{([a-zA-Z0-9_.]+)}");

    private TemplateResolver() {
    }

    static String render(String value, Map<String, Object> ctx) {
        if (value == null) return "";
        Matcher m = P.matcher(value);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String repl = resolvePath(ctx, m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(repl));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    static String resolvePath(Map<String, Object> ctx, String dotted) {
        Object cur = ctx;
        for (String part : dotted.split("\\.")) {
            if (!(cur instanceof Map<?, ?> map) || !map.containsKey(part)) {
                return "";
            }
            cur = ((Map<String, Object>) map).get(part);
        }
        return cur == null ? "" : cur.toString();
    }
}
