/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.deployment.tool.operation.exception.WriteException;


/**
 * @author NguyenT
 *
 */
public class OperationsFileWriter extends Writer<String> {

    private String filePath;

    private PrintWriter writer;

    private FileOutputStream fos;

    /**
     * @param xml
     */
    public OperationsFileWriter(String xml) {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.writer.Writer#init(org.w3c.dom.Element)
     */
    @Override
    public void init(Element rootElement) {

        NodeList nodes = rootElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            if (node.getNodeType() != 3 && "filePath".equals(node.getNodeName())) {
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
    public synchronized void write(String content) throws WriteException {
        if (writer == null) {
            try {
                fos = new FileOutputStream(new File(filePath));
                writer = new PrintWriter(new OutputStreamWriter(fos, "UTF-8"));

                writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                writer.println("<operations>");

            } catch (FileNotFoundException e) {
                throw new WriteException(e);
            } catch (UnsupportedEncodingException e) {
                throw new WriteException(e);
            }
        }
        writer.println(content);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.writer.Writer#close()
     */
    @Override
    public void close() {
        if (writer != null) {
            writer.println("</operations>");
            writer.close();

            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
