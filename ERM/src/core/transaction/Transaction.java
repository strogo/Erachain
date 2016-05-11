package core.transaction;

// import org.apache.log4j.Logger;
import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.math.MathContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
//import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

//import javax.swing.JFrame;
//import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
//import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import controller.Controller;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.block.Block;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.item.assets.AssetCls;
import database.DBSet;
import lang.Lang;
import settings.Settings;
import utils.DateTimeFormat;

public abstract class Transaction {
	
	//VALIDATION CODE
	public static final int VALIDATE_OK = 1;
	public static final int INVALID_ADDRESS = 2;
	public static final int NEGATIVE_AMOUNT = 3;
	//public static final int NEGATIVE_FEE = 4;
	public static final int NOT_ENOUGH_FEE = 4;
	public static final int NO_BALANCE = 5;
	public static final int INVALID_REFERENCE = 6;
	
	public static final int INVALID_NAME_LENGTH = 7;
	public static final int INVALID_VALUE_LENGTH = 8;
	public static final int NAME_ALREADY_REGISTRED = 9;
	
	public static final int NAME_DOES_NOT_EXIST = 10;
	public static final int INVALID_NAME_CREATOR = 11;
	public static final int NAME_ALREADY_ON_SALE = 12;
	public static final int NAME_NOT_FOR_SALE = 13;
	public static final int BUYER_ALREADY_OWNER = 14;
	public static final int INVALID_AMOUNT = 15;
	public static final int INVALID_SELLER = 16;
	
	public static final int NAME_NOT_LOWER_CASE = 17;
	
	public static final int INVALID_DESCRIPTION_LENGTH = 18;
	public static final int INVALID_OPTIONS_LENGTH = 19;
	public static final int INVALID_OPTION_LENGTH = 20;
	public static final int DUPLICATE_OPTION = 21;
	public static final int POLL_ALREADY_CREATED = 22;
	public static final int POLL_ALREADY_HAS_VOTES = 23;
	public static final int POLL_NOT_EXISTS = 24;
	public static final int OPTION_NOT_EXISTS = 25;
	public static final int ALREADY_VOTED_FOR_THAT_OPTION = 26;
	public static final int INVALID_DATA_LENGTH = 27;
	
	public static final int INVALID_QUANTITY = 28;
	public static final int ASSET_DOES_NOT_EXIST = 29;
	public static final int INVALID_RETURN = 30;
	public static final int HAVE_EQUALS_WANT = 31;
	public static final int ORDER_DOES_NOT_EXIST = 32;
	public static final int INVALID_ORDER_CREATOR = 33;
	public static final int INVALID_PAYMENTS_LENGTH = 34;
	public static final int NEGATIVE_PRICE = 35;
	public static final int INVALID_CREATION_BYTES = 36;
	public static final int AT_ERROR = 10000;
	public static final int INVALID_TAGS_LENGTH = 37;
	public static final int INVALID_TYPE_LENGTH = 38;
	
	public static final int INVALID_PUBLIC_KEY = 40;
	
	public static final int INVALID_RAW_DATA = 41;
	
	public static final int INVALID_DATE = 42;

	public static final int NOT_ENOUGH_RIGHTS = 50;
	public static final int ACCOUNT_NOT_PERSONALIZED = 52;
	public static final int DUPLICATE_KEY = 53;

	public static final int ITEM_DOES_NOT_EXIST = 54;
	public static final int ITEM_ASSET_DOES_NOT_EXIST = 55;
	public static final int ITEM_IMPRINT_DOES_NOT_EXIST = 56;
	public static final int ITEM_NOTE_NOT_EXIST = 57;
	public static final int ITEM_PERSON_NOT_EXIST = 58;
	public static final int ITEM_STATUS_NOT_EXIST = 59;
	public static final int ITEM_UNION_NOT_EXIST = 60;
	public static final int ITEM_DOES_NOT_STATUSED = 61;

	public static final int ITEM_PERSON_LATITUDE_ERROR = 70;
	public static final int ITEM_PERSON_LONGITUDE_ERROR = 71;
	public static final int ITEM_PERSON_RACE_ERROR = 72;
	public static final int ITEM_PERSON_GENDER_ERROR = 73;
	public static final int ITEM_PERSON_SKIN_COLOR_ERROR = 74;
	public static final int ITEM_PERSON_EYE_COLOR_ERROR = 75;
	public static final int ITEM_PERSON_HAIR_COLOR_ERROR = 76;
	public static final int ITEM_PERSON_HEIGHT_ERROR = 77;

	public static final int NOT_YET_RELEASED = 1000;
	
	//TYPES *******
	// universal
	public static final int EXTENDED = 0;
	// genesis
	public static final int GENESIS_ISSUE_ASSET_TRANSACTION = 1;
	public static final int GENESIS_ISSUE_NOTE_TRANSACTION = 2;
	public static final int GENESIS_ISSUE_PERSON_TRANSACTION = 3;
	public static final int GENESIS_ISSUE_STATUS_TRANSACTION = 4;
	public static final int GENESIS_ISSUE_UNION_TRANSACTION = 5;
	public static final int GENESIS_SEND_ASSET_TRANSACTION = 6;
	public static final int GENESIS_SIGN_NOTE_TRANSACTION = 7;
	public static final int GENESIS_CERTIFY_PERSON_TRANSACTION = 8;
	public static final int GENESIS_ASSIGN_STATUS_TRANSACTION = 9;
	public static final int GENESIS_ADOPT_UNION_TRANSACTION = 10;
	// ISSUE ITEMS
	public static final int ISSUE_ASSET_TRANSACTION = 21;
	public static final int ISSUE_IMPRINT_TRANSACTION = 22;
	public static final int ISSUE_NOTE_TRANSACTION = 23;
	public static final int ISSUE_PERSON_TRANSACTION = 24;
	public static final int ISSUE_STATUS_TRANSACTION = 25;
	public static final int ISSUE_UNION_TRANSACTION = 26;
	// USE ITEMS
	public static final int SEND_ASSET_TRANSACTION = 31;
	public static final int SIGN_NOTE_TRANSACTION = 32;
	public static final int CERTIFY_PUB_KEYS_TRANSACTION = 33;
	public static final int SET_STATUS_TRANSACTION = 34;
	public static final int ADOPT_UNION_TRANSACTION = 35;
	// confirms other transactions
	public static final int CONFIRM_TRANSACTION = 40;
	// exchange of assets
	public static final int CREATE_ORDER_TRANSACTION = 50;
	public static final int CANCEL_ORDER_TRANSACTION = 51;
	// voting
	public static final int CREATE_POLL_TRANSACTION =61;
	public static final int VOTE_ON_POLL_TRANSACTION = 62;
	
	public static final int RELEASE_PACK = 70;

	// old
	// public static final int GENESIS_TRANSACTION = 4 + 130;
	public static final int PAYMENT_TRANSACTION = 5 + 130;
	public static final int REGISTER_NAME_TRANSACTION = 6 + 130;
	public static final int UPDATE_NAME_TRANSACTION = 7 + 130;
	public static final int SELL_NAME_TRANSACTION = 8 + 130;
	public static final int CANCEL_SELL_NAME_TRANSACTION = 9 + 130;
	public static final int BUY_NAME_TRANSACTION = 10 + 130;
	public static final int TRANSFER_ASSET_TRANSACTION_OLD = 11 + 130;	
	public static final int ARBITRARY_TRANSACTION = 12 + 130;
	public static final int MULTI_PAYMENT_TRANSACTION = 13 + 130;
	public static final int DEPLOY_AT_TRANSACTION = 14 + 130;
	
	//public static final int ACCOUNTING_TRANSACTION = 26;
	//public static final int JSON_TRANSACTION = 27;

	// FEE PARAMETERS
	public static final long RIGHTS_KEY = AssetCls.ERMO_KEY;

	// FEE PARAMETERS	public static final int FEE_PER_BYTE = 1;

	public static final long FEE_KEY = AssetCls.FEE_KEY;
	public static final int FEE_PER_BYTE = 1;
	public static final BigDecimal FEE_RATE = new BigDecimal(0.00000001);
	public static final float FEE_POW_BASE = (float)1.5;
	public static final int FEE_POW_MAX = 6;

	//RELEASES
	//private static final long ASSETS_RELEASE = 0l;
	//private static final long POWFIX_RELEASE = 0L; // Block Version 3 // 2016-02-25T19:00:00+00:00
							
	static Logger LOGGER = Logger.getLogger(Transaction.class.getName());

	/*/
	public static long getVOTING_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return VOTING_RELEASE;
	}

	public static long getARBITRARY_TRANSACTIONS_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return ARBITRARY_TRANSACTIONS_RELEASE;
	}

	public static int getAT_BLOCK_HEIGHT_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return 1;
		}
		return AT_BLOCK_HEIGHT_RELEASE;
	}
	
	public static int getMESSAGE_BLOCK_HEIGHT_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return 1;
		}
		return MESSAGE_BLOCK_HEIGHT_RELEASE;
	}
	
	public static long getASSETS_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return ASSETS_RELEASE;
	}
	
	public static long getPOWFIX_RELEASE() {
		if(Settings.getInstance().isTestnet()) {
			return Settings.getInstance().getGenesisStamp();
		}
		return POWFIX_RELEASE;
	}
	*/
	
	//PROPERTIES LENGTH
	protected static final int SIMPLE_TYPE_LENGTH = 1;
	protected static final int TYPE_LENGTH = 4;
	//protected static final int PROP_LENGTH = 2; // properties
	public static final int TIMESTAMP_LENGTH = 8;
	public static final int REFERENCE_LENGTH = 64;
	protected static final int DATA_SIZE_LENGTH = 4;
	protected static final int ENCRYPTED_LENGTH = 1;
	protected static final int IS_TEXT_LENGTH = 1;
	protected static final int KEY_LENGTH = 8;
	protected static final int FEE_POWER_LENGTH = 1;
	//protected static final int HKEY_LENGTH = 20;
	protected static final int CREATOR_LENGTH = PublicKeyAccount.PUBLIC_KEY_LENGTH;
	// not need now protected static final int FEE_LENGTH = 8;
	public static final int SIGNATURE_LENGTH = 64;
	protected static final int BASE_LENGTH = TYPE_LENGTH + FEE_POWER_LENGTH + REFERENCE_LENGTH + TIMESTAMP_LENGTH + CREATOR_LENGTH + SIGNATURE_LENGTH;
	// in pack toByte and Parse - reference not included
	protected static final int BASE_LENGTH_AS_PACK = TYPE_LENGTH + CREATOR_LENGTH + /*REFERENCE_LENGTH*/ + SIGNATURE_LENGTH;

		
	protected String TYPE_NAME = "unknown";
	//protected int type;
	protected byte[] typeBytes;
	protected byte[] reference;
	protected BigDecimal fee  = BigDecimal.ZERO.setScale(8); // - for genesis transactions
	//protected BigDecimal fee  = new BigDecimal.valueOf(999000).setScale(8);
	protected byte feePow = 0;
	protected byte[] signature;
	protected long timestamp;
	protected PublicKeyAccount creator;
	
	// need for genesis
	protected Transaction(byte type, String type_name)
	{
		this.typeBytes = new byte[]{type,0,0,0}; // for GENESIS
		this.TYPE_NAME = type_name;
	}
	protected Transaction(byte[] typeBytes, String type_name, PublicKeyAccount creator, byte feePow, long timestamp, byte[] reference)
	{
		this.typeBytes = typeBytes;
		this.TYPE_NAME = type_name;
		this.creator = creator;
		//this.props = props;
		this.timestamp = timestamp;
		this.reference = reference;
		if (feePow < 0 ) feePow = 0;
		else if (feePow > FEE_POW_MAX ) feePow = FEE_POW_MAX;
		this.feePow = feePow;
	}
	protected Transaction(byte[] typeBytes, String type_name, PublicKeyAccount creator, byte feePow, long timestamp, byte[] reference, byte[] signature)
	{
		this(typeBytes, type_name, creator, feePow, timestamp, reference);
		this.signature = signature;
	}

	//GETTERS/SETTERS
	
	public int getType()
	{
		return Byte.toUnsignedInt(this.typeBytes[0]);
	}
	public int getVersion()
	{
		return Byte.toUnsignedInt(this.typeBytes[1]);
	}
	public static int getVersion(byte[] typeBytes)
	{
		return Byte.toUnsignedInt(typeBytes[1]);
	}
	public byte[] getTypeBytes()
	{
		return this.typeBytes;
	}
	public PublicKeyAccount getCreator() 
	{
		return this.creator;
	}

	public long getTimestamp()
	{
		return this.timestamp;
	}
	// for test signature only!!!
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public long getDeadline()
	{
		//24HOUR DEADLINE TO INCLUDE TRANSACTION IN BLOCK
		return this.timestamp + (1000*60*60*24);
	}

	/*
	// TIME
	public Long viewTime() {
		return 0L;
	}
	public Long getTime() {
		return this.viewTime();
	}
	public Long viewTime(Account account)
	{
		return 0L;
	}
	public Long getTime(Account account) {
		return this.viewTime(account);
	}
	*/
	public BigDecimal getAmount() {
		return BigDecimal.ZERO;
	}

	public BigDecimal getAmount(Account account)
	{
		return BigDecimal.ZERO;
	}
	public BigDecimal getAmount(String account)
	{
		return BigDecimal.ZERO;
	}
	
	public BigDecimal getFee(String address)
	{
		if (this.creator != null)
			if (this.creator.getAddress().equals(address))
				return this.fee;
		return BigDecimal.ZERO;
	}
	public BigDecimal getFee(Account account)
	{
		return this.getFee(account.getAddress());
	}

	public BigDecimal getFee()
	{
		return this.fee;
	}	
	public byte getFeePow()
	{
		return this.feePow;
	}	

	public long getAssetKey()
	{
		return -1l;
	}
	
	public byte[] getSignature()
	{
		return this.signature;
	}
	
	public byte[] getReference()
	{
		return this.reference;
	}			

	
	public int calcCommonFee()
	{		
		int fee =  this.getDataLength(false) + 100 * FEE_PER_BYTE;
		if (this.getDataLength(false) > 1000) {
			// add overheat
			fee += (this.getDataLength(false) - 1000) * FEE_PER_BYTE;
		}
		
		return (int) fee;
	}
	
	// get fee
	public int calcBaseFee() {
		return calcCommonFee();
	}
	
	// calc FEE by recommended and feePOW
	public void calcFee()
	{	
		
		BigDecimal fee = new BigDecimal(calcBaseFee())
				.multiply(FEE_RATE)
				.setScale(8, BigDecimal.ROUND_UP);

		if (this.feePow > 0) {
			this.fee = fee.multiply(new BigDecimal(FEE_POW_BASE).pow((int)this.feePow))
					.setScale(8, BigDecimal.ROUND_UP);
		} else {
			this.fee = fee;
		}
	}

	public Block getParent() {
		
		return DBSet.getInstance().getTransactionParentMap().getParent(this.signature);
	}

	// VIEW
	public String viewType() {
		return Byte.toUnsignedInt(typeBytes[0])+"."+Byte.toUnsignedInt(typeBytes[1]);
	}
	public String viewTypeName() {
		return TYPE_NAME;
	}
	public String viewProperies() {
		return Byte.toUnsignedInt(typeBytes[2])+"."+Byte.toUnsignedInt(typeBytes[3]);
	}
	public String viewSubTypeName() {
		return "";
	}

	public String viewCreator() {
		return creator==null?"GENESIS":creator.asPerson();
	}
	public String viewRecipient() {
		return "";
	}
	public String viewReference() {
		return reference==null?"GENESIS":Base58.encode(reference);
	}
	public String viewTimestamp() {
		return timestamp<1000?"GENESIS":DateTimeFormat.timestamptoString(timestamp);
	}
	public int viewSize(boolean asPack) {
		return getDataLength(asPack);
	}
	public String viewFee() {
		return fee.multiply(new BigDecimal(1000)).setScale(5)
				.toPlainString() + "[" + feePow + "]";
	}

	//PARSE/CONVERT
	
	@SuppressWarnings("unchecked")
	protected JSONObject getJsonBase()
	{
		JSONObject transaction = new JSONObject();
		
		transaction.put("type", Byte.toUnsignedInt(this.typeBytes[0]));
		transaction.put("record_type", this.viewTypeName());
		transaction.put("reference", Base58.encode(this.reference));
		transaction.put("signature", Base58.encode(this.signature));
		transaction.put("confirmations", this.getConfirmations());
		if (this.creator == null )
		{
			transaction.put("creator", "genesis");			
		} else {
			transaction.put("fee", this.fee.toPlainString());
			transaction.put("timestamp", this.timestamp);
			transaction.put("creator", this.creator.getAddress());
			transaction.put("version", Byte.toUnsignedInt(this.typeBytes[1]));
			transaction.put("property1", Byte.toUnsignedInt(this.typeBytes[2]));
			transaction.put("property2", Byte.toUnsignedInt(this.typeBytes[3]));
		}
		
		return transaction;
	}
	
	public void sign(PrivateKeyAccount creator, boolean asPack)
	{
		
		// use this.reference in any case and for Pack too
		// nut not with SIGN
		boolean withSign = false;
		byte[] data = this.toBytes( withSign, null );
		if ( data == null ) return;

		this.signature = Crypto.getInstance().sign(creator, data);
		if (!asPack)
			 // need for recalc! if not as a pack
			this.calcFee();
	}

	public abstract JSONObject toJson();
	
	// releaserReference == null - not as pack
	// releaserReference = reference of releaser - as pack
	public byte[] toBytes(boolean withSign, byte[] releaserReference) {

		boolean asPack = releaserReference != null;

		byte[] data = new byte[0];

		//WRITE TYPE
		data = Bytes.concat(data, this.typeBytes);

		if (!asPack) {
			//WRITE TIMESTAMP
			byte[] timestampBytes = Longs.toByteArray(this.timestamp);
			timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
			data = Bytes.concat(data, timestampBytes);
		}
		
		//WRITE REFERENCE - in any case as Pack or not
		data = Bytes.concat(data, this.reference);

		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		if (!asPack) {
			//WRITE FEE POWER
			byte[] feePowBytes = new byte[1];
			feePowBytes[0] = this.feePow;
			data = Bytes.concat(data, feePowBytes);
		}

		//SIGNATURE
		if (withSign) data = Bytes.concat(data, this.signature);
		
		return data;
		
	}
	
	public abstract int getDataLength(boolean asPack);
	
	//VALIDATE
	
	public static boolean isSignatureValid(PublicKeyAccount creator, byte[] data, byte[] signature) {

		if ( signature == null | signature.length != 64 | signature == new byte[64]) return false;
		
		if ( data == null ) return false;

		return Crypto.getInstance().verify(creator.getPublicKey(), signature, data);

	}
	
	public boolean isSignatureValid() {

		if ( this.signature == null || this.signature.length != 64 || this.signature == new byte[64]) return false;
		
		// validation with reference - not as a pack in toBytes - in any case!
		byte[] data = this.toBytes( false, null );
		if ( data == null ) return false;

		return Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data);
	}
	

	public int isValid(byte[] releaserReference)
	{
		return isValid(DBSet.getInstance(), releaserReference);
	}
	
	public int isValid(DBSet db, byte[] releaserReference)
	{
	
		//CHECK IF REFERENCE IS OK
		if(!Arrays.equals(releaserReference==null ? this.creator.getLastReference(db) : releaserReference, this.reference))
		{
			return INVALID_REFERENCE;
		}

		//CHECK CREATOR
		if(!Crypto.getInstance().isValidAddress(this.creator.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF CREATOR HAS ENOUGH MONEY
		if(this.creator.getConfirmedBalance(FEE_KEY, db).compareTo(this.fee) == -1)
		{
			return NOT_ENOUGH_FEE;
		}
						
		return VALIDATE_OK;

	}

	//PROCESS/ORPHAN
	
	public void process(boolean asPack)
	{
		this.process(DBSet.getInstance(), asPack);
	}
	//public abstract void process(DBSet db);
	public void process(DBSet db, boolean asPack)
	{
	
		if (!asPack) {
			this.calcFee();
	
			if (this.fee != null & this.fee.compareTo(BigDecimal.ZERO) > 0) {
				this.creator.setConfirmedBalance(FEE_KEY, this.creator.getConfirmedBalance(FEE_KEY, db)
						.subtract(this.fee), db);

				//UPDATE REFERENCE OF SENDER
				this.creator.setLastReference(this.signature, db);
			}
		}

	}

	public void orphan(boolean asPack)
	{
		this.orphan(DBSet.getInstance(), asPack);
	}
	
	//public abstract void orphan(DBSet db);
	public void orphan(DBSet db, boolean asPack)
	{
		if (!asPack) {
			if (this.fee != null & this.fee.compareTo(BigDecimal.ZERO) > 0) {
				this.creator.setConfirmedBalance(FEE_KEY, this.creator.getConfirmedBalance(FEE_KEY, db).add(this.fee), db);

				//UPDATE REFERENCE OF SENDER
				this.creator.setLastReference(this.reference, db);
			}
		}
	}

	
	//REST
		
	public abstract HashSet<Account> getInvolvedAccounts();
	
	public abstract HashSet<Account> getRecipientAccounts();
		
	public abstract boolean isInvolved(Account account);
 
	public int getSeq()
	{
		if(this.isConfirmed())
		{
			return this.getParent().getTransactionSeq(this.signature);
		}
		return -1;
	}

	@Override 
	public boolean equals(Object object)
	{
		if(object instanceof Transaction)
		{
			Transaction transaction = (Transaction) object;
			
			return Arrays.equals(this.getSignature(), transaction.getSignature());
		}
		
		return false;
	}

	public boolean isConfirmed()
	{
		return this.isConfirmed(DBSet.getInstance());
	}
	
	public boolean isConfirmed(DBSet db)
	{
		return DBSet.getInstance().getTransactionParentMap().contains(this.getSignature());
	}
	
	public int getConfirmations()
	{
		
		try
		{
		//CHECK IF IN TRANSACTIONDATABASE
		if(DBSet.getInstance().getTransactionMap().contains(this))
		{
			return 0;
		}
		
		//CALCULATE CONFIRMATIONS
		int lastBlockHeight = DBSet.getInstance().getHeightMap().get(DBSet.getInstance().getBlockMap().getLastBlockSignature());
		int transactionBlockHeight = DBSet.getInstance().getHeightMap().get(DBSet.getInstance().getTransactionParentMap().getParent(this.signature));
		
		//RETURN
		return 1 + lastBlockHeight - transactionBlockHeight;

		}catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
			return 0;
		}
	}

	public int getBlockVersion1()
	{
		// IF ALREADY IN THE BLOCK. CONFIRMED 
		if(this.isConfirmed())
		{
			return DBSet.getInstance().getTransactionParentMap().getParent(this.getSignature()).getVersion();
		}
		
		// IF UNCONFIRMED
		return Controller.getInstance().getLastBlock().getNextBlockVersion(DBSet.getInstance());	
	}

	public static Map<String, Map<Long, BigDecimal>> subAssetAmount(Map<String, Map<Long, BigDecimal>> allAssetAmount, String address, Long assetKey, BigDecimal amount) 
	{
		return addAssetAmount(allAssetAmount, address, assetKey, BigDecimal.ZERO.setScale(8).subtract(amount));
	}

	public static Map<String, Map<Long, BigDecimal>> addAssetAmount(Map<String, Map<Long, BigDecimal>> allAssetAmount, String address, Long assetKey, BigDecimal amount) 
	{
		Map<String, Map<Long, BigDecimal>> newAllAssetAmount;
		if(allAssetAmount != null) {
			newAllAssetAmount = new LinkedHashMap<String, Map<Long, BigDecimal>>(allAssetAmount);
		} else {
			newAllAssetAmount = new LinkedHashMap<String, Map<Long, BigDecimal>>();
		}

		Map<Long, BigDecimal> newAssetAmountOfAddress;
		
		if(!newAllAssetAmount.containsKey(address)){
			newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>();
			newAssetAmountOfAddress.put(assetKey, amount);
			
			newAllAssetAmount.put(address, newAssetAmountOfAddress);
		} else {
			if(!newAllAssetAmount.get(address).containsKey(assetKey)) {
				newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>(newAllAssetAmount.get(address));
				newAssetAmountOfAddress.put(assetKey, amount);

				newAllAssetAmount.put(address, newAssetAmountOfAddress);
			} else {
				newAssetAmountOfAddress = new LinkedHashMap<Long, BigDecimal>(newAllAssetAmount.get(address));
				BigDecimal newAmount = newAllAssetAmount.get(address).get(assetKey).add(amount);
				newAssetAmountOfAddress.put(assetKey, newAmount);
				
				newAllAssetAmount.put(address, newAssetAmountOfAddress);
			}
		}
		
		return newAllAssetAmount;
	}
	public static Map<String, Map<Long, Long>> addStatusTime(Map<String, Map<Long, Long>> allStatusTime, String address, Long assetKey, Long time) 
	{
		Map<String, Map<Long, Long>> newAllStatusTime;
		if(allStatusTime != null) {
			newAllStatusTime = new LinkedHashMap<String, Map<Long, Long>>(allStatusTime);
		} else {
			newAllStatusTime = new LinkedHashMap<String, Map<Long, Long>>();
		}

		Map<Long, Long> newStatusTimetOfAddress;
		
		if(!newAllStatusTime.containsKey(address)){
			newStatusTimetOfAddress = new LinkedHashMap<Long, Long>();
			newStatusTimetOfAddress.put(assetKey, time);
			
			newAllStatusTime.put(address, newStatusTimetOfAddress);
		} else {
			if(!newAllStatusTime.get(address).containsKey(assetKey)) {
				newStatusTimetOfAddress = new LinkedHashMap<Long, Long>(newAllStatusTime.get(address));
				newStatusTimetOfAddress.put(assetKey, time);

				newAllStatusTime.put(address, newStatusTimetOfAddress);
			} else {
				newStatusTimetOfAddress = new LinkedHashMap<Long, Long>(newAllStatusTime.get(address));
				Long newTime = newAllStatusTime.get(address).get(assetKey) + time;
				newStatusTimetOfAddress.put(assetKey, newTime);
				
				newAllStatusTime.put(address, newStatusTimetOfAddress);
			}
		}
		
		return newAllStatusTime;
	}

}
