package pt.upa.ca.domain;

import pt.upa.ca.exception.CertificateReadException;
import pt.upa.shared.domain.CertificateHelper;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class CA {
    private final String BASE_KEY_DIR;

    public CA(String baseDir) {
        BASE_KEY_DIR = baseDir;
    }

    public CA() { this (CertificateHelper.DEFAULT_BASE_KEY_DIR); }

    public byte[] getCertificateByName(String name) throws CertificateReadException {
        String certPath;
        if (name == "ca"){
            certPath = "../keys/ca/ca-certificate.pem.txt";
        } else {
            certPath = BASE_KEY_DIR + name +
                    CertificateHelper.DIR_SEPARATOR + name + CertificateHelper.CERT_EXT;
        }
            return getCertificateByPath(certPath);

    }

    public byte[] getCertificateByPath(String path) throws CertificateReadException {
        return readCertificateFile(path);
    }
    private byte[] readCertificateFile(String certificatePath) throws CertificateReadException {
        try {
            return Files.readAllBytes(Paths.get(certificatePath));
        } catch (IOException e) {
            throw new CertificateReadException("Error reading " + certificatePath + " :" + e.getMessage());
        }
    }


}
