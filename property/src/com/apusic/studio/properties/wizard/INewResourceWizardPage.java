package com.apusic.studio.properties.wizard;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract interface INewResourceWizardPage {
	public abstract int calcProgressMonitorWorkItems();

	public abstract void finishPage(IProgressMonitor paramIProgressMonitor)
			throws CoreException;

	public abstract String getProgressMonitorTaskName();
}
