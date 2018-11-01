/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.util.List;

import org.w3c.dom.Element;

import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;

import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class CreateFolder extends AbstractCreateOperation {

    private String path;


    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateFolder(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        path = rootElement.getAttribute("path");

        prerequisites.add(new ExistingObjectPrerequisite(false, String.format(Constants.QUERY_EXIST_FOLDER_NAME, path), true, "The folder " + path + " must not exist."));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext, java.util.List)
     */
    @Override
    public String executeInternal(ExecutionContext context) throws OperationExecutionException {

        try {
            Folder folder = this.findOrCreateFolders(context.getConnection().getObjectStore(), path);

            return folder.get_Id().toString();

        } catch (Exception e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @param os
     * @param folderPath
     * @return
     */
    private Folder findOrCreateFolders(ObjectStore os, String folderPath) {

        Folder folder = null;

        try {
            folder = Factory.Folder.fetchInstance(os, folderPath, null);
        } catch (EngineRuntimeException e) {
            if (!ExceptionCode.E_OBJECT_NOT_FOUND.equals(e.getExceptionCode())) {
                throw e;
            }
        }

        if (folder == null) {

            if (folderPath.endsWith("/")) {
                folderPath = folderPath.substring(0, folderPath.length() - 1);
            }
            int index = folderPath.lastIndexOf("/");
            String parentPath = folderPath.substring(0, index + 1);
            String folderName = folderPath.substring(index + 1);

            Folder parentFolder;
            if ("/".equals(parentPath) || parentPath.isEmpty()) {
                parentFolder = os.get_RootFolder();
            } else {
                parentFolder = findOrCreateFolders(os, parentPath);
            }

            folder = Factory.Folder.createInstance(os, null);
            folder.set_Parent(parentFolder);
            folder.set_FolderName(folderName);
            folder.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));
        }
        return folder;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create Folder \"" + path + "\"";
    }
}
