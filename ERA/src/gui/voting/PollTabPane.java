package gui.voting;

import gui.Gui;
import gui.models.VotesTableModel;
import lang.Lang;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import controller.Controller;
import core.item.assets.AssetCls;
import core.voting.Poll;
import utils.BigDecimalStringComparator;

public class PollTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private PollDetailsPanel pollDetailsPanel;
	private VotesTableModel myVotesTableModel;	
	private VotesTableModel allVotesTableModel;
	
	@SuppressWarnings("unchecked")
	public PollTabPane(Poll poll, AssetCls asset)
	{
		super();
			
		//POLL DETAILS
		this.pollDetailsPanel = new PollDetailsPanel(poll, asset);
		this.addTab(Lang.getInstance().translate("Poll Details"), this.pollDetailsPanel);
		
		//ALL VOTES
		allVotesTableModel = new VotesTableModel(poll.getVotes(), asset);
		final JTable allVotesTable = Gui.createSortableTable(allVotesTableModel, 0);
		
		TableRowSorter<VotesTableModel> sorter =  (TableRowSorter<VotesTableModel>) allVotesTable.getRowSorter();
		sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.addTab(Lang.getInstance().translate("All Votes"), new JScrollPane(allVotesTable));
		
		//MY VOTES
		myVotesTableModel = new VotesTableModel(poll.getVotes(Controller.getInstance().getAccounts()), asset);
		final JTable myVotesTable = Gui.createSortableTable(myVotesTableModel, 0);
		
		sorter = (TableRowSorter<VotesTableModel>) myVotesTable.getRowSorter();
		sorter.setComparator(VotesTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.addTab(Lang.getInstance().translate("My Votes"), new JScrollPane(myVotesTable));
	}

	public void setAsset(AssetCls asset)
	{
		pollDetailsPanel.setAsset(asset);
		allVotesTableModel.setAsset(asset);
		myVotesTableModel.setAsset(asset);
	}
	
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
	}
	
}
