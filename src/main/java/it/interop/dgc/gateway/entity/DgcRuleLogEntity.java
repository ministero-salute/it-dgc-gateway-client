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
package it.interop.dgc.gateway.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@CompoundIndexes({
    @CompoundIndex(name = "dgc_log_country_batch_tag", def = "{'country' : 1, 'batch_tag': 1, 'index':1}", unique = false)
})
@Document(collection = "dgc_rule_log")
public class DgcRuleLogEntity implements Serializable {
	private static final long serialVersionUID = 2330473769691102540L;

	@Id
	private String id;

    @Field(name="batch_tag")
    private String batchTag;

	@Field("operation")
	private OperationType operation;

	@Field("item_type")
	private ItemType itemType;

//	@Field("country")
//	private String country;

	@Field("log_info")
	private Map<String, List<DgcRuleLogInfo>> logInfoList;
	
	@Field("log_amount")
	private DgcRuleLogAmount logAmount;
	
	@Field("execution")
	private Date execution;

	@Field("execution_report")
  	private String executionReport;

	@Field("execution_akamai_report")
  	private String executionAkamaiReport;

	
	public enum OperationType {
		UPLOAD,
		DOWNLOAD,
		REVOKE
	}

	public enum ItemType {
		RULES,
		VALUES,
		COUNTRIES
	}

	public DgcRuleLogEntity() {
	}
	
	private DgcRuleLogEntity(OperationType operation, ItemType itemType, String batchTag,
			Date execution, String executionReport, String executionAkamaiReport) {
		this.batchTag = batchTag;
		this.operation = operation;
		this.itemType = itemType;
		this.execution = execution;
		this.executionReport = executionReport;
		this.executionAkamaiReport = executionAkamaiReport;
	}

	public static DgcRuleLogEntity buildUploadRuleLog(String batchTag, String executionReport) {
		DgcRuleLogEntity dgcLogEntity = new DgcRuleLogEntity(DgcRuleLogEntity.OperationType.UPLOAD, ItemType.RULES, batchTag,  new Date(), executionReport, null);
		return dgcLogEntity;
	}

	public static DgcRuleLogEntity buildRevokeRuleLog(String batchTag, String executionReport/*, DgcLogInfo dgcLogInfo*/) {
		DgcRuleLogEntity dgcLogEntity = new DgcRuleLogEntity(DgcRuleLogEntity.OperationType.REVOKE, ItemType.RULES, batchTag, new Date(), executionReport, null);
		return dgcLogEntity;
	}

	public static DgcRuleLogEntity buildDownloadCountyLog(String batchTag, String executionReport) {
		DgcRuleLogEntity dgcLogEntity = new DgcRuleLogEntity(DgcRuleLogEntity.OperationType.DOWNLOAD, ItemType.COUNTRIES, batchTag, new Date(), executionReport, null);
		return dgcLogEntity;
	}

	public static DgcRuleLogEntity buildDownloadValueLog(String batchTag, String executionReport) {
		DgcRuleLogEntity dgcLogEntity = new DgcRuleLogEntity(DgcRuleLogEntity.OperationType.DOWNLOAD, ItemType.VALUES, batchTag, new Date(), executionReport, null);
		return dgcLogEntity;
	}
	
	public static DgcRuleLogEntity buildDownloadRuleLog(String batchTag, String executionReport, String executionAkamaiReport,
			Map<String, List<DgcRuleLogInfo>> logInfoList, DgcRuleLogAmount logAmount) {
		DgcRuleLogEntity dgcLogEntity = new DgcRuleLogEntity(DgcRuleLogEntity.OperationType.DOWNLOAD, ItemType.RULES, batchTag, new Date(), executionReport, executionAkamaiReport);
		dgcLogEntity.logInfoList = logInfoList;
		dgcLogEntity.logAmount = logAmount;

		return dgcLogEntity;
	}
	
	
}
