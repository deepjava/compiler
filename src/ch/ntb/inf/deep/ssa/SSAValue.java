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
		case tVoid://void
			r = n + " (" + r + ")";
			break;
		case tThis://this
			r = "(" + r + ")";
			break;
		case tPhiFunc://PhiFunc
			r = r + "(" + n + ")";
			break;
		case tRef://ref
			r = n + " (" + r + ")";
			break;
		case tBoolean://Boolean
		case tChar://Char
		case tFloat://Float
		case tDouble://Double
		case tByte://Byte
		case tShort://Short
		case tInteger://Integer
		case tLong://Long
//			r = r + " (" + constant + ")"  ;
			r = n + " (" + r + ")";
			break;
		case tAref://Aref
			break;
		case tAboolean://Aboolean
			break;
		case tAchar://Achar
			break;
		case tAfloat://Afloat
			break;
		case tAdouble://Adouble
			break;
		case tAbyte://Abyte
			break;
		case tAshort://Ashort
			break;
		case tAinteger://Ainteger
			break;
		case tAlong://Along
			break;
		default:
			break;		
		
		}		
		return r;
	}
	
}
