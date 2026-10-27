package com.scholar.ui;

/**
 * Implemented by pages that need to reload their data when shown or after a
 * change elsewhere in the application.
 */
public interface Refreshable {

    void refresh();
}
