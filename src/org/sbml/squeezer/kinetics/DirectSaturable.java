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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.kinetics;

import java.util.IllegalFormatException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates the direct saturable rate law according to Liebermeister
 * et al. 2009.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-21
 */
public class DirectSaturable extends ReversiblePowerLaw implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceModulatedKinetics {

	/**
	 * @param parentReaction
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public DirectSaturable(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, types);
		unsetSBOTerm();
		setNotes("direct saturable rate law");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.ReversiblePowerLaw#denominator(org.sbml.jsbml.Reaction,
	 *      java.lang.String)
	 */
	ASTNode denominator(Reaction r, String enzyme) {
		ASTNode denominator = super.denominator(r, enzyme);
		ASTNode forward = denominator(r, enzyme, true);
		ASTNode backward = denominator(r, enzyme, false);
		if (!forward.isUnknown())
			denominator.plus(forward);
		if (!backward.isUnknown())
			denominator.minus(backward);
		return denominator;
	}

	/**
	 * This creates the denominator parts.
	 * 
	 * @param r
	 * @param enzyme
	 * @param forward
	 *            if true forward, otherwise backward
	 * @return
	 */
	private ASTNode denominator(Reaction r, String enzyme, boolean forward) {
		ASTNode term = new ASTNode(this), curr;
		Parameter kM;
		Parameter hr = parameterHillCoefficient(r.getId(), enzyme);
		ListOf<SpeciesReference> listOf = forward ? r.getListOfReactants() : r
				.getListOfProducts();
		for (SpeciesReference specRef : listOf) {
			kM = forward ? parameterMichaelisSubstrate(r.getId(), specRef
					.getSpecies(), enzyme) : parameterMichaelisProduct(r
					.getId(), specRef.getSpecies(), enzyme);
			curr = ASTNode.frac(speciesTerm(specRef),
					new ASTNode(kM, this));
			curr.raiseByThePowerOf(ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(hr, this)));
			if (term.isUnknown())
				term = curr;
			else
				term.multiplyWith(curr);
		}
		return term;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.ReversiblePowerLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "Direct saturable rate law";
	}
}
