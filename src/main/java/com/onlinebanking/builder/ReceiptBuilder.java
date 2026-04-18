package com.onlinebanking.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder Pattern: creates formatted receipt text blocks incrementally.
 */
public class ReceiptBuilder {
    private final List<String> lines = new ArrayList<>();

    public ReceiptBuilder separator() {
        lines.add("========================================");
        return this;
    }

    public ReceiptBuilder dashedSeparator() {
        lines.add("----------------------------------------");
        return this;
    }

    public ReceiptBuilder line(String text) {
        lines.add(text == null ? "" : text);
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
