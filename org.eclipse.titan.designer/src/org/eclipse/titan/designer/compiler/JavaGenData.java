package org.eclipse.titan.designer.compiler;

import java.util.Set;
import java.util.TreeSet;

/**
 * Helper class for java code generation.
 * The info is collected here before it is written out to the java files
 * @author Arpad Lovassy
 */
public class JavaGenData {

	/** the java source file without the import part */
	private StringBuilder mSrc;

	/** The imports with short class names */
	private Set<String> mImports;

	/**
	 * true for debug mode: debug info is written as comments in the generated code
	 */
	private boolean mDebug;

	public JavaGenData() {
		mSrc = new StringBuilder();
		// TreeSet keeps elements in natural order (alphabetical)
		mImports = new TreeSet<String>();
		mDebug = false;
	}

	/**
	 * @return the string where new java code is written
	 */
	public StringBuilder getSrc() {
		return mSrc;
	}

	/**
	 * Adds a new import
	 * @param aNewImport the new import with short class name. It is ignored in case of duplicate.
	 */
	public void addImport( final String aNewImport ) {
		mImports.add( aNewImport );
	}

	/**
	 * @return the imports with short class names in alphabetical order
	 */
	public Set<String> getImports() {
		return mImports;
	}

	public boolean isDebug() {
		return mDebug;
	}

	public void setDebug( final boolean aDebug ) {
		mDebug = aDebug;
	}

}