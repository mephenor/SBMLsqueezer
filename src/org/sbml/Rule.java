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
package org.sbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public abstract class Rule extends MathContainer {

	/**
	 * 
	 */
	public Rule() {
		super();
	}

	/**
	 * @param math
	 */
	public Rule(ASTNode math) {
		super(math);
	}

	/**
	 * @param sb
	 */
	public Rule(MathContainer sb) {
		super(sb);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAlgebraic() {
		return this instanceof AlgebraicRule;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAssignment() {
		return this instanceof AssignmentRule;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isRate() {
		return this instanceof RateRule;
	}
}
