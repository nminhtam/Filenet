/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.writer;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import lu.mtn.ibm.filenet.deployment.tool.operation.exception.WriteException;


/**
 * @author NguyenT
 *
 */
public abstract class Writer<T> {

    /**
     * @param xml
     */
    public Writer(String xml) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = docFactory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(xml)));

            this.init(document.getDocumentElement());

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public abstract void init(Element rootElement);

    public abstract void write(T content) throws WriteException;

    public abstract void open() throws WriteException;

    public abstract void close();
}
