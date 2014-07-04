/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.RefType;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Parser implements ICclassFileConsts {

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
			sCpuArch = g9 +19,
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
			sImgFormat = g9 + 35,
			sCondition = g9 + 36,
			sHeapClass = g9 + 37;
	
	// -------- Block keywords: 
	private static final short g10 = g9 + 38,
			sMeta = g10;

	public static final short sBoard = g10 + 1;

	private static final short sCpu = g10 + 2;

	private static final short sDevice = g10 + 3;

	private static final short sReginit = g10 + 4;

	private static final short sSegment = g10 + 5;

	private static final short sMemorymap = g10 + 6;

	private static final short sModules = g10 + 7;

	private static final short sRunConf = g10 + 8;

	private static final short sProject = g10 + 9;

	private static final short sSegmentarray = g10 + 10;

	private static final short sRegistermap = g10 + 11;

	private static final short sRegister = g10 + 12;

	public static final short sOperatingSystem = g10 + 13;

	private static final short sSysConst = g10 + 14;

	private static final short sMethod = g10 + 15;

	private static final short sMemorysector = g10 + 16;

	private static final short sMemorysectorArray = g10 + 17;

	private static final short sSystem = g10 + 18;

	public static final short sProgrammer = g10 + 19;

	private static final short sProgrammerOpts = g10 + 20;

	private static final short sCompiler = g10 + 21;

	private static final short sArch = g10 + 22;
	
	// -------- Designator, IntNumber,
	private static final short g11 = g10 + 23,
			sDesignator = g11,
			sNumber = g11 + 1;
	
	// -------- End of file: EOF
	private static final short g12 = g11 + 2,
			sEndOfFile = g12;
	
	// -------- Technology keywords
	private static final short g13 = g12 + 1,
			sRam = g13,
			sFlash = g13 + 1; 

	private static ErrorReporter reporter = ErrorReporter.reporter;
	private int sym;
	private String strBuffer;
	private int chBuffer;
	private int intNumber;
	private BufferedReader file;
	private Project currentProject;
	private SystemConstant currentConsts = null;
	private BufferedInputStream bufStream = null;
	private int lineNumber = 1;
	private String currentFileName;

	
	public Parser(File configFile) {
		currentFileName = configFile.getAbsolutePath();
		if (configFile.exists()) {
			try {
				bufStream = new BufferedInputStream(new FileInputStream(configFile));
				file = new BufferedReader(new InputStreamReader(bufStream));
			} catch (FileNotFoundException e) {
				ErrorReporter.reporter.error(205, currentFileName + " not found");
			}
		}
		if (bufStream != null) bufStream.mark(Integer.MAX_VALUE);
		else ErrorReporter.reporter.error(205, "failed to open file " + currentFileName);
	}
	
	public Parser (Project project) {
		this(project.projectFile);
		currentProject = project;
	}
	
	public void parse() {
		try {
			if (currentProject != null) parseProjectFile();
			else parseConfigFile();
			bufStream.close();
		} catch (IOException e) {
			reporter.error(205, "failure while reading file " + currentFileName);
		}
	}

	public String[] parse(int specSym) {
		String[] str = new String[]{"not available", "not available"};
		try {
			next(); // read first symbol
			if (reporter.nofErrors <= 0) meta();
			while (sym != sEndOfFile) {
				if (sym == specSym) {
					if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering board section");
					next();
					if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return null;}
					if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting board name to: " + strBuffer);
					str[0] = strBuffer;
					next();
					if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
					next();
					while (sym == sDescription || sym == sCpuType || sym == sSysConst || sym == sMemorymap || sym == sReginit || sym == sRunConf) {
						if (sym == sDescription) {
							str[1] = descriptionAssignment();
							if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting description to " + str[1]);
						}
						next();
					}
				} else next();
			}
			bufStream.close();
		} catch (IOException e) {
			ErrorReporter.reporter.error(205, "failure while reading file " + currentFileName);
		}
		return str;
	}
	

	public void parseRunConfigs() {
		try {
			next(); // read first symbol
			if (reporter.nofErrors <= 0) meta();
			while (sym != sEndOfFile) {
				if (sym == sBoard) board();
				next();
			}
			bufStream.close();
		} catch (IOException e) {
			reporter.error(205, "failure while reading file " + currentFileName);
		}
	}
	
	protected int parseConfigFile() {
		next(); // read first symbol
		if (reporter.nofErrors <= 0) meta();
		while (sym != sEndOfFile) {
			switch (sym) {
			case sCompiler:
				compiler();
				break;
			case sArch:
				arch();
				break;
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
				reporter.error(206,	"in " + currentFileName+ " at Line " + lineNumber + symToString() + " not allowed in library config files!");
				break;
			case sOperatingSystem:
				operatingSystem();
				break;
			default:
				reporter.error(206,	"in " + currentFileName+ " at Line " + lineNumber + " expectet symbol: compiler | board | cpu | programmer | operating system, received symbol: " + symToString());
				next();
			}
		}
		return 0;
	}

	protected int parseProjectFile() {
		next(); // read first symbol
		if (reporter.nofErrors <= 0) meta();
		while (sym != sEndOfFile && reporter.nofErrors <= 0) {
			switch (sym) {
			case sProject:
				project();
				break;
			default:
				reporter.error(206,	"in " + currentFileName+ " at Line " + lineNumber + " expectet symbol: project, received symbol: " + symToString());
				next();
			}
		}
		return 0;
	}

	private String readString() {
		if (sym != sQuotationMark) {reporter.error(204, "in " + currentFileName	+ " at Line " + lineNumber); return "";}
		StringBuffer sb = new StringBuffer();
		int ch;
		try {
			ch = file.read();
			while ((ch > 34 && ch <= 126) || ch == ' ' || ch == '!') {
				sb.append((char) ch);
				ch = file.read();
			}
			chBuffer = ch;
			next();
			if (sym != sQuotationMark) {reporter.error(204, "in " + currentFileName	+ " at Line " + lineNumber); return "";}
		} catch (IOException e) {
			reporter.error(205, e.getMessage());
		}
		next();
		return sb.toString();
	}

	private boolean isKeyword(String str) {
		String temp = str.toLowerCase(); // case sensitivity will not be considered
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
			} else if (str.equals("condition")) {
				sym = sCondition;
				return true;
			} else if (str.equals("const")) {
				sym = sConst;
				return true;
			} else if (str.equals("cpu")) {
				sym = sCpu;
				return true;
			} else if (str.equals("cpuarch")) {
				sym = sCpuArch;
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
			if (str.equals("exception")) {
				sym = sException;
				return true;
			} else if (str.equals("exchnd")) {
				sym = sExcHnd;
				return true;
			} else if (str.equals("exceptionbaseclass")) {
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
			} else if (str.equals("heapclass")) {
				sym = sHeapClass;
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
			} else if (str.equals("id")) {
				sym = sId;
				return true;
			} else if (str.equals("imgfile")) {
				sym = sImgFile;
				return true;
			} else if (str.equals("imgformat")) {
				sym = sImgFormat;
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
			if (str.equals("ram")) {
				sym = sRam;
				return true;
			} else if (str.equals("read")) {
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
			} else if (str.equals("runconfiguration")) {
				sym = sRunConf;
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
	 * determine the next symbol, ignores tabs and spaces
	 */
	private void next() {
		int ch = 0;
		try {
			if (chBuffer != 0) {
				ch = chBuffer;
				chBuffer = 0;
			} else {
				ch = file.read();
			}
			switch (ch) {
			case '#':
				file.readLine();
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
						ch = file.read();
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
						ch = file.read();
						if (ch == 'x' || ch == 'X') {// its maybe a hex digit
							sb = new StringBuffer();
							sb.append("0x");
							ch = file.read();
							while ((ch >= '0' && ch <= '9')
									|| (ch >= 'a' && ch <= 'f')
									|| (ch >= 'A' && ch <= 'F')) {
								sb.append((char) ch);
								ch = file.read();
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
							if (!(ch >= '0' && ch <= '9')) {reporter.error(200, "in " + currentFileName	+ " at Line " + lineNumber); chBuffer = ch;	break;}
						}
					}
					do {
						intNumber = intNumber * 10 + ch - '0';
						ch = file.read();
					} while (ch >= '0' && ch <= '9');
					chBuffer = ch;
				} else if (ch == -1)
					sym = sEndOfFile;
				else
					sym = sUndef;
			}
		} catch (IOException e) {
			reporter.error(205, e.getMessage());
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
		case sHeapClass:
			return "heapClass";
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
			return "nofsegments";
		case sKernel:
			return "kernel";
		case sExceptionBaseClass:
			return "exceptionBaseClass";
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
		case sCpuArch:
			return "cpuarch";
		case sLowlevel:
			return "lowlevel";
		case sClass:
			return "class";
		case sCondition:
			return "condition";
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
		case sArch:
			return "arch";
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
		case sRunConf:
			return "runconfiguration";
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
		case sImgFile:
			return "imgfile";
		case sImgFormat:
			return "imgformat";
		case sNone:
			return "none";
		default:
			return "";
		}
	}

	private void meta() {
		if (sym != sMeta) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: meta, received symbol: "	+ symToString()); return;}
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
		versionAssignment();
		fileDescAssignment();
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}
	
	private void compiler() {
		if (sym != sCompiler) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: compiler, received symbol: " + symToString()); return;}
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
		Configuration.setCompilerConstants(sysconst(null));
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}

	private void arch() {
		if (sym != sArch) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: arch, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering arch section");
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting arch name to: " + strBuffer);
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
		Arch arch = Configuration.getBoard().cpu.arch; 
		assert arch != null;
		if (sym == sRegistermap) arch.regs = registermap(null);
		if (dbg) StdStreams.vrb.println("[CONF] registermap for architecture done");
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}
	
	private void cpu() {
		if (sym != sCpu) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: cpu, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering cpu section");
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting CPU name to: " + strBuffer);
		CPU cpu = Configuration.getBoard().cpu;
		assert cpu != null;
		currentConsts = cpu.sysConstants;
		next();
		if(sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		while (sym == sDescription || sym == sCpuArch || sym == sSysConst || sym == sMemorymap || sym == sRegistermap || sym == sReginit) {
			if (sym == sDescription) {
				String description = descriptionAssignment();
				if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting description to " + description);
				cpu.description = HString.getHString(description);
			} else if (sym == sCpuArch) {
				String archName = archAssignment();
				if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting cpuarch to " + archName);
				Arch arch = new Arch(archName);
				cpu.arch = arch;
				Configuration.readConfigFile(Configuration.archPath, arch);
			} else if (sym == sSysConst) {
				if (dbg) StdStreams.vrb.println("[CONF] entering sysconst of cpu");
				cpu.sysConstants = sysconst(Configuration.getCompilerConstants());
				if (dbg) StdStreams.vrb.println("[CONF] sysconst of cpu done");
			} else if (sym == sMemorymap) {
				cpu.memorymap = new MemMap("Cpu");
				memorymap(cpu.memorymap);
			} else if (sym == sRegistermap) {
				cpu.regs = registermap(cpu.arch.regs);
			} else { //sym == sReginit
				cpu.regInits = reginit(cpu.regInits);
			}
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}
	
	private void board() {
		if (sym != sBoard) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: board, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering board section");
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting board name to: " + strBuffer);
		next();
		if(sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		while (sym == sDescription || sym == sCpuType || sym == sSysConst || sym == sMemorymap || sym == sReginit || sym == sRunConf) {
			if (sym == sDescription) {
				String description = descriptionAssignment();
				if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting description to " + description);
				Configuration.getBoard().description = HString.getHString(description);
			} else if (sym == sCpuType) {
				String cpuName = cpuTypeAssignment();
				if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting cpu type to " + cpuName);
				CPU cpu = new CPU(cpuName);
				Configuration.getBoard().cpu = cpu;
				Configuration.readConfigFile(Configuration.cpuPath, cpu);
			} else if (sym == sSysConst) {
				Board b = Configuration.getBoard();
				assert b != null;
				b.sysConstants = sysconst(b.cpu.sysConstants);
			} else if (sym == sMemorymap) {
				MemMap mm = Configuration.getBoard().memorymap = new MemMap("Board");
				memorymap(mm);
			} else if (sym == sReginit) Configuration.getBoard().regInits = reginit(Configuration.getBoard().regInits);
			else runConfiguration(Configuration.getBoard()); // sym == sRunConf
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}
	
	private void programmer() {
		if (sym != sProgrammer) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: programmer, received symbol: " + symToString()); return;}
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		Programmer prog = Configuration.getProgrammer();
		assert prog != null;
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		if (sym != sDescription) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString()); return;}
		prog.setDescription(descriptionAssignment());
		prog.setPluginId(pluginIdAssignment());
		prog.setClassName(classNameAssignment());
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}
	
	private void programmeropts() {}
	
	private SystemConstant sysconst(SystemConstant sysConsts) {
		if (sym != sSysConst) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: sysconst, received symbol: " + symToString()); return null;}
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		SystemConstant head = sysConsts;
		currentConsts = head;
		while (sym == sDesignator) {
			String cName = strBuffer;
			int val = varAssignment();
			SystemConstant c = new SystemConstant(cName, val);
			if (sysConsts == null) {sysConsts = c; head = sysConsts;}
			else head = (SystemConstant) head.insertHead(c);
			currentConsts = head;
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		return head;
	}
	
	private void memorymap(MemMap mm) {
		if (sym != sMemorymap) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: memorymap, received symbol: " + symToString()); return;}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering memorymap section");
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		while (sym == sDevice || sym == sSegment) {
			if (sym == sDevice) mm.addDevice(device());
			else segment(mm); // sym == sSegment
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}
	
	private Device device() {
		if(sym != sDevice) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: device, received symbol: " + symToString()); return null;}
		next();
		if(sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return null;}
		String devName = strBuffer;
		next();
		if(sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line " + lineNumber); return null;}
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
				if ((attributes & ~((1 << dpfSegRead) | (1 << dpfSegWrite))) != 0) {
					reporter.error(251, "in " + currentFileName + " at Line " + lineNumber); return null;
				}
			} else if (sym == sBase) base = baseAssignment();
			else if (sym == sWidth) width = widthAssignment();
			else if (sym == sSize) size = sizeAssignment();
			else if (sym == sTechnology) technology = technologyAssignment();
			else if (sym == sMemorytype) {
				next();
				if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber);}
				next();
				memType = readString();
				if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName + " before Line " + lineNumber);}
				next();
			} 
		}
		Device device = new Device(devName, base, size, width, attributes, technology, memType);
		while ( sym == sMemorysector || sym == sMemorysectorArray) {
			if(sym == sMemorysector) device.addSector(sector());
			else if(sym == sMemorysectorArray) sectorArray(device);
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return null;}
		if (attributes == 0 || width == 0 || size == 0 || technology == -1 || (technology > 0 && memType == null) || (technology > 0 && device.sector == null)) {
			reporter.error(223, "in " + currentFileName + " Missing attribute while creating device \"" + devName + "\"");
			return null;
		}
		next();
		return device;
	}
	
	private void sectorArray(Device dev){
		if (sym != sMemorysectorArray) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: memorysector, received symbol: " + symToString());}
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		String designator = strBuffer;
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
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
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		if (secSize == -1 || nofSectors == 0 || base == -1) {reporter.error(223, "in " + currentFileName + " Missing attribute by creation sector array in device: " + dev.toString()); return;}
		for (int i = 0; i < nofSectors; i++) dev.addSector(new MemSector(designator + "_" + i, base + i * secSize, secSize));
		next();
	}
	
	private MemSector sector(){
		if (sym != sMemorysector) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: memorysector, received symbol: " + symToString()); return null;}
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber	+ " expected symbol: designator, received symbol: "	+ symToString()); return null;}
		MemSector sec = new MemSector(strBuffer);
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		while (sym == sSectorSize || sym == sBase){
			if (sym == sSectorSize) sec.size = sectorSizeAssignment();
			else sec.address = baseAssignment();		
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return null;}
		next();
		return sec;		
	}
		
	private Segment segment(MemMap mm) {
		if (sym != sSegment) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: segment, received symbol: " + symToString()); return null;}
		next();
		String segDesignator = segmentDesignator();
		String[] fullSegName = segDesignator.split("\\.");
		Segment seg;

		if (fullSegName.length < 2) {reporter.error(222, "in "	+ currentFileName + " at Line "	+ lineNumber + " Device name not given. Sytax error in: " + segDesignator); return null;}
		Device dev = mm.getDeviceByName(fullSegName[0]);
		if(dev == null) {reporter.error(220, fullSegName[0] + " in " + currentFileName + " at line " + lineNumber); return null;}
		seg = new Segment(fullSegName[1], dev);
		seg.attributes = dev.attributes;
		seg.width = dev.width;
		dev.addSegment(seg);
		
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		while (sym == sAttributes || sym == sBase || sym == sWidth || sym == sSize) {
			switch (sym) {
			case sAttributes:
				seg.attributes = attributeAssignment();
				break;
			case sBase:
				seg.address = baseAssignment();
				break;
			case sWidth:
				seg.width = widthAssignment();
				break;
			case sSize:
				seg.size = sizeAssignment();
				break;
			}
		}
		if ((seg.attributes & (1 << dpfSegHeap)) != 0) mm.registerHeapSegment(seg);
		if ((seg.attributes & (1 << dpfSegStack)) != 0) mm.registerStackSegment(seg);
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return null;}
		next();
		return seg;
	}

	private RegisterInit reginit(RegisterInit regInit) {
		if (sym != sReginit) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: reginit, received symbol: " + symToString()); return null;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering reginit section");
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		while (sym == sDesignator) {
			if(dbg) StdStreams.vrb.println("[CONF] Parser: New reginit for register " + strBuffer);
			String rName = strBuffer;
			int val = varAssignment();
			Register r = (Register) Configuration.getBoard().cpu.regs.getItemByName(rName);
			if (r == null) assert false;
			RegisterInit r1 = new RegisterInit(r, val);
			if (regInit == null) regInit = r1;
			else regInit = (RegisterInit) regInit.insertHead(r1);
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: reginit section done");
		next();
		return regInit;
	}
	
	private void system(RunConfiguration runConfig) {
		if (sym != sSystem) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: system, received symbol: "	+ symToString()); return;}
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		while (sym == sSystemtable ) {
			Module root = moduleAssignment();
			while (root != null) {
				runConfig.addSystemModule(root);
				root = (Module)root.next;
			}
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
	}
	
	private void modules(RunConfiguration runConfig) {
		if (sym != sModules) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: modules, received symbol: " + symToString()); return;}
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		while (sym == sKernel || sym == sException || sym == sHeap	|| sym == sDesignator || sym == sDefault) {
			Module mod = moduleAssignment();
			while (mod != null) {
				runConfig.addModule(mod);
				mod = (Module)mod.next;
			}
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		if (runConfig != null) {
			Module def = runConfig.getModuleByName(Configuration.DEFAULT);
			if (def == null) {reporter.error(231, "in " + currentFileName + " at " + runConfig.name); return;}
		}
	}
	
	private Module moduleAssignment() {
		Module root = null;
		HString name = null;
		do {
			if (sym == sComma) next();
			switch (sym) {
			case sKernel:
				name = Configuration.KERNEL;
				next();
				break;
			case sException: 
				name = Configuration.EXCEPTION;
				next();
				break;
			case sHeap:
				name = Configuration.HEAP;
				next();
				break;
			case sDefault:
				name = Configuration.DEFAULT;
				next();
				break;
			case sDesignator:	// must not be system module
				String jname = concatenatedDesignator();
				name = HString.getRegisteredHString(jname);
				break;
			case sSystemtable:
				name = Configuration.SYSTEMTABLE;
				next();
				break;
			default:
				reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " Unexpected symbol: " + symToString() + " by module creation");
				break;
			}
			Module mod = new Module(name);
			if (root == null) root = mod;
			else root.appendTail(mod);
		} while (sym == sComma);
		if (sym != sColon) {reporter.error(209, "in " + currentFileName + " before Line " + lineNumber); return null;}
		do {
			SegmentAssignment assign = null;
			next();
			switch (sym) {
			case sConst:
				name = Configuration.CONST;
				next();
				break;
			case sCode:
				name = Configuration.CODE;
				next();
				break;
			case sVar:
				name = Configuration.VAR;
				next();
				break;
			case sHeap:
				name = Configuration.HEAP;
				next();
				break;
			case sStack:
				name = Configuration.STACK;
				next();
				break;
			case sSysTab:
				name = Configuration.SYSTAB;
				next();
				break;
			default:
				reporter.error(206,	"in " + currentFileName + " at Line " + lineNumber + " expected symbol: contentattribute, received symbol: " + symToString());
				return null;
			}
			if (sym != sAt) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return null;}
			next();
			String desig = segmentDesignator();
			assign = new SegmentAssignment(name, HString.getRegisteredHString(desig));
			Module m = root;
			while (m != null) {
				m.addSegmentAssignment(assign);
				assign = new SegmentAssignment(name, HString.getRegisteredHString(desig));
				m = (Module)m.next;
			}

		} while (sym == sComma);

		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return null;}
		next();
		return root;
	}
	
	private Register register() {
		if (sym != sRegister) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: register, received symbol: " + symToString()); return null;}
		next();
		if (!(sym == sDesignator || sym == sCR || sym == sMSR || sym == sFPSCR)) {
			reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator | CR | MSR | FPSCR, received symbol: " + symToString());
			return null;
		}
		Register reg;
		if (sym != sDesignator)	reg = new Register(HString.getRegisteredHString(symToString()));
		else reg = new Register(HString.getRegisteredHString(strBuffer));
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line " + lineNumber); return null;}
		next();
		while (sym == sType || sym == sAddr || sym == sSize || sym == sRepr) {
			if (sym == sType) reg.regType = typeAssignment();
			if (sym == sAddr) reg.address = addressAssignment();
			if (sym == sSize) reg.size = sizeAssignment();
			if (sym == sRepr) reg.repr = registerRepresentationAssignment();
		}
		if (reg.regType < 0 || reg.size < 0) reporter.error(223, "in " + currentFileName + " Missing attribute in creation of Register: " + reg.name.toString());
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return null;}
		next();
		return reg;
	}
	
	private Register registermap(Register regs) {
		if (sym != sRegistermap) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: registermap, received symbol: " + symToString()); return null;}
		if(dbg) StdStreams.vrb.println("[CONF] Parser: Entering registermap section");
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line " + lineNumber); return null;}
		next();
		Register head = null;
		while (sym == sRegister) {
			Register r = register();
			if (head == null) head = r;
			else head.appendTail(r);
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		return (Register) Item.appendItemList(head, head.getTail(), regs);
	}
	
	private void runConfiguration(Board b) {
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering runconfiguration section");
		if (sym != sRunConf) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: runconfiguration, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering runconfiguration section");
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		RunConfiguration runConfig = new RunConfiguration(strBuffer);
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting run configuration name to: " + strBuffer);
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line " + lineNumber); return;}
		next();
		while (sym == sDescription || sym == sReginit || sym == sSystem || sym == sModules) {
			if (sym == sDescription) runConfig.description = descriptionAssignment();
			else if (sym == sReginit) runConfig.regInits = reginit(null);
			else if (sym == sSystem) system(runConfig);
			else modules(runConfig); // sym == sModules
		}
		Module sysMod = runConfig.system;
		if (sysMod == null) {reporter.error(232, "in system " + currentFileName + " at " + runConfig.name); return;}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		b.addRunConfiguration(runConfig);
		next();
	}

	private void project() {
		if (sym != sProject) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: project, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering project section");
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		currentProject.setProjectName(strBuffer);
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting project name to: " + strBuffer);
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		if (sym != sLibPath) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: libpath, received symbol: " + symToString() + " -> libpath has to be specified first in project"); return;}
		HString[] libPath = libPathAssignment();
		if (dbg) {
			StdStreams.vrb.print("[CONF] Parser: Setting library path to: \"");
			for (int i = 0; i < libPath.length; i++) {
				StdStreams.vrb.print(libPath[i].toString());
				if (i < libPath.length - 1) StdStreams.vrb.print(", ");
			}
			StdStreams.vrb.println("\"");
		}
		Configuration.getActiveProject().createLibs(libPath);
		while (sym == sRootclasses || sym == sBoardType || sym == sOsType	|| sym == sProgrammerType || sym == sProgrammerOpts || sym == sTctFile || sym == sImgFile || sym == sImgFormat) {
			if (sym == sRootclasses) {
				if (dbg) StdStreams.vrb.print("[CONF] Parser: Setting rootclasses");
				Configuration.setRootClasses(rootClassesAssignment());
			} else if (sym == sBoardType) {
				if (dbg) StdStreams.vrb.print("[CONF] Parser: Setting board type to: ");
				Configuration.setBoard(boardTypeAssignment());
				if (dbg) if (Configuration.getBoard() != null) StdStreams.vrb.println(Configuration.getBoard().name);
			} else if (sym == sOsType) {
				if (dbg) StdStreams.vrb.print("[CONF] Parser: Setting os type to: ");
				Configuration.setOS(osTypeAssignment());				
				if (dbg) if (Configuration.getOS() != null) StdStreams.vrb.println(Configuration.getOS().name);
			} else if (sym == sProgrammerType) {
				if (dbg) StdStreams.vrb.print("[CONF] Parser: Setting programmer type to: ");
				Configuration.setProgrammer(programmerTypeAssignment());
				if (dbg) if(Configuration.getProgrammer() != null) StdStreams.vrb.println(Configuration.getProgrammer().name);
			} else if (sym == sProgrammerOpts) {
				// TODO
				next();
			} else if (sym == sImgFile) {
				currentProject.setImgFileName(imgFileAssignment());
				if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting image file to " + currentProject.getImgFileName());
			} else if (sym == sImgFormat) {
				currentProject.setImgFileFormat(imgFileFormatAssignment());
				if (dbg) {
					if(currentProject.imgFileFormat != -1){
						StdStreams.vrb.println("[CONF] Parser: Setting image file format to " + Configuration.formatMnemonics[currentProject.getImgFileFormat()]);
					}
					else{
						StdStreams.vrb.println("[CONF] Parser: Setting image file format failed " + currentProject.imgFileFormat);
					}
				}
			} else { // sym == sTctFile
				currentProject.setTctFileName(tctFileAssignment());
				if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting target command file to " + currentProject.getTctFileName());
			}
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		if (Configuration.getRootClasses() == null || Configuration.getBoard() == null || Configuration.getOS() == null) {
			reporter.error(229,"in " + currentFileName + " \"project\" tags \"rootclasses, boardtype and ostype\" must be defined"); 
			return;
		}
	}

	private void operatingSystem() {
		if (sym != sOperatingSystem) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: operatingsystem, received symbol: " + symToString()); return;}
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Entering operatingsystem section");
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return;}
		String osName = strBuffer;
		if (dbg) StdStreams.vrb.println("[CONF] Parser: Setting os name to: " + osName);
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return;}
		next();
		OperatingSystem os = Configuration.getOS();
		assert os != null;
		while (sym == sDescription || sym == sKernel || sym == sHeapClass || sym == sExceptionBaseClass || sym == sUs || sym == sLowlevel || sym == sException) {
			if (sym == sDescription) os.setDescription(descriptionAssignment());
			else if (sym == sException) os.addExceptionClass(systemClass(true));
			else if (sym == sKernel) {
				Class cls = systemClass(false);
				if (cls != null) os.kernelClass = cls;
			} else if (sym == sHeapClass) {
				Class cls = systemClass(false);
				if (cls != null) os.heapClass = cls;
			} else if (sym == sExceptionBaseClass) os.exceptionBaseClass = systemClass(false);
			else if (sym == sUs) os.usClass = systemClass(false);
			else if (sym == sLowlevel) os.llClass = systemClass(false);
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line " + lineNumber); return;}
		assert os.exceptionBaseClass != null;
		assert os.heapClass != null;
		assert os.kernelClass != null;
		if (os.exceptionBaseClass == null || os.heapClass == null || os.kernelClass == null || os.usClass == null || os.llClass == null) {
			reporter.error(229, "in "	+ currentFileName + " \"operatingsystem\" tags \"kernel, heap, exceptionbaseclass, us and lowlevel\" must be defined"); return;}
		next();
	}

	private Class systemClass(boolean isExceptionClass) {
//		boolean isExceptionBase = false;
		if (!(sym == sKernel || sym == sHeapClass || sym == sExceptionBaseClass || sym == sUs || sym == sLowlevel || sym == sException)) {
			reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + "received symbol: " + symToString()); return null;
		}
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		if (sym != sClass) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + "expected Symbol: class, received symbol: " + symToString()); return null;}
		String name = classAssignment();
		int conditionType = -1;
		if (sym == sCondition) conditionType = conditionAssignment();
		String condName = strBuffer;
		Method meth = null;
		int attr = 0, methAttr = 0;
		while (sym == sAttributes || sym == sMethod) {
			if (sym == sAttributes) {
				attr = attributeAssignment();
			} else {
				Method newMeth = systemMethod();
				if (meth == null) meth = newMeth;
				else meth.appendTail(newMeth);
				methAttr |= newMeth.accAndPropFlags;
				if (!isExceptionClass && meth.offset != -1) {reporter.error(234); return null;}
			}
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		if ((conditionType == sCpuArch && !Configuration.getBoard().cpu.arch.name.equals(HString.getHString(condName))) || 
				(conditionType == sCpuType && !Configuration.getBoard().cpu.name.equals(HString.getHString(condName))) || 
				(conditionType == sBoardType && !Configuration.getBoard().name.equals(HString.getHString(condName)))) return null;	// class does not belong to this arch, cpu or board
		HString hSysClassName = Item.stab.insertCondAndGetEntry(name);
		Class cls = new Class(hSysClassName);		
		RefType.appendRefType(cls);
		cls.methods = meth;
		cls.accAndPropFlags |= attr;
		cls.accAndPropFlags |= 1<<dpfSysPrimitive;	// class is defined in the configuration
		if ((methAttr & (1<<dpfNew)) != 0) cls.accAndPropFlags |= 1<<dpfNew;	// class contains new methods (is heap class)
		if ((methAttr & (1<<dpfExcHnd)) != 0) cls.accAndPropFlags |= 1<<dpfExcHnd;	// // class contains exception handler method
//		if (isExceptionBase) cls.accAndPropFlags |= (1 << dpfExcHnd);
		if (Configuration.dbg) Item.vrb.println("[CONF] create system class: " + name);
		return cls;
	}

	private Method systemMethod() {
		if (sym != sMethod) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: method, received symbol: "	+ symToString()); return null;}
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: "	+ symToString()); return null;}
		Method method = new Method(HString.getRegisteredHString(strBuffer));
		next();
		if (sym != sLBrace) {reporter.error(207, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		next();
		while (sym == sAttributes || sym == sId || sym == sOffset) {
			if (sym == sAttributes) {
				method.accAndPropFlags |= attributeAssignment();
			} else if (sym == sId) {
				next();
				if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return null;}
				next();
				int id = expression();
				method.id = id;
				if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return null;}
				next();
			} else if (sym == sOffset) {
				method.offset = offsetAssignment();
				method.fixed = true;
			}
		}
		if (sym != sRBrace) {reporter.error(202, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		method.accAndPropFlags |= (1 << dpfSysPrimitive);	// method is defined in the configuration
		next();
		return method;
	}

	private String versionAssignment() {
		String s;
		if (sym != sVersion) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: version, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return "";}
		next();
		s = readString();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}

	private String fileDescAssignment() {
		String s;
		if (sym != sDescription) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString());	return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return "";}
		next();
		s = readString();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}

	private int varAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber	+ " expected symbol: designator, received symbol: "	+ symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return res;}
		next();
		res = expression();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
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
			if (sym == sRParen) next();
			else reporter.error(201, "in " + currentFileName + " at Line " + lineNumber);
		} else if (sym == sDesignator) {
			assert currentConsts != null;
			value = ((SystemConstant)currentConsts.getItemByName(strBuffer)).val;
			next();
		} else reporter.error(200, "in " + currentFileName + " at Line " + lineNumber);
		if (isNeg) return -value;
		return value;
	}

	private String descriptionAssignment() {
		String s;
		if (sym != sDescription) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		s = readString();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private String pluginIdAssignment() {
		String s;
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		s = readString();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private String classNameAssignment() {
		String s;
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		s = readString();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private String boardTypeAssignment() {
		String s;
		if (sym != sBoardType) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: boardtype, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		if(sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return "";}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private String osTypeAssignment() {
		String s;
		if (sym != sOsType) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: ostype, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		if(sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return "";}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private String programmerTypeAssignment() {
		String s;
		if (sym != sProgrammerType) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: programmertype, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return "";}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private String imgFileAssignment() {
		String s;
		if (sym != sImgFile) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		s = readString();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private int imgFileFormatAssignment() {
		String s;
		if (sym != sImgFormat) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: description, received symbol: " + symToString()); return -1;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return -1;}
		next();
		if (sym != sDesignator && (sym != sBin && sym!= sHex)) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return -1;}
		if (sym == sDesignator){
			s = strBuffer;
		}
		else{
			if(sym == sBin){
				s = Configuration.formatMnemonics[0]; // "bin"
			}
			else{
				s = Configuration.formatMnemonics[1]; // "hex"
			}
		}
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return -1;}
		next();
		for(int i = 0; i < Configuration.formatMnemonics.length; i++) {
			if(Configuration.formatMnemonics[i].equalsIgnoreCase(s)) return i;
		}
		// Legacy fallback
		if (s.equals("binary")) return Configuration.BIN;
		return -1;
	}
	
	private String cpuTypeAssignment() {
		String s;
		if (sym != sCpuType) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: base, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		if(sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return "";}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}

	private String archAssignment() {
		String s;
		if (sym != sCpuArch) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: cpuarch, received symbol: " + symToString()); return "";}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return "";}
		next();
		if(sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return "";}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}
	
	private int baseAssignment() {
		int res = -1;
		if (sym != sBase) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: base, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return res;}
		next();
		res = expression();
		if(res == Integer.MAX_VALUE) res = -1;
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}
	
	private int technologyAssignment() {
		int res = -1;
		if (sym != sTechnology) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: technology, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return res;}
		next();
		if (sym == sRam) res = 0;
		else if (sym == sFlash)	res = 1;
		else {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: Ram or Flash, received symbol: " + symToString()); return res;}		
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return -1;}
		next();
		return res;
		
	}

	private int nofSectorAssignment() {
		int res = -1;
		if (sym != sNofSectors) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: nofsector, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return res;}
		next();
		res = expression();
		if(res == Integer.MAX_VALUE) res = -1;
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}

	private int widthAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sWidth) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: width, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return res;}
		next();
		res = expression();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}

	private int sizeAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sSize) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: size, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return res;}
		next();
		res = expression();

		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}

	private int sectorSizeAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sSectorSize) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: sectorsize, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return res;}
		next();
		res = expression();
		
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}

	private int attributeAssignment() {
		int res = 0;
		if (sym != sAttributes) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: attributes, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber ); return res;}
		do {
			next();
			switch (sym) {
			case sRead:
				res |= (1 << dpfSegRead);
				break;
			case sWrite:
				res |= (1 << dpfSegWrite);
				break;
			case sConst:
				res |= (1 << dpfSegConst);
				break;
			case sCode:
				res |= (1 << dpfSegCode);
				break;
			case sVar:
				res |= (1 << dpfSegVar);
				break;
			case sHeap:
				res |= (1 << dpfSegHeap);
				break;
			case sStack:
				res |= (1 << dpfSegStack);
				break;
			case sSysTab:
				res |= (1 << dpfSegSysTab);
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
				reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: some attribute, received symbol: "	+ symToString()); return res;
			}
			next();
		} while (sym == sComma);
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}

	private int typeAssignment() {
		int s;
		if (sym != sType) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: type, received symbol: " + symToString()); return sUndef;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return sUndef;}
		next();
		if (sym == sGPR || sym == sFPR || sym == sSPR || sym == sIOR || sym == sMSR || sym == sCR || sym == sFPSCR) s = sym;
		else {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " unexpected symbol: " + symToString()); return sUndef;}
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}

	private int registerRepresentationAssignment() {
		int s = sUndef;
		if (sym != sRepr) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: repr, received symbol: " + symToString()); return s;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return s;}
		next();
		if (sym != sDez && sym != sBin && sym != sHex) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: dez | bin | hex, received symbol: "	+ symToString()); return s;}
		s = sym;
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return s;}
		next();
		return s;
	}

	private int addressAssignment() {
		int res = Integer.MAX_VALUE;
		if (sym != sAddr) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: addr, received symbol: " + symToString()); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "+ lineNumber); return res;}
		next();
		res = expression();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}
	
	private int offsetAssignment() {
		int res = -1;
		if (sym != sOffset) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: addr, received symbol: " + symToString() ); return res;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return res;}
		next();
		res = expression();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return res;}
		next();
		return res;
	}

	private String concatenatedDesignator() {
		StringBuffer sb = new StringBuffer();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return sb.toString();}
		sb.append(strBuffer);
		next();
		while (sym == sDot || sym == sDiv) {
			sb.append("/");
			next();
			if (sym == sMul) sb.append("*");
			else if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return sb.toString();}
			else sb.append(strBuffer);
			next();
		}
		return sb.toString();
	}

	private String classAssignment() {
		String str = null;
		if (sym != sClass) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected: rootclasses, received symbol: " + symToString()); return null;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return null;}
		next();
		str = readString();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return null;}
		next();
		return str;
	}
	
	private int conditionAssignment() {
		int conditionType = -1;
		if (sym != sCondition) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected: condition, received symbol: " + symToString()); return -1;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return -1;}
		next();
		if (sym == sBoardType) conditionType = sBoardType;
		else if (sym == sCpuArch) conditionType = sCpuArch;
		else if (sym == sCpuType) conditionType = sCpuType;
		else {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected: cpuarch | cputype, received symbol: " + symToString()); return -1;}
		next();
		if (sym != sColon) {reporter.error(206, "in " + currentFileName	+ " at Line " + lineNumber + " expected: colon (:), received symbol: " + symToString()); return -1;}
		next();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return -1;}
		next();
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return -1;}
		next();
		return conditionType;
	}

	private String segmentDesignator() {
		StringBuffer sb = new StringBuffer();
		if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return sb.toString();}
		sb.append(strBuffer);
		next();
		while (sym == sDot) {
			next();
			if (sym != sDesignator) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected symbol: designator, received symbol: " + symToString()); return sb.toString();}
			sb.append(".");
			sb.append(strBuffer);
			next();
		}
		return sb.toString();
	}

	private HString[] rootClassesAssignment() {
		String[] tempList = new String[Configuration.maxNofRootClasses];
		int count = 0;
		if (sym != sRootclasses) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected: rootclasses, received symbol: " + symToString()); return null;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line "	+ lineNumber); return null;}
		do {
			next();
			tempList[count] = readString().replace('.', '/');
			tempList[count] = tempList[count].replace('\\', '/');
			count++;
		} while (sym == sComma);
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return null;}
		HString[] rootClasses = new HString[count];
		while (count > 0) {
			count--;
			rootClasses[count] = HString.getRegisteredHString(tempList[count]);
		}
		next();
		return rootClasses;
	}

	private HString[] libPathAssignment() {
		String[] tempList = new String[Configuration.maxNofLibPaths];
		int count = 0;
		if (sym != sLibPath) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected: libpath, received symbol: " + symToString()); return null;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return null;}
		String str;
		do {
			next();
			str = readString();
			if(str.length() > 0 && str.charAt(str.length() - 1) != '/') {
				if(!str.endsWith(".jar")) str = str + '/';
			}
			tempList[count] = str.replace('\\', '/');
			count++;
		} while (sym == sComma);
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName + " before Line " + lineNumber); return null;}
		HString[] libPaths = new HString[count];
		while (count > 0) {
			count--;
			libPaths[count] = HString.getRegisteredHString(tempList[count]);
		}
		next();
		return libPaths;
	}

	private String tctFileAssignment() {
		String tctFileName = null;
		if (sym != sTctFile) {reporter.error(206, "in " + currentFileName + " at Line " + lineNumber + " expected: tctFile, received symbol: " + symToString()); return null;}
		next();
		if (sym != sEqualsSign) {reporter.error(210, "in " + currentFileName + " at Line " + lineNumber); return null;}
		next();
		if (sym == sNone) {
			tctFileName = null;
			next();
		}
		else if (sym == sDefault) {
			tctFileName = Configuration.defaultTctFileName;
			next();
		}
		else if(sym == sQuotationMark){
			String str = readString();
			if(str.length() > 0) tctFileName = str;
			else tctFileName = null;
		}
		if (sym != sSemicolon) {reporter.error(209, "in " + currentFileName	+ " before Line " + lineNumber); return null;}
		next();
		return tctFileName;
	}
	
}
