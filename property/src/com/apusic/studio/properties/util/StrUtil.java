package com.apusic.studio.properties.util;

import java.math.BigDecimal;

public class StrUtil {
	private static final int MAXASCII = 127;

	public static String getNotNullString(String str) {
		return ((str == null) ? "" : str);
	}

	public static String getNotNullAndTrimString(String str) {
		return ((str == null) ? "" : str.trim());
	}

	public static String getNullWhenEmpty(String str) {
		return (((str != null) && (str.length() == 0)) ? null : str);
	}

	public static String getNullWhenWhiteSpace(String str) {
		return (((str != null) && (str.trim().length() == 0)) ? null : str);
	}

	public static String getNullWhenWhiteSpaceOrTrim(String str) {
		return (((str != null) && (str.trim().length() == 0)) ? null : str
				.trim());
	}

	public static String getNullWhenEmptyOrUnspecified(String str,
			String unspecifiedStr) {
		if ((str != null)
				&& (((str.length() == 0) || (str.equals(unspecifiedStr))))) {
			return null;
		}
		return str;
	}

	public static String getNullOrTrim(String str) {
		return ((str == null) ? null : str.trim());
	}

	public static boolean isNotNullNotEmpty(String str) {
		return ((str != null) && (str.trim().length() > 0));
	}

	public static boolean isNullOrEmpty(String str) {
		return ((str == null) || (str.trim().length() == 0));
	}

	public static String initialUpperCase(String str) {
		if ((str == null) || (str.length() == 0)) {
			return str;
		}
		char ch = str.charAt(0);
		if (!(Character.isUpperCase(ch))) {
			str = Character.toUpperCase(ch) + str.substring(1);
		}
		return str;
	}

	public static String initialLowerCase(String str) {
		if ((str == null) || (str.length() == 0)) {
			return str;
		}
		char ch = str.charAt(0);
		if (!(Character.isLowerCase(ch))) {
			str = Character.toLowerCase(ch) + str.substring(1);
		}
		return str;
	}

	public static String trimLeadingTabsAndSpaces(String line) {
		int size = line.length();
		int start = size;
		for (int i = 0; i < size; ++i) {
			char c = line.charAt(i);
			if (!(isIndentChar(c))) {
				start = i;
				break;
			}
		}
		if (start == 0)
			return line;
		if (start == size) {
			return "";
		}
		return line.substring(start);
	}

	public static boolean isIndentChar(char ch) {
		return ((Character.isWhitespace(ch)) && (!(isLineDelimiterChar(ch))));
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s.trim());
		} catch (Throwable localThrowable) {
			return false;
		}
		return true;
	}

	public static boolean isLineDelimiterChar(char ch) {
		return ((ch == '\n') || (ch == '\r'));
	}

	public static boolean isEqual(String str1, String str2) {
		if (str1 == null) {
			return (str2 == null);
		}
		return str1.equals(str2);
	}

	public static boolean isLong(String text) {
		try {
			Long.parseLong(text.trim());
		} catch (Throwable localThrowable) {
			return false;
		}
		return true;
	}

	public static String formatLong(long ycoordinateValue) {
		String longStr = Long.toString(ycoordinateValue);
		int size = longStr.length();
		if (size > 3) {
			int count = size / 3;
			if (size % 3 == 0) {
				--count;
			}
			StringBuffer formatStr = new StringBuffer();
			int beginIndex = 0;
			for (int i = count; i > 0; --i) {
				int endIndex = size - (i * 3);
				formatStr.append(longStr.substring(beginIndex, endIndex))
						.append(",");
				beginIndex = endIndex;
			}
			formatStr.append(longStr.substring(beginIndex));
			return formatStr.toString();
		}
		return longStr;
	}

	public static String bytesToMega(long v) {
		double value = v;
		double kiloBytes = value / 1024.0D;
		if (kiloBytes == 0.0D) {
			return value + " bytes";
		}
		double megaBytes = kiloBytes / 1024.0D;
		if (megaBytes == 0.0D) {
			BigDecimal bigKiloBytes = new BigDecimal(value).divide(
					new BigDecimal(1024.0D), 2, 6);
			return bigKiloBytes.toString() + " KB";
		}
		BigDecimal bigMegaBytes = new BigDecimal(value).divide(new BigDecimal(
				1048576.0D), 2, 6);
		return bigMegaBytes.toString() + " MB";
	}

	public static int toASCCharLength(String value) {
		if (value == null)
			return 0;
		char[] chars = value.toCharArray();
		int len = 0;
		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] > '')
				len += 2;
			else {
				++len;
			}
		}
		return len;
	}
}
