package com.apusic.studio.properties.editor;

import com.apusic.studio.properties.PropertiesImages;
import com.apusic.studio.properties.model.PropertiesModelHelper;
import com.apusic.studio.properties.preferences.PropertiesPreferenceHelper;
import java.util.List;
import java.util.Locale;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AllLocaleComboInputDialog extends TitleAreaDialog {
	private String title;
	private String message;
	private String value = "";
	private IInputValidator validator;
	private Text text;
	private Text errorMessageText;
	private String errorMessage;
	private Combo combo;
	private String namePre;
	private int labelType = 1;
	private static final int DISPLAY_NAME_LAST = 0;
	private static final int DISPLAY_NAME_NOT_LAST = 1;
	private List<Locale> allLocaleList;
	private List<Locale> commonlocaleList;
	private ComboViewer languageCountryComboViewer;
	private Button showAllCheckButton;
	boolean showAll = false;
	private List<String> filePartNameList;

	public AllLocaleComboInputDialog(Shell parentShell, String fileNamePre,
			List<String> filePartNameList) {
		super(parentShell);
		setShellStyle(16908368);
		this.title = Messages
				.getString("PropertiesEditorBasePart.dialog.addFile.title.addFile");
		setTitleImage(PropertiesImages
				.getImage("icons/properties_editor/properties_.gif"));
		this.namePre = fileNamePre;
		this.message = Messages
				.getString("PropertiesEditorBasePart.dialog.addFile.message");
		this.filePartNameList = filePartNameList;
		this.allLocaleList = PropertiesModelHelper.getAllLocale();
		this.commonlocaleList = PropertiesPreferenceHelper
				.getStoredCommonLocale();
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == 0) {
			this.value = this.text.getText();
		}
		super.buttonPressed(buttonId);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (this.title == null)
			return;
		shell.setText(this.title);
	}

	protected Control createButtonBar(Composite parent) {
		Control ret = super.createButtonBar(parent);
		validate();
		return ret;
	}

	private void validate() {
		for (int i = 0; i < this.filePartNameList.size(); ++i) {
			String name = (String) this.filePartNameList.get(i);
			String txt = this.text.getText().trim();
			int index = (name.lastIndexOf(46) > 0) ? name.lastIndexOf(46)
					: name.length();
			if (!(txt.equals(name.substring(0, index))))
				continue;
			setErrorMessage(Messages
					.getString("AllLocaleComboInputDialog.fileExistedError"));
			getButton(0).setEnabled(false);
			return;
		}
		setErrorMessage(null);
		getButton(0).setEnabled(true);
	}

	private void modifyText(int index) {
		List list = (List) this.languageCountryComboViewer.getInput();
		Locale locale = (Locale) list.get(index);
		String str = "";
		String language = locale.getLanguage();
		String country = locale.getCountry();
		str = (("".equals(language)) ? "" : "_") + language
				+ (("".equals(country)) ? "" : "_") + country;
		this.text.setText(this.namePre + str);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));
		setTitle(Messages.getString("ComboInputDialog.partName"));
		if (this.message != null) {
			setMessage(this.message);
		}
		Composite viewerCmp = new Composite(composite, 0);
		viewerCmp.setLayout(new GridLayout(2, false));
		GridData vcgd = new GridData(768);
		viewerCmp.setLayoutData(vcgd);
		this.languageCountryComboViewer = new ComboViewer(viewerCmp, 8);
		this.languageCountryComboViewer
				.setContentProvider(new ArrayContentProvider());
		this.languageCountryComboViewer
				.setLabelProvider(new InnerLabelProvider());
		this.languageCountryComboViewer.setInput(this.commonlocaleList);
		this.combo = this.languageCountryComboViewer.getCombo();
		this.combo.setData("languageCountryComboViewer");
		this.combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = AllLocaleComboInputDialog.this.combo
						.getSelectionIndex();
				Object object = AllLocaleComboInputDialog.this.languageCountryComboViewer
						.getInput();
				if (!(object instanceof List))
					return;
				AllLocaleComboInputDialog.this.modifyText(index);
				AllLocaleComboInputDialog.this.validate();
			}
		});
		int index = setComboIndex(false);
		GridData data = new GridData(768);
		this.combo.setLayoutData(data);
		addFastSelection(this.combo);
		this.showAllCheckButton = new Button(viewerCmp, 32);
		this.showAllCheckButton
				.setText(Messages
						.getString("ConfigureNewResourceFilesPage.showAllCheckButton.text"));
		this.showAllCheckButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean isAll = AllLocaleComboInputDialog.this.showAllCheckButton
						.getSelection();
				AllLocaleComboInputDialog.this.languageCountryComboViewer
						.setInput((isAll) ? AllLocaleComboInputDialog.this.allLocaleList
								: AllLocaleComboInputDialog.this.commonlocaleList);
				AllLocaleComboInputDialog.this.languageCountryComboViewer
						.refresh();
				int index = AllLocaleComboInputDialog.this.setComboIndex(isAll);
				Object object = AllLocaleComboInputDialog.this.languageCountryComboViewer
						.getInput();
				if (!(object instanceof List))
					return;
				AllLocaleComboInputDialog.this.modifyText(index);
				AllLocaleComboInputDialog.this.validate();
			}
		});
		data = new GridData();
		data.horizontalAlignment = 16777224;
		this.showAllCheckButton.setLayoutData(data);
		Composite tCmp = new Composite(composite, 0);
		tCmp.setLayout(new GridLayout(1, false));
		tCmp.setLayoutData(new GridData(768));
		this.text = new Text(tCmp, 2048);
		this.text.setEditable(false);
		this.text.setVisible(false);
		modifyText(index);
		GridData gdata = new GridData(768);
		this.text.setLayoutData(gdata);
		this.text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				AllLocaleComboInputDialog.this.validateInput();
			}
		});
		this.errorMessageText = new Text(composite, 72);
		this.errorMessageText.setLayoutData(new GridData(768));
		this.errorMessageText.setBackground(this.errorMessageText.getDisplay()
				.getSystemColor(22));
		setErrorMessage(this.errorMessage);
		applyDialogFont(composite);
		return composite;
	}

	private int setComboIndex(boolean isAll) {
		List list = (isAll) ? this.allLocaleList : this.commonlocaleList;
		int size = list.size();
		boolean flag = false;
		int index = 0;
		for (int i = 0; i < size; ++i) {
			flag = false;
			Locale locale = (Locale) list.get(i);
			String country = locale.getCountry();
			StringBuffer sb = new StringBuffer();
			sb.append(locale.getLanguage())
					.append(("".equals(country)) ? "" : "_").append(country);
			int listSize = this.filePartNameList.size();
			for (int j = 0; j < listSize; ++j) {
				String name = (String) this.filePartNameList.get(j);
				String temp = sb.toString();
				String cur = this.namePre;
				if (!("".equals(temp))) {
					cur = cur + "_" + temp;
				}
				if (!(cur.equals(name.substring(0, name.lastIndexOf(46)))))
					continue;
				flag = true;
				break;
			}
			if (flag)
				continue;
			index = i;
			break;
		}
		this.combo.select(index);
		return index;
	}

	protected Text getText() {
		return this.text;
	}

	public String getValue() {
		return this.value;
	}

	protected void validateInput() {
		String errorMessage = null;
		if (this.validator != null) {
			errorMessage = this.validator.isValid(this.text.getText());
		}
		setErrorMessage(errorMessage);
		if (errorMessage != null)
			getButton(0).setEnabled(false);
	}

	public boolean isHelpAvailable() {
		return false;
	}

	private void addFastSelection(Combo combo) {
		if (!(Platform.getOS().equals("linux")))
			return;
		combo.addKeyListener(new ComboKeyListener(combo));
	}

	private class ComboKeyListener extends KeyAdapter {
		private Combo combo;

		public ComboKeyListener(Combo paramCombo) {
			this.combo = paramCombo;
		}

		public void keyReleased(KeyEvent e) {
			String typedKey = String.valueOf(e.character).toLowerCase();
			int index = this.combo.getSelectionIndex();
			if ((index != -1)
					&& (this.combo.getItem(index).toLowerCase()
							.startsWith(typedKey))
					&& (this.combo.getItemCount() > ++index)
					&& (this.combo.getItem(index).toLowerCase()
							.startsWith(typedKey))) {
				this.combo.select(index);
			} else {
				for (int i = 0; i < this.combo.getItemCount(); ++i) {
					if (!(this.combo.getItem(i).toLowerCase()
							.startsWith(typedKey)))
						continue;
					this.combo.select(i);
					return;
				}
			}
		}
	}

	private class InnerLabelProvider extends LabelProvider {
		public String getText(Object element) {
			StringBuffer sb = new StringBuffer();
			if (element instanceof Locale) {
				Locale locale = (Locale) element;
				String displayLanguage = locale.getDisplayLanguage();
				if ("".equals(displayLanguage)) {
					sb.append(Messages
							.getString("ConfigureNewResourceFilesPage.language.default"));
				} else {
					String displayCountry = locale.getDisplayCountry();
					String country = locale.getCountry();
					String language = locale.getLanguage();
					if (AllLocaleComboInputDialog.this.labelType == 0) {
						sb.append(displayLanguage)
								.append(("".equals(displayCountry)) ? ""
										: Messages
												.getString("ConfigureNewResourceFilesPage.spr"))
								.append(displayCountry)
								.append(Messages
										.getString("ConfigureNewResourceFilesPage.ltc"))
								.append(language)
								.append(("".equals(country)) ? "" : "_")
								.append(country)
								.append(Messages
										.getString("ConfigureNewResourceFilesPage.rtc"));
					} else if (1 == AllLocaleComboInputDialog.this.labelType) {
						sb.append(language)
								.append(("".equals(country)) ? "" : "_")
								.append(country)
								.append(" ")
								.append(Messages
										.getString("ConfigureNewResourceFilesPage.ltc"))
								.append(displayLanguage)
								.append(("".equals(displayCountry)) ? ""
										: Messages
												.getString("ConfigureNewResourceFilesPage.spr"))
								.append(displayCountry)
								.append(Messages
										.getString("ConfigureNewResourceFilesPage.rtc"));
					}
				}
			}
			return sb.toString();
		}
	}
}
