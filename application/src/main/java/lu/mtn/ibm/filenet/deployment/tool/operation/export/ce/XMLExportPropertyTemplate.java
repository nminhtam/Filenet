/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.admin.LocalizedString;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.admin.PropertyTemplateBoolean;
import com.filenet.api.admin.PropertyTemplateDateTime;
import com.filenet.api.admin.PropertyTemplateFloat64;
import com.filenet.api.admin.PropertyTemplateId;
import com.filenet.api.admin.PropertyTemplateInteger32;
import com.filenet.api.admin.PropertyTemplateString;
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
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportPropertyTemplate extends AbstractXMLExportOperation<PropertyTemplate> implements ExecutableOperationOnQuery {

    private String id;

    private String name;

    private boolean exportChoiceList;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportPropertyTemplate(String xml) throws OperationInitializationException {
        super(xml);
    }


    public XMLExportPropertyTemplate(String id, String operation, String writerName, boolean exportChoiceList) throws OperationInitializationException {
        super(operation, writerName);

        this.id = id;
        this.exportChoiceList = exportChoiceList;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_ID, id), true, "The propertyTemplate with id " + id + " must exist."));
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) {

        if (rootElement.hasAttribute("id")) {
            id = rootElement.getAttribute("id");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_ID, id), true, "The propertyTemplate with id " + id + " must exist."));

        } else {
            name = rootElement.getAttribute("name");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_NAME, name), true, "The propertyTemplate with name " + name + " must exist."));

        }
        exportChoiceList = rootElement.hasAttribute("exportChoiceList") ? Boolean.valueOf(rootElement.getAttribute("exportChoiceList")) : Constants.DEFAULT_EXPORT_CHOICE_LISTS;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "PROP_TEMP_" + (id != null ? id : name);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected PropertyTemplate findObject(ExecutionContext context) {

        ObjectStore os = context.getConnection().getObjectStore();

        SearchSQL sql = new SearchSQL(id != null ? String.format(Constants.QUERY_EXPORT_PROPERTY_TEMPLATE_ID, id) : String.format(Constants.QUERY_EXPORT_PROPERTY_TEMPLATE_NAME, name));
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The propertyTemplate " + name + " must exist.");
        }
        return (PropertyTemplate) it.next();
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(PropertyTemplate object) {
        return object.get_Id().toString();
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(PropertyTemplate propertyTemplate, Document doc, ExecutionContext context) throws OperationExecutionException {
        Element root = doc.getDocumentElement();
        root.setAttribute("name", propertyTemplate.get_SymbolicName());
        if (!propertyTemplate.get_SymbolicName().equals(propertyTemplate.get_DisplayName())) {
            root.setAttribute("displayName", propertyTemplate.get_DisplayName());
        }
        root.setAttribute("type", String.valueOf(propertyTemplate.get_DataType().getValue()));
        root.setAttribute("cardinality", String.valueOf(propertyTemplate.get_Cardinality().getValue()));
        if (propertyTemplate.get_ChoiceList() != null) {

            String choiceListName = propertyTemplate.get_ChoiceList().get_DisplayName();

            if (exportChoiceList) {
                root.setAttribute("choiceListName", choiceListName);
                try {
                    XMLExportChoiceList exportChoiceList = new XMLExportChoiceList(propertyTemplate.get_ChoiceList().get_Id().toString(), this.operation, writerName);
                    exportChoiceList.execute(context);
                } catch (OperationInitializationException e) {
                    throw new OperationExecutionException(e);
                }
            } else {
                root.setAttribute("choiceListName", choiceListName);
            }
        }
        if (propertyTemplate.get_IsHidden() != null) {
            root.setAttribute("hidden", propertyTemplate.get_IsHidden().toString());
        }
        if (propertyTemplate.get_IsValueRequired() != null) {
            root.setAttribute("valueRequired", propertyTemplate.get_IsValueRequired().toString());
        }
        setTypeProperties(propertyTemplate, root, doc);

        this.write(context, doc);
    }

    @SuppressWarnings("unchecked")
    protected void writeLocalizedNames(Document doc, ExecutionContext context, PropertyTemplate propertyTemplate) {
        LocalizedStringList localizedStringList = propertyTemplate.get_DisplayNames();
        if (localizedStringList != null && !localizedStringList.isEmpty() && context.getI18nWriter() != null) {
            for (Iterator<LocalizedString> it = localizedStringList.iterator(); it.hasNext(); ) {
                LocalizedString loc = it.next();
                context.getI18nWriter().write(new String[] { loc.get_LocaleName(), "property." + doc.getDocumentElement().getAttribute("name"), loc.get_LocalizedText() });
            }
        }
    }
    
    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    @Override
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_PROPERTY_TEMPLATE;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_PROPERTY_TEMPLATE;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportPropertyTemplate.");
    }

    protected void setTypeProperties(PropertyTemplate propertyTemplate, Element root, Document doc) {
        switch (propertyTemplate.get_DataType().getValue()) {

            case TypeID.STRING_AS_INT:
                PropertyTemplateString propStr = (PropertyTemplateString) propertyTemplate;
                if (propStr.get_PropertyDefaultString() != null) {
                    root.setAttribute("defaultValue", propStr.get_PropertyDefaultString());
                }
                if (propStr.get_MaximumLengthString() != null) {
                    root.setAttribute("maxLength", propStr.get_MaximumLengthString().toString());
                }
                if (propStr.get_UsesLongColumn() != null) {
                    root.setAttribute("usesLongColumn", propStr.get_UsesLongColumn().toString());
                }
                break;

            case TypeID.LONG_AS_INT:
                PropertyTemplateInteger32 propInt = (PropertyTemplateInteger32) propertyTemplate;
                if (propInt.get_PropertyDefaultInteger32() != null) {
                    root.setAttribute("defaultValue", propInt.get_PropertyDefaultInteger32().toString());
                }
                if (propInt.get_PropertyMinimumInteger32() != null) {
                    root.setAttribute("min", propInt.get_PropertyMinimumInteger32().toString());
                }
                if (propInt.get_PropertyMaximumInteger32() != null) {
                    root.setAttribute("max", propInt.get_PropertyMaximumInteger32().toString());
                }
                break;

            case TypeID.BOOLEAN_AS_INT:
                PropertyTemplateBoolean propBool = (PropertyTemplateBoolean) propertyTemplate;
                if (propBool.get_PropertyDefaultBoolean() != null) {
                    root.setAttribute("defaultValue", propBool.get_PropertyDefaultBoolean().toString());
                }
                break;

            case TypeID.DOUBLE_AS_INT:
                PropertyTemplateFloat64 propDouble = (PropertyTemplateFloat64) propertyTemplate;
                if (propDouble.get_PropertyDefaultFloat64() != null) {
                    root.setAttribute("defaultValue", propDouble.get_PropertyDefaultFloat64().toString());
                }
                if (propDouble.get_PropertyMinimumFloat64() != null) {
                    root.setAttribute("min", propDouble.get_PropertyMinimumFloat64().toString());
                }
                if (propDouble.get_PropertyMaximumFloat64() != null) {
                    root.setAttribute("max", propDouble.get_PropertyMaximumFloat64().toString());
                }
                break;

            case TypeID.DATE_AS_INT:
                PropertyTemplateDateTime propDate = (PropertyTemplateDateTime) propertyTemplate;
                if (propDate.get_PropertyDefaultDateTime() != null) {
                    root.setAttribute("defaultValue", new SimpleDateFormat(Constants.PATTERN_DATE_TIME).format(propDate.get_PropertyDefaultDateTime()));
                }
                break;

            case TypeID.GUID_AS_INT:
                PropertyTemplateId propId = (PropertyTemplateId) propertyTemplate;
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
        return "Export PropertyTemplate " + (id != null ? " with id " + id : " with name " + name);
    }


    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
