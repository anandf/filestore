
package io.anandf.fileserver;

import com.fasterxml.jackson.annotation.JsonProperty;


import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A File entity class. A File is represented by an
 * ID, a name, along with its checksum, size
 *
 * File, that has been uploaded by the user
 */
@Entity(name = "File")
@Table(name = "FILE")
@Access(AccessType.PROPERTY)
@NamedQueries({
        @NamedQuery(name = "getFiles",
                    query = "SELECT f FROM File f"),
        @NamedQuery(name = "getFileByName",
        			query = "SELECT f FROM File f WHERE f.name = :name"),
        @NamedQuery(name = "getFileByChecksum",
                    query = "SELECT f FROM File f WHERE f.checksum = :checksum")
})
public class File {

    private int id;

    private String name;

    private String checksum;

    private long size;
    
    @Transient
    @JsonProperty(access=com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private byte[] contents;

    public File() {
    }

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic(optional = false)
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic(optional = false)
    @Column(name = "SIZE", nullable = false)
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
    
    @Basic(optional = false)
    @Column(name = "CKSUM", nullable = false)
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public byte[] getContents() {
    	return contents;
    }
    
    public void setContents(byte[] contents) {
    	this.contents = contents;
    }
}
