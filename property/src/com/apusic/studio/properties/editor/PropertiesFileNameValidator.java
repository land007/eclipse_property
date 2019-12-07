package com.apusic.studio.properties.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesFileNameValidator {
	private static final String LANGUAGE_COUNTRY_REG_EXP = "(_[a-z|A-Z]{2}_[a-z|A-Z]{2})";
	private static final String LANGUAGE_REG_EXP = "(_[a-z|A-Z]{2})";
	private static Pattern LANGUAGE_COUNTRY_PATTERN = Pattern
			.compile("(_[a-z|A-Z]{2}_[a-z|A-Z]{2})");
	private static Pattern LANGUAGE_REG_PATTERN = Pattern
			.compile("(_[a-z|A-Z]{2})");

	public static String getFileNamePre(String fileName) {
		String ret = fileName;
		Matcher lcMatcher = LANGUAGE_COUNTRY_PATTERN.matcher(fileName);
		int index = 0;
		if (lcMatcher.find()) {
			index = lcMatcher.end();
			if (index == fileName.length()) {
				ret = fileName.substring(0, index - 6);
			}
		} else {
			Matcher lMatcher = LANGUAGE_REG_PATTERN.matcher(fileName);
			if (lMatcher.find()) {
				index = lMatcher.end();
				if (index == fileName.length()) {
					ret = fileName.substring(0, index - 3);
				}
			}
		}
		return ret;
	}
}
