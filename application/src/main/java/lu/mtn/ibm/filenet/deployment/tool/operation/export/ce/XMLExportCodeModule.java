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

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.admin.CodeModule;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Factory;
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
public class XMLExportCodeModule extends AbstractXMLExportOperation<CodeModule> implements ExecutableOperationOnQuery {

    private String id;

    private boolean zipContent;

    private boolean addIdRef;

    private boolean addUpdateIdRef;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportCodeModule(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @param xml
     * @param id
     * @param tag
     * @param writerName
     * @param zipContent
     * @param addFolderPath
     * @throws OperationInitializationException
     */
    public XMLExportCodeModule(String id, String operation, String writerName, boolean zipContent) throws OperationInitializationException {
        super(operation, writerName);
        this.id = id;
        this.zipContent = zipContent;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "CodeModule", id), true, "The CodeModule with id " + id + " must exist."));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        id = rootElement.getAttribute("id");
        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "CodeModule", id), true, "The CodeModule with id " + id + " must exist."));

        zipContent = rootElement.hasAttribute("zipContent") ? Boolean.valueOf(rootElement.getAttribute("zipContent")) : Constants.DEFAULT_ZIP_CONTENT;
        addIdRef = rootElement.hasAttribute("addIdRef") ? Boolean.valueOf(rootElement.getAttribute("addIdRef")) : Constants.DEFAULT_ADD_ID_REF;
        addUpdateIdRef = rootElement.hasAttribute("addUpdateIdRef") ? Boolean.valueOf(rootElement.getAttribute("addUpdateIdRef")) : Constants.DEFAULT_ADD_UPDATE_ID_REF;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "CODE_MODULE_" + id;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected CodeModule findObject(ExecutionContext context) {
        return Factory.CodeModule.fetchInstance(context.getConnection().getObjectStore(), new Id(id), null);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(CodeModule codeModule) {
        return codeModule.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(CodeModule codeModule, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        if (UPDATE.equalsIgnoreCase(operation)) {
            if (addUpdateIdRef) {
                root.setAttribute("id", "#{CM_" + codeModule.getProperties().getStringValue("DocumentTitle") + "}");
            } else {
                root.setAttribute("id", codeModule.get_Id().toString());
            }
        }
        if (idRef == null && addIdRef) {
            root.setAttribute("idRef", "DOC_" + codeModule.get_Name());
        }
        root.setAttribute("documentTitle", codeModule.getProperties().getStringValue("DocumentTitle"));

        try {
            this.writeContents(codeModule, root, doc);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        }

        this.write(context, doc);
    }

    @SuppressWarnings("unchecked")
    protected void writeContents(CodeModule codeModule, Element root, Document doc) throws IOException {

        for (Iterator<ContentTransfer> iter = codeModule.get_ContentElements().iterator(); iter.hasNext(); ) {

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
            Element content = createElement(doc, root, "content");
            content.setAttribute("name", ct.get_RetrievalName());
            content.setAttribute("mime", ct.get_ContentType());
            if (zipContent) {
                content.setAttribute("zip", "true");
            }
            content.setTextContent(new String(Base64.encodeBase64(bs.toByteArray())));
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_CODE_MODULE;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_DOC_CONTENT;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportCodeModule.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return null;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }


}
