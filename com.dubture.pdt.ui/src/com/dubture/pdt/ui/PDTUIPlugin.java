package com.dubture.pdt.ui;

import java.io.IOException;

import org.eclipse.dltk.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.php.internal.ui.corext.template.php.CodeTemplateContextType;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.dubture.pdt.ui.preferences.PDTTemplateStore;
import com.dubture.pdt.ui.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class PDTUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.dubture.pdt.ui"; //$NON-NLS-1$

	// The shared instance
	private static PDTUIPlugin plugin;
	
	private PDTTemplateStore fCodeTemplateStore;
	
	protected ContextTypeRegistry codeTypeRegistry = null;

	private ImageDescriptorRegistry fImageDescriptorRegistry;

	/**
	 * The constructor
	 */
	public PDTUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PDTUIPlugin getDefault() {
		return plugin;
	}
	
	public TemplateStore getCodeTemplateStore() {
		if (fCodeTemplateStore == null) {

			fCodeTemplateStore = new PDTTemplateStore(
					getCodeTemplateContextRegistry(), getPreferenceStore(),
					PreferenceConstants.CODE_TEMPLATES_KEY);

			try {
				fCodeTemplateStore.load();
			} catch (IOException e) {
//				Logger.logException(e);
			}
		}

		return fCodeTemplateStore;
	}
	
	public ContextTypeRegistry getCodeTemplateContextRegistry() {
		if (codeTypeRegistry == null) {
			ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry();

			
			CodeTemplateContextType.registerContextTypes(registry);

			codeTypeRegistry = registry;
		}

		return codeTypeRegistry;
	}
	
	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return PDTUIPlugin.getDefault().internalGetImageDescriptorRegistry();
	}

	private ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (fImageDescriptorRegistry == null) {
			fImageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return fImageDescriptorRegistry;
	}
	
	

}
