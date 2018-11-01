/**
 * 
 */
package lu.mtn.ibm.casemanager.client.ri.util;

import com.filenet.api.constants.TypeID;
import com.filenet.api.util.Id;
import com.ibm.casemgmt.api.properties.CaseMgmtProperty;

/**
 * @author MTN
 *
 */
public class CaseManagerUtil {

    public static Object getPropertyValue(CaseMgmtProperty property) {

        switch (property.getPropertyType().getValue()) {
        
            case TypeID.GUID_AS_INT: 
                return ((Id) property.getValue()).toString();
            
            default:
                return property.getValue();
        }
    }
}
