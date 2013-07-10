package bp.iemanager.exporter;

import java.util.Iterator;

import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.OutlookConstants;

import sk.kasala.viliam.bakalarka.R;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;

/**
 * Class for exporting to Outlook CSV format.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class OutlookExporter extends Exporter {
	
	public OutlookExporter(Context _ctx, boolean[] _options, boolean _removeFlag) {
		super(_ctx, _options, _removeFlag);
	}
	
	public OutlookExporter(MyFileWriter _fileWriter, Context _ctx, String _selection, String[] _selectionArgs, boolean[] _options, Handler _h, boolean _removeFlag, boolean _fitFields) {
		super(_ctx, _options, _removeFlag);
		mHandler = _h;
		fileWriter = _fileWriter;
		fileWriter.copyRawFileToNewFile(context.getResources().openRawResource(R.raw.template_outlook));
		selection = _selection;
		selectionArgs = _selectionArgs;
		fitFields = _fitFields;
	}
	
	
	/**
	 * Method create new instance of CsvContact type Outlook
	 */
	protected CsvContact createNewCsvContact() {
		// New CsvLine object, which encapsulate one row of Outlook CSV file format
		return new CsvContact(OutlookConstants.OUTLOOK_ITEM_COUNT);
	}
		
	protected void processNickName(Cursor c) {
		return;
	}
	
	/**
	 * Method copies names from first CsvContact to other from list of CsvContact, if there are more CsvContact
	 * from list and also sets item Note with unique sets. 
	 */
	protected void editArrayListItems() {
		boolean firstLine = true;
		String[] nameArray = new String[5];
		String email1 = "";
		String email2 = "";
		String email3 = "";
		for (Iterator<CsvContact> iterator = listOfCsvContacts.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			if(firstLine) {
				for (int j = 0; j < nameArray.length; j++) {
					nameArray[j] = csvLine.getString(j);
				}
				email1 = csvLine.getString(OutlookConstants.O_EMAIL1);
				email2 = csvLine.getString(OutlookConstants.O_EMAIL2);
				email3 = csvLine.getString(OutlookConstants.O_EMAIL3);
				firstLine = false;
			}
			else {
				for (int j = 0; j < nameArray.length; j++) {
					csvLine.setString(j, nameArray[j]);
				}
				csvLine.setString(OutlookConstants.O_USER1, email1);
				csvLine.setString(OutlookConstants.O_USER2, email2);
				csvLine.setString(OutlookConstants.O_USER3, email3);
				csvLine.setString(OutlookConstants.O_NOTE, noteName);
			}
		}
	}

	/**
	 * Method saves information about name from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processStructuredName(Cursor c) {
		if(!isColumnNull(c, StructuredName.GIVEN_NAME))
			saveStringToArrayList(getColumnString(c, StructuredName.GIVEN_NAME), OutlookConstants.O_GIVEN_NAME);
		if(!isColumnNull(c, StructuredName.FAMILY_NAME))
			saveStringToArrayList(getColumnString(c, StructuredName.FAMILY_NAME), OutlookConstants.O_FAMILY_NAME);
		if(!isColumnNull(c, StructuredName.PREFIX))
			saveStringToArrayList(getColumnString(c, StructuredName.PREFIX), OutlookConstants.O_TITLE);
		if(!isColumnNull(c, StructuredName.MIDDLE_NAME))
			saveStringToArrayList(getColumnString(c, StructuredName.MIDDLE_NAME), OutlookConstants.O_MIDDLE_NAME);
		if(!isColumnNull(c, StructuredName.SUFFIX))
			saveStringToArrayList(getColumnString(c, StructuredName.SUFFIX), OutlookConstants.O_SUFFIX);
		
		if(!isColumnNull(c, StructuredName.DISPLAY_NAME))
			displayName = getColumnString(c, StructuredName.DISPLAY_NAME);
		else {
			CsvContact csvLineContacts = listOfCsvContacts.get(0);
			displayName = (csvLineContacts.getString(OutlookConstants.O_GIVEN_NAME) + " " 
					+ ( (csvLineContacts.getString(OutlookConstants.O_MIDDLE_NAME) != "") ? csvLineContacts.getString(OutlookConstants.O_MIDDLE_NAME) + " " : "") 
					+ csvLineContacts.getString(OutlookConstants.O_FAMILY_NAME) 
					+ ( (csvLineContacts.getString(OutlookConstants.O_SUFFIX) != "") ? ", " + csvLineContacts.getString(OutlookConstants.O_SUFFIX) : "" )
					);
			displayName = displayName.trim();
		}
		
	
	}
	
	/**
	 * Method saves information about email from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processEmail(Cursor c) {
		if(!isColumnNull(c, Email.DATA))
			saveStringToArrayList(getColumnString(c, Email.DATA), OutlookConstants.O_EMAIL1);
	}
	
	/**
	 * Method saves information about phones from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processPhone(Cursor c) {
		if(!isColumnNull(c, Phone.NUMBER)) {
			switch(getColumnInt(c, Phone.TYPE)) {
				case Phone.TYPE_HOME: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), OutlookConstants.O_HOME_PHONE);
					break;
				}
				case Phone.TYPE_WORK: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), OutlookConstants.O_WORK_PHONE);
					break;
				}
				case Phone.TYPE_MOBILE: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), OutlookConstants.O_MOBILE);
					break;
				}
				case Phone.TYPE_FAX_HOME: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), OutlookConstants.O_HOME_FAX);
					break;
				}
				case Phone.TYPE_FAX_WORK: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), OutlookConstants.O_WORK_FAX);
					break;
				}
				case Phone.TYPE_PAGER: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), OutlookConstants.O_PAGER);
					break;
				}
				case Phone.TYPE_OTHER: {
					saveStringToArrayList(getColumnString(c, Phone.NUMBER), OutlookConstants.O_OTHER_PHONE);
					break;
				}
				default:
					break;
				}								
			}			
	}
	
	/**
	 * Method saves information about NOTE from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processNote(Cursor c) {
		if(!isColumnNull(c, Note.NOTE))
			saveStringToArrayList(getColumnString(c, Note.NOTE), OutlookConstants.O_NOTE);	
	}
	
	/**
	 * Method saves information about Web Page from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processWebPages(Cursor c) {
		if(!isColumnNull(c, Website.URL))
			saveStringToArrayList(getColumnString(c, Website.URL), OutlookConstants.O_WEB_PAGE);	
	}
	
	/**
	 * Method saves information about Organization from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processOrganization(Cursor c) {
		if(!isColumnNull(c, Organization.COMPANY))
			saveStringToArrayList(getColumnString(c, Organization.COMPANY), OutlookConstants.O_COMPANY);	
		if(!isColumnNull(c, Organization.TITLE))
			saveStringToArrayList(getColumnString(c, Organization.TITLE), OutlookConstants.O_JOB_TITLE);	
	}
	
	/**
	 * Method saves information about postal addresses from cursor to the CsvContact.
	 * @param c - cursor from we will read information.
	 */
	protected void processStructuredPostal(Cursor c) {
		switch(getColumnInt(c, StructuredPostal.TYPE)) {
			case (StructuredPostal.TYPE_HOME): {
				if(!isColumnNull(c, StructuredPostal.STREET))
					saveStringToArrayList(getColumnString(c, StructuredPostal.STREET), OutlookConstants.O_HOME_STREET);
				if(!isColumnNull(c, StructuredPostal.POBOX))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POBOX), OutlookConstants.O_HOME_POBOX);	
				if(!isColumnNull(c, StructuredPostal.CITY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.CITY), OutlookConstants.O_HOME_CITY);	
				if(!isColumnNull(c, StructuredPostal.REGION))
					saveStringToArrayList(getColumnString(c, StructuredPostal.REGION), OutlookConstants.O_HOME_REGION);	
				if(!isColumnNull(c, StructuredPostal.POSTCODE))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POSTCODE), OutlookConstants.O_HOME_PSC);				
				if(!isColumnNull(c, StructuredPostal.COUNTRY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.COUNTRY), OutlookConstants.O_HOME_COUNTRY);			
				break;
			}
			case (StructuredPostal.TYPE_WORK): {
				if(!isColumnNull(c, StructuredPostal.STREET))
					saveStringToArrayList(getColumnString(c, StructuredPostal.STREET), OutlookConstants.O_WORK_STREET);
				if(!isColumnNull(c, StructuredPostal.POBOX))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POBOX), OutlookConstants.O_WORK_POBOX);	
				if(!isColumnNull(c, StructuredPostal.CITY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.CITY), OutlookConstants.O_WORK_CITY);	
				if(!isColumnNull(c, StructuredPostal.REGION))
					saveStringToArrayList(getColumnString(c, StructuredPostal.REGION), OutlookConstants.O_WORK_REGION);	
				if(!isColumnNull(c, StructuredPostal.POSTCODE))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POSTCODE), OutlookConstants.O_WORK_PSC);				
				if(!isColumnNull(c, StructuredPostal.COUNTRY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.COUNTRY), OutlookConstants.O_WORK_COUNTRY);			
				break;
			}
			case (StructuredPostal.TYPE_OTHER): {
				if(!isColumnNull(c, StructuredPostal.STREET))
					saveStringToArrayList(getColumnString(c, StructuredPostal.STREET), OutlookConstants.O_OTHER_STREET);
				if(!isColumnNull(c, StructuredPostal.POBOX))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POBOX), OutlookConstants.O_OTHER_POBOX);	
				if(!isColumnNull(c, StructuredPostal.CITY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.CITY), OutlookConstants.O_OTHER_CITY);	
				if(!isColumnNull(c, StructuredPostal.REGION))
					saveStringToArrayList(getColumnString(c, StructuredPostal.REGION), OutlookConstants.O_OTHER_REGION);	
				if(!isColumnNull(c, StructuredPostal.POSTCODE))
					saveStringToArrayList(getColumnString(c, StructuredPostal.POSTCODE), OutlookConstants.O_OTHER_PSC);				
				if(!isColumnNull(c, StructuredPostal.COUNTRY))
					saveStringToArrayList(getColumnString(c, StructuredPostal.COUNTRY), OutlookConstants.O_OTHER_COUNTRY);			
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
				if(OutlookConstants.O_EMAIL1 == position) {
					if(csvLine.isNull(OutlookConstants.O_EMAIL2)) {
						csvLine.setString(OutlookConstants.O_EMAIL2, string);
						saved = true;
						break;
					}
					else if(csvLine.isNull(OutlookConstants.O_EMAIL3)) {
						csvLine.setString(OutlookConstants.O_EMAIL3, string);
						saved = true;
						break;
					}
				}
				else if(OutlookConstants.O_HOME_PHONE == position) {
					if(csvLine.isNull(OutlookConstants.O_HOME_PHONE2)) {
						csvLine.setString(OutlookConstants.O_HOME_PHONE2, string);
						saved = true;
						break;
					}
				}
				else if(OutlookConstants.O_WORK_PHONE == position) {
					if(csvLine.isNull(OutlookConstants.O_WORK_PHONE2)) {
						csvLine.setString(OutlookConstants.O_WORK_PHONE2, string);
						saved = true;
						break;
					}
				}
			}
		}
		
		if(saved == false) {
			CsvContact csvLine =  new CsvContact(OutlookConstants.OUTLOOK_ITEM_COUNT);
			csvLine.setString(position, string);
			listOfCsvContacts.add(csvLine);
		}
		return;
	}
}
