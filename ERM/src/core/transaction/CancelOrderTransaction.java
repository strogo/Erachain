package core.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.Order;
import database.ItemAssetBalanceMap;
import database.DBSet;

public class CancelOrderTransaction extends Transaction
{
	private static final byte TYPE_ID = (byte)CANCEL_ORDER_TRANSACTION;
	private static final String NAME_ID = "Cancel Order";
	private static final int ORDER_LENGTH = 64;
	private static final int BASE_LENGTH = Transaction.BASE_LENGTH + ORDER_LENGTH;
	
	private BigInteger order;
	
	public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, BigInteger order, byte feePow, long timestamp, Long reference) {
		super(typeBytes, NAME_ID, creator, feePow, timestamp, reference);
		this.order = order;
	}
	public CancelOrderTransaction(byte[] typeBytes, PublicKeyAccount creator, BigInteger order, byte feePow, long timestamp, Long reference, byte[] signature) {
		this(typeBytes, creator, order, feePow, timestamp, reference);
		this.signature = signature;
		this.calcFee();
	}
	public CancelOrderTransaction(PublicKeyAccount creator, BigInteger order, byte feePow, long timestamp, Long reference, byte[] signature) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, order, feePow, timestamp, reference, signature);
	}
	public CancelOrderTransaction(PublicKeyAccount creator, BigInteger order, byte feePow, long timestamp, Long reference) {
		this(new byte[]{TYPE_ID, 0, 0, 0}, creator, order, feePow, timestamp, reference);
	}
	
	//GETTERS/SETTERS
	//public static String getName() { return "OLD: Cancel Order"; }

	public BigInteger getOrder()
	{
		return this.order;
	}
	
	//PARSE CONVERT
	
	public static Transaction Parse(byte[] data, Long releaserReference) throws Exception
	{	
		boolean asPack = releaserReference != null;

		//CHECK IF WE MATCH BLOCK LENGTH
		if (data.length < BASE_LENGTH_AS_PACK
				| !asPack & data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length " + data.length);
		}
		
		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;

		long timestamp = 0;
		if (!asPack) {
			//READ TIMESTAMP
			byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
			timestamp = Longs.fromByteArray(timestampBytes);	
			position += TIMESTAMP_LENGTH;
		}

		Long reference = null;
		if (!asPack) {
			//READ REFERENCE
			byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			reference = Longs.fromByteArray(referenceBytes);	
			position += REFERENCE_LENGTH;
		} else {
			reference = releaserReference;
		}
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;
		
		byte feePow = 0;
		if (!asPack) {
			//READ FEE POWER
			byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
			feePow = feePowBytes[0];
			position += 1;
		}
		
		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;
				
		//READ ORDER
		byte[] orderBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		BigInteger order = new BigInteger(orderBytes);
		position += ORDER_LENGTH;
				
		return new CancelOrderTransaction(typeBytes, creator, order, feePow, timestamp, reference, signatureBytes);
	}	

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();
								
		//ADD CREATOR/ORDER
		transaction.put("creator", this.creator.getAddress());
		transaction.put("order", Base58.encode(this.order.toByteArray()));
								
		return transaction;	
	}

	//@Override
	public byte[] toBytes(boolean withSign, Long releaserReference)
	{
		byte[] data = super.toBytes(withSign, releaserReference);

		//WRITE ORDER
		byte[] orderBytes = this.order.toByteArray();
		byte[] fill = new byte[ORDER_LENGTH - orderBytes.length];
		orderBytes = Bytes.concat(fill, orderBytes);
		data = Bytes.concat(data, orderBytes);
				
		return data;	
	}

	@Override
	public int getDataLength(boolean asPack) 
	{
		return BASE_LENGTH;
	}
	
	//VALIDATE

	//@Override
	public int isValid(DBSet db, Long releaserReference) 
	{
		//CHECK IF ORDER EXISTS
		Order order = db.getOrderMap().get(this.order);
		if(order== null)
		{
			return ORDER_DOES_NOT_EXIST;
		}
						
		//CHECK IF CREATOR IS CREATOR
		if(!order.getCreator().getAddress().equals(this.creator.getAddress()))
		{
			return INVALID_ORDER_CREATOR;
		}
								
		return super.isValid(db, releaserReference);
	}
	
	//PROCESS/ORPHAN

	//@Override
	public void process(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.process(db, asPack);
				
		//SET ORPHAN DATA
		Order order = db.getOrderMap().get(this.order);
		db.getCompletedOrderMap().add(order);
		
		//UPDATE BALANCE OF CREATOR
		this.creator.setConfirmedBalance(order.getHave(), this.creator.getConfirmedBalance(order.getHave(), db).add(order.getAmountLeft()), db);
		
		//DELETE FROM DATABASE
		db.getOrderMap().delete(this.order);	
	}

	//@Override
	public void orphan(DBSet db, boolean asPack) 
	{
		//UPDATE CREATOR
		super.orphan(db, asPack);
																
		//ADD TO DATABASE
		Order order = db.getCompletedOrderMap().get(this.order);
		db.getOrderMap().add(order);	
		
		//REMOVE BALANCE OF CREATOR
		this.creator.setConfirmedBalance(order.getHave(), this.creator.getConfirmedBalance(order.getHave(), db).subtract(order.getAmountLeft()), db);
		
		//DELETE ORPHAN DATA
		db.getCompletedOrderMap().delete(this.order);
	}

	@Override
	public HashSet<Account> getInvolvedAccounts()
	{
		HashSet<Account> accounts = new HashSet<>();
		accounts.add(this.creator);
		return accounts;
	}
	
	@Override
	public HashSet<Account> getRecipientAccounts()
	{
		return new HashSet<>();
	}
	
	@Override
	public boolean isInvolved(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return true;
		}
		
		return false;
	}

	/*
	@Override
	public BigDecimal Amount(Account account) 
	{
		String address = account.getAddress();
		
		if(address.equals(this.creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8);
		}
		
		return BigDecimal.ZERO;
	}
	*/
	
	//@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();

		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), FEE_KEY, this.fee);

		Order order;

		if(DBSet.getInstance().getCompletedOrderMap().contains(this.order))
		{
			order =  DBSet.getInstance().getCompletedOrderMap().get(this.order);
		}
		else
		{
			order =  DBSet.getInstance().getOrderMap().get(this.order);
		}	
		
		assetAmount = addAssetAmount(assetAmount, this.creator.getAddress(), order.getHave(), order.getAmountLeft());
		
		return assetAmount;
	}
	public int calcBaseFee() {
		return calcCommonFee();
	}
}