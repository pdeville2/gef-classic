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
package org.eclipse.gef.examples.logicdesigner.model;

import org.eclipse.swt.graphics.Image;

import org.eclipse.gef.examples.logicdesigner.LogicMessages;

public class GroundOutput
	extends SimpleOutput
{

private static Image GROUND_ICON = new Image(null, GroundOutput.class.getResourceAsStream("icons/ground16.gif")); //$NON-NLS-1$
static final long serialVersionUID = 1;

public Image getIconImage() {
	return GROUND_ICON;
}

public boolean getResult() {
	return false;
}

public String toString() {
	return LogicMessages.GroundOutput_LabelText;
}

}
