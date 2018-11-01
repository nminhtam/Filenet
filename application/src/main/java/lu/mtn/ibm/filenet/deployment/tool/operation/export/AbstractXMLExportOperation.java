/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import com.filenet.api.collection.BinaryList;
import com.filenet.api.collection.BooleanList;
import com.filenet.api.collection.DateTimeList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.collection.Float64List;
import com.filenet.api.collection.IdList;
import com.filenet.api.collection.Integer32List;
import com.filenet.api.collection.StringList;
import com.filenet.api.constants.PropertySettability;
import com.filenet.api.meta.ClassDescription;
import com.filenet.api.meta.PropertyDescription;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyBinary;
import com.filenet.api.property.PropertyBinaryList;
import com.filenet.api.property.PropertyBoolean;
import com.filenet.api.property.PropertyBooleanList;
import com.filenet.api.property.PropertyDateTime;
import com.filenet.api.property.PropertyDateTimeList;
import com.filenet.api.property.PropertyFloat64;
import com.filenet.api.property.PropertyFloat64List;
import com.filenet.api.property.PropertyId;
import com.filenet.api.property.PropertyIdList;
import com.filenet.api.property.PropertyInteger32;
import com.filenet.api.property.PropertyInteger32List;
import com.filenet.api.property.PropertyString;
import com.filenet.api.property.PropertyStringList;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.WriteException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.modifier.AbstractXMLExportModifier;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingWriterPrerequisite;
import lu.mtn.ibm.filenet.deployment.tool.writer.Writer;

/**
 * @author NguyenT
 *
 */
public abstract class AbstractXMLExportOperation<T> extends Operation {

    protected static final String CREATE = "create";

    protected static final String UPDATE = "update";

    protected String writerName;

    protected String operation;

    protected String idRef;

    protected Set<AbstractXMLExportModifier> modifiers;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public AbstractXMLExportOperation(String xml) throws OperationInitializationException {
        super(xml);
    }

    protected AbstractXMLExportOperation(String operation, String writerName) throws OperationInitializationException {
        super();
        this.modifiers = new HashSet<AbstractXMLExportModifier>();
        this.operation = operation;
        this.writerName = writerName;
        this.prerequisites.add(new ExistingWriterPrerequisite(writerName));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#init(org.w3c.dom.Element, java.util.List)
     */
    @Override
    public void init(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException {
        this.modifiers = new HashSet<AbstractXMLExportModifier>();

        writerName = rootElement.getAttribute("writer");
        operation = rootElement.getAttribute("operation");
        if (writerName == null) {
            throw new IllegalArgumentException("Writer paramater can't be null.");
        }
        if (operation == null) {
            throw new IllegalArgumentException("Operation paramater can't be null.");
        }

        idRef = rootElement.hasAttribute("idRef") ? rootElement.getAttribute("idRef") : null;

        this.initModifiers(rootElement);

        this.initInternal(rootElement, prerequisites);
    }

    /**
     * @throws OperationExecutionException
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#execute(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    public void execute(ExecutionContext context) throws OperationExecutionException {

        String exportRef = getExportRef();
        boolean stop = isAlreadyExported(context, exportRef);

        if (!stop) {

            addExported(context, exportRef);

            T object = findObject(context);

            if (object != null) {

                String tag = getTagFromOperation(operation);
                try {
                    Document doc = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument();
                    Element root = doc.createElement(tag);
                    doc.appendChild(root);
                if (idRef != null) {
                        root.setAttribute("idRef", idRef);
                }
                    writeDetails(object, doc, context);

                } catch (DOMException e) {
                    throw new OperationExecutionException(e);
                } catch (ParserConfigurationException e) {
                    throw new OperationExecutionException(e);
                }
            }
        }
    }

    protected abstract void initInternal(Element rootElement, List<Prerequisite> prerequisites) throws OperationInitializationException;

    protected abstract String getExportRef();

    protected abstract T findObject(ExecutionContext context);

    protected abstract String getId(T object);

    protected abstract void writeDetails(T object, Document doc, ExecutionContext context) throws OperationExecutionException;

    protected abstract String getTagFromOperation(String operation);

    @SuppressWarnings("unchecked")
    protected boolean isAlreadyExported(ExecutionContext context, String exportRef) {
        Set<String> exported = (Set<String>) context.getVariables().get(Constants.EXPORTED);
        boolean stop = false;
        synchronized (exported) {
            stop = exported.contains(exportRef);
        }
        return stop;
    }

    @SuppressWarnings("unchecked")
    protected void addExported(ExecutionContext context, String exportRef) {
        Set<String> exported = (Set<String>) context.getVariables().get(Constants.EXPORTED);
        synchronized (exported) {
           exported.add(exportRef);
        }
    }

    @SuppressWarnings("unchecked")
    protected void write(ExecutionContext context, Document element) throws OperationExecutionException {
        Writer<String> writer = (Writer<String>) context.getWriter(writerName);
        try {
            for (AbstractXMLExportModifier modifier : modifiers) {
                if (modifier.isApplicable(element)) {
                    modifier.modify(element);
                }
            }
            writer.write(toXML(element));
        } catch (WriteException e) {
            throw new OperationExecutionException(e);
        } catch (TransformerException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void writeProperties(Properties properties, Element root, Document doc, List<String> propertyNames) {

        for (String propertyName : propertyNames) {
            Property property = properties.get(propertyName);

            Object o = property.getObjectValue();
            if (o != null && !(o instanceof EngineCollection) || (o instanceof EngineCollection && !((EngineCollection) o).isEmpty()))  {

                int type = getType(property);
                Element propElement = createElement(doc, root, "property");
                propElement.setAttribute("name", property.getPropertyName());
                propElement.setAttribute("type", String.valueOf(type));
                Element valueElement;
                switch (type) {

                    case Constants.PROPERTY_TYPE_STRING:
                        valueElement = createElement(doc, propElement, "value");
                        valueElement.setTextContent(property.getStringValue());
                        break;

                    case Constants.PROPERTY_TYPE_STRING_ARRAY:
                        StringList stringList = (StringList) o;
                        for (Iterator<String> it = stringList.iterator(); it.hasNext(); ) {
                            valueElement = createElement(doc, propElement, "value");
                            valueElement.setTextContent(it.next());
                        }
                        break;

                    case Constants.PROPERTY_TYPE_INT:
                        valueElement = createElement(doc, propElement, "value");
                        valueElement.setTextContent(String.valueOf(property.getInteger32Value()));
                        break;

                    case Constants.PROPERTY_TYPE_INT_ARRAY:
                        Integer32List intList = (Integer32List) o;
                        for (Iterator<Integer> it = intList.iterator(); it.hasNext(); ) {
                            valueElement = createElement(doc, propElement, "value");
                            valueElement.setTextContent(String.valueOf(it.next()));
                        }
                        break;

                    case Constants.PROPERTY_TYPE_FLOAT:
                        valueElement = createElement(doc, propElement, "value");
                        valueElement.setTextContent(String.valueOf(property.getFloat64Value()));
                        break;

                    case Constants.PROPERTY_TYPE_FLOAT_ARRAY:
                        Float64List doubleList = (Float64List) o;
                        for (Iterator<Double> it = doubleList.iterator(); it.hasNext(); ) {
                            valueElement = createElement(doc, propElement, "value");
                            valueElement.setTextContent(String.valueOf(it.next()));
                        }
                        break;

                    case Constants.PROPERTY_TYPE_BINARY:
                        valueElement = createElement(doc, propElement, "value");
                        valueElement.setTextContent(new String(property.getBinaryValue()));
                        break;

                    case Constants.PROPERTY_TYPE_BINARY_ARRAY:
                        BinaryList byteList = (BinaryList) o;
                        for (Iterator<byte[]> it = byteList.iterator(); it.hasNext(); ) {
                            valueElement = createElement(doc, propElement, "value");
                            valueElement.setTextContent(new String(it.next()));
                        }
                        break;

                    case Constants.PROPERTY_TYPE_BOOLEAN:
                        valueElement = createElement(doc, propElement, "value");
                        valueElement.setTextContent(String.valueOf(property.getBooleanValue()));
                        break;

                    case Constants.PROPERTY_TYPE_BOOLEAN_ARRAY:
                        BooleanList booleanList = (BooleanList) o;
                        for (Iterator<Boolean> it = booleanList.iterator(); it.hasNext(); ) {
                            valueElement = createElement(doc, propElement, "value");
                            valueElement.setTextContent(String.valueOf(it.next()));
                        }
                        break;

                    case Constants.PROPERTY_TYPE_DATE_TIME:
                        propElement.setAttribute("pattern", Constants.PATTERN_DATE_TIME);
                        valueElement = createElement(doc, propElement, "value");
                        valueElement.setTextContent(new SimpleDateFormat(Constants.PATTERN_DATE_TIME).format(property.getDateTimeValue()));
                        break;

                    case Constants.PROPERTY_TYPE_DATE_TIME_ARRAY:
                        DateTimeList dateList = (DateTimeList) o;
                        propElement.setAttribute("pattern", Constants.PATTERN_DATE_TIME);
                        for (Iterator<Date> it = dateList.iterator(); it.hasNext(); ) {
                            valueElement = createElement(doc, propElement, "value");
                            valueElement.setTextContent(new SimpleDateFormat(Constants.PATTERN_DATE_TIME).format(it.next()));
                        }
                        break;

                    case Constants.PROPERTY_TYPE_ID:
                        valueElement = createElement(doc, propElement, "value");
                        valueElement.setTextContent(property.getIdValue().toString());
                        break;

                    case Constants.PROPERTY_TYPE_ID_ARRAY:
                        IdList idList = (IdList) o;
                        for (Iterator<Id> it = idList.iterator(); it.hasNext(); ) {
                            valueElement = createElement(doc, propElement, "value");
                            valueElement.setTextContent(it.next().toString());
                        }
                        break;
                }
            }
        }
    }

    /**
     * Retrieve the list of custom properties for the class.
     *
     * @param doc
     * @return
     */
    @SuppressWarnings("unchecked")
    protected List<String> retrievePropertyNames(ClassDescription classDesc) {
        List<String> result = new ArrayList<String>();

        for (Iterator<PropertyDescription> it = classDesc.get_PropertyDescriptions().iterator(); it.hasNext(); ) {
            PropertyDescription desc = it.next();
            if (!desc.get_IsSystemGenerated() && !desc.get_IsSystemOwned() && !desc.get_IsHidden()
                            && !desc.get_Settability().equals(PropertySettability.SETTABLE_ONLY_BEFORE_CHECKIN)) {
                result.add(desc.get_SymbolicName());
            }
        }
        Collections.sort(result);
        return result;
    }

    protected int getType(Property property) {

        if (property instanceof PropertyString) {
            return Constants.PROPERTY_TYPE_STRING;
        } else if (property instanceof PropertyStringList) {
            return Constants.PROPERTY_TYPE_STRING_ARRAY;
        } else if (property instanceof PropertyInteger32) {
            return Constants.PROPERTY_TYPE_INT;
        } else if (property instanceof PropertyInteger32List) {
            return Constants.PROPERTY_TYPE_INT_ARRAY;
        } else if (property instanceof PropertyFloat64) {
            return Constants.PROPERTY_TYPE_FLOAT;
        } else if (property instanceof PropertyFloat64List) {
            return Constants.PROPERTY_TYPE_FLOAT_ARRAY;
        } else if (property instanceof PropertyBinary) {
            return Constants.PROPERTY_TYPE_BINARY;
        } else if (property instanceof PropertyBinaryList) {
            return Constants.PROPERTY_TYPE_BINARY_ARRAY;
        } else if (property instanceof PropertyBoolean) {
            return Constants.PROPERTY_TYPE_BOOLEAN;
        } else if (property instanceof PropertyBooleanList) {
            return Constants.PROPERTY_TYPE_BOOLEAN_ARRAY;
        } else if (property instanceof PropertyDateTime) {
            return Constants.PROPERTY_TYPE_DATE_TIME;
        } else if (property instanceof PropertyDateTimeList) {
            return Constants.PROPERTY_TYPE_DATE_TIME_ARRAY;
        } else if (property instanceof PropertyId) {
            return Constants.PROPERTY_TYPE_ID;
        } else if (property instanceof PropertyIdList) {
            return Constants.PROPERTY_TYPE_ID_ARRAY;
        }
        throw new IllegalStateException("The property type " + property.getClass() + " is not supported.");
    }

    /**
     * @param idRef the idRef to set
     */
    public void setIdRef(String idRef) {
        this.idRef = idRef;
    }

    public <E> List<E> sort(Iterator<E> iterator, Comparator<E> comparator) {
        List<E> sortedList = new ArrayList<E>();
        for (; iterator.hasNext();) {
            sortedList.add(iterator.next());
        }
        Collections.sort(sortedList, comparator);
        return sortedList;
    }

    @SuppressWarnings("unchecked")
    protected void initModifiers(Element rootElement) throws OperationInitializationException {
        try {
            NodeList children = rootElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {

                Node node = children.item(i);

                if (node.getNodeType() != 3 && "modifiers".equals(node.getNodeName())) {

                    NodeList nodes = node.getChildNodes();
                    for (int j = 0; j < nodes.getLength(); j++) {

                        Node modifierNode = nodes.item(j);
                        if (modifierNode.getNodeType() != 3 && "modifier".equals(modifierNode.getNodeName())) {

                            String className = modifierNode.getAttributes().getNamedItem("class").getTextContent();

                            Class<AbstractXMLExportModifier> clazz = (Class<AbstractXMLExportModifier>) Class.forName(className);

                            AbstractXMLExportModifier modifier = clazz.getConstructor(Element.class).newInstance(modifierNode);
                            this.modifiers.add(modifier);
                        }
                    }
                }
            }
        } catch (DOMException e) {
            throw new OperationInitializationException(e);
        } catch (IllegalArgumentException e) {
            throw new OperationInitializationException(e);
        } catch (SecurityException e) {
            throw new OperationInitializationException(e);
        } catch (ClassNotFoundException e) {
            throw new OperationInitializationException(e);
        } catch (InstantiationException e) {
            throw new OperationInitializationException(e);
        } catch (IllegalAccessException e) {
            throw new OperationInitializationException(e);
        } catch (InvocationTargetException e) {
            throw new OperationInitializationException(e);
        } catch (NoSuchMethodException e) {
            throw new OperationInitializationException(e);
        }
    }

}
