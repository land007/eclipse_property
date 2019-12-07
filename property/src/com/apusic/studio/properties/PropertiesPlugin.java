package com.apusic.studio.properties;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PropertiesPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.apusic.studio.admin.properties";
	private static PropertiesPlugin plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public static IWorkbenchPage[] getOpenedPages() {
		List pageList = new ArrayList();
		IEditorReference[] parts = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		for (int i = 0; i < parts.length; ++i) {
			parts[i].getEditor(true);
			try {
				parts[i].getEditorInput();
			} catch (PartInitException localPartInitException) {
			}
			parts[i].getPartName();
			parts[i].getTitle();
			parts[i].getTitleToolTip();
			IWorkbenchPage page = parts[i].getPage();
			pageList.add(page);
		}
		return ((IWorkbenchPage[]) pageList.toArray(new IWorkbenchPage[0]));
	}

	public static String[] getOpenedEditorTitle() {
		IEditorReference[] parts = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		String[] names = new String[parts.length];
		for (int i = 0; i < parts.length; ++i) {
			names[i] = parts[i].getTitle();
		}
		return names;
	}

	public static IEditorPart[] getOpenedEditor() {
		IEditorReference[] parts = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		IEditorPart[] ret = new IEditorPart[parts.length];
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = parts[i].getEditor(true);
		}
		return ret;
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		if (getDefault().getWorkbench() == null) {
			return null;
		}
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static ImageDescriptor getBundledImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(
				"com.apusic.studio.admin.properties", path);
	}

	public static ImageDescriptor findImageDescriptor(String path) {
		IPath p = new Path(path);
		if ((p.isAbsolute()) && (p.segmentCount() > 1)) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(p.segment(0), p
					.removeFirstSegments(1).makeAbsolute().toString());
		}
		return getBundledImageDescriptor(p.makeAbsolute().toString());
	}

	public Image getBundledImage(String path) {
		Image image = getImageRegistry().get(path);
		if (image == null) {
			getImageRegistry().put(path, getBundledImageDescriptor(path));
			image = getImageRegistry().get(path);
		}
		return image;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static PropertiesPlugin getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		log(e, "com.apusic.studio.admin.properties");
	}

	public static void log(Throwable e, String pluginId) {
		log(new Status(4, pluginId, 4, "Internal Error", e));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		logErrorMessage(message, "com.apusic.studio.admin.properties");
	}

	public static void logErrorMessage(String message, String pluginId) {
		log(new Status(4, pluginId, 4, message, null));
	}

	public static void showEditor(IEditorInput input, String editorId,
			boolean activate) throws PartInitException {
		IWorkbenchPage page = getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = findEditor(input);
		if (editor == null) {
			editor = page.openEditor(input, editorId, activate);
		} else if (activate)
			page.activate(editor);
		else
			page.bringToTop(editor);
	}

	public static IEditorPart findEditor(IEditorInput input) {
		IWorkbenchWindow window = getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return null;
		}
		try {
			IEditorReference[] editorRefers = page.getEditorReferences();
			for (int i = 0; i < editorRefers.length; ++i) {
				IEditorReference editorRef = editorRefers[i];
				IEditorPart editor = editorRef.getEditor(false);
				if (editor == null) {
					continue;
				}
				IEditorInput tinput = editor.getEditorInput();
				if ((input != null) && (tinput.equals(input))) {
					return editor;
				}
				if ((!(input instanceof IFileEditorInput))
						|| (!(tinput instanceof IFileEditorInput)))
					continue;
				IFile file = ((IFileEditorInput) input).getFile();
				IFile tfile = ((IFileEditorInput) tinput).getFile();
				if ((file != null) && (file.equals(tfile))) {
					return editor;
				}
			}
		} catch (Throwable t) {
			log(t);
		}
		return null;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin("com.apusic.studio.admin.properties",
				path);
	}

	protected void initializeImageRegistry(ImageRegistry reg) {
		PropertiesImages.initial(reg);
	}
}
