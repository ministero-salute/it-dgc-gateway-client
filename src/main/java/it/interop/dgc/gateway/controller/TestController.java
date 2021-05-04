/*-
 *   Copyright (C) 2021 Presidenza del Consiglio dei Ministri.
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
package it.interop.dgc.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import it.interop.dgc.gateway.client.RestApiClient;
import it.interop.dgc.gateway.signing.CertificateSignatureVerifier;
import it.interop.dgc.gateway.signing.SignatureGenerator;
import it.interop.dgc.gateway.worker.DgcWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TestController {

	@Autowired(required=true)
	RestApiClient client;

	@Autowired(required=true)
	private SignatureGenerator signatureGenerator;
	
	@Autowired(required=true)
	private CertificateSignatureVerifier batchSignatureVerifier;

	@Autowired(required=true)
	private DgcWorker efgsWorker;

	
	@GetMapping("/testUpload")
	public ResponseEntity<String> testUpload() {
		StringBuffer content = new StringBuffer();
		try {
			
			efgsWorker.uploadWorker();
			log.info("OK");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			content.append("Errore: ").append(e.getMessage()).append("<br>");
		}
		return new ResponseEntity<String>(content.toString(), HttpStatus.OK);		
	}


	@GetMapping("/testDownload")
	public ResponseEntity<String> testDownload() {
		StringBuffer content = new StringBuffer();
		try {
			
			efgsWorker.downloadWorker();
			log.info("OK");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			content.append("Errore: ").append(e.getMessage()).append("<br>");
		}
		return new ResponseEntity<String>(content.toString(), HttpStatus.OK);		
	}

	
	
}
