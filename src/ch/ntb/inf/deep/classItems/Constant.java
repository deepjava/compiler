package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.strings.HString;

public class Constant extends DataItem {
	public int valueH, valueL;

	Constant(HString name, Type type, int valueHigh, int valueLow){
		super(name, type);
		this.valueH = valueHigh;
		this.valueL = valueLow;
	}

	public Constant(int valueHigh, int valueLow){
		this.valueH = valueHigh;
		this.valueL = valueLow;
	}


	//--- debug primitives
	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("const %1$s %2$s ", type.name, name);
		char typeDesc = type.name.charAt(0);
		if(typeDesc == tdFloat){
			vrb.printf("= 0x%1$x = %2$7e", valueH, Float.intBitsToFloat(valueH) );
		}else if(typeDesc == tdLong || typeDesc == cptDouble){
			int low = valueL;
			int high = valueH - (low>>31);;
			long lval = (long)low + ((long)high<<32);
			vrb.printf("= 0x%1$x", lval);
			if (typeDesc == cptDouble)  vrb.printf(" = %1$7e", Double.longBitsToDouble(lval));
		}else{// typeDesc == {tdInt, tdShort, tdByte, tdChar, tdBool}
			vrb.printf("=0x%1$x = %2$d", valueH, valueH);
		}
	}

	private void printConstVal(Item type, int valueH, int valueL){
		long value = ((long)(valueH)<<32) | (valueL&0xFFFFFFFFL);
		char category = type.name.charAt(0);
		switch(category){
		case tdBoolean:
			vrb.print(valueH != 0);
			break;
		case tdByte: 	case tdShort: case tdInt:
			vrb.print( Integer.toString(valueH) );
			break;
		case tdChar:
			vrb.print( (char)valueH );
			break;
		case tdFloat:
			vrb.print( Float.toString(Float.intBitsToFloat(valueH)) );
			break;
		case tdLong:
			vrb.printf("%1$d = 0x%1$x", value, value );
			break;
		case tdDouble:
			vrb.print( Double.longBitsToDouble(value) );
			break;
		default:
			vrb.print("<?>"); vrb.print(getClass().getName());
		}
	}

	public void printValue() {
		vrb.print("value = "); 
		printConstVal(type, valueH, valueL);
	}

	public void printValue(int indentLevel) {
		indent(indentLevel);
		vrb.print("value = "); 
		printConstVal(type, valueH, valueL);
	}
	
	public void print(int indentLevel){
		super.print(indentLevel);
		indent(1);
		vrb.print("value = "); 
		printConstVal(type, valueH, valueL);
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
