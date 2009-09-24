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
package org.sbml.squeezer;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.util.IllegalFormatException;
import java.util.Properties;
import java.util.Set;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginAction;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.squeezer.gui.KineticLawSelectionDialog;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.UpdateMessage;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.plugin.PluginSBMLReader;
import org.sbml.squeezer.plugin.PluginSBMLWriter;
import org.sbml.squeezer.plugin.SBMLsqueezerPlugin;
import org.sbml.squeezer.resources.Resource;
import org.sbml.squeezer.standalone.LibSBMLReader;
import org.sbml.squeezer.standalone.LibSBMLWriter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.3
 * @version
 */
public class SBMLsqueezer extends PluginAction implements LawListener {

	/**
	 * Possible command line options
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * 
	 */
	private enum Keys {
		/**
		 * Can be used in combination with = true or = false or just --gui.
		 */
		GUI,
		/**
		 * 
		 */
		SBML_FILE,
		/**
		 * Specifies the file where SBMLsqueezer writes its SBML output.
		 */
		SBML_OUT_FILE
	}

	/**
	 * The package where all kinetic equations are located.
	 */
	public static final String KINETICS_PACKAGE = "org.sbml.squeezer.kinetics";

	/**
	 * 
	 */
	private final static String configFile = "cfg/SBMLsqueezer.cfg";
	/**
	 * A serial version number.
	 */
	private static final long serialVersionUID = 4134514954192751545L;
	/**
	 * 
	 */
	private final static String userConfigFile = System
			.getProperty("user.home")
			+ "/.SBMLsqueezer/SBMLsqueezer.cfg";
	/**
	 * The number of the current SBMLsqueezer version.
	 */
	private static final String versionNumber = "1.2.2";

	/**
	 * 
	 * @return
	 */
	public static Properties getDefaultSettings() {
		Properties defaults;
		try {
			defaults = Resource.readProperties(Resource.class.getResource(
					configFile).getPath());
		} catch (IOException e) {
			defaults = new Properties();
			e.printStackTrace();
		}
		return correctProperties(defaults);
	}

	/**
	 * 
	 * @return versionNumber
	 */
	public static final String getVersionNumber() {
		return versionNumber;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (Exception e) {
			System.err.println("Error: could not load the libSBML library");
			e.printStackTrace();
			System.exit(1);
		}
		new SBMLsqueezer(args);
	}

	/**
	 * 
	 */
	public static void saveProperties(Properties settings) {
		if (!initProperties().equals(settings))
			try {
				RandomAccessFile file = new RandomAccessFile(userConfigFile,
						"rw");
				file.setLength(0);
				file.close();
				BufferedWriter bw = new BufferedWriter(new FileWriter(
						userConfigFile));
				for (Object key : settings.keySet()) {
					bw.append(key.toString());
					bw.append('=');
					bw.append(settings.get(key).toString());
					bw.newLine();
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * Creates an instance of the given properties, in which all keys are
	 * literals from the configuration enum and all values are objects such as
	 * Boolean, Integer, Double, Kinetics and so on.
	 * 
	 * @param properties
	 * @return
	 */
	private static Properties correctProperties(Properties properties) {
		Object keys[] = properties.keySet().toArray();
		Properties settings = new Properties();
		for (int i = keys.length - 1; i >= 0; i--) {
			CfgKeys k = CfgKeys.valueOf(keys[i].toString());
			String val = properties.get(keys[i]).toString();
			if (val.startsWith("user."))
				settings.put(k, System.getProperty(val));
			else if (val.equalsIgnoreCase("true")
					|| val.equalsIgnoreCase("false"))
				settings.put(k, Boolean.parseBoolean(val));
			else {
				boolean allDigit = true;
				short dotCount = 0;
				for (char c : val.toCharArray()) {
					if (c == '.')
						dotCount++;
					else
						allDigit &= Character.isDigit(c);
				}
				if (allDigit && dotCount == 0)
					settings.put(k, Integer.parseInt(val));
				else if (allDigit && dotCount == 1)
					settings.put(k, Double.parseDouble(val));
				else
					settings.put(k, val);
			}
		}
		return settings;
	}

	/**
	 * 
	 * @return
	 */
	private static Properties initProperties() {
		Properties properties;
		try {
			StringBuilder path = new StringBuilder(System
					.getProperty("user.home"));
			path.append("/.SBMLsqueezer");
			File f = new File(path.toString());
			if (!f.exists()) {
				f.mkdir();
				path.append("/SBMLsqueezer.cfg");
				f = new File(path.toString());
				if (!f.exists())
					f.createNewFile();
			}
			f = new File(userConfigFile);
			if (f.exists() && f.length() == 0) {
				BufferedReader br = new BufferedReader(new FileReader(
						Resource.class.getResource(configFile).getPath()));
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				String line;
				while ((line = br.readLine()) != null) {
					bw.append(line);
					bw.newLine();
				}
				bw.close();
				br.close();
			}
			properties = correctProperties(Resource
					.readProperties(userConfigFile));
			// avoid senseless exceptions if keys are missing.
			for (CfgKeys key : CfgKeys.values())
				if (!properties.containsKey(key))
					properties.put(key, getDefaultSettings().get(key));
		} catch (Exception e) {
			e.printStackTrace();
			properties = getDefaultSettings();
		}
		return properties;
	}

	/**
	 * Displays a short copyright message on the screen.
	 */
	private static void showAboutMsg() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					Resource.class.getResource("html/about.htm").getFile()));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("<"))
					continue;
				line = line.replace("</a>", "");
				line = line.replace("&#169;", "\uf8e9");
				line = line.replace("&#228;", "\u00e4");
				line = line.replace("&#252;", "\u00fc");
				if (line.endsWith("<br>"))
					line = line.substring(0, line.length() - 4);
				if (line.contains("<a href"))
					System.out
							.print(line.substring(0, line.indexOf('<') - 1) + ' ');
				else
					System.out.println(line);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * 
	 */
	public Set<Integer> possibleEnzymes;

	/**
	 * Tells if SBMLsqueezer checked for an update after CellDesigner has been
	 * run.
	 */
	private boolean isUpdateChecked = false;

	/**
	 * The CellDesigner plugin to which this action belongs.
	 */
	private SBMLsqueezerPlugin plugin;

	private SBMLio sbmlIo;

	/**
	 * Initializes SBMLsqueezer as a CellDesigner plug-in
	 * 
	 * @param plugin
	 */
	public SBMLsqueezer(SBMLsqueezerPlugin plugin) {
		this.plugin = plugin;
		this.possibleEnzymes = SBO.getDefaultPossibleEnzymes();
		showAboutMsg();
	}

	/**
	 * Initializes SBMLsqueezer as a stand-alone program.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public SBMLsqueezer(String... args) {
		Properties p = analyzeCommandLineArguments(args);
		possibleEnzymes = SBO.getDefaultPossibleEnzymes();
		sbmlIo = new SBMLio(new LibSBMLReader(possibleEnzymes),
				new LibSBMLWriter());
		if (p.containsKey(Keys.SBML_FILE))
			sbmlIo.readModel(p.get(Keys.SBML_FILE));
		if (p.containsKey(Keys.GUI)
				&& Boolean.parseBoolean(p.get(Keys.GUI).toString())) {
			checkForUpdate(true);
			new Thread(new Runnable() {
				public void run() {
					showGUI();
				}
			}).start();
		} else {
			if (sbmlIo.getNumErrors() > 0)
				System.err.println(sbmlIo.getWarnings());
			// Do a lot of other stuff...
			checkForUpdate(false);
			showAboutMsg();
			File outFile = null;
			if (p.containsKey(Keys.SBML_OUT_FILE))
				outFile = new File(p.get(Keys.SBML_OUT_FILE).toString());
			if (sbmlIo.getListOfModels().size() == 1)
				try {
					KineticLawGenerator klg = new KineticLawGenerator(sbmlIo
							.getSelectedModel(), initProperties());
					klg.generateLaws();
					if (klg.getFastReactions().size() > 0) {
						System.err.println("Model "
								+ sbmlIo.getSelectedModel().getId()
								+ " contains " + klg.getFastReactions().size()
								+ " fast reaction. This feature is currently"
								+ "ignored by SBMLsqueezer.");
					}
					klg.storeLaws(this);
					sbmlIo.saveChanges();
					if (outFile != null
							&& SBFileFilter.SBML_FILE_FILTER.accept(outFile))
						sbmlIo.writeSelectedModelToSBML(outFile
								.getAbsolutePath());
				} catch (IllegalFormatException e) {
					e.printStackTrace();
				} catch (ModificationException e) {
					e.printStackTrace();
				} catch (RateLawNotApplicableException e) {
					e.printStackTrace();
				} catch (SBMLException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
		}
	}

	public void currentNumber(int num) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getPossibleEnzymes() {
		return possibleEnzymes;
	}

	/**
	 * @return isUpdateChecked
	 */
	public boolean getUpdateChecked() {
		return isUpdateChecked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.PluginActionListener#myActionPerformed(java
	 * .awt.event.ActionEvent)
	 */
	/**
	 * Runs two threads for checking if an update for SBMLsqueezer is available
	 * and for initializing an instance of the SBMLsqueezerUI.
	 * 
	 * @param e
	 */
	public void myActionPerformed(ActionEvent e) {
		checkForUpdate(true);
		if (e.getSource() instanceof JMenuItem) {
			final String item = ((JMenuItem) e.getSource()).getText();
			new Thread(new Runnable() {
				public void run() {
					startSBMLsqueezerPlugin(item);
				}
			}).start();
		} else
			System.err.println("Unsupported source of action "
					+ e.getSource().getClass().getName());
	}

	/**
	 * Sets the value of isUpdateChecked to the value of b.
	 * 
	 * @param b
	 */
	public void setUpdateChecked(boolean b) {
		isUpdateChecked = b;
	}

	/**
	 * Shows the GUI of SBMLsqueezer stand-alone.
	 */
	public void showGUI() {
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo, initProperties());
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}

	public void totalNumber(int i) {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	private Properties analyzeCommandLineArguments(String[] args) {
		Properties p = new Properties();
		for (String string : args) {
			while (string.startsWith("-"))
				string = string.substring(1);
			if (string.contains("=")) {
				String keyVal[] = string.split("=");
				p.put(Keys.valueOf(keyVal[0].toUpperCase().replace('-', '_')),
						keyVal[1]);
			} else
				p.put(Keys.valueOf(string.toUpperCase().replace('-', '_')),
						Boolean.TRUE.toString());
		}
		return p;
	}

	/**
	 * Let's see if there is a later version of SBMLsqueezer available.
	 * 
	 * @param gui
	 *            Decides whether or not the update message should appear in a
	 *            graphical mode (gui = true) or as a text on the console
	 *            otherwise.
	 */
	private void checkForUpdate(final boolean gui) {
		final SBMLsqueezer squeezer = this;
		new Thread(new Runnable() {
			public void run() {
				try {
					UpdateMessage.checkForUpdate(gui, squeezer);
				} catch (IOException exc) {
					// Don't annoy people
					// JOptionPane.showMessageDialog(squeezer,
					// exc.getMessage());
					// exc.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Starts the SBMLsqueezer dialog window for kinetic law selection or LaTeX
	 * export.
	 * 
	 * @param mode
	 */
	private void startSBMLsqueezerPlugin(String mode) {
		SBMLio sbmlIO = new SBMLio(new PluginSBMLReader(plugin
				.getSelectedModel(), possibleEnzymes), new PluginSBMLWriter(
				plugin));
		Properties settings = initProperties();
		if (mode.equals(plugin.getMainPluginItemText()))
			(new KineticLawSelectionDialog(null, settings, sbmlIO))
					.setVisible(true);
		else if (mode.equals(plugin.getSqueezeContextMenuItemText()))
			new KineticLawSelectionDialog(null, settings, sbmlIO,
					((PluginReaction) plugin.getSelectedReactionNode().get(0))
							.getId());
		else if (mode.equals(plugin.getExportContextMenuItemText()))
			new KineticLawSelectionDialog(null, settings, sbmlIO
					.getSelectedModel().getReaction(
							((PluginReaction) plugin.getSelectedReactionNode()
									.get(0)).getId()));
		else if (mode.equals(plugin.getExporterItemText()))
			if (plugin.getSelectedModel() != null)
				new KineticLawSelectionDialog(null, settings, sbmlIO
						.getSelectedModel());
	}
}
