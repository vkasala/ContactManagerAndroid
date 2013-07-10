package bp.iemanager.exporter;

import java.util.ArrayList;
import bp.iemanager.MessageObject;
import bp.iemanager.csvcontact.CsvContact;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.RawContacts.Entity;
import android.util.Log;

/**
 * Abstract class containing all needed methods, attributes to export contacts into different CSV files.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public abstract class Exporter {
	/*
	 * Flag for exporting duplicate contact.
	 */
	protected boolean fitFields;
	protected static final String TAG = "Export";
	/*
	 * Atribute for writing CSV contact to file.
	 */
	protected MyFileWriter fileWriter;
	
	/*
	 * File name of exporting file.
	 */
	protected String newFileName;
	
	/*
	 * List of CSV Contacts.
	 */
	protected ArrayList<CsvContact> listOfCsvContacts;
	
	protected Context context;
	
	/*
	 * Picked options.
	 */
	protected boolean[] options;
	
	/*
	 * Unique name for duplicate contacts.
	 */
	protected final String noteName = "#NOTE@DUPLICATE@CONTACT#";
	
	/*
	 * ID for options.
	 */
	protected static final int OPTIONS_PHONE = 0;
	protected static final int OPTIONS_EMAIL = 1;
	protected static final int OPTIONS_ADDRESS = 2;
	protected static final int OPTIONS_ORGANIZATION = 7;
	protected static final int OPTIONS_IM = 4;
	protected static final int OPTIONS_NOTE = 5;
	protected static final int OPTIONS_NICKNAME = 6;
	protected static final int OPTIONS_WEBPAGE = 3;
	
	/*
	 * Selection for query.
	 */
	protected String selection;
	
	/*
	 * Selection arguments for query.
	 */
	protected String[] selectionArgs;
	
	/*
	 * Handler for progress dialog.
	 */
	protected Handler mHandler;
	
	/*
	 * Number of exported contacts.
	 */
	protected int numberOfExportedContacts;
	/*
	 * Display name.
	 */
	protected String displayName;
	/*
	 * Flag for removing accents characters.
	 */
	protected boolean removeFlag;
	
	public Exporter(Context _ctx, boolean[] _options, boolean _removeFlag) {
		context = _ctx;
		options = _options;
		newFileName = null;
		listOfCsvContacts = new ArrayList<CsvContact>();
		removeFlag = _removeFlag;
		fileWriter = null;
		selection = null;
		selectionArgs = null;
		mHandler = null;
		numberOfExportedContacts = 0;
		displayName = "";
	}
	
	/**
	 * Start to import all contacts saved in database of Android OS.
	 */
	public void startExport() {
		queryAllRawContacts();
	}
	
	/**
	 * Method get first CSV contact.
	 * @return CsvContact
	 */
	public CsvContact getFirstCsvLine() {
		return ((listOfCsvContacts.isEmpty() == true) ?  null : listOfCsvContacts.get(0));
	}
	
	/**
	 * Method returns all CsvContacts.
	 * @return list of CsvContacts.
	 */
	public ArrayList<CsvContact> getArrayList() {
		return (listOfCsvContacts.isEmpty() == true) ? null : listOfCsvContacts;
	}
	
	/**
	 * Method creates and return cursor of all RawContacts.
	 * @return cursor of all finded raw contacts.
	 */
	protected Cursor getCursorOfAllRawContacts() {
		return context.getContentResolver().query(RawContacts.CONTENT_URI, 
				new String[] { RawContacts._ID }, 
				RawContacts.DELETED + " = ? AND " + " ( " + selection + " ) ", 
				concat(new String[] {"0"}, selectionArgs), 
				null);
	}
	
	/**
	 * Method finds out count of exported contacts. 
	 * @return number of exported contacts.
	 */
	public int getExportedContactNumber() {
		Cursor c = null;
		try{
			c = getCursorOfAllRawContacts();
			return c.getCount();
		} finally {
			// Close the used cursor
			if(c != null)
				c.close();
		}
	}
	
	/**
	 * Build and send message to the handler(Progressdialog)
	 */
	protected void sendMessageToHandler() {
		Message msg = mHandler.obtainMessage();
		msg.obj = (MessageObject) new MessageObject(++numberOfExportedContacts, "Exporting:\n " + displayName);
		mHandler.sendMessage(msg);
	}
	
	/**
	 * Build and send exit message to the handler(Progressdialog)
	 */
	protected void sendExitMessageToHandler() {
		Message msg = mHandler.obtainMessage();
		msg.obj = (MessageObject) new MessageObject(-1,"UNIQUE^@&!EXIT_MEASSAGE");
		mHandler.sendMessage(msg);
	}
	
	/**
	 * Gets all rawContacts and step by step gets the detailed information about rawContact and update progressbar.
	 */
	protected void queryAllRawContacts() {
		Cursor c = null;
		try {
			c = getCursorOfAllRawContacts();
			if(c.getCount() == 0) {
				sendMessageToHandler();
			}
			// iterate step by step through all index and retrieve detailed information about the contact and write them to file
			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {		
				listOfCsvContacts.clear();	
				queryAllDetailedInformation(getColumnLong(c, RawContacts._ID));
				sendMessageToHandler();
			}
		} finally {
			// Uzavretie kurzora
			if(c != null)
				c.close();
		}
	}
	
	
	/**
	 * Method gets all items of raw contact and writes them to arraylist.
	 * @param rawContactId - ID RawContact-u
	 */
	public void queryAllDetailedInformation(long rawContactId) {
		listOfCsvContacts.add(createNewCsvContact());
		
		Uri rawContactsUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
		Uri entityUri = Uri.withAppendedPath(rawContactsUri, Entity.CONTENT_DIRECTORY);
		Cursor c = null;
		
		try {
			c = context.getContentResolver().query(entityUri, 
					null, 
					null, null, null);

			for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {			
				String s = getColumnString(c, Entity.MIMETYPE);
				Log.v(TAG, s);
				
				// Zaznam o Mene
				if(s.equals(StructuredName.CONTENT_ITEM_TYPE)) 
					processStructuredName(c);
				
				else if (s.equals(Organization.CONTENT_ITEM_TYPE)) {
					if(options[OPTIONS_ORGANIZATION])
						processOrganization(c);						
				}
				// Zaznam o Adrese
				else if(s.equals(StructuredPostal.CONTENT_ITEM_TYPE)){
					if(options[OPTIONS_ADDRESS])
						processStructuredPostal(c);
				}
				// Zaznam o Telefone
				else if(s.equals(Phone.CONTENT_ITEM_TYPE)) {
					if(options[OPTIONS_PHONE])
						processPhone(c);
				}
				// Zaznam o Email-och
				else if(s.equals(Email.CONTENT_ITEM_TYPE)) {
					if(options[OPTIONS_EMAIL])
						processEmail(c);
				}
				// Zaznam o Web strankach
				else if(s.equals(Website.CONTENT_ITEM_TYPE)){
					if(options[OPTIONS_WEBPAGE])
						processWebPages(c);
				}
				
				else if(s.equals(Nickname.CONTENT_ITEM_TYPE)) {
					processNickName(c);
				}
				// Zaznam o Poznamkach
				else if(s.equals(Note.CONTENT_ITEM_TYPE)){
					if(options[OPTIONS_NOTE])
						processNote(c);
				}
			}
			if(fileWriter != null) {
				writeContactToFile();
			}
			
		} finally {
			if(c != null)
				c.close();
		}
	}
	
	protected abstract CsvContact createNewCsvContact();
	protected abstract void editArrayListItems();
	protected abstract void processStructuredName(Cursor c);
	protected abstract void processEmail(Cursor c);
	protected abstract void processPhone(Cursor c);
	protected abstract void processNote(Cursor c);
	protected abstract void processWebPages(Cursor c);
	protected abstract void processOrganization(Cursor c);
	protected abstract void processStructuredPostal(Cursor c);
	protected abstract void processNickName(Cursor c);
	
	/**
	 * Writes all contacts to file.
	 */
	protected void writeContactToFile() {
		if(listOfCsvContacts.size() > 1) {
			editArrayListItems();
		}
		if(removeFlag)
			replaceAllAccentChars();
		int i = 0;
		for (CsvContact csvLine : listOfCsvContacts) {
			if(fitFields && i != 0)
				break;
			fileWriter.writeStrings(csvLine.getArrayOfStrings());
			i++;
		}
	}
	
	/**
	 * Method replace all Accent chars with it's relevant char in Ascii code
	 */
	protected void replaceAllAccentChars() {
		for (CsvContact csvLine : listOfCsvContacts) {
			csvLine.removeAccentChars();
		}
	}
	
	protected static String[] concat(String[] A, String[] B) {
		   String[] C = new String[A.length + B.length];
		   System.arraycopy(A, 0, C, 0, A.length);
		   System.arraycopy(B, 0, C, A.length, B.length);
		   return C;
	}
	
	protected static String getColumnString(Cursor c, String columnName) {
		return c.getString(c.getColumnIndex(columnName));
	}
	
	protected static long getColumnLong(Cursor c, String columnName) {
		return c.getLong(c.getColumnIndex(columnName));
	}
	
	protected static boolean isColumnNull(Cursor c, String columnName) {
		return c.isNull(c.getColumnIndex(columnName));
	}
	
	protected static int getColumnInt(Cursor c, String columnName) {
		return c.getInt(c.getColumnIndex(columnName));
	}
	
}
