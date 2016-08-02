/*
Program: Viper is a plug-in for Cytoscape. Its purpose is to generate subnetworks
based on short distance connections integrated with high-throughput expression data.
These focus networks help untangling the hairball usually presented when working with
biological network data.

Author: Marius Garmhausen
Contact: marius@garmhausen.com
Version: 0.1
*/
package org.cytoscape.viPEr;

//Import cytoscape classes
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.*;
import org.cytoscape.viPEr.ui.datamodels.PathfinderCellRenderer;
import org.cytoscape.viPEr.ui.datamodels.PathfinderTableModel;
import org.cytoscape.viPEr.ui.datamodels.ResultEntry;
import org.cytoscape.viPEr.ui.widgets.RangeSlider;
import org.cytoscape.view.model.CyNetworkViewManager;

//import function for hypergeometric testing
import jsc.distributions.Hypergeometric;

//import awt components
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

//Import Swing components
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.GapContent;


//Main wrapper function for Cytoscape apps
public class viPErApp extends AbstractCyApp implements ActionListener{
	
	//Variable initialization
    private int maxcount = 3;
    private int debugCounter = 1;
    private int debugCounterCounter = 0;
    private int unregCount = 1;
    private int popuSize = 0;
    private int diffNodes = 0;
    private String childnetworkindex = "1";
    private CyNode start;
    private CyNode target;
    private String min = "";
    private String max = "";
    private List<List<CyNode>> pfadliste = new ArrayList<List<CyNode>>(1000000);
    private List<ResPath> pathObjList;  
    private List<ResultEntry> entryList;
    private JList resNamesList;
    private JList resList;    
    private Set startProteinList;
    private List<CyNode> startProteinNodeList;
    private Set endProteinList;
    private List<CyNode> endProteinNodeList;
    private HashMap<CyNode, ResNode> nodeObjList; 
    
    private ListSelectionModel listSelectionModel;
    
    private Color rowColors[];
    
    PathfinderTableModel pathfinderModel;
    
    
    //GUI object initialization
    private JButton connectProteinsButton;
    private JButton depthSearchButton;
    private JButton buildComplexButton;
    private JButton selectPathsFromNodesButton;
    private JButton exportResultsTableButton;
    private JButton updateAttributesButton;
    private JButton startProtSelectionButton;
    private JButton endProtSelectionButton;
    private JButton startBatch;
    private JButton scoreComplexButton;
    
    //private JPanel resultsPanel;
    private JPanel resultsPanel;
    private JPanel controlPanel;
    private JPanel parameterSelectionPanel;
    private JPanel loadNetworkPanel;
    private JPanel functionStartPanel;
    private JPanel scoreComplexPanel;
    
    private JLabel stepNumberLabel;
    private JLabel expressionAttributeLabel;
    private JLabel expressionRangeLabel;
    private JLabel loadNetworkLabel;
    
    private RangeSlider rSlider;
   
    private JComboBox nodeAttributeCombox;
    
    private JTextField stepNumberTextfield;
    private JTextField minRangeTextfield;
    private JTextField maxRangeTextfield;
    
    private JTable resultTable;
    
    private final JFileChooser resultFileChooser = new JFileChooser();
    

    //Add new panel in the left container
    public static class MyCytoPanel extends JPanel implements CytoPanelComponent {				
    	public MyCytoPanel() {  		    
  		  }  
    	
    	public CytoPanelName getCytoPanelName() {
  		    return CytoPanelName.WEST;
  		}
 
  		public String getTitle() {
  			return "viPEr";
  		}
  		public Icon getIcon() {
  			return null;
  		}
  		public Component getComponent() {
  			return this;
  		}  
	}
    
	//Add results panel tab in the result panel to the right
    public static class MyResultPanel extends JPanel implements CytoPanelComponent {
    	public MyResultPanel() {
		  }  
    	
    	public CytoPanelName getCytoPanelName() {
  		    return CytoPanelName.EAST;
  		}
 
  		public String getTitle() {
  			return "Result Paths";
  		}
  		public Icon getIcon() {
  			return null;
  		}
  		public Component getComponent() {
  			return this;
  		}  
    }
    
    	
    //Main class
    public viPErApp(CySwingAppAdapter cySwingAppAdapter) {
    	super(cySwingAppAdapter);      
    	
		//create settings panel
    	MyCytoPanel controlPanel = new MyCytoPanel();	
    	
    	java.awt.GridBagConstraints gridBagConstraints;

    	controlPanel.setLayout(new java.awt.GridBagLayout());
    	
    	parameterSelectionPanel = new JPanel();
        parameterSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("2. - Select Parameters"));

        loadNetworkPanel = new JPanel();
        loadNetworkPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("1."));

        functionStartPanel = new JPanel();
        functionStartPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("3. - Start Function"));
  	
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridwidth = 400;
        loadNetworkPanel.setMaximumSize(new java.awt.Dimension(400, 32767));
        loadNetworkPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        loadNetworkPanel.setPreferredSize(new java.awt.Dimension(400, 50));
        controlPanel.add(loadNetworkPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 1;
        parameterSelectionPanel.setMaximumSize(new java.awt.Dimension(400, 32767));
        parameterSelectionPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        parameterSelectionPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        controlPanel.add(parameterSelectionPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 1;
        functionStartPanel.setMaximumSize(new java.awt.Dimension(400, 32767));
        functionStartPanel.setMinimumSize(new java.awt.Dimension(400, 50));
        functionStartPanel.setPreferredSize(new java.awt.Dimension(400, 250));
        controlPanel.add(functionStartPanel, gridBagConstraints);
       
        loadNetworkLabel = new javax.swing.JLabel();

        stepNumberLabel = new javax.swing.JLabel();
        stepNumberTextfield = new javax.swing.JTextField();
        updateAttributesButton = new javax.swing.JButton();
        minRangeTextfield = new javax.swing.JTextField();
        maxRangeTextfield = new javax.swing.JTextField();
        nodeAttributeCombox = new javax.swing.JComboBox();
        expressionAttributeLabel = new javax.swing.JLabel();
        expressionRangeLabel = new javax.swing.JLabel();

        loadNetworkPanel.setLayout(new java.awt.GridBagLayout());

        parameterSelectionPanel.setLayout(new java.awt.GridBagLayout());
        functionStartPanel.setLayout(new java.awt.GridBagLayout());

        loadNetworkLabel.setText("Load Network");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        loadNetworkPanel.add(loadNetworkLabel, gridBagConstraints);

        updateAttributesButton.setText("Update Attributes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        parameterSelectionPanel.add(updateAttributesButton, gridBagConstraints);
        updateAttributesButton.addActionListener(this);

        nodeAttributeCombox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        nodeAttributeCombox.setMaximumSize(new java.awt.Dimension(34, 50));
        parameterSelectionPanel.add(nodeAttributeCombox, gridBagConstraints);
        
               
        //Register panel with gui
    	cySwingAppAdapter.getCyServiceRegistrar().registerService(
    			controlPanel,
    			CytoPanelComponent.class,
    			new Properties()
		);
    
       
        		
        //if combobox entry changes fit the slider to the value range of the new attribute
        nodeAttributeCombox.addActionListener(new ActionListener() {

		//React on user input
        @Override
            public void actionPerformed(ActionEvent ae) {
                Double minimum = null;
                Double maximum = null;

                CyNetwork current_Network = adapter.getCyApplicationManager().getCurrentNetwork();
                List<CyNode> selectedNodes = current_Network.getNodeList();

                List<CyNode> nodeList = current_Network.getNodeList();

                Iterator<CyNode> it = nodeList.iterator();
                while (it.hasNext()) {
                    try{
                        CyNode aNode = (CyNode) it.next();
                        if (minimum == null){                        	
                        	minimum = current_Network.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), double.class);		                          
                        }
                        else if (current_Network.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), double.class) < minimum){
                            minimum = current_Network.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), double.class);
                        }


                        if (maximum == null){
                            maximum = current_Network.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), double.class);
                        }
                        else if (current_Network.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), double.class) > maximum){
                            maximum = current_Network.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), double.class);
                        }
                    }
                    catch(Exception e){

                    }
                }
                if (minimum != null){
                    rSlider.setMinimum((int)Math.round(minimum*10));
                    rSlider.setValue((int)Math.round(minimum*10));
                    minRangeTextfield.setText("" + ((double)rSlider.getValue())/10);
                }
                if (maximum != null){
                    rSlider.setMaximum((int)Math.round(maximum*10));
                    rSlider.setUpperValue((int)Math.round(maximum*10));
                    maxRangeTextfield.setText("" + ((double)rSlider.getUpperValue())/10);
                }
            }
        });

        rSlider = new RangeSlider(-200, 200);
        rSlider.setValue(5);
        rSlider.setUpperValue(20);
        rSlider.setMajorTickSpacing(10);
        rSlider.setPaintTicks(true);
        rSlider.addChangeListener(new ChangeListener() {

		//Set slider values to edit fields
        @Override
        public void stateChanged(ChangeEvent ce) {
            minRangeTextfield.setText("" + ((double)rSlider.getValue())/10);
            maxRangeTextfield.setText("" + ((double)rSlider.getUpperValue())/10);
        }});


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        parameterSelectionPanel.add(rSlider, gridBagConstraints);

        stepNumberLabel.setText("Number of Steps:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        parameterSelectionPanel.add(stepNumberLabel, gridBagConstraints);

        stepNumberTextfield.setText("3");
        stepNumberTextfield.setPreferredSize(new java.awt.Dimension(34, 28));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        parameterSelectionPanel.add(stepNumberTextfield, gridBagConstraints);

        minRangeTextfield.setText("0.5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        minRangeTextfield.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        parameterSelectionPanel.add(minRangeTextfield, gridBagConstraints);
        minRangeTextfield.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            rSlider.setValue(Math.round(Float.parseFloat(minRangeTextfield.getText())*10));
        }});

        maxRangeTextfield.setText("2");
        maxRangeTextfield.setPreferredSize(new java.awt.Dimension(50, 28));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        parameterSelectionPanel.add(maxRangeTextfield, gridBagConstraints);

        maxRangeTextfield.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            rSlider.setUpperValue(Math.round(Float.parseFloat(maxRangeTextfield.getText())*10));
        }});

        expressionAttributeLabel.setText("Expression Attribute:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        parameterSelectionPanel.add(expressionAttributeLabel, gridBagConstraints);

        expressionRangeLabel.setText("Normal expression Range:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        parameterSelectionPanel.add(expressionRangeLabel, gridBagConstraints);

        connectProteinsButton = new JButton("A to B");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        connectProteinsButton.addActionListener(this);
        functionStartPanel.add(connectProteinsButton, gridBagConstraints);

        //Button for in depth search
        depthSearchButton = new JButton("Environment search");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        depthSearchButton.addActionListener(this);
        functionStartPanel.add(depthSearchButton, gridBagConstraints);      
        
        //Button for Selecting a list of start genes
        startProtSelectionButton = new JButton("Select Start Proteinlist");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        startProtSelectionButton.addActionListener(this);
        functionStartPanel.add(startProtSelectionButton, gridBagConstraints);
        
        //Button for Selecting a list of target genes
        endProtSelectionButton = new JButton("Select Target Proteinlist");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        endProtSelectionButton.addActionListener(this);
        functionStartPanel.add(endProtSelectionButton, gridBagConstraints);
        
        
        //Button for Selecting a list of target genes
        startBatch = new JButton("Start connecting in batch");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        startBatch.addActionListener(this);
        functionStartPanel.add(startBatch, gridBagConstraints);
        
        //create and insert "Result Panel" for results tab        	       
        CytoPanel cypan = cySwingAppAdapter.getCySwingApplication().getCytoPanel(CytoPanelName.EAST);
        
        cypan.setState(CytoPanelState.DOCK);
        
        resultsPanel = new MyResultPanel();
        cySwingAppAdapter.getCyServiceRegistrar().registerService(
    			resultsPanel,
    			CytoPanelComponent.class,
    			new Properties()
		);
    }
	
	
    public class MyPanel extends JPanel {
            public MyPanel() {			
            }
    }

	//Recursive function to find all paths of certain length between 2 nodes
    public void rec_path(int count, CyNode node, List<CyNode> origin, CyNetwork G){ 		    
    	origin.add(node);
    	
        if ((count < maxcount) && (!node.equals(target))){
            List<CyNode> nebList = G.getNeighborList(node, CyEdge.Type.valueOf("ANY"));
            Iterator<CyNode> it = nebList.iterator();
            while (it.hasNext()){
                    CyNode aNode = (CyNode) it.next();

                    if(!origin.contains(aNode)){
                            rec_path(count +1, aNode, origin, G);
                        if (debugCounter++ > 100000000){
                            debugCounterCounter += debugCounter;
                            debugCounter = 0;
                            System.out.println(debugCounterCounter);
                    }
                }
            }
        }
        else if (node.equals(target)){
            List<CyNode> dest = new ArrayList<CyNode>(origin);
            pfadliste.add(dest);
        }

        if (origin.size() > 0){
            origin.remove(origin.size()-1);
        }
    }
        
	//Recursive function to search n steps into the network from start node on
    public void rec_depth_path(int count, CyNode node, List<CyNode> origin, CyNetwork G, int uCount){
    	origin.add(node);
        Double value;
        Double originValue;
        
        if (count < maxcount){
            List<CyNode> nebList = G.getNeighborList(node, CyEdge.Type.valueOf("ANY"));
            Iterator<CyNode> it = nebList.iterator();

            while (it.hasNext()){
                CyNode aNode = (CyNode) it.next();

                if(!origin.contains(aNode.getSUID().toString())){
                    if (G.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), Double.class) != null){
                        value = G.getRow(aNode).get(nodeAttributeCombox.getSelectedItem().toString(), Double.class);
                    }
                    else {
                        value = 0.0;
                    }
                    
                    if ((value <= Double.parseDouble(minRangeTextfield.getText())) ||
                            (value >= Double.parseDouble(maxRangeTextfield.getText())) ||
                            (uCount < unregCount)){
                        if ((value > Double.parseDouble(minRangeTextfield.getText())) &&
                            (value < Double.parseDouble(maxRangeTextfield.getText()))){
                            rec_depth_path(count +1, aNode, origin, G, uCount+1);
                        }
                        else {
                            rec_depth_path(count +1, aNode, origin, G, uCount);
                        }
                        
                    }
                }
            }
        }
        originValue = G.getRow(node).get(nodeAttributeCombox.getSelectedItem().toString(), Double.class);
        if (originValue == null) {
        	originValue = 0.0;
        }
        if ((originValue <= Double.parseDouble(minRangeTextfield.getText())) ||
            (originValue >= Double.parseDouble(maxRangeTextfield.getText()))){
            List<CyNode> dest = new ArrayList<CyNode>(origin);
            pfadliste.add(dest);
        }

        if (origin.size() > 0){
            origin.remove(origin.size()-1);
        }	
    }

    public boolean isNullOrEmpty(Object obj) {
        if (obj == null || obj.toString().length() < 1 || obj.toString().equals(""))
            return true;
        return false;
    }

	//Path rating to be implemented here
    public void rate_paths (){

    }
	
    public void actionPerformed(ActionEvent arg0) {   	
        	
        String tempPath = new String("");
        String tempPathNames = new String("");
        DefaultListModel listModel = new DefaultListModel();
        DefaultListModel namesListModel = new DefaultListModel();
        nodeObjList = new HashMap<CyNode, ResNode>(); 
        resList = new JList(listModel);
        resNamesList = new JList(listModel);
        int pos;
        popuSize = adapter.getCyApplicationManager().getCurrentNetwork().getNodeCount();
        CyNetwork cur_Network = adapter.getCyApplicationManager().getCurrentNetwork();
        diffNodes = 0;
        
        List<CyNode> li = adapter.getCyApplicationManager().getCurrentNetwork().getNodeList();
        for (CyNode i : li){
        	try{
	            if (!nodeAttributeCombox.getSelectedItem().toString().isEmpty()){
	                    if (cur_Network.getRow(i).get(nodeAttributeCombox.getSelectedItem().toString(), Double.class) < Double.parseDouble(minRangeTextfield.getText()) || 
	                                (cur_Network.getRow(i).get(nodeAttributeCombox.getSelectedItem().toString(), Double.class) > Double.parseDouble(maxRangeTextfield.getText()))){
	                    	diffNodes++;
	                    }
	                }
        	}
        	catch(Exception e){
        		        	
        	}
        }
        
        //When startbutton has been clicked
        if(arg0.getSource().equals(this.connectProteinsButton)){
            List<CyNode> subNetworkNodes = new ArrayList<CyNode>();

            pathObjList = new ArrayList<ResPath>();

            this.pfadliste.clear();

            this.maxcount = Integer.parseInt(stepNumberTextfield.getText());
            
            CyNetwork current_Network = adapter.getCyApplicationManager().getCurrentNetwork();

            
            //CyAttributes cyAttrList = Cytoscape.getNodeAttributes();
            if (current_Network != null) {
                List<CyNode> selectedNodes = CyTableUtil.getNodesInState(current_Network, "selected", true);

                if (selectedNodes.size() == 0) {
                        JOptionPane.showMessageDialog(null,"Nothing is selected!");
                }
                else if(selectedNodes.size() == 2) {
                        Iterator<CyNode> it = selectedNodes.iterator();

                        this.start = it.next();
                        this.target = it.next();
                        
                        this.rec_path(0, this.start, new ArrayList<CyNode>(), current_Network);
                }
                else {
                            JOptionPane.showMessageDialog(null,"Please select only 2 Nodes!");
                    }

                Iterator<List<CyNode>> ilist = pfadliste.iterator();

                while (ilist.hasNext()){
                    List<CyNode> strlist = ilist.next();
                    pathObjList.add(new ResPath(pathObjList.size()));

                    Iterator<CyNode> istring = strlist.iterator();

                    while (istring.hasNext()){
                        CyNode tempstring = istring.next();

                        //create String output for result list
                        if (!tempPath.isEmpty()) {
                                tempPath = tempPath + "," + tempstring.toString();
                        }
                        else {
                                tempPath = tempstring.toString();
                        }
                        
                        try{
                            //create String with names output for result list
                            if (!tempPathNames.isEmpty()) {                                   
                                    tempPathNames = tempPathNames + "," + current_Network.getRow(tempstring).get("name", String.class);
                            }
                            else {
                                    tempPathNames = current_Network.getRow(tempstring).get("name", String.class);
                            }
                        }
                        catch (Exception e){
                            System.out.println(e);
                        }
                        
                        //create nodeobjects in paths for scoring
                        //If Node does not exist yet create it
                        if (!nodeObjList.containsKey(tempstring)){
                                nodeObjList.put(tempstring, new ResNode(tempstring));
                        }

                        //store actual nodeidentifier to belong to the path
                        pathObjList.get(pathObjList.size()-1).nodeList.add(tempstring);

                        
                        if (!subNetworkNodes.contains(tempstring)){
                                subNetworkNodes.add(tempstring);
                        }
                    }
                    pos = resList.getModel().getSize();
                    listModel.add(pos, tempPath);
                    namesListModel.add(pos, tempPathNames);
                    tempPathNames = "";
                    tempPath = "";
                }
                
                CyRootNetworkManager rootmgr = adapter.getCyRootNetworkManager();
                
                CyRootNetwork current_root_Network = rootmgr.getRootNetwork(adapter.getCyApplicationManager().getCurrentNetwork());
                
                
                List<CyNode> nodeArray = subNetworkNodes;
                List<CyEdge> edgeArray = new ArrayList<CyEdge>();
                for (CyNode node : nodeArray){
                	for (CyNode node2 : nodeArray){
                		List<CyEdge> tempEdgeArray = current_root_Network.getConnectingEdgeList(node, node2, CyEdge.Type.valueOf("ANY"));
                		
                		for (CyEdge edge : tempEdgeArray){
                    		edgeArray.add(edge);
                    	}
                	}
                }                              

                CySubNetwork newNet = current_root_Network.addSubNetwork(nodeArray, edgeArray);          
                newNet.getRow(newNet).set(CyNetwork.NAME, "Child"+ childnetworkindex);
                
                adapter.getCyNetworkManager().addNetwork(newNet);                                               

                int tempindex = Integer.parseInt(childnetworkindex);
                tempindex++;
                childnetworkindex = "" + tempindex;

            }
            else {
                    JOptionPane.showMessageDialog(null,"There is no current Network");
            }

            String[][] data = new String[pathObjList.size()][3];

            //include pathlength is calculation
            for (int i=0;i<pathObjList.size(); i++){                    
            		data[i][0] = String.valueOf(pathObjList.get(i).calculate_geometric(Double.parseDouble(minRangeTextfield.getText()), Double.parseDouble(maxRangeTextfield.getText()), cur_Network));
                    data[i][1] = listModel.elementAt(i).toString();
                    data[i][2] = namesListModel.elementAt(i).toString();
                    System.out.println(listModel.elementAt(i).toString());
            }

            pathfinderModel = new PathfinderTableModel();

            resultTable = new JTable(pathfinderModel);
            resultTable.setColumnModel(createColumnModel());
            resultTable.setAutoCreateRowSorter(true);
            resultTable.setRowHeight(26);
            resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
            resultTable.setSelectionMode(listSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            resultTable.setIntercellSpacing(new Dimension(0,0));

            Dimension viewSize = new Dimension();
            viewSize.width = resultTable.getColumnModel().getTotalColumnWidth();
            viewSize.height = 10 * resultTable.getRowHeight();
            resultTable.setPreferredScrollableViewportSize(viewSize);

            JTableHeader header = resultTable.getTableHeader();
            header.setPreferredSize(new Dimension(30, 26));
            TableCellRenderer headerRenderer = header.getDefaultRenderer();
            if (headerRenderer instanceof JLabel) {
                ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
            }

            resultsPanel.setLayout(new BoxLayout(resultsPanel,BoxLayout.PAGE_AXIS));
            resultsPanel.removeAll();

            JScrollPane listScroller = new JScrollPane(resultTable);
            listScroller.setPreferredSize(new Dimension(250, 80));

            resultsPanel.add(listScroller);
            listScroller.setVisible(true);

            resultTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                        CyNetwork net = adapter.getCyApplicationManager().getCurrentNetwork();
                        
                        List<CyNode> nodelist = net.getNodeList();
                        for (CyNode node : nodelist){
                        	net.getRow(node).set("selected", false);
                        }
                        
                        int [] viewRows = resultTable.getSelectedRows();
                        int [] realRows = new int [viewRows.length];
                        for (int i = 0; i < viewRows.length;i++){
                            realRows[i] = resultTable.convertRowIndexToModel(viewRows[i]);
                        }

                        if (realRows.length != 0){
                            for (int i = 0; i < realRows.length; i++) {
                                    for (int j=0;j<pathObjList.get(realRows[i]).nodeList.size();j++){
                                    	net.getRow(pathObjList.get(realRows[i]).nodeList.get(j)).set("selected", true);
                                    }
                            }
                        }
                        CyEventHelper eventhelper = adapter.getCyEventHelper();
                        eventhelper.flushPayloadEvents();
                    }
                }
            );


            entryList = new ArrayList<ResultEntry>();
            ResultEntry resEnt;
            for (int i = 0; i < data.length; i++){
                resEnt = new ResultEntry(Float.parseFloat(data[i][0]), data[i][1], data[i][1].replaceAll("[^,]","").length(), data[i][2]);
                entryList.add(resEnt);
            }     

            pathfinderModel.add(entryList);
            
        }

        //Update combobox to include all actual node attributes of the network
        if(arg0.getSource().equals(this.updateAttributesButton)){
        	System.out.println("Update Attributes Action");
        	CyNetwork current_Network = adapter.getCyApplicationManager().getCurrentNetwork();
            Collection<CyColumn> networkAttr =  current_Network.getDefaultNodeTable().getColumns();
            CyColumn[] networkAttrNames = networkAttr.toArray(new CyColumn[networkAttr.size()]);
            String tempName = new String();
            nodeAttributeCombox.removeAllItems();
            
            for (CyColumn attr : networkAttr){
               
            	System.out.println(attr.toString());
                if(attr.toString().length()>20){
                        tempName = attr.toString().substring(0, 19);
                }
                else {
                    tempName = attr.toString();
                }
                
                nodeAttributeCombox.addItem(tempName);
            }
        }
        

        if(arg0.getSource().equals(this.depthSearchButton)){
            this.pfadliste.clear();
            List<CyNode> subNetworkNodes = new ArrayList<CyNode>();

            this.maxcount = Integer.parseInt(stepNumberTextfield.getText());

            CyNetwork current_Network = adapter.getCyApplicationManager().getCurrentNetwork();
            pathObjList = new ArrayList<ResPath>();
            if (current_Network != null) {
                List<CyNode> selectedNodes = CyTableUtil.getNodesInState(current_Network, "selected", true);

                if (selectedNodes.size() == 0) {
                        JOptionPane.showMessageDialog(null,"Nothing is selected!");
                }
                else if(selectedNodes.size() == 1) {
                        Iterator<CyNode> it = selectedNodes.iterator();
                        this.start = it.next();
                        this.rec_depth_path(0, this.start, new ArrayList<CyNode>(), current_Network,0);
                }
                else if(selectedNodes.size() > 1) {
                        JOptionPane.showMessageDialog(null,"Function only available for single selected nodes!");
                }

                Iterator<List<CyNode>> ilist = pfadliste.iterator();

                while (ilist.hasNext()){
                    List<CyNode> strlist = ilist.next();
                    pathObjList.add(new ResPath(pathObjList.size()));

                    Iterator<CyNode> istring = strlist.iterator();

                    while (istring.hasNext()){
                        CyNode tempstring = istring.next();

                        //create String output for result list
                        if (!tempPath.isEmpty()) {
                                tempPath = tempPath + "," + current_Network.getRow(tempstring).get("name", String.class);						
                        }
                        else {
                                tempPath = current_Network.getRow(tempstring).get("name", String.class);;
                        }

                        //create nodeobjects in paths for scoring
                        //If Node does not exist yet create it
                        if (!nodeObjList.containsKey(tempstring)){
                                nodeObjList.put(tempstring, new ResNode(tempstring));
                        }						
                        //store actual nodeidentifier to belong to the path						
                        pathObjList.get(pathObjList.size()-1).nodeList.add(tempstring);


                        if (!subNetworkNodes.contains(tempstring)){
                                subNetworkNodes.add(tempstring);
                        }														
                    }				
                    pos = resList.getModel().getSize();
                    listModel.add(pos, tempPath);
                    tempPath = "";
                }
                
                CyRootNetworkManager rootmgr = adapter.getCyRootNetworkManager();
                
                CyRootNetwork current_root_Network = rootmgr.getRootNetwork(adapter.getCyApplicationManager().getCurrentNetwork());
                
                
                List<CyNode> nodeArray = subNetworkNodes;
                List<CyEdge> edgeArray = new ArrayList<CyEdge>();
                for (CyNode node : nodeArray){
                	for (CyNode node2 : nodeArray){
                		List<CyEdge> tempEdgeArray = current_root_Network.getConnectingEdgeList(node, node2, CyEdge.Type.valueOf("ANY"));
                		
                		for (CyEdge edge : tempEdgeArray){
                    		edgeArray.add(edge);
                    	}
                	}
                }                              

                CySubNetwork newNet = current_root_Network.addSubNetwork(nodeArray, edgeArray);          
                newNet.getRow(newNet).set(CyNetwork.NAME, "Child"+ childnetworkindex);
                
                adapter.getCyNetworkManager().addNetwork(newNet);      
                
                int tempindex = Integer.parseInt(childnetworkindex);
                tempindex++;
                childnetworkindex = "" + tempindex;
            }
            else {
                    JOptionPane.showMessageDialog(null,"There is no current Network");                    
            }
        }

        //Select Startlist of proteins for batchprcessing
        if(arg0.getSource().equals(this.startProtSelectionButton)){
            CyNetwork current_Network1 = adapter.getCyApplicationManager().getCurrentNetwork();            
            if (current_Network1 != null) {
            	startProteinNodeList = new ArrayList<CyNode>(CyTableUtil.getNodesInState(current_Network1, "selected", true).size());
            	startProteinNodeList = CyTableUtil.getNodesInState(current_Network1, "selected", true);
            }
        }
        
        //Select Targetlist of proteins for batchprocessing
        if(arg0.getSource().equals(this.endProtSelectionButton)){
            CyNetwork current_Network2 = adapter.getCyApplicationManager().getCurrentNetwork();            
            if (current_Network2 != null) {
            	endProteinNodeList = new ArrayList<CyNode>(CyTableUtil.getNodesInState(current_Network2, "selected", true).size());
            	endProteinNodeList = CyTableUtil.getNodesInState(current_Network2, "selected", true);
            }
        }
        
        //Select Targetlist of proteins for batchprocessing
        if(arg0.getSource().equals(this.startBatch)){
            
            List<CyNode> subNetworkNodes = new ArrayList<CyNode>();

            pathObjList = new ArrayList<ResPath>();

            this.maxcount = Integer.parseInt(stepNumberTextfield.getText());
            
            CyNetwork current_Network = adapter.getCyApplicationManager().getCurrentNetwork();

            if (current_Network != null) {
                
                if (startProteinNodeList.isEmpty() || endProteinNodeList.isEmpty()) {
                        JOptionPane.showMessageDialog(null,"Please select start and target proteins!");
                }                
                else {
                    
                    Iterator<CyNode> it = startProteinNodeList.iterator();
                    
                    
                    while (it.hasNext()){
                        this.start = it.next();
                        Iterator<CyNode> itTargets = endProteinNodeList.iterator();
                        
                        while (itTargets.hasNext()){
                            this.target = itTargets.next();
                            this.pfadliste.clear();
                            this.rec_path(0, this.start, new ArrayList<CyNode>(), current_Network);
                            
                            Iterator<List<CyNode>> ilist = pfadliste.iterator();

                            while (ilist.hasNext()){
                                List<CyNode> strlist = ilist.next();

                                Iterator<CyNode> istring = strlist.iterator();

                                while (istring.hasNext()){
                                    CyNode tempstring = istring.next();

                                    if (!subNetworkNodes.contains(tempstring)) {
                                        subNetworkNodes.add(tempstring);
                                    }

                                }
                                tempPathNames = "";
                                tempPath = "";
                            }
                            
                            
                        }
                    }
                }
                
                
                CyRootNetworkManager rootmgr = adapter.getCyRootNetworkManager();
                
                CyRootNetwork current_root_Network = rootmgr.getRootNetwork(adapter.getCyApplicationManager().getCurrentNetwork());
                
                
                List<CyNode> nodeArray = subNetworkNodes;
                List<CyEdge> edgeArray = new ArrayList<CyEdge>();
                for (CyNode node : nodeArray){
                	for (CyNode node2 : nodeArray){
                		List<CyEdge> tempEdgeArray = current_root_Network.getConnectingEdgeList(node, node2, CyEdge.Type.valueOf("ANY"));
                		
                		for (CyEdge edge : tempEdgeArray){
                    		edgeArray.add(edge);
                    	}
                	}
                }                              

                CySubNetwork newNet = current_root_Network.addSubNetwork(nodeArray, edgeArray);          
                newNet.getRow(newNet).set(CyNetwork.NAME, "Child"+ childnetworkindex);
                
                adapter.getCyNetworkManager().addNetwork(newNet);       

                int tempindex = Integer.parseInt(childnetworkindex);
                tempindex++;
                childnetworkindex = "" + tempindex;
            }
            else {
                    JOptionPane.showMessageDialog(null,"There is no current Network");
            }
        }
    }

	//Result node class
	public class ResNode {
            public CyNode nodeId;
            private HashMap<String, Object> hm;

            public ResNode(CyNode tempstring){
                    hm = new HashMap<String, Object>();
                    this.nodeId = tempstring;
            }

            public void addAtt(String name, Object obj) {
                    hm.put(name, obj);
            }

            public boolean hasAtt(String name){
                    return hm.containsKey(name);
            }
	}
	
	//Result path consisting of result nodes
	public class ResPath {
            public int pathId;
            public double rating;
            public List<CyNode> nodeList;
            private HashMap<String, Object> pathAtt;
            private HashMap<String, HashMap<String, Object>> nodeAttHashMap;

            public ResPath(int id){
                    rating = 0;
                    nodeList = new ArrayList<CyNode>();
                    pathAtt = new HashMap<String, Object>();
                    nodeAttHashMap = new HashMap<String, HashMap<String, Object>>();
            }

            public String getAtt(String attrName){
                    return pathAtt.get(attrName).toString();
            }

            public String getNodeAtt(String nodeId, String nodeAttrName){
                    return nodeAttHashMap.get(nodeId).get(nodeAttrName).toString();
            }

            public List<CyNode> getNodeList(){
                return nodeList;
            }

            public void setAtt(String attrName, Object obj){
                    pathAtt.put(attrName, obj);
            }

            public void setNodeAtt(String nodeId, String attrName, Object obj){
                    HashMap<String, Object> tempHash = new HashMap<String, Object>();
                    tempHash.put(attrName, obj);
                    nodeAttHashMap.put(nodeId, tempHash);
            }
            
			//Perform fisher test
            public double calculate_geometric(double lowerBound, double upperBound, CyNetwork current_Network){
            	int diffExpLvl = 0;
            	int pathLength = nodeList.size();            
            	CyNode aktNode = null;
            	
            	try{
	                  Iterator <CyNode> it = nodeList.iterator();
	                  while (it.hasNext()){
	                      aktNode = it.next();
	                      if (!nodeAttributeCombox.getSelectedItem().toString().isEmpty()){
	                              if (((current_Network.getRow(aktNode).get(nodeAttributeCombox.getSelectedItem().toString(), Double.class) < lowerBound) || 
	                                          (current_Network.getRow(aktNode).get(nodeAttributeCombox.getSelectedItem().toString(), Double.class) > upperBound))){
	                                      diffExpLvl++;
	                              }
	                          }
	                  }
	              }
	              catch(NullPointerException e){
	                  System.out.println(e);
	              }
            	
            	Hypergeometric hypeScore = new Hypergeometric(pathLength, popuSize, diffNodes);
            	
            	return hypeScore.upperTailProb(diffExpLvl);
            }		
	}
	private Object makeObj(final String item)  {
		return new Object() {public String toString() {return item;}};
	}
        
        private Color[] getTableRowColors(){
            if (rowColors == null) {
                rowColors = new Color[2];
                rowColors[0] = UIManager.getColor("table.background");
                //
                rowColors[1] = new Color(200,34,200);
            }
            return rowColors;
        }
        
		//Result table model
        protected TableColumnModel createColumnModel(){
            DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
            
            TableCellRenderer cellRenderer = new PathfinderCellRenderer.RowRenderer(getTableRowColors());

            TableColumn column = new TableColumn();
            column.setModelIndex(PathfinderTableModel.PATH_LENGTH);
            column.setHeaderValue("PathLength");
            column.setPreferredWidth(26);
            column.setCellRenderer(new PathfinderCellRenderer.RowRenderer(getTableRowColors()));
            columnModel.addColumn(column);
            
            column = new TableColumn();
            column.setModelIndex(PathfinderTableModel.SCORE_COLUMN);
            column.setHeaderValue("p-value");
            column.setPreferredWidth(26);
            column.setCellRenderer(new PathfinderCellRenderer.RowRenderer(getTableRowColors()));
            columnModel.addColumn(column);

            column = new TableColumn();
            column.setModelIndex(PathfinderTableModel.PATH_COLUMN);
            column.setHeaderValue("Path");
            column.setPreferredWidth(26);
            column.setCellRenderer(new PathfinderCellRenderer.RowRenderer(getTableRowColors()));
            columnModel.addColumn(column);
            
            column = new TableColumn();
            column.setModelIndex(PathfinderTableModel.PATH_WITH_NAMES);
            column.setHeaderValue("PathSymbols");
            column.setPreferredWidth(26);
            column.setCellRenderer(new PathfinderCellRenderer.RowRenderer(getTableRowColors()));
            columnModel.addColumn(column);
            
            return columnModel;
        }
        
		//CSV export function
        private static void exportTableDataToCSV(JTable table, File file) {
            try {
                file.createNewFile();

                BufferedWriter bw = new BufferedWriter(new FileWriter(file));

                TableModel model = table.getModel();
                for (int h = 0 ; h < model.getColumnCount();h++){
                    bw.write(model.getColumnName(h).toString());
                    if (h+1 != model.getColumnCount())
                    bw.write(";");
                }
                bw.newLine();

                for (int clmCnt = model.getColumnCount(), rowCnt = model
                                .getRowCount(), i = 0; i < rowCnt; i++) {
                        for (int j = 0; j < clmCnt; j++) {
                                if (model.getValueAt(i, j) != null){
                                    String value = model.getValueAt(i, j).toString();
                                    bw.write(value);
                                }
                                if(j+1 != clmCnt)
                                    bw.write(";");
                        }
                        bw.newLine();
                }

                bw.flush();
                bw.close();
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
    }  

