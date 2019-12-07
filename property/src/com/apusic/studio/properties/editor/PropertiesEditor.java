package com.apusic.studio.properties.editor;

import com.apusic.studio.properties.PropertiesPlugin;
import com.apusic.studio.properties.model.PropertiesModelHelper;
import com.apusic.studio.properties.model.PropertyModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;

public class PropertiesEditor extends FormEditor {
	private PropertiesEditorPage page;
	public static final String EDITOR_ID = "com.apusic.studio.admin.properties.editor.PropertiesEditor";
	private PropertiesModelHelper propertiesModelHelper;
	private PropertyModel propertyModel;
	private boolean Dirty;
	private String fileNamePre = "";
	private IFile file;
	private boolean isRegularInput;
	private IEditorInput input;

	class _cls1ResourceDeltaVisitor implements IResourceDeltaVisitor {
		protected List<IFile> removedResources;

		public boolean visit(IResourceDelta delta_) {
			this.removedResources = new ArrayList<IFile>();
			if ((delta_.getFlags() != 131072)
					&& (delta_.getResource().getType() == 1)
					&& ((delta_.getKind() & 0x6) != 0)
					&& (PropertiesEditor.this.file != null)
					&& ((delta_.getKind() & 0x2) != 0)) {
				this.removedResources.add(PropertiesEditor.this.file);
			}
			return true;
		}

		public List<IFile> getRemovedResources() {
			return this.removedResources;
		}
	}

	protected IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				_cls1ResourceDeltaVisitor visitor = new _cls1ResourceDeltaVisitor();
				delta.accept(visitor);
				if (visitor == null || visitor.getRemovedResources() == null || visitor.getRemovedResources().isEmpty())
					return;
				List<IFile> col = visitor.getRemovedResources();
				for (IFile file : col) {
					IFileEditorInput input = new FileEditorInput(file);
					final IEditorPart editor = PropertiesPlugin
							.findEditor(input);
					if (editor == null)
						continue;
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							IWorkbenchWindow window = PropertiesPlugin
									.getDefault().getWorkbench()
									.getActiveWorkbenchWindow();
							IWorkbenchPage page = null;
							if (window == null)
								return;
							page = window.getActivePage();
							if (page == null)
								return;
							page.closeEditor(editor, true);
						}
					});
				}
			} catch (CoreException localCoreException) {
			}
		}
	};

	public String getFileNamePre() {
		return this.fileNamePre;
	}

	public PropertiesEditorPage getPage() {
		return this.page;
	}

	public PropertyModel getPropertyModel() {
		return this.propertyModel;
	}

	public void doSave(IProgressMonitor monitor) {
		setDirty(false);
		IFormPage page = getActivePageInstance();
		if (page != null) {
			IManagedForm form = page.getManagedForm();
			if (form != null) {
				form.dirtyStateChanged();
			}
		}
		try {
			this.propertiesModelHelper.save();
		} catch (Exception e) {
			PropertiesPlugin.log(e);
			MessageDialog
					.openError(
							getSite().getShell(),
							Messages.getString("PropertiesEditor.dialog.saveFailed.title"),
							Messages.getString("PropertiesEditor.dialog.saveFailed.message"));
		}
		try {
			this.file.getProject().refreshLocal(2, new NullProgressMonitor());
		} catch (CoreException e) {
			PropertiesPlugin.log(e);
		}
	}

	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		if ((input != null) && (input instanceof IFileEditorInput)) {
			this.isRegularInput = true;
		} else {
			this.isRegularInput = false;
			this.input = input;
			return;
		}
		IFileEditorInput iinput = (IFileEditorInput) input;
		this.file = iinput.getFile();
		String osPath = this.file.getLocation().toOSString();
		int lastIndexOfSeparator = osPath.lastIndexOf(File.separator);
		osPath = osPath.substring(0, lastIndexOfSeparator);
		if (lastIndexOfSeparator <= 0) {
			return;
		}
		String fileName = this.file.getName();
		int index = fileName.lastIndexOf(46);
		if (index <= 0)
			return;
		this.fileNamePre = fileName.substring(0, index);
		this.fileNamePre = PropertiesFileNameValidator
				.getFileNamePre(this.fileNamePre);
		setPartName(this.fileNamePre);
		try {
			this.propertiesModelHelper = new PropertiesModelHelper(osPath,
					this.fileNamePre, 1);
			this.propertyModel = this.propertiesModelHelper.getModels();
		} catch (IOException e) {
			PropertiesPlugin.log(e);
			IWorkbenchWindow window = PropertiesPlugin.getDefault()
					.getWorkbench().getActiveWorkbenchWindow();
			MessageDialog
					.openError(
							(window == null) ? null : window.getShell(),
							Messages.getString("PropertiesEditor.dialog.initFailed.title"),
							Messages.getString("PropertiesEditor.dialog.initFailed.message"));
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				this.resourceChangeListener, 1);
	}

	public boolean isSaveAsAllowed() {
		return isDirty();
	}

	protected void addPages() {
		if (!(this.isRegularInput)) {
			PropertiesFileEditor editor = new PropertiesFileEditor() {
				public boolean isEditable() {
					return false;
				}
			};
			try {
				addPage(editor, this.input);
				setPageText(0, this.input.getName());
				return;
			} catch (PartInitException e) {
				PropertiesPlugin.log(e);
				IWorkbenchWindow window = PropertiesPlugin.getDefault()
						.getWorkbench().getActiveWorkbenchWindow();
				MessageDialog
						.openError(
								(window == null) ? null : window.getShell(),
								Messages.getString("PropertiesEditor.dialog.addPageFailed.title"),
								Messages.getString("PropertiesEditor.dialog.addPageFailed.message"));
				return;
			}
		}
		this.page = new PropertiesEditorPage(
				this,
				Messages.getString("PropertiesEditor.propertiesEditorPage.name"),
				Messages.getString("PropertiesEditor.propertiesEditorPage.title"));
		try {
			addPage(this.page);
			addNewEditorPage(this.file.getName());
			Map files = this.propertyModel.getPropertiesFiles();
			Set<String> fileNames = files.keySet();
			for (String fileName : fileNames) {
				if (fileName.equals(this.file.getName()))
					continue;
				addNewEditorPage(fileName);
			}
		} catch (PartInitException e) {
			PropertiesPlugin.log(e);
			IWorkbenchWindow window = PropertiesPlugin.getDefault()
					.getWorkbench().getActiveWorkbenchWindow();
			MessageDialog
					.openError(
							(window == null) ? null : window.getShell(),
							Messages.getString("PropertiesEditor.dialog.addPageFailed.title"),
							Messages.getString("PropertiesEditor.dialog.addPageFailed.message"));
		}
	}

	public boolean isDirty() {
		return this.Dirty;
	}

	public void setDirty(boolean isDirty) {
		this.Dirty = isDirty;
	}

	public void addNewEditorPage(String fileName) {
		if ((fileName == null) || ("".equals(fileName)))
			return;
		IPath path = Path.fromPortableString(fileName);
		IContainer container = this.file.getParent();
		IFile newFile = container.getFile(path);
		PropertiesFileEditor editor = new PropertiesFileEditor() {
			public boolean isEditable() {
				return false;
			}
		};
		try {
			addPage(editor, new FileEditorInput(newFile));
			int index = getPageCount() - 1;
			setPageText(index, fileName);
		} catch (PartInitException e) {
			PropertiesPlugin.log(e);
		}
	}

	public boolean isRegularInput() {
		return this.isRegularInput;
	}
}
