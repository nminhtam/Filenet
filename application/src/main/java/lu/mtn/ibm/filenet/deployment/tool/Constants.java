/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool;

/**
 * @author NguyenT
 *
 */
public class Constants {

    public static final String PATTERN_DATE_TIME = "dd/MM/yyyy HH:mm:ss.SSS";

    public static final String TAG_CREATE_CHOICE_LIST = "createChoiceList";
    public static final String TAG_CREATE_PROPERTY_TEMPLATE = "createPropertyTemplate";
    public static final String TAG_CREATE_CLASS = "createClass";
    public static final String TAG_CREATE_DOC_CONTENT = "createDocument";
    public static final String TAG_CREATE_CUSTOM_OBJECT = "createCustomObject";
    public static final String TAG_CREATE_FOLDER = "createFolder";
    public static final String TAG_CREATE_CLASS_SUBSCRIPTION = "createClassSubscription";
    public static final String TAG_CREATE_STORED_SEARCH = "createStoredSearch";
    public static final String TAG_CREATE_CODE_MODULE = "createCodeModule";
    public static final String TAG_CREATE_EVENT_ACTION = "createEventAction";
    public static final String TAG_CREATE_LOCAL_FILE = "createLocalFile";

    public static final String TAG_CREATE_QUEUE_CONFIG = "createQueueConfig";
    public static final String TAG_CREATE_EVENT_LOG_CONFIG = "createEventLogConfig";
    public static final String TAG_CREATE_ROSTER_CONFIG = "createRosterConfig";

    public static final String TAG_EXPORT_CHOICE_LIST = "exportChoiceList";
    public static final String TAG_EXPORT_PROPERTY_TEMPLATE = "exportPropertyTemplate";
    public static final String TAG_EXPORT_CLASS = "exportClass";
    public static final String TAG_EXPORT_DOC_CONTENT = "exportDocument";
    public static final String TAG_EXPORT_CUSTOM_OBJECT = "exportCustomObject";
    public static final String TAG_EXPORT_CLASS_WORKFLOW_SUBSCRIPTION = "exportClassWorkflowSubscription";
    public static final String TAG_EXEC_QUERY_RESULT_OP = "executeOperationOnQueryResult";
    public static final String TAG_EXPORT_STORED_SEARCH = "exportStoredSearch";
    public static final String TAG_EXPORT_CODE_MODULE = "exportCodeModule";
    public static final String TAG_EXPORT_EVENT_ACTION = "exportEventAction";
    public static final String TAG_EXPORT_CLASS_SUBSCRIPTION = "exportClassSubscription";
    public static final String TAG_EXPORT_LOCAL_FILE = "exportLocalFile";

    public static final String TAG_EXPORT_SECURITY_CLASS = "exportClassSecurity";
    public static final String TAG_EXPORT_SECURITY_FOLDER = "exportFolderSecurity";
    public static final String TAG_EXPORT_SECURITY_OBJECT_STORE = "exportObjectStoreSecurity";

    public static final String TAG_EXPORT_QUEUE_CONFIG = "exportQueueConfig";
    public static final String TAG_EXPORT_EVENT_LOG_CONFIG = "exportEventLogConfig";
    public static final String TAG_EXPORT_ROSTER_CONFIG = "exportRosterConfig";
    public static final String TAG_EXPORT_REGISTERED_LIBS_CONFIG = "exportRegisteredLibsConfig";

    public static final String TAG_WRITE_OUTPUT_XML = "writeOuputXML";
    public static final String TAG_STORE_ID_REFERENCE = "storeIdReference";
    public static final String TAG_STORE_ID_REFERENCES = "storeIdReferences";
    public static final String TAG_REGEX_CONTENT_MODIFIER = "regexContentModifier";
    public static final String TAG_TRANSFER_WORKFLOW = "transferWorkflow";

    public static final String TAG_SECURITY_MAPPER_CLASS = "classSecurityMapper";
    public static final String TAG_SECURITY_MAPPER_FOLDER = "folderSecurityMapper";
    public static final String TAG_SECURITY_MAPPER_WORKFLOW_DEF = "workflowDefinitionSecurityMapper";
    public static final String TAG_SECURITY_MAPPER_OBJECTSTORE = "objectStoreSecurityMapper";

    public static final String TAG_UPDATE_DOC_CONTENT = "updateDocument";
    public static final String TAG_UPDATE_CUSTOM_OBJ = "updateCustomObject";
    public static final String TAG_UPDATE_CHOICE_LIST = "updateChoiceList";
    public static final String TAG_UPDATE_PROPERTY_TEMPLATE = "updatePropertyTemplate";
    public static final String TAG_UPDATE_CLASS = "updateClass";

    public static final String TAG_UPDATE_SECURITY_CLASS = "updateClassSecurity";
    public static final String TAG_UPDATE_SECURITY_FOLDER = "updateFolderSecurity";
    public static final String TAG_UPDATE_SECURITY_OBJECT_STORE = "updateObjectStoreSecurity";

    public static final String TAG_UPDATE_QUEUE_CONFIG = "updateQueueConfig";
    public static final String TAG_UPDATE_EVENT_LOG_CONFIG = "updateEventLogConfig";
    public static final String TAG_UPDATE_ROSTER_CONFIG = "updateRosterConfig";
    public static final String TAG_UPDATE_REGISTERED_LIBS_CONFIG = "updateRegisteredLibsConfig";

    public static final String TAG_DELETE_OBJ = "delete";

    public static final String QUERY_EXPORT_CHOICE_LIST_ID = "SELECT [Id], [ChoiceValues], [DisplayName], [DataType] FROM [ChoiceList] WHERE [Id] = %s";
    public static final String QUERY_EXPORT_CHOICE_LIST_NAME = "SELECT [Id], [ChoiceValues], [DisplayName], [DataType] FROM [ChoiceList] WHERE [DisplayName] = '%s'";
    public static final String QUERY_EXPORT_PROPERTY_TEMPLATE_ID = "SELECT [Id], [SymbolicName], [DisplayName], [DisplayNames], [DataType], [Cardinality], [ChoiceList], [IsHidden], [IsValueRequired], [PropertyDefaultInteger32], [PropertyMinimumInteger32], [PropertyMaximumInteger32], [PropertyDefaultString], [MaximumLengthString], [PropertyDefaultBoolean], [PropertyDefaultFloat64], [PropertyMinimumFloat64], [PropertyMaximumFloat64], [PropertyDefaultDateTime], [PropertyDefaultId], [UsesLongColumn] FROM [PropertyTemplate] WHERE [Id] = %s";
    public static final String QUERY_EXPORT_PROPERTY_TEMPLATE_NAME = "SELECT [Id], [SymbolicName], [DisplayName], [DisplayNames], [DataType], [Cardinality], [ChoiceList], [IsHidden], [IsValueRequired], [PropertyDefaultInteger32], [PropertyMinimumInteger32], [PropertyMaximumInteger32], [PropertyDefaultString], [MaximumLengthString], [PropertyDefaultBoolean], [PropertyDefaultFloat64], [PropertyMinimumFloat64], [PropertyMaximumFloat64], [PropertyDefaultDateTime], [PropertyDefaultId], [UsesLongColumn] FROM [PropertyTemplate] WHERE [SymbolicName] = '%s'";
    public static final String QUERY_EXPORT_CLASS_ID = "SELECT [SymbolicName], [DisplayName], [DisplayNames], [SuperclassDefinition], [ClassDescription], [PropertyDefinitions], [ImmediateSubclassDefinitions] FROM [ClassDefinition] WHERE [Id] = %s";
    public static final String QUERY_EXPORT_CLASS_NAME = "SELECT [SymbolicName], [DisplayName], [DisplayNames], [SuperclassDefinition], [ClassDescription], [PropertyDefinitions], [ImmediateSubclassDefinitions] FROM [ClassDefinition] WHERE [SymbolicName] = '%s'";
    public static final String QUERY_EXPORT_CLASS_WORKFLOW_SUBSCRIPTION_ID = "SELECT [DisplayName], [EnableManualLaunch], [FilterExpression], [FilteredPropertyId], [IncludeSubclassesRequested], [IsEnabled], [IsSynchronous], [Name], [PropertyMap], [SubscribedEvents], [WorkflowDefinition], [SubscriptionTarget] FROM [ClassWorkflowSubscription] WHERE [Id] = %s";
    public static final String QUERY_EXPORT_CLASS_WORKFLOW_SUBSCRIPTION_NAME = "SELECT [DisplayName], [EnableManualLaunch], [FilterExpression], [FilteredPropertyId], [IncludeSubclassesRequested], [IsEnabled], [IsSynchronous], [Name], [PropertyMap], [SubscribedEvents], [WorkflowDefinition], [SubscriptionTarget] FROM [ClassWorkflowSubscription] WHERE [DisplayName] = '%s'";
    public static final String QUERY_EXPORT_CLASS_SUBSCRIPTION_ID = "SELECT [DisplayName], [FilterExpression], [FilteredPropertyId], [IncludeSubclassesRequested], [IsEnabled], [IsSynchronous], [Name], [PropertyMap], [SubscribedEvents], [EventAction], [SubscriptionTarget], [EnableManualLaunch], [WorkflowDefinition] FROM [ClassSubscription] WHERE [Id] = %s";
    public static final String QUERY_EXPORT_CLASS_SUBSCRIPTION_NAME = "SELECT [DisplayName], [FilterExpression], [FilteredPropertyId], [IncludeSubclassesRequested], [IsEnabled], [IsSynchronous], [Name], [PropertyMap], [SubscribedEvents], [EventAction], [SubscriptionTarget], [EnableManualLaunch], [WorkflowDefinition] FROM [ClassSubscription] WHERE [DisplayName] = '%s'";
    public static final String QUERY_EXPORT_STORED_SEARCH_ID = "SELECT [This] FROM [StoredSearch] WHERE [Id] = %s";

    public static final String QUERY_EXIST_CHOICE_LIST_NAME = "SELECT [This] FROM [ChoiceList] WHERE [DisplayName] = '%s'";
    public static final String QUERY_EXIST_PROPERTY_TEMPLATE_ID =  "SELECT [This], [DataType] FROM [PropertyTemplate] WHERE [Id] = %s";
    public static final String QUERY_EXIST_PROPERTY_TEMPLATE_NAME =  "SELECT [This], [SymbolicName], [DisplayName], [DataType], [Cardinality], [ChoiceList], [IsHidden], [IsValueRequired], [PropertyDefaultInteger32], [PropertyMinimumInteger32], [PropertyMaximumInteger32], [PropertyDefaultString], [MaximumLengthString], [PropertyDefaultBoolean], [PropertyDefaultFloat64], [PropertyMinimumFloat64], [PropertyMaximumFloat64], [PropertyDefaultDateTime], [PropertyDefaultId] FROM [PropertyTemplate] WHERE [SymbolicName] = '%s'";
    public static final String QUERY_EXIST_CLASS_NAME = "SELECT [This] FROM [ClassDefinition] WHERE [SymbolicName] = '%s'";
    public static final String QUERY_EXIST_FOLDER_NAME = "SELECT [This] FROM [Folder] WHERE [This] = OBJECT('%s')";
    public static final String QUERY_EXIST_WORKFLOW_EVENT_ACTION = "SELECT [This] FROM [WorkflowEventAction]";
    public static final String QUERY_EXIST_CLASS_SUBSCRIPTION_NAME = "SELECT [This] FROM [ClassSubscription] WHERE [DisplayName] = '%s'";
    public static final String QUERY_EXIST_CLASS_WORKFLOW_SUBSCRIPTION_NAME = "SELECT [This] FROM [ClassWorkflowSubscription] WHERE [DisplayName] = '%s'";
    public static final String QUERY_EXIST_STORED_SEARCH_ID = "SELECT [This] FROM [StoredSearch] WHERE [Id] = %s";
    public static final String QUERY_EXIST_CODE_MODULE_EVENT_ID = "SELECT [This] FROM [EventAction] WHERE [Id] = %s AND [CodeModule] IS NOT NULL";
    public static final String QUERY_EXIST_OBJECT_ID = "SELECT [This] FROM [%s] WHERE [Id] = %s";

    public static final String QUERY_ALREADY_CHECKOUT_DOCUMENT_ID = "SELECT [This] FROM [Document] WHERE [Id] = %s AND [IsReserved] = False";

    public static final String QUERY_EXPORT_SECURITY_CLASS_ID = "SELECT [SymbolicName], [ImmediateSubclassDefinitions], [DefaultInstancePermissions], [Permissions] FROM [ClassDefinition] WHERE [Id] = %s";
    public static final String QUERY_EXPORT_SECURITY_CLASS_NAME = "SELECT [SymbolicName], [ImmediateSubclassDefinitions], [DefaultInstancePermissions], [Permissions] FROM [ClassDefinition] WHERE [SymbolicName] = '%s'";
    public static final String QUERY_EXPORT_SECURITY_FOLDER_NAME = "SELECT [This], [PathName], [Permissions], [SubFolders] FROM [Folder] WHERE [This] = OBJECT('%s')";

    public static final String EXPORTED = "exported";
    public static final String OPERATION_MODIFIERS = "OperationModifiers";

    public static final int PROPERTY_TYPE_STRING = 0;
    public static final int PROPERTY_TYPE_INT = 1;
    public static final int PROPERTY_TYPE_FLOAT = 2;
    public static final int PROPERTY_TYPE_BINARY = 3;
    public static final int PROPERTY_TYPE_BOOLEAN = 4;
    public static final int PROPERTY_TYPE_DATE_TIME = 5;
    public static final int PROPERTY_TYPE_ID = 6;
    public static final int PROPERTY_TYPE_STRING_ARRAY = 7;
    public static final int PROPERTY_TYPE_INT_ARRAY = 8;
    public static final int PROPERTY_TYPE_FLOAT_ARRAY = 9;
    public static final int PROPERTY_TYPE_BINARY_ARRAY = 10;
    public static final int PROPERTY_TYPE_BOOLEAN_ARRAY = 11;
    public static final int PROPERTY_TYPE_DATE_TIME_ARRAY = 12;
    public static final int PROPERTY_TYPE_ID_ARRAY = 13;


    public static boolean DEFAULT_ZIP_CONTENT = true;
    public static boolean DEFAULT_ADD_FOLDER_PATH = true;
    public static boolean DEFAULT_ADD_ID_REF = true;
    public static boolean DEFAULT_ADD_UPDATE_ID_REF = true;
    public static boolean DEFAULT_EXPORT_SUB_FOLDERS = false;
    public static boolean DEFAULT_EXPORT_PROPERTY_TEMPLATES = true;
    public static boolean DEFAULT_EXPORT_CHOICE_LISTS = true;
    public static boolean DEFAULT_EXPORT_SUB_CLASSES = false;
    public static boolean DEFAULT_UPDATE_KEEP_VERSIONS = true;
    public static boolean DEFAULT_EXPORT_WORKFLOW_DEF = true;
    public static boolean DEFAULT_TRANSFER_WORKFLOW_DEF = true;
    public static boolean DEFAULT_EXPORT_CODE_MODULE = true;
    public static boolean DEFAULT_EXPORT_EVENT_ACTION = true;
    public static boolean DEFAULT_USE_WORKFLOW_DEF_ID = false;
    public static boolean DEFAULT_EXPORT_DEFAULT_INSTANCE_PERM = true;
    public static boolean DEFAULT_EXPORT_PERM = true;
}
