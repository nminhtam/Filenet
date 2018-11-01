/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.modifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;

/**
 * @author NguyenT
 *
 */
public class RegexDocumentContentXMLExportModifier extends AbstractXMLExportModifier {

    private String xPathNode;

    private Map<String, String> replacements;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public RegexDocumentContentXMLExportModifier(Element rootElement) throws OperationInitializationException {
        super(rootElement);

        this.replacements = new HashMap<String, String>();

        this.xPathNode = rootElement.getAttribute("xPath");

        NodeList children = rootElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3 && "replacement".equals(node.getNodeName())) {
                replacements.put(node.getAttributes().getNamedItem("regex").getNodeValue(), node.getAttributes().getNamedItem("substitute").getNodeValue());
            }
        }
    }

    /**
     * @see be.portima.dmpoc.installation.operation.export.modifier.AbstractXMLExportModifier#isApplicable(org.w3c.dom.Document)
     */
    @Override
    public boolean isApplicable(Document element) throws OperationExecutionException {

        try {
            NodeList nodes = (NodeList) xPath.evaluate(xPathNode, element, XPathConstants.NODESET);

            return nodes != null && nodes.getLength() > 0;

        } catch (XPathExpressionException e) {
            throw new OperationExecutionException(e);
        }
    }

    /**
     * @see be.portima.dmpoc.installation.operation.export.modifier.AbstractXMLExportModifier#modify(org.w3c.dom.Document)
     */
    @Override
    public void modify(Document element) throws OperationExecutionException {

        try {
            NodeList nodes = (NodeList) xPath.evaluate(xPathNode, element, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if ("content".equals(node.getNodeName())) {

                    boolean zipContent = node.getAttributes().getNamedItem("zip") != null ? Boolean.valueOf(node.getAttributes().getNamedItem("zip").getTextContent()) : Constants.DEFAULT_ZIP_CONTENT;

                    byte[] content = this.readContent(node, zipContent);

                    content = this.modifyContent(content);

                    this.writeContent(node, zipContent, content);
                }
            }

        } catch (XPathExpressionException e) {
            throw new OperationExecutionException(e);
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        }
    }

    protected void writeContent(Node node, boolean zipContent, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream os = zipContent ? new GZIPOutputStream(bos) : bos;
        try {
            os.write(content, 0, content.length);
            os.close();

            node.setTextContent(new String(Base64.encodeBase64(bos.toByteArray())));

        } finally {
            bos.close();
        }
    }

    protected byte[] readContent(Node node, boolean zipContent) throws IOException {
        byte[] content = Base64.decodeBase64(node.getTextContent());

        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        InputStream is = zipContent ? new GZIPInputStream(bis) : bis;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int length;
            while ((length = is.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            content = bos.toByteArray();
            return content;
        } finally {
            is.close();
        }
    }

    protected byte[] modifyContent(byte[] content) throws OperationExecutionException {
        try {
            String result = new String(content);
            for (Entry<String, String> entry : replacements.entrySet()) {
                result = result.replaceAll(entry.getKey(), entry.getValue());
            }
             return result.getBytes("UTF-8");
        } catch (IOException e) {
            throw new OperationExecutionException(e);
        }
    }
}