package ch.ntb.inf.deep.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.host.ErrorReporter;

public class Parser {

	// private static final boolean DEBUG = true;

	// -------- Invalide Symbol:
	private static final short g0 = 0, sUndef = g0;
	// -------- Bracket: "(", ")", "{", "}"
	private static final short g1 = g0 + 1, sLParen = g1, sRParen = g1 + 1,
			sLBrace = g1 + 2, sRBrace = g1 + 3, sLBracket = g1 + 4,
			sRBracket = g1 + 5;
	// -------- Punctuation mark ",", '"', ";", "."
	private static final short g2 = g1 + 6, sComma = g2,
			sQuotationMark = g2 + 1, sSemicolon = g2 + 2, sDot = g2 + 3;
	// -------- Math op.: "*", "/", "+", "-"
	private static final short g3 = g2 + 4, sMul = g3, sDiv = g3 + 1,
			sPlus = g3 + 2, sMinus = g3 + 3;
	// -------- Assignment op.: "="
	private static final short g4 = g3 + 4, sEqualsSign = g4, sAt = g4 + 1;
	// -------- Access attribute : "read", "write"
	private static final short g5 = g4 + 2, sRead = g5, sWrite = g5 + 1;
	// -------- Content attribute: "const", "code", "var", "heap", "stack",
	// "sysconst"
	private static final short g6 = g5 + 2, sConst = g6, sCode = g6 + 1,
			sVar = g6 + 2, sHeap = g6 + 3, sStack = g6 + 4, sSysConst = g6 + 5;
	// -------- Register type: "GPR", "FPR", "SPR"
	private static final short g7 = g6 + 6, sGPR = g7, sFPR = g7 + 1,
			sSPR = g7 + 2;
	// -------- Register representation: "HEX", "DEZ", "BIN", "FLOAT"
	private static final short g8 = g7 + 3, sHex = g8, sDez = g8 + 1,
			sBin = g8 + 2, sFloat = g8 + 3;
	// -------- Assignment keywords; "version", "description", "import",
	// "device", "attributes", "width", "size", "base", "systemtable",
	// "programmer", "rootclasses", "segmentsize", "arraysize", "nofsegments",
	// "xx", "xxx"
	// "kernel", "heap", "interrupt", "exception", "addr", "type", "repr",
	// "libpath", "debuglevel", "printlevel"
	private static final short g9 = g8 + 4, sVersion = g9,
			sDescription = g9 + 1, sImport = g9 + 2, sAttributes = g9 + 3,
			sWidth = g9 + 4, sSize = g9 + 5, sBase = g9 + 6,
			sSystemtable = g9 + 7, sRootclasses = g9 + 8,
			sSegmentsize = g9 + 9, sArraysize = g9 + 10,
			sNofsegements = g9 + 11, sKernel = g9 + 12, sInterrupt = g9 + 13,
			sException = g9 + 14, sProgram = g9 + 15, sXx = g9 + 16,
			sXxx = g9 + 17, sAddr = g9 + 18, sType = g9 + 19, sRepr = g9 + 20,
			sLibPath = g9 + 21, sDebugLevel = g9 + 22, sPrintLevel = g9 + 23;
	// -------- Block keywords: "meta", "constants", " device", "reginit",
	// "segment", "memorymap", "map", "modules", "sysmodules", "project",
	// "segmentarray", register", operatingsystem
	private static final short g10 = g9 + 24, sMeta = g10,
			sConstants = g10 + 1, sDevice = g10 + 2, sReginit = g10 + 3,
			sSegment = g10 + 4, sMemorymap = g10 + 5, sMap = g10 + 6,
			sModules = g10 + 7, sSysmodules = g10 + 8, sProject = g10 + 9,
			sSegmentarray = g10 + 10, sRegistermap = g10 + 11,
			sRegister = g10 + 12, sOperatingSystem = g10 + 13;
	// -------- Designator, IntNumber,
	private static final short g11 = g10 + 14, sDesignator = g11,
			sNumber = g11 + 1;
	// -------- End of file: EOF
	private static final short g12 = g11 + 2, sEndOfFile = g12;
	// -------- Error codes
	private static final short errDigitExp = 101, errRParenExp = 102,
			errRBraceExp = 103, errRBracketExp = 104,
			errQuotationMarkExp = 105, errIOExp = 106,
			errUnexpectetSymExp = 107, errLBraceExp = 108,
			errLBracketExp = 109, errSemicolonMissExp = 110,
			errAssignExp = 111;

	private static int nOfErrors;
	private static int sym;
	private static String strBuffer;
	private static int chBuffer;
	private static int intNumber;
	private static ErrorReporter reporter = ErrorReporter.reporter;

	private BufferedReader configFile;
	private ArrayList<String> importList;

	public static void main(String[] args) {

		Parser p = new Parser();
		p.parseAndCreateConfig("D:/work/Crosssystem/deep/rsc/MyProject.deep");
		Dbg.vrb.println();
		Dbg.vrb.println("Config read with " + nOfErrors + " error(s)");
	}

	public void parseAndCreateConfig(String file){
		try {
			importList = new ArrayList<String>();
			configFile = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			reporter.error(errIOExp, e.getMessage());
		}
		Config();
	}

	private int Config() {
		// read first Symbol
		next();

		Meta();

		while (sym != sEndOfFile) {
			switch (sym) {
			case sConstants:
				next();
				Constants();
				break;
			case sMemorymap:
				MemoryMap();
				break;
			case sRegistermap:
				Registermap();
				break;
			case sSysmodules:
				SystemModules();
				break;
			case sReginit:
				RegInit();
				break;
			case sProject:
				Project();
				break;
			case sOperatingSystem:
				OperatingSystem();
				break;
			default:
				nOfErrors++;
				reporter.error(errUnexpectetSymExp, "expectet symbol : constants | memorymap | registermap | sysmodules | reginit | project | operatingsystem, received symbol: " + symToString() + "\n" );
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
			reporter.error(errQuotationMarkExp,
					"expected symbol: \", received symbol: " + symToString()
							+ " ");
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
				reporter.error(errQuotationMarkExp,
						"expected symbol: \", received symbol: "
								+ symToString() + " ");
				return "";
			}
		} catch (IOException e) {
			reporter.error(errIOExp, e.getMessage());
		}
		next();
		return sb.toString();
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
				next();
				break; // Ignore comments
			case '\t':
			case ' ':
			case '\r':
			case '\n':
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
			default:
				String s;
				StringBuffer sb;
				if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
						|| ch == '_') {// Designator or Keyword
					sb = new StringBuffer();
					do {
						sb.append((char) ch);
						ch = configFile.read();
					} while ((ch >= 'a' && ch <= 'z')
							|| (ch >= 'A' && ch <= 'Z')
							|| (ch >= '0' && ch <= '9') || ch == '_');
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
							intNumber = Integer.decode(sb.toString());
							break;
						} else if (ch == ';') {
							chBuffer = ch;
							return;
						} else {// check if it is a digit
							if (!(ch >= '0' && ch <= '9')) {
								nOfErrors++;
								reporter.error(errDigitExp, "Invalide Number");
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

	private static boolean isKeyword(String str) {
		String temp = str.toLowerCase();// only for keywords for which the case
		// sensitivity will not be considered
		sym = sDesignator;
		switch (temp.charAt(0)) {
		case 'a':
			if (str.equals("arraysize")) {
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
			} else if (temp.equals("bin")) {
				sym = sBin;
				return true;
			}
			break;
		case 'c':
			if (str.equals("code")) {
				sym = sCode;
				return true;
			} else if (str.equals("const")) {
				sym = sConst;
				return true;
			} else if (str.equals("constants")) {
				sym = sConstants;
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
			} else if (str.equals("debuglevel")) {
				sym = sDebugLevel;
				return true;
			}
			break;
		case 'e':
			if (str.equals("exception")) {
				sym = sException;
				return true;
			}
			break;
		case 'f':
			if (temp.equals("float")) {
				sym = sFloat;
				return true;
			} else if (temp.equals("fpr")) {
				sym = sFPR;
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
			if (str.equals("import")) {
				sym = sImport;
				return true;
			} else if (str.equals("interrupt")) {
				sym = sInterrupt;
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
			}
		case 'm':
			if (str.equals("map")) {
				sym = sMap;
				return true;
			} else if (str.equals("modules")) {
				sym = sModules;
				return true;
			} else if (str.equals("meta")) {
				sym = sMeta;
				return true;
			} else if (str.equals("memorymap")) {
				sym = sMemorymap;
				return true;
			}
			break;
		case 'n':
			if (str.equals("nofsegments")) {
				sym = sNofsegements;
				return true;
			}
			break;
		case 'o':
			if (str.equals("operatingsystem")) {
				sym = sOperatingSystem;
				return true;
			}
		case 'p':
			if (str.equals("project")) {
				sym = sProject;
				return true;
			} else if (str.equals("program")) {
				sym = sProgram;
				return true;
			} else if (str.equals("printlevel")) {
				sym = sPrintLevel;
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
			} else if (str.equals("sysconst")) {
				sym = sSysConst;
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
			} else if (str.equals("sysmodules")) {
				sym = sSysmodules;
				return true;
			} else if (str.equals("systemtable")) {
				sym = sSystemtable;
				return true;
			}
			break;
		case 't':
			if (str.equals("type")) {
				sym = sType;
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
		case 'x':
			if (str.equals("xx")) {
				sym = sXx;
				return true;
			} else if (str.equals("xxx")) {
				sym = sXxx;
				return true;
			}
		default:
			return false;
		}
		return false;
	}

	private static String symToString() {
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
		case sSysConst:
			return "sysconst";
		case sGPR:
			return "gpr";
		case sFPR:
			return "fpr";
		case sSPR:
			return "spr";
		case sHex:
			return "hex";
		case sDez:
			return "dez";
		case sBin:
			return "bin";
		case sFloat:
			return "float";
		case sVersion:
			return "version";
		case sDescription:
			return "description";
		case sImport:
			return "import";
		case sAttributes:
			return "attributes";
		case sWidth:
			return "width";
		case sSize:
			return "size";
		case sBase:
			return "base";
		case sSystemtable:
			return "systemtable";
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
		case sInterrupt:
			return "interrupt";
		case sException:
			return "exception";
		case sProgram:
			return "program";
		case sXx:
			return "xx";
		case sXxx:
			return "xxx";
		case sAddr:
			return "addr";
		case sType:
			return "type";
		case sRepr:
			return "repr";
		case sLibPath:
			return "libpath";
		case sDebugLevel:
			return "debuglevel";
		case sPrintLevel:
			return "printlevel";
		case sMeta:
			return "meta";
		case sConstants:
			return "constants";
		case sDevice:
			return "device";
		case sReginit:
			return "reginit";
		case sSegment:
			return "segment";
		case sMemorymap:
			return "memorymap";
		case sMap:
			return "map";
		case sModules:
			return "modules";
		case sSysmodules:
			return "sysmodules";
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
		case sDesignator:
			return "designator";
		case sNumber:
			return "number";
		case sEndOfFile:
			return "endoffile";
		default:
			return "";
		}
	}

	private void Meta() {
		if (sym != sMeta) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: meta, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		Dbg.vrb.println("version = " + VersionAssignment() + ";");
		Dbg.vrb.println("description = " + FileDescAssignment() + ";");
		if (sym == sImport) {
			ImportAssignment();
			//context save of static sym
			int cs = sym;
			Parser p2;
			for (int i = 0; i < importList.size(); i++) {
				p2 = new Parser();
				p2.parseAndCreateConfig(importList.get(i));
			}
			//restore context save
			sym = cs;
		}
		Dbg.vrb.println();
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		Dbg.vrb.println();
		next();
	}

	private void Constants() {
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		Dbg.vrb.println("Constants:");
		while (sym == sDesignator) {
			Dbg.vrb.println("  " + strBuffer + " = " + VarAssignment());
		}
		Dbg.vrb.println();
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void MemoryMap() {
		if (sym != sMemorymap) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: memorymap, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		Dbg.vrb.println("MemoryMap:\n");
		while (sym == sDevice || sym == sSegment || sym == sReginit
				|| sym == sModules) {
			if (sym == sDevice) {
				Device();
			} else if (sym == sSegment) {
				Segment();
			} else if (sym == sReginit) {
				RegInit();
			} else {// sModules
				Modules();
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void Device() {
		if (sym != sDevice) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: device, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return;
		}
		// TODO create Device
		Dbg.vrb.println("Device " + strBuffer + ":");
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		while (sym == sAttributes || sym == sBase || sym == sWidth
				|| sym == sSize) {
			if (sym == sAttributes) {
				int[] attr = AttributeAssignment();
				Dbg.vrb.print("  Attributes =");
				for (int i = 0; i < attr.length; i++) {
					Dbg.vrb.print(" " + symToString(attr[i]));
				}
				Dbg.vrb.println(";");
			} else if (sym == sBase) {
				Dbg.vrb.println("  Base = " + BaseAssignment());
			} else if (sym == sWidth) {
				Dbg.vrb.println("  Width = " + WidthAssignment());
			} else {// sSize
				Dbg.vrb.println("  Size = " + SizeAssignment());
			}
		}
		Dbg.vrb.println();
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void Segment() {
		if (sym != sSegment) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: segment, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		Dbg.vrb.println("Segment: " + SegmentDesignator());
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		while (sym == sDevice || sym == sAttributes || sym == sBase
				|| sym == sWidth || sym == sSize || sym == sSegmentarray
				|| sym == sSegment) {
			switch (sym) {
			case sDevice:
				Dbg.vrb.println("  device = " + DeviceAssignment());
				break;
			case sAttributes:
				int[] attr = AttributeAssignment();
				Dbg.vrb.print("  Attributes =");
				for (int i = 0; i < attr.length; i++) {
					Dbg.vrb.print(" " + symToString(attr[i]));
				}
				Dbg.vrb.println(";");
				break;
			case sBase:
				Dbg.vrb.println("  Base = " + BaseAssignment());
				break;
			case sWidth:
				Dbg.vrb.println("  Width = " + WidthAssignment());
				break;
			case sSize:
				Dbg.vrb.println("  Size = " + SizeAssignment());
				break;
			case sSegmentarray:
				SegmentArray();
				break;
			case sSegment:
				Segment();
				break;
			default:
				break;
			}
		}
		Dbg.vrb.println();
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void SegmentArray() {
		if (sym != sSegmentarray) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: segmentarray, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return;
		}
		Dbg.vrb.print("SegmentArray " + strBuffer);
		next();
		if (sym != sLBracket) {
			nOfErrors++;
			reporter.error(errLBracketExp,
					"expected symbol: [, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		if (sym == sXx) {
			// TODO Format Auto- numeration example s00, s01, s02
			Dbg.vrb.print("[xx");
		}
		if (sym == sXxx) {
			// TODO Format Auto- numeration example s000, s001, s002
			Dbg.vrb.print("[xxx");
		}
		next();
		if (sym != sRBracket) {
			nOfErrors++;
			reporter.error(errRBracketExp,
					"expected symbol: ], received symbol: " + symToString()
							+ " ");
			return;
		}
		Dbg.vrb.println("]");
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		Dbg.vrb.println("    segmentsize = " + SegmentSizeAssignment());

		while (sym == sArraysize || sym == sWidth || sym == sNofsegements) {
			if (sym == sArraysize) {
				Dbg.vrb.println("    arraysize = " + ArraySizeAssignment());
			} else if (sym == sWidth) {
				Dbg.vrb.println("    width = " + WidthAssignment());
			} else {// sNofSegments
				Dbg.vrb.println("   nofsegments = " + NofSegmentAssignment());
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void RegInit() {
		if (sym != sReginit) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: reginit, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		Dbg.vrb.println("RegInit:");
		while (sym == sDesignator) {
			Dbg.vrb
					.println("  register " + strBuffer + " = "
							+ RegAssignment());
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void Modules() {
		if (sym != sModules) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: modules, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		Dbg.vrb.println("Modules:");
		next();
		while (sym == sMap) {
			Map();
			switch (sym) {
			case sKernel:
				Dbg.vrb.println("kernel");
				next();
				break;
			case sInterrupt:
				Dbg.vrb.println("interrupt");
				next();
				break;
			case sException:
				Dbg.vrb.println("exception");
				next();
				break;
			case sHeap:
				Dbg.vrb.println("heap");
				next();
				break;
			case sProgram:
				Dbg.vrb.println("program");
				next();
				break;
			default:
				Dbg.vrb.println(readString());
				break;
			}
			if (sym != sSemicolon) {
				nOfErrors++;
				reporter.error(errSemicolonMissExp,
						"expected symbol: ;, received symbol: " + symToString()
								+ " ");
				return;
			}
			next();
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void Map() {
		if (sym != sMap) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: map, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		Dbg.vrb.print("map { ");
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		do {
			next();
			switch (sym) {
			case sConst:
				Dbg.vrb.print("const");
				next();
				break;
			case sCode:
				Dbg.vrb.print("code");
				next();
				break;
			case sVar:
				Dbg.vrb.print("var");
				next();
				break;
			case sHeap:
				Dbg.vrb.print("heap");
				next();
				break;
			case sStack:
				Dbg.vrb.print("stack");
				next();
				break;
			case sSysConst:
				Dbg.vrb.print("sysconst");
				next();
				break;
			default:
				nOfErrors++;
				reporter.error(errUnexpectetSymExp,
						"expected symbol: modules, received symbol: "
								+ symToString() + " ");
				return;
			}
			if (sym != sAt) {
				nOfErrors++;
				reporter.error(errAssignExp,
						"expected symbol: @, received symbol: " + symToString()
								+ " ");
				return;
			}
			Dbg.vrb.print("@");
			next();
			Dbg.vrb.print(SegmentDesignator() + ", ");
		} while (sym == sComma);
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		Dbg.vrb.println("}");
		next();
	}

	private void Registermap() {
		if (sym != sRegistermap) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: registermap, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		Dbg.vrb.print("registermap: ");
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		while (sym == sRegister) {
			Register();
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void Register() {
		if (sym != sRegister) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: register, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return;
		}
		Dbg.vrb.println("register " + strBuffer);
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		while (sym == sType || sym == sAddr || sym == sSize || sym == sRepr) {
			Dbg.vrb.println("  type = " + symToString(RegTypeAssignment()));
			Dbg.vrb.println("  addr = " + AddressAssignment());
			Dbg.vrb.println("  size = " + SizeAssignment());

			if (sym == sRepr) {
				Dbg.vrb.println("  repr = "
						+ symToString(RegisterRepresentationAssignment()));
			}
		}
		Dbg.vrb.println();
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void SystemModules() {
		if (sym != sSysmodules) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: sysmodules, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return;
		}
		Dbg.vrb.println("systemmodule " + strBuffer + ":");
		next();
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		ArrayList<String> systab = SystemtableAssignment();
		Dbg.vrb.print("  systemtable ");
		for (int i = 0; i < systab.size(); i++) {
			Dbg.vrb.print("@ " + systab.get(i) + "; ");
		}
		Dbg.vrb.println();
		Modules();
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
	}

	private void Project() {
		if (sym != sProject) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: project, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		Dbg.vrb.println("project:");
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		while (sym == sRootclasses || sym == sLibPath || sym == sSysmodules
				|| sym == sDebugLevel || sym == sPrintLevel) {
			if (sym == sRootclasses) {
				ArrayList<String> classes = RootClassesAssignment();
				Dbg.vrb.print("  rootclasses = ");
				for (int i = 0; i < classes.size(); i++) {
					Dbg.vrb.print(classes.get(i) + "; ");
				}
				Dbg.vrb.println();
			} else if (sym == sLibPath) {
				Dbg.vrb.println("  libpath = " + LibPathAssignment());
			} else if (sym == sSysmodules) {
				Dbg.vrb.println("  sysmodules = " + SystemModulesAssignment());
			} else if (sym == sDebugLevel) {
				Dbg.vrb.println("  debuglevel = " + DebugLevelAssignment());
			} else if (sym == sPrintLevel) {
				Dbg.vrb.println("  printlevel = " + PrintLevelAssignment());
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		Dbg.vrb.println();
		next();
	}

	private void OperatingSystem() {
		if (sym != sOperatingSystem) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: operatingsystem, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		Dbg.vrb.println("operatingsystem: ");
		if (sym != sLBrace) {
			nOfErrors++;
			reporter.error(errLBraceExp,
					"expected symbol: {, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		while (sym == sKernel || sym == sHeap || sym == sInterrupt
				|| sym == sException) {
			if (sym == sKernel) {
				Dbg.vrb.println("  kernel = " + KernelAssignment());
			} else if (sym == sHeap) {
				Dbg.vrb.println("  heap = " + HeapAssignment());
			} else if (sym == sInterrupt) {
				Dbg.vrb.println("  interrupt = " + InterruptAssignment());
			} else {
				Dbg.vrb.println("  exception = " + ExceptionAssignment());
			}
		}
		if (sym != sRBrace) {
			nOfErrors++;
			reporter.error(errRBraceExp,
					"expected symbol: }, received symbol: " + symToString()
							+ " ");
			return;
		}
		Dbg.vrb.println();
		next();
	}

	private String VersionAssignment() {
		String s;
		if (sym != sVersion) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: version, received symbol: "
							+ symToString() + " ");
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return "";
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private String FileDescAssignment() {
		String s;
		if (sym != sDescription) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: description, received symbol: "
							+ symToString() + " ");
			return "";
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return "";
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private void ImportAssignment() {
		if (sym != sImport) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: import, received symbol: "
							+ symToString() + " ");
			return;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return;
		}
		do {
			next();
			importList.add(readString());
		} while (sym == sComma);
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return;
		}
		next();
		return;
	}

	private int VarAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;

	}

	private int Expression() {
		int value = Term();
		while (sym == sPlus || sym == sMinus) {
			int operator = sym;
			next();
			if (operator == sPlus) {
				value = value + Term();
			} else if (operator == sMinus) {
				value = value - Term();
			}
		}
		return value;
	}

	private int Term() {
		int value = Factor();
		while (sym == sMul || sym == sDiv) {
			int operator = sym;
			next();
			if (operator == sMul) {
				value *= Factor();
			} else if (operator == sDiv) {
				value /= Factor();
			}
		}
		return value;
	}

	private int Factor() {
		int value = 1;
		if (sym == sNumber) {
			value = intNumber;
			next();
		} else if (sym == sLParen) {
			next();
			value = Expression();
			if (sym == sRParen) {
				next();
			} else {
				nOfErrors++;
				reporter.error(errRParenExp,
						"expected symbol: ), received symbol: " + symToString()
								+ " ");
			}
		} else if (sym == sDesignator) {
			// TODO value = getVariable(strBuffer);
			next();
		} else {
			nOfErrors++;
			reporter.error(errDigitExp,
					"expected symbol: number, received symbol: "
							+ symToString() + " ");
		}
		return value;
	}

	private String DeviceAssignment() {
		String s = "";
		if (sym != sDevice) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: device, received symbol: "
							+ symToString() + " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return s;
		}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private int BaseAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sBase) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: base, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int WidthAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sWidth) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: width, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int SizeAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sSize) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: size, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int SegmentSizeAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sSegmentsize) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: segmentsize, received symbol: "
							+ symToString() + " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int ArraySizeAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sArraysize) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: arraysize, received symbol: "
							+ symToString() + " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int NofSegmentAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sNofsegements) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: nofsegments, received symbol: "
							+ symToString() + " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int RegAssignment() {
		return VarAssignment();
	}

	private int[] AttributeAssignment() {
		int[] temp = new int[8];
		int[] res = temp;
		int count = 0;
		if (sym != sAttributes) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: attributes, received symbol: "
							+ symToString() + " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		do {
			next();
			if (sym == sRead || sym == sWrite || sym == sConst || sym == sCode
					|| sym == sVar || sym == sHeap || sym == sStack
					|| sym == sSysConst) {
				res[count] = sym;
				count++;
				next();
			} else {
				nOfErrors++;
				reporter.error(errUnexpectetSymExp,
						"expected symbol: attributes, received symbol: "
								+ symToString() + " ");
				return res;
			}
		} while (sym == sComma);
		res = new int[count];
		for (int i = 0; i < count; i++) {
			res[i] = temp[i];
		}
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int RegTypeAssignment() {
		int s;
		if (sym != sType) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: type, received symbol: " + symToString()
							+ " ");
			return sUndef;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return sUndef;
		}
		next();
		if (sym == sGPR || sym == sFPR || sym == sSPR) {
			s = sym;
		} else {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: gpr | fpr | spr, received symbol: "
							+ symToString() + " ");
			return sUndef;
		}
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private int RegisterRepresentationAssignment() {
		int s = sUndef;
		if (sym != sRepr) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: repr, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		if (sym != sDez && sym != sBin && sym != sHex) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: dez | bin | hex, received symbol: "
							+ symToString() + " ");
			return s;
		}
		s = sym;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private int AddressAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sAddr) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: addr, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp,
					"expected symbol: =, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		res = Expression();

		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private String SegmentDesignator() {
		StringBuffer sb = new StringBuffer();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return sb.toString();
		}
		sb.append(strBuffer);
		next();
		while (sym == sDot) {
			next();
			if (sym != sDesignator) {
				nOfErrors++;
				reporter.error(errUnexpectetSymExp,
						"expected symbol: designator, received symbol: "
								+ symToString() + " ");
				return sb.toString();
			}
			sb.append(".");
			sb.append(strBuffer);
			next();
		}
		return sb.toString();
	}

	private ArrayList<String> SystemtableAssignment() {
		ArrayList<String> tempList = new ArrayList<String>();
		if (sym != sSystemtable) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected: systemtable, received symbol: " + symToString()
							+ " ");
			return tempList;
		}
		do {
			next();
			if (sym != sAt) {
				nOfErrors++;
				reporter.error(errAssignExp, "expected: @, received symbol: "
						+ symToString() + " ");
				return tempList;
			}
			next();
			tempList.add(SegmentDesignator());
		} while (sym == sComma);
		next();
		return tempList;
	}

	private ArrayList<String> RootClassesAssignment() {
		ArrayList<String> tempList = new ArrayList<String>();
		if (sym != sRootclasses) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected: rootclasses, received symbol: " + symToString()
							+ " ");
			return tempList;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return tempList;
		}
		do {
			next();
			tempList.add(readString());
		} while (sym == sComma);
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return tempList;
		}
		next();
		return tempList;
	}

	private String LibPathAssignment() {
		String s = "";
		if (sym != sLibPath) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected: libpath, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return s;
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private String SystemModulesAssignment() {
		String s = "";
		if (sym != sSysmodules) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected: sysmodules, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return s;
		}
		next();
		if (sym != sDesignator) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected symbol: designator, received symbol: "
							+ symToString() + " ");
			return s;
		}
		s = strBuffer;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private String KernelAssignment() {
		String s = "";
		if (sym != sKernel) {
			nOfErrors++;
			reporter
					.error(errUnexpectetSymExp,
							"expected: kernel, received symbol: "
									+ symToString() + " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return s;
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private String HeapAssignment() {
		String s = "";
		if (sym != sHeap) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected: heap, received symbol: " + symToString() + " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return s;
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private String InterruptAssignment() {
		String s = "";
		if (sym != sInterrupt) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp,
					"expected: interrupt, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return s;
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private String ExceptionAssignment() {
		String s = "";
		if (sym != sException) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "expected: exception"
					+ ", received symbol: " + symToString() + " ");
			return s;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return s;
		}
		next();
		s = readString();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return s;
		}
		next();
		return s;
	}

	private int DebugLevelAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sDebugLevel) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "expected: exception"
					+ ", received symbol: " + symToString() + " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return res;
		}
		next();
		if (sym != sNumber) {
			nOfErrors++;
			reporter.error(errDigitExp, "expected: number, received symbol: "
					+ symToString() + " ");
			return res;
		}
		res = intNumber;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

	private int PrintLevelAssignment() {
		int res = Integer.MIN_VALUE;
		if (sym != sPrintLevel) {
			nOfErrors++;
			reporter.error(errUnexpectetSymExp, "expected: printlevel"
					+ ", received symbol: " + symToString() + " ");
			return res;
		}
		next();
		if (sym != sEqualsSign) {
			nOfErrors++;
			reporter.error(errAssignExp, "expected: =, received symbol: "
					+ symToString() + " ");
			return res;
		}
		next();
		if (sym != sNumber) {
			nOfErrors++;
			reporter.error(errDigitExp, "expected: number, received symbol: "
					+ symToString() + " ");
			return res;
		}
		res = intNumber;
		next();
		if (sym != sSemicolon) {
			nOfErrors++;
			reporter.error(errSemicolonMissExp,
					"expected symbol: ;, received symbol: " + symToString()
							+ " ");
			return res;
		}
		next();
		return res;
	}

}
