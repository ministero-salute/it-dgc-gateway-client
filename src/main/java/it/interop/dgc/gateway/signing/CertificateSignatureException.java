/*-
 *   Copyright (C) 2021 Ministero della Salute and all other contributors.
 *   Please refer to the AUTHORS file for more information.
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Affero General Public License for more details.
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.interop.dgc.gateway.signing;

import java.util.List;
import java.util.Map;

public class CertificateSignatureException extends Exception {

    private static final long serialVersionUID = 8799535509060274385L;
    private int code = 0;
    private Map<String, List<String>> responseHeaders = null;
    private String responseBody = null;

    public CertificateSignatureException() {}

    public CertificateSignatureException(Throwable throwable) {
        super(throwable);
    }

    public CertificateSignatureException(String message) {
        super(message);
    }

    public CertificateSignatureException(
        String message,
        Throwable throwable,
        int code,
        Map<String, List<String>> responseHeaders,
        String responseBody
    ) {
        super(message, throwable);
        this.code = code;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    public CertificateSignatureException(
        String message,
        int code,
        Map<String, List<String>> responseHeaders,
        String responseBody
    ) {
        this(message, (Throwable) null, code, responseHeaders, responseBody);
    }

    public CertificateSignatureException(
        String message,
        Throwable throwable,
        int code,
        Map<String, List<String>> responseHeaders
    ) {
        this(message, throwable, code, responseHeaders, null);
    }

    public CertificateSignatureException(
        int code,
        Map<String, List<String>> responseHeaders,
        String responseBody
    ) {
        this(
            (String) null,
            (Throwable) null,
            code,
            responseHeaders,
            responseBody
        );
    }

    public CertificateSignatureException(int code, String message) {
        super(message);
        this.code = code;
    }

    public CertificateSignatureException(
        int code,
        String message,
        Map<String, List<String>> responseHeaders,
        String responseBody
    ) {
        this(code, message);
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
    }

    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the HTTP response headers.
     *
     * @return A map of list of string
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Get the HTTP response body.
     *
     * @return Response body in the form of string
     */
    public String getResponseBody() {
        return responseBody;
    }
}
