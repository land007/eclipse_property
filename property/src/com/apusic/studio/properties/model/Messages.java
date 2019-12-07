package com.apusic.studio.properties.model;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "com.apusic.studio.properties.model.messages";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle("com.apusic.studio.properties.model.messages");

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException localMissingResourceException) {
		}
		return '!' + key + '!';
	}
}
