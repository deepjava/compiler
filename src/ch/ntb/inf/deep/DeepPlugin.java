package ch.ntb.inf.deep;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class DeepPlugin extends Plugin {
	//The shared instance.
	private static DeepPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
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
	public static final String ATTR_TARGET_IMAGE = ID_DEEP_MODEL + ".ATTR_TARGET_IMAGE";
	
	/**
	 * Identifier for the deep launch configuration type
	 * (value <code>deep.launchType</code>)
	 */
	public static final String ID_DEEP_LAUNCH_CONFIGURATION_TYPE = "deep.launchType";	
	
	
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
	public static File getFileInPlugin(IPath path) {
		try {
			URL installURL =
				new URL(getDefault().getDescriptor().getInstallURL(), path.toString());
			URL localURL = Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException ioe) {
			return null;
		}
	}	
}
