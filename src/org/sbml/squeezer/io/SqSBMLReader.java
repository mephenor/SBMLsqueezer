/*

 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;


/**
 * This class provides methods to create JSBML models independently from libSBML.
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 9, 2011
 * @since 1.4
 * @version $Rev$
 */
public class SqSBMLReader implements SBMLInputConverter {

	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
	
	/**
	 * 
	 */
	private LinkedList<TreeNodeChangeListener> listOfTreeNodeChangeListeners;
	/**
	 * working copy of the original JSBL model
	 */
	Model model;

	/**
	 * original JSBML model
	 */
	Model originalModel;
	
	/**
	 * 
	 */
	private HashSet<SBMLDocument> setOfDocuments;
	
	/**
	 * 
	 */
	private HashSet<IOProgressListener> setOfIOListeners;
	
	/**
	 * 
	 */
	public SqSBMLReader() {
		listOfTreeNodeChangeListeners = new LinkedList<TreeNodeChangeListener>();
		setOfDocuments = new HashSet<SBMLDocument>();
		setOfIOListeners = new HashSet<IOProgressListener>();
	}

	/**
	 * 
	 * @param model
	 * @throws Exception
	 */
	public SqSBMLReader(Object model) throws Exception {
		this();
		this.model = convertModel(model);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#addIOProgressListener(org.sbml.jsbml.util.IOProgressListener)
	 */
	public void addIOProgressListener(IOProgressListener listener) {
		setOfIOListeners.add(listener);

	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#convertModel(java.lang.Object)
	 */
	public Model convertModel(Object model) throws IllegalArgumentException, IOException, XMLStreamException {
		if (model instanceof String) {
			// file name or XML given; create SBML Document
			SBMLDocument doc = model2SBML(model.toString());
			// construct and return model
			return readModelFromSBML(doc);
		} else if (model instanceof Model) {
			// JSBML model given; return model
			return (Model) model;
		} else if (model instanceof SBMLDocument) {
			// SBMLDocument given; construct and return model
			return readModelFromSBML((SBMLDocument) model);
		} else {
			throw new IllegalArgumentException(WARNINGS.getString("WRONG_MODEL_INSTANCE"));
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#getErrorCount()
	 */
	public int getErrorCount() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#getNumErrors()
	 */
	@Deprecated
	public int getNumErrors() {
		return getErrorCount();
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#getOriginalModel()
	 */
	public Model getOriginalModel() {
		return originalModel;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#getWarnings()
	 */
	public List<SBMLException> getWarnings() {
		return new ArrayList<SBMLException>(0);
	}

	/**
	 * 
	 * @param model			a file name or XML String 
	 * @return				the corresponding sBML document
	 * @throws IOException
	 * @throws XMLStreamException 
	 */
	private SBMLDocument model2SBML(String model)
	throws IOException, XMLStreamException {
		SBMLDocument doc = null;
		File file = new File(model.toString());
		if (!file.exists() || !file.isFile() || !file.canRead()) {
			// XML
			doc = SBMLReader.read((String) model);
		} else {
			// File name
			doc = SBMLReader.read(file);
		}
		setOfDocuments.add(doc);
		return doc;
	}

	/**
	 * 
	 * @param sbmldoc
	 * @param model
	 * @return
	 */
	private Model readModelFromSBML(SBMLDocument sbmldoc) {
		// set original model
		this.originalModel = sbmldoc.getModel();
		// We can directly work with the original model, no copy needed here:
		this.model = this.originalModel;
		// add all SBaseChangeListeners to model
    if (model != null) {
      this.model.addAllChangeListeners(listOfTreeNodeChangeListeners);
    }
		// return working copy
		return this.model;
	}

}
