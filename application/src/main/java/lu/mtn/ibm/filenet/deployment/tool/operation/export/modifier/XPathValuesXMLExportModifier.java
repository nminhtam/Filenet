/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.modifier;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;

/**
 * @author NguyenT
 *
 */
public class XPathValuesXMLExportModifier extends AbstractXMLExportModifier {

    private String xPathNode;

    private String value;

    private String nodeToAdd;

    private boolean remove;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XPathValuesXMLExportModifier(Element rootElement) throws OperationInitializationException {
        super(rootElement);

        this.xPathNode = rootElement.getAttribute("xPath");
        if (rootElement.hasAttribute("value")) {
            this.value = rootElement.getAttribute("value");
        }
        if (rootElement.hasAttribute("nodeToAdd")) {
            this.nodeToAdd = rootElement.getAttribute("nodeToAdd");
        }
        this.remove = rootElement.hasAttribute("value") ? Boolean.valueOf(rootElement.getAttribute("remove")) : false;
    }

    /**
     * @see be.portima.dmpoc.installation.operation.export.modifier.AbstractXMLExportModifier#isApplicable(org.w3c.dom.Document)
     */
    @Override
    public boolean isApplicable(Document element) throws OperationExecutionException {

        try {
            NodeList nodes = (NodeList) xPath.evaluate(xPathNode, element, XPathConstants.NODESET);

            return nodes != null && nodes.getLength() > 0;

        } catch (XPathExpressionException e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @see be.portima.dmpoc.installation.operation.export.modifier.AbstractXMLExportModifier#modify(org.w3c.dom.Document)
     */
    @Override
    public void modify(Document element) throws OperationExecutionException {

        try {
            NodeList nodes = (NodeList) xPath.evaluate(xPathNode, element, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (remove) {
                    node.getParentNode().removeChild(node);
                } else {
                    if (nodeToAdd != null) {
                        Node newNodeToAdd = element.createElement(nodeToAdd);
                        node.appendChild(newNodeToAdd);
                        node = newNodeToAdd;
                    }
                    node.setTextContent(value);
                }
            }

        } catch (XPathExpressionException e) {
            throw new OperationExecutionException(e);
        }
    }
}
