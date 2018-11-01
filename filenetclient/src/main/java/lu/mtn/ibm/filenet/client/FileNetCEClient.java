/**
 *
 */
package lu.mtn.ibm.filenet.client;

import java.util.Iterator;

import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;


/**
 * @author nguyent
 *
 */
public interface FileNetCEClient {

    /**
     * @param id
     * @return
     * @throws ServiceCallException
     */
    CEDocument getDocument(String id) throws ServiceCallException;

    /**
     * @param id
     * @param additionalProps
     * @return
     * @throws ServiceCallException
     */
    CEDocument getDocument(String id, String[] additionalProps) throws ServiceCallException;

    /**
     * @param id
     * @param additionalProps
     * @param getContent
     * @return
     * @throws ServiceCallException
     */
    CEDocument getDocument(String id, String[] additionalProps, boolean getContent) throws ServiceCallException;

    /**
     * @param id
     * @param objectStore
     * @return
     * @throws ServiceCallException
     */
    CEDocument getDocument(String id, String objectStore) throws ServiceCallException;

    /**
     * @param id
     * @param objectStore
     * @param additionalProps
     * @return
     * @throws ServiceCallException
     */
    CEDocument getDocument(String id, String objectStore, String[] additionalProps) throws ServiceCallException;

    /**
     * @param id
     * @param objectStore
     * @param additionalProps
     * @param getContent
     * @return
     * @throws ServiceCallException
     */
    CEDocument getDocument(String id, String objectStore, String[] additionalProps, boolean getContent) throws ServiceCallException;

    /**
     * @param request
     * @return
     * @throws ServiceCallException
     */
    String createDocument(DocumentCreationRequest request) throws ServiceCallException;

    /**
     * @param request
     * @param ObjecStore
     * @return
     * @throws ServiceCallException
     */
    String createDocument(DocumentCreationRequest request, String objecStore) throws ServiceCallException;

    /**
     * @param documentId
     * @param documentName
     * @param folderPath
     * @throws ServiceCallException
     */
    void linkDocumentToFolder(String documentId, String documentName, String folderPath) throws ServiceCallException;

    /**
     * @param documentId
     * @param documentName
     * @param folderPath
     * @param objectStore
     * @throws ServiceCallException
     */
    void linkDocumentToFolder(String documentId, String documentName, String folderPath, String objectStore) throws ServiceCallException;

    /**
     * @param request
     * @return
     * @throws ServiceCallException
     */
    String updateDocument(DocumentModificationRequest request) throws ServiceCallException;

    /**
     * @param request
     * @param ObjecStore
     * @return
     * @throws ServiceCallException
     */
    String updateDocument(DocumentModificationRequest request, String objectStoreName) throws ServiceCallException;

    /**
     * @param documentId
     * @throws ServiceCallException
     */
    void deleteDocument(String documentId) throws ServiceCallException;

    /**
     * @param documentId
     * @throws ServiceCallException
     */
    void deleteDocument(String documentId, String objectStoreName) throws ServiceCallException;

    /**
     * @param query
     * @param objectStoreName
     * @return
     * @throws ServiceCallException
     */
    Iterator<?> search(String query, String objectStoreName) throws ServiceCallException;

    /**
     * @param query
     * @param objectStoreName
     * @param pagingSize
     * @param maxResult
     * @return
     */
    Iterator<?> search(String query, String objectStoreName, int pagingSize) throws ServiceCallException;

    /**
     * @param query
     * @param objectStoreName
     * @return
     * @throws ServiceCallException
     */
    Iterator<?> search(String query) throws ServiceCallException;

    /**
     * @param query
     * @param objectStoreName
     * @param pagingSize
     * @param maxResult
     * @return
     */
    Iterator<?> search(String query, int pagingSize) throws ServiceCallException;
}