/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.dto;

import java.io.Serializable;

/**
 * @author NguyenT
 *
 */
public class Permission implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String user;

    private String type;

    private String accessType;

    private String accessMask;

    private String depth;

    /**
     * @param user
     * @param type
     * @param accessType
     * @param accessMask
     */
    public Permission(String user, String type, String accessType, String accessMask, String depth) {
        super();
        this.user = user;
        this.type = type;
        this.accessType = accessType;
        this.accessMask = accessMask;
        this.depth = depth;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }


    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return the accessType
     */
    public String getAccessType() {
        return this.accessType;
    }

    /**
     * @return the accessMask
     */
    public String getAccessMask() {
        return this.accessMask;
    }

    /**
     * @return the depth
     */
    public String getDepth() {
        return this.depth;
    }

    /**
     * @param depth the depth to set
     */
    public void setDepth(String depth) {
        this.depth = depth;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.user == null) ? 0 : this.user.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Permission other = (Permission) obj;
        if (this.user == null) {
            if (other.user != null)
                return false;
        } else if (!this.user.equals(other.user))
            return false;
        return true;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Permission [user=");
        builder.append(this.user);
        builder.append(", type=");
        builder.append(this.type);
        builder.append(", accessType=");
        builder.append(this.accessType);
        builder.append(", accessMask=");
        builder.append(this.accessMask);
        builder.append(", depth=");
        builder.append(this.depth);
        builder.append("]");
        return builder.toString();
    }
}
