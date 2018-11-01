/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.ce;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lu.mtn.ibm.filenet.client.dto.CEDocumentContent;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateDocument;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.ce.UpdateDocument;

/**
 * @author NguyenT
 *
 */
public class WorkflowDefinitionSecurityMapper extends AbstractSecurityMapper {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public WorkflowDefinitionSecurityMapper(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper#isApplicable(lu.mtn.ibm.filenet.deployment.tool.operation.Operation)
     */
    @Override
    public boolean isApplicable(Operation operation) {

        if (operation instanceof CreateDocument) {
            return "WorkflowDefinition".equalsIgnoreCase(((CreateDocument) operation).getRequest().getDocumentClassName());
        }
        if (operation instanceof UpdateDocument) {
            return "WorkflowDefinition".equalsIgnoreCase(((UpdateDocument) operation).getClassName());
        }
        return false;
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.security.AbstractSecurityMapper#modify(lu.mtn.ibm.filenet.deployment.tool.operation.Operation)
     */
    @Override
    public void modify(Operation operation) throws OperationExecutionException {

        if (operation instanceof CreateDocument) {
            CEDocumentContent content = ((CreateDocument) operation).getRequest().getContents().get(0);
            content.setContent(updateContent(content.getContent()));

        } else if (operation instanceof UpdateDocument) {
            CEDocumentContent content = ((UpdateDocument) operation).getRequest().getContents().get(0);
            content.setContent(updateContent(content.getContent()));
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Register a workflowDefinition security mapper.";
    }

    protected byte[] updateContent(byte[] content) throws OperationExecutionException {

        String def = new String(content);

        try {
            Document doc = toDocument(def);

            processTrackers(doc);
            processParticipants(doc);

            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();

            DocumentType docType = doc.getDoctype();
            if(docType != null) {
                if (docType.getPublicId() != null) {
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, docType.getPublicId());
                }
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docType.getSystemId());
            }

            DOMSource source = new DOMSource(doc);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            sw.close();
            return sw.toString().getBytes();

        } catch (ParserConfigurationException e) {
            throw new OperationExecutionException(e);
        } catch (SAXException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        } catch (XPathExpressionException e) {
            throw new OperationExecutionException(e);
        } catch (TransformerException e) {
            throw new OperationExecutionException(e);
        }
    }

    protected void processParticipants(Document doc) throws XPathExpressionException {
        NodeList users;
        users = (NodeList) xPath.evaluate("//Step/Participant", doc, XPathConstants.NODESET);
        for (int i = 0; i < users.getLength(); i++) {
            Node field = users.item(i);
            String valueExpr = field.getAttributes().getNamedItem("Val").getTextContent();
            String participant = valueExpr.replaceAll("\\\"", "");

            String substitute = mappings.get("user:" + participant);
            if (substitute == null) {
                field.getAttributes().getNamedItem("Val").setTextContent("\"" + participant + "\"");
            } else if (!substitute.trim().isEmpty()) {
                field.getAttributes().getNamedItem("Val").setTextContent("\"" + substitute + "\"");
            } else {
                field.getParentNode().removeChild(field);
            }
        }
    }

    protected void processTrackers(Document doc) throws XPathExpressionException {
        NodeList users = (NodeList) xPath.evaluate("//Field[@Name='F_Trackers']", doc, XPathConstants.NODESET);
        for (int i = 0; i < users.getLength(); i++) {
            Node field = users.item(i);
            String valueExpr = field.getAttributes().getNamedItem("ValueExpr").getTextContent();
            valueExpr = valueExpr.replaceAll("\\{|\\}|\\\"", "");
            String trackers[] = valueExpr.split(",");

            StringBuffer sb = new StringBuffer("{");
            boolean hasOneUser = false;
            for (int j = 0; j < trackers.length; j++) {
                String substitute = mappings.get("tracker:" + trackers[j]);
                if (substitute == null) {
                    sb.append("\"").append(trackers[j]).append("\"");
                    hasOneUser = true;
                } else if (!substitute.trim().isEmpty()) { // If the substitute is empty remove the user from the list.
                    sb.append("\"").append(substitute).append("\"");
                    hasOneUser = true;
                }
            }
            if (!hasOneUser) { // If there is no user then add the default quote.
                sb.append("\"\"");
            }
            field.getAttributes().getNamedItem("ValueExpr").setTextContent(sb.toString() + "}");
        }
    }
}
