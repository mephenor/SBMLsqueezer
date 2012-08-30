/*
 * $Id: SABIORKApplication.java 973 2012-08-17 13:40:55Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/SABIORKApplication.java$
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
package org.sbml.squeezer.sabiork;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.squeezer.sabiork.wizard.SABIORKWizard;


/**
* @author Matthias Rall
* @version $Rev$
* ${tags}
*/
public class SABIORKApplication {
	
	public static SBMLDocument readSBMLDocument(File f){
		SBMLDocument sbmlDocument = null;
		try {
			sbmlDocument = SBMLReader.read(f);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sbmlDocument;
	}
	
	public static void writeSBMLDocument(SBMLDocument sbmlDocument, File f){
		try {
			SBMLWriter.write(sbmlDocument, f, ' ', (short) 4);
		} catch (SBMLException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void runGUI(final File input, final File output){
		final JFrame frame = new JFrame("SBMLsqueezer");
		frame.setLayout(new BorderLayout());
		frame.setMinimumSize(new Dimension(300, 300));
		
		JButton buttonWizard = new JButton("Open SABIO-RK Wizard");
		buttonWizard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SBMLDocument result = SABIORKWizard.getResultGUI(frame, ModalityType.APPLICATION_MODAL, readSBMLDocument(input));
				writeSBMLDocument(result, output);
			}
		});
		
		frame.add(buttonWizard, BorderLayout.NORTH);
		frame.setVisible(true);
	}
	
	public static void runConsole(File input, File output){
		Integer reactionFilter = 4;
		String pathway = null;
		String tissue = null;
		String organism = null;
		String cellularLocation = null;
		Boolean isWildtype = true;
		Boolean isMutant = true;
		Boolean isRecombinant = false;
		Boolean hasKineticData = true;
		Double lowerpHValue = 7.9;
		Double upperpHValue = 14.0;
		Double lowerTemperature = -10.0;
		Double upperTemperature = 115.0;
		Boolean isDirectSubmission = true;
		Boolean isJournal = true;
		Boolean isEntriesInsertedSince = false;
		String dateSubmitted = "15/10/2008";
		
		SBMLDocument result = SABIORKWizard.getResultConsole(readSBMLDocument(input), reactionFilter, pathway, tissue, organism, cellularLocation, isWildtype, isMutant, isRecombinant, hasKineticData, lowerpHValue, upperpHValue, lowerTemperature, upperTemperature, isDirectSubmission, isJournal, isEntriesInsertedSince, dateSubmitted);
		writeSBMLDocument(result, output);
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SABIORKApplication.runGUI(new File(args[0]), new File(args[1]));
		//runConsole(new File(args[0]), new File(args[1]));
	}

}
