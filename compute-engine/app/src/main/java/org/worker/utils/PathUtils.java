package org.worker.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class PathUtils {
    private PathUtils() {}

    public static String decodePathParam(String raw) {
        if (raw == null) return "";
        try {
            return URLDecoder.decode(raw, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return raw;
        }
    }
}
