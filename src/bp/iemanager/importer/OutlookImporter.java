package bp.iemanager.importer;

import java.util.ArrayList;
import java.util.List;

import bp.iemanager.StringUtils;
import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.OutlookConstants;
import bp.iemanager.csvcontact.ThunderbirdConstants;
import bp.iemanager.importer.ImportCostants;


import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;

import android.content.Context;

import android.database.Cursor;


import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;

import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;

import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

/**
 * Class representing importing of Outlook CSV Contacts into Android System.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class OutlookImporter extends Importer {
	/**
	 * TAG for printing debug notes
	 */
	private static final String TAG = "ImportOutlookContacts";
	
	/**
	 * Object holding all abstract view on fields between Android and File Contact fields
	 */
	protected UpdateOutlookCsvItems updateOutlookItems;
	
	/**
	 * Constructor to Sync Android Contact with csvLine.
	 * @param _ctx - context of activity to have access to contentProvider()
	 * @param _csvLine - csvLine containing fields of file contact
	 */		
	public OutlookImporter(Context _ctx, CsvContact _csvLine, MatchType _type, boolean[] _match_options) {
		super(_ctx, _csvLine, _type, _match_options);
		if(importType != ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) 
			updateOutlookItems = new UpdateOutlookCsvItems(csvLineContact);
	}
	
	/**
	 * Constructor to Sync Android Contact with csvLine.
	 * @param _ctx - context of activity to have access to contentProvider()
	 * @param _csvLine - csvLine containing fields of file contact
	 * @param _list - reference to list of Possible Contact where should be added contacts where can't be definitely determine if they are same
	 * @param _options - object containing selected USER OPTIONS
	 */
	public OutlookImporter(Context _ctx, CsvContact _csvLine, ArrayList<PossibleEqualContacts> _list, UserSelectedOptions _options) {
		super(_ctx, _csvLine, _list, _options);
		updateOutlookItems = new UpdateOutlookCsvItems(csvLineContact);
	}

	/**
	 * Method add rawContacID to Sure equal Set if at least one email number  match else
	 * is add to possible equal Set .
	 * @param email - finding email
	 * @param rawContactID - if of raw contact to which phoneNumber belongs to
	 */
	protected void sortRawContactEmail(String email, long rawContactID) {
		// If some of emails matches then declared this pair of Contacts for Equal and they will be synchronized
		if(csvLineContact.findEmailOutlook(email, matchType)) {
			if(debug) Log.v(TAG, "Raw Contact: " +  " DEFINITELY");
			setOfSureEqualRawContactIdEmail.add(rawContactID);
		}
		// For duplicate contact saved in CSV file, it's necessary to CHECK NOTE for unique string 
		else if(!csvLineContact.getString(OutlookConstants.O_NOTE).equals("") && csvLineContact.getString(OutlookConstants.O_NOTE).equals(NOTE_UNIQUE_NAME)) {
			// If it's NOTE is created with IE Manager's Exporter, then we must checked EQUALS EMAIL's to declare as MATCHED
			if(csvLineContact.findEmailUserOutlook(email, matchType)) {
				if(debug) Log.v(TAG, "Raw Contact: " + rawContactID + " DEFINITELY");
				setOfSureEqualRawContactIdEmail.add(rawContactID);
			}
		}
		// Else declared this pair as Possible Equal Contacts
		else {
			if(debug) Log.v(TAG, "Raw Contact: " + rawContactID + " MAYBE");
			setOfPossibleEqualRawContactId.add(rawContactID);
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
		if(csvLineContact.findPhoneOutlook(phoneNumber, matchType)) {
			if(debug) Log.v(TAG, "Raw Contact: " + rawContactID + " DEFINITELY");
			setOfSureEqualRawContactIdPhone.add(rawContactID);
		}
		// Else declared this pair as Possible Equal Contacts
		else {
			if(debug) Log.v(TAG, "Raw Contact: " + rawContactID + " MAYBE");
			setOfPossibleEqualRawContactId.add(rawContactID);
		}
	}

	/**
	 * Method compares Android Data values of RawContacts with CSV contact and in case of need it synchronize them. 
	 * If CSV contacts contains field which Android Contact doesn't, then we create new fields in Android Contact.
	 * With fields like address, name it synchronizes them and creates new string of them.
	 * @rawContactId - ID RawContact , in which should be created new values or updated
	 * */
	public void updateRawContact(long rawContactID) {
		Cursor c = null;
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		try {
			c = queryForRawContactsDetails(rawContactID);
					
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {				
				String s = getColumnString(c, Entity.MIMETYPE);
				if(debug)Log.v(TAG, s);
				// Entry type: Name
				if(s.equals(StructuredName.CONTENT_ITEM_TYPE)) {
					// This first conditions are for: If CSV contact doesn't contains string, 
					// then there is no need to synchronize their values. 
					if(csvLineContact.checkStructureNameOutlook())
						synchronizeStructuredName(c, ops, rawContactID);
					
				}
				// Entry type: Organization
				else if (s.equals(Organization.CONTENT_ITEM_TYPE)) {
					if(csvLineContact.checkOrganizationOutlook())
						synchronizeOrganization(c, ops, rawContactID);
											
				}
				// Entry type: Postal Address
				else if(s.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
					if(csvLineContact.checkPostalHomeOutlook() || csvLineContact.checkPostalWorkOutlook() || csvLineContact.checkPostalOtherOutlook()) {
						synchronizePostal(c, ops);
					}
				}
				// Entry type: Phone number
				else if(s.equals(Phone.CONTENT_ITEM_TYPE)){
					if(csvLineContact.checkPhoneOutlook())
						synchronizePhone(c);
					
				}
				// Entry type: Email address
				else if(s.equals(Email.CONTENT_ITEM_TYPE)){
					if(csvLineContact.checkEmailOutlook())
						synchronizeEmails(c, ops);
					
				}
				// Entry type: WebSite
				else if(s.equals(Website.CONTENT_ITEM_TYPE)){
					if(csvLineContact.checkWebsiteOutlook())
						synchronizeWebsite(c);
					
				}
				// Entry type: Note
				else if(s.equals(Note.CONTENT_ITEM_TYPE)){
					if(!csvLineContact.getString(OutlookConstants.O_NOTE).equals("")) {
						if(!csvLineContact.getString(OutlookConstants.O_NOTE).equals(NOTE_UNIQUE_NAME))
							synchronizeNote(c, ops);
						else
							updateOutlookItems.setNewNote(false);
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
		if(updateOutlookItems.isNewEmail1())
			ops.add(createEmailBuilder(rawContactId, OutlookConstants.O_EMAIL1, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewEmail2())
			ops.add(createEmailBuilder(rawContactId, OutlookConstants.O_EMAIL2, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewEmail3())
			ops.add(createEmailBuilder(rawContactId, OutlookConstants.O_EMAIL3, ImportCostants.UPDATE));
		
		if(updateOutlookItems.isNewOrganization())
			ops.add(createOrganizationBuilder(rawContactId, ImportCostants.UPDATE));
		
		if(updateOutlookItems.isNewHomeAddress())
			ops.add(createStructuredPostalBuilder(rawContactId, ImportCostants.UPDATE, StructuredPostal.TYPE_HOME));
		if(updateOutlookItems.isNewWorkAddress())
			ops.add(createStructuredPostalBuilder(rawContactId, ImportCostants.UPDATE, StructuredPostal.TYPE_WORK));
		if(updateOutlookItems.isNewOtherAddress())
			ops.add(createStructuredPostalBuilder(rawContactId, ImportCostants.UPDATE, StructuredPostal.TYPE_OTHER));
		
		if(updateOutlookItems.isNewWorkFax())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_WORK_FAX, Phone.TYPE_FAX_WORK, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewWorkPhone())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_WORK_PHONE, Phone.TYPE_WORK, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewWorkPhone2())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_WORK_PHONE2, Phone.TYPE_WORK, ImportCostants.UPDATE));
		
		if(updateOutlookItems.isNewHomeFax())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_HOME_FAX, Phone.TYPE_FAX_HOME, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewHomePhone())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_HOME_PHONE, Phone.TYPE_HOME, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewHomePhone2())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_HOME_PHONE2, Phone.TYPE_HOME, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewMobile())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_MOBILE, Phone.TYPE_MOBILE, ImportCostants.UPDATE));
		
		if(updateOutlookItems.isNewOtherPhone())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_OTHER_PHONE, Phone.TYPE_OTHER, ImportCostants.UPDATE));
		if(updateOutlookItems.isNewPager())
			ops.add(createPhoneBuilder(rawContactId, OutlookConstants.O_PAGER, Phone.TYPE_PAGER, ImportCostants.UPDATE));
		
		if(updateOutlookItems.isNewNote())
			ops.add(createWebSite(rawContactId, OutlookConstants.O_NOTE, ImportCostants.UPDATE));
		
		if(updateOutlookItems.isNewWebPage())
			ops.add(createWebSite(rawContactId, OutlookConstants.O_WEB_PAGE, ImportCostants.UPDATE));
		
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
			// First compared address fields which are same for each address type
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
					if(csvLineContact.checkPostalHomeOutlook()) { 
						// Second compared address fields from csv file on different positions
						String[] outlkAddress = new String[] {
							csvLineContact.getString(OutlookConstants.O_HOME_STREET),
							csvLineContact.getString(OutlookConstants.O_HOME_POBOX),
							csvLineContact.getString(OutlookConstants.O_HOME_CITY),
							csvLineContact.getString(OutlookConstants.O_HOME_REGION),
							csvLineContact.getString(OutlookConstants.O_HOME_PSC),
							csvLineContact.getString(OutlookConstants.O_HOME_COUNTRY)
						};
						
						MoreFieldsComparator address = new MoreFieldsComparator(androidAddress, outlkAddress, matchType);
						if(address.areAddressesEqual(minMatchAddressFields)) {
						    // Home address from CSV file won't be created as new one
							updateOutlookItems.setNewHomeAddress(false);
							newAddress = address.createNewSyncData();
						}
					}
					break;
				}
				case StructuredPostal.TYPE_WORK: {
					if(csvLineContact.checkPostalWorkOutlook()) {	
						String[] outlkAddress = new String[] {
							csvLineContact.getString(OutlookConstants.O_WORK_STREET),
							csvLineContact.getString(OutlookConstants.O_WORK_POBOX),
							csvLineContact.getString(OutlookConstants.O_WORK_CITY),
							csvLineContact.getString(OutlookConstants.O_WORK_REGION),
							csvLineContact.getString(OutlookConstants.O_WORK_PSC),
							csvLineContact.getString(OutlookConstants.O_WORK_COUNTRY)
						};
						
						MoreFieldsComparator address = new MoreFieldsComparator(androidAddress, outlkAddress, matchType);
						if(address.areAddressesEqual(minMatchAddressFields)) {
						    // Work address from CSV file won't be created as new one
							updateOutlookItems.setNewWorkAddress(false);
							newAddress =  address.createNewSyncData();
						}
					}
					break;
				}
				case StructuredPostal.TYPE_OTHER: 
					if(csvLineContact.checkPostalOtherOutlook()) {
						String[] outlkAddress = new String[] { 
							csvLineContact.getString(OutlookConstants.O_OTHER_STREET),
							csvLineContact.getString(OutlookConstants.O_OTHER_POBOX),
							csvLineContact.getString(OutlookConstants.O_OTHER_CITY),
							csvLineContact.getString(OutlookConstants.O_OTHER_REGION),
							csvLineContact.getString(OutlookConstants.O_OTHER_PSC),
							csvLineContact.getString(OutlookConstants.O_OTHER_COUNTRY)
						};
						
						MoreFieldsComparator address = new MoreFieldsComparator(androidAddress, outlkAddress, matchType);
						if(address.areAddressesEqual(minMatchAddressFields)) {
							// Other address from CSV file won't be created as new one
							updateOutlookItems.setNewOtherAddress(false);
							newAddress =  address.createNewSyncData();
						}
					}
					break;
			}
			if(newAddress != null) {
				bd = ContentProviderOperation.newUpdate(Data.CONTENT_URI)
						.withSelection(Data._ID + " = ? ", new String[] {String.valueOf(getColumnLong(c, RawContactsEntity.DATA_ID))});
				// Overwriting values with new string
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
			if(csvLineContact.getLength() > OutlookConstants.O_USER3)	
				if(!csvLineContact.getString(OutlookConstants.O_WEB_PAGE).equals("")) {
					if(csvLineContact.equal(OutlookConstants.O_WEB_PAGE, getColumnString(c, Website.URL), matchType))
						updateOutlookItems.setNewWebPage(false);
				}
		}
	}
	
	/**
	 * Method compares cursor note to CSV note and if it matches some of them, it marked the match field index 
	 * as NOT_CREATE 
	 * @param c cursor
	 */	
	private void synchronizeNote(Cursor c, ArrayList<ContentProviderOperation> ops) {
		if((!isColumnNull(c, Note.NOTE))) {
			if(!csvLineContact.getString(OutlookConstants.O_NOTE).equals("")) {
				if(csvLineContact.equal(OutlookConstants.O_NOTE, getColumnString(c, Note.NOTE), matchType))
					updateOutlookItems.setNewNote(false);
			}
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
				if((!csvLineContact.getString(OutlookConstants.O_WORK_PHONE).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_WORK_PHONE, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewWorkPhone(false);
				}
				if((!csvLineContact.getString(OutlookConstants.O_WORK_PHONE2).equals("")) && (!isColumnNull(c, Phone.NUMBER))) {
					if((csvLineContact.equal(OutlookConstants.O_WORK_PHONE2, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewWorkPhone2(false);
				}
				break;
			}
			case Phone.TYPE_HOME: {
				if((!csvLineContact.getString(OutlookConstants.O_HOME_PHONE).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_HOME_PHONE, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewHomePhone(false);
				}
				if((!csvLineContact.getString(OutlookConstants.O_HOME_PHONE2).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_HOME_PHONE2, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewHomePhone2(false);
				}
				break;
			}
			case Phone.TYPE_FAX_HOME: {
				if((!csvLineContact.getString(OutlookConstants.O_HOME_FAX).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_HOME_FAX, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewHomeFax(false);
				}
				break;
			}
			case Phone.TYPE_FAX_WORK: {
				if((!csvLineContact.getString(OutlookConstants.O_WORK_FAX).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_WORK_FAX, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewWorkFax(false);
				}
				break;
			}
			case Phone.TYPE_PAGER: {
				if((!csvLineContact.getString(OutlookConstants.O_PAGER).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_PAGER, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewPager(false);
				}
				break;
			}
			case Phone.TYPE_MOBILE: {
				if((!csvLineContact.getString(OutlookConstants.O_MOBILE).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_MOBILE, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewMobile(false);
				}
				break;
			}
			case Phone.TYPE_OTHER: {
				if((!csvLineContact.getString(OutlookConstants.O_OTHER_PHONE).equals("")) && (!isColumnNull(c, Phone.NUMBER)) ) {
					if((csvLineContact.equal(OutlookConstants.O_OTHER_PHONE, getColumnString(c, Phone.NUMBER), matchType)))
						updateOutlookItems.setNewOtherPhone(false);
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
				csvLineContact.getString(OutlookConstants.O_COMPANY),
				csvLineContact.getString(OutlookConstants.O_JOB_TITLE) 
		};
		// Get organizationFieldComparator
		MoreFieldsComparator orgComparator = new MoreFieldsComparator(androidOrganization, csvOrganization, matchType);
		// Check if the contacts are equal
		if(orgComparator.areOrganisationsEqual()) {
			updateOutlookItems.setNewOrganization(false);
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
	private void synchronizeEmails(Cursor c, ArrayList<ContentProviderOperation> ops) {
		String email = "";
		if(!isColumnNull(c, Email.DATA1)) {
			email = getColumnString(c, Email.DATA1);
			if(!csvLineContact.getString(OutlookConstants.O_EMAIL1).equals("")) {
				if(email.equals(csvLineContact.getString(OutlookConstants.O_EMAIL1)))
					updateOutlookItems.setNewEmail1(false);
			}
			if(!csvLineContact.getString(OutlookConstants.O_EMAIL2).equals("")) {
				if(email.equals(csvLineContact.getString(OutlookConstants.O_EMAIL2)))
					updateOutlookItems.setNewEmail2(false);
			}
			if(!csvLineContact.getString(OutlookConstants.O_EMAIL3).equals("")) {
				if(email.equals(csvLineContact.getString(OutlookConstants.O_EMAIL3)))
					updateOutlookItems.setNewEmail3(false);
			}
		}
	}
	
	/**
	 * Method overwrites Name fields of Android from the CSV file. There are pairs of names, and if CSV pair is 
	 * not empty, then CSV field is written to the Android Data Table regardless of value in this Android Field.
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
		
		// If fields in CSV file is not empty, then I overwrite his pair in Android RawContacts Data Table					
		if(!csvLineContact.getString(OutlookConstants.O_GIVEN_NAME).equals("")) 
			bd.withValue(StructuredName.GIVEN_NAME, csvLineContact.getString(OutlookConstants.O_GIVEN_NAME));	
		if(!csvLineContact.getString(OutlookConstants.O_FAMILY_NAME).equals(""))
			bd.withValue(StructuredName.FAMILY_NAME, csvLineContact.getString(OutlookConstants.O_FAMILY_NAME));
		if(!csvLineContact.getString(OutlookConstants.O_TITLE).equals(""))
			bd.withValue(StructuredName.PREFIX, csvLineContact.getString(OutlookConstants.O_TITLE));
		if(!csvLineContact.getString(OutlookConstants.O_MIDDLE_NAME).equals(""))
			bd.withValue(StructuredName.MIDDLE_NAME, csvLineContact.getString(OutlookConstants.O_MIDDLE_NAME));
		if(!csvLineContact.getString(OutlookConstants.O_SUFFIX).equals(""))
			bd.withValue(StructuredName.SUFFIX, csvLineContact.getString(OutlookConstants.O_SUFFIX));
		if(bd != null) {
			ops.add(bd.build());
		}
		return;
	}
	
	/**
	 * Method creates new contacts from CSV Type row.
	 * @param csvContact - object that hold all CSV fields row
	 */
	public void createNewContact(CsvContact csvContact, String accName, String accType) {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int rawIndex = ops.size();
		// Vytvor novy zaznam v RawContact
		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_NAME, accName)
				.withValue(RawContacts.ACCOUNT_TYPE, accType).build());
		
		if(csvContact.checkStructureNameOutlook())
			ops.add(createStructuredNameBuilder(rawIndex, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_EMAIL1).equals(""))
			ops.add(createEmailBuilder(rawIndex, OutlookConstants.O_EMAIL1, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_EMAIL2).equals(""))
			ops.add(createEmailBuilder(rawIndex, OutlookConstants.O_EMAIL2, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_EMAIL3).equals(""))
			ops.add(createEmailBuilder(rawIndex, OutlookConstants.O_EMAIL3, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_WORK_FAX).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_WORK_FAX, Phone.TYPE_FAX_WORK, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_WORK_PHONE).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_WORK_PHONE, Phone.TYPE_WORK, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_WORK_PHONE2).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_WORK_PHONE2, Phone.TYPE_WORK, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_HOME_FAX).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_HOME_FAX, Phone.TYPE_FAX_HOME, ImportCostants.NEW_INSERT));			
		if(!csvContact.getString(OutlookConstants.O_HOME_PHONE).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_HOME_PHONE, Phone.TYPE_HOME, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_HOME_PHONE2).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_HOME_PHONE2, Phone.TYPE_HOME, ImportCostants.NEW_INSERT));	
		if(!csvContact.getString(OutlookConstants.O_MOBILE).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_MOBILE, Phone.TYPE_MOBILE, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_PAGER).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_PAGER, Phone.TYPE_PAGER, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_OTHER_PHONE).equals(""))
			ops.add(createPhoneBuilder(rawIndex, OutlookConstants.O_OTHER_PHONE, Phone.TYPE_OTHER, ImportCostants.NEW_INSERT));
		if(csvContact.checkPostalHomeOutlook())
			ops.add(createStructuredPostalBuilder(rawIndex, ImportCostants.NEW_INSERT, StructuredPostal.TYPE_HOME));
		if(csvContact.checkPostalWorkOutlook())
			ops.add(createStructuredPostalBuilder(rawIndex, ImportCostants.NEW_INSERT, StructuredPostal.TYPE_WORK));
		if(csvContact.checkPostalOtherOutlook())
			ops.add(createStructuredPostalBuilder(rawIndex, ImportCostants.NEW_INSERT, StructuredPostal.TYPE_OTHER));
		if(csvContact.checkOrganizationOutlook())
			ops.add(createOrganizationBuilder(rawIndex, ImportCostants.NEW_INSERT));
		if(!csvContact.getString(OutlookConstants.O_NOTE).equals(""))
			if(!csvContact.getString(OutlookConstants.O_NOTE).equals(NOTE_UNIQUE_NAME))
				ops.add(createNoteBuilder(rawIndex, OutlookConstants.O_NOTE, ImportCostants.NEW_INSERT));
		if(csvContact.getLength() == OutlookConstants.OUTLOOK_ITEM_COUNT)
			if(!csvContact.getString(OutlookConstants.O_WEB_PAGE).equals(""))
				ops.add(createWebSite(rawIndex, OutlookConstants.O_WEB_PAGE, ImportCostants.NEW_INSERT));			
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
		
		if(!csvLineContact.getString(OutlookConstants.O_TITLE).equals(""))
			bd.withValue(StructuredName.PREFIX, csvLineContact.getString(OutlookConstants.O_TITLE));
		if(!csvLineContact.getString(OutlookConstants.O_GIVEN_NAME).equals(""))
			bd.withValue(StructuredName.GIVEN_NAME, csvLineContact.getString(OutlookConstants.O_GIVEN_NAME));
		if(!csvLineContact.getString(OutlookConstants.O_MIDDLE_NAME).equals(""))
			bd.withValue(StructuredName.MIDDLE_NAME, csvLineContact.getString(OutlookConstants.O_MIDDLE_NAME));
		if(!csvLineContact.getString(OutlookConstants.O_FAMILY_NAME).equals(""))
			bd.withValue(StructuredName.FAMILY_NAME, csvLineContact.getString(OutlookConstants.O_FAMILY_NAME));
		if(!csvLineContact.getString(OutlookConstants.O_SUFFIX).equals(""))
			bd.withValue(StructuredName.SUFFIX, csvLineContact.getString(OutlookConstants.O_SUFFIX));
				
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
			// TODO Premazat policka z Adresy, kedze prepisujem... moze sa stat, ze tam neprepisem policko, ktore obsahuje hodnotu.
			// TODO Mozno prerobit
			if(!csvLineContact.getString(OutlookConstants.O_HOME_STREET).equals(""))
				bd.withValue(StructuredPostal.STREET, csvLineContact.getString(OutlookConstants.O_HOME_STREET));
			if(!csvLineContact.getString(OutlookConstants.O_HOME_CITY).equals(""))
				bd.withValue(StructuredPostal.CITY, csvLineContact.getString(OutlookConstants.O_HOME_CITY));
			if(!csvLineContact.getString(OutlookConstants.O_HOME_REGION).equals(""))	
				bd.withValue(StructuredPostal.REGION, csvLineContact.getString(OutlookConstants.O_HOME_REGION));
			if(!csvLineContact.getString(OutlookConstants.O_HOME_PSC).equals(""))	
				bd.withValue(StructuredPostal.POSTCODE, csvLineContact.getString(OutlookConstants.O_HOME_PSC));
			if(!csvLineContact.getString(OutlookConstants.O_HOME_COUNTRY).equals(""))	
				bd.withValue(StructuredPostal.COUNTRY, csvLineContact.getString(OutlookConstants.O_HOME_COUNTRY));
		}
		else if(postalType == (StructuredPostal.TYPE_WORK)) {
			// TODO Premazat policka z Adresy, kedze prepisujem... moze sa stat, ze tam neprepisem policko, ktore obsahuje hodnotu.
			if(!csvLineContact.getString(OutlookConstants.O_WORK_STREET).equals(""))
				bd.withValue(StructuredPostal.STREET, csvLineContact.getString(OutlookConstants.O_WORK_STREET));
			if(!csvLineContact.getString(OutlookConstants.O_WORK_CITY).equals(""))
				bd.withValue(StructuredPostal.CITY, csvLineContact.getString(OutlookConstants.O_WORK_CITY));
			if(!csvLineContact.getString(OutlookConstants.O_WORK_REGION).equals(""))	
				bd.withValue(StructuredPostal.REGION, csvLineContact.getString(OutlookConstants.O_WORK_REGION));
			if(!csvLineContact.getString(OutlookConstants.O_WORK_PSC).equals(""))	
				bd.withValue(StructuredPostal.POSTCODE, csvLineContact.getString(OutlookConstants.O_WORK_PSC));
			if(!csvLineContact.getString(OutlookConstants.O_WORK_COUNTRY).equals(""))	
				bd.withValue(StructuredPostal.COUNTRY, csvLineContact.getString(OutlookConstants.O_WORK_COUNTRY));
		}
		else if(postalType == (StructuredPostal.TYPE_OTHER)) {
			// TODO Premazat policka z Adresy, kedze prepisujem... moze sa stat, ze tam neprepisem policko, ktore obsahuje hodnotu.
			if(!csvLineContact.getString(OutlookConstants.O_OTHER_STREET).equals(""))
				bd.withValue(StructuredPostal.STREET, csvLineContact.getString(OutlookConstants.O_OTHER_STREET));
			if(!csvLineContact.getString(OutlookConstants.O_OTHER_CITY).equals(""))
				bd.withValue(StructuredPostal.CITY, csvLineContact.getString(OutlookConstants.O_OTHER_CITY));
			if(!csvLineContact.getString(OutlookConstants.O_OTHER_REGION).equals(""))	
				bd.withValue(StructuredPostal.REGION, csvLineContact.getString(OutlookConstants.O_OTHER_REGION));
			if(!csvLineContact.getString(OutlookConstants.O_OTHER_PSC).equals(""))	
				bd.withValue(StructuredPostal.POSTCODE, csvLineContact.getString(OutlookConstants.O_OTHER_PSC));
			if(!csvLineContact.getString(OutlookConstants.O_OTHER_COUNTRY).equals(""))	
				bd.withValue(StructuredPostal.COUNTRY, csvLineContact.getString(OutlookConstants.O_OTHER_COUNTRY));
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
		
		if(!csvLineContact.getString(OutlookConstants.O_JOB_TITLE).equals(""))
			bd.withValue(Organization.TITLE, csvLineContact.getString(OutlookConstants.O_JOB_TITLE));
		if(!csvLineContact.getString(OutlookConstants.O_DEPARTMENT).equals(""))
			bd.withValue(Organization.DEPARTMENT, csvLineContact.getString(OutlookConstants.O_DEPARTMENT));
		if(!csvLineContact.getString(OutlookConstants.O_COMPANY).equals(""))	
			bd.withValue(Organization.COMPANY, csvLineContact.getString(OutlookConstants.O_COMPANY));
		return bd.build();
	}
	
	/**
	 * Method creates all possible display name combinations and return them in List of strings.
	 * @return 	created list of all possible display name combinations
	 */
	protected List<String> createDisplaysNames() {
		List<String> list = new ArrayList<String>();
		
		// NAME + SURNAME
		String name1 = csvLineContact.getString(OutlookConstants.O_GIVEN_NAME) + " " + csvLineContact.getString(OutlookConstants.O_FAMILY_NAME);
		
		// SURNAME + NAME
		String name2 = (csvLineContact.getString(OutlookConstants.O_FAMILY_NAME) + " " + csvLineContact.getString(OutlookConstants.O_GIVEN_NAME));
		
		// "NAME MIDDLE_NAME SURNAME, SUFFIX"
		String name3 = (csvLineContact.getString(OutlookConstants.O_GIVEN_NAME) + " " 
						+ ( (csvLineContact.getString(OutlookConstants.O_MIDDLE_NAME).equals("")) ? csvLineContact.getString(OutlookConstants.O_MIDDLE_NAME) + " " : "") 
						+ csvLineContact.getString(OutlookConstants.O_FAMILY_NAME) 
						+ ( (csvLineContact.getString(OutlookConstants.O_SUFFIX).equals("")) ? ", " + csvLineContact.getString(OutlookConstants.O_SUFFIX) : "" )
						);
		
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
					csvLineContact.getString(OutlookConstants.O_TITLE), 
					csvLineContact.getString(OutlookConstants.O_GIVEN_NAME) , 
					csvLineContact.getString(OutlookConstants.O_MIDDLE_NAME), 
					csvLineContact.getString(OutlookConstants.O_FAMILY_NAME), 
					csvLineContact.getString(OutlookConstants.O_SUFFIX)
			};
			
			MoreFieldsComparator name = new MoreFieldsComparator(s, s1, matchType);
			// Min fields to be equal... there is tolerancy if there are only in GIVEN NAME EQUAL
			if(name.areNamesEqual(new boolean[]{false,true,false,true,false})) {
				setOfDisplayNameEqualContactsId.add(new Long(getColumnLong(c, Data.CONTACT_ID)));
				if(debug) Log.v(TAG, c.getString(c.getColumnIndex(StructuredName.DISPLAY_NAME))  + " ID: " + String.valueOf(getColumnLong(c, Data.CONTACT_ID)));
				break;
			}
		}
		c.close();
	}
}
