package web;

import java.util.List;
import java.util.Map;

/**
 * A minimal, dependency-free JSON reader/writer.
 * Kept intentionally small: the API only exchanges flat objects and
 * simple arrays of flat objects, so a full JSON library is unnecessary.
 */
public class JsonUtil {

    // ---------- Writing ----------

    public static String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        write(obj, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append('"').append(escape((String) obj)).append('"');
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
        } else if (obj instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey().toString())).append("\":");
                write(e.getValue(), sb);
            }
            sb.append('}');
        } else if (obj instanceof List) {
            sb.append('[');
            boolean first = true;
            for (Object item : (List<?>) obj) {
                if (!first) sb.append(',');
                first = false;
                write(item, sb);
            }
            sb.append(']');
        } else {
            // Fallback: treat as string (e.g. enums, LocalDate)
            sb.append('"').append(escape(obj.toString())).append('"');
        }
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    // ---------- Reading (flat objects only) ----------

    /**
     * Parses a flat JSON object into a Map<String,String>.
     * Sufficient for this project's request bodies (login, register,
     * add/update book, issue/return), which contain only string/number
     * values with no nesting.
     */
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> result = new java.util.LinkedHashMap<>();
        if (json == null) return result;
        String trimmed = json.trim();
        if (trimmed.isEmpty()) return result;
        if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);

        int i = 0;
        int len = trimmed.length();
        while (i < len) {
            i = skipWhitespace(trimmed, i);
            if (i >= len) break;
            if (trimmed.charAt(i) == ',') { i++; continue; }

            // parse key
            if (trimmed.charAt(i) != '"') break;
            int[] keyEnd = new int[1];
            String key = parseString(trimmed, i, keyEnd);
            i = keyEnd[0];
            i = skipWhitespace(trimmed, i);
            if (i >= len || trimmed.charAt(i) != ':') break;
            i++; // skip colon
            i = skipWhitespace(trimmed, i);

            // parse value
            String value;
            if (i < len && trimmed.charAt(i) == '"') {
                int[] valEnd = new int[1];
                value = parseString(trimmed, i, valEnd);
                i = valEnd[0];
            } else {
                int start = i;
                while (i < len && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') i++;
                value = trimmed.substring(start, i).trim();
            }
            result.put(key, value);
            i = skipWhitespace(trimmed, i);
            if (i < len && trimmed.charAt(i) == ',') i++;
        }
        return result;
    }

    private static int skipWhitespace(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    private static String parseString(String s, int start, int[] endOut) {
        // s.charAt(start) == '"'
        StringBuilder sb = new StringBuilder();
        int i = start + 1;
        while (i < s.length() && s.charAt(i) != '"') {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append(next);
                }
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        endOut[0] = i + 1; // skip closing quote
        return sb.toString();
    }
}
