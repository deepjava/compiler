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

package org.deepjava.eclipse.ui.preferences;

import org.deepjava.eclipse.DeepPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class DeepPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_LIBRARY_PATH, DeepPlugin.LIB_PATH);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_BOARD, DeepPlugin.BOARD);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_OS, DeepPlugin.OS);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_PROGRAMMER, DeepPlugin.PROGRAMMER);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_PROGRAMMER_OPTIONS, DeepPlugin.PROGRAMMER_OPTIONS);
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_OPENOCD_PATH, DeepPlugin.OPENOCD_PATH);
	}

}
