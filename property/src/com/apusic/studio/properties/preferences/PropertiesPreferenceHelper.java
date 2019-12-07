package com.apusic.studio.properties.preferences;

import com.apusic.studio.properties.PropertiesPlugin;
import com.apusic.studio.properties.model.PropertiesModelHelper;
import com.apusic.studio.properties.util.StrUtil;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.preference.IPreferenceStore;

public class PropertiesPreferenceHelper {
	private static final String REGEX_VALID_COMMON_LOCALE_FORMAT_STRING = "([a-zA-Z]{0,2}[_]{1}[a-zA-Z]{0,2}\\|{1})+";
	private static Pattern _PATTERN = Pattern
			.compile("([a-zA-Z]{0,2}[_]{1}[a-zA-Z]{0,2}\\|{1})+");
	private static IPreferenceStore _PREFERENCESTORE;

	public static boolean isValidFormat(String str) {
		Matcher matcher = _PATTERN.matcher(str);
		return matcher.matches();
	}

	public static List<Locale> getStoredCommonLocale() {
		String str = getPreferenceStore()
				.getString(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.common");
		if ((StrUtil.isNullOrEmpty(str)) || (!(isValidFormat(str)))) {
			str = getPreferenceStore()
					.getDefaultString(
							"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.common");
			if ((StrUtil.isNullOrEmpty(str)) || (!(isValidFormat(str)))) {
				str = "_|zh_CN|en_US";
			}
		}
		return getLocale2(str);
	}

	public static boolean getStoredOrder() {
		return getPreferenceStore()
				.getBoolean(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.order");
	}

	public static boolean getDefaultOrder() {
		return getPreferenceStore()
				.getDefaultBoolean(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.order");
	}

	public static List<Locale> getDefaultCommonLocale() {
		String str = getPreferenceStore()
				.getDefaultString(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.common");
		if ((StrUtil.isNullOrEmpty(str)) || (!(isValidFormat(str)))) {
			str = "_|zh_CN|en_US";
		}
		return getLocale2(str);
	}

	private static List<Locale> getLocale(String str) {
		String[] numStr = str.split("[|]");
		List ret = new ArrayList();
		List all = PropertiesModelHelper.getAllLocale();
		for (int i = 0; i < numStr.length; ++i) {
			int index = Integer.parseInt(numStr[i]);
			if ((index >= all.size()) || (index < 0))
				continue;
			ret.add((Locale) all.get(index));
		}
		return ret;
	}

	private static List<Locale> getLocale2(String str) {
		String[] strPartitions = str.split("[|]");
		List ret = new ArrayList();
		String languageName = "";
		String countryName = "";
		for (int i = 0; i < strPartitions.length; ++i) {
			int downnerLineIndex = strPartitions[i].indexOf(95);
			if (downnerLineIndex < 0)
				continue;
			languageName = strPartitions[i].substring(0, downnerLineIndex);
			countryName = strPartitions[i].substring(downnerLineIndex + 1);
			ret.add(new Locale(languageName, countryName));
		}
		return ret;
	}

	private static IPreferenceStore getPreferenceStore() {
		if (_PREFERENCESTORE == null) {
			_PREFERENCESTORE = PropertiesPlugin.getDefault()
					.getPreferenceStore();
		}
		return _PREFERENCESTORE;
	}

	public static void main(String[] args) {
		System.out.println(isValidFormat("_|_|"));
		System.out.println(isValidFormat("_|zh_CN|"));
		System.out.println(isValidFormat("_|zh_CN|en_|en_US|"));
		System.out.println(isValidFormat("234|fdf|"));
		System.out.println(isValidFormat("32|133"));
	}
}
