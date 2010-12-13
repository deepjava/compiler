package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.ssa.instruction.PhiFunction;

/**
 * @author   millischer
 */
public class SSAValue implements SSAValueType {
	public int type;
	public int index = -1;	// index into state array	
	public Item constant;	// contains reference to constant data
	public int n = -1;	// each ssa-instruction is numbered for the register allocation 
	public int end;	// indicates the end number of the live range for this value
	public int reg = -1;	// register or memory slot number
	public int regLong = -1;	// 2nd register or memory slot number for longs, contains upper 4 bytes
	public int regAux1 = -1;	// auxiliary register 1, used for translating complex SSA instructions
	public int regAux2 = -1;	// auxiliary register 2, used for translating complex SSA instructions 
	public int volRegs;	// stores information about volatiles which are used to produce this value
	public int memorySlot = -1;
	public SSAValue join;	// for resolving phi functions
	public boolean nonVol;	// value resides in volatile or nonvolatile register
	public PhiFunction owner = null; //only set if the Value is a result of a phiFunction.
	
	
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
		case tAref://Aref
		case tAboolean://Aboolean
		case tAchar://Achar
		case tAfloat://Afloat
		case tAdouble://Adouble
		case tAbyte://Abyte
		case tAshort://Ashort
		case tAinteger://Ainteger
		case tAlong://Along
			r = n + " (" + r + ")";
			break;
		default:
			break;		
		
		}		
		return r;
	}
	
}
