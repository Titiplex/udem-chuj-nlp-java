package org.titiplex.model;

import java.util.List;

public record AlignedToken(
        String chujSurface,
        String glossSurface,
        List<String> chujSegments,
        List<String> glossSegments
) {
    public static AlignedToken of(String chujSurface, String glossSurface, List<String> chujSegments, List<String> glossSegments) {
        return new AlignedToken(
                chujSurface == null ? "" : chujSurface,
                glossSurface == null ? "" : glossSurface,
                List.copyOf(chujSegments),
                List.copyOf(glossSegments)
        );
    }

    public String surface() {
        return chujSurface;
    }

    public List<String> surfaceSegments() {
        return chujSegments;
    }
}
