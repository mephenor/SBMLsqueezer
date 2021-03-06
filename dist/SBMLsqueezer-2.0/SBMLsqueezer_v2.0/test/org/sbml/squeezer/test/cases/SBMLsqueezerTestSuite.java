/*
 * $Id: SBMLsqueezerTestSuite.java 1092 2014-04-10 22:44:09Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/test/org/sbml/squeezer/test/cases/SBMLsqueezerTestSuite.java $
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
package org.sbml.squeezer.test.cases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev: 1092 $
 * @since 2.0
 */
@RunWith(value=Suite.class)
@SuiteClasses(value = {
    BiBiKineticsTest.class, BiUniKineticsTest.class,
    GeneralizedMassActionTest.class, GeneRegulatoryKineticsTest.class,
    ReversibleKinetics.class, UniUniKineticsTest.class, ZeroProductsTest.class,
    ZeroReactantsTest.class
})
public class SBMLsqueezerTestSuite {
}
