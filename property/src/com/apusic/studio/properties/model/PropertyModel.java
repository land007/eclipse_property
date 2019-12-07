package com.apusic.studio.properties.model;

import com.apusic.studio.properties.preferences.PropertiesPreferenceHelper;
import com.apusic.studio.properties.util.StrUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertyModel {
	public static final String PROP_UNDEFINED = "";
	private PropertiesModelHelper propertiesModelHelper;
	private Map<String, Properties> propertiesFiles = new HashMap(5);
	private List<String> filesNameList = new ArrayList();
	private List<String> allKeysList = new ArrayList();
	private List<PropertyBean> beanList = new ArrayList();

	public void modifyKey(String oldKey, String newKey) {
		int index = this.allKeysList.indexOf(oldKey);
		if (index < 0) {
			return;
		}
		this.allKeysList.set(index, newKey);
		Iterator iter = this.propertiesFiles.values().iterator();
		while (iter.hasNext()) {
			Properties tempProperties = (Properties) iter.next();
			String value = tempProperties.getProperty(oldKey);
			if (value == null) {
				value = "";
			}
			tempProperties.setProperty(newKey, value);
			tempProperties.remove(oldKey);
		}
	}

	public PropertyModel(PropertiesModelHelper propertiesModelHelper) {
		this.propertiesModelHelper = propertiesModelHelper;
	}

	public PropertiesModelHelper getPropertiesModelHelper() {
		return this.propertiesModelHelper;
	}

	public List<String> getAllKeysList() {
		return this.allKeysList;
	}

	public List<String> getFilesNameList() {
		return this.filesNameList;
	}

	public void addProperties(String name, Properties properties) {
		if (!(StrUtil.isNotNullNotEmpty(name)))
			return;
		this.propertiesFiles.put(name, properties);
	}

	public void addProperties(String name) {
		if (!(StrUtil.isNotNullNotEmpty(name)))
			return;
		this.propertiesFiles.put(name, new Properties());
		this.propertiesModelHelper.addInuse(name);
	}

	public void removeProperties(String name) {
		if (!(StrUtil.isNotNullNotEmpty(name)))
			return;
		this.propertiesFiles.remove(name);
	}

	public Properties getProperties(String name) {
		Properties ret = null;
		if (StrUtil.isNotNullNotEmpty(name)) {
			ret = (Properties) this.propertiesFiles.get(name);
		}
		return ret;
	}

	public Map<String, Properties> getPropertiesFiles() {
		return this.propertiesFiles;
	}

	public void removeRow(String key) {
		if (key == null)
			return;
		for (Properties properties : this.propertiesFiles.values()) {
			properties.remove(key);
		}
		this.allKeysList.remove(key);
	}

	public void merge(String key, String value, String filePartName) {
		int i = 0;
		for (int size = this.filesNameList.size(); i < size; ++i) {
			String tempStr = (String) this.filesNameList.get(i);
			if (!(tempStr.equals(filePartName)))
				continue;
			Properties properties = (Properties) this.propertiesFiles
					.get(tempStr);
			properties.setProperty(key, value);
		}
	}

	public void addPropertiesKey(String key) {
		for (Properties properties : this.propertiesFiles.values()) {
			properties.setProperty(key, "");
		}
	}

	public List makeBeans()
    {
        Set fileNamesSet = propertiesFiles.keySet();
        String srcFileName;
        for(Iterator iterator = fileNamesSet.iterator(); iterator.hasNext(); filesNameList.add(srcFileName))
            srcFileName = (String)iterator.next();

        Collection propertiesInstancesSet = propertiesFiles.values();
        int index = 1;
        boolean isFirst = true;
        for(Iterator iterator1 = propertiesInstancesSet.iterator(); iterator1.hasNext();)
        {
            Properties tempProperties = (Properties)iterator1.next();
            Set pkSet = tempProperties.keySet();
            Iterator pkItr = pkSet.iterator();
            if(isFirst)
            {
                PropertyBean pb = null;
                for(; pkItr.hasNext(); beanList.add(pb))
                {
                    pb = new PropertyBean();
                    String key = pkItr.next().toString();
                    String val = tempProperties.getProperty(key);
                    pb.setKey(key);
                    pb.addValue(val != null ? val : "");
                    allKeysList.add(key);
                }

                isFirst = false;
            } else
            {
                int isNewNum = 0;
                while(pkItr.hasNext()) 
                {
                    String key = pkItr.next().toString();
                    String val = tempProperties.getProperty(key);
                    if(!allKeysList.contains(key))
                    {
                        PropertyBean pb = new PropertyBean();
                        isNewNum++;
                        pb.setKey(key);
                        for(int i = 0; i < index - 1; i++)
                            pb.addValue("");

                        pb.addValue(val);
                        allKeysList.add(key);
                        beanList.add(pb);
                    }
                }
                int allKeysListSize = allKeysList.size();
                for(int i = 0; i < allKeysListSize - isNewNum; i++)
                {
                    String val = tempProperties.getProperty((String)allKeysList.get(i));
                    if(val == null)
                        val = "";
                    ((PropertyBean)beanList.get(i)).addValue(val);
                }

            }
            index++;
        }

        Collections.sort(beanList);
        if(!PropertiesPreferenceHelper.getStoredOrder())
            Collections.reverse(beanList);
        return beanList;
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
}
