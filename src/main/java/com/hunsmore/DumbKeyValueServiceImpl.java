package com.hunsmore;

/**
 * @author htf
 */
public class DumbKeyValueServiceImpl implements KeyValueService {
    @Override
    public byte[] getBytes(String key) {
        return "hello".getBytes();
    }

    @Override
    public int setBytes(String key, byte[] bytes) {
        return -1;
    }
}
