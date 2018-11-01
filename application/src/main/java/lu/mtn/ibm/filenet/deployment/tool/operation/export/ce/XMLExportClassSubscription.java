/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.collection.StringList;
import com.filenet.api.collection.SubscribedEventList;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.WorkflowDefinition;
import com.filenet.api.events.ClassSubscription;
import com.filenet.api.events.ClassWorkflowSubscription;
import com.filenet.api.events.EventAction;
import com.filenet.api.events.SubscribedEvent;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author nguyent
 *
 */
public class XMLExportClassSubscription extends AbstractXMLExportOperation<ClassSubscription> implements ExecutableOperationOnQuery {

    private String id;

    private String name;

    private boolean exportEventAction;

    private boolean exportCodeModule;

    private boolean exportWorkflowDefinition;

    private boolean useWorkflowDefinitionId;

    private boolean transferWorkflowDefinition;



    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportClassSubscription(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) {

        if (rootElement.hasAttribute("id")) {
            id = rootElement.getAttribute("id");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "ClassSubscription", id), true, "The ClassSubscription with id " + id + " must exist."));

        } else {
            name = rootElement.getAttribute("name");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_SUBSCRIPTION_NAME, name), true, "The ClassSubscription with name " + name + " must exist."));
        }
        exportEventAction = rootElement.hasAttribute("exportEventAction") ? Boolean.valueOf(rootElement.getAttribute("exportEventAction")) : Constants.DEFAULT_EXPORT_EVENT_ACTION;
        exportCodeModule = rootElement.hasAttribute("exportCodeModule") ? Boolean.valueOf(rootElement.getAttribute("exportCodeModule")) : Constants.DEFAULT_EXPORT_CODE_MODULE;

        exportWorkflowDefinition = rootElement.hasAttribute("exportWorkflowDefinition") ? Boolean.valueOf(rootElement.getAttribute("exportWorkflowDefinition")) : Constants.DEFAULT_EXPORT_WORKFLOW_DEF;
        useWorkflowDefinitionId = rootElement.hasAttribute("useWorkflowDefinitionId") ? Boolean.valueOf(rootElement.getAttribute("useWorkflowDefinitionId")) : Constants.DEFAULT_USE_WORKFLOW_DEF_ID;
        transferWorkflowDefinition = rootElement.hasAttribute("transferWorkflowDefinition") ? Boolean.valueOf(rootElement.getAttribute("transferWorkflowDefinition")) : Constants.DEFAULT_TRANSFER_WORKFLOW_DEF;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "CLASS_SUBSCRIPT_" + (id != null ? id : name);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected ClassSubscription findObject(ExecutionContext context) {
        ObjectStore os = context.getConnection().getObjectStore();

        SearchSQL sql = new SearchSQL(id != null ? String.format(Constants.QUERY_EXPORT_CLASS_SUBSCRIPTION_ID, id) : String.format(Constants.QUERY_EXPORT_CLASS_SUBSCRIPTION_NAME, name));
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The ClassSubscription " + (id != null ? id : name)  + " must exist.");
        }
        return (ClassSubscription) it.next();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(ClassSubscription object) {
        return object.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeDetails(ClassSubscription subscription, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("class", subscription.getClassName());
        root.setAttribute("name", subscription.get_DisplayName());

        ClassDefinition targetClassDef = (ClassDefinition) subscription.get_SubscriptionTarget();
        root.setAttribute("targetClass", targetClassDef.get_SymbolicName());
        if (subscription.get_FilteredPropertyId() != null) {
            root.setAttribute("filteredPropertyId", subscription.get_FilteredPropertyId());
        }
        if (subscription.get_FilterExpression() != null) {
            root.setAttribute("filterExpression", subscription.get_FilterExpression());
        }
        root.setAttribute("includeSubclasses", String.valueOf(subscription.get_IncludeSubclassesRequested()));

        SubscribedEventList events = subscription.get_SubscribedEvents();
        if (events != null && !events.isEmpty()) {

            Iterator<SubscribedEvent> iter = events.iterator();
            SubscribedEvent event = iter.next();

            StringBuffer sb = new StringBuffer(event.get_EventClass().get_SymbolicName());
            while (iter.hasNext()) {
                event = iter.next();
                sb.append(";").append(event.get_EventClass().get_SymbolicName());
            }
            root.setAttribute("eventList", sb.toString());
        }

        if (subscription instanceof ClassWorkflowSubscription) {

            ClassWorkflowSubscription cws = (ClassWorkflowSubscription) subscription;

            root.setAttribute("enableManualLaunch", String.valueOf(cws.get_EnableManualLaunch()));

            StringList stringList = cws.get_PropertyMap();
            if (stringList != null && !stringList.isEmpty()) {
                Iterator<String> iter = stringList.iterator();
                StringBuffer sb = new StringBuffer(iter.next());
                while (iter.hasNext()) {
                    sb.append(";").append(iter.next());
                }
                root.setAttribute("propertyMap", sb.toString());
            }

            WorkflowDefinition workDef = cws.get_WorkflowDefinition();
            if (exportWorkflowDefinition) {
                try {
                    XMLExportDocument export = new XMLExportDocument(workDef.get_Id().toString(), operation, writerName, Constants.DEFAULT_ZIP_CONTENT, Constants.DEFAULT_ADD_FOLDER_PATH);
                    export.setIdRef("WD_" + workDef.get_Name());
                    export.execute(context);

                    root.setAttribute("workflowDefinitionId", "#{WD_" + workDef.get_Name() + "}");

                } catch (OperationInitializationException e) {
                    throw new OperationExecutionException(e);
                }
            } else {
                if (useWorkflowDefinitionId) {
                    root.setAttribute("workflowDefinitionId", workDef.get_Id().toString());
                } else {
                    root.setAttribute("workflowDefinitionId", "#{" + workDef.get_Name() + "}");
                }
            }
            root.setAttribute("transferWorkflowDefinition", String.valueOf(transferWorkflowDefinition));


        } else {

            root.setAttribute("isSynchronous", String.valueOf(subscription.get_IsSynchronous()));

            EventAction eventAction = subscription.get_EventAction();
            if (exportEventAction) {
                try {
                    XMLExportEventAction export = new XMLExportEventAction(eventAction.get_Id().toString(), operation, writerName, exportCodeModule);
                    export.setIdRef("EA_" + eventAction.get_Name());
                    export.execute(context);

                    root.setAttribute("eventActionId", "#{EA_" + eventAction.get_Name() + "}");

                } catch (OperationInitializationException e) {
                    throw new OperationExecutionException(e);
                }
            } else {
                root.setAttribute("eventActionId", eventAction.get_Id().toString());
            }
        }

        this.write(context, doc);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_CLASS_SUBSCRIPTION;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportClassSubscription.");
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export ClassSubscription " + (id != null ? " with id " + id : " with name " + name);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
