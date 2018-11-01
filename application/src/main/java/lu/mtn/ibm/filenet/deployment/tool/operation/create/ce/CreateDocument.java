/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingPropertyDefinitionPrerequisite;


/**
 * @author NguyenT
 *
 */
public class CreateDocument extends AbstractCreateOperation {

    private DocumentCreationRequest request;

    private Map<String, String> datePatterns;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateDocument(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        try {
            this.datePatterns = new HashMap<String, String>();
            
            String className = rootElement.getAttribute("class");
            String docName = rootElement.getAttribute("name");
            this.request = new DocumentCreationRequest(className);
            this.request.setDocumentName(docName);

            if (rootElement.hasAttribute("folder")) {
                String folder = rootElement.getAttribute("folder");
                this.request.setFolder(folder);
                prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_FOLDER_NAME, folder), true, "The folder " + folder + "must exist."));
            }

            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, className), true, "The class " + className + "must exist."));

            NodeList children = rootElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {

                Node node = children.item(i);

                if (node.getNodeType() != 3) {

                    if ("property".equals(node.getNodeName())) {

                        String name = node.getAttributes().getNamedItem("name").getNodeValue();
                        int type = Integer.valueOf(node.getAttributes().getNamedItem("type").getNodeValue());

                        prerequisites.add(new ExistingPropertyDefinitionPrerequisite(className, name, type));

                        if (node.getAttributes().getNamedItem("pattern") != null) {
                            this.datePatterns.put(name, node.getAttributes().getNamedItem("pattern").getNodeValue());
                        }

                        List<String> values = new ArrayList<String>();

                        NodeList valueNodes = node.getChildNodes();
                        for (int j = 0; j < valueNodes.getLength(); j++) {
                            Node valueNode = valueNodes.item(j);
                            if (valueNode.getNodeType() != 3 && "value".equals(valueNode.getNodeName())) {
                                values.add(valueNode.getTextContent());
                            }
                        }
                        request.getDocProps().put(name, getPropertyValue(values, type, name, this.datePatterns.get(name)));

                    } else if ("content".equals(node.getNodeName())) {

                        String name = node.getAttributes().getNamedItem("name").getNodeValue();
                        String mime = node.getAttributes().getNamedItem("mime").getNodeValue();
                        boolean zipContent = node.getAttributes().getNamedItem("zip") != null ? Boolean.valueOf(node.getAttributes().getNamedItem("zip").getNodeValue()) : Constants.DEFAULT_ZIP_CONTENT;

                        byte[] content = Base64.decodeBase64(node.getTextContent());

                        ByteArrayInputStream bs = new ByteArrayInputStream(content);
                        InputStream is = zipContent ? new GZIPInputStream(bs) : bs;

                        request.addContent(name, is, mime);
                    }
                }
            }
        } catch (Exception e) {
            throw new OperationInitializationException("Exception : " + getDescription(), e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public String executeInternal(ExecutionContext context) throws OperationExecutionException {

        try {
            String id = context.getConnection().getClient().createDocument(request);

            return id;

        } catch (Exception e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create DocumentContent \"" + request.getDocumentClassName() + "\" with the name \"" + request.getDocumentName() + "\"";
    }

    /**
     * @return the request
     */
    public DocumentCreationRequest getRequest() {
        return this.request;
    }
}
