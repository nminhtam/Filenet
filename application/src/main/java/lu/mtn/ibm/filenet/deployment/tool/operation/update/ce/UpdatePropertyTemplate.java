/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import com.filenet.api.admin.ChoiceList;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.admin.PropertyTemplateBoolean;
import com.filenet.api.admin.PropertyTemplateDateTime;
import com.filenet.api.admin.PropertyTemplateFloat64;
import com.filenet.api.admin.PropertyTemplateId;
import com.filenet.api.admin.PropertyTemplateInteger32;
import com.filenet.api.admin.PropertyTemplateString;
import com.filenet.api.collection.EngineCollection;
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
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreatePropertyTemplate;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdatePropertyTemplate extends AbstractUpdateOperation {

//    private Cardinality cardinality;

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

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdatePropertyTemplate(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element root, List<Prerequisite> prerequisites) throws OperationInitializationException {

        name = root.getAttribute("name");

        type = Integer.valueOf(root.getAttribute("type"));
//        cardinality = Cardinality.getInstanceFromInt(Integer.valueOf(root.getAttribute("cardinality")));

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

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_NAME, name), true, "The propertyTemplate " + name
                        + " must exist."));
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
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {

        ObjectStore os = context.getConnection().getObjectStore();

        SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_PROPERTY_TEMPLATE_NAME, name));
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<PropertyTemplate> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The propertyTemplate " + name + " must exist.");
        }

        PropertyTemplate template = it.next();
        switch (template.get_DataType().getValue()) {

            case TypeID.STRING_AS_INT:
                PropertyTemplateString propStr = (PropertyTemplateString) template;
                propStr.set_MaximumLengthString(maxLength);
                propStr.set_PropertyDefaultString(defaultValue);
                template = propStr;
                break;

            case TypeID.LONG_AS_INT:
                PropertyTemplateInteger32 propInt = (PropertyTemplateInteger32) template;
                propInt.set_PropertyMinimumInteger32(min != null ? min.intValue() : null);
                propInt.set_PropertyMaximumInteger32(max != null ? max.intValue() : null);
                propInt.set_PropertyDefaultInteger32(defaultValue != null ? Integer.valueOf(defaultValue) : null);
                template = propInt;
                break;

            case TypeID.BOOLEAN_AS_INT:
                PropertyTemplateBoolean propBool = (PropertyTemplateBoolean) template;
                propBool.set_PropertyDefaultBoolean(defaultValue != null ? Boolean.valueOf(defaultValue) : null);
                template = propBool;
                break;

            case TypeID.DOUBLE_AS_INT:
                PropertyTemplateFloat64 propDouble = (PropertyTemplateFloat64) template;
                propDouble.set_PropertyDefaultFloat64(defaultValue != null ? Double.valueOf(defaultValue) : null);
                propDouble.set_PropertyMinimumFloat64(min);
                propDouble.set_PropertyMaximumFloat64(max);
                template = propDouble;
                break;

            case TypeID.DATE_AS_INT:
                try {
                    PropertyTemplateDateTime propDate = (PropertyTemplateDateTime) template;
                    propDate.set_PropertyDefaultDateTime(defaultValue != null ? new SimpleDateFormat(Constants.PATTERN_DATE_TIME).parse(defaultValue) : null);
                    template = propDate;
                } catch (ParseException e) {
                    throw new UnsupportedOperationException("Type " + type + " is not supported");
                }
                break;

            case TypeID.GUID_AS_INT:
                PropertyTemplateId propId = (PropertyTemplateId) template;
                propId.set_PropertyDefaultId(defaultValue != null ? new Id(defaultValue) : null);
                template = propId;
                break;

            default:
                throw new UnsupportedOperationException("Type " + type + " is not supported");
        }
        template.set_IsHidden(hidden);
        template.set_IsValueRequired(valueRequired);

        if (this.choiceListId != null) {

            ChoiceList choiceList = Factory.ChoiceList.fetchInstance(os, new Id(context.interpretId(choiceListId)), null);
            if (choiceList == null) {
                throw new IllegalStateException("The choiceList " + choiceListId + " must exist.");
            }
            template.set_ChoiceList(choiceList);
        } else if (this.choiceListName != null) {

            sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_CHOICE_LIST_NAME, choiceListName));
            ss = new SearchScope(os);

            ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
            Iterator<?> iter = ec.iterator();

            if (!iter.hasNext()) {
                throw new IllegalStateException("The choiceList " + choiceListName + " must exist.");
            }
            ChoiceList choiceList =  (ChoiceList) it.next();

            template.set_ChoiceList(choiceList);
        }

        CreatePropertyTemplate.setLocalizedNames(name, displayName, context, os, template);

        // Save new property template to the server
        template.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));

        return template.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Update the propertyTemplate " + name;
    }

}
