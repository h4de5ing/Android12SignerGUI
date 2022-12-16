package org.example;

import org.example.api.utils.PemUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public class TestPemMain {

    public static void main(String[] args) throws CertificateException, IOException, NoSuchAlgorithmException {
        HashMap<String, String> map = new HashMap<>();
        File file = new File("SignFiles");
        PemUtils.getAllPem(file);
        for (File listFile : PemUtils.pemList) {
//            System.out.println(listFile.getParent());
            X509Certificate certObject = PemUtils.getCertObject(listFile.getAbsolutePath());
            String print = PemUtils.getThumbprintMD5(certObject);
            System.out.println(listFile.getParent() + " [" + print + "]");
            map.put(print, listFile.getParent());
        }
        System.out.println(map);
    }
}
