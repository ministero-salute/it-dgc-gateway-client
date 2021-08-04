/*-
 * ---license-start
 * eu-digital-green-certificates / dgca-businessrule-service
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

package it.interop.dgc.gateway.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@Document(collection = "country_list")
public class CountryListEntity implements Serializable {
	private static final long serialVersionUID = -3383002566473192925L;

	@Id
    private String id;

	@Field(name = "id")
    private Long countryListId;

    @Field(name = "raw_data")
    private String rawData;

    @Field(name = "hash")
    private String hash;

    @Field(name = "signature")
    private String signature;
    
    @CreatedDate
    @Field(name="created_at")
    private Date createdAt;

    @Field(name="batch_tag")
    private String downloadBatchTag;
}