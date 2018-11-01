/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.filenet.api.admin.EventClassDefinition;
import com.filenet.api.admin.PEConnectionPoint;
import com.filenet.api.admin.SubscribableClassDefinition;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.collection.StringList;
import com.filenet.api.collection.SubscribedEventList;
import com.filenet.api.constants.GuidConstants;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.WorkflowDefinition;
import com.filenet.api.events.ChangeClassEvent;
import com.filenet.api.events.ChangeStateEvent;
import com.filenet.api.events.CheckinEvent;
import com.filenet.api.events.CheckoutEvent;
import com.filenet.api.events.ClassSubscription;
import com.filenet.api.events.ClassWorkflowSubscription;
import com.filenet.api.events.CreationEvent;
import com.filenet.api.events.DeletionEvent;
import com.filenet.api.events.EventAction;
import com.filenet.api.events.SubscribedEvent;
import com.filenet.api.events.UpdateEvent;
import com.filenet.api.events.WorkflowEventAction;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

import filenet.vw.api.VWSession;
import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.TransferWorkflow;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author MTN
 *
 */
public class CreateClassSubscription extends AbstractCreateOperation {

    private String subscriptionClass;

    private String targetClass;

    private String workflowDefinitionId;

    private String name;

    private boolean enableManualLaunch;

    private boolean includeSubclasses;

    private String filterExpression;

    private String filteredPropertyId;

    private String eventList;

    private String propertyMap;

    private boolean transferWorkflowDefinition;

    private String eventActionId;

    private boolean synchronous;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateClassSubscription(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.name = rootElement.getAttribute("name");
        this.subscriptionClass = rootElement.getAttribute("class");
        this.targetClass = rootElement.getAttribute("targetClass");
        if (rootElement.hasAttribute("workflowDefinitionId")) {
            this.workflowDefinitionId = rootElement.getAttribute("workflowDefinitionId");
            this.enableManualLaunch = Boolean.valueOf(rootElement.getAttribute("enableManualLaunch"));
            this.transferWorkflowDefinition = rootElement.hasAttribute("transferWorkflowDefinition") ? Boolean.valueOf(rootElement.getAttribute("transferWorkflowDefinition")) : Constants.DEFAULT_TRANSFER_WORKFLOW_DEF;

            if (ExecutionContext.isIdMustBeInterpreted(workflowDefinitionId)) {
                prerequisites.add(new ExistingIdRefPrerequisite(workflowDefinitionId));
            } else {
                prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "WorkflowDefinition", workflowDefinitionId), true, "The workflowDefinition " + workflowDefinitionId + " must exist."));
            }

        } else if (rootElement.hasAttribute("eventActionId")) {
            this.eventActionId = rootElement.getAttribute("eventActionId");
            this.synchronous = Boolean.valueOf(rootElement.getAttribute("isSynchronous"));

            if (ExecutionContext.isIdMustBeInterpreted(eventActionId)) {
                prerequisites.add(new ExistingIdRefPrerequisite(eventActionId));
            } else {
                prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "EventAction", eventActionId), true, "The EventAction " + eventActionId + " must exist."));
            }
        }
        this.includeSubclasses = Boolean.valueOf(rootElement.getAttribute("includeSubclasses"));
        this.eventList = rootElement.getAttribute("eventList");

        this.filterExpression = rootElement.hasAttribute("filterExpression") ? rootElement.getAttribute("filterExpression") : null;
        this.filteredPropertyId = rootElement.hasAttribute("filteredPropertyId") ? rootElement.getAttribute("filteredPropertyId") : null;
        this.propertyMap = rootElement.hasAttribute("propertyMap") ? rootElement.getAttribute("propertyMap") : null;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {

        try {
            ObjectStore os = context.getConnection().getObjectStore();

            // Create a class workflow subscription.
            ClassSubscription subscription = Factory.ClassSubscription.createInstance(os, subscriptionClass);

            subscription.set_DisplayName(name);
            subscription.set_FilterExpression(filterExpression);
            subscription.set_FilteredPropertyId(filteredPropertyId);
            subscription.set_IncludeSubclassesRequested(this.includeSubclasses);

            // Get the target class.
            SubscribableClassDefinition targetObject = Factory.SubscribableClassDefinition.fetchInstance(os, targetClass, null);
            subscription.set_SubscriptionTarget(targetObject);

            // Create a list of subscribed events.
            SubscribedEventList subEventList = createSubscribedEventList(os);
            // Set subscription properties.
            subscription.set_SubscribedEvents(subEventList);

            if (this.workflowDefinitionId != null) {

                ClassWorkflowSubscription cws = (ClassWorkflowSubscription) subscription;

                // Get the workflow definition document. Calls method getWorkFlowDefinitionDocument.
                WorkflowDefinition workflowDefDocument = getWorkFlowDefinitionDocument(context, workflowDefinitionId);

                // Get the event action.
                SearchSQL sql = new SearchSQL(Constants.QUERY_EXIST_WORKFLOW_EVENT_ACTION);
                SearchScope ss = new SearchScope(os);

                EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
                Iterator<?> it = ec.iterator();

                if (!it.hasNext()) {
                    throw new IllegalStateException("The WorkflowEventAction must exist.");
                }

                WorkflowEventAction eventAction = (WorkflowEventAction) it.next();

                cws.set_WorkflowDefinition(workflowDefDocument);
                cws.set_EventAction(eventAction);
                cws.set_EnableManualLaunch(this.enableManualLaunch);

                if (propertyMap != null && !propertyMap.isEmpty()) {
                    StringList stringList = Factory.StringList.createList();
                    String[] props = propertyMap.split(";");
                    for (int i = 0; i < props.length; i++) {
                        stringList.add(props[i].trim());
                    }
                    cws.set_PropertyMap(stringList);
                }

                cws.set_VWVersion(workflowDefDocument.get_VWVersion());
                PEConnectionPoint peConnPoint = Factory.PEConnectionPoint.fetchInstance(os.get_Domain(), context.getConnection().getConnectionPoint(), null);
                cws.set_IsolatedRegionNumber(peConnPoint.get_IsolatedRegion().get_IsolatedRegionNumber());

            } else if (this.eventActionId != null) {

                subscription.set_IsSynchronous(synchronous);

                EventAction eventAction = Factory.EventAction.fetchInstance(os, new Id(context.interpretId(eventActionId)), null);
                subscription.set_EventAction(eventAction);
            }


            // Save the subscription.
            subscription.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));

            return subscription.get_Id().toString();
        } catch (Exception e) {
            throw new OperationExecutionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected SubscribedEventList createSubscribedEventList(ObjectStore os) {

        SubscribedEventList subEventList = Factory.SubscribedEvent.createList();

        String[] events = eventList.split(";");

        for (int i = 0; i < events.length; i++) {

            Id eventId;
            if (CheckoutEvent.class.getSimpleName().equalsIgnoreCase(events[i].trim())) {
                eventId = GuidConstants.Class_CheckoutEvent;
            } else if (CheckinEvent.class.getSimpleName().equalsIgnoreCase(events[i].trim())) {
                eventId = GuidConstants.Class_CheckinEvent;
            } else if (UpdateEvent.class.getSimpleName().equalsIgnoreCase(events[i].trim())) {
                eventId = GuidConstants.Class_UpdateEvent;
            } else if (CreationEvent.class.getSimpleName().equalsIgnoreCase(events[i].trim())) {
                eventId = GuidConstants.Class_CreationEvent;
            } else if (DeletionEvent.class.getSimpleName().equalsIgnoreCase(events[i].trim())) {
                eventId = GuidConstants.Class_DeletionEvent;
            } else if (ChangeClassEvent.class.getSimpleName().equalsIgnoreCase(events[i].trim())) {
                eventId = GuidConstants.Class_ChangeClassEvent;
            } else if (ChangeStateEvent.class.getSimpleName().equalsIgnoreCase(events[i].trim())) {
                eventId = GuidConstants.Class_ChangeStateEvent;
            } else {
                throw new UnsupportedOperationException("Unknown event : " + events[i]);
            }
            EventClassDefinition evDef = Factory.EventClassDefinition.getInstance(os, eventId);
            SubscribedEvent subEvent = Factory.SubscribedEvent.createInstance();
            subEvent.set_EventClass(evDef);
            subEventList.add(subEvent);
        }

        return subEventList;
    }

    protected WorkflowDefinition getWorkFlowDefinitionDocument(ExecutionContext context, String workflowDefinitionId) throws Exception {

        ObjectStore os = context.getConnection().getObjectStore();

        // Get the Workflow Definition document object.
        PropertyFilter pf = FileNetCEApiUtil.createFilter(new String[] { PropertyNames.VWVERSION, PropertyNames.ID });
        WorkflowDefinition wfdDocument = Factory.WorkflowDefinition.fetchInstance(os, new Id(context.interpretId(workflowDefinitionId)), pf);

        // Get the current VW version number.
        String vwCurrentVersion = wfdDocument.get_VWVersion();

        // Calls method getVWSession to get the VWSession object, which represents the Process Engine session.
        VWSession vwSession = context.getConnection().getVWSession();

        if (transferWorkflowDefinition) {

            if (vwCurrentVersion == null || !vwSession.checkWorkflowIdentifier(vwCurrentVersion)) {
                TransferWorkflow transferWorkflow = new TransferWorkflow(wfdDocument);
                transferWorkflow.execute(context);

                wfdDocument = Factory.WorkflowDefinition.fetchInstance(os, wfdDocument.get_Id(), pf);
            }
        }

        return wfdDocument;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create subscription " + name;
    }
}
