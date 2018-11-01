/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.pe;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import filenet.vw.api.VWExposedFieldDefinition;
import filenet.vw.api.VWIndexDefinition;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;

/**
 * @author NguyenT
 *
 */
public abstract class AbstractXMLExportPEConfigurationOperation<T> extends AbstractXMLExportOperation<T> {

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractXMLExportPEConfigurationOperation(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @param operation
     * @param writerName
     * @throws OperationInitializationException
     */
    public AbstractXMLExportPEConfigurationOperation(String operation, String writerName) throws OperationInitializationException {
        super(operation, writerName);
    }

    protected void writeIndexes(Element root, Document doc, VWIndexDefinition[] indexes) {
        Element indexesElement = null;
        for (int i = 0; i < indexes.length; i++) {
            if (!indexes[i].isSystemIndex()) {
                if (indexesElement == null) {
                    indexesElement = createElement(doc, root, "indexDefinitions");
                }
                Element current = createElement(doc, indexesElement, "index");
                current.setAttribute("name", indexes[i].getName());

                String[] fields = indexes[i].getFieldNames();
                if (fields != null && fields.length > 0) {
                    StringBuffer sb = new StringBuffer(fields[0]);
                    for (int j = 1; j < fields.length; j++) {
                        sb.append(",").append(fields[j]);
                    }
                    current.setAttribute("fields", sb.toString());
                }
            }
        }
    }

    protected void writeExposedFields(Element root, Document doc, VWExposedFieldDefinition[] fields) {
        Element exposedFieldDefinitions = null;
        for (int i = 0; i < fields.length; i++) {
            if (!fields[i].isSystemField()) {
                if (exposedFieldDefinitions == null) {
                    exposedFieldDefinitions = createElement(doc, root, "fieldDefinitions");
                }
                Element current = createElement(doc, exposedFieldDefinitions, "field");
                current.setAttribute("name", fields[i].getName());
                current.setAttribute("type", String.valueOf(fields[i].getFieldType()));
                if (fields[i].getLength() > 0) {
                    current.setAttribute("length", String.valueOf(fields[i].getLength()));
                }
            }
        }
    }
}
