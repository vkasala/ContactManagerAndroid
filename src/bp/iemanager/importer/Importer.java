package bp.iemanager.importer;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import bp.iemanager.StringUtils;
import bp.iemanager.csvcontact.CsvContact;


import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContentProviderOperation.Builder;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

/**
 * Class representing importing of CSV Contacts to Android system.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public abstract class Importer {
	
	/**
	 *	String for debug printing 
	 */
	private final static String TAG = Importer.class.getSimpleName(); 
	
	/**
	 *  Constants for recognizing duplicate contacts marked by IE Manager exporter
	 */
	protected final String NOTE_UNIQUE_NAME = "#NOTE@DUPLICATE@CONTACT#";
	
	/**
	 * Position of email match flag
	 */
	private static final int EMAIL_MATCH = 0;
	
	/**
	 * Position of phone match flag
	 */
	private static final int PHONE_MATCH = 1;
	
	/**
	 * Reference to context of activity to be able to have access to ContentProvider
	 */
	protected Context context;
	
	/**
	 * Type of import type (ThunderbirdCSV, OutlookCSV)
	 */
	protected int importType;
	
	/**
	 * Account type to which new Contact should be joined
	 */
	protected Account newContactType;
	
	/**
	 * Selected USER Match Type
	 */
	protected MatchType matchType;
	
	/**
	 * Array representing at least which pairs of fields has to match
	 * Street, Pobox, City, State, Postal Code, Country
	 */
	protected boolean[] minMatchAddressFields;
	
	/**
	 * Array representing at least which pairs of fields has to match
	 * Street, Pobox, City, State, Postal Code, Country
	 */
	protected boolean[] minMatchContactFields;
	
	/**
	 * CSV file line contact that should be Sync
	 */
	protected CsvContact csvLineContact;
	
	/**
	 * Set of DEFINITLY MATCHED for SYNC Raw Contacts IDs
	 */
	protected HashSet<Long> setOfSureEqualRawContactId;
	
	/**
	 * Set of matched phones Raw Contacts IDs
	 */
	protected HashSet<Long> setOfSureEqualRawContactIdPhone;
	
	/**
	 * Set of matched emails Raw Contacts IDs
	 */
	protected HashSet<Long> setOfSureEqualRawContactIdEmail;
	
	/**
	 * Set of possible Raw Contacts IDs
	 */
	protected HashSet<Long> setOfPossibleEqualRawContactId;
	
	/**
	 * Set containing "sure matches of display names" Contacts IDs 
	 */
	protected HashSet<Long> setOfDisplayNameEqualContactsId;
	
	/**
	 * Reference where will be added Pair of contacts which are marked as Possible Match
	 */
	private ArrayList<PossibleEqualContacts> listOfPossibleEqualContacts;
	
	/**
	 * Selection string of Account names for query RawContact with appropriate account type
	 */
	protected String selectionForAccounts = null;
	
	/**
	 * Selection args of Accounts for query RawContact with appropriate account type
	 */
	protected String[] selectionArgsForAccounts = null;
	
	/**
	 * Flag indicating turn on/off debug printing
	 */
	public boolean debug = true;
	
	
	/**
	 * Constructor to Sync Android Contact with csvLine.
	 * @param _ctx - context of activity to have access to contentProvider()
	 * @param _csvLine - csvLine containing fields of file contact
	 */	
	public Importer(Context _ctx, CsvContact _csvLine, MatchType _type, boolean[] match_options) {
		context = _ctx;
		importType = _csvLine.getType();
		csvLineContact = _csvLine;
		matchType = _type;
		minMatchAddressFields = match_options;
	}
	
	/**
	 * Constructor to Sync Android Contact with csvLine.
	 * @param _ctx - context of activity to have access to contentProvider()
	 * @param _csvLine - csvLine containing fields of file contact
	 * @param _list - reference to list of Possible Contact where should be added contacts where can't be definitely determine if they are same
	 * @param _options - object containing selected USER OPTIONS
	 */
	public Importer(Context _ctx, CsvContact _csvLine, ArrayList<PossibleEqualContacts> _list, UserSelectedOptions _options) {
		this(_ctx, _csvLine, _options.type, _options.minMatchAddressFieldds);
		newContactType = _options.newContactType;
		
		// Two main Set to which we devide find out contacts
		setOfSureEqualRawContactId = new HashSet<Long>();
		setOfPossibleEqualRawContactId = new HashSet<Long>();
		
		listOfPossibleEqualContacts = _list;
		
		minMatchContactFields = _options.minMatchContactFields;
		
		setOfDisplayNameEqualContactsId = new HashSet<Long>();
		
		if(isSetEmailSynch())
			setOfSureEqualRawContactIdEmail = new HashSet<Long>();
		else 
			setOfSureEqualRawContactIdEmail = null;
		if(isSetPhoneSynch())
			setOfSureEqualRawContactIdPhone = new HashSet<Long>();
		else 
			setOfSureEqualRawContactIdPhone = null;
		
		
		
		makeSelectionParameters(_options.chooseAccounts);
	}
	
	/**
	 * Starting import contacts
	 */
	public void startImport() {
		findContacts(createDisplaysNames());
	}
	
	protected abstract void checkStructureNameDeeply();
	protected abstract void sortRawContactPhone(String phoneNumber, long rawContactID);
	protected abstract List<String> createDisplaysNames();
	protected abstract void sortRawContactEmail(String email, long rawContactID);
	public abstract void updateRawContact(long rawContactId);
	public abstract void createNewContact(CsvContact csvContact, String accName, String accType);
	protected abstract void addNewItemsFromCsvToRawContact(ArrayList<ContentProviderOperation> ops, 
									long rawContactId);
	
	/**
	 * This function sort ID's of RawContacts into two Sets. 
	 * First: 'Definitlly equals contacts' SET. Here are ID's added if they have common name and also they have at least equal email address. 
	 * Second: 'Maybe the equals contacts' SET. There are ID's added if they matches only on the equal name.
	 * Firstly this method find out contacts ID's in RawContacts TABLE with selection: Contacts can't be marked as DELETED and is of the same account type 
	 * as selected by USER. Then it's searches Data TABLE of obtained RawContact's ID for equal email. If at least one email is equal as CSV email, then 
	 * this RawContact's is marked as Definitelly and is ready for synchronization of this two contacts. Otherwise is marked as Maybe the same contact.
	 * @param contactIdSet - set of Contacts ID's
	 */
	protected void findInRawContacts(HashSet<Long> contactsIdSet) {
		Cursor c = null;
		for (Long long1: contactsIdSet) {
			try {		
				// Synch only those with match account
				c = queryForRawContactIDsOfContacts(long1);
				if(debug) Log.v(TAG, "Found Raw Contacts: "+ long1.longValue() + " count: " + c.getCount());
								
				for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
					long rawContactID = getColumnLong(c, RawContacts._ID);
					
					// We synchronize only based on match of names 
					if(isNotSetBothSynch()) {
						setOfSureEqualRawContactId.add(rawContactID);
						break;
					}
					// Searching for at least one equal email address
					if(isSetEmailSynch()) {
						Cursor cr = null;
						try {
							cr = queryForEmailsOfRawContact(rawContactID);
							// No founded email, then this Raw Contact ID is included to PossibleMatch SET
							if(cr.getCount() == 0) {
								if(debug) Log.v(TAG, "Found Raw Contacts: " + rawContactID + " possible");
								setOfPossibleEqualRawContactId.add(new Long(rawContactID));
							}
							else 
								// We found some email iterate over them and compare them against CSV emails
								for(cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
									String email = getColumnIfNull(cr, Email.DATA);
									if(!email.equals(""))
										sortRawContactEmail(email, rawContactID);
								}
						} finally {
							if(cr !=null)
								cr.close();
						}
					}
					// Searching for at least one equal email address
					if(isSetPhoneSynch()) {
						Cursor cr = null;
						try {
							cr = queryForPhoneOfRawContact(rawContactID);
							// No founded email, then this Raw Contact ID is included to PossibleMatch SET
							if(cr.getCount() == 0) {
								if(debug) Log.v(TAG, "Found Raw Contacts: " + long1.longValue() + " possible");
								setOfPossibleEqualRawContactId.add(rawContactID);
							}
							else
								// We found some email iterate over them and compare them against CSV emails
								for(cr.moveToFirst(); !cr.isAfterLast(); cr.moveToNext()) {
									String phoneNumber = getColumnIfNull(cr, Phone.NUMBER);
									if(!phoneNumber.equals(""))
										sortRawContactPhone(phoneNumber, rawContactID);
								}
						} finally {
							if(cr !=null)
								cr.close();
						}
					}
				}
			} finally {
				if(c != null)
					c.close();
			}
		}
	}
	
	
	
	/**
	 * Method first searches for Contact.ID according to Name, then checks if this ID's are of appropriate account type
	 * and then rearrange all sets and checks whether were found pair of contacts to SYNC or weren't then it creates new
	 * contact from CSV file line. If there were found possible matches then it stores them into Possible Match Contacts.
	 * @param list - list of display names
	 */
	protected void findContacts(List<String> list) {
			
		Cursor c = null;
		String selection = buildSelectionString(list.size(), Contacts.DISPLAY_NAME, " OR ");
		setOfDisplayNameEqualContactsId.clear();
		// Array Conversion
		String[] strings = new String[list.size()];
		strings = list.toArray(strings);
		   
		try {
			c = queryForContactDisplayName(selection, strings);
			
			if(c.getCount() > 0) {
				// At least one contact with display name is matched
				for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
					if(debug) Log.v(TAG, c.getString(c.getColumnIndex(Contacts.DISPLAY_NAME))  + " ID: " + String.valueOf(getColumnLong(c, Contacts._ID)));
					setOfDisplayNameEqualContactsId.add(new Long(getColumnLong(c, Contacts._ID)));	
				}
			}
			c.close();
			
			checkStructureNameDeeply();
			
			// If possible query obtained ID's with the selected accounts and then against email of this contact and phone if they are passed
			if(!setOfDisplayNameEqualContactsId.isEmpty())
				findInRawContacts(setOfDisplayNameEqualContactsId);
			
			// If at least one options is set, then is need to make new set
			if(isSetAtLeastOneSynch())
				rearrangePartSetsToOne();
			
			// If both sets are empty, then this indicates that we didn't find match contact for CSV Contact, 
			// thus we will create new contact
			if(setOfSureEqualRawContactId.isEmpty() && setOfPossibleEqualRawContactId.isEmpty()) {
				// Kontakt nenajdeny, vytvaram novy
				if(debug) Log.v(TAG, "Kontakt nenajdeny vytvaram novy");
				createNewContact(csvLineContact, newContactType.name, newContactType.type);
			} 
			// I found a match and now we synchronize this two contacts
			if(!setOfSureEqualRawContactId.isEmpty()) {
				for (Iterator<Long> iterator = setOfSureEqualRawContactId.iterator(); iterator.hasNext();) {
					Long long1 = (Long) iterator.next();
					if(debug) Log.v(TAG, "Updatujem kontakt");
					updateRawContact(long1.longValue());
				}
			}
			if(!setOfPossibleEqualRawContactId.isEmpty() ) {
				// Now we erase all sureEqualContacts from PossibleContact if we won't to synchronize contacts
				setOfPossibleEqualRawContactId.removeAll(setOfSureEqualRawContactId);
				//eraseEqualsItems(setOfPossibleEqualRawContactId, setOfSureEqualRawContactId);
				// If we found some possible matches we will add it int List of possible matches
				addObjectsToList(setOfPossibleEqualRawContactId, listOfPossibleEqualContacts, csvLineContact);
			}
		} finally {
			if(c != null)
				c.close();
		}
	}
	
	/**
	 * Method correctly rearrange RawContact ID's into SureEqualRawContact SET and POSSIBLE RAW CONTACT SET
	 * according to OPTIONS selected by USER.
	 */
	private void rearrangePartSetsToOne() {
		if(isSetBothSynch()){
			// Make cojunction of two sets if they aren't empty
			if(!setOfSureEqualRawContactIdEmail.isEmpty() && !setOfSureEqualRawContactIdPhone.isEmpty()) {
				for (Long rawId : setOfSureEqualRawContactIdEmail) {
					if(setOfSureEqualRawContactIdPhone.contains(rawId))
						setOfSureEqualRawContactId.add(rawId);
				}
				// now remove from them them common elments
				setOfSureEqualRawContactIdEmail.removeAll(setOfSureEqualRawContactId);
				setOfSureEqualRawContactIdPhone.removeAll(setOfSureEqualRawContactId);
				// at last add it in possibleSet
				setOfPossibleEqualRawContactId.addAll(setOfSureEqualRawContactIdPhone);
				setOfPossibleEqualRawContactId.addAll(setOfSureEqualRawContactIdEmail);
			}
			else if(!setOfSureEqualRawContactIdEmail.isEmpty()) {
				setOfPossibleEqualRawContactId.addAll(setOfSureEqualRawContactIdEmail);
			}
			else if(!setOfSureEqualRawContactIdPhone.isEmpty()) {
				setOfPossibleEqualRawContactId.addAll(setOfSureEqualRawContactIdPhone);
			}
		}
		else if(isSetEmailSynch()) {
			// copy email possible match  Set to sure set
			setOfSureEqualRawContactId.addAll(setOfSureEqualRawContactIdEmail);
			setOfSureEqualRawContactIdEmail.clear();
		}
		else {
			// copy phone possible match set to Sure match Set
			setOfSureEqualRawContactId.addAll(setOfSureEqualRawContactIdPhone);
			setOfSureEqualRawContactIdPhone.clear();
		}
	}
	
	/**
	 * Method query for Contacts IDs and DispayNames with strings
	 * @param selection - selection for query
	 * @param strings - argument of selections
	 * @return cursor full of match Contacts.ID and Display_name
	 */
	protected Cursor queryForContactDisplayName(String selection, String[] strings) {
		return context.getContentResolver().query(Contacts.CONTENT_URI, 
				new String[] {Contacts._ID, Contacts.DISPLAY_NAME}, 
				selection, 
				strings, 
				null);
	}
	
	/**
	 * Method query for RawContacts ID with matched selected accounts, Contact ID and marked as NOT_DELETED
	 * @param long1 Contact_ID of requested RawContacts
	 * @return cursor full of match RawContacts._ID
	 */
	protected Cursor queryForRawContactIDsOfContacts(Long long1) {
		return context.getContentResolver().query(RawContacts.CONTENT_URI, 
				new String[] {RawContacts._ID}, 
				RawContacts.DELETED + " = ? AND " + RawContacts.CONTACT_ID + " = ? " + " AND "+ "( " + selectionForAccounts + " )", 
				concat(new String[] {"0", String.valueOf(long1.longValue())}, selectionArgsForAccounts), 
				null);
	}
	
	/**
	 * Method query for RawContacts ID with matched selected accounts, Contact ID and marked as NOT_DELETED
	 * @param long1 Contact_ID of requested RawContacts
	 * @return cursor full of match RawContacts._ID
	 */
	protected Cursor queryForEmailsOfRawContact(long rawContactID) {
		return context.getContentResolver().query(Email.CONTENT_URI,
				new String[] {Email.DATA}, // In this column the email is stored
				Data.RAW_CONTACT_ID + " = ?",			
				new String[] {String.valueOf(rawContactID)},			
				null);
	}
	
	/**
	 * Method query for RawContacts ID with matched selected accounts, Contact ID and marked as NOT_DELETED
	 * @param long1 Contact_ID of requested RawContacts
	 * @return cursor full of match RawContacts._ID
	 */
	protected Cursor queryForPhoneOfRawContact(long rawContactID) {
		return context.getContentResolver().query(Phone.CONTENT_URI,
				new String[] {Phone.NUMBER}, // In this column the email is stored
				Data.RAW_CONTACT_ID + " = ?",			
				new String[] {String.valueOf(rawContactID)},			
				null);
	}
	
	/**
	 * Method query for RawContacts ID with matched selected accounts, Contact ID and marked as NOT_DELETED
	 * @param long1 Contact_ID of requested RawContacts
	 * @return cursor full of match RawContacts._ID
	 */
	protected Cursor queryForRawContactsDetails(long rawContactID) {
		Uri rawContactsUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactID);
		
		Uri entityUri = Uri.withAppendedPath(rawContactsUri, Entity.CONTENT_DIRECTORY);
		return context.getContentResolver().query(entityUri, 
				null, //TODO Dorobit selection, iba tie ktore potrebujem... nech je to rychlejsie 
				null, 
				null, 
				null);
	}
	
	/**
	 * Method query for RawContacts ID with matched selected accounts, Contact ID and marked as NOT_DELETED
	 * @param long1 Contact_ID of requested RawContacts
	 * @return cursor full of match RawContacts._ID
	 */
	protected Cursor queryForStructureName() {
		return context.getContentResolver().query(Data.CONTENT_URI,
				new String[] {Data.CONTACT_ID, StructuredName.FAMILY_NAME, StructuredName.MIDDLE_NAME, StructuredName.SUFFIX, StructuredName.GIVEN_NAME, StructuredName.PREFIX, StructuredName.DISPLAY_NAME}, // In this column the email is stored
				Data.MIMETYPE + " = ?",			
				new String[] {StructuredName.CONTENT_ITEM_TYPE},			
				null);
	}
	
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * Works with WebSite fields.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param index - index CSV field, which should be there stored. It was created because of re-usabilty
	 * @param oppType - type of operations. UPDATE or INSERT
	 * return created builder.
	 */
	protected ContentProviderOperation createWebSite(long rawIndex, int index, int oppType) {
		
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, Website.CONTENT_ITEM_TYPE);
		bd.withValue(Website.URL, csvLineContact.getString(index));		
				
		return bd.build();
	}
	
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * Works with Note fields.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param index - index CSV field, which should be there stored. It was created because of re-usabilty
	 * @param oppType - type of operations. UPDATE or INSERT
	 * return created builder.
	 */
	protected ContentProviderOperation createNoteBuilder(long rawIndex, int index, int oppType) {
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);
		bd.withValue(Note.NOTE, csvLineContact.getString(index));
		
		return bd.build();
	}
		
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * Works with Phone fields.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param index - index CSV field, which should be there stored. It was created because of re-usabilty
	 * @param phoneType - type of new phone 
	 * @param oppType - type of operations. UPDATE or INSERT
	 * return created builder.
	 */
	protected ContentProviderOperation createPhoneBuilder(long rawIndex, int index, int phoneType, int oppType) {
		
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		bd.withValue(Phone.TYPE, phoneType);
		bd.withValue(Phone.NUMBER, csvLineContact.getString(index));		
				
		return bd.build();
	}
	
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * Works with Email fields.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param index - index CSV field, which should be there stored. It was created because of re-usabilty
	 * @param oppType - type of operations. UPDATE or INSERT
	 * return created builder.
	 */	
	protected ContentProviderOperation createEmailBuilder(long rawIndex, int index, int oppType) {
		
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
		bd.withValue(Email.TYPE, Email.TYPE_WORK);
		bd.withValue(Email.DATA, csvLineContact.getString(index));		
				
		return bd.build();
	}
	
	/**
	 * Get string value from the column of cursor
	 * @param c - from which cursor
	 * @param columnName - which column
	 * @return return value
	 */
	protected static String getColumnString(Cursor c, String columnName) {
		return c.getString(c.getColumnIndex(columnName));
	}
	
	/**
	 * Get long value from the column of cursor
	 * @param c - from which cursor
	 * @param columnName - which column
	 * @return return value
	 */
	protected static long getColumnLong(Cursor c, String columnName) {
		return c.getLong(c.getColumnIndex(columnName));
	}
	
	/**
	 * Get boolean value from the column of cursor
	 * @param c - from which cursor
	 * @param columnName - which column
	 * @return return value
	 */
	protected static boolean isColumnNull(Cursor c, String columnName) {
		return c.isNull(c.getColumnIndex(columnName));
	}
	
	/**
	 * Get Int value from the coulmn of cursor
	 * @param c - from which cursor
	 * @param columnName - which column
	 * @return return value
	 */
	protected static int getColumnInt(Cursor c, String columnName) {
		return c.getInt(c.getColumnIndex(columnName));
	}
	
	/**
	 * If column of cursor doesn't contain value return empty string else return coulmn string
	 * @param c - from which cursor
	 * @param columnName - which column
	 * @return return value
	 */
	protected static String getColumnIfNull(Cursor c, String columnName) {
		if(isColumnNull(c, columnName)) 
			return "";
		else 
			return getColumnString(c, columnName);
	}
	
	/**
	 * Make selection string for query
	 * @param count - count of string items
	 * @param foundedItem - string containg column which we want to find in cursor
	 * @param boolOpp - operation between this selection
	 * @return
	 */
	protected static String buildSelectionString(int count, String foundedItem, String boolOpp) {
		String selection = "";
		for (int i = 0; i < count; i++) {
			if(i == 0)
				selection += foundedItem + " = ? ";
			else 
				selection += " " + boolOpp + " " + foundedItem + " = ? ";
		}
		return selection;
	}
	
	/**
	 * Checks option Min match contact for email
	 * @return true/false
	 */
	private boolean isSetEmailSynch() {
		return minMatchContactFields[EMAIL_MATCH];
	}
	
	/**
	 * Checks option Min match contact for phone
	 * @return true/false
	 */
	private boolean isSetPhoneSynch() {
		return minMatchContactFields[PHONE_MATCH];
	}
	
	/**
	 * Checks option if both email/phone are set
	 * @return true/false
	 */
	private boolean isNotSetBothSynch() {
		return minMatchContactFields[PHONE_MATCH] == false && minMatchContactFields[EMAIL_MATCH] == false;
	}
	
	private boolean isSetBothSynch() {
		return minMatchContactFields[PHONE_MATCH] == true && minMatchContactFields[EMAIL_MATCH] == true;
	}
	
	/**
	 * Checks option if AT LEAST email/phone are set
	 * @return true/false
	 */
	private boolean isSetAtLeastOneSynch() {
		return minMatchContactFields[PHONE_MATCH] || minMatchContactFields[EMAIL_MATCH];
	}
	
	/**
	 * Method add new Object to the list of Possible Contact. This new object is make from item of set and csvLine.
	 * @param set - set from which we pick items
	 * @param list - list where we add new items
	 * @param csvLine - item which we add to list
	 */
	protected void addObjectsToList(HashSet<Long> set, ArrayList<PossibleEqualContacts> list, CsvContact csvLine) {
		for (Iterator<Long> iterator = set.iterator(); iterator.hasNext();) {
			Long rawContactId = (Long) iterator.next();
			list.add(new PossibleEqualContacts(rawContactId.longValue(), csvLine));
		}
	}
	
	/**
	 * Concatenation of two Array of strings.
	 * @param A - first string
	 * @param B - secont string
	 * @return new array of strings containg items from both arrays
	 */
	public static String[] concat(String[] A, String[] B) {
	   String[] C = new String[A.length + B.length];
	   System.arraycopy(A, 0, C, 0, A.length);
	   System.arraycopy(B, 0, C, A.length, B.length);
	   return C;
	}
			
	/**
	 * Make selection parameter for filtering RawContact according to account.
	 * Needs to add them to attributes selectionForAccounts and selectionArgsForAccounts
	 * @param synchronizeAccounts - set of picked accounts
	 */
	protected void makeSelectionParameters(HashSet<Account> synchronizeAccounts) {
		String selection = " ";
		int i = 0, j = 0;
		String[] selectionArgs = new String[synchronizeAccounts.size()*2];
		
		for (Iterator<Account> iterator = synchronizeAccounts.iterator(); iterator
				.hasNext();) {
			Account account = (Account) iterator.next();
			selectionArgs[j++] = account.name;
			selectionArgs[j++] = account.type;
			
			if(i == 0)
				selection +=  " ( " + RawContacts.ACCOUNT_NAME + " = ? AND " + RawContacts.ACCOUNT_TYPE + " = ? ) ";
			else 
				selection +=   " OR "+ " ( " + RawContacts.ACCOUNT_NAME + " = ? AND " + RawContacts.ACCOUNT_TYPE + " = ? ) ";
			i++;
		}
		selectionForAccounts = selection;
		selectionArgsForAccounts = selectionArgs;
		return;
	}
	
	/**
	 * Compare two strings according to Selected MatchType
	 * @param s1 - first string
	 * @param s2 - second string
	 * @param type - compare type
	 * @return return boolean according to compare type
	 */
	protected static boolean equal(String s1, String s2, MatchType type) {
		switch (type) {
			case CASE_SENSITIVE:
				return s1.equals(s2);
			case IGNORE_CASE:
				return s1.equalsIgnoreCase(s2);
			case IGNORE_ACCENTS_CASE_SENSITIVE:
				return StringUtils.convertNonAscii(s1).equals(StringUtils.convertNonAscii(s2));
			case IGNORE_ACCENTS_AND_CASES:
				return StringUtils.convertNonAscii(s1).equalsIgnoreCase(StringUtils.convertNonAscii(s2));
		}
		return false;
	}
}
