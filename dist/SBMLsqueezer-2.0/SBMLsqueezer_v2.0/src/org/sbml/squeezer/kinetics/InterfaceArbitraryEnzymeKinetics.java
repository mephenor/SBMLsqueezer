/*
 * $Id: InterfaceArbitraryEnzymeKinetics.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/InterfaceArbitraryEnzymeKinetics.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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

/**
 * Kinetic law objects that implement this interface are able to describe any
 * enzyme-catalyzed process whether or not an enzyme is explicitely assigned to
 * the reaction. In this context, arbitrary means that the stoichiometry of
 * reactants and products can be any real or integer number.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-09-22
 * @since 1.3
 * @version $Rev: 1077 $
 */
public interface InterfaceArbitraryEnzymeKinetics extends InterfaceKinteticsType {
  
}
