/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.execution;

import static lu.mtn.ibm.filenet.deployment.tool.Constants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateChoiceList;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateClass;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateClassSubscription;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateCodeModule;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateDocument;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateEventAction;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateFolder;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreatePropertyTemplate;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.pe.CreateEventLogConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.pe.CreateQueueConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.pe.CreateRosterConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.delete.ce.Delete;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportChoiceList;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportClass;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportClassSubscription;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportCodeModule;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportDocument;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportEventAction;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportLocalFile;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportPropertyTemplate;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportStoredSearch;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.security.XMLExportClassSecurity;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.security.XMLExportFolderSecurity;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.security.XmlExportObjectStoreSecurity;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.pe.XMLExportEventLogConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.pe.XMLExportQueueConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.pe.XMLExportRegisteredLibrariesConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.pe.XMLExportRosterConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.WriteOuputXML;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecuteOperationOnQueryResult;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.RegexDocumentContentModifier;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.StoreIdReference;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.StoreIdReferences;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.ce.ClassSecurityMapper;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.ce.FolderSecurityMapper;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.ce.ObjectStoreSecurityMapper;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.ce.WorkflowDefinitionSecurityMapper;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.TransferWorkflow;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.UpdateChoiceList;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.UpdateClass;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.UpdateCustomObject;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.UpdateDocument;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.UpdatePropertyTemplate;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.UpdateClassSecurity;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.UpdateFolderSecurity;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.security.UpdateObjectStoreSecurity;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.pe.UpdateEventLogConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.pe.UpdateQueueConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.pe.UpdateRegisteredLibrariesConfiguration;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.pe.UpdateRosterConfiguration;


/**
 * @author NguyenT
 *
 */
public class Execution {

    private Map<String, Class<? extends Operation>> operations;

    private List<Operation> tasks;

    private List<Operation> waitingTasks;

    /**
     *
     */
    public Execution() {

        this.operations = new HashMap<String, Class<? extends Operation>>();
        this.operations.put(TAG_CREATE_CHOICE_LIST, CreateChoiceList.class);
        this.operations.put(TAG_CREATE_PROPERTY_TEMPLATE, CreatePropertyTemplate.class);
        this.operations.put(TAG_CREATE_CLASS, CreateClass.class);
        this.operations.put(TAG_CREATE_DOC_CONTENT, CreateDocument.class);
        this.operations.put(TAG_CREATE_FOLDER, CreateFolder.class);
        this.operations.put(TAG_CREATE_CUSTOM_OBJECT, CreateFolder.class);
        this.operations.put(TAG_CREATE_CLASS_SUBSCRIPTION, CreateClassSubscription.class);
        this.operations.put(TAG_CREATE_CODE_MODULE, CreateCodeModule.class);
        this.operations.put(TAG_CREATE_EVENT_ACTION, CreateEventAction.class);
        this.operations.put(TAG_CREATE_QUEUE_CONFIG, CreateQueueConfiguration.class);
        this.operations.put(TAG_CREATE_EVENT_LOG_CONFIG, CreateEventLogConfiguration.class);
        this.operations.put(TAG_CREATE_ROSTER_CONFIG, CreateRosterConfiguration.class);

        this.operations.put(TAG_EXPORT_CHOICE_LIST, XMLExportChoiceList.class);
        this.operations.put(TAG_EXPORT_PROPERTY_TEMPLATE, XMLExportPropertyTemplate.class);
        this.operations.put(TAG_EXPORT_CLASS, XMLExportClass.class);
        this.operations.put(TAG_EXPORT_DOC_CONTENT, XMLExportDocument.class);
        this.operations.put(TAG_EXPORT_STORED_SEARCH, XMLExportStoredSearch.class);
        this.operations.put(TAG_EXPORT_CODE_MODULE, XMLExportCodeModule.class);
        this.operations.put(TAG_EXPORT_EVENT_ACTION, XMLExportEventAction.class);
        this.operations.put(TAG_EXPORT_CLASS_SUBSCRIPTION, XMLExportClassSubscription.class);
        this.operations.put(TAG_EXPORT_LOCAL_FILE, XMLExportLocalFile.class);
        this.operations.put(TAG_EXPORT_QUEUE_CONFIG, XMLExportQueueConfiguration.class);
        this.operations.put(TAG_EXPORT_EVENT_LOG_CONFIG, XMLExportEventLogConfiguration.class);
        this.operations.put(TAG_EXPORT_ROSTER_CONFIG, XMLExportRosterConfiguration.class);
        this.operations.put(TAG_EXPORT_REGISTERED_LIBS_CONFIG, XMLExportRegisteredLibrariesConfiguration.class);

        this.operations.put(TAG_EXPORT_SECURITY_CLASS, XMLExportClassSecurity.class);
        this.operations.put(TAG_EXPORT_SECURITY_FOLDER, XMLExportFolderSecurity.class);
        this.operations.put(TAG_EXPORT_SECURITY_OBJECT_STORE, XmlExportObjectStoreSecurity.class);

        this.operations.put(TAG_EXEC_QUERY_RESULT_OP, ExecuteOperationOnQueryResult.class);
        this.operations.put(TAG_WRITE_OUTPUT_XML, WriteOuputXML.class);
        this.operations.put(TAG_TRANSFER_WORKFLOW, TransferWorkflow.class);
        this.operations.put(TAG_UPDATE_DOC_CONTENT, UpdateDocument.class);
        this.operations.put(TAG_UPDATE_CUSTOM_OBJ, UpdateCustomObject.class);
        this.operations.put(TAG_UPDATE_CHOICE_LIST, UpdateChoiceList.class);
        this.operations.put(TAG_UPDATE_PROPERTY_TEMPLATE, UpdatePropertyTemplate.class);
        this.operations.put(TAG_UPDATE_CLASS, UpdateClass.class);
        this.operations.put(TAG_UPDATE_QUEUE_CONFIG, UpdateQueueConfiguration.class);
        this.operations.put(TAG_UPDATE_EVENT_LOG_CONFIG, UpdateEventLogConfiguration.class);
        this.operations.put(TAG_UPDATE_ROSTER_CONFIG, UpdateRosterConfiguration.class);
        this.operations.put(TAG_UPDATE_REGISTERED_LIBS_CONFIG, UpdateRegisteredLibrariesConfiguration.class);

        this.operations.put(TAG_UPDATE_SECURITY_CLASS, UpdateClassSecurity.class);
        this.operations.put(TAG_UPDATE_SECURITY_FOLDER, UpdateFolderSecurity.class);
        this.operations.put(TAG_UPDATE_SECURITY_OBJECT_STORE, UpdateObjectStoreSecurity.class);

        this.operations.put(TAG_DELETE_OBJ, Delete.class);

        this.operations.put(TAG_STORE_ID_REFERENCE, StoreIdReference.class);
        this.operations.put(TAG_STORE_ID_REFERENCES, StoreIdReferences.class);
        this.operations.put(TAG_REGEX_CONTENT_MODIFIER, RegexDocumentContentModifier.class);
        this.operations.put(TAG_SECURITY_MAPPER_CLASS, ClassSecurityMapper.class);
        this.operations.put(TAG_SECURITY_MAPPER_FOLDER, FolderSecurityMapper.class);
        this.operations.put(TAG_SECURITY_MAPPER_WORKFLOW_DEF, WorkflowDefinitionSecurityMapper.class);
        this.operations.put(TAG_SECURITY_MAPPER_OBJECTSTORE, ObjectStoreSecurityMapper.class);

        this.tasks = new ArrayList<Operation>();
    }

    /**
     * @return the tasks
     */
    public List<Operation> getTasks() {
        return this.tasks;
    }

    /**
     * @return the waitingTasks
     */
    public List<Operation> getWaitingTasks() {
        return this.waitingTasks;
    }

    /**
     * @return the operations
     */
    public Map<String, Class<? extends Operation>> getOperations() {
        return Collections.unmodifiableMap(this.operations);
    }
}
