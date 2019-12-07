package com.apusic.studio.properties.wizard;

import java.util.Locale;
import org.eclipse.jface.viewers.LabelProvider;

public class FileListLabelProvider extends LabelProvider {
	private ConfigureNewResourceFilesPage fConfigureResourceFilePage;

	public FileListLabelProvider(
			ConfigureNewResourceFilesPage configureResourceFilePage) {
		this.fConfigureResourceFilePage = configureResourceFilePage;
	}

	public String getText(Object element) {
		StringBuffer stringBuffer = new StringBuffer();
		if (element instanceof Locale) {
			Locale locale = (Locale) element;
			stringBuffer.append(this.fConfigureResourceFilePage.getTypeName());
			String language = locale.getLanguage();
			if (!("".equals(language))) {
				stringBuffer.append("_");
				stringBuffer.append(locale);
			}
			stringBuffer.append(".");
			stringBuffer.append("properties");
		}
		return stringBuffer.toString();
	}
}
