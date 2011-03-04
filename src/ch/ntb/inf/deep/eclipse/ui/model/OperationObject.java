package ch.ntb.inf.deep.eclipse.ui.model;

public class OperationObject {
	public long value;
	/**
	 * type of the value
	 */
	public int valueType;
	/**
	 * Name of the variable or the Register, or the full qualified Name of a TargetCMD
	 */
	public String description;
	/** 
	 * Kind of Representations:	Binary = 0, Hexadecimal = 1, Decimal = 2,Double = 3
	 */
	public int representation;
	/**
	 * Register size in bytes
	 */
	public int registerSize;
	
	/**
	 * Address of the register, memory, variable or method
	 */
	public int addr;
	
	/**
	 * Type of the Register	 
	 */
	
	public int registerType;
	
	/**
	 * operation opcode
	 */
	public int operation;
	
	/**
	 * is to determine if when an readVariable operation is chosen, thats display the value only if it is true 
	 */
	public boolean isReaded;
	/**
	 * is to determine if when an TargetCMD operation was executed, thats display the address of the method only if it is true 
	 */
	public boolean cmdSend;
	/**
	 * Message if an error occours
	 */
	public String errorMsg;
	
	public OperationObject(){
		description = "";
	}
	
	public OperationObject(int operation, String description){
		this.operation = operation;
		this.description = description;
		this.errorMsg = "";
	}
	
	public OperationObject(int operation, String description, long value, int type, int representation, int registerSize){
		this.operation = operation;
		this.description = description;
		this.value = value;
		this.valueType = type;
		this.representation = representation;
		this.registerSize = registerSize;
		this.errorMsg = "";
	}
}
