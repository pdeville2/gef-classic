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
package org.eclipse.gef.ui.actions;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.actions.RetargetAction;

import org.eclipse.gef.internal.GEFMessages;

/**
 * @author Eric Bordeau
 */
public class CopyRetargetAction extends RetargetAction {

/**
 * Constructs a new CopyRetargetAction with the default ID, label and image.
 */
public CopyRetargetAction() {
	super(IWorkbenchActionConstants.COPY, GEFMessages.CopyAction_Label);
	setImageDescriptor(WorkbenchImages.getImageDescriptor(
								ISharedImages.IMG_TOOL_COPY));
	setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(
								ISharedImages.IMG_TOOL_COPY_HOVER));
	setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(
								ISharedImages.IMG_TOOL_COPY_DISABLED));
}								

}
