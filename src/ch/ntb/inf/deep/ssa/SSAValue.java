package ch.ntb.inf.deep.ssa;

/**
 * @author   millischer
 */
public class SSAValue implements SSAValueType {
	public int type;
	public int index = -1;	// index into state array	
	public Object constant;
	public int n = -1;	// each ssa-instruction is numbered for the register allocation 
	public int end;	// indicates the end number of the live range for this value
	public SSAValue join = this;	// representative, used for joining values during register allocation
	public int reg = -1;	// register or memory slot number
	public int memorySlot = -1;
	
	public SSAValue(){
	}
	
	public String typeName(){
		return svNames[type];
	}
	
	@Override
	public String toString(){
		String r = svNames[type];
		
		switch(type){
		case 0://void
			break;
		case 1://""
			break;
		case 2://ref
			break;
		case 3://Object
			break;
		case 4://Boolean
		case 5://Char
		case 6://Float
		case 7://Double
		case 8://Byte
		case 9://Short
		case 10://Integer
		case 11://Long
			r = r + " (" + constant + ")"  ;
			break;
		case 12://Aref
			break;
		case 13://Aobject
			break;
		case 14://Aboolean
			break;
		case 15://Achar
			break;
		case 16://Afloat
			break;
		case 17://Adouble
			break;
		case 18://Abyte
			break;
		case 19://Ashort
			break;
		case 20://Ainteger
			break;
		case 21://Along
			break;
		case 22://PhiFunc
			break;
		default:
			break;		
		
		}		
		return r;
	}
	
}
