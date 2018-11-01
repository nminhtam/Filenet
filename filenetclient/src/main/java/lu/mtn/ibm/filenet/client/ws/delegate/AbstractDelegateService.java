/**
 *
 */
package lu.mtn.ibm.filenet.client.ws.delegate;

import java.util.Date;

import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FNCEWS40PortType;
import com.filenet.ns.fnce._2006._11.ws.schema.FilterElementType;
import com.filenet.ns.fnce._2006._11.ws.schema.Localization;
import com.filenet.ns.fnce._2006._11.ws.schema.ModifiablePropertyType;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectEntryType;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectFactory;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectReference;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectSpecification;
import com.filenet.ns.fnce._2006._11.ws.schema.PropertyType;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonBoolean;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonDateTime;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonFloat64;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonId;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonInteger32;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonObject;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonString;

/**
 * @author nguyent
 *
 */
public abstract class AbstractDelegateService {

    protected FNCEWS40PortType port;

    protected Localization defaultLocale;

    protected ObjectFactory factory;

    /**
     * @param port
     * @param defaultLocale
     * @param factory
     */
    public AbstractDelegateService(FNCEWS40PortType port, Localization defaultLocale, ObjectFactory factory) {
        super();
        this.port = port;
        this.defaultLocale = defaultLocale;
        this.factory = factory;
    }

    protected Object[] convertFromWebServiceProperty(PropertyType prop) {
        if (prop instanceof SingletonString) {
            SingletonString p = (SingletonString) prop;
            return new Object[] { p.getPropertyId(), p.getValue() };
        } else if (prop instanceof SingletonInteger32) {
            SingletonInteger32 p = (SingletonInteger32) prop;
            return new Object[] { p.getPropertyId(), p.getValue() };
        } else if (prop instanceof SingletonDateTime) {
            SingletonDateTime p = (SingletonDateTime) prop;
            return new Object[] { p.getPropertyId(), p.getValue() };
        } else if (prop instanceof SingletonBoolean) {
            SingletonBoolean p = (SingletonBoolean) prop;
            return new Object[] { p.getPropertyId(), p.isValue() };
        } else if (prop instanceof SingletonId) {
            SingletonId p = (SingletonId) prop;
            return new Object[] { p.getPropertyId(), p.getValue() };
        }

        return new Object[] { prop.getPropertyId(), prop };
    }

    protected ModifiablePropertyType convertToWebServiceProperty(String name, Object value) {
        if (value != null)
        {
            if (value instanceof ModifiablePropertyType)
            {
                return (ModifiablePropertyType)value;
            }
            else if (value instanceof String || value instanceof Character)
            {
                return createSingletonString(name, value);
            }
            else if (value instanceof Integer)
            {
                return createSingletonInteger(name, value);
            }
            else if (value instanceof Double)
            {
                return createSingletonFloat(name, value);
            }
            else if (value instanceof Date)
            {
                return createSingletonDate(name, value);
            }
            else if (value instanceof Boolean)
            {
                return createSingletonBoolean(name, value);
            }
            else
            {
                ModifiablePropertyType prop = (ModifiablePropertyType) value;
                prop.setPropertyId(name);
                return prop;
            }
        }
        return null;
    }

    protected SingletonBoolean createSingletonBoolean(String name, Object value) {
        SingletonBoolean prop = this.factory.createSingletonBoolean();
        prop.setPropertyId(name);
        prop.setValue((Boolean)value);
        return prop;
    }

    protected SingletonDateTime createSingletonDate(String name, Object value) {
        SingletonDateTime prop = this.factory.createSingletonDateTime();
        prop.setPropertyId(name);
        prop.setValue((Date) value);
        return prop;
    }

    protected SingletonFloat64 createSingletonFloat(String name, Object value) {
        SingletonFloat64 prop = this.factory.createSingletonFloat64();
        prop.setPropertyId(name);
        prop.setValue((Double) value);
        return prop;
    }

    protected SingletonInteger32 createSingletonInteger(String name, Object value) {
        SingletonInteger32 prop = this.factory.createSingletonInteger32();
        prop.setPropertyId(name);
        prop.setValue((Integer) value);
        return prop;
    }

    protected SingletonString createSingletonString(String name, Object value) {
        SingletonString prop = this.factory.createSingletonString();
        prop.setPropertyId(name);
        prop.setValue(value.toString());
        return prop;
    }

    protected SingletonObject createSingletonObject(String name, ObjectEntryType value) {
        SingletonObject prop = this.factory.createSingletonObject();
        prop.setPropertyId(name);
        prop.setValue(value);
        return prop;
    }

    protected ObjectSpecification createObjectSpecification(String classId, String objectId, String objectStore) {
        ObjectSpecification objectSpecification = this.factory.createObjectSpecification();
        objectSpecification.setClassId(classId);
        objectSpecification.setObjectId(objectId);
        objectSpecification.setObjectStore(objectStore);
        return objectSpecification;
    }

    protected ObjectReference createObjectReference(String classId, String objectId, String objectStore) {
        ObjectReference ojbRef = this.factory.createObjectReference();
        ojbRef.setClassId(classId);
        ojbRef.setObjectId(objectId);
        ojbRef.setObjectStore(objectStore);
        return ojbRef;
    }

    protected FilterElementType createFilterElement(String value) {
        FilterElementType f = this.factory.createFilterElementType();
        f.setValue(value);
        return f;
    }
}
