package org.titiplex.desktop.model;

import java.util.List;

public record ValidationResult(boolean ok, List<String> messages) {
}
