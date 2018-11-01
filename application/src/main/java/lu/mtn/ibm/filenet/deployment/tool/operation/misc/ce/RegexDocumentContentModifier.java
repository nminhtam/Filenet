/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.client.dto.CEDocumentContent;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateDocument;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.UpdateDocument;

/**
 * @author NguyenT
 *
 */
public class RegexDocumentContentModifier extends AbstractOperationDatasModifier {

    private String classNamePattern;

    private String contentNamePattern;

    private Map<String, String> replacements;


    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public RegexDocumentContentModifier(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier#init(org.w3c.dom.Element)
     */
    @Override
    public void init(Element rootElement) throws OperationInitializationException {

        this.replacements = new HashMap<String, String>();

        classNamePattern = rootElement.hasAttribute("classNamePattern") ? rootElement.getAttribute("classNamePattern") : null;
        contentNamePattern = rootElement.hasAttribute("contentNamePattern") ? rootElement.getAttribute("contentNamePattern") : null;

        NodeList children = rootElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3 && "replacement".equals(node.getNodeName())) {
                replacements.put(node.getAttributes().getNamedItem("regex").getNodeValue(), node.getAttributes().getNamedItem("substitute").getNodeValue());
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Register regex document content modifier";
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier#isApplicable(lu.mtn.ibm.filenet.deployment.tool.operation.Operation)
     */
    @Override
    public boolean isApplicable(Operation operation) {
        String className = null, contentName = null;
        if (operation instanceof CreateDocument) {
            DocumentCreationRequest request = ((CreateDocument) operation).getRequest();
            for (CEDocumentContent content : request.getContents()) {
                if (checkConditions(request.getDocumentClassName(), content.getName())) {
                    return true;
                }
            }

        } else if (operation instanceof UpdateDocument) {
            UpdateDocument op = (UpdateDocument) operation;
            for (CEDocumentContent content : op.getRequest().getContents()) {
                if (checkConditions(op.getClassName(), content.getName())) {
                    return true;
                }
            }
        }
        checkConditions(className, contentName);
        return false;
    }

    protected boolean checkConditions(String className, String contentName) {
        if (this.classNamePattern != null && className != null && className.matches(classNamePattern)) {
            return true;
        }
        if (this.contentNamePattern != null && contentName != null && contentName.matches(contentNamePattern)) {
            return true;
        }
        return false;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier#modify(lu.mtn.ibm.filenet.deployment.tool.operation.Operation)
     */
    @Override
    public void modify(Operation operation) throws OperationExecutionException {

        if (operation instanceof CreateDocument) {
            DocumentCreationRequest request = ((CreateDocument) operation).getRequest();
            for (CEDocumentContent content : request.getContents()) {
                if (checkConditions(request.getDocumentClassName(), content.getName())) {
                    content.setContent(modifyContent(content.getContent()));
                }
            }

        } else if (operation instanceof UpdateDocument) {
            UpdateDocument op = (UpdateDocument) operation;
            for (CEDocumentContent content : op.getRequest().getContents()) {
                if (checkConditions(op.getClassName(), content.getName())) {
                    content.setContent(modifyContent(content.getContent()));
                }
            }
        }
    }

    protected byte[] modifyContent(byte[] content) throws OperationExecutionException {
        try {
            String result = new String(content, "UTF-8");
            for (Entry<String, String> entry : replacements.entrySet()) {
                result = result.replaceAll(entry.getKey(), entry.getValue());
            }
            return result.getBytes("UTF-8");
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        }
    }
}
