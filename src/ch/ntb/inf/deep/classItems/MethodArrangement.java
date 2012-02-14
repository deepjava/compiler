package ch.ntb.inf.deep.classItems;

import java.io.PrintStream;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class MethodArrangement implements ICclassFileConsts {// arrangement
	private static PrintStream vrb = StdStreams.vrb;
	private static final boolean verbose = false, assertions = true;
	private static final boolean eTrace = true, trace = true;
//	private static MethodArrangement ma;
	private static final byte maxNofBitsForMethIndices = 16;

	private Method[] instMethods;
	private int nofInstMethods;
	int[] levelIndices;
	int topClassExtLevel;

	private Class interf;
	private Method[] interfMethods;
	private int nofInterfMethods;

	private InterfaceList interfList;

	private int[] swapLog;
	private int swapLogTop;

	MethodArrangement( int maxExtensionLevelStdClasses, int maxMethTabLen, int maxInterfMethTabLen ){
		levelIndices = new int[maxExtensionLevelStdClasses+2];
		instMethods = new Method[ maxMethTabLen + 1 ];
		interfMethods = new Method[maxInterfMethTabLen];
		interfList = new InterfaceList( );
		swapLog = new int[maxInterfMethTabLen];
	}

	void clearInstMethodArray( int length ){
		while( --length >= 0) instMethods[length] = null;
	}

	void setInterface( Class interf ){
//if(eTrace) vrb.printf(">setInterface: name=%1$s, nofMeths=%2$d\n", interf.name, interf.methTabLength);
		this.interf = interf;
		nofInterfMethods = interf.methTabLength;
		interf.getInterfaceMethodTable( interfMethods );
	}

	void swapLogReset( int nofInterfaceMethods ){
		if( swapLog.length < nofInterfaceMethods ) swapLog = new int[nofInterfaceMethods];
		else{
			while( swapLogTop >= 0) swapLog[swapLogTop--] = 0;
		}
	}

	void swapLogPush( int fromIndex, int toIndex ){
		swapLogTop++;
		swapLog[swapLogTop] = fromIndex<<maxNofBitsForMethIndices | toIndex;
	}

	void undoLoggedOperations(){
		while( swapLogTop >= 0 ){
			int from = swapLog[swapLogTop];
			int to    = from & (1<<maxNofBitsForMethIndices)-1;
			from    = from>>maxNofBitsForMethIndices;
			Method m = instMethods[to];
//if(trace) vrb.printf(" undo..: to=%1$d, from=%2$d, m.name=%3$s\n", to, from, m.name);
			m.fixed = false; // undo fixed
			instMethods[to] = instMethods[from];
			instMethods[from] = m;
			swapLogTop--;
		}
	}
	
	int getFirstIndexOfLevel(int level){
		return levelIndices[level];
	}

	int getLastIndexOfLevel(int level){
		return Math.max(levelIndices[level], levelIndices[level+1]-1);
	}

	int getLevel(int index){
		int level = topClassExtLevel;
		while( level >=0 && index < levelIndices[level]) level--;
		return level;
	}

	int selectMethod( HString methName) {
		int index = nofInstMethods-1;
		while( index >= 0 && methName != instMethods[index].name ) index--;
		return index;
	}

	int locateInterfaceMethod( int interfMethNumber ){
		if(assertions) assert interfMethNumber >= 0 && interfMethNumber < nofInterfMethods;
		int index = selectMethod( interfMethods[interfMethNumber].name );
		if(assertions) assert index >= 0;
		return index;
	}

	int locateInterfaceMethods( ){
		int seqLength = nofInterfMethods;
		int firstIndex = selectMethod( interfMethods[0].name );
		if( firstIndex < 0 ) return -1;
		int mx = 1;
		while( mx < seqLength && selectMethod( interfMethods[mx].name ) == (firstIndex+mx) ) mx++;
		if( mx == seqLength ) return firstIndex; else return -1;
	}

	/**
	 * Swap two methods on the same extension level.
	 * @param relocMethIndex  the method being relocated to the new place toIndex. This method is now fixed to its new place.
	 * @param toIndex
	 * @return true on success, false otherwise (if the methods do not belong to the same extension level or if one of the two methods was already fixed)
	 */
	boolean swapAndFix( int relocMethIndex, int toIndex ){
//if(trace) vrb.printf(">swapAndFix: relocName=%1$s, relocIndex=%2$d, toIndex=%3$d\n", instMethods[relocMethIndex].name, relocMethIndex, toIndex);
		Method mh = instMethods[relocMethIndex];
		if( getLevel(relocMethIndex) != getLevel(toIndex) )  return false;
		if( instMethods[relocMethIndex].fixed || instMethods[toIndex].fixed ) return false;
		instMethods[relocMethIndex] = instMethods[toIndex];
		instMethods[toIndex] = mh;
		swapLogPush(relocMethIndex, toIndex);
		mh.fixed = true;
//if(trace) vrb.printf("<swapAndFix\n");
		return true;
	}
	
	int  relocateIntfMethsWithinItsLevel(){
		// int  relocateIntfMethsWithinItsLevel( HString intfName, Method[] intfMethods )
//if(eTrace) vrb.printf(">relocateIntfMethsWithinItsLevel: intfName=%1$s, intfNofMeth=%2$d\n", interf.name, nofInterfMethods);
		int level = getLevel( selectMethod( interfMethods[0].name ) );
		int firstIndex = getFirstIndexOfLevel( level );
		int lastIndex = getLastIndexOfLevel( level );
		if( (lastIndex - firstIndex)+1 < nofInterfMethods ) return -1;
		int index = lastIndex+1;
		int count = 0; 
		do{
			index--;
			if( instMethods[index].fixed ) count = 0; else count++;
		}while( count < nofInterfMethods && index >= firstIndex);
		if ( count < nofInterfMethods ) return -1;
		int begin = index;

		boolean swapSuccessful = true;
		int m = 0;
		do{
			Method meth = interfMethods[m];
			int mx = selectMethod( meth.name );
			swapSuccessful = swapAndFix( mx, index );
			m++; index++;
		}while( swapSuccessful && m < nofInterfMethods);
		if( swapSuccessful ) return begin;
		else return -1;
	}

	int relocateIntfMethsWithinNeighborLevels( int nofLowerLevelMethods ){
//if(eTrace) vrb.printf(">relocateIntfMethsWithinNeighborLevels: intfName=%1$s, length=%2$d, nofLowerLevelMethods=%3$d\n", interf.name, interfMethods.length, nofLowerLevelMethods);
//		int hlIndex = selectMethod( interfMethods[nofInterfMethods-1].name );
		int higherLevel = getLevel( selectMethod( interfMethods[nofInterfMethods-1].name ) );
		int higherIndex = getFirstIndexOfLevel(higherLevel);
		int lowerIndex = higherIndex - 1;

		boolean swapSuccessful = true;
		int m = nofLowerLevelMethods - 1;
		do{
			Method meth = interfMethods[m];
			int mx = selectMethod( meth.name );
			swapSuccessful = swapAndFix( mx, lowerIndex );
			m--; lowerIndex--;
		}while( swapSuccessful && m >= 0 );
		int begin = lowerIndex;

		m = nofLowerLevelMethods;
		while( swapSuccessful && m < nofInterfMethods ){
			Method meth = interfMethods[m];
			int mx = selectMethod( meth.name );
			swapSuccessful = swapAndFix( mx, higherIndex );
			m++; higherIndex++;
		}
		if( !swapSuccessful ) begin = -1;

//if(eTrace) vrb.printf("<relocateIntfMethsWithinNeighborLevels: intfName=%1$s, begin=%2$d\n", interf.name, begin);
		return begin;
	}

	int  arrangeMethodsForThisInterface( Class cls, Class thisInterface ){
//if(eTrace) vrb.printf(">arrangeMethsFTI: cls.name=%1$s, methTabLength=%2$d, inf.name=%3$s, inf.methTabLength=%4$d\n", cls.name, cls.methTabLength, thisInterface.name, thisInterface.methTabLength );
		setInterface( thisInterface );
		
//if(eTrace) {  vrb.print(" =arrangeMethsFTI 05: ");  printInterfaceMethods(); }

		int startIndex = -1;
		if( nofInterfMethods > 1 ){
			int lowerLevel = getLevel( selectMethod( interfMethods[0].name ) );
			int higherLevel = lowerLevel;
			int m = 0;
			do{
				Method meth = interfMethods[m];
				HString methName = meth.name;
				int index = selectMethod( methName );
				higherLevel = getLevel(index);
				m++;
			}while( m < nofInterfMethods &&  higherLevel == lowerLevel );
//if(trace) vrb.printf(" =arrangeInterfaceM30: m=%1$d, lowerLevel=%2$d, higherLevel=%3$d\n",m, lowerLevel, higherLevel);
	
			swapLogReset( nofInterfMethods );
			if( higherLevel == lowerLevel )  startIndex = relocateIntfMethsWithinItsLevel();
			else /* lowerLevel < higherLevel */ startIndex = relocateIntfMethsWithinNeighborLevels(m-1);
			if( startIndex < 0 ) undoLoggedOperations();
		}
//if(eTrace) {  vrb.print(" =rrangeMethsFTI 50: ");  printInstanceMethods(); }
//if(eTrace) vrb.println("<rrangeMethsFTI\n");
		return startIndex;
	}

	/**
	 * Arrange interface methods in such a way, that no additional method table is necessary for this interface (<code>intfMethods</code>).
	 * <p>Note:<ul>
	 * <li>The arranged methods get fixed to their new position.
	 * <li>No action is taken, if the number of methods is <= 1.
	 * </ul></p>
	 * @param intfName just for debugging
	 * @param intfMethods a set of methods being called as interface methods (by <code>invokeinterface</code>)
	 * @return <ul>
	 * 		<li>the new position (>= 0) of the first method of that interface (index of interface method number 0)
	 * 		<li>0, if relocation fails or if <code>intfMethods.length <= 1</code>
	 * 		</ul>
	 */
	void arrangeInterfaceMethodsForThisClassStack( Class cls ){
//if(eTrace) vrb.printf(">arrangeInterfaceMFTCS: cls.name=%1$s, methTabLength=%2$d\n", cls.name, cls.methTabLength );

		if ( (cls.accAndPropFlags&(1<<dpfInstances)) != 0 ){
			nofInstMethods = cls.methTabLength;
			clearInstMethodArray( nofInstMethods );
			cls.getMethodTable( instMethods, levelIndices );
			topClassExtLevel = cls.extensionLevel;

			interfList.clear();
			cls.collectCallInterfacesFromTopExtLevelTo0( interfList );
			
//if(eTrace){
//	vrb.print(" =arrangeInterfaceMFTCS 02: ");  printInstanceMethods();
//	vrb.print(" =arrangeInterfaceMFTCS 03: ");  interfList.print();
//}

			if( interfList.length > 0 ){
				int nofCallInterfaces = interfList.length;
				for(int n = 0; n < nofCallInterfaces; n++ ){
					Class interf = interfList.getInterfaceAt( n );
					arrangeMethodsForThisInterface( cls, interf );
				}
				cls.updateMethodList( instMethods, nofInstMethods );
			}
		}
//if(eTrace) vrb.println("<arrangeInterfaceMFTCS\n");
	}


	/**
	 * Generate method table for this class.
	 * @param cls  specifies the class, for which the method table is generated.
	 */
	void generateMethodTableForThisClass( Class cls ){
//if(eTrace) vrb.printf(">generateMT: cls.name=%1$s, methTabLength=%2$d\n", cls.name, cls.methTabLength );

		nofInstMethods = cls.methTabLength;
		clearInstMethodArray( nofInstMethods );
		cls.getMethodTable( instMethods, levelIndices );
		topClassExtLevel = cls.extensionLevel;
		interfList.clear();

		if(assertions) assert nofInstMethods == cls.methTabLength;
//		instMethods[ nofInstMethods ] = imDelegNull;
		
		Method[] methTable;
		int totalMethTabLength = cls.methTabLength;
//if(trace)vrb.printf(" =generateMT 101: MTL=%1$d, TMTL=%2$d\n", cls.methTabLength, totalMethTabLength);
		if ( (cls.accAndPropFlags&(1<<dpfInstances)) == 0 ){
			methTable = new Method[totalMethTabLength];
			System.arraycopy(instMethods, 0, methTable, 0, nofInstMethods);
//if(trace)vrb.printf(" =generateMT 103: MTL=%1$d, TMTL=%2$d\n", cls.methTabLength, totalMethTabLength);
		}else{//(cls.accAndPropFlags&(1<<dpfInstances)) != 0
			interfList.clear();
			cls.collectCallInterfacesFromTopExtLevelTo0( interfList );

//if(eTrace){
//	vrb.print(" =generateMT 105: ");  printInstanceMethods();
//	vrb.print(" =generateMT 106: ");  interfList.print();
//}
			int nofCallInterfaces =  interfList.length;
			int baseIndex=-11;
			if( nofCallInterfaces <= 0 ){ // no interfaces for this class stack
//if(trace)vrb.print(" =generateMT 110: ");
				methTable = new Method[totalMethTabLength];
				System.arraycopy(instMethods, 0, methTable, 0, nofInstMethods);
			}else{// nofCallInterfaces > 0
				totalMethTabLength++;

				//--- a) evaluate totalMethTabLength
				if( nofCallInterfaces == 1 ){
					Class interf = interfList.getFront();
					if( interf.methTabLength > 1 ){
						setInterface( interf );
						totalMethTabLength++;
						baseIndex = this.locateInterfaceMethods();
						if( baseIndex < 0 ) totalMethTabLength += interf.methTabLength;
					}
//if(trace)vrb.printf(" =generateMT 120: MTL=%1$d, TMTL=%2$d, baseIndex=%3$d\n", cls.methTabLength, totalMethTabLength, baseIndex);
				}else{// nofCallInterfaces > 1
					if(assertions) assert totalMethTabLength == cls.methTabLength + 1;
					interfList.packIntfTable();
					for(int n = 0; n < nofCallInterfaces; n++ ){
						Class interf = interfList.getInterfaceAt( n );
						setInterface( interf );
						totalMethTabLength++;
						baseIndex = this.locateInterfaceMethods();
						if( baseIndex < 0 ) totalMethTabLength += interf.methTabLength;
					}
//if(trace)vrb.printf(" =generateMT 130: MTL=%1$d, TMTL=%2$d\n", cls.methTabLength, totalMethTabLength);
				}
				
				methTable = new Method[totalMethTabLength];
				System.arraycopy(instMethods, 0, methTable, 0, nofInstMethods);
//if(trace)vrb.printf(" =generateMT 135: MTL=%1$d, TMTL=%2$d, nofInstMethods=%3$d\n", cls.methTabLength, totalMethTabLength, nofInstMethods);
				
				//--- b) complete methTable by interface methods, tables
				int mtIndex = nofInstMethods;
				if( nofCallInterfaces == 1 ){
					Class interf = interfList.getFront();
					setInterface( interf );
					baseIndex = locateInterfaceMethods();
//if(trace)vrb.printf(" =generateMT 140: MTL=%1$d, TMTL=%2$d, baseIndex=%3$d\n", cls.methTabLength, totalMethTabLength, baseIndex);
					if( interf.methTabLength == 1 ){// 1 interface with 1 method
						if(assertions) assert baseIndex >= 0;
						methTable[ mtIndex++ ] = instMethods[ baseIndex ];
//if(trace)vrb.printf(" =generateMT 142: MTL=%1$d, TMTL=%2$d, baseIndex=%3$d\n", cls.methTabLength, totalMethTabLength, baseIndex);
					}else if( interf.methTabLength > 1 ){// 1 interface with m methods (m>1)
						methTable[ mtIndex++ ] = Method.createCompSpecMethod("imDelegI1Mm"); // imDelegI1Mm
						InterfaceTabEntry intfTabEntry;
						if( baseIndex >= 0 ){
//if(trace)vrb.printf(" =generateMT 146: mtIndex=%1$d, TMTL=%2$d, baseIndex=%3$d\n", mtIndex, totalMethTabLength, baseIndex);
							intfTabEntry =  new InterfaceTabEntry( interf, baseIndex );
							intfTabEntry.setLastTabEntry();
							methTable[ mtIndex++ ] = intfTabEntry;
							if(assertions) assert mtIndex == totalMethTabLength;
						}else{// baseIndex < 0: add method table
//if(trace)vrb.printf(" =generateMT 147: mtIndex=%1$d, TMTL=%2$d, baseIndex=%3$d\n", mtIndex, totalMethTabLength, baseIndex);
							intfTabEntry =  new InterfaceTabEntry( interf, mtIndex+1 );
							intfTabEntry.setLastTabEntry();
							methTable[ mtIndex++ ] = intfTabEntry;
							for(int m = 0; m < nofInterfMethods; m++){
								baseIndex = locateInterfaceMethod( m );
								if(assertions) assert baseIndex >= 0;
								methTable[ mtIndex++ ] = methTable[ baseIndex ];
							}
							if(assertions) assert mtIndex == totalMethTabLength;
						}
					}
//if(trace)vrb.printf(" =generateMT 149: MTL=%1$d, TMTL=%2$d\n", cls.methTabLength, totalMethTabLength);
				}else{// i interfaces (i>1), each with m methods (m>=1)
//if(trace)vrb.printf(" =generateMT 150: MTL=%1$d, TMTL=%2$d\n", cls.methTabLength, totalMethTabLength);
					if(assertions) assert mtIndex == cls.methTabLength;
					methTable[ mtIndex++ ] = Method.createCompSpecMethod("imDelegIiMm"); //imDelegIiMm;
					int itIndex = mtIndex;
					mtIndex += nofCallInterfaces;
					InterfaceTabEntry intfTabEntry = null;
					for(int n = 0; n < nofCallInterfaces; n++ ){
						Class interf = interfList.getInterfaceAt( n );
						setInterface( interf );
						baseIndex = locateInterfaceMethods();
						if( baseIndex >= 0 ){
							intfTabEntry = new InterfaceTabEntry( interf, baseIndex );
							methTable[ itIndex++ ] = intfTabEntry;
						}else{// baseIndex < 0: add method table
							intfTabEntry = new InterfaceTabEntry( interf, mtIndex );
							methTable[ itIndex++ ] = intfTabEntry;
							for(int m = 0; m < nofInterfMethods; m++){
								baseIndex = locateInterfaceMethod( m );
								if(assertions) assert baseIndex >= 0;
								methTable[ mtIndex++ ] = methTable[ baseIndex ];
							}
						}
					}
					intfTabEntry.setLastTabEntry();
//if(trace)vrb.printf(" =generateMT 159: MTL=%1$d, TMTL=%2$d\n", cls.methTabLength, totalMethTabLength);
				}
				if(assertions) assert mtIndex == totalMethTabLength;
			}
		}
		cls.extMethTable = methTable;

//if(trace){
//	vrb.printf(" =generateMT 170: methTable: class=%1$s, methTabLength=%2$d, totalMethTabLength=%3$d\n", cls.name, cls.methTabLength, totalMethTabLength);
//	int n = 0;
//	while( n < cls.extMethTable.length ){
//		vrb.printf("\t[%1$2d] ", n);
//		cls.extMethTable[n].printHeaderX(0);
//		vrb.println();
//		n++;
//	}
//}

//if(eTrace) vrb.println("<generateMT\n");
	}

	//--- debug utilities

	void printInstanceMethods(){
		vrb.println("MA.instMethods:");
		for(int index = 0; index < nofInstMethods; index++){
			Method meth = instMethods[index];
			char fixedSym;
			if( meth.fixed ) fixedSym = 'F'; else fixedSym = '~';
			vrb.printf("  [%1$d]<%2$d>%3$c %4$s.%5$s%6$s\n", index, meth.index, fixedSym, meth.owner.name, meth.name, meth.methDescriptor);  
		}
	}

	void printInterfaceMethods(){
		vrb.printf("interface: name=%1$s, #meths=%2$d:\n", interf.name, nofInterfMethods );
		for(int index = 0; index < nofInterfMethods; index++){
			Method meth = interfMethods[index];
			vrb.printf("  [%1$d] %2$s.%3$s%4$s\n", index, meth.owner.name, meth.name, meth.methDescriptor);  
		}
	}
}
