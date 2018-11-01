/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool.operation.create.ce;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.filenet.api.admin.Choice;
import com.filenet.api.admin.LocalizedString;
import com.filenet.api.collection.ChoiceList;
import com.filenet.api.constants.ChoiceType;
import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.constants.TypeID;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;

import lu.mtn.ibm.filenet.client.ri.FileNetCEApiUtil;
import lu.mtn.ibm.filenet.deployment.tool.Constants;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation;
import lu.mtn.ibm.filenet.deployment.tool.operation.exception.OperationInitializationException;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.ce.ExistingObjectPrerequisite;

/**
 * @author NguyenT
 *
 */
public class CreateChoiceList extends AbstractCreateOperation {

    private String choiceListName;

    private TypeID type;

    private ChoiceList choiceList;

    /**
     * @param xml
     * @throws OperationInitializationException
     */
    public CreateChoiceList(String xml) throws OperationInitializationException {
        super(xml);
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation#initInternal(org.w3c.dom.Element, java.util.List)
     */
    @Override
    protected void initInternal(Element root, List<Prerequisite> prerequisites) {

        this.choiceListName = root.getAttribute("name");
        this.type = TypeID.getInstanceFromInt(Integer.valueOf(root.getAttribute("type")));
        this.choiceList = Factory.Choice.createList();

        buildChoices(root, this.choiceList);

        prerequisites.add(new ExistingObjectPrerequisite(false, String.format(Constants.QUERY_EXPORT_CHOICE_LIST_NAME, choiceListName), true, "The choiceList " + choiceListName + " must not exist."));
    }

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.create.AbstractCreateOperation#executeInternal(lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext)
     */
    @Override
    protected String executeInternal(ExecutionContext context) {

        ObjectStore os = context.getConnection().getObjectStore();
        
        setLocalizedNames(choiceListName, choiceList, context, os);

        com.filenet.api.admin.ChoiceList choiceListStr = Factory.ChoiceList.createInstance(os);
        choiceListStr.set_DataType(this.type);
        choiceListStr.set_DisplayName(this.choiceListName);
        choiceListStr.set_ChoiceValues(choiceList);
        choiceListStr.save(RefreshMode.REFRESH, FileNetCEApiUtil.createFilter(new String[]{PropertyNames.ID}));

        return choiceListStr.get_Id().toString();
    }
    
    @SuppressWarnings("unchecked")
    public static void setLocalizedNames(String choiceListName, ChoiceList list, ExecutionContext context, ObjectStore os) {
        
        for (Iterator<Choice> it = list.iterator(); it.hasNext(); ) {
            Choice choice = it.next();
            
            String name = choice.get_DisplayName();
            choice.set_DisplayNames(Factory.LocalizedString.createList());
            choice.set_DisplayName(null);
            
            boolean hasObjectStoreLocale = false;
            Set<Entry<String, Properties>> entries = context.getResourceBundles().entrySet();
            for (Entry<String, Properties> entry : entries) {
                String value = entry.getValue().getProperty("choice." + choiceListName + "." + name, name);
                LocalizedString loc = Factory.LocalizedString.createInstance();
                loc.set_LocalizedText(value);
                loc.set_LocaleName(entry.getKey());
                choice.get_DisplayNames().add(loc);
                
                hasObjectStoreLocale = hasObjectStoreLocale || os.get_LocaleName().equals(entry.getKey());
            }
            
            if (!hasObjectStoreLocale) {
                LocalizedString loc = Factory.LocalizedString.createInstance();
                loc.set_LocalizedText(name);
                loc.set_LocaleName(os.get_LocaleName());
                
                choice.get_DisplayNames().add(loc);
            }
            
            if (ChoiceType.MIDNODE_INTEGER == choice.get_ChoiceType() || ChoiceType.MIDNODE_STRING == choice.get_ChoiceType()) {
                setLocalizedNames(choiceListName, choice.get_ChoiceValues(), context, os);
            }
        }
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

    /**
     * @see lu.mtn.ibm.filenet.deployment.tool.operation.Operation#getDescription()
     */
    @Override
    public String getDescription() {
        return "Create ChoiceList " + choiceListName;
    }
}
