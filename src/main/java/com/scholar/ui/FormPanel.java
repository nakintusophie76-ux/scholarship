package com.scholar.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Simple two-column form layout: each call adds a row of one or two fields.
 */
public final class FormPanel extends JPanel {

    private int row;

    public FormPanel() {
        super(new GridBagLayout());
        setOpaque(false);
    }

    public void addRow(JComponent... fields) {
        if (fields.length == 0) {
            return;
        }
        for (int i = 0; i < fields.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.gridy = row;
            gbc.weightx = 1.0 / fields.length;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(0, i == 0 ? 0 : 10, 14, 0);
            add(fields[i], gbc);
        }
        row++;
    }

    public void addSpacer() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        add(new JPanel(), gbc);
        row++;
    }

    /** Adds a single field spanning both columns. */
    public void addFull(JComponent field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 14, 0);
        add(field, gbc);
        row++;
    }
}
