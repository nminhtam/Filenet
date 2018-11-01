/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.export.ce;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.filenet.api.admin.Choice;
import com.filenet.api.admin.ChoiceList;
import com.filenet.api.admin.LocalizedString;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.collection.LocalizedStringList;
import com.filenet.api.constants.ChoiceType;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class XMLExportChoiceList extends AbstractXMLExportOperation<ChoiceList> implements ExecutableOperationOnQuery {

    private String id;

    private String name;

    public XMLExportChoiceList(String id, String operation, String writerName) throws OperationInitializationException {
        super(operation, writerName);

        this.id = id;
        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "ChoiceList", id), true, "The choiceList with id " + id + " must exist."));
    }

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public XMLExportChoiceList(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element rootElement, List<Prerequisite> prerequisites) {

        if (rootElement.hasAttribute("id")) {
            id = rootElement.getAttribute("id");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_OBJECT_ID, "ChoiceList", id), true, "The choiceList with id " + id + " must exist."));

        } else {
            name = rootElement.getAttribute("name");
            prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXIST_CHOICE_LIST_NAME, name), true, "The choiceList with name " + name + " must exist."));
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getExportRef()
     */
    @Override
    protected String getExportRef() {
        return "CHOICE_LIST_" + (id != null ? id : name);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#findObject(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected ChoiceList findObject(ExecutionContext context) {
        ObjectStore os = context.getConnection().getObjectStore();

        SearchSQL sql = new SearchSQL(id != null ? String.format(Constants.QUERY_EXPORT_CHOICE_LIST_ID, id) : String.format(Constants.QUERY_EXPORT_CHOICE_LIST_NAME, name));
        SearchScope ss = new SearchScope(os);

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The choiceList " + name + " must exist.");
        }
        return (ChoiceList) it.next();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getId(java.lang.Object)
     */
    @Override
    protected String getId(ChoiceList choiceList) {
        return choiceList.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#writeDetails(java.lang.Object, org.w3c.dom.Document, lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected void writeDetails(ChoiceList choiceList, Document doc, ExecutionContext context) throws OperationExecutionException {

        Element root = doc.getDocumentElement();
        root.setAttribute("name", choiceList.get_DisplayName());
        root.setAttribute("type", String.valueOf(choiceList.get_DataType().getValue()));

        extractChoiceList(choiceList.get_ChoiceValues(), root, doc, context);
        this.write(context, doc);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.export.AbstractXMLExportOperation#getTagFromOperation(java.lang.String)
     */
    protected String getTagFromOperation(String operation) {
        if (CREATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_CREATE_CHOICE_LIST;
        } else if (UPDATE.equalsIgnoreCase(operation)) {
            return Constants.TAG_UPDATE_CHOICE_LIST;
        }
        throw new UnsupportedOperationException(operation + " is not supported for XMLExportChoiceList.");
    }

    @SuppressWarnings("unchecked")
    protected void extractChoiceList(com.filenet.api.collection.ChoiceList choiceList, Element root, Document doc, ExecutionContext context) {
        for (Iterator<Choice> iter = choiceList.iterator(); iter.hasNext(); ) {

            Choice choice = iter.next();
            if (ChoiceType.MIDNODE_INTEGER == choice.get_ChoiceType() || ChoiceType.MIDNODE_STRING == choice.get_ChoiceType()) {

                Element group = createElement(doc, root, "group");
                group.setAttribute("name", choice.get_DisplayName());
                
                this.writeLocalizedNames(doc, context, choice);

                if (ChoiceType.MIDNODE_STRING == choice.get_ChoiceType() && choice.get_ChoiceStringValue() != null) {
                    group.setAttribute("value", choice.get_ChoiceStringValue());
                } else if (choice.get_ChoiceIntegerValue() != null) {
                    group.setAttribute("value", String.valueOf(choice.get_ChoiceIntegerValue()));
                }

                this.extractChoiceList(choice.get_ChoiceValues(), group, doc, context);

            } else {

                Element choiceElement = createElement(doc, root, "choice");
                choiceElement.setAttribute("name", choice.get_DisplayName());
                choiceElement.setAttribute("value", ChoiceType.INTEGER == choice.get_ChoiceType() ? String.valueOf(choice.get_ChoiceIntegerValue()) : choice.get_ChoiceStringValue());
                
                this.writeLocalizedNames(doc, context, choice);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void writeLocalizedNames(Document doc, ExecutionContext context, Choice choice) {
        LocalizedStringList localizedStringList = choice.get_DisplayNames();
        if (localizedStringList != null && !localizedStringList.isEmpty() && context.getI18nWriter() != null) {
            for (Iterator<LocalizedString> it = localizedStringList.iterator(); it.hasNext(); ) {
                LocalizedString loc = it.next();
                context.getI18nWriter().write(new String[] { loc.get_LocaleName(), "choice." + doc.getDocumentElement().getAttribute("name") + "." + choice.get_DisplayName(), loc.get_LocalizedText() });
            }
        }
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Export ChoiceList " + (id != null ? " with id " + id : " with name " + name);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.misc.ce.ExecutableOperationOnQuery#setNewId(java.lang.String)
     */
    @Override
    public void setNewId(String id) {
        this.id = id;
    }
}
