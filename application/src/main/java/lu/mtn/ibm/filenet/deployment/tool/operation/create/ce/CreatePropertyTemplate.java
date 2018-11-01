/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;

import com.filenet.api.admin.ChoiceList;
import com.filenet.api.admin.LocalizedString;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.admin.PropertyTemplateBoolean;
import com.filenet.api.admin.PropertyTemplateDateTime;
import com.filenet.api.admin.PropertyTemplateFloat64;
import com.filenet.api.admin.PropertyTemplateId;
import com.filenet.api.admin.PropertyTemplateInteger32;
import com.filenet.api.admin.PropertyTemplateString;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.Cardinality;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.TypeID;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class CreatePropertyTemplate extends AbstractCreateOperation {

    private Cardinality cardinality;

    private Integer type;

    private String name;

    private String displayName;

    private String choiceListId;

    private String choiceListName;

    private boolean hidden;

    private boolean valueRequired;

    private String defaultValue;

    private Integer maxLength;

    private Double min, max;

    private boolean usesLongColumn;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreatePropertyTemplate(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element root, List<Prerequisite> prerequisites) {

        name = root.getAttribute("name");

        type = Integer.valueOf(root.getAttribute("type"));
        cardinality = Cardinality.getInstanceFromInt(Integer.valueOf(root.getAttribute("cardinality")));

        if (root.hasAttribute("displayName")) {
            this.displayName = root.getAttribute("displayName");
        }
        if (root.hasAttribute("hidden")) {
            hidden = Boolean.valueOf(root.getAttribute("hidden"));
        }
        if (root.hasAttribute("valueRequired")) {
            valueRequired = Boolean.valueOf(root.getAttribute("valueRequired"));
        }
        if (root.hasAttribute("defaultValue")) {
            defaultValue = root.getAttribute("defaultValue");
        }

        switch (type) {

            case TypeID.STRING_AS_INT:
                if (root.hasAttribute("maxLength")) {
                    maxLength = Integer.valueOf(root.getAttribute("maxLength"));
                }
                if (root.hasAttribute("usesLongColumn")) {
                    usesLongColumn = Boolean.valueOf(root.getAttribute("usesLongColumn"));
                }
                break;

            case TypeID.LONG_AS_INT:
            case TypeID.DOUBLE_AS_INT:
                if (root.hasAttribute("min")) {
                    min = Double.valueOf(root.getAttribute("min"));
                }
                if (root.hasAttribute("max")) {
                    max = Double.valueOf(root.getAttribute("max"));
                }
                break;

            default:
                break;
        }

        prerequisites.add(new ExistingObjectPrerequisite(false, String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_NAME, name), true, "The propertyTemplate " + name
                        + " must not exist."));
        if (root.hasAttribute("choiceListId")) {
            choiceListId = root.getAttribute("choiceListId");
            if (ExecutionContext.isIdMustBeInterpreted(choiceListId)) {
                prerequisites.add(new ExistingIdRefPrerequisite(choiceListId));
            } else {
                prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXPORT_CHOICE_LIST_ID, choiceListId), true, "The choiceList " + choiceListId
                            + " must exist."));
            }
        } else if (root.hasAttribute("choiceListName")) {
            choiceListName = root.getAttribute("choiceListName");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXPORT_CHOICE_LIST_NAME, choiceListName), true, "The choiceList " + choiceListName
                            + " must exist."));
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public String executeInternal(ExecutionContext context) {

        ObjectStore os = context.getConnection().getObjectStore();

        PropertyTemplate template;
        switch (type) {

            case TypeID.STRING_AS_INT:
                PropertyTemplateString propStr = Factory.PropertyTemplateString.createInstance(os);
                propStr.set_MaximumLengthString(maxLength);
                propStr.set_PropertyDefaultString(defaultValue);
                propStr.set_UsesLongColumn(usesLongColumn);
                template = propStr;
                break;

            case TypeID.LONG_AS_INT:
                PropertyTemplateInteger32 propInt = Factory.PropertyTemplateInteger32.createInstance(os);
                propInt.set_PropertyMinimumInteger32(min != null ? min.intValue() : null);
                propInt.set_PropertyMaximumInteger32(max != null ? max.intValue() : null);
                propInt.set_PropertyDefaultInteger32(defaultValue != null ? Integer.valueOf(defaultValue) : null);
                template = propInt;
                break;

            case TypeID.BOOLEAN_AS_INT:
                PropertyTemplateBoolean propBool = Factory.PropertyTemplateBoolean.createInstance(os);
                propBool.set_PropertyDefaultBoolean(defaultValue != null ? Boolean.valueOf(defaultValue) : null);
                template = propBool;
                break;

            case TypeID.DOUBLE_AS_INT:
                PropertyTemplateFloat64 propDouble = Factory.PropertyTemplateFloat64.createInstance(os);
                propDouble.set_PropertyDefaultFloat64(defaultValue != null ? Double.valueOf(defaultValue) : null);
                propDouble.set_PropertyMinimumFloat64(min);
                propDouble.set_PropertyMaximumFloat64(max);
                template = propDouble;
                break;

            case TypeID.DATE_AS_INT:
                try {
                    PropertyTemplateDateTime propDate = Factory.PropertyTemplateDateTime.createInstance(os);
                    propDate.set_PropertyDefaultDateTime(defaultValue != null ? new SimpleDateFormat(Constants.PATTERN_DATE_TIME).parse(defaultValue) : null);
                    template = propDate;
                } catch (ParseException e) {
                    throw new UnsupportedOperationException("Type " + type + " is not supported");
                }
                break;

            case TypeID.GUID_AS_INT:
                PropertyTemplateId propId = Factory.PropertyTemplateId.createInstance(os);
                propId.set_PropertyDefaultId(defaultValue != null ? new Id(defaultValue) : null);
                template = propId;
                break;

            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported");
        }
        template.set_IsHidden(hidden);
        template.set_IsValueRequired(valueRequired);
        template.set_Cardinality(cardinality);
        template.set_SymbolicName(name);

        setLocalizedNames(name, displayName, context, os, template);


        if (this.choiceListId != null) {

            ChoiceList choiceList = Factory.ChoiceList.fetchInstance(os, new Id(context.interpretId(choiceListId)), null);
            if (choiceList == null) {
                throw new IllegalStateException("The choiceList " + choiceListId + " must exist.");
            }
            template.set_ChoiceList(choiceList);
        } else if (this.choiceListName != null) {

            SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_CHOICE_LIST_NAME, choiceListName));
            SearchScope ss = new SearchScope(os);

            EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
            Iterator<?> it = ec.iterator();

            if (!it.hasNext()) {
                throw new IllegalStateException("The choiceList " + choiceListName + " must exist.");
            }
            ChoiceList choiceList =  (ChoiceList) it.next();

            template.set_ChoiceList(choiceList);
        }

        

        // Save new property template to the server
        template.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));

        return template.get_Id().toString();
    }

    @SuppressWarnings("unchecked")
    public static void setLocalizedNames(String name, String displayName, ExecutionContext context, ObjectStore os, PropertyTemplate template) {
        if (displayName == null) {
            displayName = name;
        }

        // Create LocalizedString collection
        template.set_DisplayNames(Factory.LocalizedString.createList());
        
        boolean hasObjectStoreLocale = false;
        Set<Entry<String, Properties>> entries = context.getResourceBundles().entrySet();
        for (Entry<String, Properties> entry : entries) {
            String value = entry.getValue().getProperty("property." + name, displayName);
            LocalizedString loc = Factory.LocalizedString.createInstance();
            loc.set_LocalizedText(value);
            loc.set_LocaleName(entry.getKey());
            template.get_DisplayNames().add(loc);
            
            hasObjectStoreLocale = hasObjectStoreLocale || os.get_LocaleName().equals(entry.getKey());
        }
        
        if (!hasObjectStoreLocale) {
            LocalizedString loc = Factory.LocalizedString.createInstance();
            loc.set_LocalizedText(displayName);
            loc.set_LocaleName(os.get_LocaleName());
            
            template.get_DisplayNames().add(loc);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create PropertyTemplate \"" + this.name + "\"";
    }
}
