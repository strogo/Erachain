package gui;

import javax.swing.JDialog;

public class RunMenu extends JDialog {
	// Variables declaration - do not modify                     
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton2;
    public javax.swing.JButton jButton3;
    public javax.swing.JButton jButton4;
    public javax.swing.JButton jButton5;
    public javax.swing.JButton jButton6;
    // End of variables declaration  
	 public RunMenu() {
	        
	        initComponents();
	    }

	 private void initComponents() {
		 
		 setUndecorated(true);
	//		setSize(250, 150);
			setAlwaysOnTop(true);
			setResizable(true);

	        jButton1 = new javax.swing.JButton();
	        jButton2 = new javax.swing.JButton();
	        jButton3 = new javax.swing.JButton();
	        jButton4 = new javax.swing.JButton();
	        jButton5 = new javax.swing.JButton();
	        jButton6 = new javax.swing.JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
	       
	        getContentPane().setLayout(new java.awt.GridLayout(0, 1));

	        jButton1.setText("jButton1");
	        getContentPane().add(jButton1);

	 //       jButton2.setText("jButton2");
	 //       getContentPane().add(jButton2);

	  //      jButton3.setText("jButton3");
	 //       getContentPane().add(jButton3);

	 //       jButton4.setText("jButton4");
	 //       getContentPane().add(jButton4);
	        setVisible(false); 
	     //   pack();
	    }// </




}