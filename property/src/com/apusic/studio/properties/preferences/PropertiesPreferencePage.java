package com.apusic.studio.properties.preferences;

import com.apusic.studio.properties.PropertiesImages;
import com.apusic.studio.properties.PropertiesPlugin;
import com.apusic.studio.properties.model.PropertiesModelHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

public class PropertiesPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	private IPreferenceStore preferenceStore;
	private ListViewer allLanguageListViewer;
	private ListViewer commonsLanguageListViewer;
	private org.eclipse.swt.widgets.List allLanguageList;
	private org.eclipse.swt.widgets.List commonsLanguageList;
	private static final int DEFAULT_LIST_HEIGHT = 400;
	private Button addButton;
	private Button removeButton;
	private Button addAllButton;
	private Button removeAllButton;
	private Label alTipLabel;
	private Label clTipLabel;
	private Button upButton;
	private Button downButton;
	private Button isAscOrderButton;
	private Button isDescOrderButton;
	private ImageHyperlink orderSampleImageLink;
	private Image samplePositiveImage = PropertiesImages
			.getImage("icons/properties_editor/positive.gif");
	private Image sampleNegativeImage = PropertiesImages
			.getImage("icons/properties_editor/negative.gif");
	private java.util.List<Locale> selectedLanguageLocaleList = new ArrayList();
	private java.util.List<Locale> allLanguageLocaleList;
	private java.util.List<Locale> ALL_LOCALE_LIST = PropertiesModelHelper
			.getAllLocale();

	public PropertiesPreferencePage() {
		setDescription(Messages
				.getString("PropertiesPreferencePage.description"));
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(PropertiesPlugin.getDefault().getPreferenceStore());
		this.preferenceStore = getPreferenceStore();
		initViewInputs();
	}

	private void initViewInputs() {
		initCommonLanguageViewInputs();
		initAllLanguageViewInputs();
	}

	private void initCommonLanguageViewInputs() {
		this.selectedLanguageLocaleList = PropertiesPreferenceHelper
				.getStoredCommonLocale();
	}

	private void initAllLanguageViewInputs() {
		this.allLanguageLocaleList = new ArrayList();
		int listSize = this.ALL_LOCALE_LIST.size();
		for (int i = 0; i < listSize; ++i) {
			Locale l = (Locale) this.ALL_LOCALE_LIST.get(i);
			if (this.selectedLanguageLocaleList.contains(l))
				continue;
			this.allLanguageLocaleList.add(l);
		}
	}

	protected Control createContents(Composite parent) {
		TabFolder folder = new TabFolder(parent, 0);
		folder.setLayout(new GridLayout(1, false));
		GridData cgd = new GridData(1808);
		folder.setLayoutData(cgd);
		TabItem languageConfigItem = new TabItem(folder, 0);
		languageConfigItem
				.setText(Messages
						.getString("PropertiesPreferencePage.commonLanguageConfigTab.text"));
		Group languagePane = new Group(folder, 0);
		languagePane.setLayout(new GridLayout(3, false));
		languagePane.setText(Messages
				.getString("PropertiesPreferencePage.group.text"));
		createAllLanguageView(languagePane, 1);
		createControlPane(languagePane, 1);
		createCommonsLanguageView(languagePane, 1);
		GridData gd = new GridData(1808);
		gd.heightHint = 400;
		languagePane.setLayoutData(gd);
		languageConfigItem.setControl(languagePane);
		TabItem editorConfigItem = new TabItem(folder, 0);
		editorConfigItem.setText(Messages
				.getString("PropertiesPreferencePage.editorConfigTab.text"));
		Group orderPane = new Group(folder, 0);
		orderPane.setLayout(new GridLayout(3, false));
		orderPane.setText(Messages
				.getString("PropertiesPreferencePage.orderPane.text"));
		GridData ogd = new GridData(768);
		orderPane.setLayoutData(ogd);
		createOrderControl(orderPane, 1);
		editorConfigItem.setControl(orderPane);
		addDoubleClickListener();
		addSelectionListener();
		return folder;
	}

	private void createOrderControl(Composite composite, int gridColumn) {
		boolean isAsc = PropertiesPreferenceHelper.getStoredOrder();
		this.isAscOrderButton = new Button(composite, 16);
		this.isAscOrderButton.setText(Messages
				.getString("PropertiesPreferencePage.asc"));
		this.isAscOrderButton.setSelection(isAsc);
		this.isDescOrderButton = new Button(composite, 16);
		this.isDescOrderButton.setText(Messages
				.getString("PropertiesPreferencePage.desc"));
		this.isDescOrderButton.setSelection(!(isAsc));
		this.orderSampleImageLink = new ImageHyperlink(composite, 2048);
		Image tmpImg = (isAsc) ? this.samplePositiveImage
				: this.sampleNegativeImage;
		this.orderSampleImageLink.setImage(tmpImg);
		this.orderSampleImageLink.setEnabled(false);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		this.orderSampleImageLink.setLayoutData(gd);
		this.orderSampleImageLink.redraw();
	}

	private void createAllLanguageView(Composite composite, int gridColumn) {
		Composite cmp = new Composite(composite, 0);
		cmp.setLayout(new GridLayout(1, false));
		GridData cgd = new GridData(1808);
		cmp.setLayoutData(cgd);
		this.alTipLabel = new Label(cmp, 0);
		this.alTipLabel.setLayoutData(new GridData(768));
		this.allLanguageListViewer = new ListViewer(cmp, 2562);
		this.allLanguageListViewer
				.setContentProvider(new ArrayContentProvider());
		this.allLanguageListViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				StringBuffer sb = new StringBuffer();
				if (element instanceof Locale) {
					Locale l = (Locale) element;
					String displayCountry = l.getDisplayCountry();
					String country = l.getCountry();
					String displayLanguage = l.getDisplayLanguage();
					if ("".equals(displayLanguage)) {
						sb.append(Messages
								.getString("PropertiesPreferencePage.defaultLanguage"));
					} else {
						sb.append(l.getLanguage())
								.append(("".equals(country)) ? "" : "_")
								.append(country).append(" ").append("(")
								.append(displayLanguage)
								.append(("".equals(displayCountry)) ? "" : "/")
								.append(displayCountry).append(")");
					}
				}
				return sb.toString();
			}
		});
		this.allLanguageListViewer.setInput(this.allLanguageLocaleList);
		GridData gd = new GridData(1808);
		gd.horizontalSpan = gridColumn;
		gd.heightHint = 400;
		this.allLanguageList = this.allLanguageListViewer.getList();
		this.allLanguageList.setLayoutData(gd);
		this.alTipLabel.setText(Messages
				.getString("PropertiesPreferencePage.allLanguageText")
				+ " "
				+ "(" + this.allLanguageList.getItemCount() + ")");
	}

	private void createControlPane(Composite composite, int gridColumn) {
		Composite cmp = new Composite(composite, 0);
		cmp.setLayout(new GridLayout(1, true));
		Label label = new Label(cmp, 0);
		label.setText("");
		this.addButton = new Button(cmp, 0);
		this.addButton.setText(Messages
				.getString("PropertiesPreferencePage.addButtonLabel"));
		this.addButton.setToolTipText(Messages
				.getString("PropertiesPreferencePage.addButtonTip"));
		this.addButton.setLayoutData(new GridData(768));
		this.removeButton = new Button(cmp, 0);
		this.removeButton.setText(Messages
				.getString("PropertiesPreferencePage.removeButtonLabel"));
		this.removeButton.setToolTipText(Messages
				.getString("PropertiesPreferencePage.removeButtonTip"));
		this.removeButton.setLayoutData(new GridData(768));
		label = new Label(cmp, 0);
		label.setText("");
		this.addAllButton = new Button(cmp, 0);
		this.addAllButton.setText(Messages
				.getString("PropertiesPreferencePage.addAllButtonLabel"));
		this.addAllButton.setToolTipText(Messages
				.getString("PropertiesPreferencePage.addAllButtonTip"));
		this.addAllButton.setLayoutData(new GridData(768));
		this.removeAllButton = new Button(cmp, 0);
		this.removeAllButton.setText(Messages
				.getString("PropertiesPreferencePage.removeAllButtonLabel"));
		this.removeAllButton.setToolTipText(Messages
				.getString("PropertiesPreferencePage.removeAllButtonLabelTip"));
		this.removeAllButton.setLayoutData(new GridData(768));
		label = new Label(cmp, 0);
		label.setText("");
		this.upButton = new Button(cmp, 0);
		this.upButton.setText(Messages
				.getString("PropertiesPreferencePage.upButtonLabel"));
		this.upButton.setToolTipText(Messages
				.getString("PropertiesPreferencePage.upButtonTip"));
		this.upButton.setLayoutData(new GridData(768));
		this.downButton = new Button(cmp, 0);
		this.downButton.setText(Messages
				.getString("PropertiesPreferencePage.downButtonLabel"));
		this.downButton.setToolTipText(Messages
				.getString("PropertiesPreferencePage.downButtonTip"));
		this.downButton.setLayoutData(new GridData(768));
		GridData gd = new GridData(1040);
		gd.horizontalSpan = 1;
		cmp.setLayoutData(gd);
	}

	private void addSelectionListener() {
		InnerSelectionListener selectionListener = new InnerSelectionListener();
		this.addButton.addSelectionListener(selectionListener);
		this.removeButton.addSelectionListener(selectionListener);
		this.addAllButton.addSelectionListener(selectionListener);
		this.removeAllButton.addSelectionListener(selectionListener);
		this.upButton.addSelectionListener(selectionListener);
		this.downButton.addSelectionListener(selectionListener);
		this.isAscOrderButton.addSelectionListener(selectionListener);
		this.isDescOrderButton.addSelectionListener(selectionListener);
	}

	private void createCommonsLanguageView(Composite composite, int gridColumn) {
		Composite cmp = new Composite(composite, 0);
		cmp.setLayout(new GridLayout(1, false));
		GridData cgd = new GridData(1808);
		cmp.setLayoutData(cgd);
		this.clTipLabel = new Label(cmp, 0);
		this.clTipLabel.setLayoutData(new GridData(768));
		this.commonsLanguageListViewer = new ListViewer(cmp, 2562);
		this.commonsLanguageListViewer
				.setContentProvider(new ArrayContentProvider());
		this.commonsLanguageListViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				StringBuffer sb = new StringBuffer();
				if (element instanceof Locale) {
					Locale l = (Locale) element;
					String displayCountry = l.getDisplayCountry();
					String country = l.getCountry();
					String displayLanguage = l.getDisplayLanguage();
					if ("".equals(displayLanguage)) {
						sb.append(Messages
								.getString("PropertiesPreferencePage.defaultLanguage"));
					} else {
						sb.append(l.getLanguage())
								.append(("".equals(country)) ? "" : "_")
								.append(country).append(" ").append("(")
								.append(displayLanguage)
								.append(("".equals(displayCountry)) ? "" : "/")
								.append(displayCountry).append(")");
					}
				}
				return sb.toString();
			}
		});
		this.commonsLanguageListViewer
				.setInput(this.selectedLanguageLocaleList);
		GridData gd = new GridData(1808);
		gd.horizontalSpan = gridColumn;
		gd.heightHint = 400;
		this.commonsLanguageList = this.commonsLanguageListViewer.getList();
		this.commonsLanguageList.setLayoutData(gd);
		this.commonsLanguageList.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ((e.character != '') || (e.stateMask != 0))
					return;
				PropertiesPreferencePage.this.handleRemove();
				PropertiesPreferencePage.this.validate();
			}
		});
		this.clTipLabel.setText(Messages
				.getString("PropertiesPreferencePage.commonLanguage.label")
				+ " " + "(" + this.commonsLanguageList.getItemCount() + ")");
	}

	protected void performDefaults() {
		this.selectedLanguageLocaleList = PropertiesPreferenceHelper
				.getDefaultCommonLocale();
		initAllLanguageViewInputs();
		this.commonsLanguageListViewer
				.setInput(this.selectedLanguageLocaleList);
		this.allLanguageListViewer.setInput(this.allLanguageLocaleList);
		this.allLanguageListViewer.refresh();
		this.commonsLanguageListViewer.refresh();
		boolean isAsc = PropertiesPreferenceHelper.getDefaultOrder();
		this.isDescOrderButton.setSelection(!(isAsc));
		this.isAscOrderButton.setSelection(isAsc);
		this.orderSampleImageLink.setImage((isAsc) ? this.samplePositiveImage
				: this.sampleNegativeImage);
		this.orderSampleImageLink.redraw();
		super.performDefaults();
		validate();
	}

	public boolean performOk() {
		doSave2();
		return super.performOk();
	}

	private void doSave() {
		StringBuffer selectedLanguageStr = new StringBuffer();
		int listSize = this.selectedLanguageLocaleList.size();
		for (int i = 0; i < listSize; ++i) {
			Locale l = (Locale) this.selectedLanguageLocaleList.get(i);
			int index = this.ALL_LOCALE_LIST.indexOf(l);
			selectedLanguageStr.append(index + "|");
		}
		String toStoreSelectedLanguageStr = selectedLanguageStr.toString();
		boolean toStoreOrder = this.isAscOrderButton.getSelection();
		this.preferenceStore
				.setValue(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.common",
						toStoreSelectedLanguageStr);
		this.preferenceStore
				.setValue(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.order",
						toStoreOrder);
	}

	private void doSave2() {
		StringBuffer selectedLanguageStr = new StringBuffer();
		int listSize = this.selectedLanguageLocaleList.size();
		String languageName = "";
		String countryName = "";
		for (int i = 0; i < listSize; ++i) {
			Locale locale = (Locale) this.selectedLanguageLocaleList.get(i);
			languageName = locale.getLanguage();
			countryName = locale.getCountry();
			selectedLanguageStr.append(languageName).append('_')
					.append(countryName).append('|');
		}
		String toStoreSelectedLanguageStr = selectedLanguageStr.toString();
		boolean toStoreOrder = this.isAscOrderButton.getSelection();
		this.preferenceStore
				.setValue(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.common",
						toStoreSelectedLanguageStr);
		this.preferenceStore
				.setValue(
						"com.apusic.studio.properties.preferences.PropertiesPreferenceConstants.order",
						toStoreOrder);
	}

	private void validate() {
		String error = null;
		boolean removeEnable = this.commonsLanguageList.getItemCount() > 0;
		boolean addEnable = this.allLanguageList.getItemCount() > 0;
		this.removeButton.setEnabled(removeEnable);
		this.removeAllButton.setEnabled(removeEnable);
		this.addButton.setEnabled(addEnable);
		this.addAllButton.setEnabled(addEnable);
		if (this.selectedLanguageLocaleList.size() <= 0) {
			error = Messages
					.getString("PropertiesPreferencePage.error.commonsNotEmpty");
		}
		this.alTipLabel.setText(Messages
				.getString("PropertiesPreferencePage.allLanguageText")
				+ " "
				+ "(" + this.allLanguageList.getItemCount() + ")");
		this.clTipLabel.setText(Messages
				.getString("PropertiesPreferencePage.commonLanguage.label")
				+ " " + "(" + this.commonsLanguageList.getItemCount() + ")");
		setErrorMessage(error);
		setValid(error == null);
	}

	private void addDoubleClickListener() {
		this.allLanguageListViewer
				.addDoubleClickListener(new IDoubleClickListener() {
					public void doubleClick(DoubleClickEvent event) {
						PropertiesPreferencePage.this.handleAdd();
						PropertiesPreferencePage.this.validate();
					}
				});
		this.commonsLanguageListViewer
				.addDoubleClickListener(new IDoubleClickListener() {
					public void doubleClick(DoubleClickEvent event) {
						PropertiesPreferencePage.this.handleRemove();
						PropertiesPreferencePage.this.validate();
					}
				});
	}

	private void handleAdd() {
		int selectedIndex = this.allLanguageList.getSelectionIndex();
		int size = this.allLanguageList.getSelectionCount();
		if (selectedIndex < 0)
			return;
		StructuredSelection selection = (StructuredSelection) this.allLanguageListViewer
				.getSelection();
		java.util.List existedIndex = new ArrayList();
		int lastIndex = 0;
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			Locale l = (Locale) iterator.next();
			if (!(this.selectedLanguageLocaleList.contains(l))) {
				this.selectedLanguageLocaleList.add(l);
			}
			existedIndex.add(Integer.valueOf(this.selectedLanguageLocaleList
					.indexOf(l)));
			lastIndex = this.allLanguageLocaleList.indexOf(l);
			if (lastIndex == this.allLanguageLocaleList.size() - 1) {
				--lastIndex;
			}
			this.allLanguageLocaleList.remove(l);
		}
		this.commonsLanguageListViewer.refresh();
		this.commonsLanguageList.deselectAll();
		for (Iterator iterator = existedIndex.iterator(); iterator.hasNext();) {
			int i = ((Integer) iterator.next()).intValue();
			this.commonsLanguageList.select(i);
		}
		this.allLanguageListViewer.refresh();
		this.allLanguageList.deselectAll();
		this.allLanguageList
				.select((size == 1) ? selectedIndex
						: (selectedIndex == this.allLanguageList.getItemCount()) ? selectedIndex - 1
								: lastIndex);
	}

	private void handleAddAll() {
		this.selectedLanguageLocaleList = new ArrayList(this.ALL_LOCALE_LIST);
		this.commonsLanguageListViewer
				.setInput(this.selectedLanguageLocaleList);
		this.commonsLanguageListViewer.refresh();
		this.allLanguageLocaleList.clear();
		this.allLanguageListViewer.setInput(this.allLanguageLocaleList);
		this.allLanguageListViewer.refresh();
	}

	private void handleRemove() {
		int selectedIndex = this.commonsLanguageList.getSelectionIndex();
		int size = this.commonsLanguageList.getSelectionCount();
		if (selectedIndex < 0)
			return;
		StructuredSelection selection = (StructuredSelection) this.commonsLanguageListViewer
				.getSelection();
		java.util.List originIndex = new ArrayList();
		java.util.List removedLocaleList = new ArrayList();
		int lastIndex = 0;
		Locale l;
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			l = (Locale) iterator.next();
			lastIndex = this.selectedLanguageLocaleList.indexOf(l);
			if (lastIndex == this.selectedLanguageLocaleList.size() - 1) {
				--lastIndex;
			}
			this.selectedLanguageLocaleList.remove(l);
			removedLocaleList.add(l);
			originIndex.add(Integer.valueOf(this.allLanguageLocaleList
					.indexOf(l)));
		}
		this.commonsLanguageListViewer.refresh();
		this.allLanguageListViewer.refresh();
		this.allLanguageLocaleList.clear();
		for (int i = 0; i < this.ALL_LOCALE_LIST.size(); ++i) {
			l = (Locale) this.ALL_LOCALE_LIST.get(i);
			if (this.selectedLanguageLocaleList.contains(l))
				continue;
			this.allLanguageLocaleList.add(l);
		}
		this.commonsLanguageListViewer.refresh();
		this.allLanguageListViewer.refresh();
		if (selectedIndex == this.commonsLanguageList.getItemCount()) {
			--selectedIndex;
		}
		this.commonsLanguageList
				.select((size == 1) ? selectedIndex
						: (selectedIndex == this.commonsLanguageList
								.getItemCount()) ? selectedIndex - 1
								: lastIndex);
		this.allLanguageList.deselectAll();
		for (int i = 0; i < removedLocaleList.size(); ++i) {
			this.allLanguageList.select(this.allLanguageLocaleList
					.indexOf(removedLocaleList.get(i)));
		}
	}

	private void handleRemoveAll() {
		this.selectedLanguageLocaleList.clear();
		this.commonsLanguageListViewer.refresh();
		this.allLanguageLocaleList = new ArrayList(this.ALL_LOCALE_LIST);
		this.allLanguageListViewer.setInput(this.allLanguageLocaleList);
		this.allLanguageListViewer.refresh();
	}

	private void handleUp() {
		StructuredSelection selection = (StructuredSelection) this.commonsLanguageListViewer
				.getSelection();
		if (selection == null)
			return;
		Object object = selection.getFirstElement();
		Locale l = (Locale) object;
		int index = this.selectedLanguageLocaleList.indexOf(l);
		if (index <= 0)
			return;
		int size = this.commonsLanguageList.getSelectionCount();
		if (1 == size) {
			Locale temp = (Locale) this.selectedLanguageLocaleList.get(index);
			this.selectedLanguageLocaleList.add(index - 1, temp);
			this.selectedLanguageLocaleList.remove(index + 1);
		} else {
			java.util.List list = new ArrayList();
			for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
				Locale ll = (Locale) iterator.next();
				list.add(ll);
				this.selectedLanguageLocaleList.remove(ll);
			}
			int i = index;
			for (int j = 0; j < size; ++j) {
				this.selectedLanguageLocaleList.add(index - 1,
						(Locale) list.get(j));
				++i;
			}
		}
		this.commonsLanguageListViewer.refresh();
	}

	private void handleDown() {
		StructuredSelection selection = (StructuredSelection) this.commonsLanguageListViewer
				.getSelection();
		if (selection == null)
			return;
		Object object = selection.getFirstElement();
		Locale l = (Locale) object;
		int index = this.selectedLanguageLocaleList.indexOf(l);
		if (index > this.commonsLanguageList.getItemCount() - 2)
			return;
		int size = this.commonsLanguageList.getSelectionCount();
		if (1 == size) {
			Locale temp = (Locale) this.selectedLanguageLocaleList.get(index);
			this.selectedLanguageLocaleList.add(index + 2, temp);
			this.selectedLanguageLocaleList.remove(index);
		} else {
			java.util.List list = new ArrayList();
			java.util.List indexList = new ArrayList();
			int lastIndex = index;
			for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
				Locale ll = (Locale) iterator.next();
				lastIndex = this.selectedLanguageLocaleList.indexOf(ll);
				indexList.add(Integer.valueOf(lastIndex));
				list.add(ll);
			}
			int i = lastIndex + 2;
			for (int j = 0; j < size; ++j) {
				this.selectedLanguageLocaleList.add(i, (Locale) list.get(j));
				++i;
			}
			for (i = 0; i < size; ++i) {
				this.selectedLanguageLocaleList.remove(((Integer) indexList
						.get(size - 1 - i)).intValue());
			}
		}
		this.commonsLanguageListViewer.refresh();
	}

	private class InnerSelectionListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == PropertiesPreferencePage.this.addButton) {
				PropertiesPreferencePage.this.handleAdd();
			} else if (source == PropertiesPreferencePage.this.addAllButton) {
				PropertiesPreferencePage.this.handleAddAll();
			} else if (source == PropertiesPreferencePage.this.removeButton) {
				PropertiesPreferencePage.this.handleRemove();
			} else if (source == PropertiesPreferencePage.this.removeAllButton) {
				PropertiesPreferencePage.this.handleRemoveAll();
			} else if (source == PropertiesPreferencePage.this.upButton) {
				PropertiesPreferencePage.this.handleUp();
			} else if (source == PropertiesPreferencePage.this.downButton) {
				PropertiesPreferencePage.this.handleDown();
			} else if (source == PropertiesPreferencePage.this.isAscOrderButton) {
				PropertiesPreferencePage.this.orderSampleImageLink
						.setImage(PropertiesPreferencePage.this.samplePositiveImage);
				PropertiesPreferencePage.this.orderSampleImageLink.redraw();
			} else if (source == PropertiesPreferencePage.this.isDescOrderButton) {
				PropertiesPreferencePage.this.orderSampleImageLink
						.setImage(PropertiesPreferencePage.this.sampleNegativeImage);
				PropertiesPreferencePage.this.orderSampleImageLink.redraw();
			}
			PropertiesPreferencePage.this.validate();
		}
	}
}
