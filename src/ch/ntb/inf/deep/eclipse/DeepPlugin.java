package ch.ntb.inf.deep.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class DeepPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static DeepPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	/**
	 *  default Lib Path
	 */
	public static final String LIB_PATH = "I:/deep/lib";
	
	/**
	 * Unique identifier for the deep model (value 
	 * <code>deep.Model</code>).
	 */
	public static final String ID_DEEP_MODEL = "deep.Model";
	
	/**
	 * Launch configuration attribute key. Value is a path to a java
	 * program. The path is a string representing a full path
	 * to the top class in the workspace. 
	 */
	public static final String ATTR_DEEP_PROGRAM = ID_DEEP_MODEL + ".ATTR_DEEP_PROGRAM";
	/**
	 * Launch configuration attribute key. Value is a path to a java
	 * project. The path is a string representing a full path
	 * to the project folder in the workspace. 
	 */
	public static final String ATTR_DEEP_LOCATION = ID_DEEP_MODEL + ".ATTR_DEEP_LOCATION";
	/**
	 * Launch configuration attribute key. Value is a path to a target image.
	 * The path is a string representing a full path
	 * to a target image in the workspace. 
	 */
	public static final String ATTR_TARGET_CONFIG = ID_DEEP_MODEL + ".ATTR_TARGET_CONFIG";
	
	/**
	 * Identifier for the deep launch configuration type
	 * (value <code>deep.launchType</code>)
	 */
	public static final String ID_DEEP_LAUNCH_CONFIGURATION_TYPE = "deep.launchType";	
	
	/** 
	 * The relative path to the images directory. 
	 */
	private static final String IMAGES_PATH = "icons/";
	
	/**
	 * Plug-in identifier.
	 */
	public static final String PLUGIN_ID = "ch.ntb.inf.deep";
	
	/**
	 * The constructor.
	 */
	public DeepPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static DeepPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = DeepPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("ch.ntb.inf.debug.core.mpc555.DebugCorePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}
	
	
	/**
	 * Return a <code>java.io.File</code> object that corresponds to the specified
	 * <code>IPath</code> in the plugin directory, or <code>null</code> if none.
	 */
	public static File getFileInPlugin(String relPath) {
		try {
			final Bundle pluginBundle =  Platform.getBundle(DeepPlugin.PLUGIN_ID);
			
			final Path filePath = new Path(relPath);
			
			final URL fileUrl = FileLocator.find(pluginBundle, filePath, null);

			URL localURL = FileLocator.toFileURL(fileUrl);
			
			return new File(localURL.getFile());
		} catch (IOException ioe) {
			return null;
		}
	}
	
	public static Image createImage(String imagePath)
    {
         final Bundle pluginBundle =
              Platform.getBundle(DeepPlugin.PLUGIN_ID);

         final Path imageFilePath =
              new Path(DeepPlugin.IMAGES_PATH + imagePath);

         final URL imageFileUrl =
              FileLocator.find(pluginBundle, imageFilePath, null);

         return
              ImageDescriptor.createFromURL(imageFileUrl).createImage();
    }

}
