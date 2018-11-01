/**
    IBM grants you a nonexclusive copyright license to use all programming code
    examples from which you can generate similar function tailored to your own
    specific needs.

    All sample code is provided by IBM for illustrative purposes only.
    These examples have not been thoroughly tested under all conditions.  IBM,
    therefore cannot guarantee or imply reliability, serviceability, or function of
    these programs.

    All Programs or code component contained herein are provided to you �AS IS �
    without any warranties of any kind.
    The implied warranties of non-infringement, merchantability and fitness for a
    particular purpose are expressly disclaimed.

    Copyright IBM Corporation 2007, ALL RIGHTS RESERVED.
 */

package lu.mtn.ibm.filenet.client.ri;

import com.filenet.api.core.Factory;
import com.filenet.api.util.UserContext;

/**
 * This object represents the connection with the Content Engine. Once
 * connection is established, it intializes Domain and ObjectStoreSet with
 * available Domain and ObjectStoreSet.
 *
 */
public class CEConnection extends AbstractFilenetConnection {
    
    protected String ceWsUrl;

    protected String username;

    protected String password;

    protected String jaasStanza;
    
    /*
     * constructor
     */
    public CEConnection(String ceWsUrl, String username, String password, String jaasStanza) {
        this.ceWsUrl = ceWsUrl;
        this.username = username;
        this.password = password;
        this.jaasStanza = jaasStanza;
    }

    /*
     * Establishes connection with Content Engine using supplied username,
     * password, JAAS stanza and CE Uri
     */
    public void establishConnection() {
        con = Factory.Connection.getConnection(ceWsUrl);
        subject = UserContext.createSubject(con, username, password, jaasStanza);
        pushSubject();
        dom = fetchDomain();
        domainName = dom.get_Name();
        // ost = getOSSet();
        isConnected = true;
    }
}
