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
package org.sbml.squeezer.kinetics;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * 
 * This class creates an equation based on an additive model as defined in the
 * papers "Neural network model of gene expression." of Vohradský, J. 2001 and
 * "Nonlinear differential equation model for quantification of transcriptional
 * regulation applied to microarray data of Saccharomyces cerevisiae." of Vu, T.
 * T. & Vohradský, J. 2007
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * 
 */
public class Vohradsky extends AdditiveModelNonLinear implements
		InterfaceGeneRegulatoryKinetics {


	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public Vohradsky(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#function_v()
	 */
	ASTNode function_v() {
		return null;
	}


	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.AdditiveModelNonLinear#getSimpleName()
	 */
	public String getSimpleName() {
		return "Additive model: Vohradsk\u00fd 2001 and Vu & Vohradsk\u00fd 2007";
	}
}