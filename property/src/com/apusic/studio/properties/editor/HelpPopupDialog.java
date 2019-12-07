package com.apusic.studio.properties.editor;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class HelpPopupDialog extends PopupDialog {
	private String[][] tableInputs;

	protected Point getInitialLocation(Point initialSize) {
		Point point = Display.getCurrent().getCursorLocation();
		return point;
	}

	public HelpPopupDialog(Shell parent, String[][] tableInputs) {
		super(parent, 4, true, false, false, false, Messages
				.getString("PropertiesEditorPageTableSection.usingTip.title"),
				null);
		this.tableInputs = tableInputs;
	}

	protected Control createDialogArea(Composite parent) {
		Composite ret = (Composite) super.createDialogArea(parent);
		ret.setLayout(new GridLayout(3, false));
		for (String[] entry : this.tableInputs) {
			if (entry.length < 2)
				continue;
			Label tipLabel = new Label(ret, 0);
			tipLabel.setText(entry[0]);
			Label spaceLabel = new Label(ret, 0);
			spaceLabel.setVisible(false);
			GridData lgd = new GridData();
			lgd.widthHint = 25;
			spaceLabel.setLayoutData(lgd);
			Label keyLabel = new Label(ret, 0);
			keyLabel.setText(entry[1]);
			keyLabel.setLayoutData(new GridData(768));
		}
		return ret;
	}
}
