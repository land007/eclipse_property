package com.apusic.studio.properties.wizard;

import com.apusic.studio.properties.PropertiesPlugin;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.osgi.framework.Bundle;

public class NewResourcesWizard extends BasicNewResourceWizard {
	public static final String ID = "com.apusic.studio.properties.wizard.NewResourcesWizard";
	private String fNewResourceFileName;
	private boolean enableLocation;
	private IStatus finishStatus;
	private ConfigureNewResourceFilesPage configureResourceFilePage;

	public NewResourcesWizard() {
		this(null);
	}

	public NewResourcesWizard(String newResourceFileName) {
		this(newResourceFileName, true);
	}

	public NewResourcesWizard(String newResourceFileName, boolean enableLocation) {
		this.fNewResourceFileName = newResourceFileName;
		this.enableLocation = enableLocation;
	}

	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(Messages
				.getString("NewResourcesWizard.wizard.window.title"));
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		super.addPages();
		this.configureResourceFilePage = new ConfigureNewResourceFilesPage(
				"configureResourceFiles");
		addPage(this.configureResourceFilePage);
		this.configureResourceFilePage.init(getSelection(),
				this.fNewResourceFileName, this.enableLocation);
	}

	public boolean performFinish() {
		return true;
	}
}
