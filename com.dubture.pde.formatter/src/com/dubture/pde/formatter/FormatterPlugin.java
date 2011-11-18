package com.dubture.pde.formatter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


import org.eclipse.core.internal.preferences.PreferencesService;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.osgi.framework.BundleContext;

import com.dubture.pde.formatter.internal.core.formatter.CodeFormatterOptions;

public class FormatterPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.dubture.pde.formatter"; //$NON-NLS-1$
	public static final String OPTION_ID = "jp.sourceforge.pdt_tools"; //$NON-NLS-1$

	public static final String FORMATTER_PROFILE = PLUGIN_ID
			+ ".formatter_profile"; //$NON-NLS-1$

	public static final String HELP_ID = "com.dubture.pde.formatter.help." //$NON-NLS-1$
			+ Locale.getDefault().getLanguage();
	public static final String HELP_ID_FORMATTER = HELP_ID + ".formatter"; //$NON-NLS-1$
	public static final String HELP_ID_SETTINGS = HELP_ID + ".settings"; //$NON-NLS-1$

	public static final String MARKER_ID = PLUGIN_ID + ".problem"; //$NON-NLS-1$

	private static FormatterPlugin plugin;

	private IPreferenceStore fCombinedPreferenceStore;

	public FormatterPlugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static FormatterPlugin getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e));
	}

	public static void log(int severity, String message) {
		log(new Status(severity, PLUGIN_ID, message));
	}

	public static void warning(String string, ASTNode node, int offset, int end) {
		log(new Status(IStatus.WARNING, PLUGIN_ID, "Could not find '" + string
				+ "' @" + node.getClass().getSimpleName() + " [" + offset + "-"
				+ end + "]"));
	}

	public IFile getFile(IDocument document) {
		IFile file = null;
		IStructuredModel structuredModel = null;
		try {
			structuredModel = StructuredModelManager.getModelManager()
					.getExistingModelForRead(document);
			if (structuredModel != null) {
				String location = structuredModel.getBaseLocation();
				if (location != null) {
					file = ResourcesPlugin.getWorkspace().getRoot()
							.getFile(new Path(location));
				}
			}
		} finally {
			if (structuredModel != null) {
				structuredModel.releaseFromRead();
			}
		}
		return file;
	}

	public Map<String, String> getOptions(IProject project) {
		CodeFormatterOptions options = new CodeFormatterOptions(null);
		PreferencesService service = PreferencesService.getDefault();
		IScopeContext[] contexts = (project != null) ? new IScopeContext[] {
				new ProjectScope(project), new InstanceScope() }
				: new IScopeContext[] { new InstanceScope() };
		HashMap<String, String> settings = new HashMap<String, String>();
		Iterator<String> it = options.getMap().keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = service.getString(PLUGIN_ID, key, null, contexts);
			if (value != null) {
				settings.put(key, value);
			}
		}
		if (!settings.isEmpty()) {
			options.set(settings);
		}
		return options.getMap();
	}

	public IPreferenceStore getCombinedPreferenceStore() {
		if (fCombinedPreferenceStore == null) {
			IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
			fCombinedPreferenceStore = new ChainedPreferenceStore(
					new IPreferenceStore[] { getPreferenceStore(),
							generalTextStore });
		}
		return fCombinedPreferenceStore;
	}

	public void createMarker(IResource resource, int severity, String message,
			boolean persist) {
		try {
			IMarker[] markers = resource.findMarkers(MARKER_ID, false,
					IResource.DEPTH_INFINITE);
			for (IMarker marker : markers) {
				Object sev = marker.getAttribute(IMarker.SEVERITY);
				if (sev != null && !sev.equals(severity)) {
					continue;
				}
				Object msg = marker.getAttribute(IMarker.MESSAGE);
				if (msg != null && !msg.equals(message)) {
					continue;
				}
				return;
			}
		} catch (CoreException e) {
		}
		try {
			IMarker marker = resource.createMarker(MARKER_ID);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(IMarker.SEVERITY, severity);
			map.put(IMarker.MESSAGE, message);
			if (!persist) {
				map.put(IMarker.TRANSIENT, true);
			}
			marker.setAttributes(map);
		} catch (CoreException e) {
		}
	}
}
