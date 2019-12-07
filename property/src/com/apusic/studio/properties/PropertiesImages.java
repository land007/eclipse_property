package com.apusic.studio.properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

public class PropertiesImages {
	public static final String IMG_PROP_DIALOG = "icons/properties_editor/properties_.gif";
	public static final String IMG_PROP_ADD = "icons/properties_editor/add.gif";
	public static final String IMG_PROP_REMOVE = "icons/properties_editor/remove.gif";
	public static final String IMG_EDITOR_POSITIVE = "icons/properties_editor/positive.gif";
	public static final String IMG_EDITOR_NEGATIVE = "icons/properties_editor/negative.gif";

	static void initial(ImageRegistry reg) {
		createImage(reg, "icons/properties_editor/properties_.gif");
		createImage(reg, "icons/properties_editor/add.gif");
		createImage(reg, "icons/properties_editor/remove.gif");
		createImage(reg, "icons/properties_editor/positive.gif");
		createImage(reg, "icons/properties_editor/negative.gif");
	}

	private static void createImage(ImageRegistry imageRegistry,
			String imageName) {
		ImageDescriptor imgDesc = ImageDescriptor
				.createFromURL(PropertiesPlugin.getDefault().getBundle()
						.getEntry(imageName));
		imageRegistry.put(imageName, imgDesc);
	}

	public static ImageRegistry getImageRegistry() {
		return PropertiesPlugin.getDefault().getImageRegistry();
	}

	public static ImageDescriptor getImageDescriptor(String key) {
		return PropertiesPlugin.getDefault().getImageRegistry()
				.getDescriptor(key);
	}

	public static Image getImage(String key) {
		return PropertiesPlugin.getDefault().getImageRegistry().get(key);
	}
}
