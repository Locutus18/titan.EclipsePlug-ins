/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.wizards;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.editors.asn1editor.ASN1CodeSkeletons;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

/**
 * @author Kristof Szabados
 * */
public final class NewASN1ModuleCreationWizardPage extends WizardNewFileCreationPage {
	private static final String EMPTYNAMEERROR = "ASN.1 modules must have a name.";
	private static final String INVALIDMODULENAME = "Invalid ASN.1 module name {0}";
	private static final String MODULENAMEREGEXP = "[A-Z][a-zA-Z\\-_0-9]*";
	private static final Pattern MODULENAMEPATTERN = Pattern.compile(MODULENAMEREGEXP);

	private static final String TITLE = "New ASN1 module";
	private static final String DESCRIPTION = "Create a new ASN1 module";
	private static final String ERROR_MESSAGE = "When provided the extension of the ASN.1 Module must be \"asn\" or \"asn1\"";
	private static final String OCCUPIED = "This module name would create a file that already exists.";

	private boolean hasLicense;
	private final NewASN1ModuleWizard wizard;

	public NewASN1ModuleCreationWizardPage(final IStructuredSelection selection, final NewASN1ModuleWizard wizard) {
		super(TITLE, selection);
		hasLicense = LicenseValidator.check();
		this.wizard = wizard;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		final String extension = getContainerFullPath().append(getFileName()).getFileExtension();

		if (extension == null) {
			// test what will happen if we add the extension
			IPath fullPath = getContainerFullPath().append(getFileName()).addFileExtension(GlobalParser.SUPPORTED_ASN1_EXTENSIONS[1]);
			// path is invalid if any prefix is occupied by a file
			final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			while (fullPath.segmentCount() > 1) {
				if (root.getFile(fullPath).exists()) {
					setErrorMessage(OCCUPIED);
					return false;
				}
				fullPath = fullPath.removeLastSegments(1);
			}
		} else {
			// test the extension
			boolean valid = false;
			for (int i = 0; i < GlobalParser.SUPPORTED_ASN1_EXTENSIONS.length; i++) {
				if (GlobalParser.SUPPORTED_ASN1_EXTENSIONS[i].equals(extension)) {
					valid = true;
					break;
				}
			}
			if (!valid) {
				setErrorMessage(ERROR_MESSAGE);
				return false;
			}
		}

		// check modulename
		final IPath path = getContainerFullPath();
		if (hasLicense && path != null) {
			final IFile file = createFileHandle(path.append(getFileName()));
			final ProjectSourceParser projectSourceParser = GlobalParser.getProjectSourceParser(file.getProject());
			if (projectSourceParser.getLastTimeChecked() == null) {
				final WorkspaceJob job = projectSourceParser.analyzeAll();

				if (job != null) {
					try {
						job.join();
					} catch (InterruptedException e) {
						ErrorReporter.logExceptionStackTrace(e);
					}
				}
			}

			final String moduleName = getFileName();
			final int dotIndex = moduleName.indexOf('.');
			final String dotLessModuleName = dotIndex == -1 ? moduleName : moduleName.substring(0, dotIndex);
			final Module module = projectSourceParser.getModuleByName(dotLessModuleName);
			if (module != null) {
				setErrorMessage("A module with the name " + moduleName + " already exists in the project "
						+ file.getProject().getName());
				return false;
			}
		}

		// validate the syntax of the module name
		validateName();

		setErrorMessage(null);
		return true;
	}

	/**
	 * Validate the module name entered by the user.
	 * */
	private boolean validateName() {
		final String originalmoduleName = getFileName();
		if (originalmoduleName == null) {
			return false;
		}

		final int dotIndex = originalmoduleName.lastIndexOf('.');
		final String longModuleName = dotIndex == -1 ? originalmoduleName : originalmoduleName.substring(0, dotIndex);

		if ("".equals(longModuleName)) {
			setErrorMessage(EMPTYNAMEERROR);
			return false;
		}

		if (!MODULENAMEPATTERN.matcher(longModuleName).matches()) {
			setErrorMessage(MessageFormat.format(INVALIDMODULENAME, longModuleName));
			return false;
		}

		setErrorMessage(null);
		return true;
	}

	@Override
	protected InputStream getInitialContents() {

		switch (wizard.getOptionsPage().getGeneratedModuleType()) {
		case EMPTY:
			return super.getInitialContents();
		case NAME_AND_EMPTY_BODY:
			final String temporalModule = ASN1CodeSkeletons.getASN1ModuleWithEmptyBody(getModuleName());
			return new BufferedInputStream(new ByteArrayInputStream(temporalModule.getBytes()));
		case SKELETON:
			final String temporalModuleSkeleton = ASN1CodeSkeletons.getASN1ModuleSkeleton(getModuleName());
			return new BufferedInputStream(new ByteArrayInputStream(temporalModuleSkeleton.getBytes()));
		default:
			return super.getInitialContents();
		}

	}
	
	private String getModuleName() {
		final String moduleName = getFileName();
		final int dotIndex = moduleName.indexOf('.');
		final String dotLessModuleName = dotIndex == -1 ? moduleName : moduleName.substring(0, dotIndex);

		return dotLessModuleName;
	}

}
