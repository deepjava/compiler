package ch.ntb.inf.deep.ui.model;

public class ReadVariableElement {
	public String fullQualifiedName;
	public long result;
	public boolean isReaded;
	/** 
	 * Kind of Representations:	Binary = 0, Hexadecimal = 1, Decimal = 2,Double = 3
	 */
	public int representation;
	
	public ReadVariableElement(){
		fullQualifiedName = "";
		result = 0;
		isReaded = false;
		representation = 1;
	}
	
	public void setResult(int result){
		this.result = result;		
	}
	
	public void setFullQualifiedName(String fullQualifiedName){
		this.fullQualifiedName = fullQualifiedName;
	}
	
	public void setIsReaded(){
		isReaded = true;
	}
	
	public void setRepresentation(int repr){
		if(repr < 0 && 3 < repr){
			return;
		}
		representation = repr;
	}

}
