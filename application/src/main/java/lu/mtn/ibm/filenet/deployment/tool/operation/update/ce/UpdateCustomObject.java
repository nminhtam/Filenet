/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingPropertyDefinitionPrerequisite;


/**
 * @author NguyenT
 *
 */
public class UpdateCustomObject extends AbstractUpdateOperation {

    private String id;

    private DocumentModificationRequest request;

    private Map<String, String> datePatterns;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateCustomObject(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        id = rootElement.getAttribute("id");
        if (ExecutionContext.isIdMustBeInterpreted(id)) {
            prerequisites.add(new ExistingIdRefPrerequisite(id));
        } else {
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "CustomObject", id), true, "The document content with id " + id + " must exist."));
        }

        try {
            this.datePatterns = new HashMap<String, String>();
            this.request = new DocumentModificationRequest(null);

            String className = rootElement.getAttribute("class");
            String docName = rootElement.getAttribute("name");
            this.request.setDocumentName(docName);

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

                    }
                }
            }
        } catch (Exception e) {
            throw new OperationInitializationException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public String executeInternal(ExecutionContext context) throws OperationExecutionException {

        try {
            request.setDocumentId(context.interpretId(id));
            String id = context.getConnection().getClient().updateCustomObject(request);

            System.out.println(id);
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
        return "Update CustomObject with id " + id;
    }
}
