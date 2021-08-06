/*
 *  Copyright (C) 2021 Ministero della Salute and all other contributors.
 *  Please refer to the AUTHORS file for more information.
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package it.interop.dgc.gateway.worker.testdata;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import it.interop.dgc.gateway.client.base.RestApiResponse;
import it.interop.dgc.gateway.dto.TrustListItemDto;
import it.interop.dgc.gateway.dto.ValidationRuleDto;
import it.interop.dgc.gateway.enums.CertificateType;
import it.interop.dgc.gateway.model.ValidationRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

public class DgcWorkerTestHelper {

    public static final String DSC_TO_UPLOAD =
        "MIIEHjCCAgagAwIBAgIUM5lJeGCHoRF1raR6cbZqDV4vPA8wDQYJKoZIhvcNAQELBQAwTjELMAkGA1UEBhMCSVQxHzAdBgNVBAoMFk1pbmlzdGVybyBkZWxsYSBTYWx1dGUxHjAcBgNVBAMMFUl0YWx5IERHQyBDU0NBIFRFU1QgMTAeFw0yMTA1MDcxNzAyMTZaFw0yMzA1MDgxNzAyMTZaME0xCzAJBgNVBAYTAklUMR8wHQYDVQQKDBZNaW5pc3Rlcm8gZGVsbGEgU2FsdXRlMR0wGwYDVQQDDBRJdGFseSBER0MgRFNDIFRFU1QgMTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABDSp7t86JxAmjZFobmmu0wkii53snRuwqVWe3/g/wVz9i306XA5iXpHkRPZVUkSZmYhutMDrheg6sfwMRdql3aajgb8wgbwwHwYDVR0jBBgwFoAUS2iy4oMAoxUY87nZRidUqYg9yyMwagYDVR0fBGMwYTBfoF2gW4ZZbGRhcDovL2NhZHMuZGdjLmdvdi5pdC9DTj1JdGFseSUyMERHQyUyMENTQ0ElMjBURVNUJTIwMSxPPU1pbmlzdGVybyUyMGRlbGxhJTIwU2FsdXRlLEM9SVQwHQYDVR0OBBYEFNSEwjzu61pAMqliNhS9vzGJFqFFMA4GA1UdDwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAgEAIF74yHgzCGdor5MaqYSvkS5aog5+7u52TGggiPl78QAmIpjPO5qcYpJZVf6AoL4MpveEI/iuCUVQxBzYqlLACjSbZEbtTBPSzuhfvsf9T3MUq5cu10lkHKbFgApUDjrMUnG9SMqmQU2Cv5S4t94ec2iLmokXmhYP/JojRXt1ZMZlsw/8/lRJ8vqPUorJ/fMvOLWDE/fDxNhh3uK5UHBhRXCT8MBep4cgt9cuT9O4w1JcejSr5nsEfeo8u9Pb/h6MnmxpBSq3JbnjONVK5ak7iwCkLr5PMk09ncqG+/8Kq+qTjNC76IetS9ST6bWzTZILX4BD1BL8bHsFGgIeeCO0GqalFZAsbapnaB+36HVUZVDYOoA+VraIWECNxXViikZdjQONaeWDVhCxZ/vBl1/KLAdX3OPxRwl/jHLnaSXeqr/zYf9a8UqFrpadT0tQff/q3yH5hJRJM0P6Yp5CPIEArJRW6ovDBbp3DVF2GyAI1lFA2Trs798NN6qf7SkuySz5HSzm53g6JsLY/HLzdwJPYLObD7U+x37n+DDi4Wa6vM5xdC7FZ5IyWXuT1oAa9yM4h6nW3UvC+wNUusW6adqqtdd4F1gHPjCf5lpW5Ye1bdLUmO7TGlePmbOkzEB08Mlc6atl/vkx/crfl4dq1LZivLgPBwDzE8arIk0f2vCx1+4=";
    public static final String DSC_TO_REVOKE =
        "MIIEDzCCAfegAwIBAgIURldu5rsfrDeZtDBxrJ+SujMr2IswDQYJKoZIhvcNAQELBQAwSTELMAkGA1UEBhMCSVQxHzAdBgNVBAoMFk1pbmlzdGVybyBkZWxsYSBTYWx1dGUxGTAXBgNVBAMMEEl0YWx5IERHQyBDU0NBIDEwHhcNMjEwNTEyMDgxODE3WhcNMjMwNTEyMDgxMTU5WjBIMQswCQYDVQQGEwJJVDEfMB0GA1UECgwWTWluaXN0ZXJvIGRlbGxhIFNhbHV0ZTEYMBYGA1UEAwwPSXRhbHkgREdDIERTQyAxMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEnL9+WnIp9fvbcocZSGUFlSw9ffW/jbMONzcvm1X4c+pXOPEs7C4/83+PxS8Swea2hgm/tKt4PI0z8wgnIehoj6OBujCBtzAfBgNVHSMEGDAWgBS+VOVpXmeSQImXYEEAB/pLRVCw/zBlBgNVHR8EXjBcMFqgWKBWhlRsZGFwOi8vY2Fkcy5kZ2MuZ292Lml0L0NOPUl0YWx5JTIwREdDJTIwQ1NDQSUyMHhcMSxPPU1pbmlzdGVybyUyMGRlbGxhJTIwU2FsdXRlLEM9SVQwHQYDVR0OBBYEFC4bAbCvpArrgZ0E+RrqS8V7TNNIMA4GA1UdDwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAgEAjxTeF7yhKz/3PKZ9+WfgZPaIzZvnO/nmuUartgVd3xuTPNtd5tuYRNS/1B78HNNk7fXiq5hH2q8xHF9yxYxExov2qFrfUMD5HOZzYKHZcjcWFNHvH6jx7qDCtb5PrOgSK5QUQzycR7MgWIFinoWwsWIrA1AJOwfUoi7v1aoWNMK1eHZmR3Y9LQ84qeE2yDk3jqEGjlJVCbgBp7O8emzy2KhWv3JyRZgTmFz7p6eRXDzUYHtJaufveIhkNM/U8p3S7egQegliIFMmufvEyZemD2BMvb97H9PQpuzeMwB8zcFbuZmNl42AFMQ2PhQe27pU0wFsDEqLe0ETb5eR3T9L6zdSrWldw6UuXoYV0/5fvjA55qCjAaLJ0qi16Ca/jt6iKuws/KKh9yr+FqZMnZUH2D2j2i8LBA67Ie0JoZPSojr8cwSTxQBdJFI722uczCj/Rt69Y4sLdV3hNQ2A9hHrXesyQslr0ez3UHHzDRFMVlOXWCayj3LIgvtfTjKrT1J+/3Vu9fvs1+CCJELuC9gtVLxMsdRc/A6/bvW4mAsyY78ROX27Bi8CxPN5IZbtiyjpmdfr2bufDcwhwzdwsdQQDoSiIF1LZqCn7sHBmUhzoPcBJdXFET58EKow0BWcerZzpvsVHcMTE2uuAUr/JUh1SBpoJCiMIRSl+XPoEA2qqYU=";

    public static final String CSCA_TO_DOWNLOAD_RAW_DATA =
        "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";
    public static final String CSCA_TO_DOWNLOAD_KID = "lkLenRso6HI=";
    public static final String CSCA_TO_DOWNLOAD_THUMBPRINT =
        "9642de9d1b28e87256c6653c128e03d8ec0b2bcfa7b7f99c7244049b27044f0a";
    public static final String CSCA_TO_DOWNLOAD_SIGNATURE =
        "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";

    public static final String DSC_TO_DOWNLOAD_RAW_DATA =
        "MIIEljCCAn6gAwIBAgIUT+F5oDNOT/2abGQ6fIJC6yGQrK0wDQYJKoZIhvcNAQELBQAwgZgxCzAJBgNVBAYTAkRFMQ8wDQYDVQQIDAZIZXNzZW4xGjAYBgNVBAcMEUZyYW5rZnVydCBhbSBNYWluMSUwIwYDVQQKDBxULVN5c3RlbXMgSW50ZXJuYXRpb25hbCBHbWJIMRowGAYDVQQLDBFEaWdpdGFsIFNvbHV0aW9uczEZMBcGA1UEAwwQVGVzdCBUZWFtIChDU0NBKTAeFw0yMTA1MTEwODA1MjlaFw0yMTA1MTEwODA1MzlaMHExCzAJBgNVBAYTAkRFMR0wGwYDVQQIDBROb3RocmhpbmUgV2VzdHBoYWxpYTEOMAwGA1UEBwwFRXNzZW4xIDAeBgNVBAoMF1QtU3lzdGVtcyBJbnRlcm5hdGlvbmFsMREwDwYDVQQDDAhhcGktdGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMg1qkCdizhyrVkUIvl3Ajpqa5wX0VBYh4zIjdr0348JRKnkmXO0riycUQ8T3SrgEpUk0JT6XGYeHrx3xB3OTpfLQ4UeRWw1HIKNgKXqncRbig8Py2Ef7xHcuLVyPyIiMsO2qXh9R62xcC1i9/0gVuaDYAA11lbfyYcOqmfSR4lK+3nwmZtMe5OZyDvhzB0l9CdKMkAGTZuTtlWHXqND0DfEwKabyNk5LGw8wWWEzTZkhu4MfsqgaGGsNsndwiphJJ1KeZi+L0VtDGZ5hwuTcZ3q9V1ONlX6bYLoLK1e8bRZWCNVLATKedqf+bq/qlLSF0P+8OmlOLgEAuD2VD4V+Y8CAwEAATANBgkqhkiG9w0BAQsFAAOCAgEAMVp/QTGmnT1Q5eLksg49QPVPY5qvi3DzAENjT673sdnUdFPJeI2+wxCgHtf0jLAfp2Ssufego7pmDOgvweb5CqhDC34DuKwxGLY6q+UBISh7UoNhsjNJNFGmaZmCCUipM2tRoVml8ykwGEgynoTHxIBrQUi9M4/rAjD7BW/Cg/2yhsBUuI9+YMwL2sCvaxqbfstTvD/mrYDeBAUuUpS5MjyC90sSg4o+GejPUm2/Z8Y23mtnkmX+lrAqfnb/R9xDbRki7T/mg+A8nxkK61Cd0o3lAhXmWEOVTud7+wIc27p++utJJH6PTZrqE/Pm6lLRf04rHHHvVyLUoCgrvqy7SjO8X1DKJhsbH6T7e8ieU7LgO90VyFKjVtgOGVjIztUn5gWtd7pfvX6jMSiIxKjQsHfrVF6CE/jW3WFun9kaz0JwYQenM6Z88TS73NohhW9qaCwiFRKvwlz84lAP2GNRLdMEPTHzmxR/kGjYSQRs5ispgaUyEYFqEw9huaG3YFxgt4d8Ey90he+1HypB8+4mfGvfmvA/zKUEY4FkDvxFBdJffeT01bhiROp8QRtBdEEOltq8oAae9PBNt92qpcsdPaElIXfcYguX8vi2rFlXEW4mScvls/8jw1kUU3m5tdABKpezZ4sA3LyZOL+4H4BLxTZUlD0k5wCEFifETlk+az0=";
    public static final String DSC_TO_DOWNLOAD_KID = "l3DTTvY1/h0=";
    public static final String DSC_TO_DOWNLOAD_THUMBPRINT =
        "9770d34ef635fe1d99c9a18413ca3dd6604b3863dbedbd41f7aa55d473f06383";
    public static final String DSC_TO_DOWNLOAD_SIGNATURE =
        "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";

    public static final String UPLOAD_TO_DOWNLOAD_RAW_DATA =
        "MIIDmjCCAYKgAwIBAgIBAjANBgkqhkiG9w0BAQsFADBOMQswCQYDVQQGEwJJVDEfMB0GA1UEChMWTWluaXN0ZXJvIGRlbGxhIFNhbHV0ZTEeMBwGA1UEAxMVSXRhbHkgREdDIENTQ0EgVEVTVCAxMB4XDTIxMDUwNjEyNTYwMFoXDTIzMDUwNjEyNTYwMFowTTELMAkGA1UEBhMCSVQxHzAdBgNVBAoTFk1pbmlzdGVybyBkZWxsYSBTYWx1dGUxHTAbBgNVBAMTFEl0YWx5IERHQyBEU0MgVEVTVCAxMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE6q8AEtpZ6IUPv9uyn/Iy/TfMd74nzx7jeMxNnQ+quUHVABdIV44gQ8mPdpm7kezoqGMXPq2JR1FV7uTWv1ePGqNPME0wHQYDVR0OBBYEFCvpLggXW3IPTUzl+0do8GbvcEi1MB8GA1UdIwQYMBaAFMYzb0UOenRVm1nPSQJe/KjS673hMAsGA1UdDwQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAgEAWy1xHrv1rGP4XIRHOSSpCP0o5eD8Y5uGABsi1cXMXTEXU9wlM4dGYtGA4sY7N4XsiQg1WN6mxl/OPXEJmDd+25O/1O+FPNrtaanCwR8+vUkcj0q0nRNozmvSb4Fag73XZ4UiAIU2bm/3NOAo8qlvcEYleEg1oxpDEnUdKB7Zav9eqhfkPL02b38zB7GE3pk+wxNyM/Vkkot+d8cKBPd9n4Q6qn0DHHC84hC2saJRWsWkSttDbMO7hN+GPBbzfxOgtfkO0vw2ePMN08B2DN0A6AHGcZzhrdj5YHWm/lff8NIeCzlQqXPWwsiecQhRe+UCGgDWnXqQqluRmCFle6lM6WSApoDrmmMv0ixH/Ebo1kr3U6zHPZ+qhWkVJvIceXjsiA/rpsu6Cj2y5MeQw2bJlgCxEPhVRP533jPhB8GNak42uusWnog8HesF+zHtaagwXpw5pjGs/IieXlgNImexcnvts3tuFQPxrb2KYAm3x3UZFruWHrhoBS+TSTtJcuCAndl+J2s84inEu6AzNABSyvibN0YvkQmPtjcCW/RXG9smPfCTvzA7hFss3ADI7hukApcKwST5G82t1OU1r0N2GcTahQBMTLmMMM6QWs6YFRLm+z/RfgXgcUhvnRgJaHKkEd4SSm7B54YLX4h23FobExNAodH/SxYsg1fcsOYq6s8=";
    public static final String UPLOAD_TO_DOWNLOAD_KID = "8+hI4SSjinc=";
    public static final String UPLOAD_TO_DOWNLOAD_THUMBPRINT =
        "F3E848E124A38A777316EE35CE9EF472EE3E1AB8EA4AED4B785137C87A226B78";
    public static final String UPLOAD_TO_DOWNLOAD_SIGNATURE =
        "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";

    public static final String UK_DSC_TO_DOWNLOAD_RAW_DATA =
        "MIIBjDCCAYGgAwIBAgIBATACBgAwNjELMAkGA1UEBhMCVUsxCzAJBgNVBAsMAkhTMRowGAYDVQQDDBFVSyBEQ0MgRFNDIFRFU1QgMTAeFw0yMTA3MjgxNDAyMzJaFw0yMzA3MjgxNDAyMzJaMDYxCzAJBgNVBAYTAlVLMQswCQYDVQQLDAJIUzEaMBgGA1UEAwwRVUsgRENDIERTQyBURVNUIDEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAT7i6RR2ab1/gCbJAHBMtAUo88JCPh8/YGlCQiXaFvinoXE8YM/3PcmQijbDbGSjGqzYmtMefYwJ3KHdp7JlYPgo4GHMIGEMAwGA1UdEwEB/wQCMAAwHQYDVR0OBBYEFNo5o+5ea0sNMlW/75VgGJCv2AcJMEgGA1UdIwRBMD+hOqQ4MDYxCzAJBgNVBAYTAlVLMQswCQYDVQQLDAJIUzEaMBgGA1UEAwwRVUsgRENDIERTQyBURVNUIDGCAQEwCwYDVR0PBAQDAgeAMAIGAAMBAA==";
    public static final String UK_DSC_TO_DOWNLOAD_KID = "S2V5NVBSTw==";
    public static final String UK_DSC_TO_DOWNLOAD_THUMBPRINT =
        "9770d34ef635fe1d99c9a18413ca3dd6604b3863dbedbd41f7aa55d473f06383";
    public static final String UK_DSC_TO_DOWNLOAD_SIGNATURE =
        "EXAMPLESIGNATURE==";

    public static final String COUNTRY_LIST =
        "[\"DX\",\"DE\",\"ZZ\",\"XX\",\"AT\",\"LU\",\"HR\",\"SE\",\"NL\",\"FR\",\"ES\",\"DK\",\"LT\",\"GR\",\"IT\",\"CZ\",\"YY\",\"IS\",\"BG\",\"MT\",\"BE\",\"RO\",\"LV\",\"CY\",\"EE\",\"PL\",\"PT\",\"WW\",\"SI\",\"IE\",\"SK\",\"YA\",\"YB\",\"LI\",\"CH\",\"FI\",\"NO\",\"HU\",\"VA\",\"SM\",\"TR\",\"MK\",\"UA\"]";
    public static final String COUNTRY_LIST_EU_ONLY = "[\"EU\"]";
    public static final String COUNTRY_LIST_HASH =
        "54ecef1421fe57f8939400ef77e6c9055810a89ea10200d54461546b0eb2e2da";

    public static final String VALUESET_1 =
        "{\"valueSetId\":\"vaccines-covid-19-names\",\"valueSetDate\":\"2021-05-07\",\"valueSetValues\":{\"EU/1/20/1528\":{\"display\":\"Comirnaty\",\"lang\":\"en\",\"active\":true,\"system\":\"https://ec.europa.eu/health/documents/community-register/html/\",\"version\":\"\"},\"EU/1/20/1507\":{\"display\":\"Spikevax(previouslyCOVID-19VaccineModerna)\",\"lang\":\"en\",\"active\":true,\"system\":\"https://ec.europa.eu/health/documents/community-register/html/\",\"version\":\"\"},\"EU/1/21/1529\":{\"display\":\"Vaxzevria\",\"lang\":\"en\",\"active\":true,\"system\":\"https://ec.europa.eu/health/documents/community-register/html/\",\"version\":\"\"},\"EU/1/20/1525\":{\"display\":\"COVID-19VaccineJanssen\",\"lang\":\"en\",\"active\":true,\"system\":\"https://ec.europa.eu/health/documents/community-register/html/\",\"version\":\"\"},\"CVnCoV\":{\"display\":\"CVnCoV\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"Sputnik-V\":{\"display\":\"Sputnik-V\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"Convidecia\":{\"display\":\"Convidecia\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"EpiVacCorona\":{\"display\":\"EpiVacCorona\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"BBIBP-CorV\":{\"display\":\"BBIBP-CorV\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"Inactivated-SARS-CoV-2-Vero-Cell\":{\"display\":\"InactivatedSARS-CoV-2(VeroCell)\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"CoronaVac\":{\"display\":\"CoronaVac\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"Covaxin\":{\"display\":\"Covaxin(alsoknownasBBV152A,B,C)\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"},\"Covishield\":{\"display\":\"Covishield(ChAdOx1_nCoV-19)\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccineproductname\",\"version\":\"1.0\"}}}";
    public static final String VALUESET_1_ID = "vaccines-covid-19-names";
    public static final String VALUESET_1_HASH =
        "8a3abfa4a0f1130d5f3e684c64035f323d6496b8aa4ac46f87306798eda46fff";
    public static final String VALUESET_2 =
        "{\"valueSetId\":\"vaccines-covid-19-auth-holders\",\"valueSetDate\":\"2021-05-07\",\"valueSetValues\":{\"ORG-100001699\":{\"display\":\"AstraZenecaAB\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100030215\":{\"display\":\"BiontechManufacturingGmbH\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100001417\":{\"display\":\"Janssen-CilagInternational\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100031184\":{\"display\":\"ModernaBiotechSpainS.L.\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100006270\":{\"display\":\"CurevacAG\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100013793\":{\"display\":\"CanSinoBiologics\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100020693\":{\"display\":\"ChinaSinopharmInternationalCorp.-Beijinglocation\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100010771\":{\"display\":\"SinopharmWeiqidaEuropePharmaceuticals.r.o.-Praguelocation\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100024420\":{\"display\":\"SinopharmZhijun(Shenzhen)PharmaceuticalCo.Ltd.-Shenzhenlocation\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"ORG-100032020\":{\"display\":\"NovavaxCZAS\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"},\"Gamaleya-Research-Institute\":{\"display\":\"GamaleyaResearchInstitute\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccinemanufacturer\",\"version\":\"1.0\"},\"Vector-Institute\":{\"display\":\"VectorInstitute\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccinemanufacturer\",\"version\":\"1.0\"},\"Sinovac-Biotech\":{\"display\":\"SinovacBiotech\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccinemanufacturer\",\"version\":\"1.0\"},\"Bharat-Biotech\":{\"display\":\"BharatBiotech\",\"lang\":\"en\",\"active\":true,\"system\":\"http://ec.europa.eu/temp/vaccinemanufacturer\",\"version\":\"1.0\"},\"ORG-100001981\":{\"display\":\"SerumInstituteOfIndiaPrivateLimited\",\"lang\":\"en\",\"active\":true,\"system\":\"https://spor.ema.europa.eu/v1/organisations\",\"version\":\"\"}}}";
    public static final String VALUESET_2_ID = "vaccines-covid-19-auth-holders";
    public static final String VALUESET_2_HASH =
        "5c82cf77f40546609c481e333b1ba5ef7e07c722ee054435312228bac24c6cb2";
    public static final String VALUESET_3 =
        "{\"valueSetId\":\"sct-vaccines-covid-19\",\"valueSetDate\":\"2021-04-27\",\"valueSetValues\":{\"1119349007\":{\"display\":\"SARS-CoV-2mRNAvaccine\",\"lang\":\"en\",\"active\":true,\"version\":\"http://snomed.info/sct/900000000000207008/version/20210131\",\"system\":\"http://snomed.info/sct\"},\"1119305005\":{\"display\":\"SARS-CoV-2antigenvaccine\",\"lang\":\"en\",\"active\":true,\"version\":\"http://snomed.info/sct/900000000000207008/version/20210131\",\"system\":\"http://snomed.info/sct\"},\"J07BX03\":{\"display\":\"covid-19vaccines\",\"lang\":\"en\",\"active\":true,\"version\":\"2021-01\",\"system\":\"http://www.whocc.no/atc\"}}}";
    public static final String VALUESET_3_ID = "sct-vaccines-covid-19";
    public static final String VALUESET_3_HASH =
        "d28f1154e7f22b7a83a257e0f3c537ba427647d14bc15c9a2f217671ee7913ca";

    public static final String SIGNATURE_SERVICE_MOCKDATA =
        "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";

    public static final String VALIDATION_RULE_CMS =
        "MIIO7gYJKoZIhvcNAQcCoIIO3zCCDtsCAQExDzANBglghkgBZQMEAgEFADCCBRMGCSqGSIb3DQEHAaCCBQQEggUAeyJJZGVudGlmaWVyIjogIlRSLURYLTAwMDIiLCAiVHlwZSI6ICJBY2NlcHRhbmNlIiwgIkNvdW50cnkiOiAiRFgiLCAiVmVyc2lvbiI6ICIxLjAuMCIsICJTY2hlbWFWZXJzaW9uIjogIjEuMC4wIiwgIkVuZ2luZSI6ICJDRVJUTE9HSUMiLCAiRW5naW5lVmVyc2lvbiI6ICIwLjcuNSIsICJDZXJ0aWZpY2F0ZVR5cGUiOiAiVGVzdCIsICJEZXNjcmlwdGlvbiI6IFt7ImxhbmciOiAiZW4iLCAiZGVzYyI6ICJUaGUgc2FtcGxlIGZvciBhbiBhbnRpZ2VuIHRlc3QgKGUuZy4sIHJhcGlkIHRlc3QpIG11c3QgaGF2ZSBiZWVuIHRha2VuIG5vIGxvbmdlciB0aGFuIDM2IGhvdXJzIGFnby4ifSwgeyJsYW5nIjogImRlIiwgImRlc2MiOiAiRGllIFByb2JlbmFobWUgZlx1MDBmY3IgZWluZW4gQW50aWdlbi1UZXN0ICh6LkIuIFNjaG5lbGx0ZXN0KSBkYXJmIG1heGltYWwgMzYgU3R1bmRlbiB6dXJcdTAwZmNja2xpZWdlbi4ifSwgeyJsYW5nIjogImZyIiwgImRlc2MiOiAiTGUgcHJcdTAwZTlsXHUwMGU4dmVtZW50IHBvdXIgdW4gdGVzdCBhbnRpZ1x1MDBlOW5pcXVlIChwLiBleC4gdGVzdCByYXBpZGUpIG5lIGRvaXQgcGFzIGRhdGVyIGRlIHBsdXMgZGUgMzYgaGV1cmVzLiJ9LCB7ImxhbmciOiAiZXMiLCAiZGVzYyI6ICJEZWJlbiBoYWJlciB0cmFuc2N1cnJpZG8gMzYgaG9yYXMgY29tbyBtXHUwMGUxeGltbyBkZXNkZSBsYSBleHRyYWNjaVx1MDBmM24gcGFyYSB1bmEgcHJ1ZWJhIGRlIGFudFx1MDBlZGdlbm9zIChwb3IgZWplbXBsbywgdW4gYXV0b3Rlc3Qgclx1MDBlMXBpZG8pLiJ9LCB7ImxhbmciOiAiaXQiLCAiZGVzYyI6ICJJbCBjYW1waW9uZSBwZXIgaWwgdGVzdCBhbnRpZ2VuaWNvICh0ZXN0IHJhcGlkbykgZGV2ZSBlc3NlcmUgc3RhdG8gcmlsZXZhdG8gbmVsbGUgdWx0aW1lIDM2IG9yZS4ifV0sICJWYWxpZEZyb20iOiAiMjAyMS0wNy0xNlQwMDowMDowMFoiLCAiVmFsaWRUbyI6ICIyMDMwLTA2LTAxVDAwOjAwOjAwWiIsICJBZmZlY3RlZEZpZWxkcyI6IFsidC4wIiwgInQuMC5zYyIsICJ0LjAudHQiXSwgIkxvZ2ljIjogeyJpZiI6IFt7InZhciI6ICJwYXlsb2FkLnQuMCJ9LCB7ImlmIjogW3siPT09IjogW3sidmFyIjogInBheWxvYWQudC4wLnR0In0sICJMUDIxNzE5OC0zIl19LCB7Im5vdC1hZnRlciI6IFt7InBsdXNUaW1lIjogW3sidmFyIjogImV4dGVybmFsLnZhbGlkYXRpb25DbG9jayJ9LCAwLCAiZGF5Il19LCB7InBsdXNUaW1lIjogW3sidmFyIjogInBheWxvYWQudC4wLnNjIn0sIDM2LCAiaG91ciJdfV19LCB0cnVlXX0sIHRydWVdfX2gggXXMIIF0zCCA7sCFBocTzyrb38kV";
    public static final String RULE_TO_UPLOAD =
        "{\"Identifier\":\"GR-IT-0001\",\"Type\":\"Acceptance\",\"Country\":\"IT\",\"Version\":\"1.0.0\",\"SchemaVersion\":\"1.0.0\",\"Engine\":\"CERTLOGIC\",\"EngineVersion\":\"0.7.5\",\"CertificateType\":\"General\",\"Description\":[{\"lang\":\"en\",\"desc\":\"The\\\"diseaseoragenttargeted\\\"mustbeCOVID-19ofthevaluesetlist.\"}],\"ValidFrom\":\"2021-08-02T00:00:00Z\",\"ValidTo\":\"2030-06-01T00:00:00Z\",\"AffectedFields\":[\"r.0\",\"r.0.tg\",\"t.0\",\"t.0.tg\",\"v.0\",\"v.0.tg\"],\"Logic\":{\"and\":[{\"if\":[{\"var\":\"payload.r.0\"},{\"in\":[{\"var\":\"payload.r.0.tg\"},{\"var\":\"external.valueSets.disease-agent-targeted\"}]},true]},{\"if\":[{\"var\":\"payload.t.0\"},{\"in\":[{\"var\":\"payload.t.0.tg\"},{\"var\":\"external.valueSets.disease-agent-targeted\"}]},true]},{\"if\":[{\"var\":\"payload.v.0\"},{\"in\":[{\"var\":\"payload.v.0.tg\"},{\"var\":\"external.valueSets.disease-agent-targeted\"}]},true]}]}}";
    public static final String RULE_TO_UPLOAD_HASH =
        "6af3d05a9a1aa56b5b69d59dd18637dc1ab8826a16725460f43fd05022763d9as";

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

        return new RestApiResponse<List<TrustListItemDto>>(
            HttpStatus.OK,
            null,
            trustList
        );
    }

    public static RestApiResponse<String> getEmptyCountryListResponse() {
        return new RestApiResponse<String>(HttpStatus.NO_CONTENT, null, null);
    }

    public static RestApiResponse<List<String>> getEmptyValueSetsResponse() {
        return new RestApiResponse<List<String>>(
            HttpStatus.NO_CONTENT,
            null,
            null
        );
    }

    public static RestApiResponse<List<TrustListItemDto>> getEmptyUploadCertsResponse() {
        return new RestApiResponse<List<TrustListItemDto>>(
            HttpStatus.NO_CONTENT,
            null,
            null
        );
    }

    public static RestApiResponse<List<TrustListItemDto>> getEmptyTrustListResponse() {
        return new RestApiResponse<List<TrustListItemDto>>(
            HttpStatus.NO_CONTENT,
            null,
            null
        );
    }

    public static RestApiResponse<String> getCountryList() {
        return new RestApiResponse<String>(HttpStatus.OK, null, COUNTRY_LIST);
    }

    public static RestApiResponse<String> getCountryListItOnly() {
        return new RestApiResponse<String>(
            HttpStatus.OK,
            null,
            COUNTRY_LIST_EU_ONLY
        );
    }

    public static RestApiResponse<List<String>> getValueSetIdsResponse() {
        return new RestApiResponse<List<String>>(
            HttpStatus.OK,
            null,
            new ArrayList<String>(
                Arrays.asList(VALUESET_1_ID, VALUESET_2_ID, VALUESET_3_ID)
            )
        );
    }

    public static List<String> getValueSetValuesResponse() {
        return new ArrayList<String>(
            Arrays.asList(VALUESET_1, VALUESET_2, VALUESET_3)
        );
    }

    public static RestApiResponse<List<TrustListItemDto>> getUploadCertsResponse() {
        ArrayList<TrustListItemDto> trustList = new ArrayList<TrustListItemDto>();

        TrustListItemDto upload = new TrustListItemDto();
        upload.setRawData(DgcWorkerTestHelper.UPLOAD_TO_DOWNLOAD_RAW_DATA);
        upload.setKid(DgcWorkerTestHelper.UPLOAD_TO_DOWNLOAD_KID);
        upload.setSignature(DgcWorkerTestHelper.UPLOAD_TO_DOWNLOAD_SIGNATURE);
        upload.setThumbprint(DgcWorkerTestHelper.UPLOAD_TO_DOWNLOAD_THUMBPRINT);
        upload.setCertificateType(CertificateType.UPLOAD);
        upload.setTimestamp(new Date());
        upload.setCountry("IT");
        trustList.add(upload);

        return new RestApiResponse<List<TrustListItemDto>>(
            HttpStatus.OK,
            null,
            trustList
        );
    }

    public static RestApiResponse<Map<String, List<ValidationRuleDto>>> getRulesResponse() {
        Map<String, List<ValidationRuleDto>> rulesMap = new HashMap<String, List<ValidationRuleDto>>();
        ArrayList<ValidationRuleDto> rules = new ArrayList<ValidationRuleDto>();

        ValidationRuleDto validationRule = new ValidationRuleDto();
        validationRule.setCms(VALIDATION_RULE_CMS);
        Calendar validTo = Calendar.getInstance();
        validTo.add(Calendar.MONTH, 1);
        Calendar validFrom = Calendar.getInstance();
        validFrom.add(Calendar.DAY_OF_MONTH, -1);
        validationRule.setValidTo(validTo.getTime());
        validationRule.setValidFrom(validFrom.getTime());
        validationRule.setVersion("1.0.0");
        rules.add(validationRule);

        rulesMap.put("GR-EU-0001", rules);
        return new RestApiResponse<Map<String, List<ValidationRuleDto>>>(
            HttpStatus.OK,
            null,
            rulesMap
        );
    }

    public static ValidationRule getDummyValidationRule() {
        ValidationRule validationRule = new ValidationRule();

        JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

        validationRule.setLogic(
            jsonNodeFactory
                .objectNode()
                .set("field1", jsonNodeFactory.textNode("value1"))
        );
        Calendar validTo = Calendar.getInstance();
        validTo.add(Calendar.MONTH, 1);
        Calendar validFrom = Calendar.getInstance();
        validFrom.add(Calendar.DAY_OF_MONTH, -1);
        validationRule.setValidTo(validTo.getTime());
        validationRule.setValidFrom(validFrom.getTime());
        validationRule.setCertificateType("General");
        validationRule.setDescription(
            List.of(new ValidationRule.DescriptionItem("en", "de".repeat(10)))
        );
        validationRule.setEngine("CERTLOGIC");
        validationRule.setEngineVersion("1.0.0");
        validationRule.setVersion("1.0.0");
        validationRule.setAffectedFields(List.of("AB", "DE"));
        validationRule.setRegion("BW");
        validationRule.setSchemaVersion("1.0.0");
        validationRule.setType("Acceptance");
        validationRule.setIdentifier("GR-EU-0001");
        validationRule.setRawJson("{}");
        validationRule.setCountry("EU");

        return validationRule;
    }
}
