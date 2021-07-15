package com.hunsmore;

/**
 * @author htf
 */
public interface KeyValueService {
    /**
     *  Get bytes from kv storage by key. If key does not exist, return null.
     */
    byte[] getBytes(String key);

    /**
     *  Set bytes to kv storage using key. Delete a key by setting null.
     */
    int setBytes(String key, byte[] bytes);
}
