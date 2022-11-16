package io.anandf.fileserver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Class contains utility methods for generating and validating checksums for
 * files being uploaded.
 * 
 * @author anandf
 *
 */
public class Checksum {

	private static final Logger LOG = LoggerFactory.getLogger(Checksum.class.getName());

	/**
	 * Calculate the MD5 checksum for the given file
	 * 
	 * @param filePath path to the file whose checksum needs to be calculated
	 * @return MD5 checksum of the given file.
	 * @throws IOException
	 */
	public static String calculateChecksum(String filePath) throws IOException {
		// Create checksum for this file
		File file = new File(filePath);

		// Use MD5 algorithm
		MessageDigest md5Digest = null;
		try {
			md5Digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ex) {
			LOG.error("MD5 checksum algorthim not found", ex);
			return "";
		}

		return getChecksumForFile(md5Digest, file);
	}

	/**
	 * 
	 * @param digest Digest object MD5, SHA256 etc.
	 * @param file   file whose checksum needs to be calculated.
	 * @return checksum string of the given file and the digest object provided.
	 * @throws IOException if the file does not exist or when there is an error
	 *                     reading the file.
	 */
	private static String getChecksumForFile(MessageDigest digest, File file) throws IOException {
		// Get file input stream for reading the file content
		FileInputStream fis = new FileInputStream(file);

		// Create byte array to read data in chunks
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		// Read file data and update in message digest
		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}

		// close the stream; We don't need it now.
		fis.close();

		byte[] bytes = digest.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

}