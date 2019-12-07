package com.apusic.studio.properties.preferences;

import com.apusic.studio.properties.PropertiesPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PropertiesPreferenceInitializer extends
		AbstractPreferenceInitializer {
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PropertiesPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(
				"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.common",
				"_|zh_CN|en_US");
		store.setDefault(
				"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.order",
				true);
	}
}
