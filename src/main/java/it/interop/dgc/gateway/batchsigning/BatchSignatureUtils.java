/*-
 *   Copyright (C) 2020 Presidenza del Consiglio dei Ministri.
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
package it.interop.dgc.gateway.batchsigning;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Base64;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides help methods used by {@link BatchSignatureVerifier} to
 * verify a batch signature.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchSignatureUtils {

	/**
	 * Extracts the information (e.g., keyData, rollingPeriod, origin, etc.) from a
	 * {@link DiagnosisKeyBatch} object, and generates with it a byte stream used to
	 * verify the batch signature. The created byte stream has an order defined in
	 * the Federation Gateway specification.
	 *
	 * @param batch the diagnosis key batch, from which the information to generate
	 *              the bytes to verify are obtained.
	 * @return the bytes that will be used to verify the batch signature.
	 */
//	public static byte[] generateBytesToVerify(final DiagnosisKeyBatch batch) {
//		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//
//		batch.getKeysList().stream().map(BatchSignatureUtils::generateBytesToVerify)
//				.sorted(Comparator.nullsLast(Comparator.comparing(BatchSignatureUtils::bytesToBase64)))
//				.forEach(byteArrayOutputStream::writeBytes);
//
//		return byteArrayOutputStream.toByteArray();
//	}

	/**
	 * Extracts the information (e.g., keyData, rollingPeriod, origin, etc.) from a
	 * {@link DiagnosisKey} object, and generates with it a byte stream used to
	 * verify the batch signature for one entity. The created byte stream has an
	 * order defined in the Federation Gateway specification.
	 *
	 * @param diagnosisKey the diagnosis key, from which the information to generate
	 *                     the bytes to verify are obtained.
	 * @return the bytes that will be used to verify the key signature.
	 */
//	public static byte[] generateBytesToVerify(final DiagnosisKey diagnosisKey) {
//		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//		writeBytesInByteArray(diagnosisKey.getKeyData(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		writeIntInByteArray(diagnosisKey.getRollingStartIntervalNumber(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		writeIntInByteArray(diagnosisKey.getRollingPeriod(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		writeIntInByteArray(diagnosisKey.getTransmissionRiskLevel(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		writeVisitedCountriesInByteArray(diagnosisKey.getVisitedCountriesList(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		writeB64StringInByteArray(diagnosisKey.getOrigin(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		writeIntInByteArray(diagnosisKey.getReportTypeValue(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		writeIntInByteArray(diagnosisKey.getDaysSinceOnsetOfSymptoms(), byteArrayOutputStream);
//		writeSeperatorInArray(byteArrayOutputStream);
//		return byteArrayOutputStream.toByteArray();
//	}

	/**
	 * Converts a Base64 string into a byte array.
	 *
	 * @param batchSignatureBase64 the base64 string of the batch signature.
	 * @return the batch signature decoded as byte array. Returns an empty array if
	 *         conversion failed.
	 */
	static byte[] b64ToBytes(final String batchSignatureBase64) {
		return b64ToBytes(batchSignatureBase64.getBytes());
	}

	static byte[] b64ToBytes(final byte[] bytes) {
		try {
			return Base64.getDecoder().decode(bytes);
		} catch (IllegalArgumentException e) {
			log.error("Failed to convert base64 to byte array");
			return new byte[0];
		}
	}

	static String bytesToBase64(byte[] bytes) {
		try {
			return Base64.getEncoder().encodeToString(bytes);
		} catch (IllegalArgumentException e) {
			log.error("Failed to convert byte array to string");
			return null;
		}
	}

	private static void writeSeperatorInArray(final ByteArrayOutputStream byteArray) {
		byteArray.writeBytes(".".getBytes(StandardCharsets.US_ASCII));
	}

	private static void writeStringInByteArray(final String batchString, final ByteArrayOutputStream byteArray) {
		byteArray.writeBytes(batchString.getBytes(StandardCharsets.US_ASCII));
	}

	private static void writeB64StringInByteArray(final String batchString, final ByteArrayOutputStream byteArray) {
		String base64String = bytesToBase64(batchString.getBytes(StandardCharsets.US_ASCII));

		if (base64String != null) {
			writeStringInByteArray(base64String, byteArray);
		}
	}

	private static void writeIntInByteArray(final int batchInt, final ByteArrayOutputStream byteArray) {
		String base64String = bytesToBase64(ByteBuffer.allocate(4).putInt(batchInt).array());

		if (base64String != null) {
			writeStringInByteArray(base64String, byteArray);
		}
	}

	private static void writeBytesInByteArray(final ByteString bytes, ByteArrayOutputStream byteArray) {
		String base64String = bytesToBase64(bytes.toByteArray());

		if (base64String != null) {
			writeStringInByteArray(base64String, byteArray);
		}
	}

	private static void writeVisitedCountriesInByteArray(final ProtocolStringList countries,
			final ByteArrayOutputStream byteArray) {
		writeB64StringInByteArray(String.join(",", countries), byteArray);
	}

	public static String x509CertificateToPem(final X509Certificate cert) throws IOException {
		final StringWriter writer = new StringWriter();
		final JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
		pemWriter.writeObject(cert);
		pemWriter.flush();
		pemWriter.close();
		return writer.toString().replaceAll("\r\n", "\n");
	}

}
