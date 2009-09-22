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

import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates an equation based on an additive model.
 *  
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 *
 */
public class GRNAdditiveModel extends BasicKineticLaw implements GeneRegulatoryKinetics {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public GRNAdditiveModel(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, typeParameters);
	}
	
	public static boolean isApplicable(Reaction reaction) {
		if (SBO.isTranslation(reaction.getSBOTerm())
				|| SBO.isTranscription(reaction.getSBOTerm()))
			return true;
		return false;
	}

	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	@Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		
		ASTNode kineticLaw = new ASTNode(this);		
		kineticLaw = ASTNode.diff(ASTNode.times(m_i(), function_g(function_w(),function_v(), b_i())),function_l());
		
		//System.out.println(kineticLaw.toLaTeX());
		return kineticLaw;
	}
	
	ASTNode function_w() {
		Reaction r = getParentSBMLObject();
		Parameter p = null;
		String rId = getParentSBMLObject().getId();
		ASTNode modnode = null;
		ASTNode pnode = null;
		ASTNode node = new ASTNode(this);
		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
			Species modifierspec = r.getModifier(modifierNum)
					.getSpeciesInstance();
			ModifierSpeciesReference modifier = r.getModifier(modifierNum);
			if (SBO.isProtein(modifierspec.getSBOTerm())
					|| (SBO.isMessengerRNA(modifierspec.getSBOTerm())||SBO.isRNA(modifierspec.getSBOTerm()))) {
				if (SBO.isModifier(modifier.getSBOTerm())) {
					modnode = new ASTNode(modifier.getSpeciesInstance(), this);
					p = createOrGetParameter("w_", modifierNum, underscore, rId);
					pnode = new ASTNode(p, this);
					if (node.isUnknown())
						node = ASTNode.times(pnode, modnode);
					else
						node = ASTNode.sum(node, ASTNode.times(pnode, modnode));
				}
			}
		}
		return node;
	}
	
	ASTNode function_v(){
		//TODO: modifier sollen "externe Faktoren" sein
		Reaction r = getParentSBMLObject();
		Parameter p = null;
		String rId = getParentSBMLObject().getId();
		ASTNode modnode = null;
		ASTNode pnode = null;
		ASTNode node = new ASTNode(this);
		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
			Species modifierspec = r.getModifier(modifierNum)
					.getSpeciesInstance();
			ModifierSpeciesReference modifier = r.getModifier(modifierNum);
			if (SBO.isProtein(modifierspec.getSBOTerm())
					|| (SBO.isMessengerRNA(modifierspec.getSBOTerm())||SBO.isRNA(modifierspec.getSBOTerm()))) {
				if (SBO.isModifier(modifier.getSBOTerm())) {
					modnode = new ASTNode(modifier.getSpeciesInstance(), this);
					p = createOrGetParameter("v_", modifierNum, underscore, rId);
					pnode = new ASTNode(p, this);
					if (node.isUnknown())
						node = ASTNode.times(pnode, modnode);
					else
						node = ASTNode.sum(node, ASTNode.times(pnode, modnode));
				}
			}
		}
		return node;
	}
	
	ASTNode function_l(){
		Reaction r = getParentSBMLObject();
		Parameter p = null;
		String rId = getParentSBMLObject().getId();
		ASTNode modnode = null;
		ASTNode pnode = null;
		ASTNode node = new ASTNode(this);
		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
			Species modifierspec = r.getModifier(modifierNum)
					.getSpeciesInstance();
			ModifierSpeciesReference modifier = r.getModifier(modifierNum);
			if (SBO.isProtein(modifierspec.getSBOTerm())
					|| (SBO.isMessengerRNA(modifierspec.getSBOTerm())||SBO.isRNA(modifierspec.getSBOTerm()))) {
				if (SBO.isModifier(modifier.getSBOTerm())) {
					modnode = new ASTNode(modifier.getSpeciesInstance(), this);
					p = createOrGetParameter("lambda_", modifierNum, underscore, rId);
					pnode = new ASTNode(p, this);
					if (node.isUnknown())
						node = ASTNode.times(pnode, modnode);
					else
						node = ASTNode.sum(node, ASTNode.times(pnode, modnode));
				}
			}
		}
		return node;
	}
	
	ASTNode function_g(ASTNode w, ASTNode v, ASTNode b){		
		ASTNode node = ASTNode.sum(w,v,b);
		return node;
	}
	
	ASTNode b_i(){
		String rId = getParentSBMLObject().getId();
		Parameter b_i = createOrGetParameter("b_", rId, underscore);
		ASTNode b_i_node = new ASTNode(b_i, this);
		return b_i_node;
	}

	ASTNode m_i(){
		String rId = getParentSBMLObject().getId();
		Parameter m_i = createOrGetParameter("m_", rId, underscore);
		ASTNode m_i_node = new ASTNode(m_i, this);
		return m_i_node;
	}

}