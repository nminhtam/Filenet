/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe;

import filenet.vw.api.VWLogDefinition;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class ExistingEvenLogPrerequisite implements Prerequisite {

    private String eventLogName;

    private boolean mustExist;

    /**
     * @param eventLogName
     */
    public ExistingEvenLogPrerequisite(String eventLogName, boolean mustExist) {
        super();
        this.eventLogName = eventLogName;
        this.mustExist = mustExist;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {

        VWLogDefinition eventLog = context.getConnection().getVWSession().fetchSystemConfiguration().getLogDefinition(eventLogName);
        return (eventLog != null) == mustExist;
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
        return "The EventLog " + this.eventLogName + " must exist.";
    }

}
