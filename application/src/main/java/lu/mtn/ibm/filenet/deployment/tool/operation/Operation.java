/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.filenet.api.collection.BinaryList;
import com.filenet.api.collection.BooleanList;
import com.filenet.api.collection.DateTimeList;
import com.filenet.api.collection.Float64List;
import com.filenet.api.collection.IdList;
import com.filenet.api.collection.Integer32List;
import com.filenet.api.collection.StringList;
import com.filenet.api.core.Factory;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;

/**
 * @author NguyenT
 *
 */
public abstract class Operation {

    protected static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
    protected static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    static {
        try {
            DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
            DOCUMENT_BUILDER_FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    protected XPathFactory xPathFactory;

    protected List<Prerequisite> prerequisites;

    protected Operation() throws OperationInitializationException {
        this.prerequisites = new ArrayList<Prerequisite>();
        try {
            xPathFactory = XPathFactory.newInstance("http://java.sun.com/jaxp/xpath/dom");
        } catch (XPathFactoryConfigurationException e) {
            throw new OperationInitializationException(e);
        }
    }

    /**
     * @param xml
     */
    public Operation(String xml) throws OperationInitializationException {
        try {
            this.prerequisites = new ArrayList<Prerequisite>();
            xPathFactory = XPathFactory.newInstance("http://java.sun.com/jaxp/xpath/dom");

            Document document = toDocument(xml);

            this.init(document.getDocumentElement(), prerequisites);

        } catch (OperationInitializationException e) {
            throw e;
        } catch (Exception e) {
            throw new OperationInitializationException(e);
        }
    }

    /**
     * @param connection
     * @return true if all prerequisites are met.
     */
    public boolean checkPrerequisites(ExecutionContext context) {

        if (prerequisites.isEmpty()) {
            return true;
        }

        List<Prerequisite> tmp = new ArrayList<Prerequisite>(prerequisites);
        for (Prerequisite prerequisite : tmp) {
            if (prerequisite.check(context)) {
                prerequisites.remove(prerequisite);
            }
        }

        if (prerequisites.isEmpty()) {
            return true;
        }
        return false;
    }

    public abstract void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException;

    public abstract void execute(ExecutionContext context) throws OperationExecutionException;

    public abstract String getDescription();

    /**
     * @return the prerequisites
     */
    public List<Prerequisite> getPrerequisites() {
        return this.prerequisites;
    }

    @SuppressWarnings("unchecked")
    protected Object getPropertyValue(List<String> values, int type, String name, String datePattern) throws ParseException {

        switch (type) {
            case Constants.PROPERTY_TYPE_STRING:
                return values.get(0);

            case Constants.PROPERTY_TYPE_STRING_ARRAY:
                StringList stringList = Factory.StringList.createList();
                stringList.addAll(values);
                return stringList;

            case Constants.PROPERTY_TYPE_INT:
                return Integer.valueOf(values.get(0));

            case Constants.PROPERTY_TYPE_INT_ARRAY:
                Integer32List intList = Factory.Integer32List.createList();
                for (String value : values) {
                    intList.add(Integer.valueOf(value));
                }
                return intList;

            case Constants.PROPERTY_TYPE_FLOAT:
                return Double.valueOf(values.get(0));

            case Constants.PROPERTY_TYPE_FLOAT_ARRAY:
                Float64List doubleList = Factory.Float64List.createList();
                for (String value : values) {
                    doubleList.add(Double.valueOf(value));
                }
                return doubleList;

            case Constants.PROPERTY_TYPE_BINARY:
                return values.get(0).getBytes();

            case Constants.PROPERTY_TYPE_BINARY_ARRAY:
                BinaryList byteList = Factory.BinaryList.createList();
                for (String value : values) {
                    byteList.add(value.getBytes());
                }
                return byteList;

            case Constants.PROPERTY_TYPE_BOOLEAN:
                return Boolean.valueOf(values.get(0));

            case Constants.PROPERTY_TYPE_BOOLEAN_ARRAY:
                BooleanList booleanList = Factory.BooleanList.createList();
                for (String value : values) {
                    booleanList.add(Boolean.valueOf(value));
                }
                return booleanList;

            case Constants.PROPERTY_TYPE_DATE_TIME:
                return new SimpleDateFormat(datePattern != null ? datePattern : Constants.PATTERN_DATE_TIME).parse(values.get(0));

            case Constants.PROPERTY_TYPE_DATE_TIME_ARRAY:
                SimpleDateFormat sdf = new SimpleDateFormat(datePattern != null ? datePattern : Constants.PATTERN_DATE_TIME);
                DateTimeList dateList = Factory.DateTimeList.createList();
                for (String value : values) {
                    dateList.add(sdf.parse(value));
                }
                return dateList;

            case Constants.PROPERTY_TYPE_ID:
                return new Id(values.get(0));

            case Constants.PROPERTY_TYPE_ID_ARRAY:
                IdList idList = Factory.IdList.createList();
                for (String value : values) {
                    idList.add(new Id(value));
                }
                return idList;

            default:
                return null;
        }
    }

    protected Document toDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        document.setXmlStandalone(true); // Remove the standalone attribute.
        return document;
    }

    protected String toXML(Node node) throws TransformerException, IOException {
        return toXML(node, true);
    }

    protected String toXML(Node node, boolean omitXmlDeclaration) throws TransformerException, IOException {

        Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
        //transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        if (omitXmlDeclaration) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        DOMSource source = new DOMSource(node);
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        sw.close();
        return sw.toString();
    }

    protected Element createElement(Document doc, Element parent, String name) {
        Element element = doc.createElement(name);
        if (parent != null) {
            parent.appendChild(element);
        }
        return element;
    }
}
