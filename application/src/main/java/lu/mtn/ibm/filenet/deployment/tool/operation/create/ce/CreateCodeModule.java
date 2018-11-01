/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.admin.CodeModule;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.DynamicReferentialContainmentRelationship;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.ObjectStore;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public class CreateCodeModule extends AbstractCreateOperation {

    private String documentTitle;

    private ContentElementList contentElementList;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateCodeModule(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        documentTitle = rootElement.getAttribute("documentTitle");

        contentElementList = Factory.ContentElement.createList();

        NodeList children = rootElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3) {

                if ("content".equals(node.getNodeName())) {

                    String name = node.getAttributes().getNamedItem("name").getNodeValue();
                    String mime = node.getAttributes().getNamedItem("mime").getNodeValue();
                    boolean zipContent = node.getAttributes().getNamedItem("zip") != null ? Boolean.valueOf(node.getAttributes().getNamedItem("zip").getNodeValue())
                                    : Constants.DEFAULT_ZIP_CONTENT;

                    byte[] content = Base64.decodeBase64(node.getTextContent());

                    ByteArrayInputStream bs = new ByteArrayInputStream(content);
                    try {
                        InputStream is = zipContent ? new GZIPInputStream(bs) : bs;

                        ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
                        ctNew.setCaptureSource(is);
                        ctNew.set_RetrievalName(name);
                        ctNew.set_ContentType(mime);

                        contentElementList.add(ctNew);
                    } catch (IOException e) {
                        throw new OperationInitializationException(e);
                    }
                }
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {

        ObjectStore os = context.getConnection().getObjectStore();

        Folder folder = Factory.Folder.fetchInstance(os, "/CodeModules", null);

        // Create CodeModule object.
        CodeModule newCM = Factory.CodeModule.createInstance(os, "CodeModule");
        newCM.getProperties().putValue("DocumentTitle", documentTitle);
        newCM.set_ContentElements(contentElementList);

        // Check in CodeModule object.
        newCM.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
        newCM.save(RefreshMode.REFRESH);

        // File CodeModule object and save.
        DynamicReferentialContainmentRelationship drcr = (DynamicReferentialContainmentRelationship) folder.file((IndependentlyPersistableObject) newCM,
                        AutoUniqueName.AUTO_UNIQUE, documentTitle, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);

        drcr.save(RefreshMode.NO_REFRESH);

        return newCM.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create CodeModule \"" + this.documentTitle + "\"";
    }

}
