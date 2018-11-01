/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.xml.transform.TransformerException;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Element;

import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportDocument extends AbstractXMLExportOperation<Document> implements ExecutableOperationOnQuery {

    private String id;

    private boolean zipContent;

    private boolean addFolderPath;

    private boolean addIdRef;

    private boolean addUpdateIdRef;

    private Boolean keepVersions;


    /**
     * @param xml
     * @param id
     * @param tag
     * @param writerName
     * @param zipContent
     * @param addFolderPath
     * @throws OperationInitializationException
     */
    public XMLExportDocument(String id, String operation, String writerName, boolean zipContent, boolean addFolderPath) throws OperationInitializationException {
        super(operation, writerName);
        this.id = id;
        this.zipContent = zipContent;
        this.addFolderPath = addFolderPath;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "Document", id), true, "The document content with id " + id + " must exist."));
    }


    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportDocument(String xml) throws OperationInitializationException {
        super(xml);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) {

        id = rootElement.getAttribute("id");
        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "Document", id), true, "The document content with id " + id + " must exist."));

        zipContent = rootElement.hasAttribute("zipContent") ? Boolean.valueOf(rootElement.getAttribute("zipContent")) : Constants.DEFAULT_ZIP_CONTENT;
        addFolderPath = rootElement.hasAttribute("addFolderPath") ? Boolean.valueOf(rootElement.getAttribute("addFolderPath")) : Constants.DEFAULT_ADD_FOLDER_PATH;

        addIdRef = rootElement.hasAttribute("addIdRef") ? Boolean.valueOf(rootElement.getAttribute("addIdRef")) : Constants.DEFAULT_ADD_ID_REF;

        if (UPDATE.equalsIgnoreCase(operation)) {
            addUpdateIdRef = rootElement.hasAttribute("addUpdateIdRef") ? Boolean.valueOf(rootElement.getAttribute("addUpdateIdRef")) : Constants.DEFAULT_ADD_UPDATE_ID_REF;
            keepVersions = rootElement.hasAttribute("keepVersions") ? Boolean.valueOf(rootElement.getAttribute("keepVersions")) : null;
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "DOC_CONTENT_" + id;
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected Document findObject(ExecutionContext context) {
        ObjectStore os = context.getConnection().getObjectStore();

        return Factory.Document.fetchInstance(os, new Id(id), null);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(Document object) {
        return object.get_Id().toString();
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeDetails(Document doc, org.w3c.dom.Document docXML, ExecutionContext context) throws OperationExecutionException {
        Element root = docXML.getDocumentElement();
        if (UPDATE.equalsIgnoreCase(operation)) {
            if (addUpdateIdRef) {
                root.setAttribute("id", "#{" + doc.get_Name() + "}");
            } else {
                root.setAttribute("id", doc.get_Id().toString());
            }
        }
        if (idRef == null && addIdRef) {
            root.setAttribute("idRef", doc.get_Name());
        }
        root.setAttribute("class", doc.getClassName());
        root.setAttribute("name", doc.get_Name());

        if (addFolderPath) {
            Iterator<Folder> it =  doc.get_FoldersFiledIn().iterator();
            if (it.hasNext()) {
                root.setAttribute("folder", it.next().get_PathName());
            }
        }

        if (keepVersions != null) {
            root.setAttribute("keepVersions", keepVersions.toString());
        }

        List<String> propertyNames = retrievePropertyNames(doc.get_ClassDescription());

        this.writeProperties(doc.getProperties(), root, docXML, propertyNames);
        try {
            this.writeContents(doc, root, docXML);
        } catch (Exception e) {
            try {
                System.err.println("Error " + toXML(docXML));
            } catch (TransformerException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }

        this.write(context, docXML);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_DOC_CONTENT;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_DOC_CONTENT;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportDocument.");
    }

    @SuppressWarnings("unchecked")
    protected void writeContents(Document doc, Element root, org.w3c.dom.Document docXML) throws IOException {

        for (Iterator<ContentTransfer> iter = doc.get_ContentElements().iterator(); iter.hasNext(); ) {

            ContentTransfer ct = (ContentTransfer) iter.next();
            InputStream is = ct.accessContentStream();

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            OutputStream os = zipContent ? new GZIPOutputStream(bs) : bs;

            try {
                byte[] buffer = new byte[2048];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                }
            } finally {
                os.close();
            }

            Element content = createElement(docXML, root, "content");
            content.setAttribute("name", ct.get_RetrievalName());
            content.setAttribute("mime", ct.get_ContentType());
            content.setAttribute("zip", String.valueOf(zipContent));
            content.setTextContent(new String(Base64.encodeBase64(bs.toByteArray())));
        }
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export DocumentContent with id " + id;
    }


    /**
     * @param addIdRef the addIdRef to set
     */
    public void setAddIdRef(boolean addIdRef) {
        this.addIdRef = addIdRef;
    }


    /**
     * @param addUpdateIdRef the addUpdateIdRef to set
     */
    public void setAddUpdateIdRef(boolean addUpdateIdRef) {
        this.addUpdateIdRef = addUpdateIdRef;
    }


    /**
     * @param keepVersions the keepVersions to set
     */
    public void setKeepVersions(Boolean keepVersions) {
        this.keepVersions = keepVersions;
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
