package bp.iemanager.importer;

import java.util.ArrayList;
import java.util.List;

import bp.iemanager.StringUtils;
import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.ThunderbirdConstants;


import android.content.ContentProviderOperation;

import android.content.Context;
import android.content.ContentProviderOperation.Builder;
import android.database.Cursor;

import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

/**
 * Class representing importing of Thunderbird CSV Contacts into Android System.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class ThunderbirdImporter extends Importer {
	
	/**
	 * TAG for printing debug notes
	 */
	private static final String TAG = "ImportThunderbirdContacts";
	
	/**
	 * Object holding all abstract view on fields between Android and File Contact fields
	 */
	protected UpdateThunderbirdCsvItems updateThunderbirdItems;
	
	/**
	 * Constructor to Sync Android Contact with csvLine.
	 * @param _ctx - context of activity to have access to contentProvider()
	 * @param _csvLine - csvLine containing fields of file contact
	 */
	public ThunderbirdImporter(Context _ctx, CsvContact _csvLine, MatchType _type, boolean[] match_options) {
		super(_ctx, _csvLine, _type, match_options);
		if(importType == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT)
			updateThunderbirdItems = new UpdateThunderbirdCsvItems(csvLineContact);
		
	}
	
	/**
	 * Constructor to Sync Android Contact with csvLine.
	 * @param _ctx - context of activity to have access to contentProvider()
	 * @param _csvLine - csvLine containing fields of file contact
	 * @param _list - reference to list of Possible Contact where should be added contacts where can't be definitely determine if they are same
	 * @param _options - object containing selected USER OPTIONS
	 */
	public ThunderbirdImporter(Context _ctx, CsvContact _csvLine, ArrayList<PossibleEqualContacts> _list, UserSelectedOptions _options) {
		super(_ctx, _csvLine, _list, _options);
		if(importType == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT)
			updateThunderbirdItems = new UpdateThunderbirdCsvItems(csvLineContact);
	}

	/**
	 * Method add rawContacID to Sure equal Set if at least one email number  match else
	 * is add to possible equal Set .
	 * @param email - finding email
	 * @param rawContactID - if of raw contact to which phoneNumber belongs to
	 */
	protected void sortRawContactEmail(String email, long rawContactID) {
		
		// If some of Emails matches then declared this pair of Contacts for Equal and they will be synchronized
		if(csvLineContact.findEmailThunderbird(email, matchType)) {
			if(debug) Log.v(TAG, "Raw Contact: " + email + " / " + rawContactID + " DEFINITELY");
			setOfSureEqualRawContactIdEmail.add(new Long(rawContactID));
		}
		// For duplicate contact saved in CSV file, it's necessary to CHECK NOTE for unique string 
		else if(csvLineContact.getString(ThunderbirdConstants.T_NOTE) != null && csvLineContact.getString(ThunderbirdConstants.T_NOTE).equals(NOTE_UNIQUE_NAME)) {
			// If it's NOTE is created with IE Manager's Exporter, then we must checked EQUALS EMAIL's to declare as MATCHED
			if(csvLineContact.findEmailCustomThunderbird(email, matchType)) {
				if(debug) Log.v(TAG, "Raw Contact: " +  email + " / " + rawContactID + " DEFINITELY");
				setOfSureEqualRawContactIdEmail.add(new Long(rawContactID));
			}
		}
		// Else declared this pair as Possible Equal Contacts we will
		else {
			if(debug) Log.v(TAG, "Raw Contact: " + email + " / "  + rawContactID + " MAYBE");
			setOfPossibleEqualRawContactId.add(new Long(rawContactID));
		}
	}
	
	/**
	 * Method add rawContacID to Sure equal Set if at least one phone number  match else
	 * is add to possible equal Set 
	 * @param phoneNumber - finding phoneNumber
	 * @param rawContactID - if of raw contact to which phoneNumber belongs to
	 */
	protected void sortRawContactPhone(String phoneNumber, long rawContactID) {
		// If some of phone matches then declared this pair of Contacts for Equal and they will be synchronized
		if(csvLineContact.findPhoneThunderbird(phoneNumber, matchType)) {
			if(debug) Log.v(TAG, "Raw Contact: "  + phoneNumber + " / "+ rawContactID + " DEFINITELY");
			setOfSureEqualRawContactIdPhone.add(rawContactID);
		}
		// Else declared this pair as Possible Equal Contacts
		else {
			if(debug) Log.v(TAG, "Raw Contact: "  + phoneNumber + " / " + rawContactID + " MAYBE");
			setOfPossibleEqualRawContactId.add(rawContactID);
		}
	}
	
	/**
	 * Funkcia porovna aktualne hodnoty kontaktu s polozkami csv suboru a na zaklade rovnakych hodnot dochadza bud k prepisaniu alebo vytvoreniu noveho kontaktu
	 * @rawContactId - ID RawContact-u , u ktoreho maju byt prepisovane hodnoty alebo vytvarane nove.
	 * */
	public void updateRawContact(long rawContactID) {
		Cursor c = null;
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		try {
			c = queryForRawContactsDetails(rawContactID);
					
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {			
				String s = getColumnString(c, Entity.MIMETYPE);
				if(debug) Log.v(TAG, s);
				
				// Entry type: Name
				if(s.equals(StructuredName.CONTENT_ITEM_TYPE)) {
					// This first conditions are for: If CSV contact doesn't contains string, 
					// then there is no need to synchronize their values. 
					if(csvLineContact.checkStructureNameThunderbird())
						synchronizeStructuredName(c, ops, rawContactID);
				}
				// Entry type: Organisation
				else if (s.equals(Organization.CONTENT_ITEM_TYPE)) {
					if(csvLineContact.checkOrganizationThunderbird())
						synchronizeOrganization(c, ops, rawContactID);					
				}
				// Entry type: Postal Address
				else if(s.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
					if(csvLineContact.checkPostalWorkThunderbird() || csvLineContact.checkPostalHomeThunderbird())
						synchronizePostal(c, ops);
				}
				// Entry type: Phone number
				else if(s.equals(Phone.CONTENT_ITEM_TYPE)) {
					if(csvLineContact.checkPhoneThunderbird())
						synchronizePhone(c);
				}
				// Entry type: Email address
				else if(s.equals(Email.CONTENT_ITEM_TYPE)) {
					if(csvLineContact.checkEmailThunderbird())
						synchronizeEmails(c);
				}
				// Entry type: WebSite
				else if(s.equals(Website.CONTENT_ITEM_TYPE)) {
					if(csvLineContact.checkWebsiteThunderbird())
						synchronizeWebsite(c);
				}
				// Entry type: NickName
				else if(s.equals(Nickname.CONTENT_ITEM_TYPE)) {
					if(!csvLineContact.getString(ThunderbirdConstants.T_NICKNAME).equals(""))
						synchronizeNickName(c);
				}
				// Entry type: Note
				else if(s.equals(Note.CONTENT_ITEM_TYPE)){
					// Duplicate contacts
					if(!csvLineContact.getString(ThunderbirdConstants.T_NOTE).equals("")) {
						if(!csvLineContact.getString(ThunderbirdConstants.T_NOTE).equals(NOTE_UNIQUE_NAME)) 
							synchronizeNote(c);
						// T_NOTE is equal to NOTE UNIQUE NAME. It means that we adding duplicate contact
						else {
							updateThunderbirdItems.setNewNote(false);
						}
					}
				}
				// TODO Dorobit Instant Messenger policko
			}
			try {
				// Add CSV item fields which aren't marked as equal
				addNewItemsFromCsvToRawContact(ops, rawContactID);
				context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			if(c != null)
				c.close();
		}
	}
	
	/**
	 * Method searches all CSV fields that are marked for create new fields, creates and joins them to selected RawContact. 
	 * @param ops - containter for operetions that should be build as batch 
	 * @param rawContactId - RawContac ID to which should be new values be joined
	 */
	protected void addNewItemsFromCsvToRawContact(ArrayList<ContentProviderOperation> ops, long rawContactId) {
		if(updateThunderbirdItems.isNewEmail1())
			ops.add(createEmailBuilder(rawContactId, ThunderbirdConstants.T_EMAIL1, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewEmail2())
			ops.add(createEmailBuilder(rawContactId, ThunderbirdConstants.T_EMAIL2, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewWorkPhone())
			ops.add(createPhoneBuilder(rawContactId, ThunderbirdConstants.T_WORK_PHONE, Phone.TYPE_WORK, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewHomePhone())
			ops.add(createPhoneBuilder(rawContactId, ThunderbirdConstants.T_HOME_PHONE, Phone.TYPE_HOME, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewFax())
			ops.add(createPhoneBuilder(rawContactId, ThunderbirdConstants.T_FAX, Phone.TYPE_FAX_WORK, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewPager())
			ops.add(createPhoneBuilder(rawContactId, ThunderbirdConstants.T_PAGER, Phone.TYPE_PAGER, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewMobile())
			ops.add(createPhoneBuilder(rawContactId, ThunderbirdConstants.T_MOBILE, Phone.TYPE_MOBILE, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewNickName())
			ops.add(createNickNameBuilder(rawContactId, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewHomeAddress())
			ops.add(createStructuredPostalBuilder(rawContactId, ImportCostants.UPDATE, StructuredPostal.TYPE_HOME));
		if(updateThunderbirdItems.isNewWorkAddress())
			ops.add(createStructuredPostalBuilder(rawContactId, ImportCostants.UPDATE, StructuredPostal.TYPE_WORK));
		if(updateThunderbirdItems.isNewOrganization())
			ops.add(createOrganizationBuilder(rawContactId, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewNote())
			ops.add(createNoteBuilder(rawContactId, ThunderbirdConstants.T_NOTE, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewWebPage1())
			ops.add(createWebSite(rawContactId, ThunderbirdConstants.T_WEB_ADDRESS, ImportCostants.UPDATE));
		if(updateThunderbirdItems.isNewWebPage2())
			ops.add(createWebSite(rawContactId, ThunderbirdConstants.T_WEB_ADDRESS2, ImportCostants.UPDATE));
	}
	
	/**
	 * Method pulls all address fields, then compare them with CSV address fields. Then if it marks them as equals
	 * it creates new address fields, which are updated.
	 * @param c - cursor
	 * @param ops - list of operation, that should be processed as batch operations
	 */
	private void synchronizePostal(Cursor c, ArrayList<ContentProviderOperation> ops) {
		Builder bd = null;
		
		if((!isColumnNull(c, StructuredPostal.TYPE))) {
			String[] androidAddress = new String[] {
				(isColumnNull(c, StructuredPostal.STREET)) ? "" : getColumnString(c, StructuredPostal.STREET), 
				(isColumnNull(c, StructuredPostal.POBOX)) ? "" : getColumnString(c, StructuredPostal.POBOX),
				(isColumnNull(c, StructuredPostal.CITY)) ? "" : getColumnString(c, StructuredPostal.CITY),
				(isColumnNull(c, StructuredPostal.REGION)) ? "" : getColumnString(c, StructuredPostal.REGION),
				(isColumnNull(c, StructuredPostal.POSTCODE)) ? "" : getColumnString(c, StructuredPostal.POSTCODE),
				(isColumnNull(c, StructuredPostal.COUNTRY)) ? "" : getColumnString(c, StructuredPostal.COUNTRY)
			};
			String[] newAddress = null;
			switch(getColumnInt(c, StructuredPostal.TYPE)) {
				case StructuredPostal.TYPE_HOME: {
					if(csvLineContact.checkPostalHomeThunderbird()) {
						String[] thdAddress = new String[] {
							csvLineContact.getString(ThunderbirdConstants.T_HOME_STREET),
							"",
							csvLineContact.getString(ThunderbirdConstants.T_HOME_CITY),
							csvLineContact.getString(ThunderbirdConstants.T_HOME_REGION),
							csvLineContact.getString(ThunderbirdConstants.T_HOME_PSC),
							csvLineContact.getString(ThunderbirdConstants.T_HOME_COUNTRY)
						};
						
						MoreFieldsComparator address = new MoreFieldsComparator(androidAddress, thdAddress, matchType);
						if(address.areAddressesEqual(minMatchAddressFields)) {
							// Mark as not create new entry for CSV Postal Address Fields
							updateThunderbirdItems.setNewHomeAddress(false);
							newAddress = address.createNewSyncData();
						}
					}
					break;
				}
				case StructuredPostal.TYPE_WORK: {
					if(csvLineContact.checkPostalWorkThunderbird()) {
						String[] thdAddress = new String[] {
							csvLineContact.getString(ThunderbirdConstants.T_WORK_STREET),
							"",
							csvLineContact.getString(ThunderbirdConstants.T_WORK_CITY),
							csvLineContact.getString(ThunderbirdConstants.T_WORK_REGION),
							csvLineContact.getString(ThunderbirdConstants.T_WORK_PSC),
							csvLineContact.getString(ThunderbirdConstants.T_WORK_COUNTRY)
						};
						
						MoreFieldsComparator address = new MoreFieldsComparator(androidAddress, thdAddress, matchType);
						if(address.areAddressesEqual(minMatchAddressFields)) {
							// Mark as not create new entry for CSV Postal Address Fields
							updateThunderbirdItems.setNewWorkAddress(false);
							newAddress = address.createNewSyncData();					
						}
					}
					break;
				}
			}
			if(newAddress != null) {
				bd = ContentProviderOperation.newUpdate(Data.CONTENT_URI)
						.withSelection(Data._ID + " = ? ", new String[] {String.valueOf(getColumnLong(c, RawContactsEntity.DATA_ID))});
				// Overiding values with new string
				bd.withValue(StructuredPostal.STREET, newAddress[0]);	
				bd.withValue(StructuredPostal.POBOX, newAddress[1]);
				bd.withValue(StructuredPostal.CITY, newAddress[2]);
				bd.withValue(StructuredPostal.REGION, newAddress[3]);
				bd.withValue(StructuredPostal.POSTCODE, newAddress[4]);
				bd.withValue(StructuredPostal.COUNTRY, newAddress[5]);
			}
		}
		if(bd != null) {
			ops.add(bd.build());
		}
		return;
	}
	
	/**
	 * Method compares Android Contact WebSite to CSV WebSite fields and if it matches some of them, 
	 * it marked the match field index as NOT_CREATE 
	 * @param c cursor
	 */
	private void synchronizeWebsite(Cursor c) {
		if((!isColumnNull(c, Website.URL))) {
			if(!csvLineContact.getString(ThunderbirdConstants.T_WEB_ADDRESS).equals("")) {
				if(csvLineContact.equal(ThunderbirdConstants.T_WEB_ADDRESS, getColumnString(c, Website.URL), matchType))
					updateThunderbirdItems.setNewWebPage1(false);
			}
			if(!csvLineContact.getString(ThunderbirdConstants.T_WEB_ADDRESS2).equals("")) {
				if(csvLineContact.equal(ThunderbirdConstants.T_WEB_ADDRESS2, getColumnString(c, Website.URL), matchType))
					updateThunderbirdItems.setNewWebPage2(false);
			}
		}
	}
	
	/**
	 * Method compares cursor note to CSV note and if it matches some of them, it marked the match field index 
	 * as NOT_CREATE 
	 * @param c cursor
	 */
	private void synchronizeNote(Cursor c) {
		if((!isColumnNull(c, Note.NOTE))) {
			if(csvLineContact.equal(ThunderbirdConstants.T_NOTE, getColumnString(c, Note.NOTE), matchType))
				updateThunderbirdItems.setNewNote(false);
		}
	}
	
	/**
	 * Method compares cursor phone number to CSV phones numbers and if it matches some of them, it marked the matched field index 
	 * as NOT_CREATE. 
	 * @param c cursor
	 */
	private void synchronizePhone(Cursor c) {
		switch(getColumnInt(c, Phone.TYPE)) {
			case Phone.TYPE_WORK: {
				if((!csvLineContact.getString(ThunderbirdConstants.T_WORK_PHONE).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(ThunderbirdConstants.T_WORK_PHONE, getColumnString(c, Phone.NUMBER), matchType)))
						updateThunderbirdItems.setNewWorkPhone(false);
				}
				break;
			}
			case Phone.TYPE_HOME: {
				if((!csvLineContact.getString(ThunderbirdConstants.T_HOME_PHONE).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(ThunderbirdConstants.T_HOME_PHONE, getColumnString(c, Phone.NUMBER), matchType)))
						updateThunderbirdItems.setNewHomePhone(false);
				}
				break;
			}
			case Phone.TYPE_FAX_HOME: 
			case Phone.TYPE_FAX_WORK: {
				if((!csvLineContact.getString(ThunderbirdConstants.T_FAX).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(ThunderbirdConstants.T_FAX, getColumnString(c, Phone.NUMBER), matchType)))
						updateThunderbirdItems.setNewFax(false);
				}
				break;
			}
			case Phone.TYPE_PAGER: {
				if((!csvLineContact.getString(ThunderbirdConstants.T_PAGER).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(ThunderbirdConstants.T_PAGER, getColumnString(c, Phone.NUMBER), matchType)))
						updateThunderbirdItems.setNewPager(false);
				}
				break;
			}
			case Phone.TYPE_MOBILE: {
				if((!csvLineContact.getString(ThunderbirdConstants.T_MOBILE).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(ThunderbirdConstants.T_MOBILE, getColumnString(c, Phone.NUMBER), matchType)))
						updateThunderbirdItems.setNewMobile(false);
				}
				break;
			}
			default:
				break;
		}
	}
	
	/**
	 * Method synchronize cursor Organization fields with CSV Organization fields. First it pulls all Organisation fields 
	 * from  Android RawContact, then compares them and if they are equal joins them.
	 * @param c - cursor
	 * @param ops - list of operation, that should be processed as batch operations
	 * @param rawContactId - Id of RawContact 
	 */
	private void synchronizeOrganization(Cursor c, ArrayList<ContentProviderOperation> ops, long rawContactId) {
		Builder bd = null;
		String[] androidOrganization = new String[] {
				(isColumnNull(c, Organization.COMPANY)) ? "" : getColumnString(c, Organization.COMPANY) ,
				(isColumnNull(c, Organization.TITLE)) ? "" : getColumnString(c, Organization.TITLE)
			};
		String[] csvOrganization = new String[] {
				csvLineContact.getString(ThunderbirdConstants.T_COMPANY),
				csvLineContact.getString(ThunderbirdConstants.T_JOB_TITLE) 
		};
		// Get organizationFieldComparator
		MoreFieldsComparator orgComparator = new MoreFieldsComparator(androidOrganization, csvOrganization, matchType);
		// Check if the contacts are equal
		if(orgComparator.areOrganisationsEqual()) {
			// Mark as not create new entry for CSV Organization Fields
			updateThunderbirdItems.setNewOrganization(false);
			String[] newOrganization = orgComparator.createNewSyncData();
			bd = ContentProviderOperation.newUpdate(Data.CONTENT_URI)
					.withSelection(Data._ID + " = ? ", new String[] {String.valueOf(getColumnLong(c, RawContactsEntity.DATA_ID))});
			bd.withValue(Organization.COMPANY, newOrganization[0]);	
			bd.withValue(Organization.TITLE, newOrganization[1]);
		}
		if(bd != null) {
			ops.add(bd.build());
		}
		return;
	}
	
	/**
	 * Method compares Android Contact email to CSV Emails fields and if it matches some of them, 
	 * it marked the match field index as NOT_CREATE 
	 * @param c cursor
	 */
	private void synchronizeEmails(Cursor c) {
		String email = "";
		if(!isColumnNull(c, Email.DATA1)) {
			email = getColumnString(c, Email.DATA1);
			
			if(!csvLineContact.getString(ThunderbirdConstants.T_EMAIL1).equals(""))
				if(email.equals(csvLineContact.getString(ThunderbirdConstants.T_EMAIL1)))
					updateThunderbirdItems.setNewEmail1(false);
			if(!csvLineContact.getString(ThunderbirdConstants.T_EMAIL2).equals("")) 
				if(email.equals(csvLineContact.getString(ThunderbirdConstants.T_EMAIL2)))
					updateThunderbirdItems.setNewEmail2(false);
		}
	}
	
	/**
	 * Method compares Android Contact Note to CSV Note fields and if it matches some of them, 
	 * it marked the match field index as NOT_CREATE 
	 * @param c cursor
	 */
	private void synchronizeNickName(Cursor c) {
		if(getColumnString(c, Nickname.NAME).equals(csvLineContact.getString(ThunderbirdConstants.T_NICKNAME)))
			updateThunderbirdItems.setNewNickName(false);
	}
	
	/**
	 * Method overides Name fields of Android from the CSV file, if CSV file doesn't contain empty string. 
	 * Otherwise leaves Android Name fields untouched. 
	 * @param c - cursor
	 * @param ops - list of operation, that should be processed as batch operations
	 * @param rawContactId - Id of RawContact 
	 */
	private void synchronizeStructuredName(Cursor c, ArrayList<ContentProviderOperation> ops, long rawContactId) {
		long data_id = getColumnLong(c, RawContactsEntity.DATA_ID);
		Builder bd = null;
		
		bd = ContentProviderOperation.newUpdate(Data.CONTENT_URI)
			.withSelection(Data._ID + " = ? ", new String[] {String.valueOf(data_id)});
		// If there is possibility of adding some extra data then it adds it, else it don't add them.				
		if(!csvLineContact.getString(ThunderbirdConstants.T_GIVEN_NAME).equals("")) 
			bd.withValue(StructuredName.GIVEN_NAME, csvLineContact.getString(ThunderbirdConstants.T_GIVEN_NAME));	
		if(!csvLineContact.getString(ThunderbirdConstants.T_FAMILY_NAME).equals(""))
			bd.withValue(StructuredName.FAMILY_NAME, csvLineContact.getString(ThunderbirdConstants.T_FAMILY_NAME));
		if(!csvLineContact.getString(ThunderbirdConstants.T_DISPLAY_NAME).equals(""))
			bd.withValue(StructuredName.DISPLAY_NAME, csvLineContact.getString(ThunderbirdConstants.T_DISPLAY_NAME));	
		if(bd != null) {
			ops.add(bd.build());
		}
		return;
	}	
	
	/**
	 * Method creates new contacts from thunderbird CSV Type raw.
	 * @param csvContact - object that hold all Thunderbird CSV fields
	 */
	public void createNewContact(CsvContact csvContact, String accName, String accType) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		
		int rawIndex = ops.size();
		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_NAME, accName)
				.withValue(RawContacts.ACCOUNT_TYPE, accType).build());
		if(csvContact.checkStructureNameThunderbird())
			ops.add(createStructuredNameBuilder(rawIndex, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_NICKNAME).equals(""))
			ops.add(createNickNameBuilder(rawIndex, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_EMAIL1).equals(""))
			ops.add(createEmailBuilder(rawIndex, ThunderbirdConstants.T_EMAIL1, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_EMAIL2).equals(""))
			ops.add(createEmailBuilder(rawIndex, ThunderbirdConstants.T_EMAIL2, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_WORK_PHONE).equals(""))
			ops.add(createPhoneBuilder(rawIndex, ThunderbirdConstants.T_WORK_PHONE, Phone.TYPE_WORK, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_HOME_PHONE).equals(""))
			ops.add(createPhoneBuilder(rawIndex, ThunderbirdConstants.T_HOME_PHONE, Phone.TYPE_HOME, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_FAX).equals(""))
			ops.add(createPhoneBuilder(rawIndex, ThunderbirdConstants.T_FAX, Phone.TYPE_FAX_WORK, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_PAGER).equals(""))
			ops.add(createPhoneBuilder(rawIndex, ThunderbirdConstants.T_PAGER, Phone.TYPE_PAGER, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_MOBILE).equals(""))
			ops.add(createPhoneBuilder(rawIndex, ThunderbirdConstants.T_MOBILE, Phone.TYPE_MOBILE, ImportCostants.NEW_INSERT));
		
		if(csvContact.checkPostalHomeThunderbird())
			ops.add(createStructuredPostalBuilder(rawIndex, ImportCostants.NEW_INSERT, StructuredPostal.TYPE_HOME));
		if(csvContact.checkPostalWorkThunderbird())
			ops.add(createStructuredPostalBuilder(rawIndex, ImportCostants.NEW_INSERT, StructuredPostal.TYPE_WORK));
		if(csvContact.checkOrganizationThunderbird())
			ops.add(createOrganizationBuilder(rawIndex, ImportCostants.NEW_INSERT));
		
		if(!csvContact.getString(ThunderbirdConstants.T_WEB_ADDRESS).equals(""))
			ops.add(createWebSite(rawIndex, ThunderbirdConstants.T_WEB_ADDRESS, ImportCostants.NEW_INSERT));
		
		if(!csvContact.getString(ThunderbirdConstants.T_WEB_ADDRESS2).equals(""))
			ops.add(createWebSite(rawIndex, ThunderbirdConstants.T_WEB_ADDRESS2, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(ThunderbirdConstants.T_NOTE).equals(""))
			if(!csvContact.getString(ThunderbirdConstants.T_NOTE).equals(NOTE_UNIQUE_NAME))
				ops.add(createNoteBuilder(rawIndex, ThunderbirdConstants.T_NOTE, ImportCostants.NEW_INSERT));
	
		try {
			context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (Exception e) {
            if(debug) Log.e(TAG, "Vyskytla sa chyba pri update kontaktu: " + e);
			e.printStackTrace();
        }
	}
	
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param oppType - type of operations. UPDATE or INSERT
	 * return created builder.
	 */
	private ContentProviderOperation createStructuredNameBuilder(long rawIndex, int oppType) {
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		
		if(!csvLineContact.getString(ThunderbirdConstants.T_GIVEN_NAME).equals(""))
			bd.withValue(StructuredName.GIVEN_NAME, csvLineContact.getString(ThunderbirdConstants.T_GIVEN_NAME));
		if(!csvLineContact.getString(ThunderbirdConstants.T_FAMILY_NAME).equals(""))
			bd.withValue(StructuredName.FAMILY_NAME, csvLineContact.getString(ThunderbirdConstants.T_FAMILY_NAME));
				// TODO OTESTUJ TEN DISPAY NAME.. ci ho vobec treba
				//if(!csvLine.getString(ThunderbirdConstants.T_DISPLAY_NAME).equals(""))	bd.withValue(StructuredName.DISPLAY_NAME, csvLine.getString(ThunderbirdConstants.T_DISPLAY_NAME));
		return bd.build();
	}
	
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * Works with NickName android fields.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param oppType - type of operations. UPDATE or INSERT
	 * return created builder.
	 */
	private ContentProviderOperation createNickNameBuilder(long rawIndex, int oppType) {
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE);
		bd.withValue(Nickname.NAME, csvLineContact.getString(ThunderbirdConstants.T_NICKNAME));
		return bd.build();
	}
	
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * Works with Postal addresses fields.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param oppType - type of operations. UPDATE or INSERT
	 * @param postalType - type of Postal address that should be created or updated
	 * return created builder.
	 */
	private ContentProviderOperation createStructuredPostalBuilder(long rawIndex, int oppType, int postalType) {
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
		bd.withValue(StructuredPostal.TYPE, postalType);
		
		if(postalType == (StructuredPostal.TYPE_HOME)) {
			if(!csvLineContact.getString(ThunderbirdConstants.T_HOME_STREET).equals(""))
				bd.withValue(StructuredPostal.STREET, csvLineContact.getString(ThunderbirdConstants.T_HOME_STREET));
			if(!csvLineContact.getString(ThunderbirdConstants.T_HOME_CITY).equals(""))
				bd.withValue(StructuredPostal.CITY, csvLineContact.getString(ThunderbirdConstants.T_HOME_CITY));
			if(!csvLineContact.getString(ThunderbirdConstants.T_HOME_REGION).equals(""))	
				bd.withValue(StructuredPostal.REGION, csvLineContact.getString(ThunderbirdConstants.T_HOME_REGION));
			if(!csvLineContact.getString(ThunderbirdConstants.T_HOME_PSC).equals(""))	
				bd.withValue(StructuredPostal.POSTCODE, csvLineContact.getString(ThunderbirdConstants.T_HOME_PSC));
			if(!csvLineContact.getString(ThunderbirdConstants.T_HOME_COUNTRY).equals(""))	
				bd.withValue(StructuredPostal.COUNTRY, csvLineContact.getString(ThunderbirdConstants.T_HOME_COUNTRY));
		}
		else if(postalType == (StructuredPostal.TYPE_WORK)) {
			if(!csvLineContact.getString(ThunderbirdConstants.T_WORK_STREET).equals(""))
				bd.withValue(StructuredPostal.STREET, csvLineContact.getString(ThunderbirdConstants.T_WORK_STREET));
			if(!csvLineContact.getString(ThunderbirdConstants.T_WORK_CITY).equals(""))
				bd.withValue(StructuredPostal.CITY, csvLineContact.getString(ThunderbirdConstants.T_WORK_CITY));
			if(!csvLineContact.getString(ThunderbirdConstants.T_WORK_REGION).equals(""))	
				bd.withValue(StructuredPostal.REGION, csvLineContact.getString(ThunderbirdConstants.T_WORK_REGION));
			if(!csvLineContact.getString(ThunderbirdConstants.T_WORK_PSC).equals(""))	
				bd.withValue(StructuredPostal.POSTCODE, csvLineContact.getString(ThunderbirdConstants.T_WORK_PSC));
			if(!csvLineContact.getString(ThunderbirdConstants.T_WORK_COUNTRY).equals(""))	
				bd.withValue(StructuredPostal.COUNTRY, csvLineContact.getString(ThunderbirdConstants.T_WORK_COUNTRY));
		}
		return bd.build();
	}
	
	/**
	 * Method updates rawContact specified in rawIndex or creates a new DATA entry in RawContacts DATA table.
	 * Works with Organization fields.
	 * @param rawIndex - index of RawContact, to which the data should be updated
	 * @param oppType - type of operations. UPDATE or INSERT
	 * return created builder.
	 */
	private ContentProviderOperation createOrganizationBuilder(long rawIndex, int oppType) {
		
		Builder bd = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		
		if(oppType == ImportCostants.NEW_INSERT)
			bd.withValueBackReference(Data.RAW_CONTACT_ID, (int) rawIndex);
		else if(oppType == ImportCostants.UPDATE)
			bd.withValue(Data.RAW_CONTACT_ID, rawIndex);
		
		bd.withValue(Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
		bd.withValue(Organization.TYPE, Organization.TYPE_WORK);
		
		if(!csvLineContact.getString(ThunderbirdConstants.T_JOB_TITLE).equals(""))
			bd.withValue(Organization.TITLE, csvLineContact.getString(ThunderbirdConstants.T_JOB_TITLE));
		if(!csvLineContact.getString(ThunderbirdConstants.T_DEPARTMENT).equals(""))
			bd.withValue(Organization.DEPARTMENT, csvLineContact.getString(ThunderbirdConstants.T_DEPARTMENT));
		if(!csvLineContact.getString(ThunderbirdConstants.T_COMPANY).equals(""))	
			bd.withValue(Organization.COMPANY, csvLineContact.getString(ThunderbirdConstants.T_COMPANY));
		return bd.build();
	}
	
	/**
	 * Method creates all possible display name combinations and return them in List of strings.
	 * @return 	created list of all possible display name combinations
	 */	
	protected List<String> createDisplaysNames() {
		List<String> list = new ArrayList<String>();
		
		// NAME + SURNAME
		String name1 = (csvLineContact.getString(ThunderbirdConstants.T_GIVEN_NAME) + " " + csvLineContact.getString(ThunderbirdConstants.T_FAMILY_NAME));
		
		// SURNAME + NAME
		String name2 = (csvLineContact.getString(ThunderbirdConstants.T_FAMILY_NAME) + " " + csvLineContact.getString(ThunderbirdConstants.T_GIVEN_NAME));
		
		// "NAME MIDDLE_NAME SURNAME, SUFFIX"
		String name3 = (csvLineContact.getString(ThunderbirdConstants.T_DISPLAY_NAME));
		
		list.add(name1.trim());
		if(debug) Log.v(TAG, list.get(0));
		list.add(name2.trim());
		if(debug) Log.v(TAG, list.get(1));
		list.add(name3.trim());
		if(debug) Log.v(TAG, list.get(2));
		switch (matchType) {
			case CASE_SENSITIVE:
				return list;
			case IGNORE_CASE:
				return list;
			case IGNORE_ACCENTS_AND_CASES:
			case IGNORE_ACCENTS_CASE_SENSITIVE:
				list.add(StringUtils.convertNonAscii(name1.trim()));
				if(debug) Log.v(TAG, list.get(3));
				list.add(StringUtils.convertNonAscii(name2.trim()));
				if(debug) Log.v(TAG, list.get(4));
				list.add(StringUtils.convertNonAscii(name3.trim()));
				if(debug) Log.v(TAG, list.get(5));
				return list;
		}
		return null;
	}
	
	/**
	 * Method compares all possible display name combinations and if some match then it's added to the set of matched names.
	 */
	protected void checkStructureNameDeeply() {
		Cursor c = null;
		c = queryForStructureName();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String[] s = new String[5];
			s[0] = getColumnIfNull(c, StructuredName.PREFIX);
			s[1] = getColumnIfNull(c, StructuredName.GIVEN_NAME);
			s[2] = getColumnIfNull(c, StructuredName.MIDDLE_NAME);
			s[3] = getColumnIfNull(c, StructuredName.FAMILY_NAME);
			s[4] = getColumnIfNull(c, StructuredName.SUFFIX);
			
			String[] s1 = new String[] {
					"", csvLineContact.getString(ThunderbirdConstants.T_GIVEN_NAME) , "", csvLineContact.getString(ThunderbirdConstants.T_FAMILY_NAME),""
			};
			
			MoreFieldsComparator name = new MoreFieldsComparator(s, s1, matchType);
			// Min fields to be equal... there is tolerancy if there are only in GIVEN NAME EQUAL
			if(name.areNamesEqual(new boolean[]{false,true,false,true,false})) {
				if(debug) Log.v(TAG, c.getString(c.getColumnIndex(StructuredName.DISPLAY_NAME))  + " ID: " + String.valueOf(getColumnLong(c, Data.CONTACT_ID)));
				setOfDisplayNameEqualContactsId.add(new Long(getColumnLong(c, Data.CONTACT_ID)));
				continue;
			}
			if(!csvLineContact.getString(ThunderbirdConstants.T_DISPLAY_NAME).equals("")) {
				if(equal(getColumnIfNull(c, StructuredName.DISPLAY_NAME), csvLineContact.getString(ThunderbirdConstants.T_DISPLAY_NAME), matchType)) {
					if(debug) Log.v(TAG, c.getString(c.getColumnIndex(StructuredName.DISPLAY_NAME))  + " ID: " + String.valueOf(getColumnLong(c, Data.CONTACT_ID)));
					setOfDisplayNameEqualContactsId.add(new Long(getColumnLong(c, Data.CONTACT_ID)));
				}
			}
		}
		c.close();
	}
	
	
}
