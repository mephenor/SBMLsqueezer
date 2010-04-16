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
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;

/**
 * A {@link JPanel} containing a {@link JTabbedPane} with several options for
 * the configuration of {@link SBMLsqueezer} in a GUI.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 * @date 2009-09-22
 */
public class SettingsPanelAll extends SettingsPanel {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 3189416350182046246L;

	/**
	 * 
	 */
	private SettingsPanelKinetics panelKinSettings;

	/**
	 * 
	 */
	private SettingsPanelLaTeX panelLatexSettings;

	/**
	 * 
	 */
	private SettingsPanelDefaultMechanisms panelDefaultMechanisms;

	/**
	 * 
	 */
	private SettingsPanelStability panelStabilitySettings;

	/**
	 *
	 */
	private SettingsPanelSimulation panelSimulationSettings;
	/**
	 * 
	 */
	private JTextField tfOpenDir;

	/**
	 * 
	 */
	private JTextField tfSaveDir;

	/**
	 * 
	 */
	private JTabbedPane tab;

	/**
	 * 
	 */
	private boolean reversibility;

	/**
	 * 
	 * @param properties
	 */
	public SettingsPanelAll(Properties properties) {
		super(properties);
		setLayout(new GridLayout(1, 1));
		reversibility = ((Boolean) properties
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue();
		init();
	}

	private void init() {
		tab = new JTabbedPane();
		/*
		 * Kinetics settings
		 */
		panelKinSettings = new SettingsPanelKinetics(this.settings);
		tab.addTab("Kinetics settings", panelKinSettings);

		/*
		 * Reaction mechanism settings
		 */
		panelDefaultMechanisms = new SettingsPanelDefaultMechanisms(
				this.settings);
		tab.addTab("Reaction mechanisms", new JScrollPane(
				panelDefaultMechanisms,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		/*
		 * Program settings
		 */
		tfOpenDir = new JTextField();
		tfSaveDir = new JTextField();
		tfOpenDir.addKeyListener(this);
		tfSaveDir.addKeyListener(this);
		LayoutHelper lh = new LayoutHelper(new JPanel());
		lh.add(new JLabel("Open directory:"), 0, 0, 1, 1, 1, 0);
		lh.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		tfOpenDir.setText(this.settings.get(CfgKeys.OPEN_DIR).toString());
		lh.add(tfOpenDir, 2, 0, 1, 1, 1, 0);
		lh.add(new JLabel("Save directory:"), 0, 2, 1, 1, 1, 0);
		tfSaveDir.setText(this.settings.get(CfgKeys.SAVE_DIR).toString());
		lh.add(tfSaveDir, 2, 2, 1, 1, 1, 0);
		tab.addTab("Program settings", lh.getContainer());
		setLayout(new BorderLayout());

		/*
		 * LaTeX Settings
		 */
		this.panelLatexSettings = new SettingsPanelLaTeX(settings, false);
		tab.addTab("LaTeX output settings", new JScrollPane(panelLatexSettings,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		// this.add(tab);
		// addItemListener(this);

		/*
		 * Stability Settings
		 */
		panelStabilitySettings = new SettingsPanelStability(this.settings);
		tab.addTab("Stability settings", new JScrollPane(
				panelStabilitySettings,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		/*
		 * Simulation Settings
		 */
		try {
			panelSimulationSettings = new SettingsPanelSimulation(settings);
			tab.addTab("Simulation settings", new JScrollPane(
					panelSimulationSettings,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		} catch (Exception exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}

		this.add(tab);
		addItemListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#addChangeListener(javax.swing.event
	 * .ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener) {
		if (panelKinSettings != null)
			panelKinSettings.addChangeListener(listener);
		if (panelLatexSettings != null)
			panelLatexSettings.addChangeListener(listener);
		if (panelSimulationSettings != null)
			panelSimulationSettings.addChangeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.squeezer.gui.SettingsPanel#addItemListener(java.awt.event.
	 * ItemListener)
	 */
	public void addItemListener(ItemListener listener) {
		if (panelKinSettings != null)
			panelKinSettings.addItemListener(listener);
		if (panelLatexSettings != null)
			panelDefaultMechanisms.addItemListener(listener);
		if (panelSimulationSettings != null)
			panelSimulationSettings.addItemListener(listener);
	}

	/**
	 * 
	 * @return
	 */
	public Properties getSettings() {
		File f = new File(tfOpenDir.getText());
		if (f.exists() && f.isDirectory())
			settings.put(CfgKeys.OPEN_DIR, tfOpenDir.getText());
		else {
			JOptionPane.showMessageDialog(getTopLevelAncestor(), new JLabel(
					GUITools.toHTML("No such directory " + f.getPath() + '.',
							40)), "Warning", JOptionPane.WARNING_MESSAGE);
			tfOpenDir.setText(settings.get(CfgKeys.OPEN_DIR).toString());
		}
		f = new File(tfSaveDir.getText());
		if (f.exists() && f.isDirectory())
			settings.put(CfgKeys.SAVE_DIR, tfSaveDir.getText());
		else {
			JOptionPane.showMessageDialog(getTopLevelAncestor(), new JLabel(
					GUITools.toHTML("No such directory " + f.getPath() + '.',
							40)), "Warning", JOptionPane.WARNING_MESSAGE);
			tfSaveDir.setText(settings.get(CfgKeys.SAVE_DIR).toString());
		}
		for (Object key : panelKinSettings.getSettings().keySet())
			this.settings.put(key, panelKinSettings.getSettings().get(key));
		for (Object key : panelDefaultMechanisms.getSettings().keySet()) {
			try {
				Class<?> cl = Class.forName(panelDefaultMechanisms
						.getSettings().get(key).toString());
				Set<Class<?>> interfaces = new HashSet<Class<?>>();
				for (Class<?> c : cl.getInterfaces())
					interfaces.add(c);
				this.settings.put(key, cl.getCanonicalName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		for (Object key : panelLatexSettings.getProperties().keySet())
			this.settings.put(key, panelLatexSettings.getProperties().get(key));
		Properties props = panelSimulationSettings.getProperties();
		for (Object key : props.keySet())
			this.settings.put(key, props.get(key));
		return settings;
	}

	public int getSelectedIndex() {
		return tab.getSelectedIndex();
	}

	public void setSelectedIndex(int tab) {
		this.tab.setSelectedIndex(tab);
	}

	public void restoreDefaults() {
		this.settings = SBMLsqueezer.getDefaultSettings();
		removeAll();
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#itemStateChanged(java.awt.event.ItemEvent
	 * )
	 */
	public void itemStateChanged(ItemEvent e) {
		if ((e.getSource() instanceof Container)
				&& (GUITools.contains(panelKinSettings, ((Container) e
						.getSource())))) {
			if (reversibility != ((Boolean) panelKinSettings.getSettings().get(
					CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE)).booleanValue()) {
				reversibility = !reversibility;
				panelDefaultMechanisms.stateChanged(new ChangeEvent(Boolean
						.valueOf(reversibility)));
			}
		}
	}
}
