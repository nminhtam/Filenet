/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.pe;

import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import filenet.vw.api.VWExposedFieldDefinition;
import filenet.vw.api.VWIndexDefinition;
import filenet.vw.api.VWTableDefinition;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public abstract class AbstractCreatePEConfigurationOperation extends Operation {

    protected NodeList fields;

    protected NodeList indexes;

    protected String description;

    protected XPath xPath;


    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractCreatePEConfigurationOperation(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public final void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {

        try {
            this.description = rootElement.getAttribute("description");

            xPath = xPathFactory.newXPath();

            fields = (NodeList) xPath.evaluate("//fieldDefinitions/field", rootElement, XPathConstants.NODESET);
            indexes = (NodeList) xPath.evaluate("//indexDefinitions/index", rootElement, XPathConstants.NODESET);

            this.initInternal(rootElement, prerequisites);

        } catch (XPathExpressionException e) {
            throw new OperationInitializationException(e);
        }
    }

    protected abstract void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException;


    protected VWExposedFieldDefinition[] createFields(VWTableDefinition def) throws Exception {

        // Process all fields in the new configuration.
        if (fields != null && fields.getLength() > 0) {
            VWExposedFieldDefinition[] fieldDefinitions = new VWExposedFieldDefinition[fields.getLength()];
            for (int i = 0; i < fields.getLength(); i++) {
                Node field = fields.item(i);

                String name = field.getAttributes().getNamedItem("name").getNodeValue();
                int type = Integer.valueOf(field.getAttributes().getNamedItem("type").getNodeValue());
                int length = 0;
                if (field.getAttributes().getNamedItem("length") != null) {
                    length = Integer.valueOf(field.getAttributes().getNamedItem("length").getNodeValue());
                }

                fieldDefinitions[i] = def.createFieldDefinition(name, type, length);
            }
            return fieldDefinitions;
        }
        return null;
    }

    protected VWIndexDefinition[] createIndexes(VWTableDefinition def) throws Exception {

        // Process all indexes in the new configuration.
        if (indexes != null && indexes.getLength() > 0) {
            VWIndexDefinition[] indexesDefinitions = new VWIndexDefinition[indexes.getLength()];
            for (int i = 0; i < indexes.getLength(); i++) {
                Node index = indexes.item(i);

                String name = index.getAttributes().getNamedItem("name").getNodeValue();
                String fieldNames = index.getAttributes().getNamedItem("fields").getNodeValue();

                indexesDefinitions[i] = def.createIndexDefinition(name, fieldNames.split(","));
            }
            return indexesDefinitions;
        }
        return null;
    }
}
