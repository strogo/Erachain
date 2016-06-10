package core.item.unions;

//import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
 import org.apache.log4j.Logger;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import core.account.Account;
import core.crypto.Base58;

public class Union extends UnionCls {
	
	private static final int TYPE_ID = UnionCls.UNION;

	public Union(Account creator, String name, long birthday, long parent, String description)
	{
		super(TYPE_ID, creator, name, birthday, parent, description);
	}
	public Union(byte[] typeBytes, Account creator, String name, long birthday, long parent, String description)
	{
		super(typeBytes, creator, name, birthday, parent, description);
	}

	//GETTERS/SETTERS
	public String getItemSubType() { return "union"; }

	//PARSE
	// includeReference - TRUE only for store in local DB
	public static Union parse(byte[] data, boolean includeReference) throws Exception
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
		
		if(descriptionLength < 1 || descriptionLength > 4000)
		{
			throw new Exception("Invalid description length");
		}
		
		byte[] descriptionBytes = Arrays.copyOfRange(data, position, position + descriptionLength);
		String description = new String(descriptionBytes, StandardCharsets.UTF_8);
		position += descriptionLength;
		
		//READ BIRTDAY
		byte[] birthdayBytes = Arrays.copyOfRange(data, position, position + BIRTHDAY_LENGTH);
		long birthday = Longs.fromByteArray(birthdayBytes);	
		position += BIRTHDAY_LENGTH;

		//READ BIRTDAY
		byte[] parentBytes = Arrays.copyOfRange(data, position, position + BIRTHDAY_LENGTH);
		long parent = Longs.fromByteArray(parentBytes);	
		position += PARENT_LENGTH;

		byte[] reference = null;
		if (includeReference)
		{
			//READ REFERENCE
			reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
			position += REFERENCE_LENGTH;
		}
		
		//RETURN
		Union note = new Union(typeBytes, creator, name, birthday, parent, description);
		if (includeReference)
		{
			note.setReference(reference);
		}

		return note;
	}
	
}