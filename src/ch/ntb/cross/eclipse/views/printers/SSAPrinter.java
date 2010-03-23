package ch.ntb.cross.eclipse.views.printers;

import java.util.ArrayList;
import java.util.List;

import ch.ntb.cross.eclipse.ssa.PhiFunction;
import ch.ntb.cross.eclipse.ssa.SSA;
import ch.ntb.cross.eclipse.ssa.SSAInstruction;
import ch.ntb.cross.eclipse.ssa.SSANode;
import ch.ntb.cross.eclipse.ssa.SSAVar;

public class SSAPrinter {
	static List<SSANode> visitedBlocks;

	public static String getSSAString(SSA ssa) {
		visitedBlocks = new ArrayList<SSANode>();
		StringBuilder builder = new StringBuilder();

		// Start with the rootNode
		SSANode ssaNode = ssa.getDepthList().get(0);
		String methodName = new String(ssa.getMethod().getName()) + "" + new String(ssa.getMethod().getDescriptor());
		methodName = methodName.replace("<", "_");
		methodName = methodName.replace(">", "_");

		// returnString.append("concentrate=true");
		builder.append("node [shape=record" + ",width=.1,concentrate=true,height=.1,valign=\"left\",fontsize=10,fontname=\"Courier New\"" + "];\n");
		builder.append("\"" + methodName + "\"[shape=record" + ",width=.1,height=.1" + ",label=\"<label>" + methodName + "\"]\n");
		builder.append("\"" + methodName + "\" -> \"" + methodName + "node" + ssaNode.hashCode() + "\"\n");

		printSSA(builder, ssaNode, methodName);

		return builder.toString();
	}

	private static void printSSA(StringBuilder builder, SSANode node, String methodName) {
		if (!visitedBlocks.contains(node)) {
			visitedBlocks.add(node);

			builder.append("\"" + methodName + "node" + node.hashCode() + "\" [label=\"{ <label> " + node.toString());
			if (node.isLoopHeader()) {
				builder.append(" (LH) ");
			}
			builder.append(" | ");

			// Entry-Locals
			if (node.entrySet.length > 0) {
				builder.append("{");
				for (SSAVar ssaVar : node.entrySet) {
					builder.append(ssaVar + "|");
				}
				builder.replace(builder.lastIndexOf("|"), builder.lastIndexOf("|") + 1, "");
				builder.append("}");
				builder.append(" | ");
			}

			// Phi-Functions
			if (node.getPhiFunctions().size() > 0) {
				for (PhiFunction phi : node.getPhiFunctions()) {
					builder.append(phi + "\\n");
				}
				builder.append("|");
			}

			// SSA-Instructions
			for (SSAInstruction instruction : node.getInstructions()) {
				builder.append(instruction + "\\n");
			}

			// Exit-Locals
			if (node.exitSet.length > 0) {

				builder.append(" | {");
				for (SSAVar ssaVar : node.exitSet) {
					builder.append(ssaVar + "|");
				}
				builder.replace(builder.lastIndexOf("|"), builder.lastIndexOf("|") + 1, "");
				builder.append("}");
			}

			builder.append(" }\"]\n");

			/* Draw the successor connections */
			for (SSANode succ : node.successors) {
				builder.append("\"" + methodName + "node" + node.hashCode() + "\" -> \"" + methodName + "node" + succ.hashCode() + "\" "
						+ " [label=\"" + node.successors.indexOf(succ) + "/" + succ.predecessors.indexOf(node)
						+ "\",fontsize=10,fontname=\"Courier New\"]" + "\n");
			}

			for (SSANode succ : node.successors) {
				printSSA(builder, succ, methodName);
			}

			/* Draw the dominator connections */
			if (node.idom != null) {
				builder.append("\"" + methodName + "node" + node.hashCode() + "\" -> \"" + methodName + "node" + node.idom.hashCode()
						+ "\" [color=\"firebrick1\"] \n");
			}
		}
	}
}
