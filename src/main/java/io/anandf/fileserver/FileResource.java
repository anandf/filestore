
package io.anandf.fileserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.anandf.fileserver.storage.Storage;
import io.anandf.fileserver.storage.StorageFactory;
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
 * <li>GET /files: Retrieve list of all files that were uploaded</li>
 * <li>GET /files/{id}: Retrieve single file by ID</li>
 * <li>GET /files/name/{name}: Retrieve single file by name</li>
 * <li>GET /files/cksum/{cksum}: Retrieve files that has the given checksum</li>
 * <li>POST /files?name: Store the file as octet stream with the given name
 * provided in the query param</li>
 * </ul>
 *
 * Retrieve files and create new file create request.
 */
@Path("files")
public class FileResource {

	private static final Logger LOG = LoggerFactory.getLogger(FileResource.class.getName());

	@PersistenceContext(unitName = "pu1")
	private EntityManager entityManager;

	private Storage storage = StorageFactory.getDefaultStorage();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<File> getFiles() {
		return entityManager.createNamedQuery("getFiles", File.class).getResultList();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public File getFileById(@PathParam("id") String id) {
		File file = entityManager.find(File.class, Integer.valueOf(id));
		if (file == null) {
			throw new NotFoundException("Unable to find file with ID " + id);
		}
		return file;
	}

	@DELETE
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public void deleteFile(@PathParam("id") String id) {
		File file = getFileById(id);
		entityManager.remove(file);
	}

	@DELETE
	@Path("name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public void deleteFileByName(@PathParam("name") String name) {
		File file = getFileByName(name);
		entityManager.remove(file);
	}

	@GET
	@Path("name/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public File getFileByName(@PathParam("name") String name) {
		TypedQuery<File> query = entityManager.createNamedQuery("getFileByName", File.class);
		List<File> list = query.setParameter("name", name).getResultList();
		if (list.isEmpty()) {
			throw new NotFoundException("Unable to find file with name " + name);
		}
		return list.get(0);
	}

	@GET
	@Path("cksum/{cksum}")
	@Produces(MediaType.APPLICATION_JSON)
	public File getFileByChecksum(@PathParam("cksum") String name) {
		TypedQuery<File> query = entityManager.createNamedQuery("getFileByChecksum", File.class);
		List<File> list = query.setParameter("name", name).getResultList();
		if (list.isEmpty()) {
			throw new NotFoundException("Unable to find file with name " + name);
		}
		return list.get(0);
	}

	@POST
	@Path("/uploadJson")
	@Consumes(MediaType.APPLICATION_JSON)
	@Transactional(Transactional.TxType.REQUIRED)
	public void createFile(File file) {
		try {
			LOG.info("file contents:" + new String(file.getContents()));
			entityManager.persist(file);
		} catch (Exception e) {
			throw new BadRequestException("Unable to create file with ID " + file.getId());
		}
	}

	@POST
	@Path("/uploadStream")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Transactional(Transactional.TxType.REQUIRED)
	public void uploadStream(@QueryParam("name") String fileName, InputStream payload) throws IOException {
		try {
			File file = storage.storeFile(fileName, payload);
			if (fileName == null || fileName.isEmpty()) {
				throw new BadRequestException("Missing mandatory query param 'name'");
			}
			entityManager.persist(file);
		} catch (IOException ex) {
			LOG.error("error storing file", ex);
			throw ex;
		}
	}

}
