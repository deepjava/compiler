package ch.ntb.cross.eclipse.views.printers;

//import ch.ntb.cross.eclipse.cfg.ByteCodeInstruction;
import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cfg.CFGNode;

public class CFGPrinter {
	public static String getCFGString(CFG cfg) {
		StringBuilder builder = new StringBuilder();

		// First build a list of all Nodes
		CFGNode rootNode = cfg.getNode(0);
		String methodName = new String(cfg.method.getName());

		// returnString.append("concentrate=true");
		builder.append("node [shape=record" + ",width=.1,concentrate=true,height=.1,valign=\"left\",fontsize=10,fontname=\"Courier New\"" + "];\n");
		builder.append("\"" + methodName + "\"[shape=record" + ",width=.1,height=.1" + ",label=\"<label>" + methodName + "\"]\n");
		builder.append("\"" + methodName + "\" -> \"" + methodName + "node" + rootNode.hashCode() + "\"\n");

		printCFG(builder, rootNode, methodName);

		return builder.toString();
	}

	private static void printCFG(StringBuilder builder, CFGNode node, String methodName) {
		builder.append("\"" + methodName + "node" + node.hashCode() + "\" [label=\"{ <label> CFGNode [" + node.firstBCA + " - "
				+ node.lastBCA + "]");
		if (node.isLoopHeader()) {
			builder.append(" (LH) ");
		}
		builder.append(" | ");

//		for (ByteCodeInstruction instruction : node.getInstructions()) {
//			builder.append(instruction + "\\n");
//		}

		builder.append(" }\"]\n");

		/* Draw the successor connections */
//		for (CFGNode succ : node.getSuccessors()) {
//			builder.append("\"" + methodName + "node" + node.hashCode() + "\" -> \"" + methodName + "node" + succ.hashCode() + "\" \n");
//		}
//
//		if (node.getSuccessors().size() == 0 && node.next != null) {
//			builder.append("\"" + methodName + "node" + node.hashCode() + "\" -> \"" + methodName + "node" + node.next.hashCode()
//					+ "\" [color=\"forestgreen\"]\n");
//		}

		if (node.next != null) {
			printCFG(builder, node.next, methodName);
		}
	}
}
