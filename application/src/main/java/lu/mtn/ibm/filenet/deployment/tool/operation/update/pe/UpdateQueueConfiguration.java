/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.pe;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.filenet.api.admin.CodeModule;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;

import filenet.vw.api.VWAttributeInfo;
import filenet.vw.api.VWException;
import filenet.vw.api.VWExposedFieldDefinition;
import filenet.vw.api.VWIndexDefinition;
import filenet.vw.api.VWOperationDefinition;
import filenet.vw.api.VWParameterDefinition;
import filenet.vw.api.VWQueueDefinition;
import filenet.vw.api.VWSystemConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe.ExistingQueuePrerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdateQueueConfiguration extends AbstractUpdatePEConfigurationOperation {

    private String queueName;

    private String codeModuleConfig;

    private String codeModuleId;

    private NodeList operations;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateQueueConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.pe.AbstractUpdatePEConfigurationOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.queueName = rootElement.getAttribute("name");

        prerequisites.add(new ExistingQueuePrerequisite(this.queueName, true));

        try {
            Node codeModule = (Node) xPath.evaluate("//codeModule", rootElement, XPathConstants.NODE);

            if (codeModule != null) {
                this.codeModuleConfig = codeModule.getTextContent();
                this.codeModuleId = codeModule.getAttributes().getNamedItem("codeModuleId").getNodeValue();
                if (ExecutionContext.isIdMustBeInterpreted(codeModuleId)) {
                    prerequisites.add(new ExistingIdRefPrerequisite(codeModuleId));
                } else {
                    prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "CodeModule", codeModuleId), true, "The codeModule "
                                    + codeModuleId + " must exist."));
                }
            }

            operations = (NodeList) xPath.evaluate("//operation", rootElement, XPathConstants.NODESET);

        } catch (XPathExpressionException e) {
            throw new OperationInitializationException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        try {

            VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();
            VWQueueDefinition queue = config.getQueueDefinition(queueName);
            queue.setDescription(description);

            VWExposedFieldDefinition[] fieldDefinitions = createFields(queue);
            if (fieldDefinitions != null) {
                queue.createFieldDefinitions(fieldDefinitions);
                commit(config, queue, "Fields");
            }

            VWIndexDefinition[] indexesDefinitions = createIndexes(queue);
            if (indexesDefinitions != null) {
                config.updateQueueDefinition(queue);
                queue.createIndexDefinitions(indexesDefinitions);
                commit(config, queue, "Indexes");
            }

            if (codeModuleId != null) {
                codeModuleId = context.interpretId(codeModuleId);
                config.updateQueueDefinition(queue);
                updateComponentQueueConfiguration(context, queue);
                commit(config, queue, "CodeModule");
            }

            config.updateQueueDefinition(queue);
            updateOperations(queue);
            commit(config, queue, "Operations");


        } catch (VWException e) {
            throw new OperationExecutionException(e);
        } catch (ParserConfigurationException e) {
            throw new OperationExecutionException(e);
        } catch (SAXException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        } catch (Exception e) {
            throw new OperationExecutionException(e);
        }
    }

    protected void commit(VWSystemConfiguration config, VWQueueDefinition queue, String module) {
        if (config.hasChanged() || queue.hasChanged()) {
            // Commit changes
            String[] errors = config.commit();
            if (errors != null) {
                System.out.println(String.format("Errors in %s part : %s", module, Arrays.toString(errors)));
            } else {
                System.out.println(String.format("All changes in %s part have been committed.", module));
            }
        }
    }

    protected void updateOperations(VWQueueDefinition queue) throws Exception {

        if (operations != null) {
            // Create a list of all operations to delete if there are not part of the new configuration.
            Set<String> opsToDelete = new HashSet<String>();
            VWOperationDefinition[] ops = queue.getOperations();
            for (int i = 0; i < ops.length; i++) {
                opsToDelete.add(ops[i].getName());
            }

            // Process all operations in the new configuration.
            for (int i = 0; i < operations.getLength(); i++) {
                Node operation = operations.item(i);

                String opName = operation.getAttributes().getNamedItem("name").getNodeValue();

                // Remove the operation from the delete list.
                VWOperationDefinition op;
                if(opsToDelete.remove(opName)) {
                    op = queue.getOperation(opName);
                } else {
                    op = queue.createOperation(opName);
                }

                String xml = ((Node) xPath.evaluate("child::attribute", operation, XPathConstants.NODE)).getTextContent();
                VWAttributeInfo info = op.getAttributeInfo();
                if (!xml.equals(info.getFieldValue("F_OperationDescriptor"))) {
                    info.setFieldValue("F_OperationDescriptor", xml);
                    op.setAttributeInfo(info);

                    Node desc = operation.getAttributes().getNamedItem("desc");
                    if (desc != null) {
                        op.setDescription(desc.getNodeValue());
                    }

                    this.processParameters(operation, op);
                }
            }

            for (String opToDelete : opsToDelete) {
                queue.deleteOperation(opToDelete);
            }
        }
    }

    protected void processParameters(Node operation, VWOperationDefinition op) throws Exception {
        // Create a list of all parameters to delete if there are not part of the new configuration.
        Set<String> paramsToDelete = new HashSet<String>();
        VWParameterDefinition[] params = op.getParameterDefinitions();
        if (params != null && params.length > 0) {
            for (int j = 0; j < params.length; j++) {
                paramsToDelete.add(params[j].getName());
            }
        }

        // Process all parameters in the new configuration.
        NodeList children = (NodeList) xPath.evaluate("child::param", operation, XPathConstants.NODESET);
        for (int j = 0; j < children.getLength(); j++) {

            Node node = children.item(j);

            String paramName = node.getAttributes().getNamedItem("name").getNodeValue();

            // Remove the param from the delete list.
            paramsToDelete.remove(paramName);

            updateOrCreateParam(op, node, paramName);
        }

        for (String paramToDelete : paramsToDelete) {
            op.deleteParameter(paramToDelete);
        }
    }


    protected VWOperationDefinition updateOrCreateParam(VWOperationDefinition op, Node paramNode, String name) throws Exception {

        int mode = Integer.parseInt(paramNode.getAttributes().getNamedItem("mode").getNodeValue());
        int dataType = Integer.parseInt(paramNode.getAttributes().getNamedItem("dataType").getNodeValue());
        boolean isArray = Boolean.valueOf(paramNode.getAttributes().getNamedItem("isArray").getNodeValue());
        Node desc = paramNode.getAttributes().getNamedItem("desc");
        Node value = paramNode.getAttributes().getNamedItem("value");

        VWParameterDefinition param;
        try {
            param = op.getParameterDefinition(name);
            param.setDataType(dataType);
            param.setIsArray(isArray);
            param.setMode(mode);

        } catch (VWException e) {
            param = op.createParameter(name, mode, dataType, isArray);
        }

        if (desc != null) {
            param.setDescription(desc.getNodeValue());
        }
        if (value != null) {
            param.setValue(value.getNodeValue());
        }

        return op;
    }

    protected void updateComponentQueueConfiguration(ExecutionContext context, VWQueueDefinition queue) throws ParserConfigurationException, SAXException,
                    IOException, OperationExecutionException {

        VWAttributeInfo info = queue.getAttributeInfo();

        // Find the code Module
        PropertyFilter filter = new PropertyFilter();
        filter.addIncludeProperty(new FilterElement(null, null, null, "VersionSeries", null));
        filter.addIncludeProperty(new FilterElement(null, null, null, "Id", null));
        ObjectStore os = context.getConnection().getObjectStore();
        CodeModule codeModule = Factory.CodeModule.fetchInstance(os, new Id(codeModuleId), filter);

        // Get the current component queue configuration.
        String xml = (String) info.getFieldValue("F_ComponentDescriptor");
        Document currentDocument = toDocument(xml);

        try {
            // Get the new component queue configuration.
            Document newDocument = toDocument(codeModuleConfig);

            this.updateCodeModuleRef(os, codeModule, xPath, newDocument);
            this.copyAndAdaptCurrentInfo(currentDocument, xPath, newDocument);

            xml = toXML(newDocument, false);
            info.setFieldValue("F_ComponentDescriptor", xml);
            queue.setAttributeInfo(info);

        } catch (XPathExpressionException e) {
            throw new OperationExecutionException(e);
        } catch (DOMException e) {
            throw new OperationExecutionException(e);
        } catch (ParserConfigurationException e) {
            throw new OperationExecutionException(e);
        } catch (SAXException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        } catch (TransformerException e) {
            throw new OperationExecutionException(e);
        }
    }


    protected void updateCodeModuleRef(ObjectStore os, CodeModule codeModule, XPath xPath, Document newDocument) throws XPathExpressionException {

        Node node = (Node) xPath.evaluate("//jar_file_url", newDocument, XPathConstants.NODE);

        String cm = node.getTextContent();

        String[] values = cm.split("\\|");

        // New value
        StringBuffer newRef = new StringBuffer();
        for (int i = 0; i < values.length - 3; i++) {
            newRef.append(values[i]).append("|");
        }

        // Changed values
        newRef.append(os.get_Id()).append("|");
        newRef.append(codeModule.get_VersionSeries().get_Id()).append("|");
        newRef.append(codeModule.get_Id().toString());

        node.setTextContent(newRef.toString());
    }

    protected void copyAndAdaptCurrentInfo(Document currentDocument, XPath xPath, Document newDocument) throws XPathExpressionException {

        // Copy User info
        Node currentNode = (Node) xPath.evaluate("//jaas_username", currentDocument, XPathConstants.NODE);
        Node newNode = (Node) xPath.evaluate("//jaas_username", newDocument, XPathConstants.NODE);
        newNode.setTextContent(currentNode.getTextContent());

        // Copy Password info
        currentNode = (Node) xPath.evaluate("//jaas_password", currentDocument, XPathConstants.NODE);
        newNode = (Node) xPath.evaluate("//jaas_password", newDocument, XPathConstants.NODE);
        newNode.setTextContent(currentNode.getTextContent());

        // Increase the current revision
        currentNode = (Node) xPath.evaluate("//revision", currentDocument, XPathConstants.NODE);
        newNode = (Node) xPath.evaluate("//revision", newDocument, XPathConstants.NODE);
        int revision = Integer.parseInt(currentNode.getTextContent());
        ++revision;
        newNode.setTextContent(String.valueOf(revision));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        if (codeModuleId != null) {
            return String.format("Update PE Component Queue \"%s\" with CodeModule id \"%s\"", this.queueName, codeModuleId);
        }
        return String.format("Update PE Queue \"%s\"", this.queueName);
    }

}
