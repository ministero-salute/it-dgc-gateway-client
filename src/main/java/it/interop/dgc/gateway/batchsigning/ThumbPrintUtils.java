package it.interop.dgc.gateway.batchsigning;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.cert.X509CertificateHolder;
 
/**
 * A tool class for obtaining certificate thumbnails.
 * 
 * @author wiflish
 * 
 */
public class ThumbPrintUtils {

	public static String getThumbprint(X509CertificateHolder cert, String thumAlg, String delimiter) {
        if (cert == null) {
            return null;
        }
 
        if (thumAlg == null || thumAlg.length() == 0) {
            return null;
        }
 
        String thumbPrint = "";
        try {
            MessageDigest md = MessageDigest.getInstance(thumAlg);
            byte rawDigest[] = md.digest(cert.getEncoded());
            thumbPrint = getHex(rawDigest, delimiter);
        } catch (NoSuchAlgorithmException e) {
            thumbPrint = "";
			e.printStackTrace();
        } catch (IOException e) {
            thumbPrint = "";
			e.printStackTrace();
		}
 
        return thumbPrint;
    }
 
    /**
           * Obtain a certificate thumbnail. By default, the sha1 algorithm is used. The default thumbnail string is not separated.
     * 
           * @param signerCert certificate
     * @return
     */
    public static String getThumbprint(X509CertificateHolder signerCert) {
        return getThumbprint(signerCert, "sha1", null);
    }
 
    /**
           * Get a certificate thumbnail. The sha1 algorithm is used by default, separated by the specified separator.
     * 
           * @param cert certificate.
           * @param delimiter specifies the separator, such as: ":".
     * @return
     */
    public static String getThumbprint(X509CertificateHolder cert, String delimiter) {
        return getThumbprint(cert, "sha1", delimiter);
    }
 
    /**
           * The certificate digest will be converted to a hexadecimal string, which will result in a certificate thumbnail.
     * 
     * @param buf
     * @param delimiter
     * @return
     */
    private static String getHex(byte buf[], String delimiter) {
        String result = "";
 
        if (buf == null) {
            return "";
        }
 
        String defaultDelimiter = "";
        if (delimiter != null && delimiter.length() > 0) {
            defaultDelimiter = delimiter;
        }
 
        for (int i = 0; i < buf.length; i++) {
            if (i > 0) {
                result += defaultDelimiter;
            }
 
            short sValue = buf[i];
            int iValue = 0;
            iValue += sValue;
            String converted = Integer.toHexString(iValue);
 
            if (converted.length() > 2) {
                converted = converted.substring(converted.length() - 2);
            }
                         // When there is only 1 digit, the front is padded with 0.
            else if (converted.length() < 2) {
                converted = ("0" + converted);
            }
 
                         // Convert the thumbnails to uppercase letters.
            result += converted.toUpperCase();
        }
        return result;
    }
}