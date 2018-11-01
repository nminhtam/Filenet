/**
 *
 */
package lu.mtn.ibm.filenet.client.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author NguyenT
 *
 */
public class CEDocument implements Serializable {

    public static final String CLASS_DOCUMENT = "Document";
    public static final String PROP_ID = "Id";
    public static final String PROP_CONTENT_ELEMENTS = "ContentElements";
    public static final String PROP_DOC_TITLE = "DocumentTitle";

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String id;

    private Map<String, Object> properties;

    private List<CEDocumentContent> contents;
    
    private String documentClass;


    /**
     *
     */
    public CEDocument() {
        this.properties = new HashMap<String, Object>();
    }


    /**
     * @return the id
     */
    public String getId() {
        return this.id;
    }


    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }


    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * @return the documentClass
     */
    public String getDocumentClass() {
        return this.documentClass;
    }

    /**
     * @param documentClass the documentClass to set
     */
    public void setDocumentClass(String documentClass) {
        this.documentClass = documentClass;
    }

    /**
     * @return the contents
     */
    public List<CEDocumentContent> getContents() {
        return this.contents;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(List<CEDocumentContent> contents) {
        this.contents = contents;
    }

    public boolean isContentsInitialized() {
        return this.contents != null;
    }

    /**
     * @param property
     * @return
     */
    public Object get(String property) {
        return this.properties != null ? this.properties.get(property) : null;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CEDocument other = (CEDocument) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
