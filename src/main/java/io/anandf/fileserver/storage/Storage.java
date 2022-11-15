package io.anandf.fileserver.storage;

import java.io.InputStream;

import io.anandf.fileserver.File;

import java.io.IOException;

/**
 * Interface definition to manage the storage of the files.
 * 
 * @author anandf
 *
 */
public interface Storage {
	
	public File storeFile(String fileName, InputStream stream) throws IOException;

}
