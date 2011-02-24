package ch.ntb.inf.deep.eclipse.ui.model;

public class ReadVariableElement {
	public static final int tVoid = 0, tRef = 3, tBoolean = 4, tChar = 5, tFloat = 6,
			tDouble = 7, tByte = 8, tShort = 9, tInteger = 10, tLong = 11;

	public String fullQualifiedName;
	public long result;
	public boolean isReaded;
	
	/** 
	 * Kind of Representations:	Binary = 0, Hexadecimal = 1, Decimal = 2,Double = 3, Char = 4,
	 */
	public int representation;
	
	public int type;
	
	public ReadVariableElement(String fullQualifiedName){
		this.fullQualifiedName = fullQualifiedName;
		result = 0;
		isReaded = false;
		representation = 1;
		type = tVoid;
	}
		
	public void setResult(int result){
		this.result = result;		
	}
	
	public void setType(int type){
		if(type < 0 && 11 < type){
			return;
		}
		this.type = type;
	}
	
	public void setFullQualifiedName(String fullQualifiedName){
		this.fullQualifiedName = fullQualifiedName;
	}
	
	public void setIsReaded(){
		isReaded = true;
	}
	
	public void setRepresentation(int repr){
		if(repr < 0 && 4 < repr){
			return;
		}
		representation = repr;
	}

}
