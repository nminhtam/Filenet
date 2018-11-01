/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.execution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lu.mtn.ibm.filenet.deployment.tool.Connection;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.WriteException;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.AbstractOperationDatasModifier;
import lu.mtn.ibm.filenet.deployment.tool.writer.InternationalizationWriter;
import lu.mtn.ibm.filenet.deployment.tool.writer.Writer;


/**
 * @author NguyenT
 *
 */
public class ExecutionContext {

    private Connection connection;

    private Map<String, Writer<?>> writers;

    private Map<String, Set<?>> variables;

    private Map<String, String> idRefs;
    
    private Map<String, Properties> resourceBundles;
    
    private InternationalizationWriter i18nWriter;

    /**
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws DOMException
     * @throws SecurityException
     * @throws IllegalArgumentException
     *
     */
    @SuppressWarnings("unchecked")
    public ExecutionContext(String fileConfiguration) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, IllegalArgumentException, SecurityException, DOMException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        this.writers = new HashMap<String, Writer<?>>();
        this.variables = new HashMap<String, Set<?>>();
        this.variables.put(Constants.EXPORTED, new HashSet<String>());
        this.variables.put(Constants.OPERATION_MODIFIERS, new HashSet<AbstractOperationDatasModifier>());
        this.idRefs = new HashMap<String, String>();
        this.resourceBundles = new HashMap<String, Properties>();

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();

        Document document = builder.parse(new InputSource(new FileReader(new File(fileConfiguration))));

        NodeList children = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3) {

                if ("filenetConnection".equals(node.getNodeName())) {

                    NamedNodeMap atts = node.getAttributes();

                    String jaas = atts.getNamedItem("jaasStanza") != null ? atts.getNamedItem("jaasStanza").getNodeValue() : null;
                    this.setConnection(new Connection(atts.getNamedItem("url").getNodeValue(), atts.getNamedItem("user").getNodeValue(), atts.getNamedItem("password")
                                    .getNodeValue(), atts.getNamedItem("objectStore").getNodeValue(), jaas, atts.getNamedItem(
                                    "connectionPoint").getNodeValue()));

                    System.out.println("Connection " + connection.getUrl() + " " + connection.getUser());

                } else if ("writers".equals(node.getNodeName())) {

                    NodeList writers = node.getChildNodes();

                    for (int j = 0; j < writers.getLength(); j++) {

                        Node writerNode = writers.item(j);
                        if (writerNode.getNodeType() != 3 && "writer".equals(writerNode.getNodeName())) {

                            String clazz = writerNode.getAttributes().getNamedItem("class").getNodeValue();
                            Writer<?> writer = ((Class<Writer<?>>) Class.forName(clazz)).getConstructor(String.class).newInstance(writerNode.getChildNodes() != null && !writerNode.getTextContent().isEmpty() ? writerNode.getTextContent() : null);

                            this.writers.put(writerNode.getAttributes().getNamedItem("name").getNodeValue(), writer);
                        }
                    }
                } else if ("internationalization".equals(node.getNodeName())) {

                    NodeList bundles = node.getChildNodes();

                    for (int j = 0; j < bundles.getLength(); j++) {

                        Node bundleNode = bundles.item(j);
                        if (bundleNode.getNodeType() != 3 && "bundle".equals(bundleNode.getNodeName())) {

                            String lang = bundleNode.getAttributes().getNamedItem("lang").getNodeValue();
                            String file = bundleNode.getAttributes().getNamedItem("file").getNodeValue();
                            
                            Properties prop = new Properties();
                            prop.load(new FileInputStream(file));
                            
                            this.resourceBundles.put(lang, prop);
                        } else if (bundleNode.getNodeType() != 3 && "writer".equals(bundleNode.getNodeName())) {

                            this.i18nWriter = new InternationalizationWriter(bundleNode.getChildNodes() != null && !bundleNode.getTextContent().isEmpty() ? bundleNode.getTextContent() : null);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return this.connection;
    }

    /**
     * @param connection
     *            the connection to set
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return the writers
     */
    public Map<String, Writer<?>> getWriters() {
        return this.writers;
    }

    /**
     * @param name
     * @return
     */
    public Writer<?> getWriter(String name) {
        return this.writers.get(name);
    }

    /**
     * @param writers
     *            the writers to set
     */
    public void setWriters(Map<String, Writer<?>> writers) {
        this.writers = writers;
    }

    /**
     * @return the variables
     */
    public Map<String, Set<?>> getVariables() {
        return this.variables;
    }

    /**
     * @return the idRefs
     */
    public Map<String, String> getIdRefs() {
        return this.idRefs;
    }
    
    /**
     * @return the resourceBundles
     */
    public Map<String, Properties> getResourceBundles() {
        return this.resourceBundles;
    }

    /**
     * @return the i18nWriter
     */
    public InternationalizationWriter getI18nWriter() {
        return this.i18nWriter;
    }

    /**
     * @param idRefs the idRefs to set
     */
    public void setIdRefs(Map<String, String> idRefs) {
        this.idRefs = idRefs;
    }

    public static boolean isIdMustBeInterpreted(String id) {
        return id.startsWith("#{") && id.endsWith("}");
    }

    public String interpretId(String id) {
        if (isIdMustBeInterpreted(id)) {
            id = id.substring(2, id.length() - 1);
            return this.idRefs.get(id);
        }
        return id;
    }

    public void open() {
        for (Writer<?> writer : this.writers.values()) {
            try {
                writer.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.i18nWriter != null) {
            try {
                this.i18nWriter.open();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        for (Writer<?> writer : this.writers.values()) {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.i18nWriter != null) {
            try {
                this.i18nWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
