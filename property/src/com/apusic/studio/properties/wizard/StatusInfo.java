package com.apusic.studio.properties.wizard;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

public class StatusInfo implements IStatus {
	public static final IStatus OK_STATUS = new StatusInfo();
	private String statusMessage;
	private int severity;

	public StatusInfo() {
		this(0, null);
	}

	public StatusInfo(int severity, String message) {
		this.statusMessage = message;
		this.severity = severity;
	}

	public boolean isOK() {
		return (this.severity == 0);
	}

	public boolean isWarning() {
		return (2 == this.severity);
	}

	public boolean isInfo() {
		return (1 == this.severity);
	}

	public boolean isError() {
		return (4 == this.severity);
	}

	public String getMessage() {
		return this.statusMessage;
	}

	public void setError(String errorMessage) {
		Assert.isNotNull(errorMessage);
		this.statusMessage = errorMessage;
		this.severity = 4;
	}

	public void setWarning(String warningMessage) {
		Assert.isNotNull(warningMessage);
		this.statusMessage = warningMessage;
		this.severity = 2;
	}

	public void setInfo(String infoMessage) {
		Assert.isNotNull(infoMessage);
		this.statusMessage = infoMessage;
		this.severity = 1;
	}

	public void setOK() {
		this.statusMessage = null;
		this.severity = 0;
	}

	public boolean matches(int severityMask) {
		return ((this.severity & severityMask) != 0);
	}

	public boolean isMultiStatus() {
		return false;
	}

	public int getSeverity() {
		return this.severity;
	}

	public String getPlugin() {
		return "com.apusic.studio.admin.properties";
	}

	public Throwable getException() {
		return null;
	}

	public int getCode() {
		return this.severity;
	}

	public IStatus[] getChildren() {
		return new IStatus[0];
	}
}
