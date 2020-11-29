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

package org.deepjava.comp.targettest.statements;

import ch.ntb.inf.junitTarget.Assert;
import ch.ntb.inf.junitTarget.CmdTransmitter;
import ch.ntb.inf.junitTarget.MaxErrors;
import ch.ntb.inf.junitTarget.Test;

/**
 * @author 01.12.2009 jan.mrnak@ntb.ch and simon.pertschy@ntb.ch
 * 
 */
@MaxErrors(100)
public class SwitchTest {

	private static int level0State;
	private static int level1State;
	private static int level2State;
	private static int level3State;

	@Test
	public static void returnSwitchTest() {
		level0State = States.StateM5;
		for (int i = -4; i <= States.State6; i++) {
			int s = runReturnSwitchTest();
			Assert.assertEquals("ReturnSwitchTest", i, s);
		}
		CmdTransmitter.sendDone();
	}

	private static int runReturnSwitchTest() {
		switch (level0State) {
		case States.StateM5:
			level0State = States.StateM4;
			return level0State;
		case States.StateM4:
			level0State = States.StateM3;
			return level0State;
		case States.StateM3:
			level0State = States.StateM2;
			return level0State;
		case States.StateM2:
			level0State = States.StateM1;
			return level0State;
		case States.StateM1:
			level0State = States.State0;
			return level0State;
		case States.State0:
			level0State = States.State1;
			return level0State;
		case States.State1:
			level0State = States.State2;
			return level0State;
		case States.State2:
			level0State = States.State3;
			return level0State;
		case States.State3:
			level0State = States.State4;
			return level0State;
		case States.State4:
			level0State = States.State5;
			return level0State;
		case States.State5:
			level0State = States.State6;
			return level0State;
		case States.State6:
			return level0State;
		}
		return -1;
	}

	@Test
	// test break
	public static void testBreak() {
		level0State = States.State0;
		int cnt = runBreakTest();
		Assert.assertEquals("break1State", States.State3, level0State);
		Assert.assertEquals("break1Count", cnt, 0);
		cnt = runBreakTest();
		Assert.assertEquals("break2", States.State6, level0State);
		Assert.assertEquals("break2Count", cnt, 15);
		cnt = runBreakTest();
		Assert.assertEquals("break3", States.State11, level0State);
		Assert.assertEquals("break3Count", cnt, 13);
		CmdTransmitter.sendDone();
	}

	private static int runBreakTest() {
		int cnt = 0;
		switch (level0State) {
		case States.State0:
			cnt += level0State;
		case States.State1:
			cnt += level0State;
		case States.State2:
			cnt += level0State;
			level0State = States.State3;
			break;
		case States.State3:
			level0State = States.State4;
			cnt += level0State;
		case States.State4:
			level0State = States.State5;
			cnt += level0State;
		case States.State5:
			level0State = States.State6;
			cnt += level0State;
			break;
		case States.State6:
			cnt += level0State;
			level0State = States.State7;
		default:
			cnt += level0State;
			level0State = States.State11;
		}
		return cnt;
	}

	@Test
	public static void runUsortedSwitch() {
		level0State = States.State888;
		int cnt = testUnsortedSwitch();
		Assert.assertEquals("unsortedState1", States.StateM10, level0State);
		Assert.assertEquals("unsortedCount1", 888, cnt);
		cnt = testUnsortedSwitch();
		Assert.assertEquals("unsortedState2", States.StateM1, level0State);
		Assert.assertEquals("unsortedCount2", 990, cnt);
		cnt = testUnsortedSwitch();
		Assert.assertEquals("SwitchState3", States.StateM1, level0State);
		Assert.assertEquals("SwitchCount3", 0, cnt);
		CmdTransmitter.sendDone();
	}

	private static int testUnsortedSwitch() {
		int cnt = 0;
		switch (level0State) {
		case States.StateM10:
			cnt += States.StateM10; // Fail Cnt -11
		case States.State999:
			cnt += States.State999; // Fail Cnt 998
			level0State = States.StateM1;
			break;
		case States.State888:
			cnt += States.State888; // Cnt = 888
			level0State = States.StateM10;
			return cnt;
		default:
			cnt += level0State;
		}
		cnt++; // Fail Cnt 887, -999, -10
		return cnt;
	}

	@Test
	// Test nesting
	public static void testNesting() {
		level0State = States.State1;
		level1State = States.State1;
		level2State = States.State999;
		level3State = States.State10;
		int cnt = runNesting(0);
		Assert.assertEquals("Nesting1", 46, cnt);	
		level0State = States.State2;
		level1State = States.State100;
		level2State = States.State1;
		cnt = 0;
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting2", 101, cnt);
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting3", 203, cnt);
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting4", 306, cnt);
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting5", 410, cnt);
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting6", 615, cnt);
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting7", 821, cnt);
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting8", 1028, cnt);
		cnt = runNesting(cnt);
		Assert.assertEquals("Nesting9", 1236, cnt);
		CmdTransmitter.sendDone();
	}

	public static int runNesting(int cnt) {
		switch (level0State) {
		case States.State1:
			cnt++; // FailCnt 1;
			switch (level1State) {
			case States.State1:
				cnt++; // FailCnt 2;
				switch (level2State) {
				case States.State999:
					cnt++; // FailCnt 3;
					switch (level3State) {
					case States.State10:
						cnt++; // FailCnt 4;
					case States.State9:
						cnt++; // FailCnt 5;
					case States.State8:
						cnt++; // FailCnt 6;
					}
				case States.StateM10:
					cnt++; // FailCnt 7;
				default:
					cnt++; // FailCnt 8;
				}
			case States.State2:
				cnt++; // FailCnt = 9
			default:
				cnt+=37;
				return cnt; // RightCtn = 10;
			}
		case States.State2:
			switch (level1State) {
			case States.State100:
				cnt += level1State;
				switch (level2State) {
				case States.State1:
					cnt += level2State;
					level2State = States.State2; // RightCnt = 101;
					break;
				case States.State2:
					cnt += level2State; // RightCnt = 203
					level2State = States.State3;
					break;
				case States.State3:
					cnt += level2State; // RightCnt = 306
					level2State = States.State4;
					break;
				default:
					cnt += level2State; // RightCnt = 410
					level2State = States.State5;
					level1State = States.State200;
					break;
				}

				break;
			case States.State200:
				cnt += level1State;
				switch (level2State) {
				case States.State5:
					cnt += level2State; // RightCnt = 615
					level2State = States.State6;
					break;
				case States.State6:
					cnt += level2State; // RightCnt = 821
					level2State = States.State7;
					break;
				case States.State7:
					cnt += level2State; // RightCnt = 1028
					level2State = States.State8;
					break;
				default:
					cnt += level2State; // RightCnt = 1236
					level2State = States.State9;
					level1State = States.State300;
					break;
				}
				break;
			}
			return cnt;
		default:
			return -2147483648;
		}

	}

	@Test
	public static void longSwitchTest() {
		level0State = States.State0;
		for (int i = 0; i < States.length; i++)
			runLongSwitch();
		Assert.assertEquals("LongSwitchTest", States.State999, level0State);
		CmdTransmitter.sendDone();
	}

	private static int getNextState(int actState) {
		return ++actState;
	}
	
	private static void runLongSwitch() {
		switch (level0State) {
		case States.State0:
			level0State = getNextState(level0State);
			break;
		case States.State1:
			level0State = getNextState(level0State);
			break;
		case States.State2:
			level0State = getNextState(level0State);
			break;
		case States.State3:
			level0State = getNextState(level0State);
			break;
		case States.State4:
			level0State = getNextState(level0State);
			break;
		case States.State5:
			level0State = getNextState(level0State);
			break;
		case States.State6:
			level0State = getNextState(level0State);
			break;
		case States.State7:
			level0State = getNextState(level0State);
			break;
		case States.State8:
			level0State = getNextState(level0State);
			break;
		case States.State9:
			level0State = getNextState(level0State);
			break;
		case States.State10:
			level0State = getNextState(level0State);
			break;
		case States.State11:
			level0State = getNextState(level0State);
			break;
		case States.State12:
			level0State = getNextState(level0State);
			break;
		case States.State13:
			level0State = getNextState(level0State);
			break;
		case States.State14:
			level0State = getNextState(level0State);
			break;
		case States.State15:
			level0State = getNextState(level0State);
			break;
		case States.State16:
			level0State = getNextState(level0State);
			break;
		case States.State17:
			level0State = getNextState(level0State);
			break;
		case States.State18:
			level0State = getNextState(level0State);
			break;
		case States.State19:
			level0State = getNextState(level0State);
			break;
		case States.State20:
			level0State = getNextState(level0State);
			break;
		case States.State21:
			level0State = getNextState(level0State);
			break;
		case States.State22:
			level0State = getNextState(level0State);
			break;
		case States.State23:
			level0State = getNextState(level0State);
			break;
		case States.State24:
			level0State = getNextState(level0State);
			break;
		case States.State25:
			level0State = getNextState(level0State);
			break;
		case States.State26:
			level0State = getNextState(level0State);
			break;
		case States.State27:
			level0State = getNextState(level0State);
			break;
		case States.State28:
			level0State = getNextState(level0State);
			break;
		case States.State29:
			level0State = getNextState(level0State);
			break;
		case States.State30:
			level0State = getNextState(level0State);
			break;
		case States.State31:
			level0State = getNextState(level0State);
			break;
		case States.State32:
			level0State = getNextState(level0State);
			break;
		case States.State33:
			level0State = getNextState(level0State);
			break;
		case States.State34:
			level0State = getNextState(level0State);
			break;
		case States.State35:
			level0State = getNextState(level0State);
			break;
		case States.State36:
			level0State = getNextState(level0State);
			break;
		case States.State37:
			level0State = getNextState(level0State);
			break;
		case States.State38:
			level0State = getNextState(level0State);
			break;
		case States.State39:
			level0State = getNextState(level0State);
			break;
		case States.State40:
			level0State = getNextState(level0State);
			break;
		case States.State41:
			level0State = getNextState(level0State);
			break;
		case States.State42:
			level0State = getNextState(level0State);
			break;
		case States.State43:
			level0State = getNextState(level0State);
			break;
		case States.State44:
			level0State = getNextState(level0State);
			break;
		case States.State45:
			level0State = getNextState(level0State);
			break;
		case States.State46:
			level0State = getNextState(level0State);
			break;
		case States.State47:
			level0State = getNextState(level0State);
			break;
		case States.State48:
			level0State = getNextState(level0State);
			break;
		case States.State49:
			level0State = getNextState(level0State);
			break;
		case States.State50:
			level0State = getNextState(level0State);
			break;
		case States.State51:
			level0State = getNextState(level0State);
			break;
		case States.State52:
			level0State = getNextState(level0State);
			break;
		case States.State53:
			level0State = getNextState(level0State);
			break;
		case States.State54:
			level0State = getNextState(level0State);
			break;
		case States.State55:
			level0State = getNextState(level0State);
			break;
		case States.State56:
			level0State = getNextState(level0State);
			break;
		case States.State57:
			level0State = getNextState(level0State);
			break;
		case States.State58:
			level0State = getNextState(level0State);
			break;
		case States.State59:
			level0State = getNextState(level0State);
			break;
		case States.State60:
			level0State = getNextState(level0State);
			break;
		case States.State61:
			level0State = getNextState(level0State);
			break;
		case States.State62:
			level0State = getNextState(level0State);
			break;
		case States.State63:
			level0State = getNextState(level0State);
			break;
		case States.State64:
			level0State = getNextState(level0State);
			break;
		case States.State65:
			level0State = getNextState(level0State);
			break;
		case States.State66:
			level0State = getNextState(level0State);
			break;
		case States.State67:
			level0State = getNextState(level0State);
			break;
		case States.State68:
			level0State = getNextState(level0State);
			break;
		case States.State69:
			level0State = getNextState(level0State);
			break;
		case States.State70:
			level0State = getNextState(level0State);
			break;
		case States.State71:
			level0State = getNextState(level0State);
			break;
		case States.State72:
			level0State = getNextState(level0State);
			break;
		case States.State73:
			level0State = getNextState(level0State);
			break;
		case States.State74:
			level0State = getNextState(level0State);
			break;
		case States.State75:
			level0State = getNextState(level0State);
			break;
		case States.State76:
			level0State = getNextState(level0State);
			break;
		case States.State77:
			level0State = getNextState(level0State);
			break;
		case States.State78:
			level0State = getNextState(level0State);
			break;
		case States.State79:
			level0State = getNextState(level0State);
			break;
		case States.State80:
			level0State = getNextState(level0State);
			break;
		case States.State81:
			level0State = getNextState(level0State);
			break;
		case States.State82:
			level0State = getNextState(level0State);
			break;
		case States.State83:
			level0State = getNextState(level0State);
			break;
		case States.State84:
			level0State = getNextState(level0State);
			break;
		case States.State85:
			level0State = getNextState(level0State);
			break;
		case States.State86:
			level0State = getNextState(level0State);
			break;
		case States.State87:
			level0State = getNextState(level0State);
			break;
		case States.State88:
			level0State = getNextState(level0State);
			break;
		case States.State89:
			level0State = getNextState(level0State);
			break;
		case States.State90:
			level0State = getNextState(level0State);
			break;
		case States.State91:
			level0State = getNextState(level0State);
			break;
		case States.State92:
			level0State = getNextState(level0State);
			break;
		case States.State93:
			level0State = getNextState(level0State);
			break;
		case States.State94:
			level0State = getNextState(level0State);
			break;
		case States.State95:
			level0State = getNextState(level0State);
			break;
		case States.State96:
			level0State = getNextState(level0State);
			break;
		case States.State97:
			level0State = getNextState(level0State);
			break;
		case States.State98:
			level0State = getNextState(level0State);
			break;
		case States.State99:
			level0State = getNextState(level0State);
			break;
		case States.State100:
			level0State = getNextState(level0State);
			break;
		case States.State101:
			level0State = getNextState(level0State);
			break;
		case States.State102:
			level0State = getNextState(level0State);
			break;
		case States.State103:
			level0State = getNextState(level0State);
			break;
		case States.State104:
			level0State = getNextState(level0State);
			break;
		case States.State105:
			level0State = getNextState(level0State);
			break;
		case States.State106:
			level0State = getNextState(level0State);
			break;
		case States.State107:
			level0State = getNextState(level0State);
			break;
		case States.State108:
			level0State = getNextState(level0State);
			break;
		case States.State109:
			level0State = getNextState(level0State);
			break;
		case States.State110:
			level0State = getNextState(level0State);
			break;
		case States.State111:
			level0State = getNextState(level0State);
			break;
		case States.State112:
			level0State = getNextState(level0State);
			break;
		case States.State113:
			level0State = getNextState(level0State);
			break;
		case States.State114:
			level0State = getNextState(level0State);
			break;
		case States.State115:
			level0State = getNextState(level0State);
			break;
		case States.State116:
			level0State = getNextState(level0State);
			break;
		case States.State117:
			level0State = getNextState(level0State);
			break;
		case States.State118:
			level0State = getNextState(level0State);
			break;
		case States.State119:
			level0State = getNextState(level0State);
			break;
		case States.State120:
			level0State = getNextState(level0State);
			break;
		case States.State121:
			level0State = getNextState(level0State);
			break;
		case States.State122:
			level0State = getNextState(level0State);
			break;
		case States.State123:
			level0State = getNextState(level0State);
			break;
		case States.State124:
			level0State = getNextState(level0State);
			break;
		case States.State125:
			level0State = getNextState(level0State);
			break;
		case States.State126:
			level0State = getNextState(level0State);
			break;
		case States.State127:
			level0State = getNextState(level0State);
			break;
		case States.State128:
			level0State = getNextState(level0State);
			break;
		case States.State129:
			level0State = getNextState(level0State);
			break;
		case States.State130:
			level0State = getNextState(level0State);
			break;
		case States.State131:
			level0State = getNextState(level0State);
			break;
		case States.State132:
			level0State = getNextState(level0State);
			break;
		case States.State133:
			level0State = getNextState(level0State);
			break;
		case States.State134:
			level0State = getNextState(level0State);
			break;
		case States.State135:
			level0State = getNextState(level0State);
			break;
		case States.State136:
			level0State = getNextState(level0State);
			break;
		case States.State137:
			level0State = getNextState(level0State);
			break;
		case States.State138:
			level0State = getNextState(level0State);
			break;
		case States.State139:
			level0State = getNextState(level0State);
			break;
		case States.State140:
			level0State = getNextState(level0State);
			break;
		case States.State141:
			level0State = getNextState(level0State);
			break;
		case States.State142:
			level0State = getNextState(level0State);
			break;
		case States.State143:
			level0State = getNextState(level0State);
			break;
		case States.State144:
			level0State = getNextState(level0State);
			break;
		case States.State145:
			level0State = getNextState(level0State);
			break;
		case States.State146:
			level0State = getNextState(level0State);
			break;
		case States.State147:
			level0State = getNextState(level0State);
			break;
		case States.State148:
			level0State = getNextState(level0State);
			break;
		case States.State149:
			level0State = getNextState(level0State);
			break;
		case States.State150:
			level0State = getNextState(level0State);
			break;
		case States.State151:
			level0State = getNextState(level0State);
			break;
		case States.State152:
			level0State = getNextState(level0State);
			break;
		case States.State153:
			level0State = getNextState(level0State);
			break;
		case States.State154:
			level0State = getNextState(level0State);
			break;
		case States.State155:
			level0State = getNextState(level0State);
			break;
		case States.State156:
			level0State = getNextState(level0State);
			break;
		case States.State157:
			level0State = getNextState(level0State);
			break;
		case States.State158:
			level0State = getNextState(level0State);
			break;
		case States.State159:
			level0State = getNextState(level0State);
			break;
		case States.State160:
			level0State = getNextState(level0State);
			break;
		case States.State161:
			level0State = getNextState(level0State);
			break;
		case States.State162:
			level0State = getNextState(level0State);
			break;
		case States.State163:
			level0State = getNextState(level0State);
			break;
		case States.State164:
			level0State = getNextState(level0State);
			break;
		case States.State165:
			level0State = getNextState(level0State);
			break;
		case States.State166:
			level0State = getNextState(level0State);
			break;
		case States.State167:
			level0State = getNextState(level0State);
			break;
		case States.State168:
			level0State = getNextState(level0State);
			break;
		case States.State169:
			level0State = getNextState(level0State);
			break;
		case States.State170:
			level0State = getNextState(level0State);
			break;
		case States.State171:
			level0State = getNextState(level0State);
			break;
		case States.State172:
			level0State = getNextState(level0State);
			break;
		case States.State173:
			level0State = getNextState(level0State);
			break;
		case States.State174:
			level0State = getNextState(level0State);
			break;
		case States.State175:
			level0State = getNextState(level0State);
			break;
		case States.State176:
			level0State = getNextState(level0State);
			break;
		case States.State177:
			level0State = getNextState(level0State);
			break;
		case States.State178:
			level0State = getNextState(level0State);
			break;
		case States.State179:
			level0State = getNextState(level0State);
			break;
		case States.State180:
			level0State = getNextState(level0State);
			break;
		case States.State181:
			level0State = getNextState(level0State);
			break;
		case States.State182:
			level0State = getNextState(level0State);
			break;
		case States.State183:
			level0State = getNextState(level0State);
			break;
		case States.State184:
			level0State = getNextState(level0State);
			break;
		case States.State185:
			level0State = getNextState(level0State);
			break;
		case States.State186:
			level0State = getNextState(level0State);
			break;
		case States.State187:
			level0State = getNextState(level0State);
			break;
		case States.State188:
			level0State = getNextState(level0State);
			break;
		case States.State189:
			level0State = getNextState(level0State);
			break;
		case States.State190:
			level0State = getNextState(level0State);
			break;
		case States.State191:
			level0State = getNextState(level0State);
			break;
		case States.State192:
			level0State = getNextState(level0State);
			break;
		case States.State193:
			level0State = getNextState(level0State);
			break;
		case States.State194:
			level0State = getNextState(level0State);
			break;
		case States.State195:
			level0State = getNextState(level0State);
			break;
		case States.State196:
			level0State = getNextState(level0State);
			break;
		case States.State197:
			level0State = getNextState(level0State);
			break;
		case States.State198:
			level0State = getNextState(level0State);
			break;
		case States.State199:
			level0State = getNextState(level0State);
			break;
		case States.State200:
			level0State = getNextState(level0State);
			break;
		case States.State201:
			level0State = getNextState(level0State);
			break;
		case States.State202:
			level0State = getNextState(level0State);
			break;
		case States.State203:
			level0State = getNextState(level0State);
			break;
		case States.State204:
			level0State = getNextState(level0State);
			break;
		case States.State205:
			level0State = getNextState(level0State);
			break;
		case States.State206:
			level0State = getNextState(level0State);
			break;
		case States.State207:
			level0State = getNextState(level0State);
			break;
		case States.State208:
			level0State = getNextState(level0State);
			break;
		case States.State209:
			level0State = getNextState(level0State);
			break;
		case States.State210:
			level0State = getNextState(level0State);
			break;
		case States.State211:
			level0State = getNextState(level0State);
			break;
		case States.State212:
			level0State = getNextState(level0State);
			break;
		case States.State213:
			level0State = getNextState(level0State);
			break;
		case States.State214:
			level0State = getNextState(level0State);
			break;
		case States.State215:
			level0State = getNextState(level0State);
			break;
		case States.State216:
			level0State = getNextState(level0State);
			break;
		case States.State217:
			level0State = getNextState(level0State);
			break;
		case States.State218:
			level0State = getNextState(level0State);
			break;
		case States.State219:
			level0State = getNextState(level0State);
			break;
		case States.State220:
			level0State = getNextState(level0State);
			break;
		case States.State221:
			level0State = getNextState(level0State);
			break;
		case States.State222:
			level0State = getNextState(level0State);
			break;
		case States.State223:
			level0State = getNextState(level0State);
			break;
		case States.State224:
			level0State = getNextState(level0State);
			break;
		case States.State225:
			level0State = getNextState(level0State);
			break;
		case States.State226:
			level0State = getNextState(level0State);
			break;
		case States.State227:
			level0State = getNextState(level0State);
			break;
		case States.State228:
			level0State = getNextState(level0State);
			break;
		case States.State229:
			level0State = getNextState(level0State);
			break;
		case States.State230:
			level0State = getNextState(level0State);
			break;
		case States.State231:
			level0State = getNextState(level0State);
			break;
		case States.State232:
			level0State = getNextState(level0State);
			break;
		case States.State233:
			level0State = getNextState(level0State);
			break;
		case States.State234:
			level0State = getNextState(level0State);
			break;
		case States.State235:
			level0State = getNextState(level0State);
			break;
		case States.State236:
			level0State = getNextState(level0State);
			break;
		case States.State237:
			level0State = getNextState(level0State);
			break;
		case States.State238:
			level0State = getNextState(level0State);
			break;
		case States.State239:
			level0State = getNextState(level0State);
			break;
		case States.State240:
			level0State = getNextState(level0State);
			break;
		case States.State241:
			level0State = getNextState(level0State);
			break;
		case States.State242:
			level0State = getNextState(level0State);
			break;
		case States.State243:
			level0State = getNextState(level0State);
			break;
		case States.State244:
			level0State = getNextState(level0State);
			break;
		case States.State245:
			level0State = getNextState(level0State);
			break;
		case States.State246:
			level0State = getNextState(level0State);
			break;
		case States.State247:
			level0State = getNextState(level0State);
			break;
		case States.State248:
			level0State = getNextState(level0State);
			break;
		case States.State249:
			level0State = getNextState(level0State);
			break;
		case States.State250:
			level0State = getNextState(level0State);
			break;
		case States.State251:
			level0State = getNextState(level0State);
			break;
		case States.State252:
			level0State = getNextState(level0State);
			break;
		case States.State253:
			level0State = getNextState(level0State);
			break;
		case States.State254:
			level0State = getNextState(level0State);
			break;
		case States.State255:
			level0State = getNextState(level0State);
			break;
		case States.State256:
			level0State = getNextState(level0State);
			break;
		case States.State257:
			level0State = getNextState(level0State);
			break;
		case States.State258:
			level0State = getNextState(level0State);
			break;
		case States.State259:
			level0State = getNextState(level0State);
			break;
		case States.State260:
			level0State = getNextState(level0State);
			break;
		case States.State261:
			level0State = getNextState(level0State);
			break;
		case States.State262:
			level0State = getNextState(level0State);
			break;
		case States.State263:
			level0State = getNextState(level0State);
			break;
		case States.State264:
			level0State = getNextState(level0State);
			break;
		case States.State265:
			level0State = getNextState(level0State);
			break;
		case States.State266:
			level0State = getNextState(level0State);
			break;
		case States.State267:
			level0State = getNextState(level0State);
			break;
		case States.State268:
			level0State = getNextState(level0State);
			break;
		case States.State269:
			level0State = getNextState(level0State);
			break;
		case States.State270:
			level0State = getNextState(level0State);
			break;
		case States.State271:
			level0State = getNextState(level0State);
			break;
		case States.State272:
			level0State = getNextState(level0State);
			break;
		case States.State273:
			level0State = getNextState(level0State);
			break;
		case States.State274:
			level0State = getNextState(level0State);
			break;
		case States.State275:
			level0State = getNextState(level0State);
			break;
		case States.State276:
			level0State = getNextState(level0State);
			break;
		case States.State277:
			level0State = getNextState(level0State);
			break;
		case States.State278:
			level0State = getNextState(level0State);
			break;
		case States.State279:
			level0State = getNextState(level0State);
			break;
		case States.State280:
			level0State = getNextState(level0State);
			break;
		case States.State281:
			level0State = getNextState(level0State);
			break;
		case States.State282:
			level0State = getNextState(level0State);
			break;
		case States.State283:
			level0State = getNextState(level0State);
			break;
		case States.State284:
			level0State = getNextState(level0State);
			break;
		case States.State285:
			level0State = getNextState(level0State);
			break;
		case States.State286:
			level0State = getNextState(level0State);
			break;
		case States.State287:
			level0State = getNextState(level0State);
			break;
		case States.State288:
			level0State = getNextState(level0State);
			break;
		case States.State289:
			level0State = getNextState(level0State);
			break;
		case States.State290:
			level0State = getNextState(level0State);
			break;
		case States.State291:
			level0State = getNextState(level0State);
			break;
		case States.State292:
			level0State = getNextState(level0State);
			break;
		case States.State293:
			level0State = getNextState(level0State);
			break;
		case States.State294:
			level0State = getNextState(level0State);
			break;
		case States.State295:
			level0State = getNextState(level0State);
			break;
		case States.State296:
			level0State = getNextState(level0State);
			break;
		case States.State297:
			level0State = getNextState(level0State);
			break;
		case States.State298:
			level0State = getNextState(level0State);
			break;
		case States.State299:
			level0State = getNextState(level0State);
			break;
		case States.State300:
			level0State = getNextState(level0State);
			break;
		case States.State301:
			level0State = getNextState(level0State);
			break;
		case States.State302:
			level0State = getNextState(level0State);
			break;
		case States.State303:
			level0State = getNextState(level0State);
			break;
		case States.State304:
			level0State = getNextState(level0State);
			break;
		case States.State305:
			level0State = getNextState(level0State);
			break;
		case States.State306:
			level0State = getNextState(level0State);
			break;
		case States.State307:
			level0State = getNextState(level0State);
			break;
		case States.State308:
			level0State = getNextState(level0State);
			break;
		case States.State309:
			level0State = getNextState(level0State);
			break;
		case States.State310:
			level0State = getNextState(level0State);
			break;
		case States.State311:
			level0State = getNextState(level0State);
			break;
		case States.State312:
			level0State = getNextState(level0State);
			break;
		case States.State313:
			level0State = getNextState(level0State);
			break;
		case States.State314:
			level0State = getNextState(level0State);
			break;
		case States.State315:
			level0State = getNextState(level0State);
			break;
		case States.State316:
			level0State = getNextState(level0State);
			break;
		case States.State317:
			level0State = getNextState(level0State);
			break;
		case States.State318:
			level0State = getNextState(level0State);
			break;
		case States.State319:
			level0State = getNextState(level0State);
			break;
		case States.State320:
			level0State = getNextState(level0State);
			break;
		case States.State321:
			level0State = getNextState(level0State);
			break;
		case States.State322:
			level0State = getNextState(level0State);
			break;
		case States.State323:
			level0State = getNextState(level0State);
			break;
		case States.State324:
			level0State = getNextState(level0State);
			break;
		case States.State325:
			level0State = getNextState(level0State);
			break;
		case States.State326:
			level0State = getNextState(level0State);
			break;
		case States.State327:
			level0State = getNextState(level0State);
			break;
		case States.State328:
			level0State = getNextState(level0State);
			break;
		case States.State329:
			level0State = getNextState(level0State);
			break;
		case States.State330:
			level0State = getNextState(level0State);
			break;
		case States.State331:
			level0State = getNextState(level0State);
			break;
		case States.State332:
			level0State = getNextState(level0State);
			break;
		case States.State333:
			level0State = getNextState(level0State);
			break;
		case States.State334:
			level0State = getNextState(level0State);
			break;
		case States.State335:
			level0State = getNextState(level0State);
			break;
		case States.State336:
			level0State = getNextState(level0State);
			break;
		case States.State337:
			level0State = getNextState(level0State);
			break;
		case States.State338:
			level0State = getNextState(level0State);
			break;
		case States.State339:
			level0State = getNextState(level0State);
			break;
		case States.State340:
			level0State = getNextState(level0State);
			break;
		case States.State341:
			level0State = getNextState(level0State);
			break;
		case States.State342:
			level0State = getNextState(level0State);
			break;
		case States.State343:
			level0State = getNextState(level0State);
			break;
		case States.State344:
			level0State = getNextState(level0State);
			break;
		case States.State345:
			level0State = getNextState(level0State);
			break;
		case States.State346:
			level0State = getNextState(level0State);
			break;
		case States.State347:
			level0State = getNextState(level0State);
			break;
		case States.State348:
			level0State = getNextState(level0State);
			break;
		case States.State349:
			level0State = getNextState(level0State);
			break;
		case States.State350:
			level0State = getNextState(level0State);
			break;
		case States.State351:
			level0State = getNextState(level0State);
			break;
		case States.State352:
			level0State = getNextState(level0State);
			break;
		case States.State353:
			level0State = getNextState(level0State);
			break;
		case States.State354:
			level0State = getNextState(level0State);
			break;
		case States.State355:
			level0State = getNextState(level0State);
			break;
		case States.State356:
			level0State = getNextState(level0State);
			break;
		case States.State357:
			level0State = getNextState(level0State);
			break;
		case States.State358:
			level0State = getNextState(level0State);
			break;
		case States.State359:
			level0State = getNextState(level0State);
			break;
		case States.State360:
			level0State = getNextState(level0State);
			break;
		case States.State361:
			level0State = getNextState(level0State);
			break;
		case States.State362:
			level0State = getNextState(level0State);
			break;
		case States.State363:
			level0State = getNextState(level0State);
			break;
		case States.State364:
			level0State = getNextState(level0State);
			break;
		case States.State365:
			level0State = getNextState(level0State);
			break;
		case States.State366:
			level0State = getNextState(level0State);
			break;
		case States.State367:
			level0State = getNextState(level0State);
			break;
		case States.State368:
			level0State = getNextState(level0State);
			break;
		case States.State369:
			level0State = getNextState(level0State);
			break;
		case States.State370:
			level0State = getNextState(level0State);
			break;
		case States.State371:
			level0State = getNextState(level0State);
			break;
		case States.State372:
			level0State = getNextState(level0State);
			break;
		case States.State373:
			level0State = getNextState(level0State);
			break;
		case States.State374:
			level0State = getNextState(level0State);
			break;
		case States.State375:
			level0State = getNextState(level0State);
			break;
		case States.State376:
			level0State = getNextState(level0State);
			break;
		case States.State377:
			level0State = getNextState(level0State);
			break;
		case States.State378:
			level0State = getNextState(level0State);
			break;
		case States.State379:
			level0State = getNextState(level0State);
			break;
		case States.State380:
			level0State = getNextState(level0State);
			break;
		case States.State381:
			level0State = getNextState(level0State);
			break;
		case States.State382:
			level0State = getNextState(level0State);
			break;
		case States.State383:
			level0State = getNextState(level0State);
			break;
		case States.State384:
			level0State = getNextState(level0State);
			break;
		case States.State385:
			level0State = getNextState(level0State);
			break;
		case States.State386:
			level0State = getNextState(level0State);
			break;
		case States.State387:
			level0State = getNextState(level0State);
			break;
		case States.State388:
			level0State = getNextState(level0State);
			break;
		case States.State389:
			level0State = getNextState(level0State);
			break;
		case States.State390:
			level0State = getNextState(level0State);
			break;
		case States.State391:
			level0State = getNextState(level0State);
			break;
		case States.State392:
			level0State = getNextState(level0State);
			break;
		case States.State393:
			level0State = getNextState(level0State);
			break;
		case States.State394:
			level0State = getNextState(level0State);
			break;
		case States.State395:
			level0State = getNextState(level0State);
			break;
		case States.State396:
			level0State = getNextState(level0State);
			break;
		case States.State397:
			level0State = getNextState(level0State);
			break;
		case States.State398:
			level0State = getNextState(level0State);
			break;
		case States.State399:
			level0State = getNextState(level0State);
			break;
		case States.State400:
			level0State = getNextState(level0State);
			break;
		case States.State401:
			level0State = getNextState(level0State);
			break;
		case States.State402:
			level0State = getNextState(level0State);
			break;
		case States.State403:
			level0State = getNextState(level0State);
			break;
		case States.State404:
			level0State = getNextState(level0State);
			break;
		case States.State405:
			level0State = getNextState(level0State);
			break;
		case States.State406:
			level0State = getNextState(level0State);
			break;
		case States.State407:
			level0State = getNextState(level0State);
			break;
		case States.State408:
			level0State = getNextState(level0State);
			break;
		case States.State409:
			level0State = getNextState(level0State);
			break;
		case States.State410:
			level0State = getNextState(level0State);
			break;
		case States.State411:
			level0State = getNextState(level0State);
			break;
		case States.State412:
			level0State = getNextState(level0State);
			break;
		case States.State413:
			level0State = getNextState(level0State);
			break;
		case States.State414:
			level0State = getNextState(level0State);
			break;
		case States.State415:
			level0State = getNextState(level0State);
			break;
		case States.State416:
			level0State = getNextState(level0State);
			break;
		case States.State417:
			level0State = getNextState(level0State);
			break;
		case States.State418:
			level0State = getNextState(level0State);
			break;
		case States.State419:
			level0State = getNextState(level0State);
			break;
		case States.State420:
			level0State = getNextState(level0State);
			break;
		case States.State421:
			level0State = getNextState(level0State);
			break;
		case States.State422:
			level0State = getNextState(level0State);
			break;
		case States.State423:
			level0State = getNextState(level0State);
			break;
		case States.State424:
			level0State = getNextState(level0State);
			break;
		case States.State425:
			level0State = getNextState(level0State);
			break;
		case States.State426:
			level0State = getNextState(level0State);
			break;
		case States.State427:
			level0State = getNextState(level0State);
			break;
		case States.State428:
			level0State = getNextState(level0State);
			break;
		case States.State429:
			level0State = getNextState(level0State);
			break;
		case States.State430:
			level0State = getNextState(level0State);
			break;
		case States.State431:
			level0State = getNextState(level0State);
			break;
		case States.State432:
			level0State = getNextState(level0State);
			break;
		case States.State433:
			level0State = getNextState(level0State);
			break;
		case States.State434:
			level0State = getNextState(level0State);
			break;
		case States.State435:
			level0State = getNextState(level0State);
			break;
		case States.State436:
			level0State = getNextState(level0State);
			break;
		case States.State437:
			level0State = getNextState(level0State);
			break;
		case States.State438:
			level0State = getNextState(level0State);
			break;
		case States.State439:
			level0State = getNextState(level0State);
			break;
		case States.State440:
			level0State = getNextState(level0State);
			break;
		case States.State441:
			level0State = getNextState(level0State);
			break;
		case States.State442:
			level0State = getNextState(level0State);
			break;
		case States.State443:
			level0State = getNextState(level0State);
			break;
		case States.State444:
			level0State = getNextState(level0State);
			break;
		case States.State445:
			level0State = getNextState(level0State);
			break;
		case States.State446:
			level0State = getNextState(level0State);
			break;
		case States.State447:
			level0State = getNextState(level0State);
			break;
		case States.State448:
			level0State = getNextState(level0State);
			break;
		case States.State449:
			level0State = getNextState(level0State);
			break;
		case States.State450:
			level0State = getNextState(level0State);
			break;
		case States.State451:
			level0State = getNextState(level0State);
			break;
		case States.State452:
			level0State = getNextState(level0State);
			break;
		case States.State453:
			level0State = getNextState(level0State);
			break;
		case States.State454:
			level0State = getNextState(level0State);
			break;
		case States.State455:
			level0State = getNextState(level0State);
			break;
		case States.State456:
			level0State = getNextState(level0State);
			break;
		case States.State457:
			level0State = getNextState(level0State);
			break;
		case States.State458:
			level0State = getNextState(level0State);
			break;
		case States.State459:
			level0State = getNextState(level0State);
			break;
		case States.State460:
			level0State = getNextState(level0State);
			break;
		case States.State461:
			level0State = getNextState(level0State);
			break;
		case States.State462:
			level0State = getNextState(level0State);
			break;
		case States.State463:
			level0State = getNextState(level0State);
			break;
		case States.State464:
			level0State = getNextState(level0State);
			break;
		case States.State465:
			level0State = getNextState(level0State);
			break;
		case States.State466:
			level0State = getNextState(level0State);
			break;
		case States.State467:
			level0State = getNextState(level0State);
			break;
		case States.State468:
			level0State = getNextState(level0State);
			break;
		case States.State469:
			level0State = getNextState(level0State);
			break;
		case States.State470:
			level0State = getNextState(level0State);
			break;
		case States.State471:
			level0State = getNextState(level0State);
			break;
		case States.State472:
			level0State = getNextState(level0State);
			break;
		case States.State473:
			level0State = getNextState(level0State);
			break;
		case States.State474:
			level0State = getNextState(level0State);
			break;
		case States.State475:
			level0State = getNextState(level0State);
			break;
		case States.State476:
			level0State = getNextState(level0State);
			break;
		case States.State477:
			level0State = getNextState(level0State);
			break;
		case States.State478:
			level0State = getNextState(level0State);
			break;
		case States.State479:
			level0State = getNextState(level0State);
			break;
		case States.State480:
			level0State = getNextState(level0State);
			break;
		case States.State481:
			level0State = getNextState(level0State);
			break;
		case States.State482:
			level0State = getNextState(level0State);
			break;
		case States.State483:
			level0State = getNextState(level0State);
			break;
		case States.State484:
			level0State = getNextState(level0State);
			break;
		case States.State485:
			level0State = getNextState(level0State);
			break;
		case States.State486:
			level0State = getNextState(level0State);
			break;
		case States.State487:
			level0State = getNextState(level0State);
			break;
		case States.State488:
			level0State = getNextState(level0State);
			break;
		case States.State489:
			level0State = getNextState(level0State);
			break;
		case States.State490:
			level0State = getNextState(level0State);
			break;
		case States.State491:
			level0State = getNextState(level0State);
			break;
		case States.State492:
			level0State = getNextState(level0State);
			break;
		case States.State493:
			level0State = getNextState(level0State);
			break;
		case States.State494:
			level0State = getNextState(level0State);
			break;
		case States.State495:
			level0State = getNextState(level0State);
			break;
		case States.State496:
			level0State = getNextState(level0State);
			break;
		case States.State497:
			level0State = getNextState(level0State);
			break;
		case States.State498:
			level0State = getNextState(level0State);
			break;
		case States.State499:
			level0State = getNextState(level0State);
			break;
		case States.State500:
			level0State = getNextState(level0State);
			break;
		case States.State501:
			level0State = getNextState(level0State);
			break;
		case States.State502:
			level0State = getNextState(level0State);
			break;
		case States.State503:
			level0State = getNextState(level0State);
			break;
		case States.State504:
			level0State = getNextState(level0State);
			break;
		case States.State505:
			level0State = getNextState(level0State);
			break;
		case States.State506:
			level0State = getNextState(level0State);
			break;
		case States.State507:
			level0State = getNextState(level0State);
			break;
		case States.State508:
			level0State = getNextState(level0State);
			break;
		case States.State509:
			level0State = getNextState(level0State);
			break;
		case States.State510:
			level0State = getNextState(level0State);
			break;
		case States.State511:
			level0State = getNextState(level0State);
			break;
		case States.State512:
			level0State = getNextState(level0State);
			break;
		case States.State513:
			level0State = getNextState(level0State);
			break;
		case States.State514:
			level0State = getNextState(level0State);
			break;
		case States.State515:
			level0State = getNextState(level0State);
			break;
		case States.State516:
			level0State = getNextState(level0State);
			break;
		case States.State517:
			level0State = getNextState(level0State);
			break;
		case States.State518:
			level0State = getNextState(level0State);
			break;
		case States.State519:
			level0State = getNextState(level0State);
			break;
		case States.State520:
			level0State = getNextState(level0State);
			break;
		case States.State521:
			level0State = getNextState(level0State);
			break;
		case States.State522:
			level0State = getNextState(level0State);
			break;
		case States.State523:
			level0State = getNextState(level0State);
			break;
		case States.State524:
			level0State = getNextState(level0State);
			break;
		case States.State525:
			level0State = getNextState(level0State);
			break;
		case States.State526:
			level0State = getNextState(level0State);
			break;
		case States.State527:
			level0State = getNextState(level0State);
			break;
		case States.State528:
			level0State = getNextState(level0State);
			break;
		case States.State529:
			level0State = getNextState(level0State);
			break;
		case States.State530:
			level0State = getNextState(level0State);
			break;
		case States.State531:
			level0State = getNextState(level0State);
			break;
		case States.State532:
			level0State = getNextState(level0State);
			break;
		case States.State533:
			level0State = getNextState(level0State);
			break;
		case States.State534:
			level0State = getNextState(level0State);
			break;
		case States.State535:
			level0State = getNextState(level0State);
			break;
		case States.State536:
			level0State = getNextState(level0State);
			break;
		case States.State537:
			level0State = getNextState(level0State);
			break;
		case States.State538:
			level0State = getNextState(level0State);
			break;
		case States.State539:
			level0State = getNextState(level0State);
			break;
		case States.State540:
			level0State = getNextState(level0State);
			break;
		case States.State541:
			level0State = getNextState(level0State);
			break;
		case States.State542:
			level0State = getNextState(level0State);
			break;
		case States.State543:
			level0State = getNextState(level0State);
			break;
		case States.State544:
			level0State = getNextState(level0State);
			break;
		case States.State545:
			level0State = getNextState(level0State);
			break;
		case States.State546:
			level0State = getNextState(level0State);
			break;
		case States.State547:
			level0State = getNextState(level0State);
			break;
		case States.State548:
			level0State = getNextState(level0State);
			break;
		case States.State549:
			level0State = getNextState(level0State);
			break;
		case States.State550:
			level0State = getNextState(level0State);
			break;
		case States.State551:
			level0State = getNextState(level0State);
			break;
		case States.State552:
			level0State = getNextState(level0State);
			break;
		case States.State553:
			level0State = getNextState(level0State);
			break;
		case States.State554:
			level0State = getNextState(level0State);
			break;
		case States.State555:
			level0State = getNextState(level0State);
			break;
		case States.State556:
			level0State = getNextState(level0State);
			break;
		case States.State557:
			level0State = getNextState(level0State);
			break;
		case States.State558:
			level0State = getNextState(level0State);
			break;
		case States.State559:
			level0State = getNextState(level0State);
			break;
		case States.State560:
			level0State = getNextState(level0State);
			break;
		case States.State561:
			level0State = getNextState(level0State);
			break;
		case States.State562:
			level0State = getNextState(level0State);
			break;
		case States.State563:
			level0State = getNextState(level0State);
			break;
		case States.State564:
			level0State = getNextState(level0State);
			break;
		case States.State565:
			level0State = getNextState(level0State);
			break;
		case States.State566:
			level0State = getNextState(level0State);
			break;
		case States.State567:
			level0State = getNextState(level0State);
			break;
		case States.State568:
			level0State = getNextState(level0State);
			break;
		case States.State569:
			level0State = getNextState(level0State);
			break;
		case States.State570:
			level0State = getNextState(level0State);
			break;
		case States.State571:
			level0State = getNextState(level0State);
			break;
		case States.State572:
			level0State = getNextState(level0State);
			break;
		case States.State573:
			level0State = getNextState(level0State);
			break;
		case States.State574:
			level0State = getNextState(level0State);
			break;
		case States.State575:
			level0State = getNextState(level0State);
			break;
		case States.State576:
			level0State = getNextState(level0State);
			break;
		case States.State577:
			level0State = getNextState(level0State);
			break;
		case States.State578:
			level0State = getNextState(level0State);
			break;
		case States.State579:
			level0State = getNextState(level0State);
			break;
		case States.State580:
			level0State = getNextState(level0State);
			break;
		case States.State581:
			level0State = getNextState(level0State);
			break;
		case States.State582:
			level0State = getNextState(level0State);
			break;
		case States.State583:
			level0State = getNextState(level0State);
			break;
		case States.State584:
			level0State = getNextState(level0State);
			break;
		case States.State585:
			level0State = getNextState(level0State);
			break;
		case States.State586:
			level0State = getNextState(level0State);
			break;
		case States.State587:
			level0State = getNextState(level0State);
			level0State = States.State999;
			break;
		}
	}

	public static int runNesting2() {
		int cnt = 1;
		switch (level0State) {
		case States.State1:
			cnt++; // FailCnt 1;
			switch (level1State) {
			case States.State1:
				cnt++; // FailCnt 2;
				switch (level2State) {
				case States.State999:
					cnt++; // FailCnt 3;
					switch (level3State) {
					case States.State10:
						cnt++; // FailCnt 4;
					case States.State9:
						cnt++; // FailCnt 5;
					case States.State8:
						cnt++; // FailCnt 6;
					}
				case States.StateM10:
					cnt++; // FailCnt 7;
				default:
					cnt++; // FailCnt 8;
				}
			case States.State2:
				cnt++; // FailCnt = 9
			default:
				cnt+=37;
				return cnt; // RightCtn = 10;
			}
		case States.State2:
			switch (level1State) {
			case States.State100:
				cnt += level1State;
				switch (level2State) {
				case States.State1:
					cnt += level2State;
					level2State = States.State2; // RightCnt = 101;
					break;
				case States.State2:
					cnt += level2State; // RightCnt = 203
					level2State = States.State3;
					break;
				case States.State3:
					cnt += level2State; // RightCnt = 306
					level2State = States.State4;
					break;
				default:
					cnt += level2State; // RightCnt = 410
					level2State = States.State5;
					level1State = States.State200;
					break;
				}

				break;
			case States.State200:
				cnt += level1State;
				switch (level2State) {
				case States.State5:
					cnt += level2State; // RightCnt = 615
					level2State = States.State6;
					break;
				case States.State6:
					cnt += level2State; // RightCnt = 821
					level2State = States.State7;
					break;
				case States.State7:
					cnt += level2State; // RightCnt = 1028
					level2State = States.State8;
					break;
				default:
					cnt += level2State; // RightCnt = 1236
					level2State = States.State9;
					level1State = States.State300;
					break;
				}
				break;
			}
			return cnt;
		default:
			return -2147483648;
		}

	}

	public static int switchNear2(int i) {
		switch(i) {
		case 0: return 0;
		case 1: return 1;
		case 2: i++; break;
		case 3: return 3;
		case 4: i += 4;
		case 5: i += 5; break;
		default: return -1;
		}
		return i + 3;
	}

	protected boolean terminated;

	public void switchWhile0() {
		switch (0) {
		case 0: 
			while (terminated)
				terminated = false;
			break;
		default:
			break;
		}
	}

	public void switchWhile1() {
		switch (1) {
		case 0: 
			while (terminated)
				terminated = false;
			break;
		default:
			break;
		}
	}
	
	@Test
	// test if switch condition is constant
	public static void testConst() {
		SwitchTest t = new SwitchTest();
		t.terminated = true;
		t.switchWhile0();
		Assert.assertFalse("test1", t.terminated);
		t.terminated = true;
		t.switchWhile1();
		Assert.assertTrue("test2", t.terminated);
		CmdTransmitter.sendDone();
	}
	
	// tests if dead blocks are correctly removed
	public static int switchDeadBlocks1(int a) {
		switch (a) {
		case 0: 
			return 10;
		case 1: 
			break;
		case 2: 
			break;
		case 3: 
			return 20;
		}
		return 1;
	}
	
	// tests if dead blocks are correctly removed
	public static int switchDeadBlocks2(int a) {
		int b = 1;
		switch (a) {
		case 0: 
			return 10;
		case 1: 
			b += 1;
		case 2: 
			break;
		case 3: 
			return 20;
		}
		return b;
	}

	@Test
	// test if switch condition is constant
	public static void testDeadBlocks() {
		Assert.assertEquals("test1", 10, switchDeadBlocks1(0));
		Assert.assertEquals("test2", 1, switchDeadBlocks1(1));
		Assert.assertEquals("test3", 1, switchDeadBlocks1(2));
		Assert.assertEquals("test4", 20, switchDeadBlocks1(3));
		Assert.assertEquals("test5", 1, switchDeadBlocks1(10));
		Assert.assertEquals("test10", 10, switchDeadBlocks2(0));
		Assert.assertEquals("test11", 2, switchDeadBlocks2(1));
		Assert.assertEquals("test12", 1, switchDeadBlocks2(2));
		Assert.assertEquals("test13", 20, switchDeadBlocks2(3));
		Assert.assertEquals("test14", 1, switchDeadBlocks2(10));
		CmdTransmitter.sendDone();
	}
	
	

}
