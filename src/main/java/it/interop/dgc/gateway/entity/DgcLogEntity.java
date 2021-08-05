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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@CompoundIndexes(
    {
        @CompoundIndex(
            name = "dgc_log_country_batch_tag",
            def = "{'country' : 1, 'batch_tag': 1, 'index':1}",
            unique = false
        ),
    }
)
@Document(collection = "dgc_log")
public class DgcLogEntity implements Serializable {

    private static final long serialVersionUID = 2330473769691102540L;

    @Id
    private String id;

    @Field(name = "batch_tag")
    private String batchTag;

    @Field("operation")
    private OperationType operation;

    @Field("country")
    private String country;

    @Field("log_info")
    private List<DgcLogInfo> dgcLogInfoList;

    @Field("log_amount")
    private DgcLogAmount dgcLogAmount;

    @Field("execution")
    private Date execution;

    @Field("execution_report")
    private String executionReport;

    @Field("execution_akamai_report")
    private String executionAkamaiReport;

    public enum OperationType {
        UPLOAD,
        DOWNLOAD,
        REVOKE,
    }

    public DgcLogEntity() {}

    private DgcLogEntity(
        OperationType operation,
        String country,
        String batchTag,
        Date execution,
        String executionReport,
        String executionAkamaiReport
    ) {
        this.batchTag = batchTag;
        this.operation = operation;
        this.country = country;
        this.execution = execution;
        this.executionReport = executionReport;
        this.executionAkamaiReport = executionAkamaiReport;
    }

    public static DgcLogEntity buildUploadDgcLog(
        String country,
        String batchTag,
        String executionReport,
        DgcLogInfo dgcLogInfo
    ) {
        DgcLogEntity dgcLogEntity = new DgcLogEntity(
            DgcLogEntity.OperationType.UPLOAD,
            country,
            batchTag,
            new Date(),
            executionReport,
            null
        );
        List<DgcLogInfo> dgcLogInfoList = new ArrayList<DgcLogInfo>();
        dgcLogInfoList.add(dgcLogInfo);
        dgcLogEntity.setDgcLogInfoList(dgcLogInfoList);
        return dgcLogEntity;
    }

    public static DgcLogEntity buildRevokeDgcLog(
        String country,
        String batchTag,
        String executionReport,
        DgcLogInfo dgcLogInfo
    ) {
        DgcLogEntity dgcLogEntity = new DgcLogEntity(
            DgcLogEntity.OperationType.REVOKE,
            country,
            batchTag,
            new Date(),
            executionReport,
            null
        );
        List<DgcLogInfo> dgcLogInfoList = new ArrayList<DgcLogInfo>();
        dgcLogInfoList.add(dgcLogInfo);
        dgcLogEntity.setDgcLogInfoList(dgcLogInfoList);
        return dgcLogEntity;
    }

    public static DgcLogEntity buildDownloadDgcLog(
        String country,
        String batchTag,
        String executionReport,
        String executionAkamaiReport,
        List<DgcLogInfo> dgcLogInfoList,
        DgcLogAmount dgcLogAmount
    ) {
        DgcLogEntity dgcLogEntity = new DgcLogEntity(
            DgcLogEntity.OperationType.DOWNLOAD,
            country,
            batchTag,
            new Date(),
            executionReport,
            executionAkamaiReport
        );
        dgcLogEntity.dgcLogInfoList = dgcLogInfoList;
        dgcLogEntity.dgcLogAmount = dgcLogAmount;

        return dgcLogEntity;
    }
}
