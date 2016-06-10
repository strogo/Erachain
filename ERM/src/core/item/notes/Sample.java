package core.item.notes;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
 import org.apache.log4j.Logger;

import com.google.common.primitives.Ints;

import core.account.Account;
import core.crypto.Base58;

public class Sample extends NoteCls {
	
	private static final int TYPE_ID = NoteCls.SAMPLE;

	public Sample(Account creator, String name, String description)
	{
		super(TYPE_ID, creator, name, description);
	}
	public Sample(byte[] typeBytes, Account creator, String name, String description)
	{
		super(typeBytes, creator, name, description);
	}

	//GETTERS/SETTERS
	public String getItemSubType() { return "sample"; }

	//PARSE
	// includeReference - TRUE only for store in local DB
	public static Sample parse(byte[] data, boolean includeReference) throws Exception
	{	

		// READ TYPE
		byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
		int position = TYPE_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = new Account(Base58.encode(creatorBytes));
		position += CREATOR_LENGTH;
		
		//READ NAME
		//byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		//int nameLength = Ints.fromByteArray(nameLengthBytes);
		//position += NAME_SIZE_LENGTH;
		int nameLength = Byte.toUnsignedInt(data[position]);
		position ++;
		
		if(nameLength < 1 || nameLength > MAX_NAME_LENGTH)
		{
			throw new Exception("Invalid name length");
		}
		
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
				
		//READ DESCRIPTION
		byte[] descriptionLengthBytes = Arrays.copyOfRange(data, position, position + DESCRIPTION_SIZE_LENGTH);
		int descriptionLength = Ints.fromByteArray(descriptionLengthBytes);
		position += DESCRIPTION_SIZE_LENGTH;
		
		if(descriptionLength > 4000)
		{
			throw new Exception("Invalid description length");
		}
		
		byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
		String description = new String(descriptionBytes, StandardCharsets.UTF_8);
		position += descriptionLength;
		
		byte[] reference = null;
		if (includeReference)
		{
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			position += REFERENCE_LENGTH;
		}
		
		//RETURN
		Sample note = new Sample(typeBytes, creator, name, description);
		if (includeReference)
		{
			note.setReference(reference);
		}

		return note;
	}
	
}