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
package it.interop.dgc.gateway.client.base;

import it.interop.dgc.gateway.client.RestApiClient;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RestApiResponse<T> {

    private final HttpStatus statusCode;
    private final Map<String, List<String>> headers;
    private final T data;

    public RestApiResponse(
        HttpStatus statusCode,
        Map<String, List<String>> headers
    ) {
        this(statusCode, headers, null);
    }

    public RestApiResponse(
        HttpStatus statusCode,
        Map<String, List<String>> headers,
        T data
    ) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.data = data;
    }

    public String getBatchTag() {
        List<String> header = getHeaders().get(RestApiClient.BATCH_TAG);
        String batchTag = (header != null && header.size() > 0)
            ? header.get(0)
            : null;
        return "null".equalsIgnoreCase(batchTag) ? null : batchTag;
    }

    public String getNextBatchTag() {
        List<String> header = getHeaders().get(RestApiClient.NEXT_BATCH_TAG);
        String netBatchTag = (header != null && header.size() > 0)
            ? header.get(0)
            : null;
        return "null".equalsIgnoreCase(netBatchTag) ? null : netBatchTag;
    }
}
