package com.diffusiondata.example.adapter.human;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * GridbagConstraint Builder
 */
public class Constraints {

    private final GridBagConstraints result = new GridBagConstraints();

    private Constraints(int x, int y, int width, int height) {
        this.result.gridx = x;
        this.result.gridy = y;
        this.result.gridwidth = width;
        this.result.gridheight= height;
    }

    public static Constraints at(int x, int y, int width, int height) {
        return new Constraints(x, y, width, height);
    }

    public static Constraints at(int x, int y) {
        return new Constraints(x, y, 1, 1);
    }

    public Constraints anchor(int anchor) {
        this.result.anchor = anchor;
        return this;
    }

    public Constraints fill(int fill) {
        this.result.fill = fill;
        return this;
    }

    public Constraints insets(Insets insets) {
        this.result.insets = insets;
        return this;
    }

    public Constraints weightx(double weight) {
        this.result.weightx = weight;
        return this;
    }

    public GridBagConstraints build() {
        return result;
    }

}
