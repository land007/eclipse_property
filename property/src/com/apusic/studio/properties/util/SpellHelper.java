package com.apusic.studio.properties.util;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellHelper {
	private static final String FROM_ENCODE = "GBK";
	private static final String TO_ENCODE = "GBK";

	public static int compare(String str1, String str2) {
		if ((str1 == null) || (str2 == null)) {
			throw new IllegalArgumentException("argument should not be null");
		}
		int result = 0;
		String m_s1 = null;
		String m_s2 = null;
		try {
			m_s1 = new String(str1.getBytes("GBK"), "GBK");
			m_s2 = new String(str2.getBytes("GBK"), "GBK");
		} catch (Exception localException) {
			return str1.compareTo(str2);
		}
		result = chineseCompareTo(m_s1, m_s2);
		return result;
	}

	public static int getCharCode(String str) {
		if ((str == null) && ("".equals(str)))
			return -1;
		byte[] b = str.getBytes();
		int value = 0;
		for (int i = 0; (i < b.length) && (i <= 2); ++i) {
			value = value * 100 + b[i];
		}
		return value;
	}

	public static int chineseCompareTo(String str1, String str2) {
		if ((str1 == null) || (str2 == null)) {
			throw new IllegalArgumentException("argument should not be null");
		}
		int len1 = str1.length();
		int len2 = str2.length();
		int n = Math.min(len1, len2);
		for (int i = 0; i < n; ++i) {
			int s1_code = getCharCode(String.valueOf(str1.charAt(i)));
			int s2_code = getCharCode(String.valueOf(str2.charAt(i)));
			if (s1_code * s2_code < 0)
				return Math.min(s1_code, s2_code);
			if (s1_code != s2_code) {
				return (s1_code - s2_code);
			}
		}
		return (len1 - len2);
	}

	public static String getBeginCharacter(String res) {
		String a = res;
		String result = "";
		for (int i = 0; i < a.length(); ++i) {
			String current = a.substring(i, i + 1);
			if (compare(current, "°¡") < 0)
				result = result + current;
			else if ((compare(current, "°¡") >= 0)
					&& (compare(current, "×ù") <= 0)) {
				if (compare(current, "ÔÑ") >= 0)
					result = result + "z";
				else if (compare(current, "Ñ¹") >= 0)
					result = result + "y";
				else if (compare(current, "Îô") >= 0)
					result = result + "x";
				else if (compare(current, "ÍÚ") >= 0)
					result = result + "w";
				else if (compare(current, "Ëú") >= 0)
					result = result + "t";
				else if (compare(current, "Èö") >= 0)
					result = result + "s";
				else if (compare(current, "È»") >= 0)
					result = result + "r";
				else if (compare(current, "ÆÚ") >= 0)
					result = result + "q";
				else if (compare(current, "Å¾") >= 0)
					result = result + "p";
				else if (compare(current, "Å¶") >= 0)
					result = result + "o";
				else if (compare(current, "ÄÃ") >= 0)
					result = result + "n";
				else if (compare(current, "Âè") >= 0)
					result = result + "m";
				else if (compare(current, "À¬") >= 0)
					result = result + "l";
				else if (compare(current, "¿¦") >= 0)
					result = result + "k";
				else if (compare(current, "»÷") > 0)
					result = result + "j";
				else if (compare(current, "¹þ") >= 0)
					result = result + "h";
				else if (compare(current, "¸Á") >= 0)
					result = result + "g";
				else if (compare(current, "·¢") >= 0)
					result = result + "f";
				else if (compare(current, "¶ê") >= 0)
					result = result + "e";
				else if (compare(current, "´î") >= 0)
					result = result + "d";
				else if (compare(current, "²Á") >= 0)
					result = result + "c";
				else if (compare(current, "°Å") >= 0)
					result = result + "b";
				else if (compare(current, "°¡") >= 0)
					result = result + "a";
			}
		}
		return result;
	}

	public static String getFirstStr(String str) {
		char a = str.charAt(0);
		char[] aa = { a };
		String sss = new String(aa);
		if (Character.isDigit(aa[0]))
			sss = "data";
		else if (((a >= 'a') && (a <= 'z')) || ((a >= 'A') && (a <= 'Z')))
			sss = String.valueOf(a);
		else
			sss = getBeginCharacter(sss);
		return sss;
	}

	public static int getByteTotalLength(String src) {
		int ret = 0;
		char[] c = src.toCharArray();
		for (int i = 0; i < c.length; ++i) {
			String s1 = String.valueOf(c[i]);
			byte[] b = s1.getBytes();
			ret += b.length;
		}
		return ret;
	}

	public static int[] getByteLength(String src) {
		char[] c = src.toCharArray();
		int[] ret = new int[c.length];
		for (int i = 0; i < c.length; ++i) {
			String s1 = String.valueOf(c[i]);
			byte[] b = s1.getBytes();
			ret[i] = b.length;
		}
		return ret;
	}

	public static void main(String[] args) {
		System.out.println(getBeginCharacter("ccc"));
		System.out.println(getFirstStr("erererer"));
		int count = 0;
		String regEx = "[\\u4e00-\\u9fa5]";
		String str = "";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); ++i) {
				++count;
			}
		}
		System.out.println("" + count + "");
		String s = "";
		char[] c = s.toCharArray();
		for (int i = 0; i < c.length; ++i) {
			String s1 = String.valueOf(c[i]);
			byte[] b = s1.getBytes();
			System.out.print(b.length + " ");
		}
	}
}
