package weka.AzubiPlaner.Azubi_Planer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class AzubiPlanerApplication {
	public static void main(String[] args) {
		PopUp.initTrayIcon();
		SpringApplication.run(AzubiPlanerApplication.class, args);
		openBrowser("http://localhost:8080");

		String userHome = System.getProperty("user.home");
		Path path = Paths.get(userHome, "Dokumente_Azubi_Planer");

		if (!Files.exists(path)) {
			try {
				Files.createDirectory(path);
				System.out.println("Verzeichnis erstellt: " + path);
			} catch (Exception e) {
				System.out.println("Fehler beim Erstellen des Verzeichnisses: " + e.getMessage());
			}
		} else {
			System.out.println("Verzeichnis existiert bereits: " + path);
		}

		Path pdfPath = Paths.get(path.toString(), "PDF_Berichte");

		if (!Files.exists(pdfPath)) {
			try {
				Files.createDirectory(pdfPath);
				System.out.println("Verzeichnis erstellt: " + pdfPath);
			} catch (Exception e) {
				System.out.println("Fehler beim Erstellen des Verzeichnisses: " + e.getMessage());
			}
		} else {
			System.out.println("Verzeichnis existiert bereits: " + pdfPath);
		}
	}
	public static void openBrowser(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new java.net.URI(url));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
			}
		} else {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
