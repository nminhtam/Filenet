/**
 * 
 */
package lu.mtn.ibm.casemanager.client.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author MTN
 *
 */
public class CaseTask implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String id;
    
    private String name;
    
    private String launchMode;
    
    private int state;
    
    private Map<String, Object> properties;
    
    public CaseTask() {
        this.properties = new HashMap<String, Object>();
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
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the launchMode
     */
    public String getLaunchMode() {
        return this.launchMode;
    }

    /**
     * @param launchMode the launchMode to set
     */
    public void setLaunchMode(String launchMode) {
        this.launchMode = launchMode;
    }

    /**
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * @return the state
     */
    public int getState() {
        return this.state;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state) {
        this.state = state;
    }
}
