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

package ch.ntb.inf.deep.loader;

/**
 * Download Exception
 */
public class DownloaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7783915136427481365L;

	/**
	 * Constructor
	 * 
	 * @param msg
	 *            Message
	 */
	public DownloaderException(String msg) {
		super(msg);
	}

	/**
	 * Constructor
	 * 
	 * @param msg
	 *            Message
	 * @param e
	 *            Other exception
	 */
	public DownloaderException(String msg, Exception e) {
		super(msg, e);
	}
}
