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

import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.ui.IEditorPart;

/**
 * An action to save the editor's current state.
 */
public class SaveAction
	extends EditorPartAction
{

/**
 * Creates a <code>SaveAction</code> and associates it with the 
 * given editor.
 */
public SaveAction(IEditorPart editor) {
	super(editor);
}

/**
 * Returns <code>true<code> if the editor is dirty.
 */
protected boolean calculateEnabled() {
	return getEditorPart().isDirty();
}

/**
 * Initializes this action's text.
 */
protected void init(){
	setText(GEFMessages.SaveAction_Label);
	setToolTipText(GEFMessages.SaveAction_Tooltip);
	setId(GEFActionConstants.SAVE);
}

/**
 * Saves the state of the associated editor.
 */
public void run() {
	getEditorPart().getSite().getPage().saveEditor(getEditorPart(), false);
}

}
