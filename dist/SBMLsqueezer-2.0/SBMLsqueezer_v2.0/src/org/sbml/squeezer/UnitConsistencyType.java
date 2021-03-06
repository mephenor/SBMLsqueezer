/*
 * $Id: UnitConsistencyType.java 19.03.2012 17:52:21 draeger$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/UnitConsistencyType.java $
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
package org.sbml.squeezer;

/**
 * This is an enumeration of the two possible ways of how to ensure consistent
 * units within an SBML model; reacting species might either be given in
 * molecule counts ({@link #amount}) or their {@link #concentration}.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 * @date 2010-10-29
 * @version $Rev: 1082 $
 * @since 2.0
 */
public enum UnitConsistencyType {
  /**
   * Used to express that the units of a species are to be brought to substance
   * units. This can, for instance, be achieved by multiplying a species that is
   * given as a concentration with its surround compartment.
   */
  amount,
  /**
   * With this item, it can be stated that a species is to expressed in terms of
   * concentration units. This can be achieved, for instance, by dividing it by
   * its surrounding compartment.
   */
  concentration;
}
