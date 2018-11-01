/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.update.ce;

import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.admin.Choice;
import com.filenet.api.collection.ChoiceList;
import com.filenet.api.collection.EngineCollection;
import com.filenet.api.constants.ChoiceType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.TypeID;
import com.filenet.api.core.Factory;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;

import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.ce.CreateChoiceList;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationExecutionException;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class UpdateChoiceList extends AbstractUpdateOperation {

    private String choiceListName;

    private TypeID type;

    private ChoiceList choiceList;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public UpdateChoiceList(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element root, List<Prerequisite> prerequisites) throws OperationInitializationException {

        this.choiceListName = root.getAttribute("name");
        this.type = TypeID.getInstanceFromInt(Integer.valueOf(root.getAttribute("type")));
        this.choiceList = Factory.Choice.createList();

        buildChoices(root, this.choiceList);

        prerequisites.add(new ExistingObjectPrerequisite(true, String.format(Constants.QUERY_EXPORT_CHOICE_LIST_NAME, choiceListName), true, "The choiceList " + choiceListName + " must exist."));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.update.AbstractUpdateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected String executeInternal(ExecutionContext context) throws OperationExecutionException {

        SearchSQL sql = new SearchSQL(String.format(Constants.QUERY_EXPORT_CHOICE_LIST_NAME, choiceListName));
        SearchScope ss = new SearchScope(context.getConnection().getObjectStore());

        EngineCollection ec = ss.fetchObjects(sql, 1, null, Boolean.FALSE);
        Iterator<?> it = ec.iterator();

        if (!it.hasNext()) {
            throw new IllegalStateException("The choiceList " + choiceListName + " must exist.");
        }

        com.filenet.api.admin.ChoiceList choiceListStr = (com.filenet.api.admin.ChoiceList) it.next();
        
        CreateChoiceList.setLocalizedNames(choiceListName, choiceList, context, context.getConnection().getObjectStore());
        
        choiceListStr.set_DisplayName(this.choiceListName);
        choiceListStr.set_ChoiceValues(choiceList);
        choiceListStr.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));

        return choiceListStr.get_Id().toString();
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Update the choiceList " + choiceListName;
    }

    @SuppressWarnings("unchecked")
    protected void buildChoices(Node root, ChoiceList list) {

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {

            Node node = children.item(i);

            if (node.getNodeType() != 3) {
                Choice choice = Factory.Choice.createInstance();
                choice.set_DisplayName(node.getAttributes().getNamedItem("name").getNodeValue());

                if ("group".equals(node.getNodeName())) {

                    choice.set_ChoiceType(type == TypeID.LONG ? ChoiceType.MIDNODE_INTEGER : ChoiceType.MIDNODE_STRING);
                    choice.set_ChoiceValues(Factory.Choice.createList());

                    buildChoices(node, choice.get_ChoiceValues());

                    if (node.getAttributes().getNamedItem("value") != null) {
                        String value = node.getAttributes().getNamedItem("value").getNodeValue();
                        if (type == TypeID.LONG) {
                            choice.set_ChoiceIntegerValue(Integer.valueOf(value));

                        } else {
                            choice.set_ChoiceStringValue(value);
                        }
                    }
                } else if ("choice".equals(node.getNodeName())) {

                    String value = node.getAttributes().getNamedItem("value").getNodeValue();

                    if (type == TypeID.LONG) {
                        choice.set_ChoiceType(ChoiceType.INTEGER);
                        choice.set_ChoiceIntegerValue(Integer.valueOf(value));

                    } else {
                        choice.set_ChoiceType(ChoiceType.STRING);
                        choice.set_ChoiceStringValue(value);
                    }

                }
                list.add(choice);
            }
        }
    }

}
