/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionBinary;
import com.filenet.api.admin.PropertyDefinitionBoolean;
import com.filenet.api.admin.PropertyDefinitionDateTime;
import com.filenet.api.admin.PropertyDefinitionFloat64;
import com.filenet.api.admin.PropertyDefinitionId;
import com.filenet.api.admin.PropertyDefinitionInteger32;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.TypeID;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateClass;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateClass.PropDef;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.ce.XMLExportClass;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingIdRefPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdateClass extends AbstractUpdateOperation {

    private String name;

    private String displayName;

    private Boolean CBREnabled;
    
    private List<PropDef> properties;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateClass(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element root, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.properties = new ArrayList<PropDef>();

        name = root.getAttribute("name");

        if (root.hasAttribute("displayName")) {
            this.displayName = root.getAttribute("displayName");
        }
        CBREnabled = root.hasAttribute("cbrEnabled") ? Boolean.valueOf(root.getAttribute("cbrEnabled")) : null;

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, name), true, "The class " + name
                        + " must exist."));


        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3 && "property".equals(node.getNodeName())) {

                String propertyId = null, propertyName = null;
                if (node.getAttributes().getNamedItem("id") != null) {
                    propertyId = node.getAttributes().getNamedItem("id").getNodeValue();

                    if (ExecutionContext.isIdMustBeInterpreted(propertyId)) {
                        prerequisites.add(new ExistingIdRefPrerequisite(propertyId));
                    } else {
                        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_ID, propertyId), true, "The propertyTemplate " + propertyId
                                    + " must exist."));
                    }
                } else {
                    propertyName = node.getAttributes().getNamedItem("name").getNodeValue();

                    prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_NAME, propertyName), true, "The propertyTemplate " + propertyName
                                + " must exist."));
                }

                PropDef propDef = new PropDef(propertyId, propertyName);

                if (node.getAttributes().getNamedItem("hidden") != null) {
                    propDef.setHidden(Boolean.valueOf(node.getAttributes().getNamedItem("hidden").getNodeValue()));
                }
                if (node.getAttributes().getNamedItem("valueRequired") != null) {
                    propDef.setValueRequired(Boolean.valueOf(node.getAttributes().getNamedItem("valueRequired").getNodeValue()));
                }
                if (node.getAttributes().getNamedItem("defaultValue") != null) {
                    propDef.setDefaultValue(node.getAttributes().getNamedItem("defaultValue").getNodeValue());
                }
                if (node.getAttributes().getNamedItem("pattern") != null) {
                    propDef.setPattern(node.getAttributes().getNamedItem("pattern").getNodeValue());
                }
                if (node.getAttributes().getNamedItem("maxLength") != null) {
                    propDef.setMaxLength(Integer.valueOf(node.getAttributes().getNamedItem("maxLength").getNodeValue()));
                }
                if (node.getAttributes().getNamedItem("min") != null) {
                    propDef.setMin(Integer.valueOf(node.getAttributes().getNamedItem("min").getNodeValue()));
                }
                if (node.getAttributes().getNamedItem("max") != null) {
                    propDef.setMax(Integer.valueOf(node.getAttributes().getNamedItem("max").getNodeValue()));
                }
                if (node.getAttributes().getNamedItem("cbrEnabled") != null) {
                    propDef.setCBREnabled(Boolean.valueOf(node.getAttributes().getNamedItem("cbrEnabled").getNodeValue()));
                }
                if (node.getAttributes().getNamedItem("indexed") != null) {
                    propDef.setIndexed(Boolean.valueOf(node.getAttributes().getNamedItem("indexed").getNodeValue()));
                }

                this.properties.add(propDef);
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {

        ObjectStore os = context.getConnection().getObjectStore();

        ClassDefinition classDefinition = findClassDefinition(context);
        ClassDefinition parentClassDef = classDefinition.get_SuperclassDefinition();
        classDefinition.set_IsCBREnabled(CBREnabled);

        Set<String> parentProperties = XMLExportClass.extractInheritedProperties(parentClassDef);

        // Create LocalizedString collection
        CreateClass.setLocalizedNames(this.name, this.displayName, context, os, classDefinition);
        
        updateProperties(context, os, classDefinition, parentProperties);

        // Propagate the modifications in subclasses
        for (Iterator<ClassDefinition> iter = classDefinition.get_ImmediateSubclassDefinitions().iterator(); iter.hasNext(); ) {

            ClassDefinition classDef = iter.next();
            updateProperties(context, os, classDef, parentProperties);
        }

        return classDefinition.get_Id().toString();
    }

    @SuppressWarnings("unchecked")
    protected void updateProperties(ExecutionContext context, ObjectStore os, ClassDefinition classDefinition, Set<String> parentProperties) {
        PropertyDefinition[] list = (PropertyDefinition[]) classDefinition.get_PropertyDefinitions().toArray(new PropertyDefinition[0]);
        List<PropDef> props = new ArrayList<CreateClass.PropDef>(this.properties);
        Map<String, Boolean> indexedProperties = new HashMap<String, Boolean>();
        for (int i = 0; i < list.length; i++) {
            PropertyDefinition propertyDefinition = list[i];

            if (!propertyDefinition.get_IsSystemOwned() && !parentProperties.contains(propertyDefinition.get_SymbolicName())) {
                PropDef prop = null;
                for (PropDef def : props) {
                    if ((def.getId() != null && def.getId().equals(propertyDefinition.get_Id().toString()))
                           || def.getName() != null && def.getName().equals(propertyDefinition.get_Name())) {
                        prop = def;
                    }
                }

                if (prop != null) {
                    // Remove from the list. Only new properties will be presents at the end.
                    props.remove(prop);
                    updateProperties(propertyDefinition, prop);
                    
                    if (prop.getIndexed() != null) {
                        indexedProperties.put(prop.getName(), prop.getIndexed());
                    }
                } else {
                    // Property not referenced anymore.
                    classDefinition.get_PropertyDefinitions().remove(propertyDefinition);
                }
            }
        }

        // Process new properties
        SearchSQL sql;
        SearchScope ss;
        EngineCollection ec;
        for (PropDef def : props) {

            String query = def.getId() != null ? String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_ID, context.interpretId(def.getId())) : String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_NAME, def.getName());
            sql = new SearchSQL(query);
            ss = new SearchScope(os);

            ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
            Iterator<?> iter = ec.iterator();

            if (!iter.hasNext()) {
                throw new IllegalStateException("The propertyTemplate " + def.getId() + " must exist.");
            }

            PropertyTemplate template = (PropertyTemplate) iter.next();
            PropertyDefinition propertyDefinition = CreateClass.setProperties(def, template);
            classDefinition.get_PropertyDefinitions().add(propertyDefinition);
        }

        classDefinition.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID, PropertyNames.IMMEDIATE_SUBCLASS_DEFINITIONS, "PropertyDefinitions", "TableDefinition" }));
        
        if (!indexedProperties.isEmpty()) {
            CreateClass.setIndexedProperties(classDefinition, indexedProperties);
        }
    }

    protected ClassDefinition findClassDefinition(ExecutionContext context) {
        SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_CLASS_NAME, name));
        SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

//        PropertyFilter pf = FileNetCEApiUtil.createFilter(new String[] { "ImmediateSubclassDefinitions" });

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The class " + name + " must exist.");
        }

        ClassDefinition classDefinition = (ClassDefinition) it.next();
        return classDefinition;
    }


    protected void updateProperties(PropertyDefinition propertyDefinition, PropDef def) {

        if (def.getValueRequired() != null) {
            propertyDefinition.set_IsValueRequired(def.getValueRequired());
        }
        if (def.getHidden() != null) {
            propertyDefinition.set_IsHidden(def.getHidden());
        }

        switch (propertyDefinition.get_DataType().getValue()) {
            case TypeID.BINARY_AS_INT:
                PropertyDefinitionBinary propBin = (PropertyDefinitionBinary) propertyDefinition;
                if (def.getDefaultValue() != null) {
                    propBin.set_PropertyDefaultBinary(def.getDefaultValue().getBytes());
                }
                if (def.getMaxLength() != null) {
                    propBin.set_MaximumLengthBinary(def.getMaxLength());
                }
                break;
            case TypeID.BOOLEAN_AS_INT:
                PropertyDefinitionBoolean propBool = (PropertyDefinitionBoolean) propertyDefinition;
                if (def.getDefaultValue() != null) {
                    propBool.set_PropertyDefaultBoolean(Boolean.valueOf(def.getDefaultValue()));
                }
                break;
            case TypeID.DATE_AS_INT:
                if (def.getDefaultValue() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(def.getPattern() != null ? def.getPattern() : Constants.PATTERN_DATE_TIME);
                    try {
                        ((PropertyDefinitionDateTime) propertyDefinition).set_PropertyDefaultDateTime(sdf.parse(def.getDefaultValue()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TypeID.DOUBLE_AS_INT:
                PropertyDefinitionFloat64 propFloat64 = (PropertyDefinitionFloat64) propertyDefinition;
                if (def.getDefaultValue() != null) {
                    propFloat64.set_PropertyDefaultFloat64(Double.valueOf(def.getDefaultValue()));
                }
                if (def.getMin() != null) {
                    propFloat64.set_PropertyMinimumFloat64(Double.valueOf(def.getMin()));
                }
                if (def.getMax() != null) {
                    propFloat64.set_PropertyMaximumFloat64(Double.valueOf(def.getMax()));
                }
                break;
            case TypeID.GUID_AS_INT:
                if (def.getDefaultValue() != null) {
                    ((PropertyDefinitionId) propertyDefinition).set_PropertyDefaultId(new Id(def.getDefaultValue()));
                }
                break;
            case TypeID.LONG_AS_INT:
                PropertyDefinitionInteger32 propInt32 = (PropertyDefinitionInteger32) propertyDefinition;
                if (def.getDefaultValue() != null) {
                    propInt32.set_PropertyDefaultInteger32(Integer.valueOf(def.getDefaultValue()));
                }
                if (def.getMin() != null) {
                    propInt32.set_PropertyMinimumInteger32(Integer.valueOf(def.getMin()));
                }
                if (def.getMax() != null) {
                    propInt32.set_PropertyMaximumInteger32(Integer.valueOf(def.getMax()));
                }
                break;
            case TypeID.STRING_AS_INT:
                PropertyDefinitionString propStr = (PropertyDefinitionString) propertyDefinition;
                if (def.getDefaultValue() != null) {
                    propStr.set_PropertyDefaultString(def.getDefaultValue());
                }
                if (def.getMaxLength() != null) {
                    propStr.set_MaximumLengthString(def.getMaxLength());
                }
                if (def.isCBREnabled() != null) {
                    propStr.set_IsCBREnabled(def.isCBREnabled());
                }
                break;

            default:
                throw new UnsupportedOperationException("The type " + propertyDefinition.get_DataType().getValue() + " is not supported for default value.");
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Update the class " + name;
    }
}
