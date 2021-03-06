/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.common;

import org.eclipse.titan.regressiontests.common.actions.FormatLogTest;
import org.eclipse.titan.regressiontests.common.actions.MergeLogTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	FormatLogTest.class,
	MergeLogTest.class
	})
public class CommonTestSuite {
}
