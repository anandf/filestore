package io.anandf.fileserver.storage;

import java.io.InputStream;

import io.anandf.fileserver.FileMetadata;

import java.io.IOException;

/**
 * Interface to manage the storage of the files that are being uploaded to the
 * server from the client.
 * 
 * @author anandf
 *
 */
public interface StorageProvider {

	public FileMetadata storeFile(String fileName, InputStream stream) throws IOException;

	public void prepareForMultipartUpload(String fileName, int partitionCount, long fileSize,
			String checksum) throws IOException;
	
	public void storePartitionForFile(String fileName, String partitionId, InputStream stream) throws IOException;
	
	public void mergePartitionsForFile(String fileName)throws IOException;
	
	public boolean isFileValid() throws IOException;

}
