package gui.items.persons;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import lang.Lang;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
//import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.MaskFormatter;
import utils.Pair;
import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.transaction.Transaction;

import gui.transaction.OnDealClick;

@SuppressWarnings("serial")
public class IssuePersonPanel extends JPanel //JDialog //JFrame
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFeePow;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JTextField txtBirthday;
	private JTextField txtDeathday;
	private JLabel iconLabel;
	private JButton iconButton;
	@SuppressWarnings("rawtypes")
	private JComboBox txtGender;
	private JTextField txtRace;
	private JTextField txtBirthLatitude;
	private JTextField txtBirthLongitude;
	private JTextField txtSkinColor;
	private JTextField txtEyeColor;
	private JTextField txtHairСolor;
	private JTextField txtHeight;
	private JButton issueButton;
	
	private byte[] imgButes;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IssuePersonPanel()
	{
//		this.setTitle(Lang.getInstance().translate("DATACHAINS.world") + " - " + Lang.getInstance().translate("Issue Person"));
		
		String colorText ="ff0000"; // цвет текста в форме
		
		//CLOSE
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		/*
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		*/
		
	//	this.setVisible(true);
//		this.setModal(true);
	//	this.setMaximizable(true);
//		this.setTitle(Lang.getInstance().translate("Issue Person"));
	//	this.setClosable(true);
//		this.setResizable(true);
		setPreferredSize(new Dimension(800, 600));
	//	this.setLocation(50, 20);
		
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
//		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.fill = GridBagConstraints.NONE;  
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;	
		cbxGBC.gridx = 1;	
		
		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;		
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;		
		
		int gridy = 0;
		//LABEL FROM
		labelGBC.gridy = gridy;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		txtGBC.gridy = gridy++;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);
        
        //LABEL NAME
      	labelGBC.gridy = gridy;
      	JLabel nameLabel = new JLabel("<HTML><p style=':#" + colorText +"'>" +Lang.getInstance().translate("Name") + ": </p></html>");
      	this.add(nameLabel, labelGBC);
      		
      	//TXT NAME
      	txtGBC.gridy = gridy++;
      	this.txtName = new JTextField();
        this.add(this.txtName, txtGBC);
        
      
        // кнопка загрузки изображения
        labelGBC.gridy = gridy;
        iconButton = new JButton(Lang.getInstance().translate("Add Image..."));
        this.add(iconButton, labelGBC);
        iconButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
			addimage();
				
				
			}
        	
        	
        	
        });
        
        
        //LABEL DESCRIPTION
      	labelGBC.gridy = gridy;
      	labelGBC.gridx = 1;
      	JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
      	this.add(descriptionLabel, labelGBC);
      		
      	//TXTAREA DESCRIPTION
      	txtGBC.gridy = gridy++;
      	txtGBC.gridx = 2;
      	this.txtareaDescription = new JTextArea();
       	
      	this.txtareaDescription.setRows(4);
      	this.txtareaDescription.setLineWrap(true);
      	this.txtareaDescription.setColumns(20);
      //	this.txtareaDescription.setBorder(this.txtName.getBorder());

      	JScrollPane scrollDescription = new JScrollPane(this.txtareaDescription);
     // 	scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    //  	scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(scrollDescription, txtGBC);
      	      	
      	//LABEL GENDER
      	labelGBC.gridy = gridy;
      	labelGBC.gridx =0;
      	JLabel genderLabel = new JLabel(Lang.getInstance().translate("Gender") + ":");
      	this.add(genderLabel, labelGBC);
      	
      	
      	String[] items = {
      			Lang.getInstance().translate("Male"),
      			Lang.getInstance().translate("Female"),
      			Lang.getInstance().translate("-")
        	};	
      	//TXT GENDER
      	txtGBC.gridy = gridy++;
      	txtGBC.gridx=1;
      	//this.txtGender = new JTextField();
      	txtGender = new JComboBox(items);
      	//this.txtGender.setText("1");
        this.add(this.txtGender, txtGBC);
        
        
        
        //LABEL Birthday
      	labelGBC.gridy = gridy;
      	JLabel birthdayLabel = new JLabel(Lang.getInstance().translate("Birthday") + ":");
      	this.add(birthdayLabel, labelGBC);
      		
      	//TXT Birthday
      	txtGBC.gridy = gridy++;
      	this.txtBirthday = new JTextField();
      	// Маска ввода
      	MaskFormatter mf1 = null;
      	try {
			 mf1 = new MaskFormatter("####-##-##");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
      	this.txtBirthday = new JFormattedTextField(mf1); 
      	this.txtBirthday.setText("1970-12-08");
        this.add(this.txtBirthday, txtGBC);
        
        //LABEL DEATHDAY
      	labelGBC.gridy = gridy;
      	this.add(new JLabel(Lang.getInstance().translate("Deathday") + ":"), labelGBC);
      		
      	//TXT DEATHDAY
      	txtGBC.gridy = gridy++;
      	this.txtDeathday = new JTextField();
      	this.txtDeathday = new JFormattedTextField(mf1); 
      	this.txtDeathday.setText("0000-00-00");
        this.add(this.txtDeathday, txtGBC);
        
        //LABEL RACE
      	labelGBC.gridy = gridy;
      	JLabel raceLabel = new JLabel(Lang.getInstance().translate("Race") + ":");
      	this.add(raceLabel, labelGBC);
      		
      	//TXT RACE
      	txtGBC.gridy = gridy++;
      	this.txtRace = new JTextField();
      	this.txtRace.setText("-");
        this.add(this.txtRace, txtGBC);
      	
        //LABEL birthLatitude
      	labelGBC.gridy = gridy;
      	JLabel birthLatitudeLabel = new JLabel(Lang.getInstance().translate("Birth Latitude") + ":");
      	this.add(birthLatitudeLabel, labelGBC);
      		
      	//TXT birthLatitude
      	txtGBC.gridy = gridy++;
      	this.txtBirthLatitude = new JTextField();
      	this.txtBirthLatitude.setText("45.123");
        this.add(this.txtBirthLatitude, txtGBC);
      	
        //LABEL birthLongitude
      	labelGBC.gridy = gridy;
      	JLabel birthLongitudeLabel = new JLabel(Lang.getInstance().translate("Birth Longitude") + ":");
      	this.add(birthLongitudeLabel, labelGBC);
      		
      	//TXT birthLongitude
      	txtGBC.gridy = gridy++;
      	this.txtBirthLongitude = new JTextField();
      	this.txtBirthLongitude.setText("12.123");
        this.add(this.txtBirthLongitude, txtGBC);

        //LABEL skinColor
      	labelGBC.gridy = gridy;
      	JLabel skinColorLabel = new JLabel(Lang.getInstance().translate("Skin Color") + ":");
      	this.add(skinColorLabel, labelGBC);
      		
      	//TXT skinColor
      	txtGBC.gridy = gridy++;
      	this.txtSkinColor = new JTextField();
      	this.txtSkinColor.setText("");
        this.add(this.txtSkinColor, txtGBC);

        //LABEL eyeColor
      	labelGBC.gridy = gridy;
      	JLabel eyeColorLabel = new JLabel(Lang.getInstance().translate("Eye Color") + ":");
      	this.add(eyeColorLabel, labelGBC);
      		
      	//TXT eyeColor
      	txtGBC.gridy = gridy++;
      	this.txtEyeColor = new JTextField();
      	this.txtEyeColor.setText("");
        this.add(this.txtEyeColor, txtGBC);

        //LABEL hairСolor
      	labelGBC.gridy = gridy;
      	JLabel hairСolorLabel = new JLabel(Lang.getInstance().translate("Hair Сolor") + ":");
      	this.add(hairСolorLabel, labelGBC);
      		
      	//TXT hairСolor
      	txtGBC.gridy = gridy++;
      	this.txtHairСolor = new JTextField();
      	this.txtHairСolor.setText("");
        this.add(this.txtHairСolor, txtGBC);

        //LABEL height
      	labelGBC.gridy = gridy;
      	JLabel heightLabel = new JLabel(Lang.getInstance().translate("Height") + ":");
      	this.add(heightLabel, labelGBC);
      		
      	//TXT height
      	txtGBC.gridy = gridy++;
      	this.txtHeight = new JTextField();
      	this.txtHeight.setText("170");
        this.add(this.txtHeight, txtGBC);

        //LABEL FEE POW
      	labelGBC.gridy = gridy;
      	JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = gridy++;
      	this.txtFeePow = new JTextField();
      	this.txtFeePow.setText("0");
        this.add(this.txtFeePow, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = gridy;
        this.issueButton = new JButton(Lang.getInstance().translate("Issue"));
        this.issueButton.setPreferredSize(new Dimension(100, 25));
        this.issueButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
    	this.add(this.issueButton, buttonGBC);
        
        //PACK
	//	this.pack();
    //    this.setResizable(false);
     //   this.setLocationRelativeTo(null);
        this.setVisible(true);
        
        
    
        
        
	}
	
    private static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        is.close();
        return bytes;
    }
	
	protected void addimage() {
		// TODO Auto-generated method stub
		
		
		// открыть диалог для файла
		JFileChooser chooser = new JFileChooser();
		/*
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the JDK.
		final String[][] FILTERS = {{"png", "File (*.png)"},
                {"jpg" , "File(*.jpg)"}};	
		
		
		 for (int i = 0; i < FILTERS[0].length; i++) {
				FileFilterExt eff = new FileFilterExt(FILTERS[i][0], 
						                              FILTERS[i][1]);
				chooser.addChoosableFileFilter(eff);
         }
		*/
		
		 FileNameExtensionFilter filter = new FileNameExtensionFilter(
                 "Image", "png", "jpg");
		 chooser.setFileFilter(filter);
		 
	    int returnVal = chooser.showOpenDialog(getParent());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	    
	//       String a = chooser.getSelectedFile().getName();
	//       String b = chooser.getSelectedFile().getPath();
	//       String c = chooser.getSelectedFile().getAbsolutePath();
	       
	       File file = new File(chooser.getSelectedFile().getPath());
// если размер больше 30к то не вставляем	       
	       if (file.length()>30000) {
	    	   
	    	   JOptionPane.showMessageDialog(null, Lang.getInstance().translate("File Large"), Lang.getInstance().translate("File Large"), JOptionPane.ERROR_MESSAGE);
	    	   
	    	   return;
	       }
	       
// его надо в базу вставлять
	        imgButes = null; 
			try {
				imgButes = getBytesFromFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        InputStream inputStream = new ByteArrayInputStream(imgButes);
	        try {
				BufferedImage image = ImageIO.read(inputStream);
				iconButton.setIcon(new ImageIcon(imgButes));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	       
	       
	       
	    }
		
		// прочитать
		
		// преобразовать в bloom
		
		// вывести на экран в кнопку
		
		
		
		
		
	}

	public void onIssueClick()
	{
		//DISABLE
		this.issueButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.issueButton.setEnabled(true);
			
			return;
		}
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.issueButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();

		int parse = 0;
		int feePow = 0;
		byte gender = 0;
		long birthday = 0;
		long deathday = 0;
		float birthLatitude = 0;
		float birthLongitude = 0;
		int height = 0;
		Blob[] imgBlob;
		try
		{
			
			//READ FEE POW
			feePow = Integer.parseInt(this.txtFeePow.getText());

			//READ GENDER
			parse++;
			gender = (byte) (this.txtGender.getSelectedIndex());
			
			parse++;
			//birthday = Long.parseLong(this.txtBirthday.getText());
			// 1970-08-12 03:05:07
			String str = this.txtBirthday.getText();
			if (str.length() < 11) str = str + " 00:00:00";
			birthday = Timestamp.valueOf(str).getTime();

			parse++;
			str = this.txtDeathday.getText();
			if (str.equals("0000-00-00")) {
				deathday = birthday -1;
			} else {
				if (str.length() < 11) str = str + " 00:00:00";
				deathday = Timestamp.valueOf(str).getTime();
			}

			parse++;
			birthLatitude = Float.parseFloat(this.txtBirthLatitude.getText());
			
			parse++;
			birthLongitude = Float.parseFloat(this.txtBirthLongitude.getText());

			parse++;
			height = Integer.parseInt(this.txtHeight.getText());
			
		}
		catch(Exception e)
		{
			String mess = "Invalid pars... " + parse;
			switch(parse)
			{
			case 0:
				mess = "Invalid fee power 0..6";
				break;
			case 1:
				mess = "Invalid gender";
				break;
			case 2:
				mess = "Invalid birthday [YYYY-MM-DD] or [YYYY-MM-DD hh:mm:ss]";
				break;
			case 3:
				mess = "Invalid deathday [YYYY-MM-DD] or [YYYY-MM-DD hh:mm:ss]";
				break;
			case 4:
				mess = "Invalid birth Latitude -180..180";
				break;
			case 5:
				mess = "Invalid birth Longitude -90..90";
				break;
			case 6:
				mess = "Invalid height 10..255 ";
				break;
			}
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
			this.issueButton.setEnabled(true);
			return;
		}
						
		//CREATE ASSET
		//PrivateKeyAccount creator, String fullName, int feePow, long birthday,
		//byte gender, String race, float birthLatitude, float birthLongitude,
		//String skinColor, String eyeColor, String hairСolor, int height, String description
		PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
		Pair<Transaction, Integer> result = Controller.getInstance().issuePerson(
				creator, this.txtName.getText(), feePow, birthday, deathday,
				gender, this.txtRace.getText(), birthLatitude, birthLongitude,
				this.txtSkinColor.getText(), this.txtEyeColor.getText(),
				this.txtHairСolor.getText(), height,
				null, this.imgButes, this.txtareaDescription.getText()
				);
		
		//CHECK VALIDATE MESSAGE
		if (result.getB() == Transaction.VALIDATE_OK) {
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Person issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
		//	this.dispose();
		
			
			
			txtFeePow.setText("");
			txtName.setText("");
			txtareaDescription.setText("");
			txtBirthday.setText("0000-00-00");
			txtDeathday.setText("0000-00-00");
			
			txtGender.setSelectedIndex(2);
			txtRace.setText("");
			 txtBirthLatitude.setText("");
			 txtBirthLongitude.setText("");
			 txtSkinColor.setText("");
			 txtEyeColor.setText("");
			 txtHairСolor.setText("");
			 txtHeight.setText("");
			 iconButton.setText(Lang.getInstance().translate("Add Image..."));
			
			
			
		
			
			
		} else {		
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result.getB())), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		this.issueButton.setEnabled(true);
	}
}
// Фильтр выбора файлов определенного типа
class FileFilterExt extends javax.swing.filechooser.FileFilter 
{
	String extension  ;  // расширение файла
	String description;  // описание типа файлов

	FileFilterExt(String extension, String descr)
	{
		this.extension = extension;
		this.description = descr;
	}
	@Override
	public boolean accept(java.io.File file)
	{
		if(file != null) {
			if (file.isDirectory())
				return true;
			if( extension == null )
				return (extension.length() == 0);
			return file.getName().endsWith(extension);
		}
		return false;
	}
	// Функция описания типов файлов
	@Override
	public String getDescription() {
		return description;
	}
}
