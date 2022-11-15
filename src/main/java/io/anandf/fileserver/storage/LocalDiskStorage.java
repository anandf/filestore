package io.anandf.fileserver.storage;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.LoggerFactory;

import io.anandf.fileserver.File;

import org.h2.util.IOUtils;
import org.slf4j.Logger;

import io.anandf.fileserver.File;
/**
 * Storage class to store the files on a local disk
 * 
 * @author anandf
 *
 */
public class LocalDiskStorage implements Storage {

	private static final Logger LOG = LoggerFactory.getLogger(LocalDiskStorage.class.getName());

	@Override
	public File storeFile(String fileName, InputStream stream) throws IOException {
		long byteCount = 0;
		try (FileOutputStream fstream = new FileOutputStream("/tmp/" + fileName);
				DataInputStream dis = new DataInputStream(stream)) {
			LOG.info("Creating file:" + fileName);
			while (true) {
				try {
					fstream.write(dis.readByte());
					byteCount++;
				} catch (Exception e) {
					break;
				}
			}
			fstream.flush();
		} finally {
			stream.close();
		}
		File file = new File();
		file.setName(fileName);
		file.setSize(byteCount);
		file.setChecksum("dummy");
		return file;

	}

}
