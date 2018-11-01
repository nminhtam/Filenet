/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.execution;

import java.util.List;

import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;


/**
 * @author NguyenT
 *
 */
public class TaskRunner extends Thread {

    private boolean stop;

    private List<Operation> tasks;

    private ExecutionContext context;

    public TaskRunner(List<Operation> tasks, ExecutionContext context) {
        super();
        this.tasks = tasks;
        this.stop = false;
        this.context = context;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        while (!stop) {

            if (!tasks.isEmpty()) {
                Operation task = tasks.remove(0);

                if (task.checkPrerequisites(context)) {
                    try {
                        task.execute(context);
                    } catch (OperationExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
