package sp.gui;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import sp.SocksServer;

public class SocksGui {

	public static void main(String[] args) throws Exception {
		SocksServer app = SocksServer.create(args);
		show(app);
		app.run();
	}

	/**
	 * @param args
	 */
	public static void show(SocksServer ss) {

		final TrayIcon trayIcon;

		if (SystemTray.isSupported()) {

			SystemTray tray = SystemTray.getSystemTray();
			URL url = SocksGui.class.getClassLoader().getResource("sp/gui/Spongebob.jpg");
			Image image = Toolkit.getDefaultToolkit().getImage(url);

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Exiting...");
					System.exit(0);
				}
			};

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem("Exit ");
			defaultItem.addActionListener(exitListener);
			popup.add(defaultItem);

			trayIcon = new TrayIcon(image, ss.getBind() + ":" + ss.getPort() + ":" + ss.getCodeRouter(), popup);
			trayIcon.setImageAutoSize(true);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}

		} else {
			System.err.println("Systray not supported");
			// System Tray is not supported

		}

	}

}
