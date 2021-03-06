package test.blocks;

import static org.junit.Assert.*;

 import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.ItemCls;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.item.persons.PersonCls;
import core.item.persons.PersonHuman;
import core.item.statuses.StatusCls;
import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssuePersonRecord;
import core.transaction.IssueAssetTransaction;
import core.transaction.IssuePersonRecord;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import core.wallet.Wallet;
import database.AddressPersonMap;
import database.DBMap;
import database.DBSet;
import database.BlockSignsMap;
import ntp.NTP;
import database.ItemAssetMap;
import database.KKPersonStatusMap;
import database.PersonAddressMap;
import network.message.Message;


public class TestChain {

	private BlockChain blockChain;

	byte[] assetReference = new byte[64];

	static Logger LOGGER = Logger.getLogger(TestChain.class.getName());

	Long releaserReference = null;

	BigDecimal BG_ZERO = BigDecimal.ZERO.setScale(8);
	long ERM_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	//long ALIVE_KEY = StatusCls.ALIVE_KEY;
	byte FEE_POWER = (byte)1;
	byte[] personReference = new byte[64];
	long timestamp = NTP.getTime();
	
	//CREATE EMPTY MEMORY DATABASE
	private DBSet dbSet;
	private GenesisBlock gb;
	Long last_ref;
	boolean asPack = false;
	
	//CREATE KNOWN ACCOUNT
	byte[] seed = Crypto.getInstance().digest("test".getBytes());
	byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
	PrivateKeyAccount maker = new PrivateKeyAccount(privateKey);
    	
	PersonCls personGeneral;
	PersonCls person;
	long personKey = -1;
	IssuePersonRecord issuePersonTransaction;
	R_SertifyPubKeys r_SertifyPubKeys;

	//int version = 0; // without signs of person
	int version = 1; // with signs of person
	
	// INIT PERSONS
	private void init() {
		
		//dbSet = DBSet.createEmptyDatabaseSet();
		dbSet = DBSet.getInstance();

	}

	@Test
	public void onMessage_GetSignatures()
	{
		
		init();

		// CREATE BLOCKCHAIN
		blockChain = Controller.getInstance().getBlockChain();

		Block block = Controller.getInstance().getBlockByHeight(dbSet, 2081);
		byte[] blockSignature = block.getSignature();
		
		// 	test controller.Controller.onMessage(Message) -> GET_SIGNATURES_TYPE
		List<byte[]> headers = blockChain
				.getSignatures(dbSet, blockSignature);
		
		assertEquals(30, headers.size());

		
	}

	
	@Test
	public void orphan_db()
	{
		
		init();

		// GET BLOCKCHAIN
		Controller.getInstance().initBlockChain(dbSet);
		gb = Controller.getInstance().getBlockChain().getGenesisBlock();
		blockChain = Controller.getInstance().getBlockChain();

		Block block = blockChain.getLastBlock(dbSet);
		int height = block.getHeight(dbSet);
		Account creator = block.getCreator();
		int forging = creator.getForgingData(dbSet, height);
		int lastForging = creator.getLastForgingData(dbSet);

		DBSet fork = dbSet.fork();
		
		block.orphan(fork);

		int forging_o = creator.getForgingData(dbSet, height);
		int lastForging_o = creator.getLastForgingData(dbSet);
		int height_0 = block.getHeight(dbSet);

		assertEquals(1, forging);

		
	}

	//
	// TEST WIN_VALUE and TOTAL WEIGHT for CHAIN id DB
	//
	@Test
	public void find_wrong_win_walue_db()
	{
		
		init();

		// GET BLOCKCHAIN
		Controller.getInstance().initBlockChain(dbSet);
		gb = Controller.getInstance().getBlockChain().getGenesisBlock();
		blockChain = Controller.getInstance().getBlockChain();
		BlockSignsMap dbHeight = dbSet.getBlockSignsMap();

		int lastH = 0;
		
		Block block;
		long totalWin = 0l;
		int i = 1;
		while (i <= blockChain.getHeight(dbSet)) {
			block = blockChain.getBlock(dbSet, i);
			int win_value = block.calcWinValueTargeted(dbSet);
			int www = dbHeight.getWeight(block);
			if (www != win_value) {
				//assertEquals(www, win_value);
				int diff = www - win_value;
				i = i + 1 - 1;
				win_value = block.calcWinValueTargeted(dbSet);
				lastH = block.getCreator().getForgingData(dbSet, i);
				int h_i = lastH - 1;
				do {
					lastH = block.getCreator().getForgingData(dbSet, h_i--);
				} while (lastH == -1);
				lastH = block.getCreator().getForgingData(dbSet, i+1);
			}
			totalWin += win_value;
			i++;
		}
		
		long realWeight = blockChain.getFullWeight(dbSet);
		int diff = (int)(realWeight - totalWin);
		assertEquals(0, diff);
		
		assertEquals(realWeight, totalWin);
		
	}

}
