/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.execution;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;


/**
 * @author NguyenT
 *
 */
public class XMLHandler extends DefaultHandler {

    private Execution execution;

    private StringBuffer sb;

//    private boolean isAfterStartTag;
//
//    private boolean hasContent;

    private String startTag;

    private boolean startParsingOperations;

    public XMLHandler(Execution execution) {
        super();
        this.execution = execution;
        startParsingOperations = false;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if ("operations".equals(qName) && !startParsingOperations) {
            startParsingOperations = true;

        } else if (startParsingOperations) {

//            this.isAfterStartTag = true;
            if (startTag == null && execution.getOperations().containsKey(qName)) {
                sb = new StringBuffer("<").append(qName);

                for (int i = 0; i < attributes.getLength(); ++i) {
                     sb.append(" ").append(attributes.getQName(i)).append("=\"").append(StringEscapeUtils.escapeXml(attributes.getValue(i))).append("\"");
                }
                sb.append(">");

                startTag = qName;
            } else {

                sb.append("<").append(qName);
                for (int i = 0; i < attributes.getLength(); ++i) {
                    sb.append(" ").append(attributes.getQName(i)).append("=\"").append(StringEscapeUtils.escapeXml(attributes.getValue(i))).append("\"");
                }
                sb.append(">");
            }
        }
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {

//        if (hasContent) {
//            hasContent = false;
//            sb.append("]]>");
//        }
        sb.append("</").append(qName).append(">");

        if (qName.equals(startTag)) {
            Class<? extends Operation> op = execution.getOperations().get(qName);
            try {
                if (op != null) {

                    startTag = null;
                    Operation task = op.getConstructor(String.class).newInstance(sb.toString());
                    execution.getTasks().add(task);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (sb != null) {
            String s = new String(ch, start, length);

            if (!s.replaceAll("\\n|\\s*", "").isEmpty()) {
//                if (isAfterStartTag) {
//                    sb.append("<![CDATA[");
//                    isAfterStartTag = false;
//                }
//                hasContent = true;
                sb.append(StringEscapeUtils.escapeXml(s));
            }
        }
    }

}
