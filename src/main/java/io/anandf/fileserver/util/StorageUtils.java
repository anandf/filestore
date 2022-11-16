package io.anandf.fileserver.util;

import java.io.*;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * StorageUtils contains utility functions used for managing the file related operations 
 * that needs to be performed while storing the uploaded files.
 * @author anandf
 *
 */
public class StorageUtils {
	/**
	 * Joins multiple file partitions into a single file by writing the contents in the given order
	 * @param destination location of the merged file with contents from all the individual parts.
	 * @param parts individual part of the file.
	 * @throws IOException thrown when there is an error reading and writing from/to the file.
	 */
	public static void joinFiles(File destination, List<File> parts)
            throws IOException {
        try (OutputStream output = createAppendableFileStream(destination)){
            for (File aFilePart : parts) {
                appendFile(output, aFilePart);
            }
        } 
    }
	
	/**
	 * Creates a file stream which can be appended
	 * @param destination location of the merged file 
	 * @return output stream which can be used to write the individual parts.
	 * @throws FileNotFoundException if the destination file path is not found.
	 */
    private static BufferedOutputStream createAppendableFileStream(File destination)
            throws FileNotFoundException {
        return new BufferedOutputStream(new FileOutputStream(destination, true));
    }

    /**
     * Write the contents of single partition to the output stream.
     * @param output stream to write the contents.
     * @param source individual part whose contents have to be written to the destination.
     * @throws IOException thrown when there is an error reading/writing the contents.
     */
    private static void appendFile(OutputStream output, File source)
            throws IOException {
        try (InputStream input = new BufferedInputStream(new FileInputStream(source))) {
            IOUtils.copy(input, output);
        }
    }
    

}
