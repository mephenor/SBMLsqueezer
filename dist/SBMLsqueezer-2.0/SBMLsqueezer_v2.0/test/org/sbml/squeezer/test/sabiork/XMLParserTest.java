/*
 * $Id: XMLParserTest.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/test/org/sbml/squeezer/test/sabiork/XMLParserTest.java $
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
package org.sbml.squeezer.test.sabiork;

import static org.junit.Assert.*;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import org.sbml.squeezer.sabiork.util.XMLParser;

/**
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class XMLParserTest {
  
  @Test
  public void testGetMultipleXMLElementTextContent()
      throws UnsupportedEncodingException, XMLStreamException {
    String[] expecteds = new String[] { "glycolysis classical",
        "glycerolipid metabolism", "glycolysis/gluconeogenesis",
        "glycosphingolipid metabolism",
        "glycerophospholipid metabolism",
    "glycine, serine and threonine metabolism" };
    String xml = "<Pathways><Pathway>glycolysis classical</Pathway><Pathway>glycerolipid metabolism</Pathway><Pathway>glycolysis/gluconeogenesis</Pathway><Pathway>glycosphingolipid metabolism</Pathway><Pathway>glycerophospholipid metabolism</Pathway><Pathway>glycine, serine and threonine metabolism</Pathway></Pathways>";
    List<String> actuals = XMLParser.getMultipleXMLElementTextContent(xml,
      "Pathway", "");
    assertArrayEquals(expecteds, actuals.toArray());
  }
  
  @Test
  public void testGetXMLElementTextContent()
      throws UnsupportedEncodingException, XMLStreamException {
    String expected = "glycolysis classical";
    String xml = "<Pathways><Pathway>glycolysis classical</Pathway><Pathway>glycerolipid metabolism</Pathway><Pathway>glycolysis/gluconeogenesis</Pathway><Pathway>glycosphingolipid metabolism</Pathway><Pathway>glycerophospholipid metabolism</Pathway><Pathway>glycine, serine and threonine metabolism</Pathway></Pathways>";
    String actual = XMLParser.getXMLElementTextContent(xml, "Pathway", "");
    assertTrue(actual.equals(expected));
  }
  
}
