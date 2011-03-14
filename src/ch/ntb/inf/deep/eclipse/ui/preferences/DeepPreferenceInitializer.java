package ch.ntb.inf.deep.eclipse.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import ch.ntb.inf.deep.eclipse.DeepPlugin;

public class DeepPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		DeepPlugin.getDefault().getPreferenceStore().setDefault(PreferenceConstants.DEFAULT_LIBRARY_PATH, DeepPlugin.LIB_PATH);
	}

}
