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

    � Copyright IBM Corporation 2007, ALL RIGHTS RESERVED.
 */

package lu.mtn.ibm.filenet.client.ri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;


import com.filenet.api.collection.AccessPermissionList;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.ComponentRelationshipType;
import com.filenet.api.constants.CompoundDocumentState;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.VersionBindType;
import com.filenet.api.core.ComponentRelationship;
import com.filenet.api.core.Containable;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.property.FilterElement;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.property.PropertyBinary;
import com.filenet.api.property.PropertyBinaryList;
import com.filenet.api.property.PropertyBoolean;
import com.filenet.api.property.PropertyBooleanList;
import com.filenet.api.property.PropertyDateTime;
import com.filenet.api.property.PropertyDateTimeList;
import com.filenet.api.property.PropertyEngineObject;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.property.PropertyId;
import com.filenet.api.property.PropertyIdList;
import com.filenet.api.property.PropertyInteger32;
import com.filenet.api.property.PropertyInteger32List;
import com.filenet.api.property.PropertyString;
import com.filenet.api.property.PropertyStringList;
import com.filenet.api.query.RepositoryRow;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.security.AccessPermission;
import com.filenet.api.util.Id;

import lu.mtn.ibm.filenet.client.dto.CEDocument;
import lu.mtn.ibm.filenet.client.dto.CEDocumentContent;

/**
 * Provides the static methods for making API calls to Content Engine.
 */
public class FileNetCEApiUtil {

    /**
     * @param os
     * @param classId
     * @param docProperties
     * @param is
     * @return
     */
    public static Document createDocument(ObjectStore os, String classId, Map<String, Object> docProperties, List<ContentTransfer> contents) {
        Document doc = Factory.Document.createInstance(os, classId);
        checkMandatoryProperties(docProperties);
        for (Entry<String, Object> entry : docProperties.entrySet()) {
            doc.getProperties().putObjectValue(entry.getKey(), entry.getValue());
        }

        doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);

        ContentElementList cel = createContentElementList(contents);
        doc.set_ContentElements(cel);

        return doc;
    }

    public static ContentTransfer createContentTransfer(String name, InputStream input, String mimeType) {
        ContentTransfer ctNew = Factory.ContentTransfer.createInstance();
        ctNew.set_RetrievalName(name);
        ctNew.setCaptureSource(input);
        if (mimeType != null) {
            ctNew.set_ContentType(mimeType);
        }
        return ctNew;
    }

    /**
     * @param is
     * @param docName
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ContentElementList createContentElementList(List<ContentTransfer> contents) {
        if (contents != null && !contents.isEmpty()) {
            ContentElementList cel = Factory.ContentElement.createList();
            cel.addAll(contents);
            return cel;
        }
        return null;
    }

    /**
     * @param docProperties
     */
    private static void checkMandatoryProperties(Map<String, Object> docProperties) {
//        if (docProperties.isEmpty()) {
//            throw new IllegalArgumentException("The document properties can't be null.");
//        }
//        String value = (String) docProperties.get(PROP_DOC_TITLE);
//        if (value == null) {
//            throw new IllegalArgumentException("No value for \"DOCUMENT_TITLE\" could be found in the document properties.");
//        }
    }

    /**
     * @param f
     * @param folder
     * @param doc
     */
    public static ReferentialContainmentRelationship createReferentialContainmentRelationship(Folder folder, Containable o) {
        ReferentialContainmentRelationship rcr = folder.file((Document) o, AutoUniqueName.AUTO_UNIQUE, o.get_Name() != null && o.get_Name().length() > 0 ? o.get_Name() : o.get_Id().toString(), DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
        return rcr;
    }

    /**
     * @param f
     * @param folder
     * @param doc
     */
    public static ReferentialContainmentRelationship createReferentialContainmentRelationship(Folder folder, Containable o, String docName) {
        ReferentialContainmentRelationship rcr = folder.file((Document) o, AutoUniqueName.AUTO_UNIQUE, docName, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
        return rcr;
    }
    
    /**
     * @param f
     * @param folder
     * @param doc
     */
    public static ReferentialContainmentRelationship createReferentialContainmentRelationship(IndependentObject head, IndependentObject tail, String docName, ObjectStore os) {
        ReferentialContainmentRelationship rcr = Factory.DynamicReferentialContainmentRelationship.createInstance(os, null);
        rcr.set_Head(head);
        rcr.set_Tail(tail);
        rcr.set_ContainmentName(docName);
        return rcr;
    }

    /*
     * Creates the file using bytes read from Document's content stream.
     */
    public static void writeDocContentToFile(Document doc, String path, boolean useDocName) {
        String fileName = doc.get_Name();
        if (!useDocName) {
            ContentTransfer ct = (ContentTransfer) doc.get_ContentElements().get(0);
            fileName = ct.get_RetrievalName();
        }
        writeDocContentToFile(doc, path, fileName);
    }

    /*
     * Creates the file using bytes read from Document's content stream.
     */
    public static void writeDocContentToFile(Document doc, String path, String docName) {

        File f = new File(path, docName);
        writeDocContentToFile(doc, f);
    }

    /**
     * @param doc
     * @param f
     */
    private static void writeDocContentToFile(Document doc, File f) {
        InputStream is = doc.accessContentStream(0);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(f);
            IOUtils.copy(is, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Retrives the Document object specified by path.
     */
    public static Document fetchDocByPath(ObjectStore os, String path) {
        Document doc = Factory.Document.fetchInstance(os, path, null);
        return doc;
    }

    /*
     * Retrives the Document object specified by id.
     */
    public static Document fetchDocById(ObjectStore os, String id) {
        Document doc = Factory.Document.fetchInstance(os, new Id(id), null);
        return doc;
    }

    /*
     * Checks in the Document object.
     */
    public static void checkinDoc(Document doc) {
        doc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MINOR_VERSION);
        doc.save(RefreshMode.REFRESH);
        doc.refresh();
    }

    /*
     * Creates the CustomObject.
     */
    public static CustomObject createCustomObject(ObjectStore os, String className) {
        CustomObject co = Factory.CustomObject.createInstance(os, className == null || className.trim().isEmpty() ? null : className);
        return co;
    }

    /*
     * Retrives the CustomObject object specified by path.
     */
    public static CustomObject fetchCustomObjectByPath(ObjectStore os, String path) {
        CustomObject doc = Factory.CustomObject.fetchInstance(os, path, null);
        return doc;
    }

    /*
     * Retrives the CustomObject object specified by id.
     */
    public static CustomObject fetchCustomObjectById(ObjectStore os, String id) {
        Id id1 = new Id(id);
        CustomObject doc = Factory.CustomObject.fetchInstance(os, id1, null);
        return doc;
    }

    /*
     * Files the Containable object (i.e. Document, CustomObject) in specified
     * folder.
     */
    public static ReferentialContainmentRelationship fileObject(ObjectStore os, Containable o, String folderPath) {
        Folder fo = Factory.Folder.fetchInstance(os, folderPath, null);
        ReferentialContainmentRelationship rcr = createReferentialContainmentRelationship(fo, o);
        return rcr;
    }

    // /*
    // * Retrives some of the properties of Containable object (i.e. Document,
    // * CustomObject) and stores them in a HashMap, with property name as key
    // and
    // * property value as value.
    // */
    // public static Map<String, String>
    // getContainableObjectProperties(Containable o) {
    // Map<String, String> hm = new HashMap<String, String>();
    // hm.put(PROP_DOC_ID, o.get_Id().toString());
    // hm.put(PROP_DOC_TITLE, o.get_Name());
    // hm.put(CREATOR, o.get_Creator());
    // hm.put(OWNER, o.get_Owner());
    // hm.put(DATE_CREATED, o.get_DateCreated().toString());
    // hm.put(DATE_LAST_MODIFIED, o.get_DateLastModified().toString());
    // return hm;
    // }

    /*
     * Creates the Folder object at specified path using specified name.
     */
    public static void createFolder(ObjectStore os, String fPath, String fName, String className) {
        Folder f = Factory.Folder.fetchInstance(os, fPath, null);
        Folder nf = null;
        if (className.equals(""))
            nf = Factory.Folder.createInstance(os, null);
        else
            nf = Factory.Folder.createInstance(os, className);
        nf.set_Parent(f);
        nf.set_FolderName(fName);
        nf.save(RefreshMode.REFRESH);
    }

    /*
     * Creates the CompoundDocument (i.e. ComponentRelationship object).
     */
    public static ComponentRelationship createComponentRelationship(ObjectStore os, String pTitle, String cTitle) {
        ComponentRelationship cr = null;
        Document parentDoc = null;
        Document childDoc = null;

        parentDoc = Factory.Document.createInstance(os, null);
        parentDoc.getProperties().putValue(CEDocument.PROP_DOC_TITLE, pTitle);
        parentDoc.set_CompoundDocumentState(CompoundDocumentState.COMPOUND_DOCUMENT);
        parentDoc.save(RefreshMode.REFRESH);
        parentDoc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MINOR_VERSION);
        parentDoc.save(RefreshMode.REFRESH);

        childDoc = Factory.Document.createInstance(os, null);
        childDoc.getProperties().putValue(CEDocument.PROP_DOC_TITLE, cTitle);
        childDoc.set_CompoundDocumentState(CompoundDocumentState.COMPOUND_DOCUMENT);
        childDoc.save(RefreshMode.REFRESH);
        childDoc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MINOR_VERSION);
        childDoc.save(RefreshMode.REFRESH);

        cr = Factory.ComponentRelationship.createInstance(os, null);
        cr.set_ParentComponent(parentDoc);
        cr.set_ChildComponent(childDoc);
        cr.set_ComponentRelationshipType(ComponentRelationshipType.DYNAMIC_CR);
        cr.set_VersionBindType(VersionBindType.LATEST_VERSION);

        return cr;
    }

    /*
     * Retrives the CompoundDocument object using supplied ID.
     */
    public static ComponentRelationship fetchComponentRelationship(ObjectStore os, String id) {
        Id id1 = new Id(id);
        ComponentRelationship cr = Factory.ComponentRelationship.fetchInstance(os, id1, null);
        return cr;
    }

    // /*
    // * Retrives the some of the properties of CompoundDocument (i.e.
    // * ComponentRelationship object) and stores them in HashMap, property name
    // * as key and property value as value.
    // */
    // public static Map<String, String>
    // getComponentRelationshipObjectProperties(ComponentRelationship o) {
    // Map<String, String> hm = new HashMap<String, String>();
    // hm.put(ID, o.get_Id().toString());
    // hm.put(CREATOR, o.get_Creator());
    // hm.put(DATE_CREATED, o.get_DateCreated().toString());
    // hm.put(DATE_LAST_MODIFIED, o.get_DateLastModified().toString());
    // hm.put("Child Component", o.get_ChildComponent().get_Name());
    // hm.put("Parent Component", o.get_ParentComponent().get_Name());
    // return hm;
    // }

    /*
     * Retrives the RepositoryRowSet (result of querying Content Engine). Query
     * is constructed from supplied select, from, and where clause using
     * SearchSQL object. Then it creates the SearchScope object using supplied
     * ObjectStore, and queries the Content Engine using fetchRows method of
     * SearchScope object.
     */
    public static RepositoryRowSet fetchResultsRowSet(ObjectStore os, String select, String from, String where, int rows) {
        RepositoryRowSet rrs = null;
        SearchSQL q = new SearchSQL();
        SearchScope ss = new SearchScope(os);
        q.setSelectList(select);
        q.setFromClauseInitialValue(from, null, false);
        if (!where.isEmpty()) {
            q.setWhereClause(where);
        }
        if (rows != 0) {
            q.setMaxRecords(rows);
        }
        rrs = ss.fetchRows(q, null, null, null);
        return rrs;
    }

    /*
     * Gets column names to display in JTable. It takes RepositoryRow as
     * argument
     */
    @SuppressWarnings("unchecked")
    public static List<String> getResultProperties(RepositoryRow rr) {
        List<String> column = new ArrayList<String>();
        Properties ps = rr.getProperties();
        Iterator<Property> it = ps.iterator();

        while (it.hasNext()) {
            Property pt = it.next();
            String name = pt.getPropertyName();
            column.add(name);
        }
        return column;
    }

    /*
     * Retrives the properties from supplied RepositoryRow, stores them in
     * vector, and returns it.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getResultRow(RepositoryRow rr) {
        List<String> row = new ArrayList<String>();
        Properties ps = rr.getProperties();
        Iterator<Property> it = ps.iterator();

        while (it.hasNext()) {
            Property pt = it.next();
            Object value = pt.getObjectValue();
            if (value == null) {
                row.add("null");
            } else if (value instanceof EngineCollection) {
                row.add("*");
            } else {
                row.add(value.toString());
            }
        }
        return row;
    }

    /*
     * Retrives the access permission list for a Containable object, stores it
     * in Vector, and returns it.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getPermissions(Containable co) {
        List<String> permissions = new ArrayList<String>();
        AccessPermissionList apl = co.get_Permissions();
        Iterator<AccessPermission> iter = apl.iterator();
        while (iter.hasNext()) {
            AccessPermission ap = iter.next();
            permissions.add("GRANTEE_NAME: " + ap.get_GranteeName());
            permissions.add("ACCESS_MASK: " + ap.get_AccessMask().toString());
            permissions.add("ACCESS_TYPE: " + ap.get_AccessType().toString());
        }
        return permissions;
    }

    @SuppressWarnings("unchecked")
    public static byte[] getByteArrayContent(Document doc) throws IOException {

        ContentElementList docContentList = doc.get_ContentElements();
        Iterator<ContentTransfer> iter = docContentList.iterator();
        byte[] content = null;
        if (iter.hasNext()) {

            ContentTransfer ct = (ContentTransfer) iter.next();

            int docLen = ct.get_ContentSize().intValue();
            content = new byte[docLen];

            InputStream is = ct.accessContentStream();
            try {
                is.read(content, 0, docLen);

            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content;
    }

    @SuppressWarnings("unchecked")
    public static InputStream getContent(Document doc) throws IOException {

        ContentElementList docContentList = doc.get_ContentElements();
        Iterator<ContentTransfer> iter = docContentList.iterator();
        if (iter.hasNext()) {

            ContentTransfer ct = (ContentTransfer) iter.next();
            return ct.accessContentStream();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<CEDocumentContent> getContents(Document doc) throws IOException {

        List<CEDocumentContent> contents = new ArrayList<CEDocumentContent>(doc.get_ContentElements().size());

        for (Iterator<ContentTransfer> iter = doc.get_ContentElements().iterator(); iter.hasNext(); ) {

            ContentTransfer ct = (ContentTransfer) iter.next();
            InputStream is = ct.accessContentStream();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }

            CEDocumentContent content = new CEDocumentContent();
            content.setContent(os.toByteArray());
            content.setName(ct.get_RetrievalName());
            content.setMimeType(ct.get_ContentType());

            contents.add(content);
        }
        return contents;
    }

    public static PropertyFilter createFilter(String[] properties) {
        if (properties == null)
            return null;
        
        PropertyFilter pf = new PropertyFilter();
        for (int i = 0; i < properties.length; i++) {
            pf.addIncludeProperty(new FilterElement(null, null, null, properties[i], null));
        }
        return pf;

    }

    @SuppressWarnings("unchecked")
    public static Object getPropertyValue(Property property) {

        if (property instanceof PropertyString) {
            return property.getStringValue();
        } else if (property instanceof PropertyInteger32) {
            return property.getInteger32Value();
        } else if (property instanceof PropertyBoolean) {
            return property.getBooleanValue();
        } else if (property instanceof PropertyDateTime) {
            return property.getDateTimeValue();
        } else if (property instanceof PropertyBinary) {
            return property.getStringValue();
        } else if (property instanceof PropertyId) {
            return property.getIdValue();
        } else if (property instanceof PropertyStringList) {
            return property.getStringListValue().toArray(new String[0]);
        } else if (property instanceof PropertyInteger32List) {
            return property.getInteger32ListValue().toArray(new Integer[0]);
        } else if (property instanceof PropertyBooleanList) {
            return property.getBooleanListValue().toArray(new Boolean[0]);
        } else if (property instanceof PropertyDateTimeList) {
            return property.getDateTimeListValue().toArray(new Date[0]);
        } else if (property instanceof PropertyBinaryList) {
            return property.getBinaryListValue().toArray(new Byte[0]);
        } else if (property instanceof PropertyIdList) {
            return property.getIdListValue().toArray(new Id[0]);
        } else if (property instanceof PropertyEngineObject) {
            return property.getEngineObjectValue();
        }
        return property;
    }
}
