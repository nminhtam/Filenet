/**
 *
 */
package lu.mtn.ibm.filenet.client.ws.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

/**
 * @author MTN
 *
 */
public class AuthenticationSoapHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOG = Logger.getLogger(AuthenticationSoapHandler.class);

    private String user;

    private String password;


    /**
     * @param user
     * @param password
     */
    public AuthenticationSoapHandler(String user, String password) {
        super();
        this.user = user;
        this.password = password;
    }


    @Override
    public boolean handleMessage(final SOAPMessageContext msgCtx) {

        // Indicator telling us which direction this message is going in
        final Boolean outInd = (Boolean) msgCtx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        // Handler must only add security headers to outbound messages
        if (outInd.booleanValue()) {
            try {
                // Get the SOAP Envelope
                final SOAPEnvelope envelope = msgCtx.getMessage().getSOAPPart().getEnvelope();

                // Header may or may not exist yet
                SOAPHeader header = envelope.getHeader();
                if (header == null)
                    header = envelope.addHeader();

                // Add WSS Usertoken Element Tree
                final SOAPElement security = header.addChildElement("Security", "wsse", "http://schemas.xmlsoap.org/ws/2002/12/secext");
                final SOAPElement userToken = security.addChildElement("UsernameToken", "wsse");
                userToken.addChildElement("Username", "wsse").addTextNode(this.user);
                userToken.addChildElement("Password", "wsse").addTextNode(this.password);

            } catch (final Exception e) {
                LOG.error(e);
                return false;
            }
        }
        return true;
    }


    /**
     * @see javax.xml.ws.handler.Handler#handleFault(javax.xml.ws.handler.MessageContext)
     */
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }


    /**
     * @see javax.xml.ws.handler.Handler#close(javax.xml.ws.handler.MessageContext)
     */
    @Override
    public void close(MessageContext context) {
    }


    /**
     * @see javax.xml.ws.handler.soap.SOAPHandler#getHeaders()
     */
    @Override
    public Set<QName> getHeaders() {
        return null;
    }

}
