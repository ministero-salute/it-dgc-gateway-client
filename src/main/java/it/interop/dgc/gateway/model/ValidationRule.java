/*-
 * ---license-start
 * EU Digital Green Certificate Gateway Service / dgc-lib
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */
package it.interop.dgc.gateway.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ValidationRule {

    @JsonProperty("Identifier")
    private String identifier;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Region")
    private String region;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("SchemaVersion")
    private String schemaVersion;

    @JsonProperty("Engine")
    private String engine;

    @JsonProperty("EngineVersion")
    private String engineVersion;

    @JsonProperty("CertificateType")
    private String certificateType;

    @JsonProperty("Description")
    private List<DescriptionItem> description;

    @JsonProperty("ValidFrom")
    private Date validFrom;

    @JsonProperty("ValidTo")
    private Date validTo;

    @JsonProperty("AffectedFields")
    private List<String> affectedFields;

    @JsonProperty("Logic")
    private JsonNode logic;

    @JsonIgnore
    private String rawJson;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DescriptionItem {

        @JsonProperty("lang")
        String language;

        @JsonProperty("desc")
        String description;
    }
}
