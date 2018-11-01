/**
 *
 */
package lu.mtn.ibm.filenet.deployment.tool;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lu.mtn.ibm.filenet.deployment.tool.execution.Execution;
import lu.mtn.ibm.filenet.deployment.tool.execution.ExecutionContext;
import lu.mtn.ibm.filenet.deployment.tool.execution.XMLHandler;
import lu.mtn.ibm.filenet.deployment.tool.operation.Operation;
import lu.mtn.ibm.filenet.deployment.tool.prerequisite.Prerequisite;


/**
 * @author NguyenT
 *
 */
public class Main {

    public static void main(String argv[]) {

        Execution execution = new Execution();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            saxParser.parse(argv[0], new XMLHandler(execution));

            ExecutionContext context = new ExecutionContext(Main.class.getResource("/configuration.xml").getFile());

            context.open();

            for (Operation op : execution.getTasks()) {
                try {
                    if (op.checkPrerequisites(context)) {
                        op.execute(context);
                        System.out.println("Execution completed : " + op.getDescription());

                    } else {
                        StringBuffer sb = new StringBuffer("---------------------------------\n");
                        sb.append("Prerequisite(s) not met : " + op.getDescription() + "\n");
                        for (Prerequisite p: op.getPrerequisites()) {
                            sb.append("   - " + p.getDescription() + "\n");
                        }
                        System.out.println(sb.toString());
                    }
                } catch (Exception e) {
                    StringWriter s = new StringWriter();
                    PrintWriter w = new PrintWriter(s);
                    w.write("----------------------------------\n");
                    w.write("Exception of operation : " + op.getDescription() + "\n");
                    e.printStackTrace(w);
                    w.close();
                    System.out.println(s.toString());
                }
            }

            context.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
