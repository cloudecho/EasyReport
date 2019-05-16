package com.easytoolsoft.easyreport.common.crypto;

public enum KeySize {
    SIZE_128_BIT(128), SIZE_256_BIT(256);

    final int size;

    KeySize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
