/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.modifier;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;

/**
 * @author NguyenT
 *
 */
public abstract class AbstractXMLExportModifier {

    protected XPathFactory xPathFactory;

    protected XPath xPath;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractXMLExportModifier(Element element) throws OperationInitializationException {

        try {
            xPathFactory = XPathFactory.newInstance("http://java.sun.com/jaxp/xpath/dom");

            xPath = xPathFactory.newXPath();

        } catch (XPathFactoryConfigurationException e) {
            throw new OperationInitializationException(e);
        }
    }


    public abstract boolean isApplicable(Document element) throws OperationExecutionException;

    public abstract void modify(Document element) throws OperationExecutionException;
}
