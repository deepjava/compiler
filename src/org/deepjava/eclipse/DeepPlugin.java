/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.deepjava.eclipse;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.deepjava.eclipse.ui.view.ConsoleDisplayMgr;
import org.deepjava.host.StdStreams;
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
	 *  default entries for deep projects settings
	 */
	public static final String LIB_PATH = "X:\\public-programme\\deep\\lib";
	public static final String BOARD = "MicroZed";
	public static final String OS = "sts_arm";
	public static final String PROGRAMMER = "OpenOCD";
	public static final String PROGRAMMER_OPTIONS = "localhost_4444";
	
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
	 * project. The path is a string representing workspace relative path. 
	 */
	public static final String ATTR_DEEP_LOCATION = ID_DEEP_MODEL + ".ATTR_DEEP_LOCATION";
	/**
	 * Launch configuration attribute key. Value is a path to a target configuration.
	 * The path is a string representing a full path to a target image in the workspace. 
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
	public static final String PLUGIN_ID = "org.deepjava.compiler";
	
	/**
	 * The constructor.
	 */
	public DeepPlugin() {
		super();
		plugin = this;
		ConsoleDisplayMgr cdm = ConsoleDisplayMgr.getDefault();
		//Init Console
		if(cdm != null){
			cdm.clear();
			PrintStream log = new PrintStream(cdm.getNewIOConsoleOutputStream(ConsoleDisplayMgr.MSG_INFORMATION));
			PrintStream err = new PrintStream(cdm.getNewIOConsoleOutputStream(ConsoleDisplayMgr.MSG_ERROR));
			PrintStream vrb = new PrintStream(cdm.getNewIOConsoleOutputStream(ConsoleDisplayMgr.MSG_VERBOSE));
			StdStreams.vrb = vrb;
			StdStreams.log = log;
			StdStreams.err = err;
		}
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
				resourceBundle = ResourceBundle.getBundle("org.deepjava.debug.core.mpc555.DebugCorePluginResources");
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
