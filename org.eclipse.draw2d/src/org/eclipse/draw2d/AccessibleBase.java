/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d;

public class AccessibleBase {

public final int getAccessibleID() {
	/*This assumes that the native implementation of hashCode in Object is to
	 * return the pointer to the Object, which should be U-unique.
	 */
	int value = super.hashCode();
	/*
	 * Values -3, -2, and -1 are reserved by SWT's ACC class to have special meaning.
	 * Therefore, a child cannot have this value.
	 */
	if (value < 0)
		value -= 4;
	return value;
}

}