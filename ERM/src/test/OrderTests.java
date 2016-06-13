package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

import ntp.NTP;

import org.junit.Assert;
import org.junit.Test;

import core.account.PrivateKeyAccount;
import core.block.GenesisBlock;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import core.item.assets.AssetVenture;
import core.item.assets.Order;
import core.item.assets.Trade;
import core.transaction.CancelOrderTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.IssueAssetTransaction;
import core.transaction.Transaction;
import core.transaction.TransactionFactory;
import database.DBSet;

public class OrderTests 
{
	Long releaserReference = null;
	long ERMO_KEY = Transaction.RIGHTS_KEY;
	long FEE_KEY = Transaction.FEE_KEY;
	byte FEE_POWER = (byte)0;
	byte[] assetReference = new byte[64];
	long timestamp = NTP.getTime();

	DBSet db = DBSet.createEmptyDatabaseSet();
	private GenesisBlock gb = new GenesisBlock();


	//CREATE KNOWN ACCOUNT
	PrivateKeyAccount accountA;	
	PrivateKeyAccount accountB;

	IssueAssetTransaction issueAssetTransaction;
	AssetCls assetA;
	long keyA;
	AssetCls assetB;
	long keyB;
	CreateOrderTransaction orderCreation;
	
	private void init() {
						
		gb.process(db);

		byte[] seed = Crypto.getInstance().digest("test_A".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		accountA = new PrivateKeyAccount(privateKey);
		seed = Crypto.getInstance().digest("test_B".getBytes());
		privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		accountB = new PrivateKeyAccount(privateKey);
		
		// FEE FUND
		accountA.setLastReference(gb.getTimestamp(), db);
		accountA.setConfirmedBalance(ERMO_KEY, BigDecimal.valueOf(100).setScale(8), db);
		accountA.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(10).setScale(8), db);

		accountB.setLastReference(gb.getTimestamp(), db);
		accountB.setConfirmedBalance(ERMO_KEY, BigDecimal.valueOf(100).setScale(8), db);
		accountB.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(10).setScale(8), db);

    	assetA = new AssetVenture(new GenesisBlock().getGenerator(), "AAA", ".", 50000L, (byte)2, true);
		issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
    	keyA = issueAssetTransaction.getAssetKey(db);

    	assetB = new AssetVenture(new GenesisBlock().getGenerator(), "BBB", ".", 50000L, (byte)2, true);
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte)0, System.currentTimeMillis(), accountB.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
    	keyB = issueAssetTransaction.getAssetKey(db);

		//CREATE ORDER TRANSACTION
		orderCreation = new CreateOrderTransaction(accountA, keyA, 3l,
				BigDecimal.valueOf(10).setScale(8), BigDecimal.valueOf(100).setScale(8), (byte)0, timestamp, accountA.getLastReference(db));


	}
	@Test
	public void validateSignatureOrderTransaction() 
	{
		
		init();
		
		orderCreation.sign(accountA, false);
		
		//CHECK IF ORDER CREATION SIGNATURE IS VALID
		assertEquals(true, orderCreation.isSignatureValid());
		
		//INVALID SIGNATURE
		orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, 3l, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timestamp, accountA.getLastReference(db), new byte[64]);
		
		//CHECK IF ORDER CREATION SIGNATURE IS INVALID
		assertEquals(false, orderCreation.isSignatureValid());
	}
	
	@Test
	public void validateCreateOrderTransaction() 
	{
		
		init();
						
		//CHECK VALID
		long timeStamp = System.currentTimeMillis();
		CreateOrderTransaction orderCreation = new CreateOrderTransaction(accountA, keyA, AssetCls.ERMO_KEY, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db));		
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		
		//CREATE INVALID ORDER CREATION HAVE EQUALS WANT
		orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, AssetCls.FEE_KEY, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db));		
			
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.HAVE_EQUALS_WANT, orderCreation.isValid(db, releaserReference));
		
		//CREATE INVALID ORDER CREATION NOT ENOUGH BALANCE
		orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, AssetCls.ERMO_KEY, BigDecimal.valueOf(50001).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db));		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.NO_BALANCE, orderCreation.isValid(db, releaserReference));
		
		//CREATE INVALID ORDER CREATION INVALID AMOUNT
		orderCreation = new CreateOrderTransaction(accountA, keyA, AssetCls.ERMO_KEY, BigDecimal.valueOf(-50.0).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db));		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.NEGATIVE_AMOUNT, orderCreation.isValid(db, releaserReference));
		
    	assetA = new AssetVenture(new GenesisBlock().getGenerator(), "DATACHAINS.world", "This is the simulated ERM asset.", 100000L, (byte)0, false);
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		issueAssetTransaction.process(db, false);
    	keyA = assetA.getKey(db);

		//CREATE INVALID ORDER CREATION INVALID AMOUNT
		orderCreation = new CreateOrderTransaction(accountA, keyA, AssetCls.ERMO_KEY, BigDecimal.valueOf(50.01).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db));		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.INVALID_AMOUNT, orderCreation.isValid(db, releaserReference));

		//CREATE INVALID ORDER CREATION INVALID AMOUNT
		orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, keyA, 
				BigDecimal.valueOf(0.01).setScale(8), BigDecimal.valueOf(1.1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db), new byte[64]);
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.INVALID_RETURN, orderCreation.isValid(db, releaserReference));

		//CREATE INVALID ORDER CREATION WANT DOES NOT EXIST
		orderCreation = new CreateOrderTransaction(accountA, 111l, AssetCls.ERMO_KEY, BigDecimal.valueOf(0.1).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db), new byte[64]);		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.ASSET_DOES_NOT_EXIST, orderCreation.isValid(db, releaserReference));

		//CREATE INVALID ORDER CREATION WANT DOES NOT EXIST
		orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, 114l, BigDecimal.valueOf(0.1).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, accountA.getLastReference(db), new byte[64]);		
					
		//CHECK IF ORDER CREATION INVALID
		assertEquals(Transaction.ASSET_DOES_NOT_EXIST, orderCreation.isValid(db, releaserReference));
		
		//CREATE ORDER CREATION INVALID REFERENCE
		orderCreation = new CreateOrderTransaction(accountA, AssetCls.FEE_KEY, AssetCls.ERMO_KEY, BigDecimal.valueOf(0.1).setScale(8), BigDecimal.valueOf(1).setScale(8), (byte)0, timeStamp, -12345L, new byte[64]);		
			
		//CHECK IF  ORDER CREATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, orderCreation.isValid(db, releaserReference));
										
	}
	
	@Test
	public void parseCreateOrderTransaction() 
	{
		
		init();
		
		orderCreation.sign(accountA, false);
		
		//CONVERT TO BYTES
		byte[] rawOrderCreation = orderCreation.toBytes(true, null);
		assertEquals(rawOrderCreation.length, orderCreation.getDataLength(false));
		
		CreateOrderTransaction parsedOrderCreation = null;
		try 
		{	
			//PARSE FROM BYTES
			parsedOrderCreation = (CreateOrderTransaction) TransactionFactory.getInstance().parse(rawOrderCreation, releaserReference);
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction. " + e);
		}
			
		//CHECK INSTANCE
		assertEquals(true, parsedOrderCreation instanceof CreateOrderTransaction);
		
		//CHECK SIGNATURE
		assertEquals(true, Arrays.equals(orderCreation.getSignature(), parsedOrderCreation.getSignature()));
		
		//CHECK HAVE
		assertEquals(orderCreation.getOrder().getHave(), parsedOrderCreation.getOrder().getHave());	
		
		//CHECK WANT
		assertEquals(orderCreation.getOrder().getWant(), parsedOrderCreation.getOrder().getWant());	
			
		//CHECK AMOUNT
		assertEquals(0, orderCreation.getOrder().getAmountHave().compareTo(parsedOrderCreation.getOrder().getAmountHave()));	
		
		//CHECK PRICE
		assertEquals(0, orderCreation.getOrder().getAmountWant().compareTo(parsedOrderCreation.getOrder().getAmountWant()));
		
		//CHECK FEE
		assertEquals(orderCreation.getFee(), parsedOrderCreation.getFee());	
		
		//CHECK REFERENCE
		assertEquals((long)orderCreation.getReference(), (long)parsedOrderCreation.getReference());	
		
		//CHECK TIMESTAMP
		assertEquals(orderCreation.getTimestamp(), parsedOrderCreation.getTimestamp());				
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawOrderCreation = new byte[orderCreation.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawOrderCreation, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
		
		//////////////////////////////////
		/////////// TRADE PARSE //////////
		Trade tradeParse = new Trade(BigInteger.TEN, BigInteger.ONE,
				BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(10).setScale(8), timestamp);
		byte[] tradeRaw = tradeParse.toBytes();
		Assert.assertEquals(tradeRaw.length, tradeParse.getDataLength());
		Trade tradeParse_1 = null;
		try {
			tradeParse_1 = Trade.parse(tradeRaw);
		} catch (Exception e) {
			
		}
		Assert.assertEquals(tradeParse_1.getTimestamp(), tradeParse.getTimestamp());


	}
	
	@Test
	public void testOrderProcessingDivisible()
	{
		
		init();

		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		// amountHAVE 1000 - amountWant 100
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8),BigDecimal.valueOf(100).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[64]);
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_A = createOrderTransaction.getOrder().getId();
		
		//CREATE ORDER TWO (SELLING 999 B FOR A AT A PRICE OF 5) (I CAN BUY AT INCREMENTS OF 0,00000010)
		//GENERATES TRADE 100 B FOR 1000 A			
		createOrderTransaction = new CreateOrderTransaction(accountB, keyB, keyA, 
				BigDecimal.valueOf(999).setScale(8),BigDecimal.valueOf(4995).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[]{5, 6});
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_B = createOrderTransaction.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(accountA.getConfirmedBalance(keyA, db), BigDecimal.valueOf(49000).setScale(8)); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyB, db), BigDecimal.valueOf(49001).setScale(8)); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(100).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(1000).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		Order orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		Trade trade = orderB.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_B));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_A));
		Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(BigDecimal.valueOf(100)));
		
		//CREATE ORDER THREE (SELLING 99.99999999 A FOR B AT A PRICE OF 0.2
		BigDecimal amoHave = new BigDecimal(BigInteger.valueOf(9999999999L), 8);
		BigDecimal amoWant = BigDecimal.valueOf(19.99999999);
		
		createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				amoHave, amoWant,
				(byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_C = createOrderTransaction.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, db).compareTo(new BigDecimal("48900.00000001"))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(49001))); //BALANCE B FOR ACCOUNT B	
		assertEquals(accountA.getConfirmedBalance(keyB, db).setScale(8), new BigDecimal("119.99999999").setScale(8)); //BALANCE B FOR ACCOUNT A
		assertEquals(accountB.getConfirmedBalance(keyA, db), new BigDecimal("1099.99999995").setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(new BigDecimal("119.99999999")));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		Order orderC = db.getOrderMap().get(orderID_C);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilledHave().compareTo(new BigDecimal("99.99999995")));
		Assert.assertEquals(false, orderC.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		trade = orderC.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_C));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_B));
		Assert.assertEquals(0, trade.getAmountHave().compareTo(new BigDecimal("19.99999999")));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(new BigDecimal("99.99999995")));
	}

	//////////////////////////// reverse price
	@Test
	public void testOrderProcessingDivisible2()
	{
		
		init();

		//CREATE ORDER ONE (SELLING 100 A FOR B AT A PRICE OF 10)
		// amountHAVE 100 - amountWant 1000
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(100).setScale(8),BigDecimal.valueOf(1000).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[64]);
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_A = createOrderTransaction.getOrder().getId();
		
		//CREATE ORDER TWO (SELLING 4995 B FOR A AT A PRICE OF 0.05))
		//GENERATES TRADE 100 B FOR 1000 A
		createOrderTransaction = new CreateOrderTransaction(accountB, keyB, keyA, 
				BigDecimal.valueOf(4995).setScale(8),BigDecimal.valueOf(249.75).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[]{5, 6});
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_B = createOrderTransaction.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(accountA.getConfirmedBalance(keyA, db), BigDecimal.valueOf(49900).setScale(8)); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(1000).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyB, db), BigDecimal.valueOf(45005).setScale(8)); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(100).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		Order orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(1000).setScale(8));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		Trade trade = orderB.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_B));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_A));
		Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(1000).setScale(8));
		
		//CREATE ORDER THREE (SELLING 19.99999999 A FOR B AT A PRICE OF 20)
		BigDecimal amoHave = BigDecimal.valueOf(1.999999999);
		BigDecimal amoWant = BigDecimal.valueOf(3.999999998);
		
		createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				amoHave, amoWant,
				(byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_C = createOrderTransaction.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(accountA.getConfirmedBalance(keyA, db), new BigDecimal("49880.00000001")); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyB, db), BigDecimal.valueOf(45005).setScale(8)); //BALANCE B FOR ACCOUNT B	
		assertEquals(accountA.getConfirmedBalance(keyB, db).setScale(8), new BigDecimal("1799.99999960")); //BALANCE B FOR ACCOUNT A
		assertEquals(accountB.getConfirmedBalance(keyA, db), new BigDecimal("139.99999998").setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(orderA.getFulfilledHave(), BigDecimal.valueOf(100).setScale(8));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(orderB.getFulfilledHave(), new BigDecimal("1799.99999960").setScale(8));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		Order orderC = db.getOrderMap().get(orderID_C);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(orderC.getFulfilledHave(), new BigDecimal("39.99999998").setScale(8));
		Assert.assertEquals(false, orderC.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		trade = orderC.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_C));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_B));
		Assert.assertEquals(trade.getAmountHave(), new BigDecimal("799.99999960").setScale(8));
		Assert.assertEquals(trade.getAmountWant(), new BigDecimal("39.99999998").setScale(8));
	}

	
	@Test
	public void testOrderProcessingWantDivisible()
	{		
		
		init();
		//CREATE ASSET
		assetA = new AssetVenture(accountA, "a", "a", 50000l, (byte) 8, false);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		issueAssetTransaction.process(db, false);
				

		//CREATE ASSET
		assetB = new AssetVenture(accountB, "b", "b", 50000l, (byte) 8, true);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte)0, System.currentTimeMillis(), accountB.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
		
		long keyA = assetA.getKey(db);
		long keyB = assetB.getKey(db);

		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8),BigDecimal.valueOf(100).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[64]);
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_A = orderCreation.getOrder().getId();

		//CREATE ORDER TWO (SELLING 99.9 B FOR A AT A PRICE OF 5)
		//GENERATES TRADE 99,9 B FOR 495 A
		orderCreation = new CreateOrderTransaction(accountB, keyB, keyA,
				BigDecimal.valueOf(99.9).setScale(8),BigDecimal.valueOf(495).setScale(8), (byte)0, System.currentTimeMillis(), accountB.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_B = orderCreation.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(49900.1))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(99.9).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(999).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = db.getOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(999)));
		Assert.assertEquals(false, orderA.isFulfilledHave());
		
		Order orderB = db.getCompletedOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(99.9)));
		Assert.assertEquals(true, orderB.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		Trade trade = orderB.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_B));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_A));
		Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(999)));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(BigDecimal.valueOf(99.9)));
		
		//CREATE ORDER THREE (SELLING 99 A FOR B AT A PRICE OF 0.2)
		//GENERATED TRADE 99 A FOR 9.9 B
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(99).setScale(8),BigDecimal.valueOf(19.8).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_C = orderCreation.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(48901))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(49900.1))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(99.9))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(999))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = db.getOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(999)));
		Assert.assertEquals(false, orderA.isFulfilledHave());
		
		orderB = db.getCompletedOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(99.9)));
		Assert.assertEquals(true, orderB.isFulfilledHave());
		
		Order orderC = db.getOrderMap().get(orderID_C);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderC.isFulfilledHave());
		
		//CHECK TRADES
		Assert.assertEquals(0, orderC.getInitiatedTrades(db).size());
	}
	
	@Test
	public void testOrderProcessingHaveDivisible()
	{
		
		init();

		//CREATE ASSET
		assetA = new AssetVenture(accountA, "a", "a", 50000l, (byte) 8, true);
				
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
				
		//CREATE ASSET
		assetB = new AssetVenture(accountB, "b", "b", 50000l, (byte) 8, false);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB,assetB, (byte)0, System.currentTimeMillis(), accountB.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
		
		long keyA = assetA.getKey(db);
		long keyB = assetB.getKey(db);

		
		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(100).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_A = orderCreation.getOrder().getId();
		
		//CREATE ORDER TWO (SELLING 200 B FOR PRICE OF 5)
		//GENERATES TRADE 100 B FOR 1000 A
		orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, 
				BigDecimal.valueOf(200).setScale(8),BigDecimal.valueOf(1000).setScale(8), (byte)0, System.currentTimeMillis(), accountB.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_B = orderCreation.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(49800))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(100))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(1000))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		Order orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		Trade trade = orderB.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_B));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_A));
		Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(BigDecimal.valueOf(100)));
		
		//CREATE ORDER THREE (SELLING 99 A FOR B AT A PRICE OF 0.2) (I CAN BUY AT INCREMENTS OF 1)
		//GENERATED TRADE 95 A for 19 B
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(95.9).setScale(8),BigDecimal.valueOf(19).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_C = orderCreation.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(accountA.getConfirmedBalance(keyA, db), BigDecimal.valueOf(48904.1).setScale(8)); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(49800))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(119).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(1095.9).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(119)));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		Order orderC = db.getCompletedOrderMap().get(orderID_C);
		Assert.assertEquals(false, db.getOrderMap().contains(orderC.getId()));
		Assert.assertEquals(orderC.getFulfilledHave(), BigDecimal.valueOf(95.9).setScale(8));
		Assert.assertEquals(true, orderC.isFulfilledHave());
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		trade = orderC.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_C));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_B));
		Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(19)));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(BigDecimal.valueOf(95.9)));
	}
	
	@Test
	public void testOrderProcessingHaveDivisible3()
	{
		
		init();

		//CREATE ASSET
		assetA = new AssetVenture(accountA, "a", "a", 100l, (byte) 0, false);
				
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		issueAssetTransaction.process(db, false);
				
		//CREATE ASSET
		assetB = new AssetVenture(accountB, "b", "b", 100000l, (byte) 8, true);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte)0, System.currentTimeMillis(), accountB.getLastReference(db));
		issueAssetTransaction.process(db, false);
		
		long keyA = assetA.getKey(db);
		long keyB = assetB.getKey(db);

		
		//CREATE ORDER _A  SELL 1A for 15000 = 15000
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1).setScale(8), BigDecimal.valueOf(15000).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_A = orderCreation.getOrder().getId();

		//CREATE ORDER _B  SELL 2A x 20000 = 40000
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(2).setScale(8), BigDecimal.valueOf(40000).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_B = orderCreation.getOrder().getId();

		//CREATE ORDER _C SELL 4A x 25000 = 100000
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(4).setScale(8), BigDecimal.valueOf(100000).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_C = orderCreation.getOrder().getId();

		//CREATE ORDER _D (BUY) 30000 x 2 = 60000
		orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, 
				BigDecimal.valueOf(60000).setScale(8),BigDecimal.valueOf(2).setScale(8), (byte)0, System.currentTimeMillis(), accountB.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID_D = orderCreation.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(accountA.getConfirmedBalance(keyA, db), BigDecimal.valueOf(93).setScale(8)); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyB, db), BigDecimal.valueOf(65000).setScale(8)); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(35000).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(2).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1)));
		Assert.assertEquals(0, orderA.getFulfilledWant().compareTo(BigDecimal.valueOf(15000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		Assert.assertEquals(true, orderA.isFulfilled());
		
		Order orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(1).setScale(8));
		Assert.assertEquals(orderB.getFulfilledWant(), BigDecimal.valueOf(20000).setScale(8));
		Assert.assertEquals(false, orderB.isFulfilledHave());	

		Order orderC = db.getOrderMap().get(orderID_C);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderC.getId()));
		Assert.assertEquals(orderC.getFulfilledHave(), BigDecimal.valueOf(0).setScale(8));
		Assert.assertEquals(false, orderC.isFulfilledHave());

		// buy order
		Order orderD = db.getCompletedOrderMap().get(orderID_D);
		Assert.assertEquals(false, db.getOrderMap().contains(orderD.getId()));
		Assert.assertEquals(orderD.getFulfilledHave(), BigDecimal.valueOf(35000).setScale(8));
		Assert.assertEquals(orderD.getFulfilledWant(), BigDecimal.valueOf(2).setScale(8));
		Assert.assertEquals(false, orderD.isFulfilledHave());	
		Assert.assertEquals(true, orderD.isFulfilledWant());
		Assert.assertEquals(true, orderD.isFulfilled());

		//CHECK TRADES
		Assert.assertEquals(2, orderD.getInitiatedTrades(db).size());
		
		Trade trade = orderD.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_D));
		
		// this may be WRONG in some case - reRUN task!
		if (trade.getTarget().compareTo(orderID_A) == 0) {
			Assert.assertEquals(0, trade.getTarget().compareTo(orderID_A));
			Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(1).setScale(8));
			Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(15000).setScale(8));

			trade = orderD.getInitiatedTrades(db).get(1);
			Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_D));
			Assert.assertEquals(0, trade.getTarget().compareTo(orderID_B));
			Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(1).setScale(8));
			Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(20000).setScale(8));
		}


		//CREATE ORDER _E - buy 23000 x 2 = 46000
		orderCreation = new CreateOrderTransaction(accountB, keyB, keyA,
				BigDecimal.valueOf(46000).setScale(8),BigDecimal.valueOf(2).setScale(8), (byte)0, System.currentTimeMillis(), accountB.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountB, false);
		orderCreation.process(db, false);
		BigInteger orderID_E = orderCreation.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(accountA.getConfirmedBalance(keyA, db), BigDecimal.valueOf(93).setScale(8)); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyB, db), BigDecimal.valueOf(19000).setScale(8)); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(55000).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(3).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		/// order in memory !!!
		Order orderE = db.getOrderMap().get(orderID_E);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderE.getId()));
		Assert.assertEquals(orderE.getFulfilledHave(), BigDecimal.valueOf(20000).setScale(8));
		Assert.assertEquals(0, orderE.getFulfilledWant().compareTo(BigDecimal.valueOf(1)));
		Assert.assertEquals(false, orderE.isFulfilledHave());
		Assert.assertEquals(false, orderE.isFulfilled());

		Assert.assertEquals(false, db.getOrderMap().contains(orderB.getId()));
		Assert.assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(2).setScale(8));
		Assert.assertEquals(orderB.getFulfilledWant(), BigDecimal.valueOf(40000).setScale(8));
		Assert.assertEquals(true, orderB.isFulfilledHave());
		Assert.assertEquals(true, orderB.isFulfilledWant());
						
		//CHECK TRADES
		Assert.assertEquals(1, orderE.getInitiatedTrades(db).size());
		
		trade = orderE.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_E));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_B));
		Assert.assertEquals(trade.getAmountHave(), BigDecimal.valueOf(1).setScale(8));
		Assert.assertEquals(trade.getAmountWant(), BigDecimal.valueOf(20000).setScale(8));

	}
	

	@Test
	public void testOrderProcessingNonDivisible()
	{
		
		init();
		
		//CREATE ASSET
		AssetCls assetA = new AssetVenture(accountA, "a", "a", 50000l, (byte)2, false);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
		
		//CREATE ASSET
		AssetCls assetB = new AssetVenture(accountB, "b", "b", 50000l, (byte)8, false);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte)0, System.currentTimeMillis(), accountB.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
		
		long keyA = assetA.getKey(db);
		long keyB = assetB.getKey(db);
		

		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB, 
				BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(100).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false); // need for Order.getID()
		orderCreation.process(db, false);
		BigInteger orderID_A = orderCreation.getOrder().getId();

		
		//CREATE ORDER TWO (SELLING 1000 B FOR A AT A PRICE OF 5)
		//GENERATES TRADE 100 B FOR 1000 A
		orderCreation = new CreateOrderTransaction(accountB, keyB, keyA, 
				BigDecimal.valueOf(1000).setScale(8), BigDecimal.valueOf(5000).setScale(8), (byte)0, System.currentTimeMillis(), accountB.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountB, false); // need for Order.getID()
		orderCreation.process(db, false);
		BigInteger orderID_B = orderCreation.getOrder().getId();
		
		
		//CHECK BALANCES
		Assert.assertEquals(accountA.getConfirmedBalance(keyA, db), BigDecimal.valueOf(49000).setScale(8)); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyB, db), BigDecimal.valueOf(49000).setScale(8).setScale(8)); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(100).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(1000).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		Order orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(100)));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(1, orderB.getInitiatedTrades(db).size());
		
		Trade trade = orderB.getInitiatedTrades(db).get(0);
		assertEquals(trade.getInitiator(), orderID_B);
		assertEquals(trade.getTarget(), orderID_A);
		Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(BigDecimal.valueOf(100)));
			
		//CREATE ORDER THREE (SELLING 24 A FOR B AT A PRICE OF 0.2)
		//GENERATES TRADE 20 A FOR 4 B
		orderCreation = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(24).setScale(8),BigDecimal.valueOf(4).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		assertEquals(Transaction.VALIDATE_OK, orderCreation.isValid(db, releaserReference));
		orderCreation.sign(accountA, false); // need for Order.getID()
		orderCreation.process(db, false);
		BigInteger orderID_C = orderCreation.getOrder().getId();
		
		//CHECK BALANCES
		assertEquals(accountA.getConfirmedBalance(keyA, db), BigDecimal.valueOf(48976).setScale(8)); //BALANCE A FOR ACCOUNT A
		assertEquals(accountB.getConfirmedBalance(keyB, db), BigDecimal.valueOf(49000).setScale(8)); //BALANCE B FOR ACCOUNT B	
		assertEquals(accountA.getConfirmedBalance(keyB, db).setScale(8), BigDecimal.valueOf(104).setScale(8)); //BALANCE B FOR ACCOUNT A
		assertEquals(accountB.getConfirmedBalance(keyA, db), BigDecimal.valueOf(1024).setScale(8)); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(104)));
		Assert.assertEquals(false, orderB.isFulfilledHave());
		
		Order orderC = db.getCompletedOrderMap().get(orderID_C);
		Assert.assertEquals(false, db.getOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilledHave().compareTo(BigDecimal.valueOf(24)));
		Assert.assertEquals(true, orderC.isFulfilledHave());
		
		//CHECK TRADES
		Assert.assertEquals(1, orderC.getInitiatedTrades(db).size());
		
		trade = orderC.getInitiatedTrades(db).get(0);
		/// ??? assertEquals(trade.getInitiator(), orderID);
		assertEquals(trade.getTarget(),orderID_B);
		Assert.assertEquals(0, trade.getAmountHave().compareTo(BigDecimal.valueOf(4)));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(BigDecimal.valueOf(24)));
	}
	
	
	@Test
	public void testOrderProcessingMultipleOrders()
	{

		init();
		//CREATE ASSET
		AssetCls assetA = new AssetVenture(accountA, "a", "a", 50000l, (byte) 8, true);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
				
		accountB.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);
		
		//CREATE ASSET
		AssetCls assetB = new AssetVenture(accountB, "b", "b", 50000l, (byte) 8, true);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte)0, System.currentTimeMillis(), accountB.getLastReference(db), new byte[64]);
		issueAssetTransaction.process(db, false);
		
		long keyA = assetA.getKey(db);
		long keyB = assetB.getKey(db);

		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8),BigDecimal.valueOf(100).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_A = createOrderTransaction.getOrder().getId();
		
		//CREATE ORDER TWO (SELLING 1000 A FOR B AT A PRICE FOR 0.20)
		createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8),BigDecimal.valueOf(200).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_B = createOrderTransaction.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(48000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(50000))); //BALANCE B FOR ACCOUNT B	
		assertEquals(accountA.getConfirmedBalance(keyB, db), BigDecimal.valueOf(0).setScale(8)); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(0))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = db.getOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderA.isFulfilledHave());
		
		Order orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(0, orderB.getInitiatedTrades(db).size());
		
		//CREATE ORDER THREE (SELLING 150 B FOR A AT A PRICE OF 5)
		createOrderTransaction = new CreateOrderTransaction(accountB, keyB, keyA,
				BigDecimal.valueOf(150).setScale(8),BigDecimal.valueOf(750).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(db), new byte[]{3, 4});
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(db, false);
		BigInteger orderID_C = createOrderTransaction.getOrder().getId();
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(48000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(49850))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyB, db).compareTo(BigDecimal.valueOf(150))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyA, db).compareTo(BigDecimal.valueOf(1250))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = db.getCompletedOrderMap().get(orderID_A);
		Assert.assertEquals(false, db.getOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(1000)));
		Assert.assertEquals(true, orderA.isFulfilledHave());
		
		orderB = db.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, db.getCompletedOrderMap().contains(orderB.getId()));
		Assert.assertEquals(0, orderB.getFulfilledHave().compareTo(BigDecimal.valueOf(250)));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		Order orderC = db.getCompletedOrderMap().get(orderID_C);
		Assert.assertEquals(false, db.getOrderMap().contains(orderC.getId()));
		Assert.assertEquals(0, orderC.getFulfilledHave().compareTo(BigDecimal.valueOf(150)));
		Assert.assertEquals(true, orderC.isFulfilledHave());
		
		//CHECK TRADES
		Assert.assertEquals(0, orderA.getInitiatedTrades(db).size());
		Assert.assertEquals(0, orderB.getInitiatedTrades(db).size());
		Assert.assertEquals(2, orderC.getInitiatedTrades(db).size());
		
		Trade trade = orderC.getInitiatedTrades(db).get(1);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_C));
		assertEquals(trade.getTarget(), orderID_A);
		Assert.assertEquals(0, trade.getAmountHave().compareTo(new BigDecimal("1000")));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(new BigDecimal("100")));
		
		trade = orderC.getInitiatedTrades(db).get(0);
		Assert.assertEquals(0, trade.getInitiator().compareTo(orderID_C));
		Assert.assertEquals(0, trade.getTarget().compareTo(orderID_B));
		Assert.assertEquals(0, trade.getAmountHave().compareTo(new BigDecimal("250")));
		Assert.assertEquals(0, trade.getAmountWant().compareTo(new BigDecimal("50")));
	}
	


	@Test
	public void testOrderProcessingForks()
	{

		init();
		
		//CREATE ASSET
		AssetCls assetA = new AssetVenture(accountA, "a", "a", 50000l, (byte) 8, true);
		
		//CREATE ISSUE ASSET TRANSACTION
		Transaction issueAssetTransaction = new IssueAssetTransaction(accountA, assetA, (byte)0, System.currentTimeMillis(), accountA.getLastReference(db));
		issueAssetTransaction.process(db, false);
				
		//transaction = new GenesisTransaction(accountB, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(dbSet, false);
		accountB.setConfirmedBalance(FEE_KEY, BigDecimal.valueOf(1).setScale(8), db);

		//CREATE ASSET
		AssetCls assetB = new AssetVenture(accountB, "b", "b", 50000l, (byte) 8, true);
		
		//CREATE ISSUE ASSET TRANSACTION
		issueAssetTransaction = new IssueAssetTransaction(accountB, assetB, (byte)0, System.currentTimeMillis(), accountB.getLastReference(db));
		issueAssetTransaction.process(db, false);
		
		long keyA = assetA.getKey(db);
		long keyB = assetB.getKey(db);

		//CREATE ORDER ONE (SELLING 1000 A FOR B AT A PRICE OF 0.10)
		DBSet fork1 = db.fork();
		CreateOrderTransaction createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8),BigDecimal.valueOf(100).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(fork1), new byte[]{5,6});
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(fork1, false);
		BigInteger orderID_A = createOrderTransaction.getOrder().getId();
		
		//CREATE ORDER TWO (SELLING 1000 A FOR B AT A PRICE FOR 0.20)
		DBSet fork2 = fork1.fork();
		createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8),BigDecimal.valueOf(200).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(fork2), new byte[]{1, 2});
		createOrderTransaction.sign(accountA, false);
		createOrderTransaction.process(fork2, false);
		BigInteger orderID_B = createOrderTransaction.getOrder().getId();
				
		//CREATE ORDER THREE (SELLING 150 B FOR A AT A PRICE OF 5)
		DBSet fork3 = fork2.fork();
		createOrderTransaction = new CreateOrderTransaction(accountB, keyB, keyA,
				BigDecimal.valueOf(150).setScale(8),BigDecimal.valueOf(750).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(fork3), new byte[]{3, 4});
		createOrderTransaction.sign(accountB, false);
		createOrderTransaction.process(fork3, false);
		BigInteger orderID_C = createOrderTransaction.getOrder().getId();
		
		//ORPHAN ORDER THREE
		createOrderTransaction.orphan(fork3, false);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, fork3).compareTo(BigDecimal.valueOf(48000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, fork3).compareTo(BigDecimal.valueOf(50000))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyB, fork3).compareTo(BigDecimal.valueOf(0))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyA, fork3).compareTo(BigDecimal.valueOf(0))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		Order orderA = fork3.getOrderMap().get(orderID_A);
		Assert.assertEquals(false, fork3.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderA.isFulfilledHave());
		
		Order orderB = fork3.getOrderMap().get(orderID_B);
		Assert.assertEquals(false, fork3.getCompletedOrderMap().contains(orderB.getId()));
		assertEquals(orderB.getFulfilledHave(), BigDecimal.valueOf(0).setScale(8));
		Assert.assertEquals(false, orderB.isFulfilledHave());	
		
		//CHECK TRADES
		Assert.assertEquals(0, orderB.getInitiatedTrades(fork3).size());
		
		//ORPHAN ORDER TWO
		createOrderTransaction = new CreateOrderTransaction(accountA, keyA, keyB,
				BigDecimal.valueOf(1000).setScale(8),BigDecimal.valueOf(200).setScale(8), (byte)0, System.currentTimeMillis(), accountA.getLastReference(fork2), new byte[]{1, 2});
		createOrderTransaction.orphan(fork2, false);
		
		//CHECK BALANCES
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyA, fork2).compareTo(BigDecimal.valueOf(49000))); //BALANCE A FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyB, fork2).compareTo(BigDecimal.valueOf(50000))); //BALANCE B FOR ACCOUNT B	
		Assert.assertEquals(0, accountA.getConfirmedBalance(keyB, fork2).compareTo(BigDecimal.valueOf(0))); //BALANCE B FOR ACCOUNT A
		Assert.assertEquals(0, accountB.getConfirmedBalance(keyA, fork2).compareTo(BigDecimal.valueOf(0))); //BALANCE A FOR ACCOUNT B
		
		//CHECK ORDERS
		orderA = fork2.getOrderMap().get(orderID_A);
		Assert.assertEquals(false, fork2.getCompletedOrderMap().contains(orderA.getId()));
		Assert.assertEquals(0, orderA.getFulfilledHave().compareTo(BigDecimal.valueOf(0)));
		Assert.assertEquals(false, orderA.isFulfilledHave());
		
		Assert.assertEquals(false, fork2.getOrderMap().contains(orderID_C));
		Assert.assertEquals(false, fork2.getCompletedOrderMap().contains(orderID_C));
	}

	
	//CANCEL ORDER
	
	@Test
	public void validateSignatureCancelOrderTransaction()
	{
		

		init();
		
		//CREATE ORDER CANCEL
		Transaction cancelOrderTransaction = new CancelOrderTransaction(accountA, BigInteger.TEN, FEE_POWER, timestamp, accountA.getLastReference(db));
		cancelOrderTransaction.sign(accountA, false);
		//CHECK IF ORDER CANCEL IS VALID
		assertEquals(true, cancelOrderTransaction.isSignatureValid());
		
		//INVALID SIGNATURE
		cancelOrderTransaction = new CancelOrderTransaction(accountA, BigInteger.TEN, FEE_POWER, timestamp, accountA.getLastReference(db), new byte[1]);
		
		//CHECK IF ORDER CANCEL
		assertEquals(false, cancelOrderTransaction.isSignatureValid());
	}
	
	@Test
	public void validateCancelOrderTransaction() 
	{

		init();
				
		//CREATE ORDER
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID = orderCreation.getOrder().getId();

		//CREATE CANCEL ORDER
		//Long time = maker.getLastReference(db);
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(accountA, orderID, FEE_POWER, System.currentTimeMillis(), accountA.getLastReference(db));		

		//CHECK IF CANCEL ORDER IS VALID
		assertEquals(Transaction.VALIDATE_OK, cancelOrderTransaction.isValid(db, releaserReference));
		
		cancelOrderTransaction = new CancelOrderTransaction(accountA, new BigInteger(new byte[]{5,7}), FEE_POWER, System.currentTimeMillis(), accountA.getLastReference(db));		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.ORDER_DOES_NOT_EXIST, cancelOrderTransaction.isValid(db, releaserReference));
		
		//CREATE INVALID CANCEL ORDER INCORRECT CREATOR
		byte[] seed = Crypto.getInstance().digest("invalid".getBytes());
		byte[] privateKey = Crypto.getInstance().createKeyPair(seed).getA();
		PrivateKeyAccount invalidCreator = new PrivateKeyAccount(privateKey);
		cancelOrderTransaction = new CancelOrderTransaction(invalidCreator, orderID, FEE_POWER, System.currentTimeMillis(), accountA.getLastReference(db), new byte[]{1,2});		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.INVALID_ORDER_CREATOR, cancelOrderTransaction.isValid(db, releaserReference));
				
		//CREATE INVALID CANCEL ORDER NO BALANCE
		DBSet fork = db.fork();
		cancelOrderTransaction = new CancelOrderTransaction(accountA, orderID, FEE_POWER, System.currentTimeMillis(), accountA.getLastReference(db), new byte[]{1,2});		
		accountA.setConfirmedBalance(FEE_KEY, BigDecimal.ZERO, fork);		
		
		//CHECK IF CANCEL ORDER IS INVALID
		assertEquals(Transaction.NOT_ENOUGH_FEE, cancelOrderTransaction.isValid(fork, releaserReference));
				
		//CREATE CANCEL ORDER INVALID REFERENCE
		cancelOrderTransaction = new CancelOrderTransaction(accountA, orderID, FEE_POWER, System.currentTimeMillis(), -123L, new byte[]{1,2});		
				
		//CHECK IF NAME REGISTRATION IS INVALID
		assertEquals(Transaction.INVALID_REFERENCE, cancelOrderTransaction.isValid(db, releaserReference));
		
	}

	@Test
	public void parseCancelOrderTransaction() 
	{
		
		init();
		
		//CREATE ORDER
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID = orderCreation.getOrder().getId();

		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(accountA, orderID, FEE_POWER, System.currentTimeMillis(), accountA.getLastReference(db));
		cancelOrderTransaction.sign(accountA, false);
				
		//CONVERT TO BYTES
		byte[] rawCancelOrder = cancelOrderTransaction.toBytes(true, null);
		
		//CHECK DATALENGTH
		assertEquals(rawCancelOrder.length, cancelOrderTransaction.getDataLength(false));
		
		try 
		{	
			//PARSE FROM BYTES
			CancelOrderTransaction parsedCancelOrder = (CancelOrderTransaction) TransactionFactory.getInstance().parse(rawCancelOrder, releaserReference);
			
			//CHECK INSTANCE
			assertEquals(true, parsedCancelOrder instanceof CancelOrderTransaction);
			
			//CHECK SIGNATURE
			assertEquals(true, Arrays.equals(cancelOrderTransaction.getSignature(), parsedCancelOrder.getSignature()));
			
			//CHECK AMOUNT CREATOR
			assertEquals(cancelOrderTransaction.getAmount(accountA), parsedCancelOrder.getAmount(accountA));	
			
			//CHECK OWNER
			assertEquals(cancelOrderTransaction.getCreator().getAddress(), parsedCancelOrder.getCreator().getAddress());	
			
			//CHECK ORDER
			assertEquals(0, cancelOrderTransaction.getOrder().compareTo(parsedCancelOrder.getOrder()));	
			
			//CHECK FEE
			assertEquals(cancelOrderTransaction.getFee(), parsedCancelOrder.getFee());	
			
			//CHECK REFERENCE
			assertEquals(cancelOrderTransaction.getReference(), parsedCancelOrder.getReference());	
			
			//CHECK TIMESTAMP
			assertEquals(cancelOrderTransaction.getTimestamp(), parsedCancelOrder.getTimestamp());				
		}
		catch (Exception e) 
		{
			fail("Exception while parsing transaction.");
		}
		
		//PARSE TRANSACTION FROM WRONG BYTES
		rawCancelOrder = new byte[cancelOrderTransaction.getDataLength(false)];
		
		try 
		{	
			//PARSE FROM BYTES
			TransactionFactory.getInstance().parse(rawCancelOrder, releaserReference);
			
			//FAIL
			fail("this should throw an exception");
		}
		catch (Exception e) 
		{
			//EXCEPTION IS THROWN OK
		}	
	}
	
	@Test
	public void processCancelOrderTransaction()
	{

		init();
		
		//CREATE ORDER
		assertEquals(BigDecimal.valueOf(assetA.getQuantity()).setScale(8), accountA.getConfirmedBalance( keyA, db));
		orderCreation.sign(accountA, false);
		orderCreation.process(db, false);
		BigInteger orderID = orderCreation.getOrder().getId();

		assertEquals(BigDecimal.valueOf(assetA.getQuantity())
				.subtract(orderCreation.getOrder().getAmountHave()).setScale(8),
				accountA.getConfirmedBalance( keyA, db));


		//CREATE CANCEL ORDER
		CancelOrderTransaction cancelOrderTransaction = new CancelOrderTransaction(accountA, orderID, FEE_POWER, System.currentTimeMillis(), accountA.getLastReference(db));
		cancelOrderTransaction.sign(accountA, false);
		
		cancelOrderTransaction.process(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(assetA.getQuantity()).setScale(8),
				accountA.getConfirmedBalance(keyA, db));
						
		//CHECK REFERENCE SENDER
		assertEquals(cancelOrderTransaction.getTimestamp(), accountA.getLastReference(db));
				
		//CHECK ORDER EXISTS
		assertEquals(false, db.getOrderMap().contains(new BigInteger(new byte[]{5,6})));

		////////// OPHRAN ////////////////
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(assetA.getQuantity()).setScale(8), accountA.getConfirmedBalance( keyA, db));
		cancelOrderTransaction.orphan(db, false);
		
		//CHECK BALANCE SENDER
		assertEquals(BigDecimal.valueOf(assetA.getQuantity())
				.subtract(orderCreation.getOrder().getAmountHave()).setScale(8),
				accountA.getConfirmedBalance( keyA, db));
						
		//CHECK REFERENCE SENDER
		assertEquals(orderCreation.getTimestamp(), accountA.getLastReference(db));
				
		//CHECK ORDER EXISTS
		assertEquals(true, db.getOrderMap().contains(orderID));
	}

}
