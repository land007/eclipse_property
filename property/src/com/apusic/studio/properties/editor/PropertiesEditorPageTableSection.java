package com.apusic.studio.properties.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.apusic.studio.properties.PropertiesPlugin;
import com.apusic.studio.properties.model.PropertyModel;
import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.urlshow.pinyin.PinYin;
import com.urlshow.pinyin.ToneType;

public class PropertiesEditorPageTableSection extends SectionPart {
	private static final int DEFAULT_COLUMN_WEIGHT = 130;
	private static final String KEY = Messages
			.getString("PropertiesEditorBasePart.KEY");
	private static final String DEFAULT_KEY_PREFIX = "Key";
	private static final String DEFAULT_KEY_COPY_SUFFIX = ".copy";
	private static final String DEFAULT_VALUE = "";
	private static final int TOOLTIP_VITICAL_OFFSET = 5;
	private String[] columnNames;
	private PropertyModel propertyModel;
	private int resourceFilesNum = 0;
	private int currentColumn;
	private List<PropertyModel.PropertyBean> beanList;
	private List<String> filesNameList;
	private CellEditor[] editors;
	private boolean sortType = true;
	private int sortOrder = 0;
	private PropertiesEditor propertiesEditor;
	private PropertiesEditorPage propertiesEditorPage;
	private TableViewer propertyTableViewer;
	private Button addColumnButton;
	private Button addRowButton;
	private Button removeRowButton;
	private Combo searchTextCombo;
	private String searchStr = "";
	private Button caseSetvCheckBox;
	private boolean isCaseSenstive = false;
	private PropertyModel.PropertyBean tempPb4Copy;
	private ToolTip duplicatedToolTip;
	private HelpPopupDialog helpPopupDialog;
	private Table propertyTable;

	private static String PUNCTUATION = "，|。|？|；|：|“|‘|【|】|、|～|·|！|…";
	private static String PUNCTUATION_REPLACEALL = "\\s|&nbsp;|"+PUNCTUATION;
	private static String PUNCTUATION_CHARACTERS = "\\u4e00-\\u9fa5|"+PUNCTUATION;
	//([\u4e00-\u9fa5]|(?<=[\u4e00-\u9fa5])(\s)+?(?=[\u4e00-\u9fa5])|&nbsp;)+
	private static String REGEX = "(["+PUNCTUATION_CHARACTERS+"]|(?<=["+PUNCTUATION_CHARACTERS+"])(\\s)+?(?=["+PUNCTUATION_CHARACTERS+"])|&nbsp;)+";//"[\u4e00-\u9fa5|，|。|？|；|：|“|‘|【|】|、|～|·|！]+"; //"[\u4e00-\u9fa5]+"; //"[^\\x00-\\xff]+";
//	private static String FREGEX = "[^\u4e00-\u9fa5|，|。|？|；|：|“|‘|【|】|、|～|·|！]+";//"[^\u4e00-\u9fa5]+";
	private static String SPLIT = "_";
	private static String BRACKETS1 = "-";
	private static String BRACKETS2 = "";

	private IDocument document = null;
	private String text = "";
	private int offset = 0;
	private int length = 0;

	public TableViewer getPropertyTv() {
		return this.propertyTableViewer;
	}

	public void dispose() {
		super.dispose();
		if (this.duplicatedToolTip != null)
			this.duplicatedToolTip.dispose();
	}

	public PropertiesEditorPageTableSection(Composite parent,
			FormToolkit toolkit, int style, PropertiesEditorPage pep) {
		super(parent, toolkit, style);
		this.propertiesEditorPage = pep;
		this.propertiesEditor = pep.getEditor();
		getSection()
				.setText(
						Messages.getString("PropertiesEditorPageTableSection.title.partition")
								+ " " + this.propertiesEditor.getFileNamePre());
		Composite client = toolkit.createComposite(getSection());
		init();
		createContents(client, toolkit);
		getSection().setClient(client);
		if (this.propertyTable.getItemCount() > 0) {
			this.propertyTable.select(0);
			IStructuredSelection talbeSelection = (IStructuredSelection) this.propertyTableViewer
					.getSelection();
			PropertyModel.PropertyBean firstPb = (PropertyModel.PropertyBean) talbeSelection
					.getFirstElement();
			this.propertyTableViewer.editElement(firstPb, 0);
		} else {
			PropertyModel.PropertyBean pb = new PropertyModel.PropertyBean();
			String newKey = getDefaultKeyName();
			pb.setKey(newKey);
			this.propertyModel.getAllKeysList().add(newKey);
			for (int i = 0; i < this.resourceFilesNum; ++i) {
				pb.addValue("");
			}
			this.beanList.add(pb);
			this.propertyTableViewer.setInput(this.beanList);
			this.propertyModel.addPropertiesKey(newKey);
			this.propertyTable.select(0);
			this.propertyTableViewer.refresh();
			this.propertyTableViewer.editElement(
					((IStructuredSelection) this.propertyTableViewer
							.getSelection()).getFirstElement(), 0);
			setFocus();
			this.propertyTable.forceFocus();
		}
	}

	private void init() {
		this.propertyModel = this.propertiesEditor.getPropertyModel();
		this.beanList = this.propertyModel.makeBeans();
		this.filesNameList = this.propertyModel.getFilesNameList();
		this.resourceFilesNum = this.filesNameList.size();
		this.columnNames = new String[this.resourceFilesNum + 1];
		this.columnNames[0] = KEY;
		for (int i = 1; i < this.columnNames.length; ++i) {
			this.columnNames[i] = ((String) this.filesNameList.get(i - 1));
		}
	}

	public PropertiesEditorPage getPage() {
		return this.propertiesEditorPage;
	}

	public void createContents(Composite container, FormToolkit toolkit) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		Composite searchPaneComposite = new Composite(container, 0);
		GridData srhGd = new GridData(1808);
		searchPaneComposite.setLayoutData(srhGd);
		searchPaneComposite.setLayout(new GridLayout(2, false));
		this.searchTextCombo = new Combo(searchPaneComposite, 2048);
		GridData stGd = new GridData(769);
		this.searchTextCombo.setLayoutData(stGd);
		this.searchTextCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String tmpStr = PropertiesEditorPageTableSection.this.searchTextCombo
						.getText().trim();
				PropertiesEditorPageTableSection.this.searchStr = ((PropertiesEditorPageTableSection.this.isCaseSenstive) ? tmpStr
						: tmpStr.toLowerCase());
				PropertiesEditorPageTableSection.this.propertyTableViewer
						.refresh();
			}
		});
		this.searchTextCombo.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				String str = PropertiesEditorPageTableSection.this.searchTextCombo
						.getText().trim();
				if (str.length() <= 0)
					return;
				int index = PropertiesEditorPageTableSection.this.searchTextCombo
						.indexOf(str);
				if (index < 0) {
					PropertiesEditorPageTableSection.this.searchTextCombo.add(
							str, 0);
				} else {
					PropertiesEditorPageTableSection.this.searchTextCombo
							.remove(index);
					PropertiesEditorPageTableSection.this.searchTextCombo.add(
							str, 0);
				}
				PropertiesEditorPageTableSection.this.searchTextCombo.select(0);
			}
		});
		this.caseSetvCheckBox = new Button(searchPaneComposite, 32);
		GridData cscb = new GridData(3);
		this.caseSetvCheckBox.setLayoutData(cscb);
		this.caseSetvCheckBox.setText(Messages
				.getString("PropertiesEditorBasePart.isCaseSensitive"));
		this.caseSetvCheckBox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PropertiesEditorPageTableSection.this.isCaseSenstive = PropertiesEditorPageTableSection.this.caseSetvCheckBox
						.getSelection();
				String tmpStr = PropertiesEditorPageTableSection.this.searchTextCombo
						.getText().trim();
				PropertiesEditorPageTableSection.this.searchStr = ((PropertiesEditorPageTableSection.this.isCaseSenstive) ? tmpStr
						: tmpStr.toLowerCase());
				PropertiesEditorPageTableSection.this.propertyTableViewer
						.refresh();
			}
		});
		this.propertyTableViewer = new TableViewer(searchPaneComposite, 67586) {
			protected void triggerEditorActivationEvent(
					ColumnViewerEditorActivationEvent event) {
				Object object = event.getSource();
				if (!(object instanceof ViewerCell))
					return;
				EventObject eventObject = event.sourceEvent;
				if (eventObject instanceof MouseEvent) {
					ViewerCell currentViewerCell = (ViewerCell) object;
					int currentIndex = currentViewerCell.getColumnIndex();
					PropertiesEditorPageTableSection.this.currentColumn = currentIndex;
					Rectangle currentCellRectangle = currentViewerCell
							.getViewerRow().getBounds(currentIndex);
					String str = currentViewerCell.getText();
					Font font = currentViewerCell.getControl().getFont();
					GC gc = new GC(Display.getDefault());
					gc.setFont(font);
					Point size = gc.stringExtent(str);
					int textLength = size.x;
					gc.dispose();
					Rectangle validRectangle = new Rectangle(
							currentCellRectangle.x, currentCellRectangle.y,
							(textLength <= 15) ? 15 : textLength,
							currentCellRectangle.height);
					MouseEvent mouseEvent = (MouseEvent) eventObject;
					if (!(validRectangle.contains(mouseEvent.x, mouseEvent.y)))
						return;
					super.triggerEditorActivationEvent(event);
				} else {
					super.triggerEditorActivationEvent(event);
				}
			}
		};
		this.propertyTableViewer.getControl().setLayoutData(new GridData(1808));
		this.propertyTable = this.propertyTableViewer.getTable();
		this.propertyTable.setLayoutData(new GridData(1808));
		this.propertyTable.setLinesVisible(true);
		this.propertyTable.setHeaderVisible(true);
		this.propertyTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				PropertiesEditorPageTableSection.this.hideKeyDupToolTip();
			}
		});
		this.propertyTable.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				PropertiesEditorPageTableSection.this.hideKeyDupToolTip();
				IStructuredSelection selection = (IStructuredSelection) PropertiesEditorPageTableSection.this.propertyTableViewer
						.getSelection();
				PropertyModel.PropertyBean firstSelectPropertyBean = (PropertyModel.PropertyBean) selection
						.getFirstElement();
				if (firstSelectPropertyBean == null)
					return;
				PropertiesEditorPageTableSection.this.handleCommonKeyEvent(
						firstSelectPropertyBean, event);
				if (('' == event.character) && (event.stateMask == 0)) {
					PropertiesEditorPageTableSection.this.handleRemoveRow();
				} else if ((61 == event.keyCode) && (262144 == event.stateMask)) {
					PropertiesEditorPageTableSection.this.searchTextCombo
							.setText("");
					label575: PropertiesEditorPageTableSection.this
							.handleAddRowAfterCurrentRow(getDefaultKeyName(),
									-1);
				} else if ((((99 == event.keyCode) || (67 == event.keyCode)))
						&& (262144 == event.stateMask)) {
					if (PropertiesEditorPageTableSection.this.tempPb4Copy == null) {
						PropertiesEditorPageTableSection.this.tempPb4Copy = new PropertyModel.PropertyBean();
					}
					String copiedKeyName = firstSelectPropertyBean.getKey();
					PropertiesEditorPageTableSection.this.tempPb4Copy
							.setKey(copiedKeyName + ".copy");
					PropertiesEditorPageTableSection.this.tempPb4Copy
							.getValues().clear();
					List tempValuesList = firstSelectPropertyBean.getValues();
					int i = 0;
					for (int size = tempValuesList.size(); i < size; ++i) {
						PropertiesEditorPageTableSection.this.tempPb4Copy
								.addValue((String) tempValuesList.get(i));
					}
				} else {
					if (((118 != event.keyCode) && (86 != event.keyCode))
							|| (262144 != event.stateMask)
							|| (PropertiesEditorPageTableSection.this.tempPb4Copy == null))
						return;
					firstSelectPropertyBean.getValues().clear();
					List tempValuesList = PropertiesEditorPageTableSection.this.tempPb4Copy
							.getValues();
					int listSize = tempValuesList.size();
					for (int i = 0; i < listSize; ++i) {
						firstSelectPropertyBean
								.addValue((String) tempValuesList.get(i));
						PropertiesEditorPageTableSection.this.propertyModel.merge(
								firstSelectPropertyBean.getKey(),
								(String) tempValuesList.get(i),
								PropertiesEditorPageTableSection.this.columnNames[(i + 1)]);
					}
					String oldKey = firstSelectPropertyBean.getKey();
					String newKey = PropertiesEditorPageTableSection.this.tempPb4Copy
							.getKey();
					int index = newKey.lastIndexOf(46);
					index = (index > 0) ? index : newKey.length();
					if (!(oldKey.equals(newKey.subSequence(0, index)))) {
						StringBuffer toPasteKey = new StringBuffer();
						toPasteKey.append(newKey);
						Object[] existKeys = PropertiesEditorPageTableSection.this.propertyModel
								.getAllKeysList().toArray();
						for (int i = 1;; ++i) {
							toPasteKey.append(i);
							for (int j = 0; j < existKeys.length; ++j) {
								if (!(toPasteKey.toString()
										.equals(existKeys[j])))
									continue;
								int length = toPasteKey.length();
								toPasteKey.delete(length
										- String.valueOf(i).length(), length);
								PropertiesEditorPageTableSection.this
										.handleAddRowAfterCurrentRow(
												getDefaultKeyName(), -1);
							}
							break;
						}
						String pastedNewKey = toPasteKey.toString();
						firstSelectPropertyBean.setKey(pastedNewKey);
						PropertiesEditorPageTableSection.this.propertyModel
								.modifyKey(oldKey, pastedNewKey);
					}
					PropertiesEditorPageTableSection.this.propertiesEditor
							.setDirty(true);
					PropertiesEditorPageTableSection.this.markDirty();
					PropertiesEditorPageTableSection.this.propertyTableViewer
							.refresh();
					PropertiesEditorPageTableSection.this.updateButtons();
				}
			}
		});
		TableColumn tableColumn = new TableColumn(this.propertyTable, 16384);
		tableColumn.setText(KEY);
		tableColumn.setWidth(DEFAULT_COLUMN_WEIGHT);
		tableColumn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Collections
						.sort(PropertiesEditorPageTableSection.this.beanList);
				if (!(PropertiesEditorPageTableSection.this.sortType)) {
					Collections
							.reverse(PropertiesEditorPageTableSection.this.beanList);
				}
				PropertiesEditorPageTableSection.this.sortType = (!(PropertiesEditorPageTableSection.this.sortType));
				PropertiesEditorPageTableSection.this.sortOrder += 1;
				PropertiesEditorPageTableSection.this.propertyTableViewer
						.refresh();
			}
		});
		for (int i = 0; i < this.resourceFilesNum; ++i) {
			tableColumn = new TableColumn(this.propertyTable, 16384);
			String columnName = (String) this.filesNameList.get(i);
			tableColumn.setText(columnName.substring(0,
					columnName.lastIndexOf(46)));
			tableColumn.setWidth(DEFAULT_COLUMN_WEIGHT);
		}
		this.propertyTableViewer
				.setContentProvider(new InnerArrayContentProvider());
		this.propertyTableViewer
				.setLabelProvider(new InnerPropertiesTableLabelProvider());
		this.propertyTableViewer.setInput(this.beanList);
		this.propertyTableViewer.setColumnProperties(this.columnNames);
		this.propertyTableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						PropertiesEditorPageTableSection.this.updateButtons();
					}
				});
		this.propertyTableViewer
				.addDoubleClickListener(new TableDoubleClickListener());
		this.editors = new CellEditor[this.resourceFilesNum + 1];
		for (int i = 0; i < this.resourceFilesNum + 1; ++i) {
			this.editors[i] = new TextCellEditor(this.propertyTable);
		}
		this.propertyTableViewer.setCellEditors(this.editors);
		this.propertyTableViewer.setCellModifier(new InnerCellModifier());
		CellEditor[] cellEditors = this.propertyTableViewer.getCellEditors();
		for (int i = 0; i < cellEditors.length; ++i) {
			Control control = cellEditors[i].getControl();
			if (control == null)
				continue;
			control.addKeyListener(new InnerCellEditorKeyListener());
			control.addTraverseListener(new InnerCellEditorTraverseListener());
		}
		GridData gd = new GridData(1808);
		gd.horizontalSpan = 2;
		gd.heightHint = 350;
		gd.widthHint = 600;
		this.propertyTableViewer.getControl().setLayoutData(gd);
		Composite ctrlPaneComposite = new Composite(container, 128);
		ctrlPaneComposite.setLayout(new GridLayout(1, false));
		GridData data = new GridData(DEFAULT_COLUMN_WEIGHT);
		data.verticalSpan = 1;
		ctrlPaneComposite.setLayoutData(data);
		toolkit.createLabel(ctrlPaneComposite, "");
		toolkit.createLabel(ctrlPaneComposite, "");
		this.addRowButton = new Button(ctrlPaneComposite, 128);
		this.addRowButton.setText(Messages
				.getString("PropertiesEditorBasePart.addRowButton.text"));
		this.addRowButton
				.setToolTipText(Messages
						.getString("PropertiesEditorPageTableSection.addRowButton.tooltip.text"));
		GridData arbGd = new GridData(768);
		this.addRowButton.setLayoutData(arbGd);
		this.addRowButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PropertiesEditorPageTableSection.this.searchTextCombo
						.setText("");
				PropertiesEditorPageTableSection.this
						.handleAddRowAfterCurrentRow(getDefaultKeyName(), -1);
			}
		});
		this.removeRowButton = new Button(ctrlPaneComposite, 128);
		this.removeRowButton.setText(Messages
				.getString("PropertiesEditorBasePart.removeRowButton.text"));
		this.removeRowButton
				.setToolTipText(Messages
						.getString("PropertiesEditorPageTableSection.removeRowButton.tooltip.text"));
		GridData rrbGd = new GridData(768);
		this.removeRowButton.setLayoutData(rrbGd);
		this.removeRowButton.setEnabled(false);
		this.removeRowButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PropertiesEditorPageTableSection.this.handleRemoveRow();
				if (!(PropertiesEditorPageTableSection.this.removeRowButton
						.isEnabled()))
					return;
				PropertiesEditorPageTableSection.this.removeRowButton
						.forceFocus();
			}
		});
		this.addColumnButton = new Button(ctrlPaneComposite, 0);
		this.addColumnButton.setText(Messages
				.getString("PropertiesEditorBasePart.addResourceButton.text"));
		GridData acbGd = new GridData(768);
		this.addColumnButton.setLayoutData(acbGd);
		this.addColumnButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (PropertiesEditorPageTableSection.this.getPage().getEditor()
						.isDirty()) {
					boolean isOK = MessageDialog.openConfirm(
							PropertiesEditorPageTableSection.this.getSection()
									.getShell(),
							Messages.getString("PropertiesEditorPageTableSection.dialog.unsave.title"),
							Messages.getString("PropertiesEditorPageTableSection.dialog.unsave.message"));
					if (isOK) {
						PropertiesEditorPageTableSection.this.getPage()
								.getEditor().doSave(new NullProgressMonitor());
					}
				}
				AllLocaleComboInputDialog allLocaleComboInputDialog = new AllLocaleComboInputDialog(
						PropertiesEditorPageTableSection.this.getSection()
								.getShell(),
						PropertiesEditorPageTableSection.this.propertyModel
								.getPropertiesModelHelper().getFileNamePre(),
						PropertiesEditorPageTableSection.this.filesNameList);
				allLocaleComboInputDialog.setHelpAvailable(false);
				String value = null;
				if (allLocaleComboInputDialog.open() != 0)
					return;
				value = allLocaleComboInputDialog.getValue();
				int i = 0;
				for (int listSize = PropertiesEditorPageTableSection.this.filesNameList
						.size(); i < listSize; ++i) {
					String existName = (String) PropertiesEditorPageTableSection.this.filesNameList
							.get(i);
					if (!(value.equals(existName.substring(0,
							existName.lastIndexOf(46)))))
						continue;
					MessageDialog.openError(
							PropertiesEditorPageTableSection.this.getSection()
									.getShell(),
							Messages.getString("PropertiesEditorPageTableSection.error.newFileFailed.title"),
							Messages.getString("PropertiesEditorPageTableSection.error.newFileFailed.msg"));
					return;
				}
				PropertiesEditorPageTableSection.this.handleAddColumn(value);
				IFileEditorInput input = (IFileEditorInput) PropertiesEditorPageTableSection.this
						.getPage().getEditor().getEditorInput();
				IFile file = input.getFile();
				try {
					IContainer container = file.getParent();
					if (container == null)
						return;
					container.refreshLocal(2, new NullProgressMonitor());
					String fileName = value + "." + "properties";
					PropertiesEditorPageTableSection.this.propertiesEditor
							.addNewEditorPage(fileName);
				} catch (CoreException ce) {
					PropertiesPlugin.log(ce);
				}
			}
		});

		DropTarget target = new DropTarget(propertyTableViewer.getControl(),
				DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY
						| DND.DROP_LINK);
		target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		target.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent e) {
				if (e.detail == DND.DROP_DEFAULT)
					e.detail = DND.DROP_COPY;
			}

			public void dragOperationChanged(DropTargetEvent e) {
				if (e.detail == DND.DROP_DEFAULT)
					e.detail = DND.DROP_COPY;
			}
			
			private String trim(String a, String b) {
				a = a.trim();
			    while(a.startsWith(b)){
			       a = a.substring(1,a.length());
			    }
			    while(a.endsWith(b)){
			       a = a.substring(0,a.length()-1);
			    }
				return a;
			}

			public void drop(DropTargetEvent e) {
				// System.out.println((String) e.data);
				AbstractTextEditor editor = (AbstractTextEditor) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getActiveEditor();
				editor.getDocumentProvider().getDocument(
						editor.getEditorInput());
				try {
					//String objs[] = internationalization1(text, "${", "}");
					String objs[] = internationalization1(text, "${language.get(\"", "\")}");
					if (!objs[1].equals("")) {
						TableItem item = (TableItem) e.item;
						boolean have = false;
						String haveValue = "";
						int n = 0;
						int i = 0;
						for (int listSize = PropertiesEditorPageTableSection.this.beanList
								.size(); i < listSize; ++i) {
							PropertyModel.PropertyBean tempPropertyBean = (PropertyModel.PropertyBean) PropertiesEditorPageTableSection.this.beanList
									.get(i);
							if (tempPropertyBean.getKey().equals(objs[1])) {
								have = true;
								int number = getPreNumber("");
								haveValue = tempPropertyBean.getValues().get(
										number);
							}
							if (tempPropertyBean.getKey()
									.equals(item.getText())) {
								n = i;
							}
						}
						if (have) {
							if (objs[2].equals(haveValue)) {
								document.replace(offset, length, objs[0]);// 更改editor
							} else {
								// 提示有冲突
								showMessage(objs[1]
										+ Messages
												.getString("PropertiesEditorPageTableSection.yijingzai_")
										+ haveValue
										+ Messages
												.getString("PropertiesEditorPageTableSection._zhongshiyong_bunengyingyongyu_")
										+ objs[2]
										+ Messages
												.getString("PropertiesEditorPageTableSection._qingshoudongtianjia_"));
							}
						} else {
							String str = objs[0];
							str = trim(str, "\"");// java文件打开，jsp关闭
							document.replace(offset, length, str);// objs[0]更改editor
							String[] values = new String[filesNameList.size()];
							String fileNamePre = propertiesEditor
									.getFileNamePre();
							for (int j = 0; j < filesNameList.size(); j++) {
								String filesName = filesNameList.get(j);
								if (filesName.equals(fileNamePre
										+ ".properties")) {
									values[j] = objs[2];
								} else {
									String[] films = objs[2].split(SPLIT);
									StringBuilder sb = new StringBuilder();
									String planguage = filesName.substring(
											filesName.indexOf(fileNamePre
													+ SPLIT)
													+ (fileNamePre + SPLIT)
															.length(),
											filesName.indexOf(".properties"));
									Language language = Language
											.fromString(planguage);
									if (language != null) {
										for (int k = 0; k < films.length; k++) {
											if (k != 0) {
												sb.append(SPLIT);
											}
											try {
//												Translate
//														.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
//												String tran = Translate
//														.execute(
//																films[k],
//																Language.CHINESE,
//																language);
												GoogleAPI.setHttpReferrer("http://jiayq007.appspot.com/test"/* Enter the URL of your site here */);
											    GoogleAPI.setKey("AIzaSyDfkN-DqqhoHtqGZy-N565kAquv5QN-1jc"/* Enter your API key here */);
												String tran = Translate.DEFAULT.execute(films[k], Language.CHINESE, language);
//											    System.out.println(tran);
												sb.append(tran);
//												sb.append(films[k]);
											} catch (Exception e0) {
												e0.printStackTrace();
											}
										}
									}
									values[j] = sb.toString();
								}
							}
							handleAddRowAfterCurrentRow(objs[1], n, values);// 添加一行
						}
					}
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		});

		propertiesEditorPage.getSite().getPage().getWorkbenchWindow()
				.getSelectionService()
				.addPostSelectionListener(new ISelectionListener() {

					@Override
					public void selectionChanged(IWorkbenchPart part,
							ISelection selection) {
						if(part instanceof TextEditor){
							IEditorInput editorInput = ((TextEditor) part)
									.getEditorInput();
							document = ((TextEditor) part).getDocumentProvider()
									.getDocument(editorInput);
							offset = ((ITextSelection) selection).getOffset();
							length = ((ITextSelection) selection).getLength();
							text = ((ITextSelection) selection).getText();
						}
					}
				});

		initDuplicatedToolTip();
		initKeyAssistDialog();
		addHelpIcon();
	}

	private int getPreNumber(String pre) {
		String fileNamePre = propertiesEditor.getFileNamePre();
		if (!(pre == null || pre.equals(""))) {
			fileNamePre = fileNamePre + SPLIT + pre;
		}
		for (int j = 0; j < filesNameList.size(); j++) {
			String filesName = filesNameList.get(j);
			if (filesName.equals(fileNamePre + ".properties")) {
				return j;
			}
		}
		return -1;
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(propertyTableViewer.getControl()
				.getShell(), Messages
				.getString("PropertiesEditorPageTableSection.guojihuagongju"),
				message);
	}

	private void addHelpIcon() {
		Section section = getSection();
		ImageHyperlink helpImageLink = new ImageHyperlink(section, 0);
		helpImageLink.setImage(JFaceResources.getImage("dialog_help_image"));
		helpImageLink.setBackground(section.getTitleBarGradientBackground());
		helpImageLink
				.setToolTipText(Messages
						.getString("PropertiesEditorPageTableSection.helpImg.toolTip.text"));
		helpImageLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				PropertiesEditorPageTableSection.this.helpPopupDialog.open();
			}
		});
		section.setTextClient(helpImageLink);
	}

	private void handleAddRow() {
		PropertyModel.PropertyBean toAddPropertyBean = new PropertyModel.PropertyBean();
		String newKey = getDefaultKeyName();
		toAddPropertyBean.setKey(newKey);
		for (int i = 0; i < this.resourceFilesNum; ++i) {
			toAddPropertyBean.addValue("");
		}
		this.propertyModel.getAllKeysList().add(newKey);
		this.beanList.add(toAddPropertyBean);
		this.propertyModel.addPropertiesKey(newKey);
		this.propertyTableViewer.setInput(this.beanList);
		int index = this.beanList.indexOf(toAddPropertyBean);
		this.propertyTable.setSelection(index);
		this.propertyTableViewer.refresh();
		Object object = ((IStructuredSelection) this.propertyTableViewer
				.getSelection()).getFirstElement();
		if (object != null) {
			this.propertyTableViewer.editElement(
					((IStructuredSelection) this.propertyTableViewer
							.getSelection()).getFirstElement(), 0);
		}
		this.propertiesEditor.setDirty(true);
		markDirty();
		updateButtons();
	}

	/**
	 * 标准替换
	 * 
	 * @param text
	 * @param begin
	 * @param end
	 * @return 替换模板/key/中文/
	 */
	private String[] internationalization1(String text, String begin, String end) {
		String[] objs = new String[3];
		RegResult regResult = getRegResult(text, REGEX);
		List<Group> list1 = regResult.getResults();
		List<Group> list2 = regResult.getUnresults();
		int size1 = list1.size();
		int size2 = list2.size();
		int size = size1 > size2 ? size1 : size2;
		StringBuilder value = new StringBuilder();// 国际化汉字
		for (int i = 0; i < size1; i++) {
			Group group1 = list1.get(i);
			if (i != 0) {
				value.append(SPLIT);
			}
			value.append(group1.getName());
			// System.out.println(group.getName() + group.getStart()
			// + group.getEnd()); // 打印所有
		}
		String vb = value.toString().replaceAll(PUNCTUATION_REPLACEALL, "~");
		String key = PinYin.getPinYin(vb, ToneType.QUAN_PIN);
		objs[1] = key;
		objs[2] = value.toString();
		StringBuilder content = new StringBuilder();// 被替换内容
		boolean isbegin = false;//是开头
		for (int i = 0; i < size; i++) {
			String name1 = null;
			if (i < size1) {
				Group group1 = list1.get(i);
				if (i == 0) {
					if (group1.getStart() == 0) {
						isbegin = true;
					}
				}
				name1 = group1.getName();
			}
			String name2 = null;
			if (i < size2) {
				Group group2 = list2.get(i);
				name2 = group2.getName();
			}
			if (isbegin) {
				if (name1 != null) {
					content.append(begin + key + BRACKETS1 + i + BRACKETS2
							+ end);
				}
				if (name2 != null) {
					content.append(name2);
				}
			} else {
				if (name2 != null) {
					content.append(name2);
				}
				if (name1 != null) {
					content.append(begin + key + BRACKETS1 + i + BRACKETS2
							+ end);
				}
			}
		}
		objs[0] = content.toString();
		return objs;
	}

//	/**
//	 * 严格替换
//	 * 
//	 * @param text
//	 * @param begin
//	 * @param end
//	 * @return
//	 */
//	private String[] internationalization2(String text, String begin, String end) {
//		String[] objs = new String[3];
//		Pattern pattern1 = Pattern.compile(REGEX);// [^\\x00-\\xff]+
//		Pattern pattern2 = Pattern.compile(FREGEX);// [\\x00-\\xff]+
//		Matcher matcher1 = pattern1.matcher(text);
//		Matcher matcher2 = pattern2.matcher(text);
//		List<Group> list1 = new ArrayList<Group>();
//		List<Group> list2 = new ArrayList<Group>();
//		while (matcher1.find()) {
//			list1.add(new Group(matcher1.group(), matcher1.start(), matcher1
//					.end()));
//		}
//		while (matcher2.find()) {
//			list2.add(new Group(matcher2.group(), matcher2.start(), matcher2
//					.end()));
//		}
//		int size1 = list1.size();
//		int size2 = list2.size();
//		int size = size1 > size2 ? size1 : size2;
//		String value = text.replaceAll(FREGEX, SPLIT);// 国际化汉字
//		String key = PinYin.getPinYin(value, ToneType.QUAN_PIN);
//		objs[1] = key;
//		objs[2] = value;
//		StringBuilder content = new StringBuilder();// 被替换内容
//		boolean isbegin = false;
//		boolean isEnd = false;
//		for (int i = 0; i < size; i++) {
//			String name1 = null;
//			if (i < size1) {
//				Group group1 = list1.get(i);
//				if (i == 0) {
//					if (group1.getStart() == 0) {
//						isbegin = true;
//					}
//				}
//				if (i == size1 - 1) {
//					if (group1.getEnd() == text.length()) {
//						isEnd = true;
//					}
//				}
//				name1 = group1.getName();
//			}
//			String name2 = null;
//			if (i < size2) {
//				Group group2 = list2.get(i);
//				name2 = group2.getName();
//			}
//			if (isbegin) {
//				if (isEnd) {
//					if (name1 != null) {
//						content.append(begin + key + BRACKETS1 + i + BRACKETS2
//								+ end);
//					}
//					if (name2 != null) {
//						content.append(name2);
//					}
//				} else {
//					if (name1 != null) {
//						content.append(begin + key + BRACKETS1 + i + BRACKETS2
//								+ end);
//					}
//					if (name2 != null) {
//						content.append(name2);
//					}
//					if (i == size - 1) {
//						content.append(begin + key + BRACKETS1 + (i + 1)
//								+ BRACKETS2 + end);
//					}
//				}
//			} else {
//				if (i == 0) {
//					content.append(begin + key + BRACKETS1 + i + BRACKETS2
//							+ end);
//				}
//				if (isEnd) {
//					if (name2 != null) {
//						content.append(name2);
//					}
//					if (name1 != null) {
//						content.append(begin + key + BRACKETS1 + (i + 1)
//								+ BRACKETS2 + end);
//					}
//				} else {
//					if (name2 != null) {
//						content.append(name2);
//					}
//					if (name1 != null) {
//						content.append(begin + key + BRACKETS1 + (i + 1)
//								+ BRACKETS2 + end);
//					}
//					if (i == size - 1) {
//						content.append(begin + key + BRACKETS1 + (i + 1)
//								+ BRACKETS2 + end);
//					}
//				}
//			}
//		}
//		objs[0] = content.toString();
//		return objs;
//	}
	
	public static RegResult getRegResult(String text, String regex) {
		Pattern pattern1 = Pattern.compile(regex);// [^\\x00-\\xff]+
		Matcher matcher1 = pattern1.matcher(text);
		List<Group> results = new ArrayList<Group>();
		while (matcher1.find()) {
			results.add(new Group(text// matcher1.group()
					, matcher1.start(), matcher1.end()));
		}
		List<Group> unresults = new ArrayList<Group>();
		boolean isbegin = false;
		int last = 0;
		int size = results.size();
		int length = text.length();// 字符串长度
		for (int i = 0; i < size; i++) {
			Group group = results.get(i);
			int start = group.getStart();
			int end = group.getEnd();
			if (i == 0 && start == 0) {// 第一个不是汉字
				isbegin = true;
			} else {// 排除第一轮是汉字
				unresults.add(new Group(text, last, start));
			}
			if (i == size - 1 && end < length) {// 最后一个不是汉字
				unresults.add(new Group(text, end, length));
			}
			last = end;// 记录上一个汉字结束
		}
		RegResult regResult = new RegResult(results, unresults, isbegin);
		return regResult;
	}

	public static class RegResult {

		private List<Group> results;

		private List<Group> unresults;

		private boolean isbegin = false;

		public RegResult(List<Group> results, List<Group> unresults,
				boolean isbegin) {
			super();
			this.results = results;
			this.unresults = unresults;
			this.isbegin = isbegin;
		}

		public List<Group> getResults() {
			return results;
		}

		public List<Group> getUnresults() {
			return unresults;
		}

		public boolean isIsbegin() {
			return isbegin;
		}

	}

	public static class Group {
		private String name;

		private int start;

		private int end;

		public Group(String text, int start, int end) {
			super();
			this.name = text.substring(start, end);
			this.start = start;
			this.end = end;
		}

		public String getName() {
			return name;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		@Override
		public String toString() {
			return "Group [name=" + name + ", start=" + start + ", end=" + end
					+ "]";
		}

	}

	private void handleAddRowAfterCurrentRow(String newKey, int insertIndex,
			String... values) {
		if (insertIndex < 0) {
			IStructuredSelection selection = (IStructuredSelection) this.propertyTableViewer
					.getSelection();
			PropertyModel.PropertyBean currentPropertyBean = (PropertyModel.PropertyBean) selection
					.getFirstElement();
			insertIndex = this.beanList.size();
			if (currentPropertyBean != null) {
				insertIndex = this.beanList.indexOf(currentPropertyBean) + 1;
			}
		}
		PropertyModel.PropertyBean toAddPropertyBean = new PropertyModel.PropertyBean();
		toAddPropertyBean.setKey(newKey);
		this.propertyModel.getAllKeysList().add(newKey);
		this.beanList.add(insertIndex, toAddPropertyBean);
		this.propertyModel.addPropertiesKey(newKey);
		for (int i = 0; i < this.resourceFilesNum; ++i) {
			String value = "";
			if (i < values.length) {
				value = values[i];
			}
			toAddPropertyBean.addValue(value);
			this.propertyModel.merge(newKey, value,
					PropertiesEditorPageTableSection.this.columnNames[(i + 1)]);
		}
		this.propertyTableViewer.setInput(this.beanList);
		int index = this.beanList.indexOf(toAddPropertyBean);
		this.propertyTable.setSelection(index);
		this.propertyTableViewer.refresh();
		//jiayq 2014/06/04
//		Object object = ((IStructuredSelection) this.propertyTableViewer
//				.getSelection()).getFirstElement();
//		if (object != null) {
//			this.propertyTableViewer.editElement(object, 0);
//		}
		this.propertiesEditor.setDirty(true);
		markDirty();
		updateButtons();
	}

	private void handleRemoveRow() {
		int index = this.propertyTable.getSelectionIndex();
		int size = this.propertyTable.getSelectionCount();
		IStructuredSelection structuredSelection = (IStructuredSelection) this.propertyTableViewer
				.getSelection();
		if (size > 1) {
			for (Iterator iterator = structuredSelection.iterator(); iterator
					.hasNext();) {
				PropertyModel.PropertyBean toRmv = (PropertyModel.PropertyBean) iterator
						.next();
				this.propertyModel.removeRow(toRmv.getKey());
				this.beanList.remove(toRmv);
			}
			this.propertyTableViewer.refresh();
		} else {
			PropertyModel.PropertyBean propertyBean = (PropertyModel.PropertyBean) structuredSelection
					.getFirstElement();
			if (propertyBean != null) {
				this.propertyModel.removeRow(propertyBean.getKey());
				this.beanList.remove(propertyBean);
				this.propertyTableViewer.setInput(this.beanList);
				int itemCount = this.propertyTable.getItemCount();
				if (index > itemCount - 1) {
					index = itemCount - 1;
				}
				this.propertyTableViewer.refresh();
				this.propertyTable.setSelection(index);
			}
		}
		this.propertiesEditor.setDirty(true);
		markDirty();
		updateButtons();
	}

	private void handleAddColumn(String newFileNamePartition) {
		TableColumn newColumn = new TableColumn(this.propertyTable, 16384);
		String newFileName = newFileNamePartition + "." + "properties";
		newColumn.setText(newFileNamePartition);
		newColumn.setWidth(DEFAULT_COLUMN_WEIGHT);
		CellEditor[] oldEditors = this.propertyTableViewer.getCellEditors();
		TextCellEditor newEditor = new TextCellEditor(this.propertyTable);
		CellEditor[] newEditors = new CellEditor[oldEditors.length + 1];
		this.filesNameList.add(newFileName);
		this.propertyModel.addProperties(newFileName);
		this.resourceFilesNum = this.filesNameList.size();
		this.columnNames = new String[this.resourceFilesNum + 1];
		this.columnNames[0] = KEY;
		for (int i = 1; i < this.columnNames.length; ++i) {
			this.columnNames[i] = ((String) this.filesNameList.get(i - 1));
		}
		System.arraycopy(oldEditors, 0, newEditors, 0, oldEditors.length);
		newEditors[oldEditors.length] = newEditor;
		Control control = newEditor.getControl();
		if (control != null) {
			control.addKeyListener(new InnerCellEditorKeyListener());
			control.addTraverseListener(new InnerCellEditorTraverseListener());
		}
		int i = 0;
		for (int listSize = this.beanList.size(); i < listSize; ++i) {
			PropertyModel.PropertyBean propertyBean4AddNewValue = (PropertyModel.PropertyBean) this.beanList
					.get(i);
			propertyBean4AddNewValue.addValue("");
		}
		this.propertyTableViewer.setColumnProperties(this.columnNames);
		this.propertyTableViewer.setCellEditors(newEditors);
		this.propertyTableViewer.setCellModifier(new InnerCellModifier());
		this.propertiesEditor.setDirty(true);
		markDirty();
		this.propertyTableViewer.refresh();
	}

	private String getDefaultKeyName() {
		StringBuffer ret = new StringBuffer();
		ret.append("Key");
		Object existKeys[] = propertyModel.getAllKeysList().toArray();
		int i = 1;
		do {
			label0: {
				ret.append(i);
				for (int j = 0; j < existKeys.length; j++) {
					if (!ret.toString().equals(existKeys[j]))
						continue;
					int length = ret.length();
					ret.delete(length - String.valueOf(i).length(), length);
					break label0;
				}

				return ret.toString();
			}
			i++;
		} while (true);
	}

	private void updateButtons() {
		int num = this.propertyTableViewer.getTable().getSelectionCount();
		this.removeRowButton.setEnabled(num > 0);
	}

	private void showKeyDupToolTip(PropertyModel.PropertyBean tempPb) {
		CellEditor cellEditor = this.propertyTableViewer.getCellEditors()[0];
		Text text = (Text) cellEditor.getControl();
		int x = text.getLocation().x;
		int y = text.getLocation().y;
		Composite parent = text.getParent();
		while (parent != null) {
			x += parent.getLocation().x;
			y += parent.getLocation().y;
			parent = parent.getParent();
		}
		this.duplicatedToolTip.setLocation(x, y - 5);
		this.duplicatedToolTip
				.setText(Messages
						.getString("PropertiesEditorPageTableSection.dupError.msg.pre")
						+ Messages
								.getString("PropertiesEditorPageTableSection.dupError.msg.key")
						+ Messages
								.getString("PropertiesEditorPageTableSection.dupError.msg.lm")
						+ tempPb.getKey()
						+ Messages
								.getString("PropertiesEditorPageTableSection.dupError.msg.exiested"));
		this.duplicatedToolTip.setVisible(true);
	}

	private void hideKeyDupToolTip() {
		if (!(this.duplicatedToolTip.isVisible()))
			return;
		this.duplicatedToolTip.setVisible(false);
	}

	private void initDuplicatedToolTip() {
		this.duplicatedToolTip = new ToolTip(getSection().getShell(), 1);
		this.duplicatedToolTip.setAutoHide(true);
	}

	private void initKeyAssistDialog() {
		String[][] tableInput = {
				{
						Messages.getString("PropertiesEditorPageTableSection.addRow"),
						Messages.getString("PropertiesEditorPageTableSection.addRow.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.removeRow"),
						Messages.getString("PropertiesEditorPageTableSection.removeRow.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.save"),
						Messages.getString("PropertiesEditorPageTableSection.save.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.copyRow"),
						Messages.getString("PropertiesEditorPageTableSection.copyRow.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.pasteRow"),
						Messages.getString("PropertiesEditorPageTableSection.pasteRow.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.moveUP"),
						Messages.getString("PropertiesEditorPageTableSection.moveUp.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.moveDown"),
						Messages.getString("PropertiesEditorPageTableSection.moveDown.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.moveLeft"),
						Messages.getString("PropertiesEditorPageTableSection.moveLeft.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.moveRight"),
						Messages.getString("PropertiesEditorPageTableSection.moveRight.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.next"),
						Messages.getString("PropertiesEditorPageTableSection.next.key") },
				{
						Messages.getString("PropertiesEditorPageTableSection.previous"),
						Messages.getString("PropertiesEditorPageTableSection.previous.key") } };
		this.helpPopupDialog = new HelpPopupDialog(getSection().getShell(),
				tableInput);
	}

	private void handleCommonKeyEvent(
			PropertyModel.PropertyBean currentRowPropertyBean, KeyEvent e) {
		int index = this.propertyTable.getSelectionIndex();
		if ((16777219 == e.keyCode) && (262144 == e.stateMask)) {
			if (this.currentColumn <= 0)
				return;
			this.currentColumn -= 1;
			this.propertyTableViewer.editElement(currentRowPropertyBean,
					this.currentColumn);
		} else if ((16777220 == e.keyCode) && (262144 == e.stateMask)) {
			if (this.resourceFilesNum + 1 <= this.currentColumn)
				return;
			this.currentColumn += 1;
			this.propertyTableViewer.editElement(currentRowPropertyBean,
					this.currentColumn);
		} else if ((16777217 == e.keyCode) && (262144 == e.stateMask)) {
			if (index <= 0)
				return;
			this.propertyTable.deselectAll();
			this.propertyTable.select(--index);
			IStructuredSelection structuredSelection = (IStructuredSelection) this.propertyTableViewer
					.getSelection();
			PropertyModel.PropertyBean upperRowPropertyBean = (PropertyModel.PropertyBean) structuredSelection
					.getFirstElement();
			if (upperRowPropertyBean == null)
				return;
			this.propertyTableViewer.editElement(upperRowPropertyBean,
					this.currentColumn);
		} else {
			if ((16777218 != e.keyCode)
					|| (262144 != e.stateMask)
					|| (index >= this.propertyTableViewer.getTable()
							.getItemCount() - 1))
				return;
			PropertyModel.PropertyBean downnerRowPropertyBean = (PropertyModel.PropertyBean) this.propertyTable
					.getItem(++index).getData();
			if (downnerRowPropertyBean == null)
				return;
			this.propertyTableViewer.editElement(downnerRowPropertyBean,
					this.currentColumn);
		}
	}

	private class InnerArrayContentProvider extends ArrayContentProvider {
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Object[]) {
				Object objs[] = (Object[]) inputElement;
				List propertyBeanList = new ArrayList();
				Object aobj[];
				int j = (aobj = objs).length;
				for (int i = 0; i < j; i++) {
					Object obj = aobj[i];
					if (obj instanceof com.apusic.studio.properties.model.PropertyModel.PropertyBean) {
						com.apusic.studio.properties.model.PropertyModel.PropertyBean propertyBean = (com.apusic.studio.properties.model.PropertyModel.PropertyBean) obj;
						String key = propertyBean.getKey();
						if (isCaseSenstive
								&& key.startsWith(searchStr)
								|| !isCaseSenstive
								&& key.toLowerCase().startsWith(
										searchStr.toLowerCase()))
							propertyBeanList.add(propertyBean);
					}
				}
				return propertyBeanList.toArray();
			}
			if (inputElement instanceof Collection) {
				Collection objs = (Collection) inputElement;
				List propertyBeanList = new ArrayList();
				for (Iterator iterator = objs.iterator(); iterator.hasNext();) {
					Object obj = iterator.next();
					if (obj instanceof com.apusic.studio.properties.model.PropertyModel.PropertyBean) {
						com.apusic.studio.properties.model.PropertyModel.PropertyBean propertyBean = (com.apusic.studio.properties.model.PropertyModel.PropertyBean) obj;
						String key = propertyBean.getKey();
						if (isCaseSenstive
								&& key.startsWith(searchStr)
								|| !isCaseSenstive
								&& key.toLowerCase().startsWith(
										searchStr.toLowerCase()))
							propertyBeanList.add(propertyBean);
					}
				}
				return propertyBeanList.toArray();
			} else {
				return new Object[0];
			}
		}
	}

	private class InnerCellEditorKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if ('\t' != e.character) {
				PropertiesEditorPageTableSection.this.hideKeyDupToolTip();
			}
			IStructuredSelection selection = (IStructuredSelection) PropertiesEditorPageTableSection.this.propertyTableViewer
					.getSelection();
			PropertyModel.PropertyBean currentRowPropertyBean = (PropertyModel.PropertyBean) selection
					.getFirstElement();
			if (currentRowPropertyBean == null)
				return;
			PropertiesEditorPageTableSection.this.handleCommonKeyEvent(
					currentRowPropertyBean, e);
			if ((61 != e.keyCode) || (262144 != e.stateMask)) {
				return;
			}
			CellEditor cellEditor = PropertiesEditorPageTableSection.this.propertyTableViewer
					.getCellEditors()[PropertiesEditorPageTableSection.this.currentColumn];
			String str = ((Text) cellEditor.getControl()).getText().trim();
			String oldKey = currentRowPropertyBean.getKey();
			if (PropertiesEditorPageTableSection.this.currentColumn == 0) {
				if ((!("".equals(str))) && (!(oldKey.equals(str)))) {
					boolean dulplicated = false;
					int i = 0;
					for (int listSize = PropertiesEditorPageTableSection.this.beanList
							.size(); i < listSize; ++i) {
						PropertyModel.PropertyBean tempPropertyBean = (PropertyModel.PropertyBean) PropertiesEditorPageTableSection.this.beanList
								.get(i);
						if (!(tempPropertyBean.getKey().equals(str)))
							continue;
						PropertiesEditorPageTableSection.this
								.showKeyDupToolTip(tempPropertyBean);
						dulplicated = true;
						break;
					}
					if (!(dulplicated)) {
						currentRowPropertyBean.setKey(str);
						PropertiesEditorPageTableSection.this.propertyModel
								.modifyKey(oldKey, str);
					}
				}
			} else if (PropertiesEditorPageTableSection.this.currentColumn > 0) {
				List tempValuesList = currentRowPropertyBean.getValues();
				if ((PropertiesEditorPageTableSection.this.currentColumn - 1 < tempValuesList
						.size())
						&& (PropertiesEditorPageTableSection.this.currentColumn - 1 < PropertiesEditorPageTableSection.this.filesNameList
								.size())
						&& (!(((String) tempValuesList
								.get(PropertiesEditorPageTableSection.this.currentColumn - 1))
								.equals(str)))) {
					String fileName = (String) PropertiesEditorPageTableSection.this.filesNameList
							.get(PropertiesEditorPageTableSection.this.currentColumn - 1);
					currentRowPropertyBean
							.setValue(
									PropertiesEditorPageTableSection.this.currentColumn - 1,
									str);
					PropertiesEditorPageTableSection.this.propertyModel.merge(
							oldKey, str, fileName);
				}
			}
			PropertiesEditorPageTableSection.this.propertyTableViewer.refresh();
			PropertiesEditorPageTableSection.this.searchTextCombo.setText("");
			PropertiesEditorPageTableSection.this.handleAddRowAfterCurrentRow(
					getDefaultKeyName(), -1);
		}

		public void keyReleased(KeyEvent e) {
		}
	}

	private class InnerCellEditorTraverseListener implements TraverseListener {
		public void keyTraversed(TraverseEvent e) {
			IStructuredSelection selection = (IStructuredSelection) PropertiesEditorPageTableSection.this.propertyTableViewer
					.getSelection();
			PropertyModel.PropertyBean currentRowPropertyBean = (PropertyModel.PropertyBean) selection
					.getFirstElement();
			int index = PropertiesEditorPageTableSection.this.propertyTable
					.getSelectionIndex();
			if ((currentRowPropertyBean == null) || (index < 0))
				return;
			if (16 == e.detail) {
				if (PropertiesEditorPageTableSection.this.currentColumn < PropertiesEditorPageTableSection.this.resourceFilesNum) {
					PropertiesEditorPageTableSection.this.currentColumn += 1;
					PropertiesEditorPageTableSection.this.propertyTableViewer
							.editElement(
									currentRowPropertyBean,
									PropertiesEditorPageTableSection.this.currentColumn);
				} else {
					if ((PropertiesEditorPageTableSection.this.currentColumn != PropertiesEditorPageTableSection.this.resourceFilesNum)
							|| (index >= PropertiesEditorPageTableSection.this.propertyTable
									.getItemCount() - 1))
						return;
					PropertyModel.PropertyBean newFirstSelectPropertyBean = (PropertyModel.PropertyBean) PropertiesEditorPageTableSection.this.propertyTable
							.getItem(++index).getData();
					if (newFirstSelectPropertyBean == null)
						return;
					PropertiesEditorPageTableSection.this.propertyTableViewer
							.editElement(newFirstSelectPropertyBean, 0);
				}
			} else {
				if (8 != e.detail)
					return;
				if (PropertiesEditorPageTableSection.this.currentColumn > 0) {
					PropertiesEditorPageTableSection.this.currentColumn -= 1;
					PropertiesEditorPageTableSection.this.propertyTableViewer
							.editElement(
									currentRowPropertyBean,
									PropertiesEditorPageTableSection.this.currentColumn);
				} else {
					if ((PropertiesEditorPageTableSection.this.currentColumn != 0)
							|| (index <= 0))
						return;
					PropertiesEditorPageTableSection.this.propertyTable
							.deselectAll();
					PropertiesEditorPageTableSection.this.propertyTable
							.select(--index);
					IStructuredSelection ss = (IStructuredSelection) PropertiesEditorPageTableSection.this.propertyTableViewer
							.getSelection();
					PropertyModel.PropertyBean newFirstSelectPropertyBean = (PropertyModel.PropertyBean) ss
							.getFirstElement();
					if (newFirstSelectPropertyBean == null)
						return;
					PropertiesEditorPageTableSection.this.propertyTableViewer
							.editElement(
									newFirstSelectPropertyBean,
									PropertiesEditorPageTableSection.this.resourceFilesNum);
				}
			}
		}
	}

	private class InnerCellModifier implements ICellModifier {
		public boolean canModify(Object element, String property) {
			return true;
		}

		public Object getValue(Object element, String property) {
			String ret = "";
			int index = getColumnIndex(property);
			if ((element instanceof com.apusic.studio.properties.model.PropertyModel.PropertyBean)
					&& index >= 0) {
				com.apusic.studio.properties.model.PropertyModel.PropertyBean propertyBean = (com.apusic.studio.properties.model.PropertyModel.PropertyBean) element;
				switch (index) {
				case 0:
					ret = propertyBean.getKey();
					break;
				default:
					List tempValuesList = propertyBean.getValues();
					if (tempValuesList.size() >= index && index > 0)
						ret = (String) tempValuesList.get(index - 1);
					break;
				}
			}
			return ret;
		}

		public void modify(Object element, String property, Object value) {
			int tableColumnIndex = getColumnIndex(property);
			if (tableColumnIndex < 0)
				return;
			PropertyModel.PropertyBean propertyBean = null;
			TableItem tableItem = null;
			if (element instanceof TableItem) {
				tableItem = (TableItem) element;
				propertyBean = (PropertyModel.PropertyBean) tableItem.getData();
			} else {
				propertyBean = (PropertyModel.PropertyBean) element;
			}
			String value4Modify = (String) value;
			if (value4Modify == null)
				return;
			if (tableColumnIndex == 0) {
				value4Modify = value4Modify.trim();
				if ((!("".equals(value4Modify)))
						&& (!(propertyBean.getKey().equals(value4Modify)))) {
					int i = 0;
					for (int listSize = PropertiesEditorPageTableSection.this.beanList
							.size(); i < listSize; ++i) {
						PropertyModel.PropertyBean tempPropertyBean = (PropertyModel.PropertyBean) PropertiesEditorPageTableSection.this.beanList
								.get(i);
						if (!(tempPropertyBean.getKey().equals(value4Modify)))
							continue;
						PropertiesEditorPageTableSection.this
								.showKeyDupToolTip(tempPropertyBean);
						return;
					}
					String oldKey = propertyBean.getKey();
					propertyBean.setKey(value4Modify);
					PropertiesEditorPageTableSection.this.propertyModel
							.modifyKey(oldKey, value4Modify);
				}
			} else {
				List tempValuesList = propertyBean.getValues();
				if (tempValuesList.size() > tableColumnIndex - 1) {
					if (((String) tempValuesList.get(tableColumnIndex - 1))
							.equals(value4Modify)) {
						return;
					}
					propertyBean.setValue(tableColumnIndex - 1, value4Modify);
					PropertiesEditorPageTableSection.this.propertyModel.merge(
							propertyBean.getKey(), value4Modify, property);
				}
			}
			PropertiesEditorPageTableSection.this.propertiesEditor
					.setDirty(true);
			PropertiesEditorPageTableSection.this.markDirty();
			PropertiesEditorPageTableSection.this.propertyTableViewer.refresh();
			PropertiesEditorPageTableSection.this.updateButtons();
		}

		private int getColumnIndex(String property) {
			int ret = -1;
			for (int i = 0; i < PropertiesEditorPageTableSection.this.columnNames.length; ++i) {
				if (!(property
						.equals(PropertiesEditorPageTableSection.this.columnNames[i])))
					continue;
				PropertiesEditorPageTableSection.this.currentColumn = i;
				ret = i;
				break;
			}
			return ret;
		}
	}

	private class InnerPropertiesTableLabelProvider extends LabelProvider
			implements ITableLabelProvider {
		public String getColumnText(Object element, int col) {
			String ret = "";
			if (element instanceof com.apusic.studio.properties.model.PropertyModel.PropertyBean) {
				com.apusic.studio.properties.model.PropertyModel.PropertyBean propertyBean = (com.apusic.studio.properties.model.PropertyModel.PropertyBean) element;
				switch (col) {
				case 0:
					ret = propertyBean.getKey();
					break;
				default:
					List tempValuesList = propertyBean.getValues();
					if (tempValuesList.size() >= col && col > 0)
						ret = (String) tempValuesList.get(col - 1);
					break;
				}
			}
			return ret;
		}

		public Image getColumnImage(Object element, int col) {
			return null;
		}
	}

	private class TableDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection selection = (IStructuredSelection) PropertiesEditorPageTableSection.this.propertyTableViewer
					.getSelection();
			PropertyModel.PropertyBean propertyBean = (PropertyModel.PropertyBean) selection
					.getFirstElement();
			if ((propertyBean == null)
					|| (PropertiesEditorPageTableSection.this.currentColumn < 0))
				return;
			StringBuffer stringBuffer = new StringBuffer();
			String fileName = "";
			String initValue;
			String title;
			if (PropertiesEditorPageTableSection.this.currentColumn == 0) {
				initValue = propertyBean.getKey();
				stringBuffer
						.append(Messages
								.getString("PropertiesEditorBasePart.mutiInputDialog.message.key"));
				title = Messages
						.getString("PropertiesEditorBasePart.mutiInputDialog.title.key");
			} else {
				fileName = (String) PropertiesEditorPageTableSection.this.filesNameList
						.get(PropertiesEditorPageTableSection.this.currentColumn - 1);
				initValue = (String) propertyBean
						.getValues()
						.get(PropertiesEditorPageTableSection.this.currentColumn - 1);
				stringBuffer
						.append(Messages
								.getString("PropertiesEditorBasePart.mutiInputDialog.message.prop"))
						.append(Messages
								.getString("PropertiesEditorBasePart.lgt"))
						.append(fileName)
						.append(Messages
								.getString("PropertiesEditorBasePart.rgt"));
				title = Messages
						.getString("PropertiesEditorBasePart.mutiInputDialog.title.prop");
			}
			MultiLineInputDialog inputDialog = new MultiLineInputDialog(
					PropertiesEditorPageTableSection.this.getSection()
							.getShell(),
					title,
					stringBuffer.toString(),
					initValue,
					(PropertiesEditorPageTableSection.this.currentColumn == 0) ? new IInputValidator() {
						public String isValid(String newText) {
							String ret = null;
							if ("".equals(newText.trim())) {
								ret = Messages
										.getString("PropertiesEditorBasePart.mutiInputDialog.error.tip");
							}
							return ret;
						}
					}
							: null);
			String newValue = null;
			if (inputDialog.open() == 0) {
				newValue = inputDialog.getValue().trim();
			}
			if (newValue == null)
				return;
			if (PropertiesEditorPageTableSection.this.currentColumn == 0) {
				String oldKey = propertyBean.getKey();
				if (!(newValue.equals(oldKey))) {
					for (int i = 0; i < PropertiesEditorPageTableSection.this.beanList
							.size(); ++i) {
						PropertyModel.PropertyBean tempPb = (PropertyModel.PropertyBean) PropertiesEditorPageTableSection.this.beanList
								.get(i);
						if (!(tempPb.getKey().equals(newValue)))
							continue;
						PropertiesEditorPageTableSection.this
								.showKeyDupToolTip(tempPb);
						return;
					}
					PropertiesEditorPageTableSection.this.propertyModel
							.modifyKey(oldKey, newValue);
					propertyBean.setKey(newValue);
				}
			} else {
				propertyBean
						.setValue(
								PropertiesEditorPageTableSection.this.currentColumn - 1,
								newValue);
				PropertiesEditorPageTableSection.this.propertyModel.merge(
						propertyBean.getKey(), newValue, fileName);
			}
			PropertiesEditorPageTableSection.this.propertiesEditor
					.setDirty(true);
			PropertiesEditorPageTableSection.this.markDirty();
			PropertiesEditorPageTableSection.this.propertyTableViewer.refresh();
			PropertiesEditorPageTableSection.this.updateButtons();
		}
	}
}
