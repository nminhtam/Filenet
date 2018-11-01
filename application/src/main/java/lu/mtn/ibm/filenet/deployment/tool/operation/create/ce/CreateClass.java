/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.ColumnDefinition;
import com.filenet.api.admin.LocalizedString;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.admin.PropertyDefinitionBinary;
import com.filenet.api.admin.PropertyDefinitionBoolean;
import com.filenet.api.admin.PropertyDefinitionDateTime;
import com.filenet.api.admin.PropertyDefinitionFloat64;
import com.filenet.api.admin.PropertyDefinitionId;
import com.filenet.api.admin.PropertyDefinitionInteger32;
import com.filenet.api.admin.PropertyDefinitionString;
import com.filenet.api.admin.PropertyTemplate;
import com.filenet.api.admin.TableDefinition;
import com.filenet.api.collection.ColumnDefinitionList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.TypeID;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.WorkflowDefinition;
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
public class CreateClass extends AbstractCreateOperation {

    private String name;

    private String displayName;

    private String parentClass;
    
    private Boolean CBREnabled;

    private List<PropDef> properties;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateClass(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element root, List<Prerequisite> prerequisites) {

        this.properties = new ArrayList<PropDef>();

        name = root.getAttribute("name");
        parentClass = root.getAttribute("parent");
        CBREnabled = root.hasAttribute("cbrEnabled") ? Boolean.valueOf(root.getAttribute("cbrEnabled")) : null;
        

        if (root.hasAttribute("displayName")) {
            this.displayName = root.getAttribute("displayName");
        }

        prerequisites.add(new ExistingObjectPrerequisite(false, String.format(Constants.QUERY_EXIST_CLASS_NAME, name), true, "The class " + name
                        + " must not exist."));
        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CLASS_NAME, parentClass), true, "The parent class " + parentClass
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
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String executeInternal(ExecutionContext context) {

        ObjectStore os = context.getConnection().getObjectStore();

        ClassDefinition parentClassDefinition = Factory.ClassDefinition.fetchInstance(os, parentClass, null);
        ClassDefinition newClassDefinition = parentClassDefinition.createSubclass();
        newClassDefinition.set_SymbolicName(name);
        newClassDefinition.set_IsCBREnabled(CBREnabled);

        // Create LocalizedString collection
        setLocalizedNames(this.name, this.displayName, context, os, newClassDefinition);
        
        Map<String, Boolean> indexedProperties = new HashMap<String, Boolean>();
        for (PropDef def : this.properties) {

            String query = def.getId() != null ? String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_ID, context.interpretId(def.getId())) : String.format(Constants.QUERY_EXIST_PROPERTY_TEMPLATE_NAME, def.getName());
            SearchSQL sql = new SearchSQL(query);
            SearchScope ss = new SearchScope(os);

            EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
            Iterator<?> it = ec.iterator();

            if (!it.hasNext()) {
                throw new IllegalStateException("The propertyTemplate " + def.getId() + " must exist.");
            }

            PropertyTemplate template = (PropertyTemplate) it.next();
            PropertyDefinition propertyDefinition = setProperties(def, template);
            newClassDefinition.get_PropertyDefinitions().add(propertyDefinition);
            
            if (def.getIndexed() != null) {
                indexedProperties.put(def.getName(), def.getIndexed());
            }
        }
        
        newClassDefinition.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID, "PropertyDefinitions", "TableDefinition" }));
        
        if (!indexedProperties.isEmpty()) {
            setIndexedProperties(newClassDefinition, indexedProperties);
        }
        
        return newClassDefinition.get_Id().toString();
    }

    @SuppressWarnings("unchecked")
    public static void setIndexedProperties(ClassDefinition newClassDefinition, Map<String, Boolean> indexedProperties) {
         
        Map<Id, String> indexes = new HashMap<Id, String>();
        for (Iterator<PropertyDefinition> pit = newClassDefinition.get_PropertyDefinitions().iterator(); pit.hasNext(); ) {
            PropertyDefinition def = pit.next();
            if (indexedProperties.containsKey(def.get_SymbolicName())) {
                indexes.put(def.get_ColumnId(), def.get_SymbolicName());
            }
        }
        
        TableDefinition td = newClassDefinition.get_TableDefinition();
        ColumnDefinitionList columns = td.get_ColumnDefinitions();
        for (Iterator<ColumnDefinition> it = columns.iterator(); it.hasNext(); ) {
            ColumnDefinition c = it.next();
            if (indexes.containsKey(c.get_ColumnId())) {
                String name = indexes.get(c.get_ColumnId());
                c.set_IsSingleIndexed(indexedProperties.get(name));
            }
        }
        td.save(RefreshMode.NO_REFRESH);
    }

    @SuppressWarnings("unchecked")
    public static void setLocalizedNames(String name, String displayName, ExecutionContext context, ObjectStore os, ClassDefinition newClassDefinition) {
        
        newClassDefinition.set_DisplayNames(Factory.LocalizedString.createList());
        
        if (displayName == null) {
            displayName = name;
        }
        
        boolean hasObjectStoreLocale = false;
        Set<Entry<String, Properties>> entries = context.getResourceBundles().entrySet();
        for (Entry<String, Properties> entry : entries) {
            String value = entry.getValue().getProperty("class." + name, displayName);
            LocalizedString loc = Factory.LocalizedString.createInstance();
            loc.set_LocalizedText(value);
            loc.set_LocaleName(entry.getKey());
            newClassDefinition.get_DisplayNames().add(loc);
            
            hasObjectStoreLocale = hasObjectStoreLocale || os.get_LocaleName().equals(entry.getKey());
        }
        
        if (!hasObjectStoreLocale) {
            LocalizedString loc = Factory.LocalizedString.createInstance();
            loc.set_LocalizedText(displayName);
            loc.set_LocaleName(os.get_LocaleName());
            
            newClassDefinition.get_DisplayNames().add(loc);
        }
    }

    public static PropertyDefinition setProperties(PropDef def, PropertyTemplate template) {
        PropertyDefinition propertyDefinition = template.createClassProperty();

        if (def.getValueRequired() != null) {
            propertyDefinition.set_IsValueRequired(def.getValueRequired());
        }
        if (def.getHidden() != null) {
            propertyDefinition.set_IsHidden(def.getHidden());
        }

        switch (template.get_DataType().getValue()) {
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
                    SimpleDateFormat sdf = new SimpleDateFormat(def.getPattern() != null ? def.pattern : Constants.PATTERN_DATE_TIME);
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
                throw new UnsupportedOperationException("The type " + template.get_DataType().getValue() + " is not supported for default value.");
        }

        return propertyDefinition;
    }



    public WorkflowDefinition getWorkFlowDefinitionDocument(ObjectStore os, String conn_point, String ceUri, String workflowDefinitionId) throws Exception {
//        // Get the Workflow Definition document object.
//        WorkflowDefinition wfdDocument = Factory.WorkflowDefinition.fetchInstance(os, new Id(workflowDefinitionId), null);
//
//        // Get the current VW version number to test its validity.
//        String vwCurrentVersion = wfdDocument.get_VWVersion();
//        // Get the VWSession object for testing the VW version number.
//        VWSession vwSession = getVWSession(conn_point, ceUri);
//        if (vwCurrentVersion == null || !vwSession.checkWorkflowIdentifier(vwCurrentVersion)) {
//            String vwNewVersion;
//            System.out.println("Workflow definition does not have a VW version number, or it is not up to date");
//            vwNewVersion = transferWorkflowDefinition(os, vwSession, wfdDocument);
//            wfdDocument.set_VWVersion(vwNewVersion);
//            wfdDocument.save(RefreshMode.REFRESH);
//        }
//        return wfdDocument;
        return null;
    }

    public static class PropDef {

        private String id;

        private String name;

        private Boolean hidden;

        private Boolean valueRequired;

        private String defaultValue;

        private String pattern;

        private Integer maxLength;

        private Integer min;

        private Integer max;
        
        private Boolean CBREnabled;
        
        private Boolean indexed;

        public PropDef(String id, String name) {
            super();
            this.id = id;
            this.name = name;
        }

        /**
         * @return the hidden
         */
        public Boolean getHidden() {
            return this.hidden;
        }

        /**
         * @param hidden the hidden to set
         */
        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }

        /**
         * @return the valueRequired
         */
        public Boolean getValueRequired() {
            return this.valueRequired;
        }

        /**
         * @param valueRequired the valueRequired to set
         */
        public void setValueRequired(Boolean valueRequired) {
            this.valueRequired = valueRequired;
        }

        /**
         * @return the defaultValue
         */
        public String getDefaultValue() {
            return this.defaultValue;
        }

        /**
         * @param defaultValue the defaultValue to set
         */
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * @return the pattern
         */
        public String getPattern() {
            return this.pattern;
        }

        /**
         * @param pattern the pattern to set
         */
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        /**
         * @return the maxLength
         */
        public Integer getMaxLength() {
            return this.maxLength;
        }

        /**
         * @param maxLength the maxLength to set
         */
        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }

        /**
         * @return the min
         */
        public Integer getMin() {
            return this.min;
        }

        /**
         * @param min the min to set
         */
        public void setMin(Integer min) {
            this.min = min;
        }

        /**
         * @return the max
         */
        public Integer getMax() {
            return this.max;
        }

        /**
         * @param max the max to set
         */
        public void setMax(Integer max) {
            this.max = max;
        }

        /**
         * @return the id
         */
        public String getId() {
            return this.id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return the cBREnabled
         */
        public Boolean isCBREnabled() {
            return this.CBREnabled;
        }

        /**
         * @param CBREnabled the CBREnabled to set
         */
        public void setCBREnabled(Boolean CBREnabled) {
            this.CBREnabled = CBREnabled;
        }

        /**
         * @return the indexed
         */
        public Boolean getIndexed() {
            return this.indexed;
        }

        /**
         * @param indexed the indexed to set
         */
        public void setIndexed(Boolean indexed) {
            this.indexed = indexed;
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create Class " + this.name;
    }
}
