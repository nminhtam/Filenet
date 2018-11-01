/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;

import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
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
public class CreateCustomObject extends AbstractCreateOperation {

    private DocumentCreationRequest request;

    private Map<String, String> datePatterns;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateCustomObject(String xml) throws OperationInitializationException {
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
            String folder = rootElement.getAttribute("folder");
            this.request = new DocumentCreationRequest(className);
            this.request.setDocumentName(docName);
            this.request.setFolder(folder);

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

                    }
                }
            }
        } catch (Exception e) {
            throw new OperationInitializationException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public String executeInternal(ExecutionContext context) throws OperationExecutionException {

        ObjectStore os = context.getConnection().getObjectStore();

        CustomObject customObject = Factory.CustomObject.createInstance(context.getConnection().getObjectStore(), request.getDocumentClassName());

        for (Entry<String, Object> entry : request.getDocProps().entrySet()) {
            customObject.getProperties().putObjectValue(entry.getKey(), entry.getValue());
        }
        customObject.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));

        String id = customObject.get_Id().toString();

        if (request.getFolder() != null) {
            Folder fo = Factory.Folder.fetchInstance(os, request.getFolder(), null);
            ReferentialContainmentRelationship rcr = fo.file(customObject, AutoUniqueName.AUTO_UNIQUE, request.getDocumentName(), DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
            rcr.save(RefreshMode.NO_REFRESH);
        }

        return id;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create CustomObject \"" + this.request.getDocumentClassName() + "\"";
    }
}
