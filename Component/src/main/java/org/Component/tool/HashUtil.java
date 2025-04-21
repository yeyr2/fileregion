package org.Component.tool;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

// 散列函数
public class HashUtil {
    public static final HashType MD5 = new HashType("MD5");
    public static final HashType SHA1 = new HashType("SHA-1");
    public static final HashType SHA256 = new HashType("SHA-256");
    public static final HashType SHA512 = new HashType("SHA-512");

    public static String getHash(File file, HashType hashType) throws Exception {
        String hashResult;
        try(FileInputStream fileInputStream = new FileInputStream(file)){
            byte[] buffer = new byte[1024];
            MessageDigest hash = MessageDigest.getInstance(hashType.type);
            int read;
            while ((read = fileInputStream.read(buffer)) > 0 ) {
                hash.update(buffer, 0, read);
            }
            hashResult = toHexString(hash.digest());
        }
        return hashResult;
    }

    private static String toHexString(byte[] hash) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hash) {
            stringBuilder.append(String.format("%02x", b & 0xFF));
        }
        return stringBuilder.toString();
    }

    private static class HashType{
        private final String type;
        private HashType(String type) {
            this.type = type;
        }
    }
}
