package core.account;
//04/01 +- 
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple4;

import api.ApiErrorFactory;

//import com.google.common.primitives.Bytes;

import at.AT_Transaction;
import database.Item_Map;
import controller.Controller;
import core.BlockGenerator;
import core.block.Block;
import core.crypto.Base58;
import core.item.ItemCls;
import core.item.persons.PersonCls;
import core.item.statuses.StatusCls;
import core.naming.Name;
//import core.item.assets.AssetCls;
import core.transaction.Transaction;
import core.transaction.TransactionAmount;
import utils.NameUtils;
import utils.NumberAsString;
import utils.Pair;
import utils.NameUtils.NameResult;
import database.DBSet;
import database.NameMap;
import ntp.NTP;

public class Account {
	
	public static final int ADDRESS_LENGTH = 25;
	private static final long ERM_KEY = Transaction.RIGHTS_KEY;
	private static final long FEE_KEY = Transaction.FEE_KEY;
	public static final long ALIVE_KEY = StatusCls.ALIVE_KEY;
	public static final long DEAD_KEY = StatusCls.DEAD_KEY;

	protected String address;
	
	private byte[] lastBlockSignature;
	private BigDecimal generatingBalance; //used  for forging balance
	
	protected Account()
	{
		this.generatingBalance = BigDecimal.ZERO.setScale(8);
	}
	
	public Account(String address)
	{

		// test address
		assert(Base58.decode(address) instanceof byte[] );

		this.address = address;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	//BALANCE
	
	// GET
	public BigDecimal getUnconfirmedBalance(long key)
	{
		return Controller.getInstance().getUnconfirmedBalance(this, key);
	}
	/*
	public BigDecimal getConfirmedBalance()
	{
		return this.getConfirmedBalance(DBSet.getInstance());
	}
	public BigDecimal getConfirmedBalance(DBSet db)
	{
		return db.getAssetBalanceMap().get(getAddress(), Transaction.FEE_KEY);
	}
	*/
	public BigDecimal getConfirmedBalance(long key)
	{
		return this.getConfirmedBalance(key, DBSet.getInstance());
	}
	
	public BigDecimal getConfirmedBalance(long key, DBSet db)
	{
		return db.getAssetBalanceMap().get(getAddress(), key);
	}
	/*
	public Integer setConfirmedPersonStatus(long personKey, long statusKey, int end_date, DBSet db)
	{
		return db.getPersonStatusMap().addItem(personKey, statusKey, end_date);
	}
	*/

	// SET
	/*
	public void setConfirmedBalance(BigDecimal amount)
	{
		this.setConfirmedBalance(amount, DBSet.getInstance());
	}
	public void setConfirmedBalance(BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		db.getAssetBalanceMap().set(getAddress(), Transaction.FEE_KEY, amount);
	}
	*/
	//
	public void setConfirmedBalance(long key, BigDecimal amount)
	{
		this.setConfirmedBalance(key, amount, DBSet.getInstance());
	}

	public void setConfirmedBalance(long key, BigDecimal amount, DBSet db)
	{
		//UPDATE BALANCE IN DB
		db.getAssetBalanceMap().set(getAddress(), key, amount);
	}

	// STATUS
	/*
	public void setConfirmedPersonStatus(long personKey, long statusKey, Integer days)
	{
		this.setConfirmedPersonStatus(personKey, statusKey, days, DBSet.getInstance());
	}
		
	public void setConfirmedPersonStatus(long personKey, long statusKey, Integer days, DBSet db)
	{
		//UPDATE PRIMARY TIME IN DB
		db.getPersonStatusMap().set(personKey, statusKey, days);
	}
	*/

	
	public BigDecimal getBalance(int confirmations, long key)
	{
		return this.getBalance(confirmations, key, DBSet.getInstance());
	}
	/*
	public BigDecimal getBalance(int confirmations)
	{
		return this.getBalance(confirmations, FEE_KEY, DBSet.getInstance());
	}
	public BigDecimal getBalance(int confirmations, DBSet db)
	{
		return this.getBalance(confirmations, FEE_KEY, DBSet.getInstance());
	}
	*/
	public BigDecimal getBalance(int confirmations, long key, DBSet db)
	{
		//CHECK IF UNCONFIRMED BALANCE
		if(confirmations <= 0)
		{
			return this.getUnconfirmedBalance(key);
		}
		
		//IF 1 CONFIRMATION
		if(confirmations == 1)
		{
			return this.getConfirmedBalance(key, db);
		}
		
		//GO TO PARENT BLOCK 10
		BigDecimal balance = this.getConfirmedBalance(key, db);
		Block block = db.getBlockMap().getLastBlock();
		
		for(int i=1; i<confirmations && block != null && block instanceof Block; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this))
				{
					balance = balance.subtract(transaction.getAmount(this));
				}
			}
				
			block = block.getParent(db);
		}
		
		//RETURN
		return balance;
	}
	
	private void updateGeneratingBalance(DBSet db)
	{
		//CHECK IF WE NEED TO RECALCULATE
		if(this.lastBlockSignature == null)
		{
			this.lastBlockSignature = db.getBlockMap().getLastBlockSignature();
			calculateGeneratingBalance(db);
		}
		else
		{
			//CHECK IF WE NEED TO RECALCULATE
			if(!Arrays.equals(this.lastBlockSignature, db.getBlockMap().getLastBlockSignature()))
			{
				this.lastBlockSignature = db.getBlockMap().getLastBlockSignature();
				calculateGeneratingBalance(db);
			}
		}
	}
	
	// balance FOR generation
	public void calculateGeneratingBalance(DBSet db)
	{
		//CONFIRMED BALANCE + ALL NEGATIVE AMOUNTS IN LAST 9 BLOCKS - foe ERM_KEY only
		BigDecimal balance = this.getConfirmedBalance(ERM_KEY, db);
		
		Block block = db.getBlockMap().getLastBlock();
		
		for(int i=1; i<BlockGenerator.RETARGET && block != null && block.getHeight(db) > 1; i++)
		{
			for(Transaction transaction: block.getTransactions())
			{
				if(transaction.isInvolved(this) & transaction instanceof TransactionAmount)
				{
					TransactionAmount ta = (TransactionAmount)transaction;
					
					if(ta.getKey() == ERM_KEY & transaction.getAmount(this).compareTo(BigDecimal.ZERO) == 1)
					{
						balance = balance.subtract(transaction.getAmount(this));
					}
				}
			}
			LinkedHashMap<Tuple2<Integer,Integer>,AT_Transaction> atTxs = db.getATTransactionMap().getATTransactions(block.getHeight(db));
			Iterator<AT_Transaction> iter = atTxs.values().iterator(); 
			while ( iter.hasNext() )
			{
				AT_Transaction key = iter.next();
				if ( key.getRecipient().equals( this.getAddress() ) )
				{
					balance = balance.subtract( BigDecimal.valueOf(key.getAmount(), 8) );
				}
			}
				
			block = block.getParent(db);
		}
		
		//DO NOT GO BELOW 0
		if(balance.compareTo(BigDecimal.ZERO) == -1)
		{
			balance = BigDecimal.ZERO.setScale(8);
		}
		
		this.generatingBalance = balance;
	}
	
	public BigDecimal getGeneratingBalance()
	{
		return this.getGeneratingBalance(DBSet.getInstance());
	}
	
	public BigDecimal getGeneratingBalance(DBSet db)
	{	
		//UPDATE
		updateGeneratingBalance(db);
		
		//RETURN
		return this.generatingBalance;
	}
	
	//REFERENCE
	
	public Long getLastReference()
	{
		return this.getLastReference(DBSet.getInstance());
	}
	
	public Long getLastReference(DBSet db)
	{
		return db.getReferenceMap().get(this.getAddress());
	}
	
	public void setLastReference(Long timestamp)
	{
		this.setLastReference(timestamp, DBSet.getInstance());
	}
	
	public void setLastReference(Long timestamp, DBSet db)
	{
		db.getReferenceMap().set(this.getAddress(), timestamp);
	}
	
	public void removeReference() 
	{
		this.removeReference(DBSet.getInstance());
	}
	
	public void removeReference(DBSet db) 
	{
		db.getReferenceMap().delete(this.getAddress());
	}
	
	//TOSTRING
	
	@Override
	public String toString()
	{
		Tuple2<Integer, PersonCls> personRes = this.hasPerson();
		String personStr;
		String addressStr;
		if (personRes == null) {
			personStr = "";
			addressStr = this.getAddress();
		}
		else {
			personStr = personRes.b.getShort();
			addressStr = this.getAddress().substring(0, 8);
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			else if (personRes.a == 0) personStr = "[++]" + personStr;
			else if (personRes.a == 1) personStr = "[+]" + personStr;
		}
		return " {" + NumberAsString.getInstance().numberAsString(this.getConfirmedBalance(FEE_KEY)) + "}"
				+ " " + addressStr + " " + personStr;
	}
	
	public String toString(long key)
	{
		Tuple2<Integer, PersonCls> personRes = this.hasPerson();
		String personStr;
		String addressStr;
		if (personRes == null) {
			personStr = "";
			addressStr = this.getAddress();
		}
		else {
			personStr = personRes.b.getShort();
			addressStr = this.getAddress().substring(0, 8);
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			else if (personRes.a == 0) personStr = "[++]" + personStr;
			else if (personRes.a == 1) personStr = "[+]" + personStr;
		}
		return NumberAsString.getInstance().numberAsString(this.getConfirmedBalance(key))
				+ " {" + NumberAsString.getInstance().numberAsString(this.getConfirmedBalance(FEE_KEY)) + "}"
				+ " " + addressStr + " " + personStr;
	}
	
	//////////
	public String viewPerson() {
		Tuple2<Integer, PersonCls> personRes = this.hasPerson();
		if (personRes == null) {
			return "";
		} else {
			String personStr = personRes.b.toString();
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			//else if (personRes.a == 0) personStr = "[+]" + personStr; // default is permanent ACTIVE
			else if (personRes.a == 1) personStr = "[+]" + personStr;
			return personStr;
		}
		
	}
	
	public String asPerson()
	{
		Tuple2<Integer, PersonCls> personRes = this.hasPerson();
		if (personRes == null) {
			return this.getAddress();
		}
		else {
			String personStr = personRes.b.getShort();
			String addressStr = this.getAddress().substring(0, 8);
			if (personRes.a == -2) personStr = "[-]" + personStr;
			else if (personRes.a == -1) personStr = "[?]" + personStr;
			//else if (personRes.a == 0) personStr = "[+]" + personStr; // default is permanent ACTIVE
			else if (personRes.a == 1) personStr = "[+]" + personStr;
			return addressStr + " " + personStr;
		}
	}
	
	@Override
	public int hashCode()
	{
		return this.getAddress().hashCode();
	}
	
	//EQUALS
	@Override
	public boolean equals(Object b)
	{
		if(b instanceof Account)
		{
			return this.getAddress().equals(((Account) b).getAddress());
		}
		
		return false;	
	}

	// personKey, days, block, reference
	public static Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DBSet db, String address) {
		return db.getAddressPersonMap().getItem(address);				
	}
	public Tuple4<Long, Integer, Integer, Integer> getPersonDuration(DBSet db) {
		return getPersonDuration(db, this.address);
	}
	
	public boolean isPerson(DBSet db) {
		
		// IF DURATION ADDRESS to PERSON IS ENDED
		Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(db);
		if (addressDuration == null) return false;
		// TEST TIME and EXPIRE TIME
		long current_time = NTP.getTime();
		
		// TEST TIME and EXPIRE TIME for PERSONALIZE address
		int days = addressDuration.b;
		if (days < 0 ) return false;
		if (days * (long)86400000 < current_time ) return false;

		// IF PERSON ALIVE
		Long personKey = addressDuration.a;
		Tuple4<Long, Long, Integer, Integer> personDuration = db.getPersonStatusMap().getItem(personKey, ALIVE_KEY);
		// TEST TIME and EXPIRE TIME for ALIVE person
		Long end_date = personDuration.b;
		if (end_date == null ) return true; // permanent active
		if (end_date < current_time + 86400000 ) return false; // - 1 day
		
		return true;
		
	}
	public Tuple2<Integer, PersonCls> hasPerson(DBSet db) {
		
		// IF DURATION ADDRESS to PERSON IS ENDED
		Tuple4<Long, Integer, Integer, Integer> addressDuration = this.getPersonDuration(db);
		if (addressDuration == null) return null;
		// TEST TIME and EXPIRE TIME
		long current_time = NTP.getTime();
		
		// get person
		Long personKey = addressDuration.a;
		PersonCls person = (PersonCls)Controller.getInstance().getItem(ItemCls.PERSON_TYPE, personKey);
		
		// TEST ADDRESS is ACTIVE?
		int days = addressDuration.b;
		// TODO x 1000 ?
		if (days < 0 || days * (long)86400000 < current_time )
			return new Tuple2<Integer, PersonCls>(-1, person);

		// IF PERSON is DEAD
		Tuple4<Long, Long, Integer, Integer> personDead = db.getPersonStatusMap().getItem(personKey, DEAD_KEY);
		if (personDead != null) {
			// person is dead
			return new Tuple2<Integer, PersonCls>(-2, person);
		}

		// IF PERSON is ALIVE
		Tuple4<Long, Long, Integer, Integer> personDuration = db.getPersonStatusMap().getItem(personKey, ALIVE_KEY);
		// TEST TIME and EXPIRE TIME for ALIVE person
		Long end_date = personDuration.b;
		if (end_date == null )
			// permanent active
			return new Tuple2<Integer, PersonCls>(0, person);
		if (end_date < current_time + 86400000 )
			// ALIVE expired
			return new Tuple2<Integer, PersonCls>(-1, person);
		
		return new Tuple2<Integer, PersonCls>(1, person);
		
	}
	public Tuple2<Integer, PersonCls> hasPerson() {
		return hasPerson(DBSet.getInstance());
	}

}