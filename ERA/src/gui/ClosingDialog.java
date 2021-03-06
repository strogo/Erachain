package gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import controller.Controller;
import lang.Lang;

@SuppressWarnings("serial")
public class ClosingDialog extends JFrame{

	private static final Logger LOGGER = Logger.getLogger(ClosingDialog.class);
	private JDialog waitDialog;
	private AboutFrame about_Frame;
	
	public ClosingDialog()
	{
		try {
			Gui.getInstance().hideMainFrame();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		
		//CREATE WAIT DIALOG
	//	JOptionPane optionPane = new JOptionPane(Lang.getInstance().translate("Saving database. Please wait..."), JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
		about_Frame = AboutFrame.getInstance();
		about_Frame.set_console_Text(Lang.getInstance().translate("Saving database. Please wait..."));
		this.waitDialog =  AboutFrame.getInstance();//new JDialog();
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.waitDialog.setIconImages(icons);
		this.waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);	
		this.waitDialog.setTitle(Lang.getInstance().translate("Closing..."));
		//this.waitDialog.setContentPane(about_Frame);	
		this.waitDialog.setModal(false);
		this.waitDialog.pack();
		this.waitDialog.setLocationRelativeTo(null);
		this.waitDialog.setAlwaysOnTop(true);
		this.waitDialog.setVisible(true);
		
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	Controller.getInstance().deleteObservers();
		    	Controller.getInstance().stopAll();
		    	waitDialog.dispose();
		    	System.exit(0);
		    }
		});
    	
	}
}
