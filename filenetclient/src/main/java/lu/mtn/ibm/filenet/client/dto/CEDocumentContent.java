/**
 *
 */
package lu.mtn.ibm.filenet.client.dto;

import java.io.Serializable;

/**
 * @author NguyenT
 *
 */
public class CEDocumentContent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String PROP_CONTENT_TYPE = "ContentType";
    public static final String PROP_RETRIEVAL_NAME = "RetrievalName";
    public static final String PROP_CONTENT = "Content";

    private String name;

    private String mimeType;

    private byte[] content;


    /**
     *
     */
    public CEDocumentContent() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the content
     */
    public byte[] getContent() {
        return this.content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return this.mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


}
