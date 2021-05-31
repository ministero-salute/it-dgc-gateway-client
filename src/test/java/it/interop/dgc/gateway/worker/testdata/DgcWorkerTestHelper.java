package it.interop.dgc.gateway.worker.testdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;

import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.enums.CertificateType;

public class DgcWorkerTestHelper {

	public final static String DSC_TO_UPLOAD = "MIIEHjCCAgagAwIBAgIUM5lJeGCHoRF1raR6cbZqDV4vPA8wDQYJKoZIhvcNAQELBQAwTjELMAkGA1UEBhMCSVQxHzAdBgNVBAoMFk1pbmlzdGVybyBkZWxsYSBTYWx1dGUxHjAcBgNVBAMMFUl0YWx5IERHQyBDU0NBIFRFU1QgMTAeFw0yMTA1MDcxNzAyMTZaFw0yMzA1MDgxNzAyMTZaME0xCzAJBgNVBAYTAklUMR8wHQYDVQQKDBZNaW5pc3Rlcm8gZGVsbGEgU2FsdXRlMR0wGwYDVQQDDBRJdGFseSBER0MgRFNDIFRFU1QgMTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABDSp7t86JxAmjZFobmmu0wkii53snRuwqVWe3/g/wVz9i306XA5iXpHkRPZVUkSZmYhutMDrheg6sfwMRdql3aajgb8wgbwwHwYDVR0jBBgwFoAUS2iy4oMAoxUY87nZRidUqYg9yyMwagYDVR0fBGMwYTBfoF2gW4ZZbGRhcDovL2NhZHMuZGdjLmdvdi5pdC9DTj1JdGFseSUyMERHQyUyMENTQ0ElMjBURVNUJTIwMSxPPU1pbmlzdGVybyUyMGRlbGxhJTIwU2FsdXRlLEM9SVQwHQYDVR0OBBYEFNSEwjzu61pAMqliNhS9vzGJFqFFMA4GA1UdDwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAgEAIF74yHgzCGdor5MaqYSvkS5aog5+7u52TGggiPl78QAmIpjPO5qcYpJZVf6AoL4MpveEI/iuCUVQxBzYqlLACjSbZEbtTBPSzuhfvsf9T3MUq5cu10lkHKbFgApUDjrMUnG9SMqmQU2Cv5S4t94ec2iLmokXmhYP/JojRXt1ZMZlsw/8/lRJ8vqPUorJ/fMvOLWDE/fDxNhh3uK5UHBhRXCT8MBep4cgt9cuT9O4w1JcejSr5nsEfeo8u9Pb/h6MnmxpBSq3JbnjONVK5ak7iwCkLr5PMk09ncqG+/8Kq+qTjNC76IetS9ST6bWzTZILX4BD1BL8bHsFGgIeeCO0GqalFZAsbapnaB+36HVUZVDYOoA+VraIWECNxXViikZdjQONaeWDVhCxZ/vBl1/KLAdX3OPxRwl/jHLnaSXeqr/zYf9a8UqFrpadT0tQff/q3yH5hJRJM0P6Yp5CPIEArJRW6ovDBbp3DVF2GyAI1lFA2Trs798NN6qf7SkuySz5HSzm53g6JsLY/HLzdwJPYLObD7U+x37n+DDi4Wa6vM5xdC7FZ5IyWXuT1oAa9yM4h6nW3UvC+wNUusW6adqqtdd4F1gHPjCf5lpW5Ye1bdLUmO7TGlePmbOkzEB08Mlc6atl/vkx/crfl4dq1LZivLgPBwDzE8arIk0f2vCx1+4=";
	public final static String DSC_TO_REVOKE = "MIIEDzCCAfegAwIBAgIURldu5rsfrDeZtDBxrJ+SujMr2IswDQYJKoZIhvcNAQELBQAwSTELMAkGA1UEBhMCSVQxHzAdBgNVBAoMFk1pbmlzdGVybyBkZWxsYSBTYWx1dGUxGTAXBgNVBAMMEEl0YWx5IERHQyBDU0NBIDEwHhcNMjEwNTEyMDgxODE3WhcNMjMwNTEyMDgxMTU5WjBIMQswCQYDVQQGEwJJVDEfMB0GA1UECgwWTWluaXN0ZXJvIGRlbGxhIFNhbHV0ZTEYMBYGA1UEAwwPSXRhbHkgREdDIERTQyAxMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEnL9+WnIp9fvbcocZSGUFlSw9ffW/jbMONzcvm1X4c+pXOPEs7C4/83+PxS8Swea2hgm/tKt4PI0z8wgnIehoj6OBujCBtzAfBgNVHSMEGDAWgBS+VOVpXmeSQImXYEEAB/pLRVCw/zBlBgNVHR8EXjBcMFqgWKBWhlRsZGFwOi8vY2Fkcy5kZ2MuZ292Lml0L0NOPUl0YWx5JTIwREdDJTIwQ1NDQSUyMHhcMSxPPU1pbmlzdGVybyUyMGRlbGxhJTIwU2FsdXRlLEM9SVQwHQYDVR0OBBYEFC4bAbCvpArrgZ0E+RrqS8V7TNNIMA4GA1UdDwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAgEAjxTeF7yhKz/3PKZ9+WfgZPaIzZvnO/nmuUartgVd3xuTPNtd5tuYRNS/1B78HNNk7fXiq5hH2q8xHF9yxYxExov2qFrfUMD5HOZzYKHZcjcWFNHvH6jx7qDCtb5PrOgSK5QUQzycR7MgWIFinoWwsWIrA1AJOwfUoi7v1aoWNMK1eHZmR3Y9LQ84qeE2yDk3jqEGjlJVCbgBp7O8emzy2KhWv3JyRZgTmFz7p6eRXDzUYHtJaufveIhkNM/U8p3S7egQegliIFMmufvEyZemD2BMvb97H9PQpuzeMwB8zcFbuZmNl42AFMQ2PhQe27pU0wFsDEqLe0ETb5eR3T9L6zdSrWldw6UuXoYV0/5fvjA55qCjAaLJ0qi16Ca/jt6iKuws/KKh9yr+FqZMnZUH2D2j2i8LBA67Ie0JoZPSojr8cwSTxQBdJFI722uczCj/Rt69Y4sLdV3hNQ2A9hHrXesyQslr0ez3UHHzDRFMVlOXWCayj3LIgvtfTjKrT1J+/3Vu9fvs1+CCJELuC9gtVLxMsdRc/A6/bvW4mAsyY78ROX27Bi8CxPN5IZbtiyjpmdfr2bufDcwhwzdwsdQQDoSiIF1LZqCn7sHBmUhzoPcBJdXFET58EKow0BWcerZzpvsVHcMTE2uuAUr/JUh1SBpoJCiMIRSl+XPoEA2qqYU=";

	public final static String CSCA_TO_DOWNLOAD_RAW_DATA = "MIIIBzCCBb+gAwIBAgIQc55Lvm9bVwmjNqwFgc0RijA9BgkqhkiG9w0BAQowMKANMAsGCWCGSAFlAwQCA6EaMBgGCSqGSIb3DQEBCDALBglghkgBZQMEAgOiAwIBQDBjMQswCQYDVQQGEwJERTEVMBMGA1UEChMMRC1UcnVzdCBHbWJIMSQwIgYDVQQDExtELVRSVVNUIFJvb3QgVGVzdCBDQSAyIDIwMTgxFzAVBgNVBGETDk5UUkRFLUhSQjc0MzQ2MB4XDTE5MDUwOTExMTIxOFoXDTMzMTAwNDA2MTU0MlowYDELMAkGA1UEBhMCREUxFTATBgNVBAoTDEQtVHJ1c3QgR21iSDEhMB8GA1UEAxMYRC1UUlVTVCBUZXN0IENBIDItMiAyMDE5MRcwFQYDVQRhEw5OVFJERS1IUkI3NDM0NjCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBANXhB5+drMwwX++ZMGFEsnlB0HjeuNIHH0pgPhNCwDONoIZWWATPMW0SMVopGWyz8ajUQznOQ06Q/yVyqeJnKJwRjzSqQvcxAHMBkGaZwu+r+ePnRcM46ShAotY2nei0rOmAbl1sySfMz9h0ccfNjDFX4V1bJ/bNcTX1iBat2xxIy/HDsKXjpOvkADFgh9hfL4G6PAnfOzqwLnG5rLPVoXNmbMwU/GHkjOQL72bQIs0nKP41lgXOB6MQPmOxluMyBfh+w1Rd5cbel7C9/h/ZSYlwsg4lSt6t6TBXBwcmT0gOkVyz3mJBFZtpeLp2fB+kxOj4ZNrzMLJxoc/mG2MbLvRevANK1RZc8NrM3zlCwhu7IPO6npV48IUEK61f7soTGiBlFcSb6kQJ456RF+YI3btUhe1V2ut1Uoolc+4nlNs4MCDK0BkRGYYxRvX3e4UJLtQ9gQ87k6geEe5dglNqhuXdwn+pKlZhvnEYqlgZmbQPHi1xOjuYVdF6ZrEi5cm6Wog+cGoZSEBs8egyFU/zJN8lPpgqL4SyclPtSV8TSDJ/hxCm8hHaAPS8a2yffphllaZ9i7J9r8+6MNFt6TGdl+UReH4RCS8H6xT8IiA0bUhH0OCUcivsIibYRd5BK8D5oe51K3Tf5hx9MmYZZ4wBMYJnZvixnblINKxKJTgibfA/AgMBAAGjggJYMIICVDAfBgNVHSMEGDAWgBShkUWQEYUjtdw3eFbx1VWWJbcFWDCCAQYGCCsGAQUFBwEBBIH5MIH2MCsGCCsGAQUFBzABhh9odHRwOi8vc3RhZ2luZy5vY3NwLmQtdHJ1c3QubmV0MEoGCCsGAQUFBzAChj5odHRwOi8vd3d3LmQtdHJ1c3QubmV0L2NnaS1iaW4vRC1UUlVTVF9Sb290X1Rlc3RfQ0FfMl8yMDE4LmNydDB7BggrBgEFBQcwAoZvbGRhcDovL2RpcmVjdG9yeS5kLXRydXN0Lm5ldC9DTj1ELVRSVVNUJTIwUm9vdCUyMFRlc3QlMjBDQSUyMDIlMjAyMDE4LE89RC1UcnVzdCUyMEdtYkgsQz1ERT9jQUNlcnRpZmljYXRlP2Jhc2U/MBcGA1UdIAQQMA4wDAYKKwYBBAGlNAICAjCBygYDVR0fBIHCMIG/MHugeaB3hnVsZGFwOi8vZGlyZWN0b3J5LmQtdHJ1c3QubmV0L0NOPUQtVFJVU1QlMjBSb290JTIwVGVzdCUyMENBJTIwMiUyMDIwMTgsTz1ELVRydXN0JTIwR21iSCxDPURFP2NlcnRpZmljYXRlcmV2b2NhdGlvbmxpc3QwQKA+oDyGOmh0dHA6Ly9jcmwuZC10cnVzdC5uZXQvY3JsL2QtdHJ1c3Rfcm9vdF90ZXN0X2NhXzJfMjAxOC5jcmwwHQYDVR0OBBYEFFB2kqAa7IGukcLdqAlSaDfeUYRPMA4GA1UdDwEB/wQEAwIBBjASBgNVHRMBAf8ECDAGAQH/AgEAMD0GCSqGSIb3DQEBCjAwoA0wCwYJYIZIAWUDBAIDoRowGAYJKoZIhvcNAQEIMAsGCWCGSAFlAwQCA6IDAgFAA4ICAQCByOucGXcI5cDfSoigtixzqxe0UB4rCnNwBahzLqB7BMOFaRI7WOgfUd4JAMhKur7RBFXrP/mOq6jq10QAHiamjQ3rcxCo5yy5b/omfmg//eqOd36GCSLSUF2j9WBLsQVwbI4Ey6VuKFkT26hClAdeiOd5MmHKEAbT34f65J9vEt0Vc8bjJ77AG7QNva5ry7WkZviy8Lld9jptOKCOnRVGxFH7O68YQ0B5Llv4JyNaTfl8cpHWZ2ToUhQcMXVRGm32FV0PX7RC89RFkRypsK+Uyrk+6mshPZzpGzvCj5QmCoLwxh0dzpgDKaxr4M6wiLSyPO4jol1hsp0199V0m7gV+ZhVTtL+f/4bUNarjMK/v7ehM9ZYPrzz9+E+p5YxWRBqA7MP/PvXtUKdgL+sd/q0kfIknAl3GMhx9BLY6Ovm2k5zPUyrTiQ8vxBdCShqsk+myGQ+JG2tQT1dv5V2ksE43HpeWPjcQtWEXGXmVrwVb6oYMbnCZToLeuLv6QlKiurhfjQiEUNK7wcBY3wB7NudN3x7hC58sCNj45dTZnI4q1YriZx/H6t+3+snfscRvG6WVMFkFru8q2+92J7VfLU4XUnH/f2GOqJFS3i3rM0M1GBRO06XCjCmyjcYhZEUkViknHJOSKeS1Cu4AGbTuUVZR9zSGIwJ5kEe98f7x+kz0w==";
	public final static String CSCA_TO_DOWNLOAD_KID = "lkLenRso6HI=";
	public final static String CSCA_TO_DOWNLOAD_THUMBPRINT = "9642de9d1b28e87256c6653c128e03d8ec0b2bcfa7b7f99c7244049b27044f0a";
	public final static String CSCA_TO_DOWNLOAD_SIGNATURE = "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";

	public final static String DSC_TO_DOWNLOAD_RAW_DATA = "MIIEljCCAn6gAwIBAgIUT+F5oDNOT/2abGQ6fIJC6yGQrK0wDQYJKoZIhvcNAQELBQAwgZgxCzAJBgNVBAYTAkRFMQ8wDQYDVQQIDAZIZXNzZW4xGjAYBgNVBAcMEUZyYW5rZnVydCBhbSBNYWluMSUwIwYDVQQKDBxULVN5c3RlbXMgSW50ZXJuYXRpb25hbCBHbWJIMRowGAYDVQQLDBFEaWdpdGFsIFNvbHV0aW9uczEZMBcGA1UEAwwQVGVzdCBUZWFtIChDU0NBKTAeFw0yMTA1MTEwODA1MjlaFw0yMTA1MTEwODA1MzlaMHExCzAJBgNVBAYTAkRFMR0wGwYDVQQIDBROb3RocmhpbmUgV2VzdHBoYWxpYTEOMAwGA1UEBwwFRXNzZW4xIDAeBgNVBAoMF1QtU3lzdGVtcyBJbnRlcm5hdGlvbmFsMREwDwYDVQQDDAhhcGktdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMg1qkCdizhyrVkUIvl3Ajpqa5wX0VBYh4zIjdr0348JRKnkmXO0riycUQ8T3SrgEpUk0JT6XGYeHrx3xB3OTpfLQ4UeRWw1HIKNgKXqncRbig8Py2Ef7xHcuLVyPyIiMsO2qXh9R62xcC1i9/0gVuaDYAA11lbfyYcOqmfSR4lK+3nwmZtMe5OZyDvhzB0l9CdKMkAGTZuTtlWHXqND0DfEwKabyNk5LGw8wWWEzTZkhu4MfsqgaGGsNsndwiphJJ1KeZi+L0VtDGZ5hwuTcZ3q9V1ONlX6bYLoLK1e8bRZWCNVLATKedqf+bq/qlLSF0P+8OmlOLgEAuD2VD4V+Y8CAwEAATANBgkqhkiG9w0BAQsFAAOCAgEAMVp/QTGmnT1Q5eLksg49QPVPY5qvi3DzAENjT673sdnUdFPJeI2+wxCgHtf0jLAfp2Ssufego7pmDOgvweb5CqhDC34DuKwxGLY6q+UBISh7UoNhsjNJNFGmaZmCCUipM2tRoVml8ykwGEgynoTHxIBrQUi9M4/rAjD7BW/Cg/2yhsBUuI9+YMwL2sCvaxqbfstTvD/mrYDeBAUuUpS5MjyC90sSg4o+GejPUm2/Z8Y23mtnkmX+lrAqfnb/R9xDbRki7T/mg+A8nxkK61Cd0o3lAhXmWEOVTud7+wIc27p++utJJH6PTZrqE/Pm6lLRf04rHHHvVyLUoCgrvqy7SjO8X1DKJhsbH6T7e8ieU7LgO90VyFKjVtgOGVjIztUn5gWtd7pfvX6jMSiIxKjQsHfrVF6CE/jW3WFun9kaz0JwYQenM6Z88TS73NohhW9qaCwiFRKvwlz84lAP2GNRLdMEPTHzmxR/kGjYSQRs5ispgaUyEYFqEw9huaG3YFxgt4d8Ey90he+1HypB8+4mfGvfmvA/zKUEY4FkDvxFBdJffeT01bhiROp8QRtBdEEOltq8oAae9PBNt92qpcsdPaElIXfcYguX8vi2rFlXEW4mScvls/8jw1kUU3m5tdABKpezZ4sA3LyZOL+4H4BLxTZUlD0k5wCEFifETlk+az0=";
	public final static String DSC_TO_DOWNLOAD_KID = "l3DTTvY1/h0=";
	public final static String DSC_TO_DOWNLOAD_THUMBPRINT = "9770d34ef635fe1d99c9a18413ca3dd6604b3863dbedbd41f7aa55d473f06383";
	public final static String DSC_TO_DOWNLOAD_SIGNATURE = "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";

	public final static String SIGNATURE_SERVICE_MOCKDATA = "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";

	public static RestApiResponse<List<TrustListItemDto>> getTrustListResponse() {

		ArrayList<TrustListItemDto> trustList = new ArrayList<TrustListItemDto>();

		TrustListItemDto trust1 = new TrustListItemDto();
		trust1.setRawData(DgcWorkerTestHelper.CSCA_TO_DOWNLOAD_RAW_DATA);
		trust1.setKid(DgcWorkerTestHelper.CSCA_TO_DOWNLOAD_KID);
		trust1.setSignature(DgcWorkerTestHelper.CSCA_TO_DOWNLOAD_SIGNATURE);
		trust1.setThumbprint(DgcWorkerTestHelper.CSCA_TO_DOWNLOAD_THUMBPRINT);
		trust1.setCertificateType(CertificateType.CSCA);
		trust1.setTimestamp(new Date());
		trust1.setCountry("DE");
		trustList.add(trust1);

		TrustListItemDto trust2 = new TrustListItemDto();
		trust2.setRawData(DgcWorkerTestHelper.DSC_TO_DOWNLOAD_RAW_DATA);
		trust2.setKid(DgcWorkerTestHelper.DSC_TO_DOWNLOAD_KID);
		trust2.setSignature(DgcWorkerTestHelper.DSC_TO_DOWNLOAD_SIGNATURE);
		trust2.setThumbprint(DgcWorkerTestHelper.DSC_TO_DOWNLOAD_THUMBPRINT);
		trust2.setCertificateType(CertificateType.DSC);
		trust2.setTimestamp(new Date());
		trust2.setCountry("DE");
		trustList.add(trust2);

		return new RestApiResponse<List<TrustListItemDto>>(HttpStatus.OK, null, trustList);
	}
}