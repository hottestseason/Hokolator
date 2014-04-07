package com.hottestseason.hokolator;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private final Map<String, Object> values = new HashMap<>();

    public void set(String key, Object value) {
        values.put(key, value);
    }

    public Object get(String key) {
        return values.get(key);
    }
}
