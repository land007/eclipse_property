import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ReadProperties {

	private static String fileNamePre = "cms";

	private static String SPLIT = "_";

	private static String[] languages = { "de", "en", "fr", "ja", "pt", "zh" };

	private static Map<String, Integer> languagesMap = new HashMap<String, Integer>();

	private static String languagedef = "en";

	private static String internationalDir = "/Volumes/jiayq/Documents/runtime-Eclipse应用程序/test/src/";

	// private static PropertyBeans propertyBeans;

	private static List<PropertyBean> beanList = new ArrayList<PropertyBean>();

	private String language;

	static {
		try {
			List<String> inuses = getSamePrefixFileNamesAsInuseFileNamesInCurrentDir(internationalDir);
			String[] inuseFile = (String[]) inuses.toArray(new String[inuses
					.size()]);
			// int number = 0;
			// for (int i = 0; i < inuseFile.length; i++) {
			// String name = inuseFile[i];
			// if ((name.endsWith(".properties"))
			// && (name.startsWith(fileNamePre + SPLIT + language))) {
			// number = i;
			// break;
			// }
			// }
			for (int i = 0; i < languages.length; i++) {
				for (int j = 0; j < inuseFile.length; j++) {
					String name = inuseFile[j];
					if ((name.endsWith(".properties"))
							&& (name.startsWith(fileNamePre + SPLIT
									+ languages[i]))) {
						languagesMap.put(languages[i], i);
						break;
					}
				}
			}
			Properties[] properties = new Properties[inuseFile.length];
			for (int i = 0; i < inuseFile.length; i++) {
				properties[i] = new Properties();
				FileInputStream os = null;
				try {
					File tempFile = new File(internationalDir + inuseFile[i]);
					if (tempFile.exists()) {
						os = new FileInputStream(tempFile);
						properties[i].load(os);
					}
				} finally {
					if (os != null) {
						os.close();
					}
				}
			}
			// List<PropertyBean> beanList = new ArrayList<PropertyBean>();
			List<String> allKeysList = new ArrayList<String>();
			int index = 1;
			boolean isFirst = true;
			for (Properties tempProperties : properties) {
				Set pkSet = tempProperties.keySet();
				Iterator pkItr = pkSet.iterator();
				if (isFirst) {
					PropertyBean pb = null;
					for (; pkItr.hasNext(); beanList.add(pb)) {
						pb = new PropertyBean();
						String key = pkItr.next().toString();
						String val = tempProperties.getProperty(key);
						pb.setKey(key);
						pb.addValue(val != null ? val : "");
						allKeysList.add(key);
					}
					isFirst = false;
				} else {
					int isNewNum = 0;
					while (pkItr.hasNext()) {
						String key = pkItr.next().toString();
						String val = tempProperties.getProperty(key);
						if (!allKeysList.contains(key)) {
							PropertyBean pb = new PropertyBean();
							isNewNum++;
							pb.setKey(key);
							for (int i = 0; i < index - 1; i++)
								pb.addValue("");

							pb.addValue(val);
							allKeysList.add(key);
							beanList.add(pb);
						}
					}
					int allKeysListSize = allKeysList.size();
					for (int i = 0; i < allKeysListSize - isNewNum; i++) {
						String val = tempProperties
								.getProperty((String) allKeysList.get(i));
						if (val == null)
							val = "";
						((PropertyBean) beanList.get(i)).addValue(val);
					}
				}
				index++;
			}
			// propertyBeans = new PropertyBeans(beanList, number);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ReadProperties(String language) {
		super();
		this.language = language;
	}

	public String get(String key) {
		Integer ilanguage = languagesMap.get(language);
		if (ilanguage == null) {
			ilanguage = languagesMap.get(languagedef);
		}
		PropertyBeans propertyBeans = new PropertyBeans(beanList, ilanguage);
		return propertyBeans.get(key);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// System.out.println(propertyBeans.get("di1jiao1-0"));
		ReadProperties languages_zh = new ReadProperties("zh");
		ReadProperties languages_ja = new ReadProperties("ja");
		ReadProperties languages_de = new ReadProperties("de");
		System.out.println(languages_ja.get("di1jiao1-0"));
		
		System.out.println("密码必须由6－20个字符组成");
		System.out.println(languages_zh.get("mi4ma3bi4xu1you2_ge3zi4fu2zu3cheng2-0") + "6－20" + languages_zh.get("mi4ma3bi4xu1you2_ge3zi4fu2zu3cheng2-1"));
		System.out.println(languages_ja.get("mi4ma3bi4xu1you2_ge3zi4fu2zu3cheng2-0"));
		System.out.println(languages_de.get("mi4ma3bi4xu1you2_ge3zi4fu2zu3cheng2-0") + "6－20" + languages_de.get("mi4ma3bi4xu1you2_ge3zi4fu2zu3cheng2-1"));
		
	}

	public static class PropertyBeans {
		private Map<String, String> beanMap;

		public PropertyBeans(List<ReadProperties.PropertyBean> beanList,
				int number) {
			super();
			beanMap = new HashMap<String, String>();
			for (PropertyBean propertyBean : beanList) {
				beanMap.put(propertyBean.getKey(), propertyBean.getValues()
						.get(number));
			}
		}

		public String get(String key) {
			String value = "";
			String[] keys = key.split("-");
			if (keys.length == 1) {
				value = beanMap.get(keys[0]);
			} else if (keys.length == 2) {
				value = beanMap.get(keys[0]);
				if (value == null) {
					value = "";
				}
				String[] values = value.split(SPLIT);
				try {
					int i = Integer.parseInt(keys[1]);
					if (i < values.length) {
						value = values[i];
					}
				} catch (Exception e) {
				}
			}
			return value;

		}

	}

	public static class PropertyBean implements Comparable<Object> {
		private String key = "";
		private List<String> values = new ArrayList();

		public String getKey() {
			return this.key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public List<String> getValues() {
			return this.values;
		}

		public void addValue(String value) {
			this.values.add(value);
		}

		public void addValue(int index, String value) {
			if ((index < 0) || (index > this.values.size()))
				return;
			this.values.add(index, value);
		}

		public void setValue(int index, String value) {
			if ((index < 0) || (index >= this.values.size()))
				return;
			this.values.set(index, value);
		}

		public int compareTo(Object object) {
			PropertyBean otherPropertyBean = (PropertyBean) object;
			String myKey = getKey().toLowerCase();
			String otherKey = otherPropertyBean.getKey().toLowerCase();
			return myKey.compareTo(otherKey);
		}
	}

	private static List<String> getSamePrefixFileNamesAsInuseFileNamesInCurrentDir(
			String prePath) {
		List<String> tempList = new ArrayList<String>(2);
		File parent = new File(prePath);
		File[] files = parent.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if ((name.endsWith(".properties"))
						&& (name.startsWith(fileNamePre))) {
					String middleStr = name.substring(fileNamePre.length(),
							name.lastIndexOf(46));
					int middleStrLength = middleStr.length();
					boolean flag = (name.startsWith(fileNamePre + "."))
							|| ((name.startsWith(fileNamePre + SPLIT)) && (((3 == middleStrLength) || ((6 == middleStrLength) && (3 == middleStr
									.lastIndexOf(95))))));
					return flag;
				}
				return false;
			}
		});
		for (File file : files) {
			String fileName = file.getName();
			tempList.add(fileName);
		}
		return tempList;
	}

	// public abstract String[] generateRelativePage();

}
