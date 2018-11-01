/**
 *
 */
package lu.mtn.ibm.filenet.client.ws.delegate;

import static lu.mtn.ibm.filenet.client.dto.CEDocument.CLASS_DOCUMENT;
import static lu.mtn.ibm.filenet.client.dto.CEDocument.PROP_CONTENT_ELEMENTS;
import static lu.mtn.ibm.filenet.client.dto.CEDocument.PROP_DOC_TITLE;
import static lu.mtn.ibm.filenet.client.dto.CEDocument.PROP_ID;
import static lu.mtn.ibm.filenet.client.dto.CEDocumentContent.PROP_CONTENT;
import static lu.mtn.ibm.filenet.client.dto.CEDocumentContent.PROP_CONTENT_TYPE;
import static lu.mtn.ibm.filenet.client.dto.CEDocumentContent.PROP_RETRIEVAL_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FNCEWS40PortType;
import com.filenet.ns.fnce._2006._11.ws.mtom.wsdl.FaultResponse;
import com.filenet.ns.fnce._2006._11.ws.schema.ChangeRequestType;
import com.filenet.ns.fnce._2006._11.ws.schema.ChangeResponseType;
import com.filenet.ns.fnce._2006._11.ws.schema.CheckinAction;
import com.filenet.ns.fnce._2006._11.ws.schema.CheckoutAction;
import com.filenet.ns.fnce._2006._11.ws.schema.ContentData;
import com.filenet.ns.fnce._2006._11.ws.schema.CreateAction;
import com.filenet.ns.fnce._2006._11.ws.schema.DeleteAction;
import com.filenet.ns.fnce._2006._11.ws.schema.DependentObjectType;
import com.filenet.ns.fnce._2006._11.ws.schema.ErrorRecordType;
import com.filenet.ns.fnce._2006._11.ws.schema.ErrorStackResponse;
import com.filenet.ns.fnce._2006._11.ws.schema.ErrorStackType;
import com.filenet.ns.fnce._2006._11.ws.schema.ExecuteChangesRequest;
import com.filenet.ns.fnce._2006._11.ws.schema.ExecuteChangesResponse;
import com.filenet.ns.fnce._2006._11.ws.schema.GetObjectsRequest;
import com.filenet.ns.fnce._2006._11.ws.schema.GetObjectsResponse;
import com.filenet.ns.fnce._2006._11.ws.schema.InlineContent;
import com.filenet.ns.fnce._2006._11.ws.schema.ListOfObject;
import com.filenet.ns.fnce._2006._11.ws.schema.Localization;
import com.filenet.ns.fnce._2006._11.ws.schema.ModifiedPropertiesType;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectEntryType;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectFactory;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectReference;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectRequestType;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectResponseType;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectSpecification;
import com.filenet.ns.fnce._2006._11.ws.schema.ObjectValue;
import com.filenet.ns.fnce._2006._11.ws.schema.PropertyFilterType;
import com.filenet.ns.fnce._2006._11.ws.schema.PropertyType;
import com.filenet.ns.fnce._2006._11.ws.schema.ReservationType;
import com.filenet.ns.fnce._2006._11.ws.schema.SingleObjectResponse;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonId;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonObject;
import com.filenet.ns.fnce._2006._11.ws.schema.SingletonString;
import com.filenet.ns.fnce._2006._11.ws.schema.UpdateAction;

import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.CEDocumentContent;
import lu.mtn.ibm.filenet.client.dto.DocumentCreationRequest;
import lu.mtn.ibm.filenet.client.dto.DocumentModificationRequest;
import lu.mtn.ibm.filenet.client.exception.ServiceCallException;

/**
 * @author nguyent
 *
 */
public class DocumentDelegateService extends AbstractDelegateService {

    /**
     *
     */
    private static final String OBJECT_STORE = "ObjectStore";
    private static final String CLASS_CONTENT_TRANSFER = "ContentTransfer";

    /**
     * @param port
     * @param defaultLocale
     * @param factory
     */
    public DocumentDelegateService(FNCEWS40PortType port, Localization defaultLocale, ObjectFactory factory) {
        super(port, defaultLocale, factory);
    }

    /**
     * @param id
     * @param objectStore
     * @param additionalProps
     * @param getContent
     * @return
     * @throws ServiceCallException
     * @throws FaultResponse
     */
    public CEDocument getDocument(String id, String objectStore, String[] additionalProps, boolean getContent) throws ServiceCallException, FaultResponse {

        GetObjectsRequest request = this.factory.createGetObjectsRequest();

        // Set a reference to the document to retrieve
        ObjectSpecification objectSpecification = createObjectSpecification(CLASS_DOCUMENT, id, objectStore);

        // Create property filter object and set its attributes
        PropertyFilterType objPropFilter = this.factory.createPropertyFilterType();
        objPropFilter.setMaxRecursion(3);

        // Create filter element for ContentElements property
        objPropFilter.getIncludeProperties().add(createFilterElement(PROP_DOC_TITLE));
        objPropFilter.getIncludeProperties().add(createFilterElement(PROP_ID));
        if (getContent) {
            objPropFilter.getIncludeProperties().add(createFilterElement(PROP_CONTENT_ELEMENTS));
            objPropFilter.getIncludeProperties().add(createFilterElement(PROP_CONTENT));
            objPropFilter.getIncludeProperties().add(createFilterElement(PROP_RETRIEVAL_NAME));
            objPropFilter.getIncludeProperties().add(createFilterElement(PROP_CONTENT_TYPE));
        }
        if (additionalProps != null) {
            // Create filter element array to hold IncludeProperties specifications
            for (String name : additionalProps) {
                objPropFilter.getIncludeProperties().add(createFilterElement(name));
            }
        }

        // Create an object request for a GetObjects operation
        ObjectRequestType objObjectRequestType = this.factory.createObjectRequestType();
        objObjectRequestType.setSourceSpecification(objectSpecification);
        objObjectRequestType.setPropertyFilter(objPropFilter);

        request.getObjectRequest().add(objObjectRequestType);

        GetObjectsResponse resp = port.getObjects(request, defaultLocale);

        // Process response
        List<ObjectResponseType> objContentRespTypes = resp.getObjectResponse();
        ObjectResponseType objContentRespType = objContentRespTypes.get(0);
        if (objContentRespType instanceof SingleObjectResponse) {
            return processGetDocumentResponse(objectSpecification, objContentRespType);

        } else if (objContentRespType instanceof ErrorStackResponse) {
            ErrorStackResponse objContentErrorResp = (ErrorStackResponse) objContentRespType;
            ErrorStackType objErrorStackType = objContentErrorResp.getErrorStack();
            ErrorRecordType objErrorRecordType = objErrorStackType.getErrorRecord().get(0);
            throw new ServiceCallException("Error [" + objErrorRecordType.getDescription() + "] occurred. " + " Err source instanceof [" + objErrorRecordType.getSource() + "]");
        } else {
            throw new IllegalStateException("Unknown data type returned in content response: [" + objContentRespType.getClass() + "]");
        }
    }

    /**
     * @param request
     * @param objecStore
     * @return
     * @throws FaultResponse
     * @throws ServiceCallException
     */
    public String createDocument(DocumentCreationRequest request, String objecStore) throws FaultResponse, ServiceCallException {
        ExecuteChangesRequest executeChangesRequest = createCreateDocumentRequest(request, objecStore);

        // Create ChangeResponseType element array
        // Call ExecuteChanges operation to implement the doc creation and checkin
        ExecuteChangesResponse response = this.port.executeChanges(executeChangesRequest, defaultLocale);
        List<ChangeResponseType> changeResponseTypes = response.getChangeResponse();

        // The new document object should be returned, unless there instanceof an error
        if (changeResponseTypes == null || changeResponseTypes.isEmpty()) {
            throw new ServiceCallException("A valid object was not returned from the createDocument operation");
        }

        ChangeResponseType changeResponseType = changeResponseTypes.get(0);
        for (PropertyType prpProperty : changeResponseType.getProperty()) {
            if (PROP_ID.equals(prpProperty.getPropertyId())) {
                String id = ((SingletonId) prpProperty).getValue();

                if (request.getFolder() != null) {
                    this.linkDocumentToFolder(id, request.getDocumentName(), request.getFolder(), objecStore);
                }
                return id;
            }
        }
        throw new IllegalStateException("The id property could not be found.");
    }

    /**
     * @param documentId
     * @param documentName
     * @param folderPath
     * @param objectStore
     * @throws FaultResponse
     * @throws ServiceCallException
     */
    public void linkDocumentToFolder(String documentId, String documentName, String folderPath, String objectStore) throws FaultResponse, ServiceCallException {

        // Build the Create action for a DynamicReferentialContainmentRelationship object
        CreateAction objCreate = factory.createCreateAction();
        objCreate.setClassId("DynamicReferentialContainmentRelationship");
        objCreate.setAutoUniqueContainmentName(Boolean.TRUE);

        // Assign the action to the ChangeRequestType element
        ChangeRequestType changeRequestType = factory.createChangeRequestType();
        changeRequestType.getAction().add(objCreate);

        // Specify the target object (an object store) for the actions
        changeRequestType.setTargetSpecification(createObjectReference(OBJECT_STORE, objectStore, null));
        changeRequestType.setId("1");

        ModifiedPropertiesType properties = factory.createModifiedPropertiesType();
        changeRequestType.setActionProperties(properties);

        // Specify and set a string-valued property for the ContainmentName property
        SingletonString prpContainmentName = createSingletonString("ContainmentName", documentName);
        properties.getProperty().add(prpContainmentName);

        // Create an object reference to the document to file
        ObjectReference objDocument = createObjectReference(CEDocument.CLASS_DOCUMENT, documentId, objectStore);

        // Create an object reference to the folder in which to file the document
        ObjectSpecification objFolder = createObjectSpecification("Folder", folderPath, objectStore);

        // Specify and set an object-valued property for the Head property
        SingletonObject prpHead = createSingletonObject("Head", (ObjectEntryType) objDocument); // Set its value to the Document object
        properties.getProperty().add(prpHead);

        // Specify and set an object-valued property for the Tail property
        SingletonObject prpTail = createSingletonObject("Tail", (ObjectEntryType) objFolder); // Set its value to the Document object
        properties.getProperty().add(prpTail);

        // Build a list of properties to exclude on the new DRCR object that will be returned
        // changeRequestType.setRefreshFilter(factory.createPropertyFilterType());
        // changeRequestType.getRefreshFilter().getExcludeProperties().add("Owner");
        // changeRequestType.getRefreshFilter().getExcludeProperties().add("DateLastModified");

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest executeChangesRequest = factory.createExecuteChangesRequest();
        executeChangesRequest.getChangeRequest().add(changeRequestType);
        executeChangesRequest.setRefresh(false); // return a refreshed object

        ExecuteChangesResponse response = this.port.executeChanges(executeChangesRequest, defaultLocale);
        List<ChangeResponseType> changeResponseTypes = response.getChangeResponse();

        // The new document object should be returned, unless there instanceof an error
        if (changeResponseTypes == null || changeResponseTypes.isEmpty()) {
            throw new ServiceCallException("A valid object was not returned from the linkDocumentToFolder operation");
        }
    }

    /**
     * @param request
     * @param objectStore
     * @return
     * @throws FaultResponse
     * @throws ServiceCallException
     */
    public String updateDocument(DocumentModificationRequest request, String objectStore) throws FaultResponse, ServiceCallException {

        if (request.getDocumentName() != null) {
            request.getDocProps().put(PROP_DOC_TITLE, request.getDocumentName());
        }

        if (request.getContents() == null || request.getContents().isEmpty()) {
            updateDocumentProperties(request, objectStore);
            return request.getDocumentId();

        } else {

            ExecuteChangesRequest executeChangesRequest = createCheckoutRequest(request.getDocumentId(), request.getDocProps(), objectStore);

            ExecuteChangesResponse response = port.executeChanges(executeChangesRequest, defaultLocale);
            List<ChangeResponseType> changeResponseTypes = response.getChangeResponse();

            // The new document object should be returned, unless there is an error
            if (changeResponseTypes == null || changeResponseTypes.isEmpty())
            {
                throw new ServiceCallException("A valid object was not returned from the checkout operation");
            }

            // Get Reservation object from Reservation property
            SingletonObject reservationProp = (SingletonObject) changeResponseTypes.get(0).getProperty().get(0);
            ObjectValue reservation = (ObjectValue) reservationProp.getValue();

            // Execute a checkin
            executeChangesRequest = createCheckinRequest(request.getDocProps(), request.getContents(), reservation, objectStore);

            response = port.executeChanges(executeChangesRequest, defaultLocale);
            changeResponseTypes = response.getChangeResponse();

            // The new document object should be returned, unless there is an error
            if (changeResponseTypes == null || changeResponseTypes.isEmpty())
            {
                throw new ServiceCallException("A valid object was not returned from the checkin operation");
            }

            ChangeResponseType changeResponseType = changeResponseTypes.get(0);
            for (PropertyType prpProperty : changeResponseType.getProperty()) {
                if (PROP_ID.equals(prpProperty.getPropertyId())) {
                    return ((SingletonId) prpProperty).getValue();
                }
            }
            throw new IllegalStateException("The id property could not be found.");
        }
    }

    public void deleteDocument(String id, String objectStore) throws FaultResponse, ServiceCallException {

        ExecuteChangesRequest request = createDeleteRequest(id, objectStore);

        // Create ChangeResponseType element array
        // Call ExecuteChanges operation to implement the doc creation and checkin
        ExecuteChangesResponse response = this.port.executeChanges(request, defaultLocale);
        List<ChangeResponseType> changeResponseTypes = response.getChangeResponse();

        // The new document object should be returned, unless there is an error
        if (changeResponseTypes == null || changeResponseTypes.isEmpty())
        {
            throw new ServiceCallException("A valid object was not returned from the delete operation");
        }
    }


    protected ExecuteChangesRequest createCheckinRequest(Map<String, Object> docProperties, List<CEDocumentContent> docContents, ObjectValue reservation, String objectStore)
    {
        // Build the Checkin action
        CheckinAction checkin = factory.createCheckinAction();
        checkin.setCheckinMinorVersion(false);
        checkin.setAutoClassify(false);

        // Assign the actions to the ChangeRequestType element
        ChangeRequestType changeRequestType = new ChangeRequestType();
        changeRequestType.getAction().add(checkin);

        // Specify the target object (Reservation object) for the actions
        changeRequestType.setTargetSpecification(createObjectReference(CLASS_DOCUMENT, reservation.getObjectId(), objectStore));
        changeRequestType.setId("1");

        ModifiedPropertiesType properties = factory.createModifiedPropertiesType();
        changeRequestType.setActionProperties(properties);

        this.addDocumentContents(docContents, properties);

        for (Entry<String, Object> pair : docProperties.entrySet()) {
            properties.getProperty().add(this.convertToWebServiceProperty(pair.getKey(), pair.getValue()));
        }

        // Assign the list of excluded properties to the ChangeRequestType element
        changeRequestType.setRefreshFilter(factory.createPropertyFilterType());

        // Create a filter to only get the new Id as property in the response. No other property will be refreshed.
        changeRequestType.getRefreshFilter().getIncludeProperties().add(createFilterElement(PROP_ID));


        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest executeChangesRequest = this.factory.createExecuteChangesRequest();
        executeChangesRequest.getChangeRequest().add(changeRequestType);
        executeChangesRequest.setRefresh(true); // return a refreshed object

        return executeChangesRequest;
    }

    protected ExecuteChangesRequest createCheckoutRequest(String documentId, Map<String, Object> docProperties, String objectStore) {
        // Build the Checkout action
        CheckoutAction checkout = factory.createCheckoutAction();
        checkout.setReservationType(ReservationType.EXCLUSIVE);

        // Assign the actions to the ChangeRequestType element
        ChangeRequestType changeRequestType = new ChangeRequestType();
        changeRequestType.getAction().add(checkout);

        // Specify the target object for the actions
        changeRequestType.setTargetSpecification(createObjectReference(CLASS_DOCUMENT, documentId, objectStore));
        changeRequestType.setId("1");

        // Create a Property Filter to get Reservation property
        PropertyFilterType propFilter = new PropertyFilterType();
        propFilter.setMaxRecursion(1);
        propFilter.getIncludeProperties().add(createFilterElement("Reservation"));
        propFilter.getIncludeProperties().add(createFilterElement(PROP_DOC_TITLE));

        if (docProperties != null) {
            for (Entry<String, Object> pair : docProperties.entrySet()) {
                propFilter.getIncludeProperties().add(createFilterElement(pair.getKey()));
            }
        }

        // Assign the list of included properties to the ChangeRequestType element
        changeRequestType.setRefreshFilter(propFilter);

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest executeChangesRequest = this.factory.createExecuteChangesRequest();
        executeChangesRequest.getChangeRequest().add(changeRequestType);
        executeChangesRequest.setRefresh(true); // return a refreshed object

        return executeChangesRequest;
    }

    protected void updateDocumentProperties(DocumentModificationRequest request, String objectStore) throws FaultResponse, ServiceCallException {

        if (!request.getDocProps().isEmpty()) {
            ExecuteChangesRequest executeChangesRequest = createUpdateRequest(request.getDocumentId(), request.getDocProps(), objectStore);

            ExecuteChangesResponse response = port.executeChanges(executeChangesRequest, defaultLocale);
            List<ChangeResponseType> changeResponseTypes = response.getChangeResponse();

            // The new document object should be returned, unless there is an error
            if (changeResponseTypes == null || changeResponseTypes.isEmpty()) {
                throw new ServiceCallException("A valid object was not returned from the updateDocumentProperties operation");
            }
        }
    }

    protected ExecuteChangesRequest createUpdateRequest(String documentId, Map<String, Object> docProperties, String objectStore) {
        // Build the update action
        UpdateAction update = factory.createUpdateAction();

        // Assign the actions to the ChangeRequestType element
        ChangeRequestType changeRequestType = new ChangeRequestType();
        changeRequestType.getAction().add(update);

        // Specify the target object (Reservation object) for the actions
        changeRequestType.setTargetSpecification(createObjectReference(CLASS_DOCUMENT, documentId, objectStore));
        changeRequestType.setId("1");

        // Build a list of properties to set in the new doc
        ModifiedPropertiesType inputProps = factory.createModifiedPropertiesType();

        for (Entry<String, Object> pair : docProperties.entrySet()) {
            inputProps.getProperty().add(this.convertToWebServiceProperty(pair.getKey(), pair.getValue()));
        }

        // Assign list of document properties to set in ChangeRequestType element
        changeRequestType.setActionProperties(inputProps);

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest executeChangesRequest = this.factory.createExecuteChangesRequest();
        executeChangesRequest.getChangeRequest().add(changeRequestType);
        executeChangesRequest.setRefresh(false); // return a refreshed object

        return executeChangesRequest;
    }

    protected ExecuteChangesRequest createCreateDocumentRequest(DocumentCreationRequest request, String objectStore) {
        // Build the Create action for a document
        CreateAction createAction = this.factory.createCreateAction();
        createAction.setClassId(request.getDocumentClassName());

        // Build the Checkin action
        CheckinAction checkInAction = this.factory.createCheckinAction();
        checkInAction.setCheckinMinorVersion(false);
        checkInAction.setAutoClassify(false);

        // Assign the actions to the ChangeRequestType element
        ChangeRequestType changeRequestType = this.factory.createChangeRequestType();
        changeRequestType.getAction().add(createAction); // Assign Create action
        changeRequestType.getAction().add(checkInAction); // Assign Checkin action

        // Specify the target object (an object store) for the actions
        changeRequestType.setTargetSpecification(createObjectReference(OBJECT_STORE, objectStore, null));
        changeRequestType.setId("1");

        ModifiedPropertiesType properties = this.factory.createModifiedPropertiesType();
        changeRequestType.setActionProperties(properties);

        addDocumentContents(request.getContents(), properties);

        // Add all properties
        for (Entry<String, Object> pair : request.getDocProps().entrySet()) {
            properties.getProperty().add(this.convertToWebServiceProperty(pair.getKey(), pair.getValue()));
        }

        // Assign the name of the document.
        properties.getProperty().add(createSingletonString(CEDocument.PROP_DOC_TITLE, request.getDocumentName()));

        // Create a filter to only get the new Id as property in the response. No other property will be refreshed.
        changeRequestType.setRefreshFilter(this.factory.createPropertyFilterType());
        changeRequestType.getRefreshFilter().getIncludeProperties().add(createFilterElement(PROP_ID));

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest executeChangesRequest = this.factory.createExecuteChangesRequest();
        executeChangesRequest.getChangeRequest().add(changeRequestType);
        executeChangesRequest.setRefresh(true); // return a refreshed object

        return executeChangesRequest;
    }

    protected ExecuteChangesRequest createDeleteRequest(String id, String objectStore)
    {
        // Build the Delete action for a document
        DeleteAction deleteAction = new DeleteAction();

        // Assign the actions to the ChangeRequestType element
        ChangeRequestType changeRequestType = new ChangeRequestType();
        changeRequestType.getAction().add(deleteAction);

        // Specify the target object for the actions
        changeRequestType.setTargetSpecification(createObjectReference(CLASS_DOCUMENT, id, objectStore));

        // Create array of ChangeRequestType elements and assign ChangeRequestType element to it
        ChangeRequestType[] changeRequestTypeArray = new ChangeRequestType[1];
        changeRequestTypeArray[0] = changeRequestType;

        // Build ExecuteChangesRequest element and assign ChangeRequestType element array to it
        ExecuteChangesRequest executeChangesRequest = this.factory.createExecuteChangesRequest();
        executeChangesRequest.getChangeRequest().add(changeRequestType);
        executeChangesRequest.setRefresh(true);

        return executeChangesRequest;
    }


    protected void addDocumentContents(List<CEDocumentContent> docContents, ModifiedPropertiesType properties) {
        // Create reference to the object set of ContentTransfer objects returned by the Document.ContentElements property
        ListOfObject prpContentElement = this.factory.createListOfObject();
        prpContentElement.setPropertyId(PROP_CONTENT_ELEMENTS);

        for (CEDocumentContent docContent : docContents) {

            // Read data stream from file containing the document content
            InlineContent inlineContent = this.factory.createInlineContent();
            inlineContent.setBinary(docContent.getContent());

            // Create reference to Content pseudo-property
            ContentData contentData = this.factory.createContentData();
            contentData.setValue(inlineContent);
            contentData.setPropertyId(PROP_CONTENT);

            // Create an object reference to dependently persistable ContentTransfer object
            DependentObjectType contentTransfer = this.factory.createDependentObjectType();
            contentTransfer.setClassId(CLASS_CONTENT_TRANSFER);
            contentTransfer.setDependentAction("Insert");

            // Assign Content property to ContentTransfer object
            contentTransfer.getProperty().add(contentData);

            // Set the retrival name so FileNet could deduce the mime-type.
            contentTransfer.getProperty().add(convertToWebServiceProperty(PROP_RETRIEVAL_NAME, docContent.getName()));
            if (docContent.getMimeType() != null) {
                contentTransfer.getProperty().add(convertToWebServiceProperty(PROP_CONTENT_TYPE, docContent.getMimeType()));
            }
            prpContentElement.getValue().add(contentTransfer);
        }
        properties.getProperty().add(prpContentElement);
    }

    protected CEDocument processGetDocumentResponse(ObjectSpecification objectSpecification, ObjectResponseType objContentRespType) throws FaultResponse, ServiceCallException {
        SingleObjectResponse objContentElemResp = (SingleObjectResponse) objContentRespType;
        ObjectValue objectValue = objContentElemResp.getObject();

        // Process document's properties.
        CEDocument document = new CEDocument();
        for (PropertyType prpProperty : objectValue.getProperty()) {
            Object[] val = convertFromWebServiceProperty(prpProperty);
            document.getProperties().put((String) val[0], val[1]);
        }

        if (document.getProperties().containsKey(PROP_CONTENT_ELEMENTS)) {
            this.processDocumentContents(objectSpecification, document);
        }
        return document;
    }

    /**
     * @param objectSpecification
     * @param document
     * @throws FaultResponse
     * @throws ServiceCallException
     */
    protected void processDocumentContents(ObjectSpecification objectSpecification, CEDocument document) throws FaultResponse, ServiceCallException {
        // Get the ContentElements property of the Document object
        ListOfObject contentElements = (ListOfObject) document.getProperties().get(PROP_CONTENT_ELEMENTS);
        document.getProperties().put(PROP_CONTENT_ELEMENTS, null);

        // The document has at least one content.
        if (contentElements.getValue() != null && !contentElements.getValue().isEmpty()) {

            List<CEDocumentContent> contents = new ArrayList<CEDocumentContent>();
            document.setContents(contents);

            for (DependentObjectType contentTransfer : contentElements.getValue()) {
                CEDocumentContent docContent = new CEDocumentContent();
                contents.add(docContent);

                for (PropertyType prpProperty : contentTransfer.getProperty()) {
                    Object[] val = convertFromWebServiceProperty(prpProperty);

                    String propName = (String) val[0];
                    if (PROP_CONTENT.equals(propName)) {
                        ContentData contentData = (ContentData) val[1];
                        InlineContent inlineContent = (InlineContent) contentData.getValue();
                        docContent.setContent(inlineContent.getBinary());
                    } else if (PROP_RETRIEVAL_NAME.equals(propName)) {
                        docContent.setName((String) val[1]);
                    } else if (PROP_CONTENT_TYPE.equals(propName)) {
                        docContent.setMimeType((String) val[1]);
                    }
                }
            }
        }
    }

    // /**
    // * @param objectSpecification
    // * @param document
    // * @throws FaultResponse
    // * @throws WebServiceCallException
    // */
    // private void processDocumentContents(ObjectSpecification objectSpecification, CEDocument document) throws FaultResponse,
    // WebServiceCallException {
    // // Get the ContentElements property of the Document object
    // ListOfObject contentElements = (ListOfObject) document.getProperties().get(PROP_CONTENT_ELEMENTS);
    // document.getProperties().put(PROP_CONTENT_ELEMENTS, null);
    //
    // // The document has at least one content.
    // if (contentElements.getValue() != null && !contentElements.getValue().isEmpty()) {
    //
    // List<CEDocumentContent> contents = new ArrayList<CEDocumentContent>();
    // document.setContents(contents);
    //
    // for (int i = 0; i < contentElements.getValue().size(); ++i) {
    //
    // fetchContentTransfer(objectSpecification, contents, i);
    // }
    // }
    // }
    //
    // /**
    // * @param objectSpecification
    // * @param contents
    // * @param i
    // * @throws FaultResponse
    // * @throws WebServiceCallException
    // */
    // protected void fetchContentTransfer(ObjectSpecification objectSpecification, List<CEDocumentContent> contents, int i) throws FaultResponse,
    // WebServiceCallException {
    // ByteArrayOutputStream contentStream = null;
    // String continueFrom = null;
    //
    // CEDocumentContent content = new CEDocumentContent();
    // contents.add(content);
    //
    // while (contentStream == null || continueFrom != null) {
    //
    // // Construct element specification for GetContent request
    // ElementSpecificationType objElemSpecType = this.factory.createElementSpecificationType();
    // objElemSpecType.setItemIndex(i);
    // objElemSpecType.setElementSequenceNumber(0);
    //
    // // Construct the GetContent request
    // ContentRequestType contentReqType = this.factory.createContentRequestType();
    // contentReqType.setCacheAllowed(true);
    // contentReqType.setId("1");
    // contentReqType.setMaxBytes(Integer.MAX_VALUE);
    // contentReqType.setContinueFrom(continueFrom);
    // contentReqType.setElementSpecification(objElemSpecType);
    // contentReqType.setSourceSpecification(objectSpecification);
    //
    // GetContentRequest getContentReq = this.factory.createGetContentRequest();
    // getContentReq.getContentRequest().add(contentReqType);
    // getContentReq.setValidateOnly(false);
    //
    // // Call web service to get the content.
    // GetContentResponse resp = port.getContent(getContentReq, defaultLocale);
    //
    // // Process GetContent response
    // ContentResponseType objContentRespType = resp.getContentResponse().get(0);
    // if (objContentRespType instanceof ContentElementResponse) {
    // ContentElementResponse objContentElemResp = (ContentElementResponse) objContentRespType;
    //
    // if (content.getName() == null) {
    // content.setName(objContentElemResp.getRetrievalName());
    // }
    // InlineContent objInlineContent = (InlineContent) objContentElemResp.getContent();
    // continueFrom = objContentElemResp.getContinueFrom();
    // byte[] partialContent = objInlineContent.getBinary();
    // if (contentStream == null) {
    // contentStream = new ByteArrayOutputStream();
    // }
    // contentStream.write(partialContent, 0, partialContent.length);
    // } else if (objContentRespType instanceof ContentErrorResponse) {
    // ContentErrorResponse objContentErrorResp = (ContentErrorResponse) objContentRespType;
    // ErrorStackType objErrorStackType = objContentErrorResp.getErrorStack();
    // ErrorRecordType objErrorRecordType = objErrorStackType.getErrorRecord().get(0);
    // throw new WebServiceCallException("Error [" + objErrorRecordType.getDescription() + "] occurred. " + " Err source instanceof [" +
    // objErrorRecordType.getSource() + "]");
    // } else {
    // throw new IllegalStateException("Unknown data type returned in content response: [" + objContentRespType.getClass() + "]");
    // }
    // }
    // content.setContent(contentStream.toByteArray());
    // }

}
