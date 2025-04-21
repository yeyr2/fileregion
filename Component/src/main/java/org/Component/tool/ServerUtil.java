package org.Component.tool;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.io.File;
import java.security.cert.CertificateException;

/**
 * Some useful methods for server side.
 */
public final class ServerUtil {

    private static final boolean SSL = System.getProperty("ssl") != null;

    // 加载证书和私钥
    String certFilePath = "path/to/certificate.crt";
    String privateKeyFilePath = "path/to/private_key.pem";
    String privateKeyPassword = "private_key_password";

    public ServerUtil() {
    }

    public static SslContext buildSslContext() throws CertificateException, SSLException {
        if (!SSL) {
            return null;
        }
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder
                .forServer(ssc.certificate(), ssc.privateKey())
                .build();
    }

    public SslContext createSslContext() throws SSLException, CertificateException {
        if (!SSL) {
            return null;
        }
        // 创建SSL上下文
        return SslContextBuilder.forClient()
                .trustManager(new File(certFilePath))
                .keyManager(new File(certFilePath), new File(privateKeyFilePath), privateKeyPassword)
                .build();
    }
}