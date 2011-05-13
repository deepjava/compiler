package ch.ntb.inf.deep.testClasses;


public class T06Operators {

	public static boolean conditionalOperator1() {
		// example, where operand stack is not empty at end of node
		int mark;
		boolean bRes;
		boolean a = false, b = true, c = false;
		mark = 101;
		bRes = a ? b : c;
		mark = 102;
		bRes = a ? (b ? c : a) : (c ? a : b);
		mark = 103;
		return bRes;
	}
	
	public static boolean conditionalOperator2() {
		int mark;
		boolean bRes = false;
		boolean a = false, b = true, c = false;
		int d = 1, e = 2, f = 3;
		int iRes;
		mark = 101;
		iRes = d < e ? d : f;
		mark = 102;
		iRes = d < e ? (a ? d : e) : (b != c ? e : f);
		mark = 103;
		return (mark > iRes) && bRes;
	}

	public static boolean conditionalOperator3() {
		boolean bRes = false; boolean a = false, b = true;
		bRes = a && bRes ? a != b : bRes;
		return bRes;
	}

	public static boolean conditionalOperator4(int a) {
		boolean b = a > 10;
		return b;
	}

}
