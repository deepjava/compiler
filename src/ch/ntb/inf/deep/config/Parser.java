/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.config;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.ICjvmInstructionOpcs;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Parser implements ErrorCodes, IAttributes, ICclassFileConsts, ICjvmInstructionOpcs {

	private static final boolean dbg = false;

	// -------- Invalid Symbol:
	private static final short g0 = 0,
			sUndef = g0;
	
	// -------- Brackets: 
	private static final short g1 = g0 + 1,
			sLParen = g1,
			sRParen = g1 + 1,
			sLBrace = g1 + 2,
			sRBrace = g1 + 3,
			sLBracket = g1 + 4,
			sRBracket = g1 + 5;
	
	// -------- Punctuation marks:
	private static final short g2 = g1 + 6,
			sComma = g2,
			sQuotationMark = g2 + 1,
			sSemicolon = g2 + 2,
			sDot = g2 + 3,
			sColon = g2 + 4;
	
	// -------- Math operators:
	private static final short g3 = g2 + 5,
			sMul = g3,
			sDiv = g3 + 1,
			sPlus = g3 + 2,
			sMinus = g3 + 3;
	
	// -------- Assignment operators: 
	private static final short g4 = g3 + 4,
			sEqualsSign = g4,
			sAt = g4 + 1;
	
	// -------- Access attributes: 
	private static final short g5 = g4 + 2,
			sRead = g5,
			sWrite = g5 + 1;
	
	// -------- Content attributes: 
	private static final short g6 = g5 + 2,
			sConst = g6,
			sCode = g6 + 1,
			sVar = g6 + 2,
			sHeap = g6 + 3,
			sStack = g6 + 4,
			sSysTab = g6 + 5,
			sDefault = g6 + 6,
			sNone = g6 + 7;
	
	// --------Types and flags: 
	public static final short g7 = g6 + 8,
			sGPR = g7,
			sFPR = g7 + 1,
			sSPR = g7 + 2,
			sIOR = g7 + 3,
			sUnsafe = g7 + 4,
			sSynthetic = g7 + 5,
			sNew = g7 + 6,
			sMSR = g7 + 7,
			sCR = g7 + 8,
			sFPSCR = g7 + 9,
			sExcHnd = g7 + 10;
	
	// -------- Register representation: 
	public static final short g8 = g7 + 11,
			sHex = g8,
			sDez = g8 + 1,
			sBin = g8 + 2;
	
	// -------- Assignment keywords; 
	private static final short g9 = g8 + 3,
			sVersion = g9,
			sDescription = g9 + 1,
			sInclude = g9 + 2,
			sAttributes = g9 + 3,
			sWidth = g9 + 4,
			sSize = g9 + 5,
			sBase = g9 + 6,
			sRootclasses = g9 + 7,
			sSegmentsize = g9 + 8,
			sArraysize = g9 + 9,
			sNofsegements = g9 + 10,
			sKernel = g9 + 11,
			sExceptionBaseClass = g9 + 12,
			sUs = g9 + 13,
			sAddr = g9 + 14,
			sType = g9 + 15,
			sRepr = g9 + 16,
			sLibPath = g9 + 17,
			sBoardType = g9 + 18,
			sArch = g9 +19,
			sLowlevel = g9 + 20,
			sClass = g9 + 21,
			sId = g9 + 22,
			sException = g9 + 23,
			sOffset = g9 + 24,
			sTechnology = g9 + 25,
			sMemorytype = g9 + 26,
			sNofSectors = g9 + 27,
			sSectorSize = g9 + 28,
			sSystemtable = g9 + 29,
			sTctFile = g9 + 30,
			sCpuType = g9 + 31,
			sProgrammerType = g9 + 32,
			sOsType = g9 + 33,
			sImgFile = g9 + 34,
			sImgFormat = g9 + 35;
	
	// -------- Block keywords: 
	private static final short g10 = g9 + 37,
			sMeta = g10,
			sBoard = g10 + 1,
			sCpu = g10 + 2,
			sDevice = g10 + 3,
			sReginit = g10 + 4,
			sSegment = g10 + 5,
			sMemorymap = g10 + 6,
			sModules = g10 + 7,
			sTargetConf = g10 + 8,
			sProject = g10 + 9,
			sSegmentarray = g10 + 10,
			sRegistermap = g10 + 11,
			sRegister = g10 + 12,
			sOperatingSystem = g10 + 13,
			sSysConst = g10 + 14,
			sMethod = g10 + 15,
			sMemorysector = g10 + 16,
			sMemorysectorArray = g10 + 17,
			sSystem = g10 + 18,
			sProgrammer = g10 + 19,
			sProgrammerOpts = g10 + 20,
			sCompiler = g10 + 21;
	
	// -------- Designator, IntNumber,
	private static final short g11 = g10 + 22,
			sDesignator = g11,
			sNumber = g11 + 1;
	
	// -------- End of file: EOF
	private static final short g12 = g11 + 2,
			sEndOfFile = g12;
	
	// -------- Technology keywords
	private static final short g13 = g12 + 1,
			sRam = g13,
			sFlash = g13 + 1; 

	protected static int nOfErrors;
	private static ErrorReporter reporter = ErrorReporter.reporter;
	
	private int sym;
	private String strBuffer;
	private int chBuffer;
	private int intNumber;

	private BufferedReader configFile;
	private Library currentLib;
	private Project currentProject;
	
	private BufferedInputStream bufStream = null;

	// For error prints
	private int lineNumber = 1;
	private String currentFileName;

	private Constants currentConsts = null;
	
	public Parser(File configFile, Library lib) {
		currentFileName = configFile.getAbsolutePath();
		currentLib = lib;
		if(configFile.exists()){
			try {
				bufStream = new BufferedInputStream(new FileInputStream(configFile));
			}
			catch (FileNotFoundException e) {
				ErrorReporter.reporter.error(errIOExp, currentFileName + " not found");
			}
		}
		if(bufStream != null){
			bufStream.mark(Integer.MAX_VALUE);
			
		}
		else {
			ErrorReporter.reporter.error(errIOExp, "failed to open file " + currentFileName);
		}
	}
	
	public Parser(File projectFile, Project project) {
		currentFileName = projectFile.getAbsolutePath();
		currentProject = project;
		if(projectFile.exists()){
			try {
				bufStream = new BufferedInputStream(new FileInputStream(projectFile));
			}
			catch (FileNotFoundException e) {
				ErrorReporter.reporter.error(errIOExp, currentFileName + " not found");
			}
		}
		if(bufStream != null){
			bufStream.mark(Integer.MAX_VALUE);
			
		}
		else {
			ErrorReporter.reporter.error(errIOExp, "failed to open file " + currentFileName);
		}
	}
	
	static void incrementErrors() {
		nOfErrors++;
	}

	public void start() {
		clear();
		if(reporter.nofErrors <= 0) {
			try {
				bufStream.reset();
				configFile = new BufferedReader(new InputStreamReader(bufStream));
				if(this.currentLib != null) {
					readConfigFile();
				}
				else {
					readProjectFile();
				}
				bufStream.close();
			}
			catch(IOException e) {
				ErrorReporter.reporter.error(errIOExp, "failure while reading file " + currentFileName);
			}
		}
	}
	
	protected int readConfigFile() {
		// read first Symbol
		next();

		if(reporter.nofErrors <= 0) meta();

//		while (sym != sEndOfFile && reporter.nofErrors <= 0) {
		while (sym != sEndOfFile) {
			switch (sym) {
			case sCompiler:
				compiler();
				break;
//			case sArch:
//				arch();
//				break;
			case sBoard:
				board();
				break;
			case sCpu:
				cpu();
				break;
			case sProgrammer:
				programmer();
				break;
			case sProject:
				reporter.error(errUnexpectetSymExp,	"in " + currentFileName+ " at Line " + lineNumber + symToString() + " not allowed in library config files!");
				break;
			case sOperatingSystem:
				operatingSystem();
				break;
			default:
				nOfErrors++;
				reporter.error(errUnexpectetSymExp,	"in " + currentFileName+ " at Line " + lineNumber + " expectet symbol: compiler | board | cpu | programmer | operating system, received symbol: " + symToString());
				next();
			}
		}
		return 0;
	}
	
	protected int readProjectFile() {
		// read first Symbol
		next();

		if(reporter.nofErrors <= 0) meta();

		while (sym != sEndOfFile && reporter.nofErrors <= 0) {
			switch (sym) {
			case sProject:
				project();
				break;
			default:
				nOfErrors++;
				reporter.error(errUnexpectetSymExp,	"in " + currentFileName+ " at Line " + lineNumber + " expectet symbol: project, received symbol: " + symToString());
				next();
			}
		}
		return 0;
	}

	/**
	 * Reads a String from the Configfile
	 * 
	 * @return a String
	 */
	private String readString() {
		if (sym != sQuotationMark) {
			nOfErrors++;
			reporter.error(errQuotationMarkExp, "in " + currentFileName
					+ " at Line " + lineNumber);
			return "";
		}
		StringBuffer sb = new StringBuffer();
		int ch;
		try {
			ch = configFile.read();
			while ((ch > 34 && ch <= 126) || ch == ' ' || ch == '!') {
				sb.append((char) ch);
				ch = configFile.read();
			}
			chBuffer = ch;
			next();
			if (sym != sQuotationMark) {
				nOfErrors++;
				reporter.error(errQuotationMarkExp, "in " + currentFileName	+ " at Line " + lineNumber);
				return "";
			}
		} catch (IOException e) {
			reporter.error(errIOExp, e.getMessage());
		}
		next();
		return sb.toString();
	}

	private boolean isKeyword(String str) {
		String temp = str.toLowerCase();// only for keywords for which the case
		// sensitivity will not be considered
		sym = sDesignator;
		switch (temp.charAt(0)) {
		case 'a':
			if (str.equals("arch")) {
				sym = sArch;
				return true;
			} else if (str.equals("arraysize")) {
				sym = sArraysize;
				return true;
			} else if (str.equals("attributes")) {
				sym = sAttributes;
				return true;
			} else if (str.equals("addr")) {
				sym = sAddr;
				return true;
			}
			break;
		case 'b':
			if (str.equals("base")) {
				sym = sBase;
				return true;
			} else if (temp.equals("board")) {
				sym = sBoard;
				return true;
			} else if (temp.equals("boardtype")) {
				sym = sBoardType;
				return true;
			} else if (temp.equals("bin")) {
				sym = sBin;
				return true;
			} 
			break;
		case 'c':
			if (str.equals("code")) {
				sym = sCode;
				return true;
			} else if (str.equals("compiler")) {
					sym = sCompiler;
					return true;
			} else if (str.equals("const")) {
				sym = sConst;
				return true;
			} else if (str.equals("cpu")) {
				sym = sCpu;
				return true;
			} else if (str.equals("cputype")) {
				sym = sCpuType;
				return true;
			} else if (str.equals("class")) {
				sym = sClass;
				return true;
			} else if (temp.equals("cr")) {
				sym = sCR;
				return true;
			}
			break;
		case 'd':
			if (temp.equals("dez")) {
				sym = sDez;
				return true;
			} else if (str.equals("device")) {
				sym = sDevice;
				return true;
			} else if (str.equals("description")) {
				sym = sDescription;
				return true;
			} else if (str.equals("default")) {
				sym = sDefault;
				return true;
			}
			break;
		case 'e':
			if(str.equals("exception")){
				sym = sException;
				return true;
			}if(str.equals("exchnd")){
				sym = sExcHnd;
				return true;
			}else if(str.equals("exceptionbaseclass")) {
				sym = sExceptionBaseClass;
				return true;
			}
			break;
		case 'f':
			if (temp.equals("fpr")) {
				sym = sFPR;
				return true;
			} else if (temp.equals("fpscr")) {
				sym = sFPSCR;
				return true;
			} else if(str.equals("flash")){
				sym = sFlash;
				return true;
			}
			break;
		case 'g':
			if (temp.equals("gpr")) {
				sym = sGPR;
				return true;
			}
			break;
		case 'h':
			if (str.equals("heap")) {
				sym = sHeap;
				return true;
			} else if (temp.equals("hex")) {
				sym = sHex;
				return true;
			} 
			break;
		case 'i':
			if (str.equals("include")) {
				sym = sInclude;
				return true;
			} else if (temp.equals("ior")) {
				sym = sIOR;
				return true;
			}else if (str.equals("id")) {
				sym = sId;
				return true;
			} 
			break;
		case 'k':
			if (str.equals("kernel")) {
				sym = sKernel;
				return true;
			}
			break;
		case 'l':
			if (str.equals("libpath")) {
				sym = sLibPath;
				return true;
			}else if (str.equals("lowlevel")) {
				sym = sLowlevel;
				return true;
			}
		case 'm':
			if (str.equals("modules")) {
				sym = sModules;
				return true;
			} else if (str.equals("meta")) {
				sym = sMeta;
				return true;
			} else if (str.equals("memorymap")) {
				sym = sMemorymap;
				return true;
			} else if (str.equals("method")) {
				sym = sMethod;
				return true;
			} else if (temp.equals("msr")) {
				sym = sMSR;
				return true;
			} else if (str.equals("memorytype")){
				sym = sMemorytype;
				return true;
			} else if (str.equals("memorysector")){
				sym = sMemorysector;
				return true;
			} else if (str.equals("memorysectorarray")){
				sym = sMemorysectorArray;
			}
			break;
		case 'n':
			if (str.equals("nofsegments")) {
				sym = sNofsegements;
				return true;
			} else if (temp.equals("new")) {
				sym = sNew;
				return true;
			} else if (str.equals("nofsectors")){
				sym = sNofSectors;
				return true;
			} else if (str.equals("none")){
				sym = sNone;
				return true;
			}
			break;
		case 'o':
			if (str.equals("operatingsystem")) {
				sym = sOperatingSystem;
				return true;
			}else if(str.equals("offset")){
				sym = sOffset;
				return true;
			}else if(str.equals("ostype")){
				sym = sOsType;
				return true;
			}
		case 'p':
			if (str.equals("project")) {
				sym = sProject;
				return true;
			} else if (str.equals("programmer")) {
				sym = sProgrammer;
				return true;
			} else if (str.equals("programmertype")) {
				sym = sProgrammerType;
				return true;
			} else if (str.equals("programmeropts")) {
				sym = sProgrammerOpts;
				return true;
			}
			break;
		case 'r':
			if (str.equals("read")) {
				sym = sRead;
				return true;
			} else if (str.equals("register")) {
				sym = sRegister;
				return true;
			} else if (str.equals("repr")) {
				sym = sRepr;
				return true;
			} else if (str.equals("registermap")) {
				sym = sRegistermap;
				return true;
			} else if (str.equals("reginit")) {
				sym = sReginit;
				return true;
			} else if (str.equals("rootclasses")) {
				sym = sRootclasses;
				return true;
			} else if (str.equals("ram")){
				sym = sRam;
				return true;
			}
			break;
		case 's':
			if (str.equals("size")) {
				sym = sSize;
				return true;
			} else if (str.equals("stack")) {
				sym = sStack;
				return true;
			} else if (str.equals("segment")) {
				sym = sSegment;
				return true;
			} else if (str.equals("systab")) {
				sym = sSysTab;
				return true;
			} else if (str.equals("segmentsize")) {
				sym = sSegmentsize;
				return true;
			} else if (str.equals("segmentarray")) {
				sym = sSegmentarray;
				return true;
			} else if (temp.equals("spr")) {
				sym = sSPR;
				return true;
			} else if (str.equals("synthetic")) {
				sym = sSynthetic;
				return true;
			} else if (str.equals("sysconst")) {
				sym = sSysConst;
				return true;
			} else if (str.equals("sectorsize")){
				sym = sSectorSize;
				return true;
			} else if (str.equals("system")){
				sym = sSystem;
				return true;
			}else if(str.equals("systemtable")){
				sym = sSystemtable;
				return true;
			}
			break;
		case 't':
			if (str.equals("type")) {
				sym = sType;
				return true;
			} else if (str.equals("targetconfiguration")) {
				sym = sTargetConf;
				return true;
			} else if (str.equals("technology")){
				sym = sTechnology;
				return true;
			} else if (str.equals("tctfile")){
				sym = sTctFile;
				return true;
			}
		case 'u':
			if (temp.equals("unsafe")) {
				sym = sUnsafe;
				return true;
			}else if (str.equals("us")) {
				sym = sUs;
				return true;
			}
		case 'v':
			if (str.equals("var")) {
				sym = sVar;
				return true;
			} else if (str.equals("version")) {
				sym = sVersion;
				return true;
			}
			break;
		case 'w':
			if (str.equals("width")) {
				sym = sWidth;
				return true;
			} else if (str.equals("write")) {
				sym = sWrite;
				return true;
			}
			break;
		default:
			return false;
		}
		return false;
	}

	/**
	 * determine the next symbol ignores tabs and spaces
	 */
	private void next() {
		int ch = 0;
		try {
			if (chBuffer != 0) {
				ch = chBuffer;
				chBuffer = 0;
			} else {
				ch = configFile.read();
			}
			switch (ch) {
			case '#':
				configFile.readLine();
				lineNumber++;
				next();
				break; // Ignore comments
			case '\n':
				lineNumber++;
			case '\r':
			case '\t':
			case ' ':
				next();
				break; // Ignore spaces, tabs and CR
			case '{':
				sym = sLBrace;
				break;
			case '}':
				sym = sRBrace;
				break;
			case '(':
				sym = sLParen;
				break;
			case ')':
				sym = sRParen;
				break;
			case '[':
				sym = sLBracket;
				break;
			case ']':
				sym = sRBracket;
				break;
			case '*':
				sym = sMul;
				break;
			case '/':
				sym = sDiv;
				break;
			case '+':
				sym = sPlus;
				break;
			case '-':
				sym = sMinus;
				break;
			case ',':
				sym = sComma;
				break;
			case '"':
				sym = sQuotationMark;
				break;
			case '=':
				sym = sEqualsSign;
				break;
			case ';':
				sym = sSemicolon;
				break;
			case '@':
				sym = sAt;
				break;
			case '.':
				sym = sDot;
				break;
			case ':':
				sym = sColon;
				break;
			default:
				String s;
				StringBuffer sb;
				if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_' || ch == '<' || ch == '>') {// Designator or Keyword
					sb = new StringBuffer();
					do {
						sb.append((char) ch);
						ch = configFile.read();
					} while ((ch >= 'a' && ch <= 'z')
							|| (ch >= 'A' && ch <= 'Z')
							|| (ch >= '0' && ch <= '9') || ch == '_' || ch == '<' || ch == '>');
					chBuffer = ch;
					s = sb.toString();
					if (!isKeyword(s)) {
						strBuffer = s;
					}
				} else if (ch >= '0' && ch <= '9') { // Number
					sym = sNumber;
					intNumber = 0;
					if (ch == '0') {
						ch = configFile.read();
						if (ch == 'x' || ch == 'X') {// its maybe a hex digit
							sb = new StringBuffer();
							sb.append("0x");
							ch = configFile.read();
							while ((ch >= '0' && ch <= '9')
									|| (ch >= 'a' && ch <= 'f')
									|| (ch >= 'A' && ch <= 'F')) {
								sb.append((char) ch);
								ch = configFile.read();
							}
							chBuffer = ch;
							//work around for problem when in hex-int-number the most significant bit is set;
							if(sb.length() > 9 && sb.charAt(2) > '7'){
								String most = sb.substring(2, 3);
								sb.replace(2, 3, "0");
								intNumber = (Integer.parseInt(most,16) << 28) |Integer.decode(sb.toString());
								break;
							}
							intNumber = Integer.decode(sb.toString());
							break;
						} else if (ch == ';') {
							chBuffer = ch;
							return;
						} else {// check if it is a digit
							if (!(ch >= '0' && ch <= '9')) {
								nOfErrors++;
								reporter.error(errDigitExp, "in " + currentFileName	+ " at Line " + lineNumber);
								chBuffer = ch;
								break;
							}
						}
					}
					do {
						intNumber = intNumber * 10 + ch - '0';
						ch = configFile.read();
					} while (ch >= '0' && ch <= '9');
					chBuffer = ch;
				} else if (ch == -1)
					sym = sEndOfFile;
				else
					sym = sUndef;
			}
		} catch (IOException e) {
			reporter.error(errIOExp, e.getMessage());
		}
	}

	private String symToString() {
		return symToString(sym);
	}

	private static String symToString(int sym) {
		switch (sym) {
		case sUndef:
			return "undefine";
		case sLParen:
			return "(";
		case sRParen:
			return ")";
		case sLBrace:
			return "{";
		case sRBrace:
			return "}";
		case sLBracket:
			return "[";
		case sRBracket:
			return "]";
		case sComma:
			return ",";
		case sQuotationMark:
			return "\"";
		case sSemicolon:
			return ";";
		case sDot:
			return ".";
		case sColon:
			return ":";
		case sMul:
			return "*";
		case sDiv:
			return "/";
		case sPlus:
			return "+";
		case sMinus:
			return "-";
		case sEqualsSign:
			return "=";
		case sAt:
			return "@";
		case sRead:
			return "read";
		case sWrite:
			return "write";
		case sConst:
			return "const";
		case sCode:
			return "code";
		case sVar:
			return "var";
		case sHeap:
			return "heap";
		case sStack:
			return "stack";
		case sSysTab:
			return "systab";
		case sDefault:
			return "default";
		case sGPR:
			return "gpr";
		case sFPR:
			return "fpr";
		case sSPR:
			return "spr";
		case sIOR:
			return "ior";
		case sUnsafe:
			return "unsafe";
		case sSynthetic:
			return "synthetic";
		case sNew:
			return "new";
		case sMSR:
			return "MSR";
		case sCR:
			return "CR";
		case sFPSCR:
			return "FPSCR";
		case sExcHnd:
			return "exchnd";
		case sHex:
			return "hex";
		case sDez:
			return "dez";
		case sBin:
			return "bin";
		case sVersion:
			return "version";
		case sDescription:
			return "description";
		case sInclude:
			return "include";
		case sAttributes:
			return "attributes";
		case sWidth:
			return "width";
		case sSize:
			return "size";
		case sBase:
			return "base";
		case sRootclasses:
			return "rootclasses";
		case sSegmentsize:
			return "segmentsize";
		case sArraysize:
			return "arraysize";
		case sNofsegements:
			return "nofsegements";
		case sKernel:
			return "kernel";
		case sExceptionBaseClass:
			return "exceptionbaseclass";
		case sUs:
			return "us";
		case sAddr:
			return "addr";
		case sType:
			return "type";
		case sRepr:
			return "repr";
		case sLibPath:
			return "libpath";
		case sBoardType:
			return "boardtype";
		case sArch:
			return "arch";
		case sLowlevel:
			return "lowlevel";
		case sClass:
			return "class";
		case sId:
			return "id";
		case sException:
			return "exception";
		case sOffset:
			return "offset";
		case sTechnology:
			return "technology";
		case sMemorytype:
			return "memorytype";
		case sNofSectors:
			return "nofsectors";
		case sSectorSize:
			return "sectorsize";
		case sSystemtable:
			return "systemtable";
		case sCpuType:
			return "cputype";
		case sProgrammerType:
			return "programmertype";
		case sOsType:
			return "ostype";
		case sMeta:
			return "meta";
		case sCompiler:
			return "compiler";
		case sBoard:
			return "board";
		case sCpu:
			return "cpu";
		case sDevice:
			return "device";
		case sReginit:
			return "reginit";
		case sSegment:
			return "segment";
		case sMemorymap:
			return "memorymap";
		case sModules:
			return "modules";
		case sTargetConf:
			return "targetconfiguration";
		case sProject:
			return "project";
		case sSegmentarray:
			return "segmentarray";
		case sRegistermap:
			return "registermap";
		case sRegister:
			return "register";
		case sOperatingSystem:
			return "operatingsystem";
		case sSysConst:
			return "sysconst";
		case sMethod:
			return "method";
		case sMemorysector:
			return "memorysector";
		case sMemorysectorArray:
			return "memorysectorarray";
		case sSystem:
			return "system";
		case sProgrammer:
			return "programmer";
		case sProgrammerOpts:
			return "programmeropts";
		case sDesignator:
			return "designator";
		case sNumber:
			return "number";
		case sEndOfFile:
			return "endoffile";
		case sRam:
			return "ram";
		case sFlash:
			return "flash";
		case sTctFile:
			return "tctfile";
		case sNone:
			return "none";
		default:
			return "";
		}
	}

	private void meta() {
		if (sym != sMeta) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: meta, received symbol: "
					+ symToString());
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
		versionAssignment();
		fileDescAssignment();
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
	}
	
	private void compiler() {
		if (sym != sCompiler) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: compiler, received symbol: " + symToString());
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
		currentConsts = Configuration.compilerConstants;
		sysconst(currentConsts);
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
	}

	private void board() {
		if(sym != sBoard) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: board, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering board section");
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting board name to: " + strBuffer);
		Board b = currentLib.addBoard(strBuffer); 
		currentConsts = b.sysConstants;
		next();
		if(sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		while(sym == sDescription || sym == sCpuType || sym == sSysConst || sym == sMemorymap || sym == sReginit || sym == sTargetConf) {
			if(sym == sDescription) {
				String description = descriptionAssignment();
				if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting description to " + description);
				b.setDescription(description);
			}
			else if(sym == sCpuType) {
				String cpuName = cpuTypeAssignment();
				if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting cpu type to " + cpuName);
				CPU cpu = currentLib.getCpuByName(cpuName);
				if(cpu == null) {
					cpu = currentLib.addCpu(cpuName);
				}
				b.setCpu(cpu);
			}
			else if(sym == sSysConst) {
				sysconst(b.sysConstants);
			}
			else if(sym == sMemorymap) {
				b.memorymap = new MemoryMap();
				memorymap(b.memorymap);
			}
			else if(sym == sReginit) {
				reginit(b.reginit);
			}
			else { // sym == sTargetConf
				targetconfiguration(b);
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
	}
	
	private void cpu() {
		if(sym != sCpu) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: cpu, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering cpu section");
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting CPU name to: " + strBuffer);
		CPU cpu = currentLib.addCpu(strBuffer);
		currentConsts = cpu.sysConstants;
		next();
		if(sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		while(sym == sDescription || sym == sArch || sym == sSysConst || sym == sMemorymap || sym == sRegistermap || sym == sReginit) {
			if(sym == sDescription) {
				String description = descriptionAssignment();
				if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting description to " + description);
				cpu.setDescription(description);
			}
			else if(sym == sArch) {
				String arch = archAssignment();
				if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting arch to " + arch);
				cpu.setArch(arch);
			}
			else if(sym == sSysConst) {
				sysconst(cpu.sysConstants);
			}
			else if(sym == sMemorymap) {
				cpu.memorymap = new MemoryMap();
				memorymap(cpu.memorymap);
			}
			else if(sym == sRegistermap) {
				registermap(cpu);
			}
			else { //sym == sReginit
				reginit(cpu.reginit);
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
	}
	
	private void programmer() {
		if(sym != sProgrammer) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: programmer, received symbol: " + symToString());
			return;
		}
		next();
		
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return;
		}
		Programmer prog = currentLib.addProgrammer(strBuffer);
		next();
		
		if(sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		
		if(sym != sDescription) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString());
			return;
		}
		prog.setDescription(descriptionAssignment());
		
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
	}
	
	private void programmeropts() {
		
	}
	
	private void sysconst(Constants sysConsts) {
		if (sym != sSysConst) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: sysconst, received symbol: " + symToString());
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		while (sym == sDesignator) {
			sysConsts.addConst(strBuffer, varAssignment());
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
	}
	
	private void memorymap(MemoryMap mm) {
		if (sym != sMemorymap) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: memorymap, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering memorymap section");
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		while (sym == sDevice || sym == sSegment || sym == sSegmentarray) {
			if (sym == sDevice) {
				mm.addDevice(device());
			}
			else if (sym == sSegment) {
				//Segment s = segment(mm, false, 0, 0, null);
				Segment s = segment(mm, null);
//				if(s != null) mm.addSegment(s);
			}
			else { // sym == sSegmentarray
				segmentArray(mm, false, null);
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
	}
	
	private Device device() {
		if(sym != sDevice) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: device, received symbol: " + symToString());
			return null;
		}
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return null;
		}
		String devName = strBuffer;
		next();
		if(sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		next();
		String memType = null;
		int technology = -1;
		int attributes = 0;
		int base = 0;
		int width = 0;
		int size = 0;
		while (sym == sAttributes || sym == sBase || sym == sWidth	|| sym == sSize || sym == sTechnology || sym == sMemorytype ) {
			if (sym == sAttributes) {
				attributes = attributeAssignment();
				int mask = ((1 << atrRead) | (1 << atrWrite));
				if ((attributes & ~mask) != 0) {
					attributes = attributes & mask;
					// TODO warn user
				}
			}
			else if (sym == sBase) {
				base = baseAssignment();
			}
			else if (sym == sWidth) {
				width = widthAssignment();
			}
			else if (sym == sSize){
				size = sizeAssignment();
			}
			else if (sym == sTechnology){
				technology = technologyAssignment();
			}
			else if (sym == sMemorytype){
				next();
				if (sym != sEqualsSign) {
					nOfErrors++;
					reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
				}
				next();
				memType = readString();
				if (sym != sSemicolon) {
					nOfErrors++;
					reporter.error(errSemicolonMissExp, "in " + currentFileName + " before Line " + lineNumber);
				}
				next();
			} 
		}
		Device device =new Device(devName, base, size, width, attributes, technology);
		device.setMemoryType(memType);
		while( sym == sMemorysector || sym == sMemorysectorArray) {
			if(sym == sMemorysector){
				device.addSector(sector());
			}
			else if(sym == sMemorysectorArray) {
				sectorArray(device);
			}
		}
		if(sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		if (attributes == 0 || width == 0 || size == 0 || technology == -1 || (technology > 0 && memType == null) || (technology > 0 && device.sector == null)) {
			reporter.error(errInconsistentattributes, "in " + currentFileName + " Missing attribute while creating device \"" + devName + "\"");
			return null;
		}
		next();
		return device;
	}
	
	private void sectorArray(Device dev){
		if (sym != sMemorysectorArray) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: memorysector, received symbol: " + symToString());
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: designator, received symbol: "
					+ symToString());
			return;
		}
		String designator = strBuffer;
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		int secSize = -1;
		int nofSectors = 0;
		int base = -1;
		while (sym == sSectorSize || sym == sNofSectors || sym == sBase){
			if(sym == sSectorSize){
				secSize = sectorSizeAssignment();
			}else if(sym == sBase){
				base = baseAssignment();
			}else{ //sNofSectors
				nofSectors = nofSectorAssignment();
			}			
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;

		}
		if(secSize == -1 || nofSectors == 0 || base == -1){
			reporter.error(errInconsistentattributes, "in " + currentFileName
					+ " Missing attribute by creation sector array in device: "
					+ dev.toString());
			return;
		}
		for(int i = 0; i < nofSectors; i++){
			dev.addSector(new Memorysector(designator + "_" + i, base + i * secSize, secSize));
		}
		next();
	}
	
	private Memorysector sector(){
		if (sym != sMemorysector) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: memorysector, received symbol: " + symToString());
			return null;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: designator, received symbol: "
					+ symToString());
			return null;
		}
		Memorysector sec = new Memorysector(strBuffer);
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return null;
		}
		next();
		while (sym == sSectorSize || sym == sBase){
			if(sym == sSectorSize){
				sec.setSize(sectorSizeAssignment());
			}else{ //sBase
				sec.setBaseAddress(baseAssignment());
			}			
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		next();
		return sec;		
	}
		
	private Segment segment(MemoryMap mm, Segment parentSegment) {
		if(sym != sSegment) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: segment, received symbol: " + symToString());
			return null;
		}
		next();
		String segDesignator = segmentDesignator();
		String[] fullSegName = segDesignator.split("\\.");
		Segment seg;
		
		if(parentSegment != null || fullSegName.length > 2) { // segment is a sub segment
			String segName;
			if(fullSegName.length > 2) {
				segName = fullSegName[fullSegName.length - 1];
				parentSegment = mm.getSegmentByName(segDesignator.substring(0, segDesignator.lastIndexOf('.')));
				if(parentSegment == null) {
					reporter.error(errNoSuchSegment,"in "	+ currentFileName + " at Line "	+ lineNumber + " Segment not found: " + fullSegName[fullSegName.length - 2]);
					incrementErrors();
					return null;
				}
			}
			else { 
				segName = segDesignator;
			}
			seg = new Segment(segName, parentSegment.owner);
			seg.setAttributes(parentSegment.attributes);
			seg.setWidth(parentSegment.width);
			parentSegment.addSubSegment(seg);
		}
		else { // segment is not a sub segment
			if(fullSegName.length < 2) { // device name not given
				reporter.error(errSyntax,"in "	+ currentFileName + " at Line "	+ lineNumber + " Device name not given. Sytax error in: " + segDesignator);
				incrementErrors();
				return null;
			}
			Device dev = mm.getDeviceByName(fullSegName[0]);
			if(dev == null) {
				ErrorReporter.reporter.error(errNoSuchDevice, fullSegName[0] + " in " + currentFileName + " at line " + lineNumber);
				incrementErrors();
				return null;
			}
			seg = new Segment(fullSegName[1], dev);
			seg.setAttributes(dev.getAttributes());
			seg.setWidth(dev.getWidth());
			dev.addSegment(seg);
		}
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return null;
		}
		next();
		while (sym == sAttributes || sym == sBase || sym == sWidth || sym == sSize) {
			switch (sym) {
			case sAttributes:
				seg.setAttributes(attributeAssignment());
				break;
			case sBase:
				seg.setBaseAddress(baseAssignment());
				break;
			case sWidth:
				seg.setWidth(widthAssignment());
				break;
			case sSize:
				seg.setSize(sizeAssignment());
				break;
			}
		}
		while (sym == sSegmentarray || sym == sSegment) {
			if (sym == sSegmentarray) {
				segmentArray(mm, true, seg);
			}
			else { // sym == sSegment
				segment(mm, seg);
			}
		}
		if ((seg.attributes & (1 << atrHeap)) != 0) {
			mm.registerHeapSegment(seg);
		}
		if ((seg.attributes & (1 << atrStack)) != 0) {
			mm.registerStackSegment(seg);
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		next();
		return seg;
	}

	private void segmentArray(MemoryMap mm, boolean isSubSegment, Segment parent) {
		int arraySize = 0;
		int baseAddr = -1;
		int width = -1;
		int attributes = 0;
		int nofSegments = 0;

		if (sym != sSegmentarray) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber	+ " expected symbol: segmentarray, received symbol: " + symToString());
			return;
		}
		next();
		String segName = segmentDesignator();
		Device dev;
		if (!isSubSegment) {
			int indexOf = segName.indexOf('.', 0);
			if(indexOf != -1) {
				segName = segName.substring(indexOf + 1);
				String devName = segName.substring(0, indexOf);
				dev = mm.getDeviceByName(devName);
				if (dev == null) {
					reporter.error(errNoSuchDevice, devName.toString() + " for Segment " + segName.toString());
					return;
				}

				indexOf = segName.indexOf('.', 0);
				if(indexOf != -1) {// it is true when the new Segment is a
					// Subsegment
					String segment = segName.substring(0, indexOf);
					Segment seg = dev.getSegementByName(segment);
					segName = segName.substring(indexOf + 1);
					indexOf = segName.indexOf('.', 0);
					while(indexOf != -1) {
						segment = segName.substring(0, indexOf);
						seg = seg.getSubSegmentByName(segment);
						segName = segName.substring(indexOf + 1);
						indexOf = segName.indexOf('.', 0);
					}
					parent = seg;
					attributes = seg.getAttributes();
					width = seg.width;
				}
				else {
					attributes = dev.getAttributes();
					width = dev.getWidth();
				}
			}
			else {
				reporter.error(errSyntax,	"Error in memorymap segementarray definition (" + segName.toString() + "), segmentarray names starts with the device name!");
				return;
			}
		}
		else {
			dev = parent.owner;
		}

		if(sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		int segSize = segmentSizeAssignment();
		while (sym == sArraysize || sym == sWidth || sym == sNofsegements || sym == sBase || sym == sAttributes) {
			if(sym == sArraysize) {
				arraySize = arraySizeAssignment();
			}
			else if(sym == sNofsegements) {
				nofSegments = nofSegmentAssignment();
			}
			else if(sym == sWidth) {
				width = widthAssignment();
			}
			else if(sym == sAttributes) {
				attributes = attributeAssignment();
			}
			else{ // sym == sBase
				baseAddr = baseAssignment();
			}
		}
		if(sym != sRBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "  + lineNumber);
			return;
		}
		if(arraySize != 0) {
			if(nofSegments != 0) {
				if(nofSegments != (arraySize / segSize)) {
					reporter.error(errInconsistentattributes,"Number of segemts in segmentarray creation not as expected");
					return;
				}
			}
			else {
				nofSegments = arraySize / segSize;
				if(arraySize % segSize != 0) {
					reporter.error(errInconsistentattributes,"Segmentsize is not a multiple of Arraysize");
					return;
				}
			}
		}
		else {
			if(nofSegments == 0) {
				reporter.error(errInconsistentattributes, "in " + currentFileName + " Missing attribute in segmentarray creation");
				return;
			}
		}
		// from here is nofSegments != 0
		if(attributes == 0){
			attributes = parent.attributes;
		}
		if(width == -1){
			width = parent.width;
		}
		Segment root = new Segment(segName.toString() + 1, dev, baseAddr, segSize, width, attributes);
		Segment current = root;

		// setReference for heap and stack segments
		if((current.attributes & (1 << atrHeap)) != 0) {
			mm.registerHeapSegment(current);
		}
		if((current.attributes & (1 << atrStack)) != 0) {
			mm.registerStackSegment(current);
		}

		for(int i = 2; i <= nofSegments; i++) {
			if(baseAddr != -1) {
				baseAddr += segSize;
			}
			current.next = new Segment(segName.toString() + i, dev, baseAddr, segSize, width, attributes);
			current = (Segment)current.next;
			// setReference for heap and stack segments
			if ((current.attributes & (1 << atrHeap)) != 0) {
				mm.registerHeapSegment(current);
			}
			if ((current.attributes & (1 << atrStack)) != 0) {
				mm.registerStackSegment(current);
			}
		}
		next();
		if (parent != null) {
			parent.addSubSegment(root);
		} else if (!isSubSegment) {
			dev.addSegment(root);
		} else {
			reporter.error(errInvalideParameter, "in " + currentFileName + " at Line " + lineNumber + " Parent Segment must be given for Subsegmentarrays");
		}
	}
	
	private void reginit(RegisterInitList reginits) {
		if (sym != sReginit) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: reginit, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering reginit section");
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		while (sym == sDesignator) {
				if(dbg) StdStreams.vrb.println("[CONF] Parser: New reginit for register " + strBuffer);
				reginits.addRegInit(strBuffer,	varAssignment());
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
	}
	
	private void system(TargetConfiguration targetConfig) {
		if (sym != sSystem) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: system, received symbol: "
					+ symToString());
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		while (sym == sSystemtable ) {
			Module root = moduleAssignment(false);
			while (root != null) {
				targetConfig.addSystemModule(root);
				root = (Module)root.next;
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
	}
	
	// If modules are called from MemoryMap, targetConfig = null;
	private void modules(TargetConfiguration targetConfig) {
		if (sym != sModules) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: modules, received symbol: " + symToString());
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		while (sym == sKernel || sym == sException || sym == sHeap	|| sym == sDesignator || sym == sDefault) {
			Module root = moduleAssignment(true);
			while (root != null) {
				targetConfig.addModule(root);
				root = (Module)root.next;
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		if(targetConfig != null){
			Module def = targetConfig.getModuleByName(Configuration.DEFAULT);
			if(def == null){
				nOfErrors++;
				reporter.error(errNoDefaultSegmentDef, "in " + currentFileName + " at " + targetConfig.name);
				return;
			}
		}
	}
	
	private Module moduleAssignment(boolean modulesBlock){
		// if it was called from modules set modulesBlock true otherwise false
		Module root = null, current = null;
		do {
			if (sym == sComma) {// breaks the endless loop
				next();
			}
			switch (sym) {
			case sKernel:
				if (root == null) {
					root = new Module(Configuration.KERNEL);
					current = root;
				} else {
					current.next = new Module(Configuration.KERNEL);
					current = (Module)current.next;
				}
				next();
				break;
			case sException:
				if (root == null) {
					root = new Module(Configuration.EXCEPTION);
					current = root;
				} else {
					current.next = new Module(Configuration.EXCEPTION);
					current = (Module)current.next;
				}
				next();
				break;
			case sHeap:
				if (root == null) {
					root = new Module(Configuration.HEAP);
					current = root;
				} else {
					current.next = new Module(Configuration.HEAP);
					current = (Module)current.next;
				}
				next();
				break;
			case sDefault:
				if (root == null) {
					root = new Module(Configuration.DEFAULT);
					current = root;
				} else {
					current.next = new Module(Configuration.DEFAULT);
					current = (Module)current.next;
				}
				next();
				break;
			case sDesignator:
				if(!modulesBlock){
					reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " Unexpected symbol: " + symToString() + " by system block creation");
					break;
				}
				if (root == null) {
					root = new Module(concatenatedDesignator());
					current = root;
				} else {
					current.next = new Module(concatenatedDesignator());
					current = (Module)current.next;
				}
				break;
			case sSystemtable:
				if (root == null) {
					root = new Module(Configuration.SYSTEMTABLE);
					current = root;
				} else {
					current.next = new Module(Configuration.SYSTEMTABLE);
					current = (Module)current.next;
				}
				next();
				break;
			default:
				reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " Unexpected symbol: " + symToString() + " by module creation");
				break;
			}
		} while (sym == sComma);
		if (sym != sColon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName + " before Line " + lineNumber);
			return null;
		}
		do {
			current = root;
			SegmentAssignment assign = null;
			next();
			switch (sym) {
			case sConst:
				assign = new SegmentAssignment(Configuration.CONST);
				next();
				break;
			case sCode:
				assign = new SegmentAssignment(Configuration.CODE);
				next();
				break;
			case sVar:
				assign = new SegmentAssignment(Configuration.VAR);
				next();
				break;
			case sHeap:
				assign = new SegmentAssignment(Configuration.HEAP);
				next();
				break;
			case sStack:
				assign = new SegmentAssignment(Configuration.STACK);
				next();
				break;
			case sSysTab:
				assign = new SegmentAssignment(Configuration.SYSTAB);
				next();
				break;
			default:
				nOfErrors++;
				reporter.error(errUnexpectetSymExp,	"in " + currentFileName + " at Line " + lineNumber + " expected symbol: contentattribute, received symbol: " + symToString());
				return null;
			}
			if (sym != sAt) {
				nOfErrors++;
				reporter.error(errAssignExp, "in " + currentFileName
						+ " at Line " + lineNumber);
				return null;
			}
			next();
			assign.setSegmentDesignator(segmentDesignator());
			while (current != null) {
				if(modulesBlock){					
					current.setSegmentAssignment(assign);
				}else{
					current.addSegmentAssignment(assign);
				}
				current = (Module)current.next;
			}

		} while (sym == sComma);

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return null;
		}
		next();
		return root;
	}
	
	private Register register() {
		if (sym != sRegister) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: register, received symbol: "
					+ symToString());
			return null;
		}
		next();
		if (!(sym == sDesignator || sym == sCR || sym == sMSR || sym == sFPSCR)) { // TODO check this
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: designator | CR | MSR | FPSCR, received symbol: "
					+ symToString());
			return null;
		}
		Register reg;
		if(sym != sDesignator){
			reg = new Register(HString.getRegisteredHString(symToString()));
		} else {
			reg = new Register(HString.getRegisteredHString(strBuffer));
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: New register " + reg.getName());
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		next();
		while (sym == sType || sym == sAddr || sym == sSize || sym == sRepr) {
			if (sym == sType) {
				reg.setType(typeAssignment());
			}
			if (sym == sAddr) {
				reg.setAddress(addressAssignment());
			}
			if (sym == sSize) {
				reg.setSize(sizeAssignment());
			}
			if (sym == sRepr) {
				reg.setRepresentation(registerRepresentationAssignment());
			}
		}
		if (reg.type < 0 || reg.size < 0) {
			reporter.error(errInconsistentattributes, "in " + currentFileName
					+ " Missing attribute in creation of Register: "
					+ reg.getName().toString());
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		next();
		return reg;
	}
	
	private void registermap(CPU cpu) {
		if (sym != sRegistermap) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: registermap, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering registermap section");
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		cpu.registermap = new RegisterMap();
		next();
		while (sym == sRegister) {
			cpu.registermap.addRegister(register());
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
	}
	
	private void targetconfiguration(Board b) {
		if (sym != sTargetConf) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: targetconfiguration, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering targetconfiguration section");
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return;
		}
		TargetConfiguration targetConfig = new TargetConfiguration(strBuffer, b);
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting target configuration name to: " + strBuffer);
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		next();
		while(sym == sReginit || sym == sSystem || sym == sModules){
			if(sym == sReginit){
				reginit(targetConfig.reginits);
			}
			else if(sym == sSystem){				
				system(targetConfig);
			}
			else { // sym == sModules
				modules(targetConfig);
			}
		}
		Module sysMod = targetConfig.getSystemModules();
		if(sysMod == null){
			nOfErrors++;
			reporter.error(errNoSysTabSegmentDef, "in system " + currentFileName + " at " + targetConfig.name);
			return;
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		b.addTargetConfiguration(targetConfig);
		next();
	}

	private void project() {
		if (sym != sProject) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: project, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering project section");
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return;
		}
		currentProject.setName(strBuffer);
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting project name to: " + strBuffer);
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		if (sym != sLibPath) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: libpath, received symbol: " + symToString() + " -> libpath has to be specified first in project");
			return;
		}
		HString[] libPath = libPathAssignment();
		if(dbg)  {
			StdStreams.vrb.print("[CONF] Parser: Setting library path to: \"");
			for(int i = 0; i < libPath.length; i++) {
				StdStreams.vrb.print(libPath[i].toString());
				if(i < libPath.length - 1) StdStreams.vrb.print(", ");
			}
			StdStreams.vrb.println("\"");
		}
		currentProject.setLibPath(libPath);
		while(sym == sRootclasses || sym == sBoardType || sym == sOsType	|| sym == sProgrammerType || sym == sProgrammerOpts || sym == sTctFile || sym == sImgFile || sym == sImgFormat) {
			if(sym == sRootclasses) {
				if(dbg) StdStreams.vrb.print("[CONF] Parser: Setting rootclasses to: ");
				HString[] classes = rootClassesAssignment();
				currentProject.setRootClasses(classes);
				if(dbg) StdStreams.vrb.println(currentProject.getRootClassNames());
			}
			else if (sym == sBoardType) {
				if(dbg) StdStreams.vrb.print("[CONF] Parser: Setting board type to: ");
				String boardName = boardTypeAssignment();
				Board b = currentProject.setBoard(boardName);
				if(b == null) {
					reporter.error(errBoardNotFound, boardName + ", searched in \"" + currentProject.getLibPathAsSingleString() + "\"");
					return;
				}
				if(dbg) if(b != null) StdStreams.vrb.println(b.getName());
			}
			else if(sym == sOsType) {
				if(dbg) StdStreams.vrb.print("[CONF] Parser: Setting os type to: ");
				String osName = osTypeAssignment();
				OperatingSystem os = currentProject.setOperatingSystem(osName);
				if(os == null) {
					reporter.error(errBoardNotFound, osName + ", searched in \"" + currentProject.getLibPathAsSingleString() + "\"");
					return;
				}
				if(dbg) if(os != null) StdStreams.vrb.println(os.getName());
			}
			else if(sym == sProgrammerType) {
				if(dbg) StdStreams.vrb.print("[CONF] Parser: Setting programmer type to: ");
				String programmerName = programmerTypeAssignment();
				Programmer programmer = currentProject.setProgrammer(programmerName);
				if(programmer == null) {
					reporter.error(errBoardNotFound, programmerName + ", searched in \"" + currentProject.getLibPathAsSingleString() + "\"");
					return;
				}
				if(dbg) if(programmer != null) StdStreams.vrb.println(programmer.getName());
			}
			else if(sym == sProgrammerOpts) {
				// TODO
				next();
			}
			else if(sym == sImgFile) {
				// TODO
				next();
			}
			else if(sym == sImgFormat) {
				// TODO
				next();
			}
			else { // sym == sTctFile
				currentProject.setTctFile(tctFileAssignment());
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		if (currentProject.getRootClasses() == null || currentProject.getBoard() == null) {
			nOfErrors++;
			reporter.error(errMissingTag,"in " + currentFileName + " \"project\" tags \"rootclasses, boardtype and ostype\" must be defined");
			return;
		}
	}

	private void operatingSystem() {
		if (sym != sOperatingSystem) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: operatingsystem, received symbol: " + symToString());
			return;
		}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering operatingsystem section");
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return;
		}
		String osName = strBuffer;
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting os name to: " + osName);
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return;
		}
		next();
		OperatingSystem os = currentLib.addOperatingSystem(osName);
		while (sym == sDescription || sym == sKernel || sym == sHeap || sym == sExceptionBaseClass || sym == sUs || sym == sLowlevel || sym == sException) {
			if(sym == sDescription) {
				String description = descriptionAssignment();
				if(dbg) StdStreams.vrb.println("[CONF] Parser: Setting description to " + description);
				os.setDescription(description);
			}
			else if(sym == sException){
				os.addException(systemClass(true));
			}
			else if (sym == sKernel) {
				os.setKernel(systemClass(false));
			}
			else if (sym == sHeap) {
				os.setHeap(systemClass(false));
			}
			else if (sym == sExceptionBaseClass) {
				os.setExceptionBaseClass(systemClass(false));
			}
			else if (sym == sUs) {
				os.setUs(systemClass(false));
			}
			else if (sym == sLowlevel) {
				os.setLowLevel(systemClass(false));
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line " + lineNumber);
			return;
		}
		if (os.getExceptionBaseClass() == null || os.getHeap() == null || os.getKernel() == null || os.getUs() == null || os.getLowLevel() == null) {
			nOfErrors++;
			reporter.error(	errMissingTag,"in "	+ currentFileName + " \"operatingsystem\" tags \"kernel, heap, exceptionbaseclass, us and lowlevel\" must be defined");
			return;
		}
		currentLib.addOperatingSystem(osName);
		next();
	}

	private SystemClass systemClass(boolean isExceptionClass) {
		boolean isExceptionBase = false;
		if (!(sym == sKernel || sym == sHeap || sym == sExceptionBaseClass
				|| sym == sUs || sym == sLowlevel || sym == sException)) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber + "received symbol: "
					+ symToString());
			return null;
		}
		if(sym == sExceptionBaseClass){
			isExceptionBase = true;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return null;
		}
		next();
		if (sym != sClass) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ "expected Symbol: class, received symbol: "
					+ symToString());
			return null;
		}
		SystemClass clazz = new SystemClass(classAssignment());
		SystemMethod meth;
		while (sym == sMethod) {
			meth = method();
			clazz.addMethod(meth);
			clazz.addAttributes(meth.attributes);
			if(!isExceptionClass && meth.offset != -1){
				reporter.error(errFixMethAddrNotSupported);
				return null;
			}
		}

		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return null;
		}
		next();
		if(isExceptionBase){
			clazz.addAttributes(1 << dpfExcHnd);
		}
		return clazz;

	}

	private SystemMethod method() {
		if (sym != sMethod) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: method, received symbol: "
					+ symToString());
			return null;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: designator, received symbol: "
					+ symToString());
			return null;
		}
		SystemMethod method = new SystemMethod(strBuffer);
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return null;
		}
		next();
		while (sym == sAttributes || sym == sId || sym  == sOffset) {
			if (sym == sAttributes) {
				method.attributes |= attributeAssignment();
			} else if (sym == sId) {
				next();
				if (sym != sEqualsSign) {
					nOfErrors++;
					reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
					return null;
				}
				next();
				int id = expression();
				if((id & 0xFFFFF000) > 0){
					nOfErrors++;
					reporter.error(errInvalideParameter, "id for method" + method.name + "to great, max 12 bit number");
					return null;
				}
				method.attributes |= id;

				if (sym != sSemicolon) {
					nOfErrors++;
					reporter.error(errSemicolonMissExp, "in " + currentFileName
							+ " before Line " + lineNumber);
					return null;
				}
				next();
			}else if(sym == sOffset){
				method.offset = offsetAssignment();
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return null;
		}
		next();
		return method;
	}

	private String versionAssignment() {
		String s;
		if (sym != sVersion) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: version, received symbol: " + symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return "";
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}

	private String fileDescAssignment() {
		String s;
		if (sym != sDescription) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: description, received symbol: "
					+ symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return "";
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}

	private int varAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: designator, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		res = expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;

	}

	private int expression() {
		int value = term();
		while (sym == sPlus || sym == sMinus) {
			int operator = sym;
			next();
			if (operator == sPlus) {
				value = value + term();
			} else if (operator == sMinus) {
				value = value - term();
			}
		}
		return value;
	}

	private int term() {
		int value = factor();
		while (sym == sMul || sym == sDiv) {
			int operator = sym;
			next();
			if (operator == sMul) {
				value *= factor();
			} else if (operator == sDiv) {
				value /= factor();
			}
		}
		return value;
	}

	private int factor() {
		int value = Integer.MAX_VALUE;
		boolean isNeg = false;
		if (sym == sMinus){
			isNeg = true;
			next();
		}
		if (sym == sNumber) {
			value = intNumber;
			next();
		} else if (sym == sLParen) {
			next();
			value = expression();
			if (sym == sRParen) {
				next();
			} else {
				nOfErrors++;
				reporter.error(errRParenExp, "in " + currentFileName + " at Line " + lineNumber);
			}
		} else if (sym == sDesignator) {
			value = currentConsts.getValueOfConstant(strBuffer);
			next();
		} else {
			nOfErrors++;
			reporter.error(errDigitExp, "in " + currentFileName + " at Line " + lineNumber);
		}
		if(isNeg){
			return -value;
		}
		return value;
	}

	private String descriptionAssignment() {
		String s;
		if (sym != sDescription) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return "";
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}
	
	private String boardTypeAssignment() {
		String s;
		if (sym != sBoardType) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: boardtype, received symbol: " + symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return "";
		}
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return "";
		}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}
	
	private String osTypeAssignment() {
		String s;
		if (sym != sOsType) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: ostype, received symbol: " + symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return "";
		}
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return "";
		}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}
	
	private String programmerTypeAssignment() {
		String s;
		if (sym != sProgrammerType) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: programmertype, received symbol: " + symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return "";
		}
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return "";
		}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}
	
	private String cpuTypeAssignment() {
		String s;
		if (sym != sCpuType) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: base, received symbol: " + symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return "";
		}
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return "";
		}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}

	private String archAssignment() {
		String s;
		if (sym != sArch) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: arch, received symbol: " + symToString());
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return "";
		}
		next();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return "";
		}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}
	
	private int baseAssignment() {
		int res = -1;
		if (sym != sBase) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: base, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		res = expression();
		if(res == Integer.MAX_VALUE){
			res = -1;
		}

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}
	
	private int technologyAssignment() {
		int res = -1;
		if (sym != sTechnology) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: technology, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		if(sym == sRam){
			res = 0;
		}else if(sym == sFlash){
			res = 1;
		}else{
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: Ram or Flash, received symbol: " + symToString());
			return res;
		}		
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return -1;
		}
		next();
		return res;
		
	}

	private int nofSectorAssignment() {
		int res = -1;
		if (sym != sNofSectors) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: nofsector, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		res = expression();
		if(res == Integer.MAX_VALUE){
			res = -1;
		}
		
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int widthAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sWidth) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: width, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		res = expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int sizeAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sSize) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: size, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		res = expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int sectorSizeAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sSectorSize) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: sectorsize, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return res;
		}
		next();
		res = expression();
		
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int segmentSizeAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sSegmentsize) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: segmentsize, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return res;
		}
		next();
		res = expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int arraySizeAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sArraysize) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: arraysize, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return res;
		}
		next();
		res = expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int nofSegmentAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sNofsegements) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: nofsegments, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		res = expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int attributeAssignment() {
		int res = 0;
		if (sym != sAttributes) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: attributes, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber );
			return res;
		}
		do {
			next();
			switch (sym) {
			case sRead:
				res |= (1 << atrRead);
				break;
			case sWrite:
				res |= (1 << atrWrite);
				break;
			case sConst:
				res |= (1 << atrConst);
				break;
			case sCode:
				res |= (1 << atrCode);
				break;
			case sVar:
				res |= (1 << atrVar);
				break;
			case sHeap:
				res |= (1 << atrHeap);
				break;
			case sStack:
				res |= (1 << atrStack);
				break;
			case sSysTab:
				res |= (1 << atrSysTab);
				break;
			case sNew:
				res |= (1 << dpfNew);
				break;
			case sUnsafe:
				res |= (1 << dpfUnsafe);
				break;
			case sSynthetic:
				res |= (1 << dpfSynthetic);
				break;
			case sExcHnd:
				res |= (1 << dpfExcHnd);
				break;
			default:
				nOfErrors++;
				reporter.error(errUnexpectetSymExp, "in " + currentFileName
						+ " at Line " + lineNumber
						+ " expected symbol: some attribute, received symbol: "
						+ symToString());
				return res;
			}
			next();
		} while (sym == sComma);
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private int typeAssignment() {
		int s;
		if (sym != sType) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: type, received symbol: " + symToString());
			return sUndef;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return sUndef;
		}
		next();
		if (sym == sGPR || sym == sFPR || sym == sSPR || sym == sIOR || sym == sMSR || sym == sCR || sym == sFPSCR) {
			s = sym;
		} else {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " unexpected symbol: " + symToString());
			return sUndef;
		}
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}

	private int registerRepresentationAssignment() {
		int s = sUndef;
		if (sym != sRepr) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: repr, received symbol: " + symToString());
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return s;
		}
		next();
		if (sym != sDez && sym != sBin && sym != sHex) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: dez | bin | hex, received symbol: "
					+ symToString());
			return s;
		}
		s = sym;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return s;
		}
		next();
		return s;
	}

	private int addressAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sAddr) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: addr, received symbol: "
					+ symToString());
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "+ lineNumber);
			return res;
		}
		next();
		res = expression();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}
	
	private int offsetAssignment() {
		int res = -1;
		if (sym != sOffset) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: addr, received symbol: "
					+ symToString() );
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return res;
		}
		next();
		res = expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return res;
		}
		next();
		return res;
	}

	private String concatenatedDesignator() {
		StringBuffer sb = new StringBuffer();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected symbol: designator, received symbol: "
					+ symToString());
			return sb.toString();
		}
		sb.append(strBuffer);
		next();
		while (sym == sDot || sym == sDiv) {
			sb.append("/");
			next();
			if (sym == sMul) {
				sb.append("*");
			} else if (sym != sDesignator) {
				nOfErrors++;
				reporter.error(errUnexpectetSymExp, "in " + currentFileName
						+ " at Line " + lineNumber
						+ " expected symbol: designator, received symbol: "
						+ symToString());
				return sb.toString();
			} else {
				sb.append(strBuffer);
			}
			next();
		}
		return sb.toString();
	}

	private String classAssignment() {
		String str = null;
		if (sym != sClass) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName
					+ " at Line " + lineNumber
					+ " expected: rootclasses, received symbol: "	// TODO check this: rootclasses or classes???
					+ symToString());
			return null;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		
			next();
			str = readString();
			
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return null;
		}
		next();
		return str;
	}

	private String segmentDesignator() {
		StringBuffer sb = new StringBuffer();
		if(sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
			return sb.toString();
		}
		sb.append(strBuffer);
		next();
		while(sym == sDot) {
			next();
			if (sym != sDesignator) {
				nOfErrors++;
				reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString());
				return sb.toString();
			}
			sb.append(".");
			sb.append(strBuffer);
			next();
		}
		return sb.toString();
	}

	private HString[] rootClassesAssignment() {
		String[] tempList = new String[Configuration.maxNofRootClasses];
		int count = 0;
		if (sym != sRootclasses) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected: rootclasses, received symbol: " + symToString());
			return null;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line "	+ lineNumber);
			return null;
		}
		do {
			next();
			tempList[count] = readString().replace('.', '/');
			tempList[count] = tempList[count].replace('\\', '/');
			count++;
		} while (sym == sComma);
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName	+ " before Line " + lineNumber);
			return null;
		}
		HString[] rootClasses = new HString[count];
		while(count > 0) {
			count--;
			rootClasses[count] = HString.getRegisteredHString(tempList[count]);
		}
		next();
		return rootClasses;
	}

	private HString[] libPathAssignment() {
		String[] tempList = new String[Configuration.maxNofRootClasses];
		int count = 0;
		if (sym != sLibPath) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected: libpath, received symbol: " + symToString());
			return null;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		String str;
		do {
			next();
			str = readString();
			if(str.length() > 0 && str.charAt(str.length() - 1) != '/') {
				if(!str.endsWith(".jar")){
					str = str + '/';
				}
			}
			tempList[count] = str.replace('\\', '/');
			count++;
		} while (sym == sComma);
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName + " before Line " + lineNumber);
			return null;
		}
		HString[] libPaths = new HString[count];
		while(count > 0) {
			count--;
			libPaths[count] = HString.getRegisteredHString(tempList[count]);
		}
		next();
		return libPaths;
	}

	private String tctFileAssignment() {
		String tctFileName = null;
		if (sym != sTctFile) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "in " + currentFileName + " at Line " + lineNumber + " expected: tctFile, received symbol: " + symToString());
			return null;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "in " + currentFileName + " at Line " + lineNumber);
			return null;
		}
		next();
		if(sym == sNone) {
			tctFileName = null;
			next();
		}
		else if (sym == sDefault) {
			tctFileName = Configuration.defaultTctFileName;
			next();
		}
		else if(sym == sQuotationMark){
			String str = readString();
			if(str.length() > 0) {
				tctFileName = str;
			}
			else {
				tctFileName = null;
			}
		}
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp, "in " + currentFileName
					+ " before Line " + lineNumber);
			return null;
		}
		next();
		return tctFileName;
	}
	
	protected static void clear() {
		nOfErrors = 0;
	}

}
