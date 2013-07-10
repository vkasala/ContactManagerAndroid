package bp.iemanager.exporter;

import java.util.Iterator;

import sk.kasala.viliam.bakalarka.R;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.ThunderbirdConstants;

/**
 * Class for exporting to Thunderbird CSV format.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class ThunderbirdExporter extends Exporter {
	
	public ThunderbirdExporter(Context _ctx, boolean[] _options, boolean _removeFlag) {
		super(_ctx, _options, _removeFlag);
	}
	
	public ThunderbirdExporter(MyFileWriter _fileWriter, Context _ctx, String _selection, String[] _selectionArgs, boolean[] _options, Handler _h, boolean _removeFlag, boolean _fitFields) {
		super(_ctx, _options, _removeFlag);
		mHandler = _h;
		selection = _selection;
		selectionArgs = _selectionArgs;	
		// Objekt pre zapisovanie dat
		fileWriter = _fileWriter;
		// Nakopiruj sablonu csv Thunderbird suboru do noveho suboru.
		fileWriter.copyRawFileToNewFile(context.getResources().openRawResource(R.raw.template_thunderbird));
		fitFields = _fitFields;
	}
	
	/**
	 * Method create new instance of CsvContact type Outlook
	 */
	protected CsvContact createNewCsvContact() {
		// New CsvLine object, which encapsulate one row of Outlook CSV file format
		return new CsvContact(ThunderbirdConstants.THUDERBIRD_ITEM_COUNT);
	}
	
	/**
	 * Method copies names from first CsvContact to other from list of CsvContact, if there are more CsvContact
	 * from list and also sets item Note with unique sets. 
	 */
	protected void editArrayListItems() {
		boolean firstLine = true;
		String[] nameArray = new String[3];
		String email1 = "";
		String email2 = "";
		for (Iterator<CsvContact> iterator = listOfCsvContacts.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			if(firstLine) {
				for (int j = 0; j < nameArray.length; j++) {
					nameArray[j] = csvLine.getString(j);
				}
				email1 = csvLine.getString(ThunderbirdConstants.T_EMAIL1);
				email2 = csvLine.getString(ThunderbirdConstants.T_EMAIL2);
				firstLine = false;
			}
			else {
				for (int j = 0; j < nameArray.length; j++) {
					csvLine.setString(j, nameArray[j]);
				}
				csvLine.setString(ThunderbirdConstants.T_OTHER, email1);
				csvLine.setString(ThunderbirdConstants.T_OTHER2, email2);
				csvLine.setString(ThunderbirdConstants.T_NOTE, noteName);
			}
		}
	}
	
	/**
	 * Method saves information about name from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processStructuredName(Cursor c) {
		if(!isColumnNull(c, StructuredName.GIVEN_NAME))
			saveStringToArrayList(getColumnString(c, StructuredName.GIVEN_NAME), ThunderbirdConstants.T_GIVEN_NAME);
		if(!isColumnNull(c, StructuredName.FAMILY_NAME))
			saveStringToArrayList(getColumnString(c, StructuredName.FAMILY_NAME), ThunderbirdConstants.T_FAMILY_NAME);
		if(!isColumnNull(c, StructuredName.DISPLAY_NAME)) {
			saveStringToArrayList(getColumnString(c, StructuredName.DISPLAY_NAME), ThunderbirdConstants.T_DISPLAY_NAME);
			displayName = getColumnString(c, StructuredName.DISPLAY_NAME);
		}
		else {
			displayName = listOfCsvContacts.get(0).getString(ThunderbirdConstants.T_GIVEN_NAME) + " " + listOfCsvContacts.get(0).getString(ThunderbirdConstants.T_FAMILY_NAME).trim();
		}
		return;
	}
	
	/**
	 * Method saves information about email from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processEmail(Cursor c) {	
		 if(!isColumnNull(c, Email.DATA))
			saveStringToArrayList(getColumnString(c, Email.DATA), ThunderbirdConstants.T_EMAIL1);
		 return;
	}
	
	/**
	 * Method saves information about phones from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processPhone(Cursor c) {		
		if(!isColumnNull(c, Phone.NUMBER)) {
			switch(getColumnInt(c, Phone.TYPE)) {
				case Phone.TYPE_HOME: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), ThunderbirdConstants.T_HOME_PHONE);
					break;
				}
				case Phone.TYPE_WORK: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), ThunderbirdConstants.T_WORK_PHONE);
					break;
				}
				case Phone.TYPE_MOBILE: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), ThunderbirdConstants.T_MOBILE);
					break;
				}
				case Phone.TYPE_FAX_HOME:
				case Phone.TYPE_FAX_WORK: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), ThunderbirdConstants.T_FAX);
					break;
				}
				case Phone.TYPE_PAGER: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), ThunderbirdConstants.T_PAGER);
					break;
				}
				default:
					break;
				}								
			}
	}
	
	/**
	 * Method saves information about NickName from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processNickName(Cursor c) {
		if(!isColumnNull(c, Nickname.NAME))
			saveStringToArrayList(getColumnString(c, Nickname.NAME), ThunderbirdConstants.T_NICKNAME);	
	}
	
	/**
	 * Method saves information about NOTE from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processNote(Cursor c) {
		if(!isColumnNull(c, Note.NOTE))
			saveStringToArrayList(getColumnString(c, Note.NOTE), ThunderbirdConstants.T_NOTE);	
	}
	
	/**
	 * Method saves information about Web Page from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processWebPages(Cursor c) {
		if(!isColumnNull(c, Website.URL))
			saveStringToArrayList(getColumnString(c, Website.URL), ThunderbirdConstants.T_WEB_ADDRESS);	
	}
	
	/**
	 * Method saves information about Organization from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processOrganization(Cursor c) {
		if(!isColumnNull(c, Organization.COMPANY))
			saveStringToArrayList(getColumnString(c, Organization.COMPANY), ThunderbirdConstants.T_COMPANY);	
		if(!isColumnNull(c, Organization.TITLE))
			saveStringToArrayList(getColumnString(c, Organization.TITLE), ThunderbirdConstants.T_JOB_TITLE);	
	}
	
	/**
	 * Method saves information about postal addresses from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processStructuredPostal(Cursor c) {
		switch(getColumnInt(c, StructuredPostal.TYPE)) {
			case (StructuredPostal.TYPE_HOME): {
				if(!isColumnNull(c, StructuredPostal.STREET))
					saveStringToArrayList(getColumnString(c, StructuredPostal.STREET), ThunderbirdConstants.T_HOME_STREET);
				// TODO STREE + POBOX do kolonky ULICA
				/*if(!isColumnNull(c, StructuredPostal.POBOX))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POBOX), ThunderbirdConstants.T_HOME_PSC);	*/
				if(!isColumnNull(c, StructuredPostal.CITY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.CITY), ThunderbirdConstants.T_HOME_CITY);	
				if(!isColumnNull(c, StructuredPostal.REGION))
					saveStringToArrayList(getColumnString(c, StructuredPostal.REGION), ThunderbirdConstants.T_HOME_REGION);	
				if(!isColumnNull(c, StructuredPostal.POSTCODE))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POSTCODE), ThunderbirdConstants.T_HOME_PSC);				
				if(!isColumnNull(c, StructuredPostal.COUNTRY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.COUNTRY), ThunderbirdConstants.T_HOME_COUNTRY);			
				break;
			}
			case (StructuredPostal.TYPE_WORK): {
				if(!isColumnNull(c, StructuredPostal.STREET))
					saveStringToArrayList(getColumnString(c, StructuredPostal.STREET), ThunderbirdConstants.T_WORK_STREET);
				// TODO STREE + POBOX do kolonky ULICA
				/*if(!isColumnNull(c, StructuredPostal.POBOX))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POBOX), ThunderbirdConstants.T_HOME_PSC);	*/
				if(!isColumnNull(c, StructuredPostal.CITY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.CITY), ThunderbirdConstants.T_WORK_CITY);	
				if(!isColumnNull(c, StructuredPostal.REGION))
					saveStringToArrayList(getColumnString(c, StructuredPostal.REGION), ThunderbirdConstants.T_WORK_REGION);	
				if(!isColumnNull(c, StructuredPostal.POSTCODE))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POSTCODE), ThunderbirdConstants.T_WORK_PSC);				
				if(!isColumnNull(c, StructuredPostal.COUNTRY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.COUNTRY), ThunderbirdConstants.T_WORK_COUNTRY);			
				break;
			}
			default: 
				break;
		}
	}
	
	/**
	 * Method saves string to CsvContact to specified position. If the position is already full, 
	 * then it creates new CsvContact and saved it there.
	 * @param string - string which we will be saved.
	 * @param position - postion where should be the string saved.
	 */
	protected void saveStringToArrayList(String string, int position) {
		boolean saved = false;
		for (Iterator<CsvContact> iterator = listOfCsvContacts.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			if(csvLine.isNull(position)) {
				csvLine.setString(position, string);
				saved = true;
				break;
			}
			else if(!csvLine.isNull(position)) {  
				if(ThunderbirdConstants.T_EMAIL1 == position) {
					if(csvLine.isNull(ThunderbirdConstants.T_EMAIL2)) {
						csvLine.setString(ThunderbirdConstants.T_EMAIL2, string);
						saved = true;
						break;
					}
				}
				else if(ThunderbirdConstants.T_WEB_ADDRESS == position) {
					if(csvLine.isNull(ThunderbirdConstants.T_WEB_ADDRESS2)) {
						csvLine.setString(ThunderbirdConstants.T_WEB_ADDRESS2, string);
						saved = true;
						break;
					}
				}
			}
		}
		
		if(saved == false) {
			CsvContact csvLine = new CsvContact(ThunderbirdConstants.THUDERBIRD_ITEM_COUNT);
			csvLine.setString(position, string);
			listOfCsvContacts.add(csvLine);
		}
		return;
	}
}
