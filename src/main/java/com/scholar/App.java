package com.scholar;

import com.scholar.service.AuthService;
import com.scholar.store.DataStore;
import com.scholar.ui.LoginFrame;
import com.scholar.ui.Theme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

/**
 * Application entry point.
 *
 * <p>Data is stored in the {@code data} folder relative to the working
 * directory. Use {@code -Dscholar.data.dir=/some/folder} to override it.</p>
 */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(App::start);
    }

    private static void start() {
        try {
            Path dataDirectory = Path.of(System.getProperty("scholar.data.dir", "data"));
            DataStore store = new DataStore(dataDirectory);
            store.initialize();

            System.out.println("Scholar Manager data folder: " + store.directory());
            new LoginFrame(new AuthService(store), store).setVisible(true);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Scholar Manager could not start.\n\n" + ex.getMessage(),
                    "Startup error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
