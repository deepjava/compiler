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

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
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
