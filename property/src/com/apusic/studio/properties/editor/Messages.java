package com.apusic.studio.properties.editor;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "com.apusic.studio.properties.editor.messages";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle("com.apusic.studio.properties.editor.messages");

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException localMissingResourceException) {
		}
		return '!' + key + '!';
	}
}
