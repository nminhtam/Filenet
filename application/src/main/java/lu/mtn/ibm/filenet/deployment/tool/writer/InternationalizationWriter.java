/**
 * 
 */
package lu.mtn.ibm.filenet.deployment.tool.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.deployment.tool.operation.exception.WriteException;

/**
 * @author MTN
 *
 */
public class InternationalizationWriter extends Writer<String[]> {

    private String filePath;

    private Map<String, Properties> properties;

    /**
     * @param xml
     */
    public InternationalizationWriter(String xml) {
        super(xml);
        this.properties = new HashMap<String, Properties>();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.writer.Writer#init(org.w3c.dom.Element)
     */
    @Override
    public void init(Element rootElement) {

        NodeList nodes = rootElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            if (node.getNodeType() != 3 && "filePathPrefix".equals(node.getNodeName())) {
                this.filePath = node.getTextContent();
                break;
            }
        }
    }

    /**
     * @throws WriteException
     * @see lu.mtn.ibm.filenet.deployment.tool.writer.Writer#open()
     */
    @Override
    public void open() throws WriteException {
    }

    /**
     * @throws WriteException
     * @see lu.mtn.ibm.filenet.deployment.tool.writer.Writer#write(java.lang.Object)
     */
    @Override
    public synchronized void write(String[] content) {
        
        String lang = content[0];
        Properties props = this.properties.get(lang);
        if (props == null) {
            props = new Properties();
            this.properties.put(lang, props);
        }
        
        props.put(content[1], content[2]);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.writer.Writer#close()
     */
    @Override
    public void close() {
        for (Entry<String, Properties> entry : this.properties.entrySet()) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(filePath + "_" + entry.getKey() + ".properties"));
                
                entry.getValue().store(fos, null);
                
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
