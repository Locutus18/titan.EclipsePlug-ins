/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.templates;

import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.TTCN3.TemplateRestriction;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the any or omit template.
 * 
 * @author Kristof Szabados
 * */
public final class AnyOrOmit_Template extends TTCN3Template {
	private static final String MANDATORYWARNING = "Using `*' for a mandatory field";
	private static final String SIGNATUREERROR = "Generic wildcard `*'' cannot be used for signature `{0}''";

	@Override
	/** {@inheritDoc} */
	public Template_type getTemplatetype() {
		return Template_type.ANY_OR_OMIT;
	}

	@Override
	/** {@inheritDoc} */
	public String getTemplateTypeName() {
		if (isErroneous) {
			return "erroneous any or omit";
		}

		return "any or omit";
	}

	@Override
	/** {@inheritDoc} */
	public String createStringRepresentation() {
		final StringBuilder builder = new StringBuilder("*");
		if (lengthRestriction != null) {
			builder.append(lengthRestriction.createStringRepresentation());
		}
		if (isIfpresent) {
			builder.append("ifpresent");
		}
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public void checkSpecificValue(final CompilationTimeStamp timestamp, final boolean allowOmit) {
		getLocation().reportSemanticError("A specific value expected instead of an any or omit");
	}

	@Override
	/** {@inheritDoc} */
	public void checkRecursions(final CompilationTimeStamp timestamp, final IReferenceChain referenceChain) {
		// nothing to be done
	}

	@Override
	/** {@inheritDoc} */
	public void checkThisTemplateGeneric(final CompilationTimeStamp timestamp, final IType type, final boolean isModified,
			final boolean allowOmit, final boolean allowAnyOrOmit, final boolean subCheck, final boolean implicitOmit) {
		if (!allowAnyOrOmit) {
			location.reportSemanticWarning(MANDATORYWARNING);
		}

		if (!getIsErroneous(timestamp)) {
			final IType last = type.getTypeRefdLast(timestamp);
			if (Type_type.TYPE_SIGNATURE.equals(last.getTypetype())) {
				location.reportSemanticError(MessageFormat.format(SIGNATUREERROR, last.getFullName()));
				setIsErroneous(true);
			}
		}

		checkLengthRestriction(timestamp, type);
		if (!allowOmit && isIfpresent) {
			location.reportSemanticError("`ifpresent' is not allowed here");
		}
		if (subCheck) {
			type.checkThisTemplateSubtype(timestamp, this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean chkRestrictionNamedListBaseTemplate(final CompilationTimeStamp timestamp, final String definitionName,
			final Set<String> checkedNames, final int neededCheckedCnt, final Location usageLocation) {
		usageLocation.reportSemanticError(MessageFormat.format(RESTRICTIONERROR, definitionName, getTemplateTypeName()));
		return false;
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkPresentRestriction(final CompilationTimeStamp timestamp, final String definitionName, final Location usageLocation) {
		checkRestrictionCommon(timestamp, definitionName, TemplateRestriction.Restriction_type.TR_PRESENT, usageLocation);
		usageLocation.reportSemanticError(MessageFormat.format(RESTRICTIONERROR, definitionName, getTemplateTypeName()));
		return false;
	}
	
	@Override
	/** {@inheritDoc} */
	public boolean hasSingleExpression() {
		if (lengthRestriction != null || isIfpresent /* TODO:  || get_needs_conversion()*/) {
			return false;
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getSingleExpresion(JavaGenData aData, boolean castIsNeeded) {
		StringBuilder result = new StringBuilder();

		if (castIsNeeded && (lengthRestriction != null || isIfpresent)) {
			result.append( "\t//TODO: fatal error while generating " );
			result.append( getClass().getSimpleName() );
			result.append( ".generateSingleExpression() !\n" );
			// TODO: fatal error
			return result;
		}

		aData.addBuiltinTypeImport( "Base_Template.template_sel" );
		result.append( "template_sel.ANY_OR_OMIT" );
		
		//TODO handle cast needed

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void generateJavaInit(JavaGenData aData, StringBuilder source, String name) {
		aData.addBuiltinTypeImport( "Base_Template.template_sel" );
		source.append(name);
		source.append(".assign( ");
		source.append(getSingleExpresion(aData, false));
		source.append( " );\n" );
		//TODO missing parts need to be completed

		if (isIfpresent) {
			source.append(name);
			source.append(".set_ifPresent();\n");
		}
	}
}
