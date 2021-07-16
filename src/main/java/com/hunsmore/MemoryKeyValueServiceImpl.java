package com.hunsmore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author htf
 */
public class MemoryKeyValueServiceImpl implements KeyValueService {
    private final Map<String, byte[]> map = new HashMap<>();

    @Override
    public byte[] getBytes(String key) {
        return map.getOrDefault(key, null);
    }

    @Override
    public int setBytes(String key, byte[] bytes) {
        map.put(key, bytes);
        return 1;
    }
}
