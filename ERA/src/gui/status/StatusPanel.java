package gui.status;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import gui.MainFrame;
import gui.PasswordPane;

@SuppressWarnings("serial")
public class StatusPanel extends JPanel 
{
	private StatusPanel th;

	public StatusPanel()
	{
		super();
		th = this;
		
		this.add(new NetworkStatus(), BorderLayout.EAST);
		
		WalletStatus walletStatus = new WalletStatus();
		walletStatus.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) 
			{
				if(e.getClickCount() == 2) 
				{
					PasswordPane.switchLockDialog(MainFrame.getInstance());
			    }
			}
		});
		
		this.add(walletStatus, BorderLayout.EAST);
		this.add(new ForgingStatus(), BorderLayout.EAST);
		
	}
}

