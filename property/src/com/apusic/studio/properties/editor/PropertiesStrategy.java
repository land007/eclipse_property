package com.apusic.studio.properties.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

public class PropertiesStrategy implements IEditorMatchingStrategy {
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		IEditorPart editor = editorRef.getEditor(false);
		if (editor instanceof PropertiesEditor) {
			String newInputFilePath = input.getToolTipText();
			String newInputFileName = input.getName();
			if (newInputFileName.endsWith(".properties")) {
				String openedEditorInputPath = editorRef.getTitleToolTip();
				String openedEditorInputPartName = editorRef.getPartName();
				int lastSlashIndex = newInputFilePath.lastIndexOf(47);
				if (lastSlashIndex > 0) {
					newInputFilePath = newInputFilePath.substring(0,
							lastSlashIndex);
				}
				int preLastSlashIndex = openedEditorInputPath.lastIndexOf(47);
				if (preLastSlashIndex > 0) {
					openedEditorInputPath = openedEditorInputPath.substring(0,
							preLastSlashIndex);
				}
				int index = newInputFileName.lastIndexOf(46);
				if (index >= 0) {
					newInputFileName = newInputFileName.substring(0, index);
				}
				newInputFileName = PropertiesFileNameValidator
						.getFileNamePre(newInputFileName);
				if ((openedEditorInputPartName.equals(newInputFileName))
						&& (openedEditorInputPath.equals(newInputFilePath))) {
					return true;
				}
			}
		}
		return false;
	}
}
