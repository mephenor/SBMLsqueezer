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
package org.sbml.squeezer.kinetics;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;
import org.sbml.squeezer.util.SBMLtools;

/**
 * Force-dependent modular rate law (FM) according to Liebermeister et al. 2010.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2009-09-21
 * @since 1.3
 * @version $Rev$
 */
public class ForceDependentModularRateLaw extends PowerLawModularRateLaw
		implements InterfaceUniUniKinetics, InterfaceBiUniKinetics,
		InterfaceBiBiKinetics, InterfaceArbitraryEnzymeKinetics,
		InterfaceReversibleKinetics, InterfaceModulatedKinetics {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 5981285140198111258L;

	/**
	 * 
	 * @param parentReaction
	 * @param types
	 * @throws RateLawNotApplicableException
	 */
	public ForceDependentModularRateLaw(Reaction parentReaction,
			Object... types) throws RateLawNotApplicableException {
		super(parentReaction, types);
		SBMLtools.setSBOTerm(this,532); // force-dependent modular rate law
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.ReversiblePowerLaw#denominator(org.sbml.jsbml
	 * .Reaction)
	 */
	ASTNode denominator(String enzyme) {
		ASTNode denominator = new ASTNode(this);
		ASTNode forward = denominator(enzyme, true);
		ASTNode backward = denominator(enzyme, false);
		if (!forward.isUnknown())
			denominator = forward;
		if (!backward.isUnknown()) {
			if (!denominator.isUnknown())
				denominator.multiplyWith(backward);
			else
				denominator = backward;
		}
		denominator.sqrt();
		ASTNode competInhib = specificModificationSummand(enzyme);
		return competInhib == null ? denominator : denominator
				.plus(competInhib);
	}

	/**
	 * This actually creates the parts of the denominator.
	 * 
	 * @param forward
	 *            true means forward, false backward.
	 * @return
	 */
	private final ASTNode denominator(String enzyme, boolean forward) {
		ASTNode term = new ASTNode(this), curr;
		LocalParameter kM;
		Reaction r = getParentSBMLObject();
		ListOf<SpeciesReference> listOf = forward ? r.getListOfReactants() : r
				.getListOfProducts();
		LocalParameter hr = parameterFactory
				.parameterReactionCooperativity(enzyme);
		for (SpeciesReference specRef : listOf) {
			kM = parameterFactory.parameterMichaelis(specRef.getSpecies(),
					enzyme, forward);
			curr = ASTNode.frac(speciesTerm(specRef), new ASTNode(kM, this));
			curr.raiseByThePowerOf(ASTNode.times(new ASTNode(hr, this),
					new ASTNode(specRef.getStoichiometry(), this)));
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
		return Bundles.MESSAGES.getString("FORCE_DEPENDENT_MODULAR_RATE_LAW_SIMPLE_NAME");
	}
}
