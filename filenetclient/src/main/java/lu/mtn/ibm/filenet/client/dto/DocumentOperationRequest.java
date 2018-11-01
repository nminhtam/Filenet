/**
 *
 */
package lu.mtn.ibm.filenet.client.dto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author NguyenT
 *
 */
public abstract class DocumentOperationRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected String documentName;

    protected Map<String, Object> docProps;

    protected List<CEDocumentContent> contents;

    /**
     *
     */
    public DocumentOperationRequest() {
        this.docProps = new HashMap<String, Object>();
    }

    /**
     * @return the documentName
     */
    public String getDocumentName() {
        return this.documentName;
    }

    /**
     * @param documentName the documentName to set
     */
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    /**
     * @return the docProps
     */
    public Map<String, Object> getDocProps() {
        return this.docProps;
    }

    /**
     * @param docProps the docProps to set
     */
    public void setDocProps(Map<String, Object> docProps) {
        this.docProps = docProps;
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

    /**
     * @param file
     * @throws FileNotFoundException
     */
    public void addContent(File file) throws IOException {
        this.addContent(file.getName(), new FileInputStream(file));
    }


    /**
     * @param name
     * @param content
     * @throws FileNotFoundException
     */
    public void addContent(String name, byte[] content) {
        this.addContent(name, content, null);
    }

    /**
     * @param name
     * @param input
     * @throws FileNotFoundException
     */
    public void addContent(String name, InputStream input) throws IOException {
        this.addContent(name, input, null);
    }

    /**
     * @param file
     * @param mimeType
     * @throws FileNotFoundException
     */
    public void addContent(File file, String mimeType) throws IOException {
        this.addContent(file.getName(), new FileInputStream(file), mimeType);
    }

    /**
     * @param name
     * @param content
     * @param mimeType
     * @throws FileNotFoundException
     */
    public void addContent(String name, byte[] content, String mimeType) {
        CEDocumentContent docContent = new CEDocumentContent();
        docContent.setContent(content);
        docContent.setName(name);
        docContent.setMimeType(mimeType);

        if (contents == null) {
            contents = new ArrayList<CEDocumentContent>();
        }
        contents.add(docContent);
    }

    /**
     * @param name
     * @param input
     * @param mimeType
     * @throws FileNotFoundException
     */
    public void addContent(String name, InputStream input, String mimeType) throws IOException {

        byte[] buffer = new byte[1024];
        int length;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        while ((length = input.read(buffer)) != -1) {
            bs.write(buffer, 0, length);
        }
        this.addContent(name, bs.toByteArray(), mimeType);
    }
}
