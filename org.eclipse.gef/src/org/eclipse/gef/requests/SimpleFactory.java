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
package org.eclipse.gef.requests;

/**
 * @author hudsonr
 * @since 2.1
 */
public class SimpleFactory implements CreationFactory {

private Class type;

/**
 * Creates a SimpleFactory.
 *
 * @param aClass The class to be instantiated using this factory.
 */
public SimpleFactory(Class aClass) {
	type= aClass;
}

/**
 * Create the new object
 *
 * @return The newly created object.
 */
public Object getNewObject() {
	try {
		return type.newInstance();
	} catch (Exception exc) {
		return null;
	}
}

/**
 * Returns the type of object this factory creates.
 *
 * @return The type of object this factory creates.
 */
public Object getObjectType() {
	return type;
}


}
