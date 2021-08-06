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

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class DgcLogAmount {

    @Field(name = "num_csca")
    private Integer numCsca = 0;

    @Field(name = "num_dsc")
    private Integer numDsc = 0;

    @Field(name = "num_new_csca")
    private Integer numNewCsca = 0;

    @Field(name = "num_new_dsc")
    private Integer numNewDsc = 0;

    @Field(name = "num_invalid_csca")
    private Integer numInvalidCsca = 0;

    @Field(name = "num_invalid_dsc")
    private Integer numInvalidDsc = 0;

    @Field(name = "num_revoked")
    private Integer numRevoked = 0;

    public Integer incNumCsca() {
        return ++numCsca;
    }

    public Integer incNumDsc() {
        return ++numDsc;
    }

    public Integer incNumNewCsca() {
        return ++numNewCsca;
    }

    public Integer incNumNewDsc() {
        return ++numNewDsc;
    }

    public Integer incNumInvalidCsca() {
        return ++numInvalidCsca;
    }

    public Integer incNumInvalidDsc() {
        return ++numInvalidDsc;
    }

    public Integer incNumRevoked() {
        return ++numRevoked;
    }
}
