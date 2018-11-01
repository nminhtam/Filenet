/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.LocalizedString;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionBinary;
import com.filenet.api.admin.PropertyDefinitionBoolean;
import com.filenet.api.admin.PropertyDefinitionDateTime;
import com.filenet.api.admin.PropertyDefinitionFloat64;
import com.filenet.api.admin.PropertyDefinitionId;
import com.filenet.api.admin.PropertyDefinitionInteger32;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.collection.LocalizedStringList;
import com.filenet.api.constants.TypeID;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.comparator.ClassDefinitionComparator;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.comparator.PropertyDefinitionComparator;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportClass extends AbstractXMLExportOperation<ClassDefinition> implements ExecutableOperationOnQuery {

    private String id;

    private String name;

    private boolean exportPropertyTemplates;

    private boolean exportChoiceList;

    private boolean exportSubClasses;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportClass(String xml) throws OperationInitializationException {
        super(xml);
    }


    protected XMLExportClass(String name, String operation, String writerName, boolean exportPropertyTemplates, boolean exportChoiceList, boolean exportSubClasses) throws OperationInitializationException {
        super(operation, writerName);
        this.name = name;
        this.exportChoiceList = exportChoiceList;
        this.exportPropertyTemplates = exportPropertyTemplates;
        this.exportSubClasses = exportSubClasses;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, name), true, "The class with name " + name + " must exist."));
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) {

        if (rootElement.hasAttribute("id")) {
            id = rootElement.getAttribute("id");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "ClassDefinition", id), true, "The class with id " + id + " must exist."));

        } else {
            name = rootElement.getAttribute("name");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, name), true, "The class with name " + name + " must exist."));

        }

        exportPropertyTemplates = rootElement.hasAttribute("exportPropertyTemplates") ? Boolean.valueOf(rootElement.getAttribute("exportPropertyTemplates")) : Constants.DEFAULT_EXPORT_PROPERTY_TEMPLATES;
        exportChoiceList = rootElement.hasAttribute("exportChoiceList") ? Boolean.valueOf(rootElement.getAttribute("exportChoiceList")) : Constants.DEFAULT_EXPORT_CHOICE_LISTS;
        exportSubClasses = rootElement.hasAttribute("exportSubClasses") ? Boolean.valueOf(rootElement.getAttribute("exportSubClasses")) : Constants.DEFAULT_EXPORT_SUB_CLASSES;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "CLASS_" + (id != null ? id : name);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected ClassDefinition findObject(ExecutionContext context) {
        ObjectStore os = context.getConnection().getObjectStore();

        SearchSQL sql = new SearchSQL(id != null ? String.format(Constants.QUERY_EXPORT_CLASS_ID, id) : String.format(Constants.QUERY_EXPORT_CLASS_NAME, name));
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The propertyTemplate " + name + " must exist.");
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
        try {
            ClassDefinition parentClassDef = classDefinition.get_SuperclassDefinition();

            Set<String> parentProperties = extractInheritedProperties(parentClassDef);

            root.setAttribute("name", classDefinition.get_SymbolicName());
            if (parentClassDef != null) {
                root.setAttribute("parent", parentClassDef.get_SymbolicName());
            }
            if (!classDefinition.get_SymbolicName().equals(classDefinition.get_DisplayName())) {
                root.setAttribute("displayName", classDefinition.get_DisplayName());
            }
            
            this.writeLocalizedNames(doc, context, classDefinition);

            if (exportPropertyTemplates) {
                List<PropertyDefinition> sortedPropsList = this.sort(classDefinition.get_PropertyDefinitions().iterator(), new PropertyDefinitionComparator());
                for (PropertyDefinition propDefinition : sortedPropsList) {

                    if (!propDefinition.get_IsSystemOwned() && !parentProperties.contains(propDefinition.get_SymbolicName())) {

                        XMLExportPropertyTemplate exportPropertyTemplate = new XMLExportPropertyTemplate(propDefinition.get_PropertyTemplate().get_Id().toString(), this.operation, writerName, exportChoiceList);
                        exportPropertyTemplate.execute(context);
                    }
                }
            }

            // Iterate and keep the order of properties as created.
            for (Iterator<PropertyDefinition> iter = classDefinition.get_PropertyDefinitions().iterator(); iter.hasNext(); ) {
                PropertyDefinition propDefinition = iter.next();

                if (!propDefinition.get_IsSystemOwned() && !parentProperties.contains(propDefinition.get_SymbolicName())) {

                    Element propertyElement = createElement(doc, root, "property");
                    propertyElement.setAttribute("name", propDefinition.get_SymbolicName());

                    if (propDefinition.get_IsHidden() != null) {
                        propertyElement.setAttribute("hidden", String.valueOf(propDefinition.get_IsHidden()));
                    }
                    if (propDefinition.get_IsValueRequired() != null) {
                        propertyElement.setAttribute("valueRequired", String.valueOf(propDefinition.get_IsValueRequired()));
                    }

                    setProperties(propertyElement, doc, propDefinition);
                }
            }

            this.write(context, doc);

            if (exportSubClasses) {

                List<ClassDefinition> sortedClassesList = this.sort(classDefinition.get_ImmediateSubclassDefinitions().iterator(), new ClassDefinitionComparator());
                for (ClassDefinition classDef : sortedClassesList) {

                    XMLExportClass exportClass = new XMLExportClass(classDef.get_SymbolicName(), operation, writerName, exportPropertyTemplates, exportChoiceList, exportSubClasses);
                    exportClass.execute(context);
                }
            }

        } catch (OperationInitializationException e) {
            throw new OperationExecutionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void writeLocalizedNames(Document doc, ExecutionContext context, ClassDefinition classDefinition) {
        LocalizedStringList localizedStringList = classDefinition.get_DisplayNames();
        if (localizedStringList != null && !localizedStringList.isEmpty() && context.getI18nWriter() != null) {
            for (Iterator<LocalizedString> it = localizedStringList.iterator(); it.hasNext(); ) {
                LocalizedString loc = it.next();
                context.getI18nWriter().write(new String[] { loc.get_LocaleName(), "class." + doc.getDocumentElement().getAttribute("name"), loc.get_LocalizedText() });
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_CLASS;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_CLASS;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportClass.");
    }

    @SuppressWarnings("unchecked")
    public static Set<String> extractInheritedProperties(ClassDefinition parentClassDef) {
        Set<String> parentProperties = new HashSet<String>();
        if (parentClassDef != null) {
            for (Iterator<PropertyDefinition> iter = parentClassDef.get_PropertyDefinitions().iterator(); iter.hasNext(); ) {
                PropertyDefinition propDefinition = iter.next();
                parentProperties.add(propDefinition.get_SymbolicName());
            }
        }
        return parentProperties;
    }

    protected void setProperties(Element root, Document doc, PropertyDefinition propDefinition) {
        switch (propDefinition.get_DataType().getValue()) {

            case TypeID.BINARY_AS_INT:
                PropertyDefinitionBinary propBin = (PropertyDefinitionBinary) propDefinition;
                if (propBin.get_PropertyDefaultBinary() != null) {
                    root.setAttribute("defaultValue", new String(propBin.get_PropertyDefaultBinary()));
                }
                if (propBin.get_MaximumLengthBinary() != null) {
                    root.setAttribute("maxLength", String.valueOf(propBin.get_MaximumLengthBinary()));
                }
                break;

            case TypeID.STRING_AS_INT:
                PropertyDefinitionString propStr = (PropertyDefinitionString) propDefinition;
                if (propStr.get_PropertyDefaultString() != null) {
                    root.setAttribute("defaultValue", propStr.get_PropertyDefaultString());
                }
                if (propStr.get_MaximumLengthString() != null) {
                    root.setAttribute("maxLength", String.valueOf(propStr.get_MaximumLengthString()));
                }
                break;

            case TypeID.LONG_AS_INT:
                PropertyDefinitionInteger32 propInt = (PropertyDefinitionInteger32) propDefinition;
                if (propInt.get_PropertyDefaultInteger32() != null) {
                    root.setAttribute("defaultValue", String.valueOf(propInt.get_PropertyDefaultInteger32()));
                }
                if (propInt.get_PropertyMinimumInteger32() != null) {
                    root.setAttribute("min", String.valueOf(propInt.get_PropertyMinimumInteger32()));
                }
                if (propInt.get_PropertyMaximumInteger32() != null) {
                    root.setAttribute("max", String.valueOf(propInt.get_PropertyMaximumInteger32()));
                }
                break;

            case TypeID.BOOLEAN_AS_INT:
                PropertyDefinitionBoolean propBool = (PropertyDefinitionBoolean) propDefinition;
                if (propBool.get_PropertyDefaultBoolean() != null) {
                    root.setAttribute("defaultValue", String.valueOf(propBool.get_PropertyDefaultBoolean()));
                }
                break;

            case TypeID.DOUBLE_AS_INT:
                PropertyDefinitionFloat64 propDouble = (PropertyDefinitionFloat64) propDefinition;
                if (propDouble.get_PropertyDefaultFloat64() != null) {
                    root.setAttribute("defaultValue", String.valueOf(propDouble.get_PropertyDefaultFloat64()));
                }
                if (propDouble.get_PropertyMinimumFloat64() != null) {
                    root.setAttribute("min", String.valueOf(propDouble.get_PropertyMinimumFloat64()));
                }
                if (propDouble.get_PropertyMaximumFloat64() != null) {
                    root.setAttribute("max", String.valueOf(propDouble.get_PropertyMaximumFloat64()));
                }
                break;

            case TypeID.DATE_AS_INT:
                PropertyDefinitionDateTime propDate = (PropertyDefinitionDateTime) propDefinition;
                if (propDate.get_PropertyDefaultDateTime() != null) {
                    root.setAttribute("defaultValue", new SimpleDateFormat(Constants.PATTERN_DATE_TIME).format(propDate.get_PropertyDefaultDateTime()));
                    root.setAttribute("pattern", Constants.PATTERN_DATE_TIME);
                }
                break;

            case TypeID.GUID_AS_INT:
                PropertyDefinitionId propId = (PropertyDefinitionId) propDefinition;
                if (propId.get_PropertyDefaultId() != null) {
                    root.setAttribute("defaultValue", propId.get_PropertyDefaultId().toString());
                }
                break;

            default:
                break;
        }
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export Class " + (id != null ? "with id " + id : "with name " + name);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
