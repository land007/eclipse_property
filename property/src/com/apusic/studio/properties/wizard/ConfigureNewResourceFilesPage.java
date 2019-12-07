package com.apusic.studio.properties.wizard;

import com.apusic.studio.properties.PropertiesImages;
import com.apusic.studio.properties.model.PropertiesModelHelper;
import com.apusic.studio.properties.preferences.PropertiesPreferenceHelper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class ConfigureNewResourceFilesPage extends NewTypeWizardPage implements
		INewResourceWizardPage {
	private int labelType = 1;
	private static final int DISPLAY_NAME_LAST = 0;
	private static final int DISPLAY_NAME_NOT_LAST = 1;
	private ComboViewer languageCountryComboViewer;
	private Button showAllCheckButton;
	private ListViewer listViewer;
	private Button addButton;
	private Button removeButton;
	private IStatus resourceNameStatus;
	private IStatus listSelectionStatus;
	private Locale currentCombination;
	private java.util.List<Locale> selectedFiles;
	private java.util.List<Locale> allLocaleList;
	private java.util.List<Locale> commonlocaleList;
	private java.util.List<String> existedFileNames;
	private java.util.List<IFile> createdFiles;
	private org.eclipse.swt.widgets.List fileList;
	private Combo languageCountryCombo;
	private boolean editorOPened;

	protected ConfigureNewResourceFilesPage(String pageName) {
		super(true, pageName);
		setTitle(Messages.getString("ConfigureNewResourceFilesPage.title"));
		setDescription(Messages
				.getString("ConfigureNewResourceFilesPage.description"));
		setPageComplete(false);
		this.resourceNameStatus = new StatusInfo();
		this.listSelectionStatus = new StatusInfo();
		this.currentCombination = new Locale("");
		this.allLocaleList = PropertiesModelHelper.getAllLocale();
		this.commonlocaleList = PropertiesPreferenceHelper
				.getStoredCommonLocale();
	}

	public void init(IStructuredSelection selection,
			String newResourceFileName, boolean enableLocation) {
		IJavaElement jelem = getInitialJavaElement(selection);
		initContainerPage(jelem);
		initTypePage(jelem);
		if (!(enableLocation)) {
			setPackageFragmentRoot(getPackageFragmentRoot(), enableLocation);
			setPackageFragment(getPackageFragment(), enableLocation);
		}
		if (newResourceFileName != null) {
			setTypeName(newResourceFileName, enableLocation);
		}
		doStatusUpdate();
	}

	private void doStatusUpdate() {
		IStatus[] status = {
				this.fContainerStatus,
				(isEnclosingTypeSelected()) ? this.fEnclosingTypeStatus
						: this.fPackageStatus, this.resourceNameStatus,
				this.listSelectionStatus };
		updateStatus(status);
	}

	protected void handleFieldChanged(String fieldName) {
		super.handleFieldChanged(fieldName);
		this.resourceNameStatus = resourceNameChanged();
		checkCombination();
		if (this.listViewer != null) {
			this.listViewer.refresh();
		}
		this.listSelectionStatus = listSelectionChanged();
		doStatusUpdate();
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, 0);
		int nColumns = 4;
		composite.setLayout(new GridLayout(nColumns, false));
		createContainerControls(composite, nColumns);
		createPackageControls(composite, nColumns);
		createTypeNameControls(composite, nColumns);
		createSeperatorControls(composite, nColumns);
		createLanguageCountryControls(composite, nColumns);
		Group group = new Group(composite, 4);
		GridData gd = new GridData(768);
		gd.horizontalSpan = nColumns;
		group.setLayoutData(gd);
		group.setText(Messages
				.getString("ConfigureNewResourceFilesPage.arm.files"));
		group.setLayout(new GridLayout(2, false));
		createListControls(group, nColumns);
		createSeperatorControls(composite, nColumns);
		setControl(composite);
		addListener();
	}

	public void setVisible(boolean visible) {
		if (visible) {
			if (getTypeName().length() > 0) {
				checkCombination();
			} else {
				setFocus();
			}
		}
		super.setVisible(visible);
	}

	private void checkRemoveButton() {
		boolean isRemoveButtonEnable = !(this.listViewer.getSelection()
				.isEmpty());
		this.removeButton.setEnabled(isRemoveButtonEnable);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	private java.util.List<String> getExistedPropertiesFileNamesInCurrentDirectory() {
		java.util.List ret = new ArrayList(0);
		IPackageFragment packageFragment = getPackageFragment();
		if (packageFragment != null) {
			String prePath = "";
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
					.getRoot();
			IFile preFile = workspaceRoot.getFile(packageFragment.getPath());
			prePath = preFile.getLocation().toFile().getPath();
			File tempIOFileDirectory = new File(prePath + File.separator);
			File[] files = (File[]) null;
			if (tempIOFileDirectory.isDirectory()) {
				files = tempIOFileDirectory.listFiles();
				if (files != null) {
					for (File f : files) {
						String fileName = f.getName();
						if (!(fileName.endsWith(".properties"))) {
							continue;
						}
						ret.add(fileName);
					}
				}
			}
		}
		return ret;
	}

	protected IStatus packageChanged() {
		IStatus ret = super.packageChanged();
		this.existedFileNames = getExistedPropertiesFileNamesInCurrentDirectory();
		return ret;
	}

	void checkCombination() {
		if ((this.languageCountryComboViewer == null)
				|| (this.addButton == null))
			return;
		StructuredSelection structuredSelection = (StructuredSelection) this.languageCountryComboViewer
				.getSelection();
		Locale locale = (Locale) structuredSelection.getFirstElement();
		String language = "";
		String country = "";
		if (locale != null) {
			language = locale.getLanguage();
			country = locale.getCountry();
		}
		boolean enableAddButton = true;
		this.currentCombination = new Locale(language, country, "");
		int selectedFilesSize = this.selectedFiles.size();
		for (int i = 0; i < selectedFilesSize; ++i) {
			if (!(this.currentCombination.equals(this.selectedFiles.get(i))))
				continue;
			enableAddButton = false;
			break;
		}
		if (enableAddButton) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(getTypeName());
			String currentCombinationlanguage = this.currentCombination
					.getLanguage();
			if (!("".equals(currentCombinationlanguage))) {
				stringBuffer.append("_");
				stringBuffer.append(this.currentCombination);
			}
			stringBuffer.append(".");
			stringBuffer.append("properties");
			try {
				IPackageFragment packageFragment = getPackageFragment();
				if (packageFragment != null) {
					Object[] obj = new Object[0];
					if (packageFragment.isDefaultPackage()) {
						IJavaElement javaElement = packageFragment.getParent();
						if (javaElement != null) {
							obj = ((IPackageFragmentRoot) javaElement)
									.getNonJavaResources();
						}
					} else {
						obj = packageFragment.getNonJavaResources();
					}
					for (int i = 0; i < obj.length; ++i) {
						if ((!(obj[i] instanceof IResource))
								|| (1 != ((IResource) obj[i]).getType()))
							continue;
						IFile file = (IFile) obj[i];
						if (!(file.getName().equals(stringBuffer.toString())))
							continue;
						enableAddButton = false;
						this.addButton.setEnabled(enableAddButton);
						break;
					}
				}
			} catch (JavaModelException localJavaModelException) {
			}
		}
	}

	private void createLanguageCountryControls(Composite composite, int nColumns) {
		Label label = new Label(composite, 0);
		label.setText(Messages
				.getString("ConfigureNewResourceFilesPage.label.languageType"));
		label.setLayoutData(new GridData(0, 16777216, false, false));
		this.languageCountryComboViewer = new ComboViewer(composite, 8);
		this.languageCountryComboViewer
				.setContentProvider(new ArrayContentProvider());
		this.languageCountryComboViewer
				.setLabelProvider(new InnerLabelProvider());
		this.languageCountryComboViewer.setInput(this.commonlocaleList);
		this.languageCountryCombo = this.languageCountryComboViewer.getCombo();
		this.languageCountryCombo.select(0);
		GridData data = new GridData(768);
		data.horizontalSpan = 2;
		this.languageCountryCombo.setLayoutData(data);
		addFastSelection(this.languageCountryCombo);
		this.showAllCheckButton = new Button(composite, 32);
		this.showAllCheckButton
				.setText(Messages
						.getString("ConfigureNewResourceFilesPage.showAllCheckButton.text"));
		this.showAllCheckButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (ConfigureNewResourceFilesPage.this.showAllCheckButton
						.getSelection()) {
					ConfigureNewResourceFilesPage.this.languageCountryComboViewer
							.setInput(ConfigureNewResourceFilesPage.this.allLocaleList);
				} else {
					ConfigureNewResourceFilesPage.this.languageCountryComboViewer
							.setInput(ConfigureNewResourceFilesPage.this.commonlocaleList);
				}
				ConfigureNewResourceFilesPage.this.languageCountryComboViewer
						.refresh();
				ConfigureNewResourceFilesPage.this.languageCountryComboViewer
						.getCombo().select(0);
				ConfigureNewResourceFilesPage.this.checkCombination();
			}
		});
		data = new GridData();
		data.horizontalAlignment = 16777224;
		this.showAllCheckButton.setLayoutData(data);
	}

	private void createListControls(Composite composite, int nColumns) {
		Composite listComposite = new Composite(composite, 0);
		listComposite.setLayout(new GridLayout(2, false));
		this.listViewer = new ListViewer(listComposite, 2818);
		this.listViewer.setContentProvider(new ArrayContentProvider());
		this.listViewer.setLabelProvider(new FileListLabelProvider(this));
		this.selectedFiles = new LinkedList();
		this.listViewer.setInput(this.selectedFiles);
		this.fileList = this.listViewer.getList();
		this.fileList.setLayoutData(new GridData(4, 4, true, true, 1, 5));
		Composite cmp = new Composite(listComposite, 0);
		cmp.setLayout(new GridLayout(1, true));
		this.addButton = new Button(cmp, 8);
		this.addButton.setImage(PropertiesImages
				.getImage("icons/properties_editor/add.gif"));
		this.addButton.setLayoutData(new GridData(0, 0, false, false));
		this.removeButton = new Button(cmp, 8);
		this.removeButton.setEnabled(false);
		this.removeButton.setImage(PropertiesImages
				.getImage("icons/properties_editor/remove.gif"));
		this.removeButton.setLayoutData(new GridData(0, 0, false, false));
		listComposite.setLayoutData(new GridData(4, 4, true, true, 2, 1));
	}

	private void addListener() {
		InnerControlSelectionListener SelectionListener = new InnerControlSelectionListener();
		this.addButton.addSelectionListener(SelectionListener);
		this.removeButton.addSelectionListener(SelectionListener);
		this.languageCountryCombo.addSelectionListener(SelectionListener);
		this.fileList.addSelectionListener(SelectionListener);
	}

	private void createSeperatorControls(Composite composite, int nColumns) {
		Label label = new Label(composite, 258);
		label.setLayoutData(new GridData(4, 0, false, false, nColumns, 1));
	}

	private IStatus resourceNameChanged() {
		String string = getTypeName();
		int severity = 0;
		String message = "OK";
		if (string.length() == 0) {
			severity = 4;
			message = Messages
					.getString("ConfigureNewResourceFilesPage.error.fileName.empty");
		} else {
			IStatus javaConvenstionsStatus = JavaConventions
					.validateJavaTypeName(string, null, null);
			if (javaConvenstionsStatus.getSeverity() == 4) {
				severity = javaConvenstionsStatus.getSeverity();
				message = javaConvenstionsStatus.getMessage();
			} else {
				severity = javaConvenstionsStatus.getSeverity();
				message = javaConvenstionsStatus.getMessage();
			}
			Locale[] ls = (Locale[]) this.selectedFiles.toArray(new Locale[0]);
			for (Locale l : ls) {
				StringBuffer sb = new StringBuffer();
				String country = l.getCountry();
				String language = l.getLanguage();
				sb.append(string).append((language.equals("")) ? "" : "_")
						.append(language)
						.append((country.equals("")) ? "" : "_")
						.append(country).append(".").append("properties");
				String fileName = sb.toString();
				for (int i = 0; i < this.existedFileNames.size(); ++i) {
					if (!(fileName.equals(this.existedFileNames.get(i))))
						continue;
					severity = 4;
					message = Messages
							.getString("ConfigureNewResourceFilesPage.error.fileExist")
							+ fileName;
					org.eclipse.swt.widgets.List list = this.listViewer
							.getList();
					list.deselectAll();
					list.select(this.selectedFiles.indexOf(l));
					list.forceFocus();
					this.removeButton.setEnabled(true);
					return new StatusInfo(severity, message);
				}
			}
		}
		return new StatusInfo(severity, message);
	}

	private IStatus listSelectionChanged() {
		if ((this.selectedFiles != null) && (this.selectedFiles.isEmpty())) {
			return new StatusInfo(
					4,
					Messages.getString("ConfigureNewResourceFilesPage.error.nofilesSelected"));
		}
		return new StatusInfo();
	}

	public int calcProgressMonitorWorkItems() {
		int work = 0;
		work += this.selectedFiles.size() * 5;
		++work;
		return work;
	}

	public void finishPage(IProgressMonitor monitor) throws CoreException {
		IPackageFragmentRoot root = getPackageFragmentRoot();
		IPackageFragment pack = getPackageFragment();
		if (pack == null) {
			pack = root.getPackageFragment("");
		}
		if (!(pack.exists())) {
			String packName = pack.getElementName();
			root.createPackageFragment(packName, true, null);
		}
		monitor.worked(1);
		this.createdFiles = new ArrayList();
		String filename;
		for (int i = 0; i < this.selectedFiles.size(); ++i) {
			filename = ((ILabelProvider) this.listViewer.getLabelProvider())
					.getText(this.selectedFiles.get(i));
			IFile newFile = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(getPackageFragment().getPath().append(filename));
			if (newFile.exists())
				continue;
			this.createdFiles.add(newFile);
		}
		if (this.editorOPened)
			return;
		for (IFile file : this.createdFiles) {
			file.create(new ByteArrayInputStream(new byte[0]), false,
					new SubProgressMonitor(monitor, 5));
		}
	}

	public java.util.List<IFile> getCreateFile() {
		if (this.createdFiles == null) {
			throw new IllegalStateException();
		}
		return this.createdFiles;
	}

	public String getProgressMonitorTaskName() {
		return Messages
				.getString("ConfigureNewResourceFilesPage.msg.creatingFiles");
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
					break;
				}
			}
			ConfigureNewResourceFilesPage.this.checkCombination();
		}
	}

	private class InnerControlSelectionListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			int index;
			if (source == ConfigureNewResourceFilesPage.this.addButton) {
				ConfigureNewResourceFilesPage.this.selectedFiles
						.add(ConfigureNewResourceFilesPage.this.currentCombination);
				ConfigureNewResourceFilesPage.this.listViewer.refresh();
				ConfigureNewResourceFilesPage.this
						.handleFieldChanged("NewContainerWizardPage.container");
				Combo combo = ConfigureNewResourceFilesPage.this.languageCountryComboViewer
						.getCombo();
				index = combo.getSelectionIndex() + 1;
				if (index == combo.getItemCount()) {
					--index;
				}
				combo.select(index);
				ConfigureNewResourceFilesPage.this.checkCombination();
			} else if (source == ConfigureNewResourceFilesPage.this.removeButton) {
				org.eclipse.swt.widgets.List listInListViewer = ConfigureNewResourceFilesPage.this.listViewer
						.getList();
				index = listInListViewer.getSelectionIndex();
				if (index < 0)
					return;
				StructuredSelection selection = (StructuredSelection) ConfigureNewResourceFilesPage.this.listViewer
						.getSelection();
				for (Iterator iterator = selection.iterator(); iterator
						.hasNext();) {
					ConfigureNewResourceFilesPage.this.selectedFiles
							.remove(iterator.next());
				}
				ConfigureNewResourceFilesPage.this.listViewer.refresh();
				if (index == listInListViewer.getItemCount()) {
					--index;
				}
				listInListViewer.select(index);
				ConfigureNewResourceFilesPage.this.checkRemoveButton();
				ConfigureNewResourceFilesPage.this
						.handleFieldChanged("NewContainerWizardPage.container");
				ConfigureNewResourceFilesPage.this.checkCombination();
			} else if (source == ConfigureNewResourceFilesPage.this.languageCountryCombo) {
				ConfigureNewResourceFilesPage.this.checkCombination();
			} else {
				if (source != ConfigureNewResourceFilesPage.this.fileList)
					return;
				ConfigureNewResourceFilesPage.this.checkRemoveButton();
			}
		}
	}

	private class InnerLabelProvider extends LabelProvider {
		public String getText(Object element) {
			StringBuffer stringBuffer = new StringBuffer();
			if (element instanceof Locale) {
				Locale locale = (Locale) element;
				String displayLanguage = locale.getDisplayLanguage();
				if ("".equals(displayLanguage)) {
					stringBuffer
							.append(Messages
									.getString("ConfigureNewResourceFilesPage.language.default"));
				} else {
					String displayCountry = locale.getDisplayCountry();
					String country = locale.getCountry();
					String language = locale.getLanguage();
					if (ConfigureNewResourceFilesPage.this.labelType == 0) {
						stringBuffer
								.append(displayLanguage)
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
					} else if (1 == ConfigureNewResourceFilesPage.this.labelType) {
						stringBuffer
								.append(language)
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
			return stringBuffer.toString();
		}
	}
}
