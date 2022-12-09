package org.example.api.utils;

import org.example.api.APIController;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PemUtils {
    public static void run() {
        File file = new File("SignFiles");
        PemUtils.pemList.clear();
        PemUtils.getAllPem(file);
        for (File listFile : PemUtils.pemList) {
            try {
                X509Certificate certObject = PemUtils.getCertObject(listFile.getAbsolutePath());
                String print = PemUtils.getThumbprint(certObject);
                System.out.println(listFile.getParent() + " [" + print + "]");
                APIController.map.put(print, listFile.getParent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Set<File> pemList = new HashSet<>();

    public static void getAllPem(File file) {
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if (listFile.isDirectory()) getAllPem(listFile);
            else if (listFile.getName().contains(".pem")) pemList.add(listFile.getAbsoluteFile());
        }
    }

    public static X509Certificate getCertObject(String filePath) throws IOException, CertificateException {
        try (FileInputStream is = new FileInputStream(filePath)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(is);
        }
    }

    /**
     * 用下面getThumbprintSHA1方法替代
     *
     * @param cert
     * @return
     * @throws NoSuchAlgorithmException
     * @throws CertificateEncodingException
     */
    @Deprecated
    public static String getThumbprint(X509Certificate cert) throws
            NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(cert.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    public static String getThumbprintSHA1(X509Certificate cert) throws
            NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(cert.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    public static String getThumbprintMD5(X509Certificate cert) throws
            NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(cert.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    public static String getThumbprintSHA256(X509Certificate cert) throws
            NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(cert.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }
}