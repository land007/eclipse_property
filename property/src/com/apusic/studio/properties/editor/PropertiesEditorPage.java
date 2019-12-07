package com.apusic.studio.properties.editor;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class PropertiesEditorPage extends FormPage {
	private PropertiesEditor propertiesEditor;
	private PropertiesEditorPageTableSection pebPart;

	public PropertiesEditorPageTableSection getPebPart() {
		return this.pebPart;
	}

	public PropertiesEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
		this.propertiesEditor = ((PropertiesEditor) editor);
	}

	public PropertiesEditor getEditor() {
		return this.propertiesEditor;
	}

	PropertiesEditorPageTableSection getPropertiesEditorBasePart() {
		return this.pebPart;
	}

	protected void createFormContent(IManagedForm managedForm) {
		boolean isRegularInput = this.propertiesEditor.isRegularInput();
		if (!(isRegularInput)) {
			return;
		}
		ScrolledForm form = managedForm.getForm();
		Composite body = form.getBody();
		GridLayout gridLayout = new GridLayout(2, false);
		body.setLayout(gridLayout);
		FormEditor editor = getEditor();
		FormToolkit toolkit = editor.getToolkit();
		GridData gd = new GridData(1810);
		Composite com = new Composite(body, 0);
		GridLayout comlayout = new GridLayout();
		com.setLayout(comlayout);
		com.setLayoutData(gd);
		com.setBackground(body.getBackground());
		this.pebPart = new PropertiesEditorPageTableSection(com, toolkit, 256,
				this);
		gd = new GridData(1808);
		this.pebPart.getSection().setLayoutData(gd);
		this.pebPart.getSection().setExpanded(true);
		managedForm.addPart(this.pebPart);
	}
}
