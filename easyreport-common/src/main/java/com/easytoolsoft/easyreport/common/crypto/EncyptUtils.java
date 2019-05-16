package com.easytoolsoft.easyreport.common.crypto;

import com.google.common.base.Strings;

/**
 * Created by yong.ma on 2019/5/16.
 */
public class EncyptUtils {
    private static final AesBytesEncryptor ENCRYPTOR;

    static {
        String passwd = System.getenv("AES_PASSWORD");
        if (Strings.isNullOrEmpty(passwd)) {
            passwd = "3adc394bd3fa26c1d541f9a703f9ef93";
        }
        ENCRYPTOR = new AesBytesEncryptor(passwd, "30912c9f6a1c17555b321d3d33197f9a");
    }

    public static byte[] encrypt(byte[] bytes) {return ENCRYPTOR.encrypt(bytes);}

    public static byte[] decrypt(byte[] encryptedBytes) {return ENCRYPTOR.decrypt(encryptedBytes);}
}
