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

package ch.ntb.inf.deep.eclipse.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import ch.ntb.inf.deep.eclipse.DeepPlugin;

public class DeepPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_LIBRARY_PATH, DeepPlugin.LIB_PATH);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_BOARD, DeepPlugin.BOARD);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_OS, DeepPlugin.OS);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_PROGRAMMER, DeepPlugin.PROGRAMMER);
	}

}
