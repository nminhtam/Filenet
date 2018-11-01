/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class ExistingIdRefPrerequisite implements Prerequisite {

    private String id;

    /**
     *
     */
    public ExistingIdRefPrerequisite(String id) {
        this.id = id;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {
        String tmp = context.interpretId(id);
        return tmp != null && !tmp.equals(id);
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
        return "The idRef " + this.id + " must exist.";
    }

}
