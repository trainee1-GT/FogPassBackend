package train.local.fogpass.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Computes field-level diff between two DTO objects (not entities). Masks configured fields.
 */
public final class ChangeLogUtil {
    private final ObjectMapper mapper;

    public ChangeLogUtil(ObjectMapper mapper) {
        this.mapper = mapper.copy().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Map<String, Object> diff(Object before, Object after, Set<String> maskedFields) {
        Map<String, Object> diff = new HashMap<>();
        Map<String, Object> beforeMap = mapper.convertValue(before, Map.class);
        Map<String, Object> afterMap = mapper.convertValue(after, Map.class);

        // Iterate keys from both maps
        for (String key : union(beforeMap, afterMap)) {
            Object b = beforeMap.get(key);
            Object a = afterMap.get(key);
            if (!Objects.equals(b, a)) {
                Map<String, Object> change = new HashMap<>();
                change.put("from", maskIfNeeded(key, b, maskedFields));
                change.put("to", maskIfNeeded(key, a, maskedFields));
                diff.put(key, change);
            }
        }
        return diff;
    }

    public String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    // Produce a masked representation of an object tree by field name
    public Object maskTree(Object obj, Set<String> maskedFields) {
        if (obj == null) return null;
        if (obj instanceof Map<?,?> map) {
            Map<String,Object> out = new HashMap<>();
            for (Map.Entry<?,?> e : map.entrySet()) {
                String k = String.valueOf(e.getKey());
                Object v = e.getValue();
                out.put(k, maskIfNeeded(k, maskTree(v, maskedFields), maskedFields));
            }
            return out;
        }
        if (obj instanceof Iterable<?> it) {
            java.util.List<Object> list = new java.util.ArrayList<>();
            for (Object o : it) list.add(maskTree(o, maskedFields));
            return list;
        }
        // Fallback: convert to map and recurse
        if (!(obj instanceof String) && !(obj instanceof Number) && !(obj instanceof Boolean)) {
            Map<String,Object> asMap = mapper.convertValue(obj, Map.class);
            return maskTree(asMap, maskedFields);
        }
        return obj;
    }

    private static Object maskIfNeeded(String field, Object value, Set<String> maskedFields) {
        if (value == null) return null;
        if (maskedFields != null && maskedFields.stream().anyMatch(f -> f.equalsIgnoreCase(field))) {
            if (value instanceof String s) {
                return s.isBlank() ? "" : "****";
            }
            return "****";
        }
        return value;
    }

    private static Set<String> union(Map<String, Object> a, Map<String, Object> b) {
        Set<String> s = new java.util.HashSet<>(a.keySet());
        s.addAll(b.keySet());
        return s;
    }
}