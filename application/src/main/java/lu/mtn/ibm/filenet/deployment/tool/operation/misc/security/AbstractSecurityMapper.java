/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.misc.security;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier;

/**
 * @author NguyenT
 *
 */
public abstract class AbstractSecurityMapper extends AbstractOperationDatasModifier {

    protected Map<String, String> mappings;

    protected XPath xPath;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractSecurityMapper(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier#init(org.w3c.dom.Element)
     */
    @Override
    public final void init(Element rootElement) throws OperationInitializationException {
        this.mappings = new HashMap<String, String>();
        xPath = xPathFactory.newXPath();

        try {
            NodeList list = (NodeList) xPath.evaluate("child::mapping", rootElement, XPathConstants.NODESET);

            for (int i = 0; i < list.getLength(); i++) {
                Node mapping = list.item(i);

                mappings.put(mapping.getAttributes().getNamedItem("name").getTextContent(), mapping.getAttributes().getNamedItem("substitute").getTextContent());
            }

        } catch (XPathExpressionException e) {
            throw new OperationInitializationException(e);
        }

        this.initInternal(rootElement);
    }

    public void initInternal(Element rootElement) throws OperationInitializationException {

    }
}
