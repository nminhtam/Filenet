/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.pe;

import filenet.vw.api.VWRosterDefinition;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class ExistingRosterPrerequisite implements Prerequisite {

    private String rosterName;

    private boolean mustExist;

    /**
     * @param rosterName
     */
    public ExistingRosterPrerequisite(String rosterName, boolean mustExist) {
        super();
        this.rosterName = rosterName;
        this.mustExist = mustExist;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {

        VWRosterDefinition roster = context.getConnection().getVWSession().fetchSystemConfiguration().getRosterDefinition(rosterName);
        return (roster != null) == mustExist;
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
        return "The Roster " + this.rosterName + " must exist.";
    }

}
