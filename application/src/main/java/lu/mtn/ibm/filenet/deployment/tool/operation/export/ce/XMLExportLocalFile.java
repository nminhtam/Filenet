/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingLocalFilePrerequisite;

/**
 * @author MTN
 *
 */
public class XMLExportLocalFile extends AbstractXMLExportOperation<File> {

    private String path;

    private boolean zipContent;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportLocalFile(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.path = rootElement.getAttribute("path");
        zipContent = rootElement.hasAttribute("zipContent") ? Boolean.valueOf(rootElement.getAttribute("zipContent")) : Constants.DEFAULT_ZIP_CONTENT;

        prerequisites.add(new ExistingLocalFilePrerequisite(this.path));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "FILE_" + path;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected File findObject(ExecutionContext context) {
        return new File(path);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(File object) {
        return object.getPath();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(File file, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("name", file.getName());
        root.setAttribute("folder", file.getParent());

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();

            byte[] tmp = new byte[2048];
            int length;
            while ((length = fis.read(tmp)) != -1) {
                bs.write(tmp, 0, length);
            }

            Element content = createElement(doc, root, "content");
            content.setAttribute("zip", String.valueOf(zipContent));
            content.setTextContent(new String(Base64.encodeBase64(bs.toByteArray())));

        } catch (Exception e) {
            throw new OperationExecutionException("Could not read file " + file.getAbsolutePath());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }

        this.write(context, doc);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_LOCAL_FILE;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportLocalFile.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export file " + path;
    }

}
