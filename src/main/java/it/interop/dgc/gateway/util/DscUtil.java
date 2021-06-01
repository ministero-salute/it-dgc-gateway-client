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
package it.interop.dgc.gateway.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.interop.dgc.gateway.entity.DgcLogEntity.OperationType;


public class DscUtil {
	public static Integer CONNECT_TIMEOUT_DEFAULT = 30000;
	public static Integer READ_TIMEOUT_DEFAULT = 30000;


	public static String batchTagGenerator(OperationType operation) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
		return operation.name().concat("-").concat(df.format(new Date()));
	}
	
	public static int parseWithDefault(String s, int defaultVal) {
	    return s.matches("-?\\d+") ? Integer.parseInt(s) : defaultVal;   
	}
}
