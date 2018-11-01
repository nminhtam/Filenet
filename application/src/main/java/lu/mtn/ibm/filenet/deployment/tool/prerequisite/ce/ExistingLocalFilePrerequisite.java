/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce;

import java.io.File;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;


/**
 * @author MTN
 *
 */
public class ExistingLocalFilePrerequisite implements Prerequisite {

    private String path;

    /**
     *
     */
    public ExistingLocalFilePrerequisite(String path) {
        this.path = path;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite#check(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public boolean check(ExecutionContext context) {
        return new File(path).exists();
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
        return "The local file " + path + " must exist";
    }


}
