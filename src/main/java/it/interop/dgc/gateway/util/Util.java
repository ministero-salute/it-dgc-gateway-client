package it.interop.dgc.gateway.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.interop.dgc.gateway.entity.DgcLogEntity.OperationType;


public class Util {

	public static String batchTagGenerator(OperationType operation) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		return operation.name().concat("-").concat(df.format(new Date()));
	}
}
