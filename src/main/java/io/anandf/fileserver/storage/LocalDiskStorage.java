package io.anandf.fileserver.storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anandf.fileserver.FileMetadata;
import io.anandf.fileserver.Status;
import io.anandf.fileserver.util.Checksum;
import io.anandf.fileserver.util.StorageUtils;
/**
 * Storage class to store the files on a local disk
 * 
 * @author anandf
 *
 */
public class LocalDiskStorage implements StorageProvider {

	private static final Logger LOG = LoggerFactory.getLogger(LocalDiskStorage.class.getName());
	
	private String storagePath;
	
	/**
	 * Constructor:
	 * Initialize the storage directory in which the uploaded files would be stored.
	 */
	public LocalDiskStorage() {
		// Initialize the storage path to store the uploaded files. If environment variable is not set
		// use the OS defined directory for storing temporary files. 
		// NOTE: Do not use temporary directory in Production env.
		this.storagePath = Optional.ofNullable(System.getenv("DATA_STORAGE_PATH")).orElse(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public FileMetadata storeFile(String fileName, InputStream stream) throws IOException {
		String targetFilePath = storagePath + "/" + fileName;
		LOG.info("Creating file:" + targetFilePath);
		
		long byteCount = writeStreamToFile(targetFilePath, stream);
		
		FileMetadata file = new FileMetadata();
		file.setName(fileName);
		file.setSize(byteCount);
		file.setStatus(Status.COMMITED);
		file.setChecksum(Checksum.calculateChecksum(targetFilePath));
		return file;

	}

	@Override
	public void prepareForMultipartUpload(String fileName, int partitionCount, long fileSize,
			String checksum) throws IOException {
		File targetDir = new File(storagePath + "/" + fileName);
		FileUtils.forceMkdir(targetDir);
	}

	/**
	 * Store the contents of the partition in the input stream for the given file.
	 */
	@Override
	public void storePartitionForFile(String fileName, String partitionId, InputStream stream) throws IOException {
		String targetFilePath = storagePath + "/" + fileName + "/" + partitionId + "_" + fileName;
		LOG.info("Creating file:" + targetFilePath);
		writeStreamToFile(targetFilePath, stream);
	}

	/**
	 * Merge multiple partitions of the file that was uploaded 
	 */
	@Override
	public void mergePartitionsForFile(String fileName) throws IOException {
		
		File targetDir = new File(storagePath + "/" + fileName);
		File mergedFile = new File(storagePath + "/" + "merged_" + fileName );
		Collection<File> partitionFiles = FileUtils.listFiles(targetDir, new SuffixFileFilter(fileName), null);
		List<File> sortedPartitionFiles = partitionFiles.stream().sorted().collect(Collectors.toList());
		StorageUtils.joinFiles(mergedFile, sortedPartitionFiles);
	}

	/**
	 * Checks if the merged file checksum matches with what was computed by the client before 
	 * initiating the multi part upload.
	 */
	@Override
	public boolean isFileValid() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}
	
	private long writeStreamToFile(String targetFilePath, InputStream stream) throws IOException {
		long byteCount = 0;
		try (FileOutputStream fstream = new FileOutputStream(targetFilePath);
				DataInputStream dis = new DataInputStream(stream)) {

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
		return byteCount;
	}

}
