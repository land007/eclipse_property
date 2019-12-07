package com.apusic.studio.properties.editor;

import com.apusic.studio.properties.PropertiesImages;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MultiLineInputDialog extends TitleAreaDialog {
	private String title;
	private String message;
	private String value = "";
	private IInputValidator validator;
	private Text text;
	private Text errorMessageText;
	private String errorMessage;

	public MultiLineInputDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell);
		setShellStyle(16909392);
		this.title = dialogTitle;
		setTitleImage(PropertiesImages
				.getImage("icons/properties_editor/properties_.gif"));
		this.message = dialogMessage;
		if (initialValue != null) {
			this.value = initialValue;
		}
		this.validator = validator;
	}

	protected void buttonPressed(int buttonId) {
		this.value = ((buttonId == 0) ? this.text.getText() : null);
		super.buttonPressed(buttonId);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (this.title == null)
			return;
		shell.setText(this.title);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		if (this.message != null) {
			setMessage(this.message);
		}
		setTitle(Messages.getString("MultiLineInputDialog.partName"));
		this.text = new Text(composite, 2114);
		GridData data = new GridData(1808);
		data.heightHint = (20 * this.text.getLineHeight());
		data.widthHint = convertHorizontalDLUsToPixels(200);
		this.text.setLayoutData(data);
		this.text.setFocus();
		if (this.value != null) {
			this.text.setText(this.value);
			this.text.selectAll();
		}
		this.text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				MultiLineInputDialog.this.validateInput();
			}
		});
		Label sepLabel = new Label(composite, 258);
		sepLabel.setLayoutData(new GridData(768));
		this.errorMessageText = new Text(composite, 131144);
		this.errorMessageText.setLayoutData(new GridData(768));
		this.errorMessageText.setBackground(this.errorMessageText.getDisplay()
				.getSystemColor(22));
		setErrorMessage(this.errorMessage);
		applyDialogFont(composite);
		return composite;
	}

	protected Text getText() {
		return this.text;
	}

	public String getValue() {
		return ((this.value != null) ? this.value : "");
	}

	public boolean isHelpAvailable() {
		return false;
	}

	protected void validateInput() {
		String errorMessage = null;
		if (this.validator != null) {
			errorMessage = this.validator.isValid(this.text.getText());
		}
		setErrorMessage(errorMessage);
		getButton(0).setEnabled(errorMessage == null);
	}
}
