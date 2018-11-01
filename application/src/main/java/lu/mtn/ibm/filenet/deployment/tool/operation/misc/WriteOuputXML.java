/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.WriteException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.writer.Writer;

/**
 * @author NguyenT
 *
 */
public class WriteOuputXML extends Operation {

    private String innerXML;

    private String writerName;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public WriteOuputXML(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.writerName = rootElement.getAttribute("writer");
        if (rootElement.getTextContent() != null && !rootElement.getTextContent().trim().isEmpty()) {
            this.innerXML = rootElement.getTextContent();
        } else {
            try {
                this.innerXML = "";
                Node modifierNode = rootElement.getFirstChild();
                while (modifierNode != null && modifierNode.getNodeName() != null) {

                    this.innerXML += toXML(modifierNode);
                    modifierNode = rootElement.getNextSibling();
                }
            } catch (Exception e) {
                throw new OperationInitializationException(e);
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        Writer<String> writer = (Writer<String>) context.getWriter(writerName);
        try {

            if (innerXML.charAt(0) == '\n') {
                innerXML = innerXML.replaceFirst("\\n\\s*", "");
            }
            writer.write(innerXML);
        } catch (WriteException e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "WriteOutputXML " + this.innerXML;
    }

}
