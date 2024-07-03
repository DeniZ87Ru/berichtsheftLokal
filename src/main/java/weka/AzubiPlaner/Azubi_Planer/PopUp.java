package weka.AzubiPlaner.Azubi_Planer;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

import static weka.AzubiPlaner.Azubi_Planer.AzubiPlanerApplication.openBrowser;

public class PopUp {
    private static PopUp instance = null;
    private static boolean isTrayIconAdded = false;
    private static TrayIcon trayIcon;

    private PopUp() {
        initTrayIcon();
    }

    public static void initTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray wird nicht unterstützt");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final SystemTray tray = SystemTray.getSystemTray();

        trayIcon = new TrayIcon(createImage("/static/Weka.png", "tray icon"));
        MenuItem openItem = new MenuItem("Öffnen");
        openItem.addActionListener(e -> openBrowser("http://localhost:8080"));
        popup.insert(openItem, 0); // Fügt den "Öffnen"-Menüpunkt an erster Stelle ein
        MenuItem exitItem = new MenuItem("Beenden");
        exitItem.addActionListener(e -> System.exit(0));
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        boolean isAlreadyAdded = false;
        for (TrayIcon existingTrayIcon : tray.getTrayIcons()) {
            if (existingTrayIcon.getImage().equals(trayIcon.getImage())) {
                isAlreadyAdded = true;
                break;
            }
        }

        if (!isAlreadyAdded) {
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon konnte nicht hinzugefügt werden.");
            }
        }
    }

    protected static Image createImage(String path, String description) {
        URL imageURL = AzubiPlanerApplication.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Ressource nicht gefunden: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
