/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce;

import java.util.Iterator;


import com.filenet.api.admin.ClassDefinition;
import com.filenet.api.admin.PropertyDefinition;
import com.filenet.api.collection.PropertyDefinitionList;
import com.filenet.api.constants.Cardinality;
import com.filenet.api.constants.TypeID;
import com.filenet.api.core.Factory;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class ExistingPropertyDefinitionPrerequisite implements Prerequisite {

    private String className;

    private String propertyName;

    private int propertyType;


    /**
     * @param className
     * @param propertyName
     * @param propertyType
     */
    public ExistingPropertyDefinitionPrerequisite(String className, String propertyName, int propertyType) {
        super();
        this.className = className;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {

        ClassDefinition classDef = Factory.ClassDefinition.fetchInstance(context.getConnection().getObjectStore(), className, null);

        if (classDef != null) {

            PropertyDefinition prop = getPropertyDef(classDef.get_PropertyDefinitions(), propertyName);

            if (prop != null) {

                switch (propertyType) {
                    case Constants.PROPERTY_TYPE_STRING:
                        return prop.get_DataType() == TypeID.STRING;

                    case Constants.PROPERTY_TYPE_STRING_ARRAY:
                        return prop.get_DataType() == TypeID.STRING && Cardinality.LIST.equals(prop.get_Cardinality());

                    case Constants.PROPERTY_TYPE_INT:
                        return prop.get_DataType() == TypeID.LONG;

                    case Constants.PROPERTY_TYPE_INT_ARRAY:
                        return prop.get_DataType() == TypeID.LONG && Cardinality.LIST.equals(prop.get_Cardinality());

                    case Constants.PROPERTY_TYPE_FLOAT:
                        return prop.get_DataType() == TypeID.DOUBLE;

                    case Constants.PROPERTY_TYPE_FLOAT_ARRAY:
                        return prop.get_DataType() == TypeID.DOUBLE && Cardinality.LIST.equals(prop.get_Cardinality());

                    case Constants.PROPERTY_TYPE_BINARY:
                        return prop.get_DataType() == TypeID.BINARY;

                    case Constants.PROPERTY_TYPE_BINARY_ARRAY:
                        return prop.get_DataType() == TypeID.BINARY && Cardinality.LIST.equals(prop.get_Cardinality());

                    case Constants.PROPERTY_TYPE_BOOLEAN:
                        return prop.get_DataType() == TypeID.BOOLEAN;

                    case Constants.PROPERTY_TYPE_BOOLEAN_ARRAY:
                        return prop.get_DataType() == TypeID.BOOLEAN && Cardinality.LIST.equals(prop.get_Cardinality());

                    case Constants.PROPERTY_TYPE_DATE_TIME:
                        return prop.get_DataType() == TypeID.DATE;

                    case Constants.PROPERTY_TYPE_DATE_TIME_ARRAY:
                        return prop.get_DataType() == TypeID.DATE && Cardinality.LIST.equals(prop.get_Cardinality());

                    case Constants.PROPERTY_TYPE_ID:
                        return prop.get_DataType() == TypeID.GUID;

                    case Constants.PROPERTY_TYPE_ID_ARRAY:
                        return prop.get_DataType() == TypeID.GUID && Cardinality.LIST.equals(prop.get_Cardinality());

                    default:
                        throw new IllegalStateException("The type " + propertyType + " is not supported.");
                }
            }
        }

        return false;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#isBlocking()
     */
    @Override
    public boolean isBlocking() {
        return true;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#getDescription()
     */
    @Override
    public String getDescription() {
        return "The property " + propertyName + " must exist in class " + className + " and must be of type " + propertyType + ".";
    }


    @SuppressWarnings("unchecked")
    protected PropertyDefinition getPropertyDef(PropertyDefinitionList props, String name) {
        for (Iterator<PropertyDefinition> it = props.iterator(); it.hasNext(); ) {
            PropertyDefinition prop = it.next();
            if (name.equals(prop.get_SymbolicName())) {
                return prop;
            }
        }
        return null;
    }
}
