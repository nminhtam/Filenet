/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.pe;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
public class CreateQueueConfiguration extends AbstractCreatePEConfigurationOperation {

    private String queueName;

    private boolean connectorQueue;

    private int queueType;

    private String codeModuleConfig;

    private String codeModuleId;

    private NodeList operations;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateQueueConfiguration(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.pe.AbstractCreatePEConfigurationOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.queueName = rootElement.getAttribute("name");
        this.connectorQueue = Boolean.parseBoolean(rootElement.getAttribute("connectorQueue"));
        this.queueType = Integer.valueOf(rootElement.getAttribute("type"));

        prerequisites.add(new ExistingQueuePrerequisite(this.queueName, false));

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
            VWQueueDefinition queue = config.createQueueDefinition(queueName, queueType);
            queue.setIsConnectorQueue(connectorQueue);
            queue.setDescription(description);

            createFields(queue);
            createIndexes(queue);

            if (codeModuleId != null) {
                codeModuleId = context.interpretId(codeModuleId);

                VWQueueDefinition ce_opQueue = config.getQueueDefinition("CE_Operations");

                queue.setIsConnectorQueue(true);
                setComponentQueueConfiguration(context, queue, ce_opQueue);
            }

            createOperations(queue);
            // Commit changes
            String[] errors = config.commit();
            if (errors != null) {
                System.out.println(String.format("Errors : %s", Arrays.toString(errors)));
            } else {
                System.out.println(String.format("All changes have been committed."));
            }

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



    protected void createOperations(VWQueueDefinition queue) throws Exception {

        // Process all operations in the new configuration.
        if (operations != null) {
            for (int i = 0; i < operations.getLength(); i++) {
                Node operation = operations.item(i);

                String opName = operation.getAttributes().getNamedItem("name").getNodeValue();

                VWOperationDefinition op = queue.createOperation(opName);
                Node desc = operation.getAttributes().getNamedItem("desc");
                if (desc != null) {
                    op.setDescription(desc.getNodeValue());
                }

                String xml = ((Node) xPath.evaluate("child::attribute", operation, XPathConstants.NODE)).getTextContent();
                VWAttributeInfo info = op.getAttributeInfo();
                info.setFieldValue("F_OperationDescriptor", xml);
                op.setAttributeInfo(info);

                this.processParameters(operation, op);
            }
        }
    }

    protected void processParameters(Node operation, VWOperationDefinition op) throws Exception {

        // Process all parameters in the new configuration.
        NodeList children = (NodeList) xPath.evaluate("child::param", operation, XPathConstants.NODESET);

        for (int j = 0; j < children.getLength(); j++) {

            Node node = children.item(j);
            String paramName = node.getAttributes().getNamedItem("name").getNodeValue();
            createParam(op, node, paramName);
        }
    }


    protected VWOperationDefinition createParam(VWOperationDefinition op, Node paramNode, String name) throws Exception {

        int mode = Integer.parseInt(paramNode.getAttributes().getNamedItem("mode").getNodeValue());
        int dataType = Integer.parseInt(paramNode.getAttributes().getNamedItem("dataType").getNodeValue());
        boolean isArray = Boolean.valueOf(paramNode.getAttributes().getNamedItem("isArray").getNodeValue());
        Node desc = paramNode.getAttributes().getNamedItem("desc");
        Node value = paramNode.getAttributes().getNamedItem("value");

        VWParameterDefinition param = op.createParameter(name, mode, dataType, isArray);

        if (desc != null) {
            param.setDescription(desc.getNodeValue());
        }
        if (value != null) {
            param.setValue(value.getNodeValue());
        }

        return op;
    }

    protected void setComponentQueueConfiguration(ExecutionContext context, VWQueueDefinition queue, VWQueueDefinition ce_opQueue) throws ParserConfigurationException, SAXException,
                    IOException, OperationExecutionException {

        // Find the code Module
        PropertyFilter filter = new PropertyFilter();
        filter.addIncludeProperty(new FilterElement(null, null, null, "VersionSeries", null));
        ObjectStore os = context.getConnection().getObjectStore();
        CodeModule codeModule = Factory.CodeModule.fetchInstance(os, new Id(codeModuleId), filter);

        try {
            // Get the new component queue configuration.
            Document newDocument = toDocument(codeModuleConfig);

            this.setCodeModuleRef(os, codeModule, xPath, newDocument);
            this.adaptCurrentInfo(xPath, newDocument, ce_opQueue);

            String xml = toXML(newDocument);

            VWAttributeInfo info = queue.getAttributeInfo();
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


    protected void setCodeModuleRef(ObjectStore os, CodeModule codeModule, XPath xPath, Document newDocument) throws XPathExpressionException {

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
        newRef.append(codeModuleId);

        node.setTextContent(newRef.toString());
    }

    protected void adaptCurrentInfo(XPath xPath, Document newDocument, VWQueueDefinition ce_opQueue) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

        VWAttributeInfo info = ce_opQueue.getAttributeInfo();
        String xml = (String) info.getFieldValue("F_ComponentDescriptor");
        Document ce_opQueueDocument = toDocument(xml);

        // Copy User info from the CE_Operations queue
        Node newNode = (Node) xPath.evaluate("//jaas_username", newDocument, XPathConstants.NODE);
        Node ce_opQueueNode = (Node) xPath.evaluate("//jaas_username", ce_opQueueDocument, XPathConstants.NODE);
        newNode.setTextContent(ce_opQueueNode.getTextContent());

        // Copy Password info from the CE_Operations queue
        newNode = (Node) xPath.evaluate("//jaas_password", newDocument, XPathConstants.NODE);
        ce_opQueueNode = (Node) xPath.evaluate("//jaas_password", ce_opQueueDocument, XPathConstants.NODE);
        newNode.setTextContent(ce_opQueueNode.getTextContent());

        // Increase the current revision
        newNode = (Node) xPath.evaluate("//revision", newDocument, XPathConstants.NODE);
        newNode.setTextContent("1");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        if (codeModuleId != null) {
            return String.format("Create PE Component Queue \"%s\" with CodeModule id \"%s\"", this.queueName, codeModuleId);
        }
        return String.format("Create PE Queue \"%s\"", this.queueName);
    }

}
