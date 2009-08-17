/*
 * Feb 8, 2008
 *
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbml.squeezer.kinetics;

import java.io.IOException;
import java.util.List;

import org.sbml.Model;
import org.sbml.Reaction;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Feb 8, 2008
 **/
public class ZerothOrderReverseGMAK extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public ZerothOrderReverseGMAK(Reaction parentReaction,
			Model model) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
		reactantOrder = Double.NaN;
		productOrder = 0;
	}

	public ZerothOrderReverseGMAK(Reaction parentReaction,
			Model model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
		reactantOrder = Double.NaN;
		productOrder = 0;
	}


	// @Override
	protected Object dissociation(List<String> catalysts, int c) {
		StringBuffer kdiss = concat("kdiss_", getParentSBMLObject().getId());
		if (catalysts.size() > 0)
			kdiss = concat(kdiss, underscore, catalysts.get(c));
		addLocalParameter(kdiss);
		return new StringBuffer(kdiss);
	}
}
