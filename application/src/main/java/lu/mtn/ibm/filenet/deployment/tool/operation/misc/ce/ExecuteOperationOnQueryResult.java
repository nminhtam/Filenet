/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.collection.EngineCollection;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.delete.ce.Delete;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportChoiceList;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportClass;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportClassSubscription;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportCodeModule;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportCustomObject;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportDocument;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportEventAction;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportPropertyTemplate;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportStoredSearch;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.TransferWorkflow;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class ExecuteOperationOnQueryResult extends Operation {

    private String query;

    private String operation;

    private Document xmlOperation;

    private static final Map<String, String> OPERATIONS;

    static {
        OPERATIONS = new HashMap<String, String>();

        OPERATIONS.put(Constants.TAG_EXPORT_DOC_CONTENT, XMLExportDocument.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_CUSTOM_OBJECT, XMLExportCustomObject.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_PROPERTY_TEMPLATE, XMLExportPropertyTemplate.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_CHOICE_LIST, XMLExportChoiceList.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_CLASS, XMLExportClass.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_STORED_SEARCH, XMLExportStoredSearch.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_CODE_MODULE, XMLExportCodeModule.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_EVENT_ACTION, XMLExportEventAction.class.getName());
        OPERATIONS.put(Constants.TAG_EXPORT_CLASS_SUBSCRIPTION, XMLExportClassSubscription.class.getName());
        OPERATIONS.put(Constants.TAG_TRANSFER_WORKFLOW, TransferWorkflow.class.getName());
        OPERATIONS.put(Constants.TAG_DELETE_OBJ, Delete.class.getName());
    }

    /**
     * @param xmlOperation
     * @throws OperationInitializationException
     */
    public ExecuteOperationOnQueryResult(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.query = rootElement.getAttribute("query");
        this.operation = rootElement.getAttribute("operation");

        try {
            xmlOperation = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
            Element root = xmlOperation.createElement(operation);
            xmlOperation.appendChild(root);

        NodeList children = rootElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3) {
                if ("content".equals(node.getNodeName())) {
                    initContent(node);
                } else {
                    root.setAttribute(node.getNodeName(), node.getTextContent());
            	}
        	}
    }
        } catch (DOMException e) {
            throw new OperationInitializationException(e);
        } catch (ParserConfigurationException e) {
            throw new OperationInitializationException(e);
        }
    }

    protected void initContent(Node contentNode) {

        NodeList children = contentNode.getChildNodes();

        Element root = xmlOperation.getDocumentElement();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3) {

                root.appendChild(xmlOperation.adoptNode(node));
            }
        }
    }
    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        try {
            String clazz = OPERATIONS.get(operation);

            if (clazz != null) {
                SearchSQL sql = new SearchSQL(query);
                SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

                EngineCollection ec = ss.fetchRows(sql, 2000, null, Boolean.TRUE);

                Constructor<Operation> constructor = ((Class<Operation>) Class.forName(clazz)).getConstructor(String.class);
                xmlOperation.getDocumentElement().setAttribute("id", "");
                Operation task = constructor.newInstance(toXML(xmlOperation));

                for (Iterator<?> it = ec.iterator(); it.hasNext(); ) {

                    RepositoryRow row = (RepositoryRow) it.next();

                    ((ExecutableOperationOnQuery) task).setNewId(row.getProperties().getIdValue("Id").toString());

                    task.execute(context);
                }
            }
        } catch (SecurityException e) {
            throw new OperationExecutionException(e);
        } catch (IllegalArgumentException e) {
            throw new OperationExecutionException(e);
        } catch (NoSuchMethodException e) {
            throw new OperationExecutionException(e);
        } catch (ClassNotFoundException e) {
            throw new OperationExecutionException(e);
        } catch (InstantiationException e) {
            throw new OperationExecutionException(e);
        } catch (IllegalAccessException e) {
            throw new OperationExecutionException(e);
        } catch (InvocationTargetException e) {
            throw new OperationExecutionException(e);
        } catch (TransformerException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "ExecuteOperationOnQueryResult with op \"" + this.operation + "\" on result of query \"" + this.query + "\"";
    }


}
