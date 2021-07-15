package com.hunsmore;

/**
 * @author htf
 */
public class SimplestKeyValueServiceImpl implements KeyValueService{
    @Override
    public byte[] getBytes(String key) {
        return new byte[0];
    }

    @Override
    public int setBytes(String key, byte[] bytes) {
        return 0;
    }
}
