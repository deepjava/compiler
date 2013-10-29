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

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;

public class SegmentAssignment extends Item {
	HString contentAttribute;
	HString segmentDesignator;
	Segment seg;
	Device dev;

	public SegmentAssignment(HString attr, HString desig){
		contentAttribute = attr;
		segmentDesignator = desig;
		String[] jname = desig.toString().split("\\.");
		Board b = Configuration.getBoard();
		if (b == null) {ErrorReporter.reporter.error(238, ", must be defined before target configuration"); return;}
		dev = b.getDeviceByName(HString.getRegisteredHString(jname[0]));
		if (dev == null) {ErrorReporter.reporter.error(220, ", device required in target configuration is not found in memory map"); return;}
		seg = dev.getSegmentByName(jname[1]);
		if (seg == null) {ErrorReporter.reporter.error(254, ", segment required in target configuration is not found in memory map"); return;}
	}
	
	public String toString() {
		return (contentAttribute + "@" + dev.name + "." + seg.name);
	}
	
}
