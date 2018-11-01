/**
 *
 */
package lu.mtn.ibm.filenet.client.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;


import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FNCEWS40PortType;
import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FNCEWS40Service;
import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FaultResponse;
import com.filenet.ns.fnce._2006._11.ws.schema.Localization;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectFactory;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectValue;

import lu.mtn.ibm.filenet.client.FileNetCEClient;
import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;
import lu.mtn.ibm.filenet.client.ws.delegate.DocumentDelegateService;
import lu.mtn.ibm.filenet.client.ws.delegate.SearchDelegateService;
import lu.mtn.ibm.filenet.client.ws.handler.AuthenticationSoapHandler;

//

/**
 * @author NguyenT
 *
 */
public class FileNetCEWebServiceClient implements FileNetCEClient {

    private static final Logger LOGGER = Logger.getLogger(FileNetCEWebServiceClient.class);

    private String osName;

    private DocumentDelegateService documentDelegateService;

    private SearchDelegateService searchDelegateService;

    private static final int DEFAULT_PAGING_SIZE = 1000;


    /**
     *
     */
    @SuppressWarnings("rawtypes")
    public FileNetCEWebServiceClient(String url, String user, String password, String defaultObjectStore) {

        this.osName = defaultObjectStore;

        FNCEWS40Service service = new FNCEWS40Service(FileNetCEWebServiceClient.class.getResource("/wsdl/FNCEWS40MTOM.wsdl"), // Force using wsdl from
                                                                                                                              // the jar file
                        new QName("http://www.filenet.com/ns/fnce/2006/11/ws/MTOM/wsdl", "FNCEWS40Service"));

        FNCEWS40PortType port = service.getFNCEWS40MTOMPort();

        BindingProvider bindingProvider = (BindingProvider) port;

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        // requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, timeout);
        // requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT, timeout);

        // requestContext.put(BindingProvider.USERNAME_PROPERTY, user);
        // requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);

        SOAPBinding binding = (SOAPBinding) bindingProvider.getBinding();
        binding.setMTOMEnabled(true);

        List<Handler> handlerList = binding.getHandlerChain();
        if (handlerList == null)
            handlerList = new ArrayList<Handler>();

        handlerList.add(new AuthenticationSoapHandler(user, password));
        bindingProvider.getBinding().setHandlerChain(handlerList);

        Localization defaultLocale = new Localization();
        defaultLocale.setLocale("en-US");

        ObjectFactory factory = new ObjectFactory();

        this.documentDelegateService = new DocumentDelegateService(port, defaultLocale, factory);
        this.searchDelegateService = new SearchDelegateService(port, defaultLocale, factory);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String)
     */
    @Override
    public CEDocument getDocument(String id) throws ServiceCallException {
        try {
            return documentDelegateService.getDocument(id, this.osName, null, true);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String[])
     */
    @Override
    public CEDocument getDocument(String id, String[] additionalProps) throws ServiceCallException {
        try {
            return documentDelegateService.getDocument(id, this.osName, additionalProps, true);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String[], boolean)
     */
    @Override
    public CEDocument getDocument(String id, String[] additionalProps, boolean getContent) throws ServiceCallException {
        try {
            return documentDelegateService.getDocument(id, this.osName, additionalProps, getContent);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String)
     */
    @Override
    public CEDocument getDocument(String id, String objectStore) throws ServiceCallException {
        try {
            return documentDelegateService.getDocument(id, objectStore, null, true);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public CEDocument getDocument(String id, String objectStore, String[] additionalProps) throws ServiceCallException {
        try {
            return documentDelegateService.getDocument(id, objectStore, additionalProps, true);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String, java.lang.String[], boolean)
     */
    @Override
    public CEDocument getDocument(String id, String objectStore, String[] additionalProps, boolean getContent) throws ServiceCallException {
        try {
            return documentDelegateService.getDocument(id, objectStore, additionalProps, getContent);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }


    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#createDocument(lu.mtn.ibm.filenet.client.ce.dto.ce.dto.DocumentCreationRequest)
     */
    @Override
    public String createDocument(DocumentCreationRequest request) throws ServiceCallException {
        try {
            return documentDelegateService.createDocument(request, this.osName);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#createDocument(lu.mtn.ibm.filenet.client.ce.dto.ce.dto.DocumentCreationRequest, java.lang.String)
     */
    @Override
    public String createDocument(DocumentCreationRequest request, String ObjecStore) throws ServiceCallException {
        try {
            return documentDelegateService.createDocument(request, ObjecStore);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }


    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#linkDocumentToFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void linkDocumentToFolder(String documentId, String documentName, String folderPath) throws ServiceCallException {
        try {
            documentDelegateService.linkDocumentToFolder(documentId, documentName, folderPath, this.osName);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#linkDocumentToFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void linkDocumentToFolder(String documentId, String documentName, String folderPath, String objectStore) throws ServiceCallException {
        try {
            documentDelegateService.linkDocumentToFolder(documentId, documentName, folderPath, objectStore);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#updateDocument(lu.mtn.ibm.filenet.client.ce.dto.ce.dto.DocumentModificationRequest)
     */
    @Override
    public String updateDocument(DocumentModificationRequest request) throws ServiceCallException {
        try {
            return documentDelegateService.updateDocument(request, this.osName);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#updateDocument(lu.mtn.ibm.filenet.client.ce.dto.ce.dto.DocumentModificationRequest, java.lang.String)
     */
    @Override
    public String updateDocument(DocumentModificationRequest request, String objecStore) throws ServiceCallException {
        try {
            return documentDelegateService.updateDocument(request, objecStore);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#deleteDocument(java.lang.String)
     */
    @Override
    public void deleteDocument(String documentId) throws ServiceCallException {
        this.deleteDocument(documentId, this.osName);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#deleteDocument(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteDocument(String documentId, String objecStore) throws ServiceCallException {
        try {
            documentDelegateService.deleteDocument(documentId, objecStore);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String, java.lang.String)
     */
    @Override
    public Iterator<ObjectValue> search(String query, String objectStoreName) throws ServiceCallException {
        try {
            return searchDelegateService.search(objectStoreName, query, DEFAULT_PAGING_SIZE);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String, java.lang.String, int)
     */
    @Override
    public Iterator<ObjectValue> search(String query, String objectStoreName, int pagingSize) throws ServiceCallException {
        try {
            return searchDelegateService.search(objectStoreName, query, pagingSize);
        } catch (FaultResponse e) {
            LOGGER.error(e);
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String)
     */
    @Override
    public Iterator<ObjectValue> search(String query) throws ServiceCallException {
        return this.search(query, osName);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String, int)
     */
    @Override
    public Iterator<ObjectValue> search(String query, int pagingSize) throws ServiceCallException {
        return this.search(query, osName, pagingSize);
    }
}
