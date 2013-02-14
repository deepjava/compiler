package ch.ntb.inf.deep.comp.targettest.statements;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * NTB 12.06.2009
 * 
 * @author Jan Mrnak
 * 
 * 
 *         Changes:
 */
@MaxErrors(100)
public class IfTest {
	public byte[] data = new byte[4];
	public int start, end, bufLen;
	public boolean full = false;
	
	
	// normal variants
	public static int normal(boolean c) {
		int res = -1;
		if (c)
			res = 11;
		return res;
	}

	public static int inLine(boolean c) {
		int res = -1;
		if (c)
			;
		else
			res = 12;
		return res;
	}

	public static int noStatement(boolean c) {
		int res = -1;
		if (c)
			;
		return res;
	}

	@Test
	// Test normal variants
	public static void testNormal() {
		int res;
		res = normal(true);
		Assert.assertEquals("normal1", 11, res);
		res = normal(false);
		Assert.assertEquals("normal2", -1, res);

		res = inLine(true);
		Assert.assertEquals("inLine1", -1, res);
		res = inLine(false);
		Assert.assertEquals("inLine2", 12, res);

		res = noStatement(true);
		Assert.assertEquals("noStatement1", -1, res);
		res = noStatement(false);
		Assert.assertEquals("noStatement2", -1, res);
		CmdTransmitter.sendDone();
	}

	// and, or variants
	public static int brace(boolean c) {
		int res = -1;
		if (c) {
			res = 11;
		} else {
			res = 12;
		}
		return res;
	}

	public static int and(boolean ca, boolean cb) {
		int res = -1;
		if (ca && cb) {
			res = 11;
		} else {
			res = 12;
		}
		return res;
	}

	public static int andOr1(boolean ca, boolean cb, boolean cc, boolean cd) {
		int res = -1;
		if (ca && cb) {
			if (cc || cd) {
				res = 11;
			}
		}
		return res;
	}

	public static int andOr2(boolean ca, boolean cb, boolean cc, boolean cd) {
		int res = -1;
		if (ca && cb) {
			if (cc || cd) {
				res = 11;
			} else {
				res = 12;
			}
		}
		return res;
	}

	public static int orAnd1(boolean ca, boolean cb, boolean cc, boolean cd) {
		int res = -1;
		if (ca || cb) {
			if (cc && cd) {
				res = 11;
			}
		}
		return res;
	}

	public static int orAnd2(boolean ca, boolean cb, boolean cc, boolean cd) {
		int res = -1;
		if (ca || cb) {
			if (cc && cd) {
				res = 11;
			} else {
				res = 12;
			}
		}
		return res;
	}

	@Test
	// Test and, or variants
	public static void testAndOrVariants() {
		int res;

		res = brace(true);
		Assert.assertEquals("brace1", 11, res);
		res = brace(false);
		Assert.assertEquals("brace2", 12, res);

		res = and(true, true);
		Assert.assertEquals("and1", 11, res);
		res = and(true, false);
		Assert.assertEquals("and2", 12, res);
		res = and(false, true);
		Assert.assertEquals("and3", 12, res);
		res = and(false, false);
		Assert.assertEquals("and4", 12, res);

		res = andOr1(true, true, true, true);
		Assert.assertEquals("andOr11", 11, res);
		res = andOr1(true, true, false, true);
		Assert.assertEquals("andOr12", 11, res);
		res = andOr1(true, true, true, false);
		Assert.assertEquals("andOr13", 11, res);
		res = andOr1(true, true, false, false);
		Assert.assertEquals("andOr14", -1, res);
		res = andOr1(true, false, true, true);
		Assert.assertEquals("andOr15", -1, res);
		res = andOr1(false, true, true, true);
		Assert.assertEquals("andOr16", -1, res);
		res = andOr1(false, false, true, true);
		Assert.assertEquals("andOr17", -1, res);

		res = andOr2(true, true, true, true);
		Assert.assertEquals("andOr21", 11, res);
		res = andOr2(true, true, false, true);
		Assert.assertEquals("andOr22", 11, res);
		res = andOr2(true, true, true, false);
		Assert.assertEquals("andOr23", 11, res);
		res = andOr2(true, true, false, false);
		Assert.assertEquals("andOr24", 12, res);
		res = andOr2(true, false, true, true);
		Assert.assertEquals("andOr25", -1, res);
		res = andOr2(false, true, true, true);
		Assert.assertEquals("andOr26", -1, res);
		res = andOr2(false, false, true, true);
		Assert.assertEquals("andOr27", -1, res);

		res = orAnd1(true, true, true, true);
		Assert.assertEquals("orAnd11", 11, res);
		res = orAnd1(true, true, false, true);
		Assert.assertEquals("orAnd12", -1, res);
		res = orAnd1(true, true, true, false);
		Assert.assertEquals("orAnd13", -1, res);
		res = orAnd1(true, true, false, false);
		Assert.assertEquals("orAnd14", -1, res);
		res = orAnd1(true, false, true, true);
		Assert.assertEquals("orAnd15", 11, res);
		res = orAnd1(false, true, true, true);
		Assert.assertEquals("orAnd16", 11, res);
		res = orAnd1(false, false, true, true);
		Assert.assertEquals("orAnd17", -1, res);

		res = orAnd2(true, true, true, true);
		Assert.assertEquals("orAnd21", 11, res);
		res = orAnd2(true, true, false, true);
		Assert.assertEquals("orAnd22", 12, res);
		res = orAnd2(true, true, true, false);
		Assert.assertEquals("orAnd23", 12, res);
		res = orAnd2(true, true, false, false);
		Assert.assertEquals("orAnd24", 12, res);
		res = orAnd2(true, false, true, true);
		Assert.assertEquals("orAnd25", 11, res);
		res = orAnd2(false, true, true, true);
		Assert.assertEquals("orAnd26", 11, res);
		res = orAnd2(false, false, true, true);
		Assert.assertEquals("orAnd27", -1, res);
		CmdTransmitter.sendDone();
	}

	// 3 variables variants
	public static int threeVars1(boolean ca, boolean cb, boolean cc) {
		int res = -1;
		if (ca) {
			if (cb & cc) {
				res = 11;
			} else {
				res = 12;
			}
		}
		return res;
	}

	public static int threeVars2(boolean ca, boolean cb, boolean cc) {
		int res = -1;
		if (ca) {
			if (cb && cc) {
			} else {
				res = 12;
			}
		}
		return res;
	}

	public static int threeVars3(boolean ca, boolean cb, boolean cc) {
		int res = -1;
		if (ca) {
			if (cb && cc) {
				res = 11;
			} else {
			}
		}
		return res;
	}

	public static int threeVars4(boolean ca, boolean cb, boolean cc) {
		int res = -1;
		if (ca) {
			if (cb && cc) {
			} else {
			}
			res = 13;
		}
		return res;
	}

	@Test
	// Test 3 variables variants
	public static void testThreeVars() {
		int res;

		res = threeVars1(true, true, true);
		Assert.assertEquals("threeVars11", 11, res);
		res = threeVars1(true, true, false);
		Assert.assertEquals("threeVars12", 12, res);
		res = threeVars1(true, false, true);
		Assert.assertEquals("threeVars13", 12, res);
		res = threeVars1(true, false, false);
		Assert.assertEquals("threeVars14", 12, res);
		res = threeVars1(false, true, true);
		Assert.assertEquals("threeVars15", -1, res);
		res = threeVars1(false, true, false);
		Assert.assertEquals("threeVars16", -1, res);
		res = threeVars1(false, false, true);
		Assert.assertEquals("threeVars17", -1, res);
		res = threeVars1(false, false, false);
		Assert.assertEquals("threeVars18", -1, res);

		res = threeVars2(true, true, true);
		Assert.assertEquals("threeVars21", -1, res);
		res = threeVars2(true, true, false);
		Assert.assertEquals("threeVars22", 12, res);
		res = threeVars2(true, false, true);
		Assert.assertEquals("threeVars23", 12, res);
		res = threeVars2(true, false, false);
		Assert.assertEquals("threeVars24", 12, res);
		res = threeVars2(false, true, true);
		Assert.assertEquals("threeVars25", -1, res);
		res = threeVars2(false, true, false);
		Assert.assertEquals("threeVars26", -1, res);
		res = threeVars2(false, false, true);
		Assert.assertEquals("threeVars27", -1, res);
		res = threeVars2(false, false, false);
		Assert.assertEquals("threeVars28", -1, res);

		res = threeVars3(true, true, true);
		Assert.assertEquals("threeVars31", 11, res);
		res = threeVars3(true, true, false);
		Assert.assertEquals("threeVars32", -1, res);
		res = threeVars3(true, false, true);
		Assert.assertEquals("threeVars33", -1, res);
		res = threeVars3(true, false, false);
		Assert.assertEquals("threeVars34", -1, res);
		res = threeVars3(false, true, true);
		Assert.assertEquals("threeVars35", -1, res);
		res = threeVars3(false, true, false);
		Assert.assertEquals("threeVars36", -1, res);
		res = threeVars3(false, false, true);
		Assert.assertEquals("threeVars37", -1, res);
		res = threeVars3(false, false, false);
		Assert.assertEquals("threeVars38", -1, res);

		res = threeVars4(true, true, true);
		Assert.assertEquals("threeVars41", 13, res);
		res = threeVars4(true, true, false);
		Assert.assertEquals("threeVars42", 13, res);
		res = threeVars4(true, false, true);
		Assert.assertEquals("threeVars43", 13, res);
		res = threeVars4(true, false, false);
		Assert.assertEquals("threeVars44", 13, res);
		res = threeVars4(false, true, true);
		Assert.assertEquals("threeVars45", -1, res);
		res = threeVars4(false, true, false);
		Assert.assertEquals("threeVars46", -1, res);
		res = threeVars4(false, false, true);
		Assert.assertEquals("threeVars47", -1, res);
		res = threeVars4(false, false, false);
		Assert.assertEquals("threeVars48", -1, res);

		CmdTransmitter.sendDone();
	}

	// else if variants
	public static int elseIf1(boolean ca, boolean cb, boolean cc, boolean cd) {
		int res = -1;
		if (ca) {
			res = 11;
		} else if (cb && cc) {
			res = 12;
		} else if (cd) {
			res = 13;
		} else if (cc || cd) {
			res = 14;
		}
		return res;
	}

	public static int elseIf2(boolean ca, boolean cb, boolean cc, boolean cd) {
		int res = -1;
		if (ca) {
			res = 11;
		} else if (cb && cc) {
			res = 12;
		} else if (cd) {
			res = 13;
		} else if (cc || cd) {
			res = 14;
		} else {
			res = 15;
		}
		return res;
	}

	@Test
	// Test else if variants
	public static void testElseIf() {
		int res;

		res = elseIf1(true, true, true, true);
		Assert.assertEquals("elseIf11", 11, res);
		res = elseIf1(false, true, true, true);
		Assert.assertEquals("elseIf12", 12, res);
		res = elseIf1(false, false, true, true);
		Assert.assertEquals("elseIf13", 13, res);
		res = elseIf1(false, true, false, true);
		Assert.assertEquals("elseIf14", 13, res);
		res = elseIf1(false, false, false, true);
		Assert.assertEquals("elseIf15", 13, res);
		res = elseIf1(false, false, true, false);
		Assert.assertEquals("elseIf16", 14, res);
		res = elseIf1(false, false, false, false);
		Assert.assertEquals("elseIf17", -1, res);

		res = elseIf2(true, true, true, true);
		Assert.assertEquals("elseIf21", 11, res);
		res = elseIf2(false, true, true, true);
		Assert.assertEquals("elseIf22", 12, res);
		res = elseIf2(false, true, true, false);
		Assert.assertEquals("elseIf23", 12, res);
		res = elseIf2(false, false, true, true);
		Assert.assertEquals("elseIf24", 13, res);
		res = elseIf2(false, true, false, true);
		Assert.assertEquals("elseIf25", 13, res);
		res = elseIf2(false, false, false, true);
		Assert.assertEquals("elseIf26", 13, res);
		res = elseIf2(false, false, true, false);
		Assert.assertEquals("elseIf27", 14, res);
		res = elseIf2(false, true, false, false);
		Assert.assertEquals("elseIf27", 15, res);

		CmdTransmitter.sendDone();
	}

	// nesting variants
	public static int nesting(boolean ca, boolean cb, boolean cc, boolean cd,
			boolean ce) {
		int res = -1;

		if (!ca || !cb) {
			if (cc) {
				res = 11;
				if (cd) {
					res = 12;
					if (ce) {
						res = 13;
					} else {
						res = 14;
					}
				} else {
					res = 15;
				}
			} else {
				res = 21;
				if (ca) {
					res = 22;
				} else {
					res = 23;
				}
			}
		} else {
			res = 31;
		}
		return res;
	}

	@Test
	// Test nesting variants
	public static void testNesting() {
		int res;
		res = nesting(true, true, true, true, true);
		Assert.assertEquals("nesting1", 31, res);
		res = nesting(true, true, false, true, true);
		Assert.assertEquals("nesting2", 31, res);
		res = nesting(true, true, true, false, true);
		Assert.assertEquals("nesting3", 31, res);
		res = nesting(true, true, true, true, false);
		Assert.assertEquals("nesting4", 31, res);
		res = nesting(true, false, true, true, true);
		Assert.assertEquals("nesting5", 13, res);
		res = nesting(true, false, true, true, false);
		Assert.assertEquals("nesting6", 14, res);
		res = nesting(true, false, true, false, true);
		Assert.assertEquals("nesting7", 15, res);
		res = nesting(true, false, true, false, false);
		Assert.assertEquals("nesting8", 15, res);
		res = nesting(true, false, false, false, true);
		Assert.assertEquals("nesting9", 22, res);
		res = nesting(false, false, false, false, false);
		Assert.assertEquals("nesting10", 23, res);

		CmdTransmitter.sendDone();
	}
	
	// various tests
	static int i1;
    public static void if2() {	// constants
        int a = 1;
        int b = 2;
        if(a >= b){
            a = 6;
        }
        else{
            a = 8;
        }
        b=a;
        a++;
        b++;
        i1 = b;
    }
    
    public static void if3() {	
        int x = 0, y = 1;
        if(x == 0) {
            if(y == 1) {
                x++;
                if(x == 1) {
                    y++;
                    if(y == 2) {
                        int a, b, c;                        
                        a = 1;
                        b = 2;
                        c = a - b;
                        c++;
                        i1 = c;
                    }

                }
            }
        }
    }

	public void if4() {	
		boolean bool = true;
		int a = 1;
		int b = 0;

		if(bool) {
			b = 8;
		}
		else {
			b = 2;
		}
		a = b;
		a++;
		i1 = a;
	}
	
	public int if5(int n, int m){//example from m?ssenb?ck for loadParameter
		if(n < 0){
			n = 0; m = 0;
		}
		return n + m;
	}

	@Test
	public static void testVarious() {
		if2();
		Assert.assertEquals("various1", 9, i1);
		if3();
		Assert.assertEquals("various2", 0, i1);
		IfTest t = new IfTest();
		t.if4();
		Assert.assertEquals("various3", 9, i1);
		int res = t.if5(2,3);
		Assert.assertEquals("various4", 5, res);
		res = t.if5(-2,3);
		Assert.assertEquals("various5", 0, res);

		CmdTransmitter.sendDone();
	}
	
	public boolean add(byte data){
		if(!full){
			this.data[start = (++start % this.data.length)]= data;
			if(start == end)full = true;
			return true;
		}
		return false;
	}
	
	@Test
	public static void testAdd(){
		IfTest buf = new IfTest();
		buf.add((byte)10);
		Assert.assertEquals("add10", 1, buf.start);
		buf.add((byte)20);
		Assert.assertEquals("add20", 2, buf.start);
		
		CmdTransmitter.sendDone();
	}
	

}
