/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.io;

import org.sbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class LibSBMLconverter extends AbstractSBMLconverter {

	private Model model;
	private SBMLDocument doc;

	/**
	 * 
	 */
	public LibSBMLconverter(org.sbml.libsbml.Model model) {
		super();
		this.model = convert(model);
		this.model.addChangeListener(this);
	}
	
	public LibSBMLconverter(String fileName) {
		super();
		doc = (new SBMLReader()).readSBML(fileName);
		this.model = convert(doc.getModel());
		this.model.addChangeListener(this);
	}

	public static Model convert(org.sbml.libsbml.Model model) {
		Model m = new Model(model.getId());
		// TODO
		return m;
	}

}
