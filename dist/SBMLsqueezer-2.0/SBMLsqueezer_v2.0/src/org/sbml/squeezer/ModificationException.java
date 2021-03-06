/*
 * $Id: ModificationException.java 1081 2014-02-22 15:15:58Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/ModificationException.java $
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
 * An exception that can be raised if a wront type of modification is assigned
 * to a reaction; it is a special kind of a
 * {@link RateLawNotApplicableException}.
 * 
 * @since 1.0
 * @version $Rev: 1081 $
 * @author Andreas Dr&auml;ger
 * @date Aug 13, 2007
 */
public class ModificationException extends RateLawNotApplicableException {
  
  /**
   * Gernerated serial version identifier.
   */
  private static final long serialVersionUID = 495775752324636450L;
  
  /**
   * Construct a new exception for cases in which transcriptional activation
   * or translational activation or inhibition, respectively, was used where
   * regular activation or inhibition had to be applied or the other way
   * arround.
   */
  public ModificationException() {
    super();
  }
  
  /**
   * Construct a new exception for cases in which transcriptional activation
   * or translational activation or inhibition, respectively, was used where
   * regular activation or inhibition had to be applied or the other way
   * arround.
   * 
   * @param message
   *            Exception message
   */
  public ModificationException(String message) {
    super(message);
  }
  
  /**
   * Construct a new exception for cases in which transcriptional activation
   * or translational activation or inhibition, respectively, was used where
   * regular activation or inhibition had to be applied or the other way
   * arround.
   * 
   * @param message
   *            Exception message
   * @param cause
   *            Reason for the exception to be thrown.
   */
  public ModificationException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
