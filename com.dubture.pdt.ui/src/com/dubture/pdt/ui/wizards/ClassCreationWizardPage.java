/*******************************************************************************
 * This file is part of the PDT Extensions eclipse plugin.
 * 
 * (c) Robert Gruendler <r.gruendler@gmail.com>
 * 
 * Modified by Marek Maksimczyk <marek.maksimczyk@mandos.net.pl>
 * 
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 ******************************************************************************/
package com.dubture.pdt.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.internal.core.SourceType;
import org.eclipse.dltk.internal.ui.dialogs.OpenTypeSelectionDialog2;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.wizards.NewSourceModulePage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.php.internal.core.project.PHPNature;
import org.eclipse.php.internal.ui.PHPUILanguageToolkit;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import com.dubture.pdt.formatter.core.ast.Formatter;
import com.dubture.pdt.ui.PDTPluginImages;
import com.dubture.pdt.ui.codemanipulation.ClassStubParameter;
import com.dubture.pdt.ui.codemanipulation.ClassStub;

@SuppressWarnings("restriction")
public class ClassCreationWizardPage extends NewSourceModulePage {

	private static final int IS_ABSTRACT_INDEX = 0;
	private static final int IS_FINAL_INDEX = 1;
	
	private static final int INTERFACES = 1;
	private static final int CLASSES = 2;
	
	private Composite container;
	private int nColumns;
	protected ISelection selection;

	protected String initialClassName = null;
	protected String initialNamespace = null;
	protected String initialFilename = null;
	protected IScriptFolder initialFolder = null;

	// TODO: Is it necessary?
	private List<IType> interfaces = new ArrayList<IType>();

	// Controls field in dialog.
	private StringDialogField classNameField;
	private StringDialogField fileNameField;
	private StringButtonDialogField superClassField;
	private StringDialogField namespaceField;
	private SelectionButtonDialogFieldGroup classModifierField;

	// TODO: Check creation process.
	private TableViewer interfaceTable;
	private Button commentCheckbox;
	private Button superClassConstructors;
	private Button abstractMethods;
	
	public ClassCreationWizardPage(final ISelection selection, String initialFileName) {
		super();
		setImageDescriptor(PDTPluginImages.DESC_WIZBAN_NEW_PHPCLASS);
		this.selection = selection;
		this.initialFilename = initialFileName;
	}

	public ClassCreationWizardPage(final ISelection selection, String initialFileName, String namespace,
			String className, IScriptFolder scriptFolder) {
		this(selection, initialFileName);

		this.initialNamespace = namespace;
		this.initialClassName = className;
		this.initialFolder = scriptFolder;
	}

	private OpenTypeSelectionDialog2 getDialog(int type, String title, String message, boolean multi) {
		boolean getClasses;
		boolean getInterfaces;

		getClasses = type == ClassCreationWizardPage.CLASSES ? true : false;
		getInterfaces = type == ClassCreationWizardPage.INTERFACES ? true : false;

		OpenTypeSelectionDialog2 dialog = new OpenTypeSelectionDialog2(DLTKUIPlugin.getActiveWorkbenchShell(), multi,
				PlatformUI.getWorkbench().getProgressService(), null, IDLTKSearchConstants.TYPE,
				new PHPTypeSelectionExtension(getClasses, getInterfaces), PHPUILanguageToolkit.getInstance());

		dialog.setTitle(title);
		dialog.setMessage(message);

		return dialog;
	}

	private SelectionListener interfaceRemoveListener = new SelectionListener() {

		@SuppressWarnings("rawtypes")
		@Override
		public void widgetSelected(SelectionEvent e) {

			ISelection select = interfaceTable.getSelection();

			if (select instanceof StructuredSelection) {

				StructuredSelection selection = (StructuredSelection) select;
				Iterator it = selection.iterator();

				while (it.hasNext()) {
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

	private SelectionListener interfaceSelectionListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {

			OpenTypeSelectionDialog2 dialog = getDialog(ClassCreationWizardPage.INTERFACES, "Interface selection",
					"Select interface", true);

			int result = dialog.open();
			if (result != IDialogConstants.OK_ID)
				return;
			IType interfaceObject;
			Object[] types = dialog.getResult();
			if (types != null && types.length > 0) {
				for (int i = 0; i < types.length; i++) {
					interfaceObject = (IType) types[i];
					interfaces.add(interfaceObject);
					interfaceTable.add(interfaceObject);
				}
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {

		}
	};

	@Override
	protected void createContentControls(Composite composite, int nColumns) {

		container = composite;
		this.nColumns = nColumns;

		createContainerControls(composite, nColumns);

		createClassControls();

		if (initialFolder != null) {
			setScriptFolder(initialFolder, true);
		}

	}

	private void createClassControls() {

		createNameControls();

		createClassModifierControls();

		createFileNameControls();

		createNamespaceControls();

		createSuperClassControls();

		createSeparator();

		createInterfaceArea();

		createAdditionalPart();

	}

	private void createAdditionalPart() {
		GridData gd;

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns;
		gd.heightHint = 20;

		Label methodLabel = new Label(container, SWT.NONE);
		methodLabel.setText("Which method stubs would you like to create?");
		methodLabel.setLayoutData(gd);

		Label methodDummy = new Label(container, SWT.NONE);
		methodDummy.setText("");

		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = nColumns - 1;

		superClassConstructors = new Button(container, SWT.CHECK);
		superClassConstructors.setText("Su&perclass constructor");
		superClassConstructors.setLayoutData(gd);
		superClassConstructors.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				superClassConstructors.getSelection();
			}
		});

		Label methodDummy2 = new Label(container, SWT.NONE);
		methodDummy2.setText("");

		abstractMethods = new Button(container, SWT.CHECK);
		abstractMethods.setText("In&herited abstract methods");
		abstractMethods.setSelection(true);
		abstractMethods.setLayoutData(gd);
		abstractMethods.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				abstractMethods.getSelection();
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
		gd.horizontalSpan = nColumns - 1;

		commentCheckbox = new Button(container, SWT.CHECK);
		commentCheckbox.setText("&Generate element comments");
		commentCheckbox.setLayoutData(gd);
		commentCheckbox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				commentCheckbox.getSelection();
			}
		});

		// dialogChanged();
		// setControl(container);
		// Dialog.applyDialogFont(container);
	}

	private void createInterfaceArea() {
		GridData gd;
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;

		Label interfaceLabel = new Label(container, SWT.NULL);
		interfaceLabel.setText("&Interfaces:");
		interfaceLabel.setLayoutData(gd);

		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		interfaceTable = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
				| SWT.BORDER);

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
		addInterface.setText("&Add...");
		addInterface.addSelectionListener(interfaceSelectionListener);

		Button removeInterface = new Button(buttonContainer, SWT.NULL);
		removeInterface.setText("&Remove");
		removeInterface.addSelectionListener(interfaceRemoveListener);
	}

	private void createSeparator() {
		GridData gd;
		gd = new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, true, true, 3, 3);
		gd.heightHint = 20;

		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(gd);
	}

	private void createClassModifierControls() {
		String[] buttonsName = { "abs&tract", "fina&l" };
		classModifierField = new SelectionButtonDialogFieldGroup(SWT.CHECK, buttonsName, nColumns);
		classModifierField.setLabelText("Modifiers:");
		classModifierField.doFillIntoGrid(container, nColumns);

		classModifierField.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField field) {

				SelectionButtonDialogFieldGroup field2 = (SelectionButtonDialogFieldGroup) field;

				if (field2.getSelectionButton(IS_ABSTRACT_INDEX).getSelection() && field2.getSelectionButton(IS_FINAL_INDEX).getSelection()) {
					updateStatus("A class cannot be abstract and final at the same time");
				}
			}
		});
	}

	private void createFileNameControls() {

		fileNameField = new StringDialogField();
		fileNameField.setLabelText("Fil&ename:");
		fileNameField.doFillIntoGrid(container, nColumns - 1);
		DialogField.createEmptySpace(container);

		fileNameField.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField field) {

				if (!fileNameField.equals("") && getScriptFolder().getSourceModule(fileNameField.getText()).exists()) { //$NON-NLS-1$
					updateStatus("The specified class already exists"); //$NON-NLS-1$
					return;
				}
				int dotIndex = fileNameField.getText().lastIndexOf('.');
				if ((fileNameField.getText().length() == 0 || dotIndex == 0) && fileNameField.getText().length() > 0) {
					updateStatus(PHPUIMessages.PHPFileCreationWizardPage_15); //$NON-NLS-1$
					return;
				}

				if (dotIndex != -1) {
					String fileNameWithoutExtention = fileNameField.getText().substring(0, dotIndex);
					for (int i = 0; i < fileNameWithoutExtention.length(); i++) {
						char ch = fileNameWithoutExtention.charAt(i);
						if (!(Character.isJavaIdentifierPart(ch) || ch == '.' || ch == '-')) {
							updateStatus(PHPUIMessages.PHPFileCreationWizardPage_16); //$NON-NLS-1$
							return;
						}
					}
				}
			}
		});
	}

	private void createNamespaceControls() {
		namespaceField = new StringDialogField();
		namespaceField.setLabelText("Na&mespace:");
		namespaceField.doFillIntoGrid(container, nColumns - 1);
		DialogField.createEmptySpace(container);

		// TODO: Autocomplete namespaces?
		// List<INamespaceResolver> resolvers =
		// ExtensionManager.getDefault().getNamespaceResolvers();
		//
		// IScriptFolder folder = getScriptFolder();
		//
		// for (INamespaceResolver resolver : resolvers) {
		// String ns = resolver.resolve(folder);
		// if (ns != null && ns.length() > 0) {
		// namespaceField.setText(ns);
		// break;
		// }
		// }
	}

	/**
	 * TODO: Create autocomplete on field.
	 */
	private void createSuperClassControls() {

		superClassField = new StringButtonDialogField(new IStringButtonAdapter() {

			@Override
			public void changeControlPressed(DialogField field) {

				OpenTypeSelectionDialog2 dialog = getDialog(ClassCreationWizardPage.CLASSES, "Superclass selection",
						"Select superclass", false);
				int result = dialog.open();
				if (result != IDialogConstants.OK_ID)
					return;

				Object searchedObject[] = dialog.getResult();
				SourceType type;
				type = (SourceType) searchedObject[0];
				((StringDialogField) field).setText(type.getElementName());
			}
		});

		superClassField.setLabelText("&Superclass:");
		superClassField.setButtonLabel("Bro&wse...");
		superClassField.doFillIntoGrid(container, nColumns);
	}

	private void createNameControls() {

		classNameField = new StringDialogField();
		classNameField.setLabelText("&Name:");
		classNameField.doFillIntoGrid(container, nColumns - 1);
		DialogField.createEmptySpace(container);
		classNameField.setFocus();

		classNameField.setDialogFieldListener(new IDialogFieldListener() {

			@Override
			public void dialogFieldChanged(DialogField field) {
				if (classNameField.getText().length() == 0) {
					setMessage("Enter name of class.", IMessageProvider.ERROR);
					fileNameField.setTextWithoutUpdate("");

					return;
				}

				fileNameField.setText(classNameField.getText() + ".php");

				if (classNameField.getText().length() > 0 && Character.isLowerCase(classNameField.getText().charAt(0))) {
					setMessage("Classes starting with lowercase letters are discouraged", IMessageProvider.WARNING);

					return;
				}
			}
		});
	}

	// protected void dialogChanged() {
	//
	// final String container = getScriptFolderText();
	//
	// if (container.length() == 0) {
	//			updateStatus(PHPUIMessages.PHPFileCreationWizardPage_10); //$NON-NLS-1$
	// return;
	// }
	//
	// }

	protected void updateStatus(final String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	@Override
	protected String getPageTitle() {

		return "PHP Class";
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
		ClassStubParameter classStubParameter = new ClassStubParameter();

		classStubParameter.setName(classNameField.getText());
		classStubParameter.setAbstractClass(classModifierField.isSelected(IS_ABSTRACT_INDEX));
		classStubParameter.setFinalClass(classModifierField.isSelected(IS_FINAL_INDEX));
		classStubParameter.setNamespace(namespaceField.getText());

		ClassStub classStub = new ClassStub(classStubParameter);
		String content = classStub.toString();

		IDocument doc = Formatter.createPHPDocument();
		Formatter formatter = new Formatter();
		doc.set(content);
		formatter.format(doc);

		return doc.get();
	}

	@Override
	protected void handleFieldChanged(String fieldName) {
		// TODO Auto-generated method stub
		super.handleFieldChanged(fieldName);
	}
}
