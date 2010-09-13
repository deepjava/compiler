package ch.ntb.inf.deep.testClasses;

public class T08Calls {
	
	public static int classMethCall(){
		int y = 0;
		int x = classMethod(3);
		y = 2;
		
		return y+x;		
	}
	
	public static int objectMethCall(){
		T08Calls call = new T08Calls();
		return call.objectMethod(9);
	}
	
	public static void callToAnotherClass(){
		T01SimpleMethods.emptyMethodStatic();
	}
	
	
	/*
	 * Helper method
	 */
	public static int classMethod(int param){
		int x = 0;
		for(int i = 0; i < param; i++){
			x = x+3;
		}
		return x;		
	}
	/*
	 * Helper method
	 */
	public int objectMethod(int param){
		return param % 2;		
	}

}
