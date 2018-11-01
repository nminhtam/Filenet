/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.pe;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.filenet.api.admin.CodeModule;
import com.filenet.api.core.Factory;
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
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportDocument;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe.ExistingQueuePrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportQueueConfiguration extends AbstractXMLExportPEConfigurationOperation<VWQueueDefinition> {

    private String name;

    private XPath xPath = xPathFactory.newXPath();

    private boolean exportCodeModule;

    private boolean hasCodeModule;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportQueueConfiguration(String xml) throws OperationInitializationException {
        super(xml);
        hasCodeModule = false;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.name = rootElement.getAttribute("name");
        this.exportCodeModule = rootElement.hasAttribute("exportCodeModule") ? Boolean.valueOf(rootElement.getAttribute("exportCodeModule")) : Constants.DEFAULT_EXPORT_CODE_MODULE;

        prerequisites.add(new ExistingQueuePrerequisite(this.name, true));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "QUEUE_" + name;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected VWQueueDefinition findObject(ExecutionContext context) {
        VWSystemConfiguration config = context.getConnection().getVWSession().fetchSystemConfiguration();
        VWQueueDefinition queue = config.getQueueDefinition(name);
        return queue;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(VWQueueDefinition object) {
        return name;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(VWQueueDefinition queue, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("name", name);
        if (queue.getDescription() != null && !queue.getDescription().isEmpty()) {
            root.setAttribute("description", queue.getDescription());
        }
        if (CREATE.equalsIgnoreCase(operation)) {
            root.setAttribute("type", String.valueOf(queue.getQueueType()));
            root.setAttribute("connectorQueue", String.valueOf(queue.getIsConnectorQueue()));
        }

        this.writeExposedFields(root, doc, queue.getFields());
        this.writeIndexes(root, doc, queue.getIndexes());
        this.writeCodeModuleDetails(context, queue, root, doc);
        this.writeOperations(root, doc, queue);

        this.write(context, doc);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_QUEUE_CONFIG;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_QUEUE_CONFIG;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportQueueConfiguration.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return String.format("Export configuration for PE %sQueue \"%s\"", hasCodeModule ? "Component " : "", name);
    }

    protected void writeOperations(Element root, Document doc, VWQueueDefinition queue) {

        VWOperationDefinition[] ops = queue.getOperations();
        if (ops != null && ops.length > 0) {
            Element operations = createElement(doc, root, "operations");
            root.appendChild(operations);
            for (int i = 0; i < ops.length; i++) {
                VWOperationDefinition op = ops[i];

                Element opElement = createElement(doc, operations, "operation");
                opElement.setAttribute("name", op.getName());
                if (op.getDescription() != null && !op.getDescription().isEmpty()) {
                    opElement.setAttribute("desc", op.getDescription());
                }

                String xml = (String) op.getAttributeInfo().getFieldValue("F_OperationDescriptor");
                Element attributeElement = createElement(doc, opElement, "attribute");
                attributeElement.appendChild(doc.createCDATASection(xml));

                VWParameterDefinition[] paramDefs = op.getParameterDefinitions();
                for (int j = 0; j < paramDefs.length; j++) {
                    VWParameterDefinition paramDef = paramDefs[j];

                    Element paramElement = createElement(doc, opElement, "param");
                    paramElement.setAttribute("name", paramDef.getName());
                    if (paramDef.getDescription() != null && !paramDef.getDescription().isEmpty()) {
                        paramElement.setAttribute("desc", paramDef.getDescription());
                    }
                    paramElement.setAttribute("dataType", String.valueOf(paramDef.getDataType()));
                    paramElement.setAttribute("isArray", String.valueOf(paramDef.getIsArray()));
                    paramElement.setAttribute("mode", String.valueOf(paramDef.getMode()));

                    if (paramDef.getValue() != null && !paramDef.getValue().isEmpty()) {
                        paramElement.setAttribute("value", paramDef.getValue());
                    }
                }
            }
        }
    }

    protected void writeCodeModuleDetails(ExecutionContext context, VWQueueDefinition queue, Element root, Document doc) throws OperationExecutionException {

        try {
            VWAttributeInfo info = queue.getAttributeInfo();
            if (info.getAttributeNames() != null) {
                hasCodeModule = true;

                String xml = (String) info.getFieldValue("F_ComponentDescriptor");

                // Extract the codeModule id
                Node node = (Node) xPath.evaluate("//jar_file_url", toDocument(xml), XPathConstants.NODE);
                String cm = node.getTextContent();
                String[] values = cm.split("\\|");
                String codeModuleId = values[values.length - 1];

                // Create the xml element
                Element codeModuleElement = createElement(doc, root, "codeModule");
                codeModuleElement.appendChild(doc.createCDATASection(xml));

                CodeModule codeModule = Factory.CodeModule.fetchInstance(context.getConnection().getObjectStore(), new Id(codeModuleId), null);

                if (exportCodeModule) {

                    String codeModuleName = codeModule.getProperties().getStringValue("DocumentTitle");

                    try {
                        XMLExportDocument xmlExportCodeModule = new XMLExportDocument(codeModule.get_Id().toString(), operation, writerName, Constants.DEFAULT_ZIP_CONTENT, Constants.DEFAULT_ADD_FOLDER_PATH);
                        xmlExportCodeModule.setIdRef(codeModuleName);
                        xmlExportCodeModule.setAddIdRef(true);
                        xmlExportCodeModule.setAddUpdateIdRef(true);
                        xmlExportCodeModule.execute(context);

                        codeModuleElement.setAttribute("codeModuleId", "#{" + codeModuleName + "}");

                    } catch (OperationInitializationException e) {
                        throw new OperationExecutionException(e);
                    }
                } else {
                    codeModuleElement.setAttribute("codeModuleId", codeModuleId);
                }
            }

        } catch (VWException e) {
            throw new OperationExecutionException(e);
        } catch (DOMException e) {
            throw new OperationExecutionException(e);
        } catch (XPathExpressionException e) {
            throw new OperationExecutionException(e);
        } catch (ParserConfigurationException e) {
            throw new OperationExecutionException(e);
        } catch (SAXException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        }
    }

}
