package kMeans;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class KMeansGUI extends JPanel implements ActionListener {
	
	JPanel options;
	static JTextArea log;
	JPanel buttons;
	
	String[] centerTypes = {"Random Selection", "Random Partition", "Maximin"};
	String[] normTypes = {"None", "Min-Max", "Z-Score"}; 
	
	JLabel file, iter, threshold, cluster, norm, centerType;
	JTextField fileName, iterNum, thresh, clusterNum; 
	JComboBox<String> centers, norms; 
	
	JButton run, close; 
	
	KMeans km;
	
	public KMeansGUI()
	{
		super(new BorderLayout());
		
		//makeLog(); 
	    makeOptions(); 
	    makeButtons(); 
 
	    this.setVisible(true);
	}
	
	public void makeLog()
	{
		log = new JTextArea(5,20);
		log.setMargin(new Insets(5,5,5,5));
	    log.setEditable(false);
	    JScrollPane logScrollPane = new JScrollPane(log);
	    
	    add(logScrollPane, BorderLayout.CENTER);
	}
	
	public void makeButtons()
	{
		buttons = new JPanel(); 
		run = new JButton("Run");
		close = new JButton("Close"); 
		
		run.addActionListener(this);
		close.addActionListener(this);
		
		buttons.add(run);
		buttons.add(close); 
		
		this.add(buttons, BorderLayout.SOUTH); 
	}
	
	public void makeOptions()
	{	
		options = new JPanel(new GridLayout(7, 2)); 
		
		file = new JLabel("File");
		fileName = new JTextField("landsat.txt"); 
		options.add(file);
		options.add(fileName);
		fileName.addActionListener(this);
		
		iter = new JLabel("Iterations");
		iterNum = new JTextField("100"); 
		options.add(iter);
		options.add(iterNum); 
		iterNum.addActionListener(this);
		
		cluster = new JLabel("Clusters");
		clusterNum = new JTextField("6"); 
		options.add(cluster); 
		options.add(clusterNum);
		clusterNum.addActionListener(this);
		
		threshold = new JLabel("Threshold");
		thresh = new JTextField("0.001"); 
		options.add(threshold); 
		options.add(thresh);
		thresh.addActionListener(this);
		
		norm = new JLabel("Normalization Type");
		norms = new JComboBox<String>(normTypes); 
		options.add(norm);
		options.add(norms);
		norms.addActionListener(this);
		
		centerType = new JLabel("Center Selection Type");
		centers = new JComboBox<String>(centerTypes);
		options.add(centerType);
		options.add(centers);
		centers.addActionListener(this);
		
		this.add(options, BorderLayout.NORTH); 
	}
	public void init()
	{
		km = new KMeans(fileName.getText(), Integer.parseInt(clusterNum.getText()), Integer.parseInt(iterNum.getText()), Double.parseDouble(thresh.getText()) , norms.getSelectedItem().toString().toLowerCase(), centers.getSelectedItem().toString().toLowerCase());
	}
	public void runAlgorithm()
	{
		km.run();
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == run)
		{
			init();
			runAlgorithm();
		}
		else if(e.getSource() == close)
			System.exit(0); 
	}
	
	private static void createGUI()
	{
        //Create and set up the window.
        JFrame frame = new JFrame("K Means Clustering");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Add content to the window.
        frame.add(new KMeansGUI());
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}

	public static void main(String[] args) {
		createGUI();

	}

}
