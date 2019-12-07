package com.apusic.studio.properties.model;

import com.apusic.studio.properties.PropertiesPlugin;
import com.apusic.studio.properties.util.DirectoryHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class PropertiesModelHelper {
	private PropertyModel propertyModel;
	private List<String> inuses;
	private String prePath;
	public static final String PROPERTIES_FILE_PREFIX = "messages";
	public static final String PROPERTIES_FILE_SUFFIX = "properties";
	public static final String DOT = ".";
	public static final String BOTTOM_LINE = "_";
	public static final String COMPANY_INFO = "created by Apusic";
	public static final int CLASS_RELATIVE = 0;
	public static final int OS_PATH = 1;
	private String fileNamePre;
	private static List<Locale> ALL_LOCALE = new ArrayList();
	static {
		Locale[] ls = Locale.getAvailableLocales();
		ALL_LOCALE.add(new Locale("", "", ""));
		for (int i = 0; i < ls.length; ++i) {
			ALL_LOCALE.add(ls[i]);
		}
		Collections.sort(ALL_LOCALE, new Comparator() {
			public int compare(Object first, Object second) {
				Locale locale1 = (Locale) first;
				Locale locale2 = (Locale) second;
				String firstLanguageName = locale1.getLanguage();
				String secondLanguageName = locale2.getLanguage();
				return firstLanguageName.compareTo(secondLanguageName);
			}
		});
	}

	public String getFileNamePre() {
		return this.fileNamePre;
	}

	public static List<Locale> getAllLocale() {
		return Collections.unmodifiableList(ALL_LOCALE);
	}

	public PropertiesModelHelper() throws IOException {
		this("", "messages", 0);
	}

	public PropertiesModelHelper(String relativePath, String fileNamePre,
			int getPathType) throws IOException {
		this.propertyModel = new PropertyModel(this);
		setDir(relativePath, getPathType);
		this.fileNamePre = fileNamePre;
		initInuse();
	}

	private void setDir(String relativePath, int getPathType)
			throws IOException {
		if (getPathType == 0) {
			this.prePath = DirectoryHelper.getFullPathRelateClass(relativePath,
					PropertiesModelHelper.class);
		} else {
			if (1 != getPathType)
				return;
			this.prePath = relativePath;
		}
	}

	private void initInuse() {
		this.inuses = getSamePrefixFileNamesAsInuseFileNamesInCurrentDir();
	}

	public void addInuse(String inuse) {
		Properties newProperties = this.propertyModel.getProperties(inuse);
		if (newProperties == null)
			return;
		for (String key : this.propertyModel.getAllKeysList()) {
			newProperties.setProperty(key, "");
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(this.prePath + File.separator
					+ inuse));
			newProperties.store(fos, "created by Apusic");
			this.inuses.add(inuse);
		} catch (FileNotFoundException e) {
			PropertiesPlugin.log(e);
		} catch (IOException e) {
			PropertiesPlugin.log(e);
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					PropertiesPlugin.log(e);
				}
		}
	}

	private List<String> getSamePrefixFileNamesAsInuseFileNamesInCurrentDir() {
		List tempList = new ArrayList(2);
		File parent = new File(this.prePath);
		File[] files = parent.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if ((name.endsWith(".properties"))
						&& (name.startsWith(PropertiesModelHelper.this.fileNamePre))) {
					String middleStr = name.substring(
							PropertiesModelHelper.this.fileNamePre.length(),
							name.lastIndexOf(46));
					int middleStrLength = middleStr.length();
					boolean flag = (name
							.startsWith(PropertiesModelHelper.this.fileNamePre
									+ "."))
							|| ((name
									.startsWith(PropertiesModelHelper.this.fileNamePre
											+ "_")) && (((3 == middleStrLength) || ((6 == middleStrLength) && (3 == middleStr
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

	public PropertyModel getModels() throws IOException
	  {
	    String[] inuseFile = (String[])this.inuses.toArray(new String[this.inuses.size()]);
	    Properties[] properties = new Properties[inuseFile.length];
	    for (int i = 0; i < inuseFile.length; ++i)
	    {
	      properties[i] = new Properties();
	      FileInputStream os = null;
	      try
	      {
	        File tempFile = new File(this.prePath + File.separator + inuseFile[i]);
	        if (tempFile.exists())
	        {
	          os = new FileInputStream(tempFile);
	          properties[i].load(os);
	          this.propertyModel.addProperties(inuseFile[i], properties[i]);
	        }
	      }
	      finally
	      {
	        if (os != null)
	        {
	          os.close();
	        }
	      }
	    }

	    return this.propertyModel;
	  }

	public void save() throws Exception {
		int i = 0;
		for (int listSize = this.inuses.size(); i < listSize; ++i) {
			String fileName = (String) this.inuses.get(i);
			Properties properties4save = this.propertyModel
					.getProperties(fileName);
			if (properties4save == null)
				continue;
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File(this.prePath
						+ File.separator + fileName));
				properties4save.store(fos, "created by Apusic");
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
	}
}
