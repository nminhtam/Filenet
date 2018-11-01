/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.security;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportClassSecurity extends AbstractXMLExportSecurity<ClassDefinition> {

    private String id;

    private String name;

    private boolean exportSubClasses;

    private boolean exportPermissions;

    private boolean exportDefaultInstancePermissions;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportClassSecurity(String xml) throws OperationInitializationException {
        super(xml);
    }

    protected XMLExportClassSecurity(String name, String operation, String writerName, boolean exportSubClasses, boolean exportDefaultInstancePermissions, boolean exportPermissions) throws OperationInitializationException {
        super(operation, writerName);
        this.name = name;
        this.exportSubClasses = exportSubClasses;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, name), true, "The class with name " + name + " must exist."));

        this.exportDefaultInstancePermissions = exportDefaultInstancePermissions;
        this.exportPermissions = exportPermissions;
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        if (rootElement.hasAttribute("id")) {
            id = rootElement.getAttribute("id");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "ClassDefinition", id), true, "The class with id " + id + " must exist."));

        } else {
            name = rootElement.getAttribute("name");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, name), true, "The class with name " + name + " must exist."));

        }
        exportSubClasses = rootElement.hasAttribute("exportSubClasses") ? Boolean.valueOf(rootElement.getAttribute("exportSubClasses")) : Constants.DEFAULT_EXPORT_SUB_CLASSES;
        exportDefaultInstancePermissions = rootElement.hasAttribute("exportDefaultInstancePermissions") ? Boolean.valueOf(rootElement.getAttribute("exportDefaultInstancePermissions")) : Constants.DEFAULT_EXPORT_DEFAULT_INSTANCE_PERM;
        exportPermissions = rootElement.hasAttribute("exportPermissions") ? Boolean.valueOf(rootElement.getAttribute("exportPermissions")) : Constants.DEFAULT_EXPORT_PERM;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "CLASS-SEC_" + (id != null ? id : name);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected ClassDefinition findObject(ExecutionContext context) {
        ObjectStore os = context.getConnection().getObjectStore();

        SearchSQL sql = new SearchSQL(id != null ? String.format(Constants.QUERY_EXPORT_SECURITY_CLASS_ID, id) : String.format(Constants.QUERY_EXPORT_SECURITY_CLASS_NAME, name));
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The class " + name + " must exist.");
        }
        return (ClassDefinition) it.next();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(ClassDefinition classDefinition) {
        return classDefinition.get_Id().toString();
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void writeDetails(ClassDefinition classDefinition, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("name", classDefinition.get_SymbolicName());

        if (exportDefaultInstancePermissions) {
            Element permissionsElement = createElement(doc, root, "defaultInstancePermissions");
            writePermissions(permissionsElement, doc, classDefinition.get_DefaultInstancePermissions());
        }
        if (exportPermissions) {
            Element permissionsElement = createElement(doc, root, "permissions");
            writePermissions(permissionsElement, doc, classDefinition.get_Permissions());
        }

        this.write(context, doc);

        if (exportSubClasses) {
            try {
                for (Iterator<ClassDefinition> iter = classDefinition.get_ImmediateSubclassDefinitions().iterator(); iter.hasNext(); ) {

                    ClassDefinition classDef = iter.next();
                    XMLExportClassSecurity exportClass = new XMLExportClassSecurity(classDef.get_SymbolicName(), operation, writerName, exportSubClasses, exportDefaultInstancePermissions, exportPermissions);
                    exportClass.execute(context);
                }
            } catch (OperationInitializationException e) {
                throw new OperationExecutionException(e);
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        return Constants.TAG_UPDATE_SECURITY_CLASS;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export Class Security " + (id != null ? "with id " + id : "with name " + name);
    }

//    protected User findUser(ExecutionContext context, String user) {
//        return Factory.User.fetchInstance(context.getConnection().getObjectStore().getConnection(), user, null);
//    }
}
