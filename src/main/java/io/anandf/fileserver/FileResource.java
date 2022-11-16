
package io.anandf.fileserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anandf.fileserver.storage.StorageProvider;
import io.anandf.fileserver.storage.StorageProviderFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * This class implements REST endpoints to interact with File Uploads. The
 * following operations are supported:
 *
 * <ul>
 * <li>GET /v1/files: Retrieve list of all files that were uploaded</li>
 * <li>GET /v1/files/{id}: Retrieve single file by ID</li>
 * <li>GET /v1/files/name/{name}: Retrieve single file by name</li>
 * <li>GET /v1/files/cksum/{cksum}: Retrieve files that has the given checksum</li>
 * <li>POST /v1/files/uploadStream?name: Store the file as a single byte stream with the given name </li>
 * <li>POST /v1/files/initiateMultiPartitionUpload: Initiate a multipart upload for the given <code>FileMetadata</code> object. </li>
 * <li>POST /v1/files/uploadMultiPartitionStream: Upload a partition stream for a given partition of the file. </li>
 * <li>POST /v1/files/commitMultiPartitionUpload: Indicates that all the partitions are transfered, and its safe to merge the partitions. </li>
 *
 * </ul>
 *
 * Retrieve files and create new file create request.
 */
@Path("v1/files")
public class FileResource {

	private static final Logger LOG = LoggerFactory.getLogger(FileResource.class.getName());

	@PersistenceContext(unitName = "pu1")
	private EntityManager entityManager;

	private StorageProvider storage = StorageProviderFactory.getDefaultStorage();

	/**
	 * Lists all the files that were uploaded to this server
	 * @return list of <code>FileMetadata</code> objects
	 * Code - Description
	 * 200  - Successful retrival of the list of files.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<FileMetadata> getFiles() {
		return entityManager.createNamedQuery("getFiles", FileMetadata.class).getResultList();
	}

	/**
	 * Returns the <code>FileMetadata</code> object for the file with the given id
	 * @param id unique id of the file
	 * @return the <code>FileMetadata</code> object for the file with the given id
	 */
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public FileMetadata getFileById(@PathParam("id") String id) {
		FileMetadata file = entityManager.find(FileMetadata.class, Integer.valueOf(id));
		if (file == null) {
			throw new NotFoundException("Unable to find file with ID " + id);
		}
		return file;
	}

	/**
	 * Deletes the file with the given id
	 * @param id of the file to be deleted.
	 * Code - Description
	 * 204  - file successfully deleted
	 * 404  - if a file with the given id could not be found
	 * 
	 */ 
	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public void deleteFile(@PathParam("id") String id) {
		FileMetadata file = getFileById(id);
		if (file == null) {
			throw new NotFoundException("File with id '" + id + "' not found");
		}
		entityManager.remove(file);
	}
	
	
	/**
	 * Deletes the file with the given name
	 * @param name of the file to be deleted.
	 * Code - Description
	 * 204  - file successfully deleted
	 * 404  - if a file with the given id could not be found
	 * 
	 */ 
	@DELETE
	@Path("name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public void deleteFileByName(@PathParam("name") String name) {
		FileMetadata file = getFileByName(name);
		if (file == null) {
			throw new NotFoundException("File with id '" + name + "' not found");
		}
		entityManager.remove(file);
	}

	/**
	 * Returns the metadata of the file with the given name
	 * @param name name of the file whose metadata is required
	 * @return
	 * Code - Description
	 * 200  - returns the file metadata for the given name
	 * 404  - if a file with the given name could not be found
	 */
	@GET
	@Path("name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public FileMetadata getFileByName(@PathParam("name") String name) {
		TypedQuery<FileMetadata> query = entityManager.createNamedQuery("getFileByName", FileMetadata.class);
		List<FileMetadata> list = query.setParameter("name", name).getResultList();
		if (list.isEmpty()) {
			throw new NotFoundException("Unable to find file with name " + name);
		}
		return list.get(0);
	}

	/**
	 * Returns the file name matching the given checksum. This method can be used if the file contents
	 * are already present with the same contents. If multiple file names are found for the same contents,
	 * then the first entry is returned.
	 * @param checksum string to used for the querying
	 * @return an instance of <code>FileMetadata</code>
	 */
	@GET
	@Path("cksum/{cksum}")
	@Produces(MediaType.APPLICATION_JSON)
	public FileMetadata getFileByChecksum(@PathParam("cksum") String checksum) {
		TypedQuery<FileMetadata> query = entityManager.createNamedQuery("getFileByChecksum", FileMetadata.class);
		List<FileMetadata> list = query.setParameter("name", checksum).getResultList();
		if (list.isEmpty()) {
			throw new NotFoundException("Unable to find file with name " + checksum);
		}
		return list.get(0);
	}

	/**
	 * Upload the file with the given name and the contents of the file passed as stream
	 * @param name of the file
	 * @param payload contents of the file sent as octet stream media type
	 * @throws IOException thrown when there is an error reading from the payload stream
	 */
	@POST
	@Path("/uploadStream")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Transactional(Transactional.TxType.REQUIRED)
	public void uploadStream(@QueryParam("name") String fileName, InputStream payload) throws IOException {
		try {
			if (fileName == null || fileName.isEmpty()) {
				throw new BadRequestException("Missing mandatory query param 'name'");
			}
			FileMetadata file = storage.storeFile(fileName, payload);
			entityManager.persist(file);
		} catch (IOException ex) {
			LOG.error("error storing file", ex);
			throw ex;
		}
	}
	
	/**
	 * 
	 * @param file to be uploaded as multipartition
	 * Code - Description
	 */
	@POST
	@Path("/initiateMultiPartitionUpload")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public void initiateMultiPartitionUpload(FileMetadata file) throws IOException {
		entityManager.persist(file);
	}
	
	/**
	 * Uploads the stream of data for a single partition
	 * @param fileName name of the file
	 * @param partition id of the stream being transfered.
	 * @param payload
	 * @throws IOException
	 */

	@POST
	@Path("/uploadMultiPartitionStream")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Transactional(Transactional.TxType.REQUIRED)
	public void uploadStream(@QueryParam("name") String fileName, @QueryParam("partition") int partition,
			InputStream payload) throws IOException {
		try {
			if (fileName == null || fileName.isEmpty()) {
				throw new BadRequestException("Missing mandatory query param 'name'");
			}
			FileMetadata file = storage.storeFile(fileName, payload);
			entityManager.persist(file);
		} catch (IOException ex) {
			LOG.error("error storing file", ex);
			throw ex;
		}
	}
	
	@POST
	@Path("/commitMultiPartitionUpload")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public void commit(@QueryParam("name")String fileName) throws IOException {
		storage.mergePartitionsForFile(fileName);
	}


}
