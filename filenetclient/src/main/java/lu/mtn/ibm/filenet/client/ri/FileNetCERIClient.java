/**
 *
 */
package lu.mtn.ibm.filenet.client.ri;

import static lu.mtn.ibm.filenet.client.dto.CEDocument.PROP_DOC_TITLE;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.ReservationType;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.client.FileNetCEClient;
import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.CEDocumentContent;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;

/**
 * Implementation of the FileNet Client using the Reference Implementation API.
 *
 * @author nguyent
 *
 */
public class FileNetCERIClient implements FileNetCEClient {

    private AbstractFilenetConnection connection;

    private Map<String, ObjectStore> objectStores;

    protected String osName;

    /**
     *
     */
    public FileNetCERIClient(String ceWsUrl, String username, String password, String defaultObjectStore, String jaasStanza) {
        super();
        this.osName = defaultObjectStore;

        this.connection = new CEConnection(ceWsUrl, username, password, jaasStanza);
        this.objectStores = new HashMap<String, ObjectStore>();
    }
    
    /**
    *
    */
   public FileNetCERIClient(AbstractFilenetConnection connection, String defaultObjectStore) {
       super();
       
       this.osName = defaultObjectStore;

       this.connection = connection;
       this.objectStores = new HashMap<String, ObjectStore>();
   }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String)
     */
    @Override
    public CEDocument getDocument(String id) throws ServiceCallException {
        return this.getDocument(id, null, true);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String[])
     */
    @Override
    public CEDocument getDocument(String id, String[] additionalProps) throws ServiceCallException {
        return this.getDocument(id, additionalProps, true);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String[], boolean)
     */
    @Override
    public CEDocument getDocument(String id, String[] additionalProps, boolean getContent) throws ServiceCallException {
        return this.getDocument(id, osName, additionalProps, getContent);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String)
     */
    @Override
    public CEDocument getDocument(String id, String objectStore) throws ServiceCallException {
        return this.getDocument(id, objectStore, null, true);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public CEDocument getDocument(String id, String objectStore, String[] additionalProps) throws ServiceCallException {
        return this.getDocument(id, objectStore, additionalProps, true);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#getDocument(java.lang.String, java.lang.String, java.lang.String[], boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public CEDocument getDocument(String id, String objectStore, String[] additionalProps, boolean getContent) throws ServiceCallException {

        try {
            Set<String> props = new HashSet<String>();
            if (getContent) {
               props.add("ContentElements");
            }
            if (additionalProps != null) {
                for (int i = 0; i < additionalProps.length; i++) {
                    props.add(additionalProps[i]);
                }
            }
            Document doc = Factory.Document.fetchInstance(this.getObjectStore(objectStore), new Id(id), FileNetCEApiUtil.createFilter(props.toArray(new String[0])));
            CEDocument ceDoc = null;
            if (doc != null) {
                ceDoc = new CEDocument();
                ceDoc.setDocumentClass(doc.getClassName());
                ceDoc.setId(id);

                for (Iterator<Property> it = doc.getProperties().iterator(); it.hasNext();) {

                    Property property = it.next();
                    ceDoc.getProperties().put(property.getPropertyName(), FileNetCEApiUtil.getPropertyValue(property));
                }
                if (getContent)
                    ceDoc.setContents(FileNetCEApiUtil.getContents(doc));
            }
            return ceDoc;

        } catch (Exception e) {
            throw new ServiceCallException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public CEDocument getCurrentDocumentByVersionSeriesId(String vsId, String objectStore, String[] additionalProps, boolean getContent) throws ServiceCallException {
        try {
            StringBuilder sb = new StringBuilder("SELECT Id");
            if (getContent) {
                sb.append(", ContentElements");
            }
            if (additionalProps != null) {
                for (int i = 0; i < additionalProps.length; i++) {
                    sb.append(", ").append(additionalProps[i]);
                }
            }
            sb.append(" FROM Document WHERE VersionSeries = '").append(vsId).append("' AND IsCurrentVersion = True");
            
            Iterator<Object> ite = search(sb.toString(), objectStore);
            CEDocument ceDoc = null;
            if (ite.hasNext()) {
                Document doc = (Document) ite.next();
                ceDoc = new CEDocument();
                ceDoc.setDocumentClass(doc.getClassName());
                ceDoc.setId(doc.get_Id().toString());

                for (Iterator<Property> it = doc.getProperties().iterator(); it.hasNext();) {

                    Property property = it.next();
                    ceDoc.getProperties().put(property.getPropertyName(), FileNetCEApiUtil.getPropertyValue(property));
                }
                if (getContent)
                    ceDoc.setContents(FileNetCEApiUtil.getContents(doc));
            }
            return ceDoc;
        } catch (Exception e) {
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#createDocument(lu.mtn.ibm.filenet.client.ce.dto.ce.dto.DocumentCreationRequest)
     */
    @Override
    public String createDocument(DocumentCreationRequest request) throws ServiceCallException {
        return this.createDocument(request, osName);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#createDocument(lu.mtn.ibm.filenet.client.ce.dto.ce.dto.DocumentCreationRequest,
     *      java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String createDocument(DocumentCreationRequest request, String objecStore) throws ServiceCallException {

        try {
            ObjectStore os = this.getObjectStore(objecStore);
            Document doc = Factory.Document.createInstance(os, request.getDocumentClassName());
            for (Entry<String, Object> entry : request.getDocProps().entrySet()) {
                doc.getProperties().putObjectValue(entry.getKey(), entry.getValue());
            }
            doc.getProperties().putValue(CEDocument.PROP_DOC_TITLE, request.getDocumentName());

            doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);

            List<CEDocumentContent> contents = request.getContents();

            if (contents != null && !contents.isEmpty()) {

                List<ContentTransfer> contentTransfers = new ArrayList<ContentTransfer>(contents.size());
                for (CEDocumentContent content : contents) {
                    contentTransfers.add(FileNetCEApiUtil.createContentTransfer(content.getName(), new ByteArrayInputStream(content.getContent()), content.getMimeType()));
                }

                ContentElementList cel = Factory.ContentElement.createList();
                cel.addAll(contentTransfers);
                doc.set_ContentElements(cel);
            }

            doc.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));

            if (request.getFolder() != null) {
                linkDocumentToFolder(doc, request.getDocumentName(), request.getFolder(), os);
            }
            return doc.get_Id().toString();
        } catch (EngineRuntimeException e) {
            throw new ServiceCallException(e);
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#linkDocumentToFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void linkDocumentToFolder(String documentId, String documentName, String folderPath) throws ServiceCallException {
        this.linkDocumentToFolder(documentId, documentName, folderPath, osName);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#linkDocumentToFolder(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void linkDocumentToFolder(String documentId, String documentName, String folderPath, String objectStore) throws ServiceCallException {
        ObjectStore os = this.getObjectStore(objectStore);
        Document doc = Factory.Document.fetchInstance(os, new Id(documentId), null);
        this.linkDocumentToFolder(doc, documentName, folderPath, os);
    }

    private void linkDocumentToFolder(Document doc, String documentName, String folderPath, ObjectStore objectStore) throws ServiceCallException {
        Folder fo = Factory.Folder.fetchInstance(objectStore, folderPath, null);
        ReferentialContainmentRelationship rcr = FileNetCEApiUtil.createReferentialContainmentRelationship(fo, doc, documentName);
        rcr.save(RefreshMode.NO_REFRESH);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#updateDocument(lu.mtn.ibm.filenet.client.ce.dto.ce.dto.DocumentModificationRequest)
     */
    @Override
    public String updateDocument(DocumentModificationRequest request) throws ServiceCallException {
        return this.updateDocument(request, osName);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#updateDocument(lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest, java.lang.String)
     */
    @Override
    public String updateDocument(DocumentModificationRequest request, String objecStore) throws ServiceCallException {
        return this.updateDocument(request, objecStore, true, false);
    }
    
    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#updateDocument(lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest, java.lang.String)
     */
    public String updateDocument(DocumentModificationRequest request, String objecStore, boolean forceNewVersion) throws ServiceCallException {
        return this.updateDocument(request, objecStore, true, forceNewVersion);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#deleteDocument(java.lang.String)
     */
    @Override
    public void deleteDocument(String documentId) throws ServiceCallException {
        this.deleteDocument(documentId, osName);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#deleteDocument(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteDocument(String documentId, String objecStore) throws ServiceCallException {
        Document doc = Factory.Document.fetchInstance(this.getObjectStore(objecStore), documentId, null);
        doc.delete();
        doc.save(RefreshMode.NO_REFRESH);
    }



    public String updateCustomObject(DocumentModificationRequest request) throws ServiceCallException {
        return this.updateCustomObject(request, osName);
    }

    public String updateCustomObject(DocumentModificationRequest request, String ObjecStore) throws ServiceCallException {

        ObjectStore os = this.getObjectStore(ObjecStore);

        PropertyFilter pf = FileNetCEApiUtil.createFilter(new String[] { PROP_DOC_TITLE });
        if (request.getDocProps() != null) {
            for (Entry<String, Object> entry : request.getDocProps().entrySet()) {
                pf.addIncludeProperty(new FilterElement(null, null, null, entry.getKey(), null));
            }
        }

        CustomObject co = Factory.CustomObject.fetchInstance(os, new Id(request.getDocumentId()), pf);

        if (request.getDocProps() != null) {
            Properties props = co.getProperties();
            for (Entry<String, Object> entry : request.getDocProps().entrySet()) {
                props.putObjectValue(entry.getKey(), entry.getValue());
            }
            co.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));
        }
        return co.get_Id().toString();
    }

    public String updateDocument(DocumentModificationRequest request, boolean keepVersions, boolean forceNewVersion) throws ServiceCallException {
        return this.updateDocument(request, osName, keepVersions, forceNewVersion);
    }

    @SuppressWarnings("unchecked")
    public String updateDocument(DocumentModificationRequest request, String ObjecStore, boolean keepVersions, boolean forceNewVersion) throws ServiceCallException {

        ObjectStore os = this.getObjectStore(ObjecStore);

        PropertyFilter pf = FileNetCEApiUtil.createFilter(new String[] { PROP_DOC_TITLE });
        if (request.getDocProps() != null) {
            for (Entry<String, Object> entry : request.getDocProps().entrySet()) {
                pf.addIncludeProperty(new FilterElement(null, null, null, entry.getKey(), null));
            }
        }

        Document doc = Factory.Document.fetchInstance(os, new Id(request.getDocumentId()), pf);

        // The contents of the document is modified, not only properties so create a new version.
        if (request.getContents() != null || forceNewVersion) {

            // Check out the Document object and save it.
            doc.checkout(ReservationType.EXCLUSIVE, null, doc.getClassName(), doc.getProperties());
            doc.save(RefreshMode.REFRESH);

            // Get the Reservation object from the Document object.
            Document reservation = (Document) doc.get_Reservation();

            reservation.getProperties().putValue(PROP_DOC_TITLE, request.getDocumentName() != null ? request.getDocumentName() : doc.getProperties().getStringValue(PROP_DOC_TITLE));

            if (request.getDocProps() != null) {
                Properties props = reservation.getProperties();
                for (Entry<String, Object> entry : request.getDocProps().entrySet()) {
                    props.putObjectValue(entry.getKey(), entry.getValue());
                }
            }

            // Add content to the Reservation object.
           
            // Add a ContentElementList containing one ContentTransfer object.
            List<ContentTransfer> contentTransfers = new ArrayList<ContentTransfer>(request.getContents().size());
            for (CEDocumentContent content : request.getContents()) {
                contentTransfers.add(FileNetCEApiUtil.createContentTransfer(content.getName(), new ByteArrayInputStream(content.getContent()), content.getMimeType()));
            }

            ContentElementList cel = Factory.ContentElement.createList();
            cel.addAll(contentTransfers);
            reservation.set_ContentElements(cel);

            reservation.save(RefreshMode.NO_REFRESH);

            // Check in Reservation object as major version.
            reservation.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);

            if (!keepVersions) {

                reservation.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID, PropertyNames.VERSIONS}));

                Iterator<Document> it = reservation.get_Versions().iterator();
                while (it.hasNext()) {
                    Document d = it.next();
                    if (!d.get_IsCurrentVersion()) {
                        d.delete();
                        d.save(RefreshMode.NO_REFRESH);
                    }
                }
            } else {
                reservation.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));
            }

            return reservation.get_Id().toString();

        } else {

            if (request.getDocProps() != null) {
                Properties props = doc.getProperties();
                for (Entry<String, Object> entry : request.getDocProps().entrySet()) {
                    props.putObjectValue(entry.getKey(), entry.getValue());
                }
                doc.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));
            }
            return doc.get_Id().toString();
        }
    }

    /**
    *
    */
    public void openConnection() {
        if (!this.connection.isConnected()) {
            this.connection.establishConnection();
        } else {
            this.connection.pushSubject();
        }
    }

    /**
     *
     */
    public void reConnect() {
        this.connection.establishConnection();
    }

    /**
     *
     */
    public void pushSubject() {
        this.connection.pushSubject();
    }

    /**
     * @param objectStoreName
     * @return
     */
    public ObjectStore getObjectStore(String objectStoreName) {
        this.openConnection();

        ObjectStore os = objectStores.get(objectStoreName);
        if (os == null) {
            os = this.connection.fetchOS(objectStoreName);
            objectStores.put(objectStoreName, os);
        }
        return os;
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Object> search(String query, String objectStoreName) throws ServiceCallException {

        ObjectStore os = this.getObjectStore(objectStoreName);

        SearchSQL sql = new SearchSQL(query);
        SearchScope ss = new SearchScope(os);
        EngineCollection ec = ss.fetchObjects(sql, null, null, Boolean.TRUE);

        return ec.iterator();
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String, java.lang.String, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Object> search(String query, String objectStoreName, int pagingSize) throws ServiceCallException {

        ObjectStore os = this.getObjectStore(objectStoreName);

        SearchSQL sql = new SearchSQL(query);
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, pagingSize, null, Boolean.TRUE);
        return ec.iterator();
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String)
     */
    @Override
    public Iterator<Object> search(String query) throws ServiceCallException {
        return this.search(query, osName);
    }

    /**
     * @see lu.mtn.ibm.filenet.client.FileNetCEClient#search(java.lang.String, int)
     */
    @Override
    public Iterator<Object> search(String query, int pagingSize) throws ServiceCallException {
        return this.search(query, osName, pagingSize);
    }

    /**
     * @param os
     * @param folderPath
     * @return
     */
    public Folder findOrCreateFolders(String folderPath) {
        return this.findOrCreateFolders(osName, folderPath);
    }
    
    /**
     * @param os
     * @param folderPath
     * @return
     */
    public Folder findOrCreateFolders(String objectStoreName, String folderPath) {

        ObjectStore os = this.getObjectStore(objectStoreName);
        
        return findOrCreateFolders(os, folderPath);
    }

    private Folder findOrCreateFolders(ObjectStore os, String folderPath) {
        Folder folder = null;

        try {
            folder = Factory.Folder.fetchInstance(os, folderPath, null);
        } catch (EngineRuntimeException e) {
            if (!ExceptionCode.E_OBJECT_NOT_FOUND.equals(e.getExceptionCode())) {
                throw e;
            }
        }

        if (folder == null) {

            if (folderPath.endsWith("/")) {
                folderPath = folderPath.substring(0, folderPath.length() - 1);
            }
            int index = folderPath.lastIndexOf("/");
            String parentPath = folderPath.substring(0, index + 1);
            String folderName = folderPath.substring(index + 1);

            Folder parentFolder;
            if ("/".equals(parentPath) || parentPath.isEmpty()) {
                parentFolder = os.get_RootFolder();
            } else {
                parentFolder = findOrCreateFolders(os, parentPath);
            }

            folder = Factory.Folder.createInstance(os, null);
            folder.set_Parent(parentFolder);
            folder.set_FolderName(folderName);
            folder.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));
        }
        return folder;
    }
    
    public Folder findOrCreateFolders(String folderPath, String folderClassName, Map<String, Object> props) {
        Folder folder = null;

        ObjectStore os = this.getObjectStore(osName);
        
        try {
            folder = Factory.Folder.fetchInstance(os, folderPath, null);
        } catch (EngineRuntimeException e) {
            if (!ExceptionCode.E_OBJECT_NOT_FOUND.equals(e.getExceptionCode())) {
                throw e;
            }
        }

        if (folder == null) {

            if (folderPath.endsWith("/")) {
                folderPath = folderPath.substring(0, folderPath.length() - 1);
            }
            int index = folderPath.lastIndexOf("/");
            String parentPath = folderPath.substring(0, index + 1);
            String folderName = folderPath.substring(index + 1);

            Folder parentFolder;
            if ("/".equals(parentPath) || parentPath.isEmpty()) {
                parentFolder = os.get_RootFolder();
            } else {
                parentFolder = findOrCreateFolders(os, parentPath);
            }

            folder = Factory.Folder.createInstance(os, folderClassName);
            
            for (Entry<String, Object> entry : props.entrySet()) {
                folder.getProperties().putObjectValue(entry.getKey(), entry.getValue());
            }
            
            folder.set_Parent(parentFolder);
            folder.set_FolderName(folderName);
            folder.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));
        }
        return folder;
    }
    
    public void releaseConnection() {
        this.connection.release();
    }
}
