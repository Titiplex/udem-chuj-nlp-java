package org.titiplex.rules;

import org.titiplex.model.AlignedToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class RuleSelector {
    private final boolean onGloss;
    private final Pattern regex;
    private final Set<String> inList;
    private final List<String> beforeValues;
    private final boolean ignoreCase;

    public RuleSelector(boolean onGloss, Pattern regex, Set<String> inList, List<String> beforeValues, boolean ignoreCase) {
        this.onGloss = onGloss;
        this.regex = regex;
        this.inList = inList == null ? Set.of() : Set.copyOf(inList);
        this.beforeValues = beforeValues == null ? List.of() : List.copyOf(beforeValues);
        this.ignoreCase = ignoreCase;
    }

    public List<Integer> select(List<AlignedToken> tokens) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) if (matches(tokens.get(i))) out.add(i);
        return out;
    }

    public boolean matches(AlignedToken token) {
        List<String> parts = onGloss ? token.glossSegments() : token.chujSegments();
        String surface = onGloss ? token.glossSurface() : token.chujSurface();
        for (String part : parts) {
            for (String before : beforeValues) if (eq(part, before)) return true;
            for (String item : inList) if (eq(part, item)) return true;
        }
        if (surface != null) {
            for (String before : beforeValues) if (eq(surface, before)) return true;
            for (String item : inList) if (eq(surface, item)) return true;
        }
        if (regex != null) {
            if (regex.matcher(String.join("-", parts)).find()) return true;
            if (surface != null && regex.matcher(surface).find()) return true;
        }
        return beforeValues.isEmpty() && inList.isEmpty() && regex == null;
    }

    private boolean eq(String a, String b) {
        if (ignoreCase) return norm(a).equals(norm(b));
        return a != null && a.equals(b);
    }

    private String norm(String v) {
        return v == null ? "" : v.toLowerCase(Locale.ROOT);
    }
}
