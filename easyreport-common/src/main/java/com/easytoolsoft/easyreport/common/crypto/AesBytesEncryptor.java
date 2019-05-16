/*
 * Copyright 2011-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.easytoolsoft.easyreport.common.crypto;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;

import static com.easytoolsoft.easyreport.common.crypto.CipherUtils.doFinal;
import static com.easytoolsoft.easyreport.common.crypto.CipherUtils.initCipher;
import static com.easytoolsoft.easyreport.common.crypto.CipherUtils.newCipher;
import static com.easytoolsoft.easyreport.common.crypto.CipherUtils.newSecretKey;
import static org.springframework.security.crypto.util.EncodingUtils.concatenate;
import static org.springframework.security.crypto.util.EncodingUtils.subArray;

/**
 * Encryptor that uses 128/256-bit AES encryption. <br>
 *
 * NOTE: <br>
 * https://github.com/spring-projects/spring-security/issues/2917 <br>
 * So I add the parameter of KeySize into the constructor.
 *
 * @author Keith Donald
 * @author Dave Syer
 */
public final class AesBytesEncryptor implements BytesEncryptor {

    private final SecretKey secretKey;

    private final Cipher encryptor;

    private final Cipher decryptor;

    private final BytesKeyGenerator ivGenerator;

    private CipherAlgorithm alg;

    private static final String AES_CBC_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";

    public enum CipherAlgorithm {

        CBC(AES_CBC_ALGORITHM, NULL_IV_GENERATOR), GCM(AES_GCM_ALGORITHM, KeyGenerators
            .secureRandom(16));

        private BytesKeyGenerator ivGenerator;
        private String name;

        private CipherAlgorithm(String name, BytesKeyGenerator ivGenerator) {
            this.name = name;
            this.ivGenerator = ivGenerator;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public AlgorithmParameterSpec getParameterSpec(byte[] iv) {
            return this == CBC ? new IvParameterSpec(iv) : new GCMParameterSpec(128, iv);
        }

        public Cipher createCipher() {
            return newCipher(this.toString());
        }

        public BytesKeyGenerator defaultIvGenerator() {
            return this.ivGenerator;
        }
    }

    public AesBytesEncryptor(String password, CharSequence salt) {
        this(password, salt, null);
    }

    public AesBytesEncryptor(String password, CharSequence salt,
        BytesKeyGenerator ivGenerator) {
        this(password, salt, ivGenerator, CipherAlgorithm.CBC, KeySize.SIZE_128_BIT);
    }

    public AesBytesEncryptor(String password, CharSequence salt,
        BytesKeyGenerator ivGenerator, CipherAlgorithm alg, KeySize keySize) {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), Hex.decode(salt),
            1024, keySize.getSize());
        SecretKey secretKey = newSecretKey("PBKDF2WithHmacSHA1", keySpec);
        this.secretKey = new SecretKeySpec(secretKey.getEncoded(), "AES");
        this.alg = alg;
        this.encryptor = alg.createCipher();
        this.decryptor = alg.createCipher();
        this.ivGenerator = ivGenerator != null ? ivGenerator : alg.defaultIvGenerator();
    }

    public byte[] encrypt(byte[] bytes) {
        synchronized (this.encryptor) {
            byte[] iv = this.ivGenerator.generateKey();
            initCipher(this.encryptor, Cipher.ENCRYPT_MODE, this.secretKey,
                this.alg.getParameterSpec(iv));
            byte[] encrypted = doFinal(this.encryptor, bytes);
            return this.ivGenerator != NULL_IV_GENERATOR ? concatenate(iv, encrypted)
                : encrypted;
        }
    }

    public byte[] decrypt(byte[] encryptedBytes) {
        synchronized (this.decryptor) {
            byte[] iv = iv(encryptedBytes);
            initCipher(this.decryptor, Cipher.DECRYPT_MODE, this.secretKey,
                this.alg.getParameterSpec(iv));
            return doFinal(
                this.decryptor,
                this.ivGenerator != NULL_IV_GENERATOR ? encrypted(encryptedBytes,
                    iv.length) : encryptedBytes);
        }
    }

    // internal helpers

    private byte[] iv(byte[] encrypted) {
        return this.ivGenerator != NULL_IV_GENERATOR ? subArray(encrypted, 0,
            this.ivGenerator.getKeyLength()) : NULL_IV_GENERATOR.generateKey();
    }

    private byte[] encrypted(byte[] encryptedBytes, int ivLength) {
        return subArray(encryptedBytes, ivLength, encryptedBytes.length);
    }

    private static final BytesKeyGenerator NULL_IV_GENERATOR = new BytesKeyGenerator() {

        private final byte[] VALUE = new byte[16];

        public int getKeyLength() {
            return this.VALUE.length;
        }

        public byte[] generateKey() {
            return this.VALUE;
        }

    };
}
