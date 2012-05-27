/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.ui.wizards.classes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.IDLTKSearchScope;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.internal.ui.dialogs.OpenTypeSelectionDialog2;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.dialogs.StatusInfo;
import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.php.internal.core.model.PhpModelAccess;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.ui.PHPUILanguageToolkit;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.dubture.pdt.formatter.core.ast.Formatter;
import com.dubture.pdt.ui.ExtensionManager;
import com.dubture.pdt.ui.PDTPluginImages;
import com.dubture.pdt.ui.codemanipulation.CodeGeneration;
import com.dubture.pdt.ui.extension.INamespaceResolver;


@SuppressWarnings("restriction")
public class ClassCreationWizardPage extends NewSourceModulePage {

	protected Text fileText;
	protected Text superClassText;
	protected ISelection selection;
	protected Label targetResourceLabel;
	protected Label superClassLabel;
	private String filename = "";	
	private String className = "";
	private String sClass = "";
	private String namespace = "";
	private String modifier = "";
	private boolean generateComments = false;
	private boolean generateAbstract = true;
	private boolean generateConstructor = false;
	
	
	private List<String> interfaces = new ArrayList<String>();
	
	private TableViewer interfaceTable;	
	private Button abstractCheckbox;
	private Button finalCheckbox;
	
	protected boolean isPEAR;	
	
	protected String initialClassName = null;
	protected String initialNamespace = null;
	protected String initialFilename = null;
	protected IScriptFolder initialFolder = null;
	
	public ClassCreationWizardPage(final ISelection selection, String initialFileName) {
		super();
		setImageDescriptor(PDTPluginImages.DESC_WIZBAN_NEW_PHPCLASS);
		this.selection = selection;
		this.initialFilename = initialFileName;
	}
	
	public ClassCreationWizardPage(final ISelection selection, String initialFileName, String namespace, String className, IScriptFolder scriptFolder) {
		this(selection, initialFileName);
		
		this.initialNamespace = namespace;
		this.initialClassName = className;
		this.initialFolder = scriptFolder;
		
	}
	

	
	private OpenTypeSelectionDialog2 getDialog(int type, String title, String message) {
		
		final Shell p = DLTKUIPlugin.getActiveWorkbenchShell();
		OpenTypeSelectionDialog2 dialog = new OpenTypeSelectionDialog2(p,
				true, PlatformUI.getWorkbench().getProgressService(), null,
				type, PHPUILanguageToolkit.getInstance());

		dialog.setTitle(title);
		dialog.setMessage(message);

		return dialog;
		
	}
	
	
	private SelectionListener changeListener = new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			dialogChanged();
			
			if (e.widget == abstractCheckbox) {
				modifier = "abstract";
			} else if (e.widget == finalCheckbox)
				modifier = "final";
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

			
		}
	};
	
	
	private SelectionListener interfaceRemoveListener = new SelectionListener() {
		
		@SuppressWarnings("rawtypes")
		@Override
		public void widgetSelected(SelectionEvent e) {
			
			ISelection select = interfaceTable.getSelection();
			
			if (select instanceof StructuredSelection) {
				
				StructuredSelection selection = (StructuredSelection) select;				
				Iterator it = selection.iterator();
				
				while(it.hasNext()) {					
					String next = (String) it.next();					
					interfaces.remove(next);					
				}
				
				interfaceTable.setInput(interfaces);
			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			
		}
	};
	
	
	
	private SelectionListener superClassSelectionListener  = new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {

			OpenTypeSelectionDialog2 dialog = getDialog(IDLTKSearchConstants.TYPE, "Superclass selection", "Select superclass");

			int result = dialog.open();
			if (result != IDialogConstants.OK_ID)
				return;
			
			Object[] types = dialog.getResult();
			if (types != null && types.length > 0) {
				IModelElement type = null;
				for (int i = 0; i < types.length; i++) {
					type = (IModelElement) types[i];
					try {
													
						String superclass = "";
						
						if (type.getParent() == null)
							return;
						
						superclass += type.getParent().getElementName() + "\\";
						
						isPEAR = false;
						if (superclass.endsWith(".php\\")) {
							superclass = "";
							isPEAR = true;
						}
						
						superclass += type.getElementName();
						
						
						superClassText.setText(superclass);
						
						ClassCreationWizardPage.this.sClass = superclass;

					} catch (Exception x) {

					}
				}
			}				
			
			
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
	private SelectionListener interfaceSelectionListener = new SelectionListener() {
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			
			OpenTypeSelectionDialog2 dialog = getDialog(IDLTKSearchConstants.TYPE, "Interface selection", "Select interface");

			int result = dialog.open();
			if (result != IDialogConstants.OK_ID)
				return;
			
			Object[] types = dialog.getResult();
			if (types != null && types.length > 0) {
				IModelElement type = null;
				for (int i = 0; i < types.length; i++) {
					type = (IModelElement) types[i];
					try {
													
						String _interface = "";
						
						if (type.getParent() == null)
							return;
						
						_interface += type.getParent().getElementName() + "\\";
						_interface += type.getElementName();
						
						interfaces.add(_interface);						
						interfaceTable.setInput(interfaces);

					} catch (Exception x) {

					}
				}
			}				
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			
		}
	};
//	private AutoCompleteField acField;
	
	/*
	private KeyListener acListener = new KeyListener() {
		
		@Override
		public void keyReleased(KeyEvent e) {

			
		}
		
		@Override
		public void keyPressed(KeyEvent e) {

			List<String> props = new ArrayList<String>();
			
//			IScriptProject project = DLTKCore.create(getProject());
//			IDLTKSearchScope scope = SearchEngine.createSearchScope(project);
//			IType[] types = PhpModelAccess.getDefault().findTypes(superClassText.getText(), MatchRule.PREFIX, 0, 0, scope, null);
//			
//			for (IType type : types) {		
//				
//				props.add(type.getElementName());					
//			}
//				
//			
//			acField.setProposals((String[]) props.toArray(new String[props.size()]));			
			
		}
	};
	*/
//	private ControlDecoration decoration;
	private Button commentCheckbox;
	private Label namespaceLabel;
	private Text namespaceText;
	private Button superClassConstructors;
	private Button abstractMethods;
	private Label filenameLabel;
	private Text filenameText;
	
	@Override
	protected void createContentControls(Composite composite, int nColumns) {
	
		createContainerControls(composite, nColumns);
		createClassControls(composite, nColumns);
		
		if (initialFolder != null) {
			setScriptFolder(initialFolder, true);
		}
	
	}

	protected void createClassControls(final Composite container, int nColumns) {
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 200;
		
		filenameLabel = new Label(container, SWT.NONE);
		filenameLabel.setText("Filename:");
		
		filenameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		filenameText.setLayoutData(gd);
		
		if (initialFilename != null) {
			filenameText.setText(initialFilename);
			filename = initialFilename;
		}
		
		filenameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e)
			{
				filename= filenameText.getText();
				dialogChanged();
				
			}
		});
		
		Label ph3= new Label(container, SWT.None);
		ph3.setText("");		
		
		namespaceLabel = new Label(container, SWT.NONE);
		namespaceLabel.setText("Namespace:");
		
		namespaceText = new Text(container, SWT.BORDER | SWT.SINGLE);
		namespaceText.setLayoutData(gd);
		
		if (initialNamespace != null) {
			namespaceText.setText(initialNamespace);
		}
		
		namespaceText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				namespace = namespaceText.getText();				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
		
		
		List<INamespaceResolver> resolvers = ExtensionManager.getDefault().getNamespaceResolvers();
		
		IScriptFolder folder = getScriptFolder();
		
		for (INamespaceResolver resolver : resolvers) {			
			String ns = resolver.resolve(folder);			
			if (ns != null && ns.length() > 0) {
				namespaceText.setText(ns);				
				break;
			}
		}
		
		namespace = namespaceText.getText();
		
		Label ph1= new Label(container, SWT.None);
		ph1.setText("");		
		
		superClassLabel = new Label(container, SWT.NONE);
		superClassLabel.setText("Superclass:");
		
		superClassText = new Text(container, SWT.BORDER | SWT.SINGLE);
		superClassText.setLayoutData(gd);
		superClassText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				sClass = superClassText.getText();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
		
		superClassText.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {				
//				decoration.hide();				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
//				decoration.show();

			}
		});


		//TODO: find a way to retrieve the fully qualified name in the autocompletion
		// this doesn't perform when simply calling type.getFullyQualifiedname() in the proposal handler
		
//		acField = new AutoCompleteField(superClassText, new TextContentAdapter(), null);
		
//		decoration = new ControlDecoration(superClassLabel, SWT.RIGHT | SWT.TOP);
		
//		Image errorImage = FieldDecorationRegistry.getDefault()
//		        .getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage();
//		decoration.setImage(errorImage);
//		decoration.setDescriptionText("Content assist available.");
//		decoration.setShowHover(true);
//		decoration.hide();
		
		Button button = new Button(container, SWT.NULL);
		button.setText("Browse...");
				
		button.addSelectionListener(superClassSelectionListener);	

		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = nColumns;
		gd.heightHint = 20;
		
		Label separator = new Label (container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(gd);		
		
		
		targetResourceLabel = new Label(container, SWT.NULL);
		targetResourceLabel.setText("Name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		fileText.setFocus();
		gd = new GridData(GridData.FILL_HORIZONTAL);
				
		fileText.setLayoutData(gd);
		
		if (initialClassName != null) {
			fileText.setText(initialClassName);
			className = initialClassName;
		}
		
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				dialogChanged();
				className = fileText.getText();
				filename = className + ".php";
				filenameText.setText(filename );
			}
		});
		
		
		Label empty = new Label(container, SWT.None);
		empty.setText("");

		Label modifierLabel = new Label(container, SWT.NULL);
		modifierLabel.setText("Modifiers:");

		gd = new GridData();
		gd.verticalAlignment = SWT.LEFT;		
		
		RowLayout modifierLayout = new RowLayout(SWT.HORIZONTAL);
		
		Composite modifierContainer = new Composite(container, SWT.NULL);
		modifierContainer.setLayout(modifierLayout);
		
		abstractCheckbox = new Button(modifierContainer, SWT.CHECK | SWT.LEFT);	
		abstractCheckbox.setText("abstract");
		abstractCheckbox.addSelectionListener(changeListener);
		
	    finalCheckbox = new Button(modifierContainer, SWT.CHECK | SWT.LEFT);
	    finalCheckbox.setText("final");		
	    finalCheckbox.addSelectionListener(changeListener);
		
	    Label dummyLabel = new Label(container, SWT.NULL);
	    dummyLabel.setText("");
	    
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		
		Label interfaceLabel = new Label(container, SWT.NULL);
		interfaceLabel.setText("Interfaces:");
		interfaceLabel.setLayoutData(gd);
		
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;		
		
		interfaceTable = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		interfaceTable.setContentProvider(ArrayContentProvider.getInstance());
		interfaceTable.setInput(interfaces);
		interfaceTable.getControl().setLayoutData(gridData);
		
		FillLayout buttonLayout = new FillLayout();
		buttonLayout.type = SWT.VERTICAL;
		
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		
		Composite buttonContainer = new Composite(container, SWT.NULL);
		buttonContainer.setLayout(buttonLayout);
		buttonContainer.setLayoutData(gd);
		
		
		Button addInterface = new Button(buttonContainer, SWT.NULL);
		addInterface.setText("Add...");
		addInterface.addSelectionListener(interfaceSelectionListener);

		Button removeInterface = new Button(buttonContainer, SWT.NULL);
		removeInterface.setText("Remove");
		
		removeInterface.addSelectionListener(interfaceRemoveListener);
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns;
		gd.heightHint = 20;
		
		Label methodLabel = new Label(container, SWT.NONE);
		methodLabel.setText("Which method stubs would you like to create?");
		methodLabel.setLayoutData(gd);
		
		Label methodDummy= new Label(container, SWT.NONE);
		methodDummy.setText("");
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns-1;
				
		superClassConstructors = new Button(container, SWT.CHECK);
		superClassConstructors .setText("Superclass constructor");
		superClassConstructors .setLayoutData(gd);
		superClassConstructors.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				generateConstructor = superClassConstructors.getSelection();				
			}
		});

		Label methodDummy2= new Label(container, SWT.NONE);
		methodDummy2.setText("");
		
		abstractMethods = new Button(container, SWT.CHECK);
		abstractMethods.setText("Inherited abstract methods");
		abstractMethods.setSelection(true);
		abstractMethods.setLayoutData(gd);
		abstractMethods.addSelectionListener(new SelectionAdapter() {		
			@Override
			public void widgetSelected(SelectionEvent e) {

				generateAbstract = abstractMethods.getSelection();
			}
		});
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns;
		gd.heightHint = 20;
		
		Label commentLabel = new Label(container, SWT.NONE);
		commentLabel.setText("Do you want to add comments?");
		commentLabel.setLayoutData(gd);
		
		Label dummy = new Label(container, SWT.NONE);
		dummy.setText("");
		
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns-1;
				
		commentCheckbox = new Button(container, SWT.CHECK);
		commentCheckbox.setText("Generate element comments");
		commentCheckbox.setLayoutData(gd);
		commentCheckbox.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				generateComments = commentCheckbox.getSelection();
			}
		});
		
		dialogChanged();
//		setControl(container);
//		Dialog.applyDialogFont(container);
				
	}
	
	protected String getContainerName() {
		
		return getScriptFolderText();		
		
	}


	protected void dialogChanged() {
		
		final String container = getContainerName();
		final String fileName = getFileName();
		
		if (abstractCheckbox.getSelection() && finalCheckbox.getSelection()) {			
			updateStatus("A class cannot be abstract and final at the same time");
			return;
		}
		if (container.length() == 0) {
			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_10); //$NON-NLS-1$
			return;
		}
		
		if (fileName != null
				&& !fileName.equals("") && getScriptFolder().getSourceModule(fileName).exists()) { //$NON-NLS-1$
			updateStatus("The specified class already exists"); //$NON-NLS-1$
			return;
		}
		
		int dotIndex = fileName.lastIndexOf('.');
		if (fileName.length() == 0 || dotIndex == 0) {
			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_15); //$NON-NLS-1$
			return;
		}

		if (dotIndex != -1) {
			String fileNameWithoutExtention = fileName.substring(0, dotIndex);
			for (int i = 0; i < fileNameWithoutExtention.length(); i++) {
				char ch = fileNameWithoutExtention.charAt(i);
				if (!(Character.isJavaIdentifierPart(ch) || ch == '.' || ch == '-')) {
					updateStatus(PHPUIMessages.PHPFileCreationWizardPage_16); //$NON-NLS-1$
					return;
				}
			}
		}
				
		String text = fileText.getText();
		
		if (text.length() > 0 && Character.isLowerCase(fileText.getText().charAt(0))) {						
			setMessage("Classes starting with lowercase letters are discouraged", IMessageProvider.WARNING);						
		} else {
			setMessage("");
		}
		
		updateStatus(new IStatus[]{new StatusInfo()});
	}

	protected void updateStatus(final String message) {
		
		setErrorMessage(message);
		setPageComplete(message == null);
	}
	
	@Override
	protected String getFileText() {

		if (initialFilename != null) {
			
			if (filenameText != null) {
				return filenameText.getText();
			}
			
			return initialFilename;
		}
		
		return super.getFileText();
	}


	public String getFileName()
	{
		return filename;
	}

	public String getSuperclass() {

		return superClassText.getText();

	}
	
	public List<String> getInterfaces() {
		
		return interfaces;
		
	}


	public String getModifiers() {

		if (abstractCheckbox.getSelection())
			return "abstract ";

		if (finalCheckbox.getSelection())
			return "final ";
			
		return "";
		
	}
	
	public boolean shouldGenerateComments() {

		return generateComments;
		
	}


	@Override
	protected String getPageTitle() {

		return "New PHP Class";

	}


	@Override
	protected String getPageDescription() {

		return "Create a new PHP Class";

	}


	@Override
	protected String getRequiredNature() {

		return PHPNature.ID;

	}
	
	
	@Override
	protected String getFileContent(ISourceModule module) throws CoreException {

		PhpModelAccess model = PhpModelAccess.getDefault();		
		IDLTKSearchScope scope = SearchEngine.createSearchScope(getScriptFolder().getScriptProject());
		
		NullProgressMonitor monitor = new NullProgressMonitor();
		IType[] superTypes = model.findTypes(sClass, MatchRule.EXACT, 0, 0, scope, monitor);
		
		IType superclass = null;
		
		if (superTypes.length == 1) {
			superclass = superTypes[0];
		}
		
		List<IType> interfaces = new ArrayList<IType>();
		
		for (String iface : getInterfaces()) {			
			IType[] ifaces = model.findTypes(iface, MatchRule.EXACT, 0, 0, scope, monitor);			
			if (ifaces.length == 1) {
				interfaces.add(ifaces[0]);
			}
		}
		
		String content = CodeGeneration.getClassStub(getScriptFolder().getScriptProject(), className, 
				namespace, modifier, superclass, interfaces, generateConstructor, generateAbstract, generateComments);
		
		IDocument doc = Formatter.createPHPDocument();
		Formatter formatter = new Formatter();		
		doc.set(content);
		formatter.format(doc);		
		return doc.get();
		
	}
	
}
