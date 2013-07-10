package bp.iemanager.csvcontact;

import java.io.Serializable;

import bp.iemanager.StringUtils;
import bp.iemanager.importer.MatchType;


/**
 * This class represents one CSV contact for row. 
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class CsvContact implements Serializable {
	/**
	 * Auto generated serial ID
	 */
	private static final long serialVersionUID = -1821639886723668862L;
	/**
	 * Array of strings of CSV items.
	 */
	protected String[] csvLine;
	/**
	 * Type of CSV contact - Outlook/Thunderbird
	 */
	protected int csvType;
		
	public CsvContact(String strings[]) {		
		createNewCopy(strings);
		setType();
	}
	
	public CsvContact(int _count) {
		csvLine = new String[_count];
		emptyStringsIfNull();
		setType();
	}
	
	/**
	 * Method removes accent chars.
	 */
	public void removeAccentChars() {
		for (int i = 0; i < csvLine.length; i++) {
			csvLine[i] = StringUtils.convertNonAscii(csvLine[i]);
		}
	}
	
	/**
	 * Method checks if csvLine object has at least one value to export
	 * @return true/false according the 
	 */
	public boolean hasAtLeastOneValue() {
		for (int i = 0; i < csvLine.length; i++) {
			if(!csvLine[i].equals(""))
				return true;
		}
		return false;
	}
	
	/**
	 * Creates new copy of string.
	 * @param strings - items of CSV contact.
	 */
	protected void createNewCopy(String[] strings) {
		if(strings.length > ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
			csvLine = new String[OutlookConstants.OUTLOOK_ITEM_COUNT];
			emptyStringsIfNull();
			for (int i = 0; i < strings.length; i++) {
				csvLine[i] = new String(strings[i]);
			}
		}
		else {
			csvLine = new String[ThunderbirdConstants.THUDERBIRD_ITEM_COUNT];
			emptyStringsIfNull();
			for (int i = 0; i < strings.length; i++) {
				csvLine[i] = new String(strings[i]);
			}
		}
	}
	
	/**
	 * Method null all CSV items.
	 */
	public void emptyStringsIfNull() {
		for (int i = 0; i < csvLine.length; i++) {
			if(csvLine[i] == null) {
				csvLine[i] = "";
			}
		}
	}
		
	/**
	 * Method compares string with string item in CSV contact.
	 * @param index - index of item in CSV contact.
	 * @param s - string which we want to compare.
	 * @return true/false if they are equal.
	 */
	public boolean equalTwoStrings(int index, String s) {
		return csvLine[index].equals(s);
	}
	
	/**
	 * Method return length of item specified by index.
	 * @param index - index of item in CSV contact.
	 * @return length of item.
	 */
	public int getStringLength(int index) {
		return csvLine[index].length();
	}
	
	/**
	 * Method removes all white spaces at the begining and at the end of all items.
	 */
	public void callTrimOn() {
		for (int i = 0; i < csvLine.length; i++) {
			csvLine[i] = csvLine[i].trim();
		}
	}
	
	public int getLength() {
		return csvLine.length;
	}
	
	public int getType() {
		return csvType;
	}
	
	private void setType() {
		if(csvLine.length == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT)
			csvType = ThunderbirdConstants.THUDERBIRD_ITEM_COUNT;
		else
			csvType = OutlookConstants.OUTLOOK_ITEM_COUNT;
	}
	
	/**
	 * Checks postal home address items of thunderbird contact.
	 * @return true/false if there is at least one home address item. 
	 */
	public boolean checkPostalHomeThunderbird() {
		
		return (!csvLine[ThunderbirdConstants.T_HOME_CITY].equals("") || !csvLine[ThunderbirdConstants.T_HOME_COUNTRY].equals("") ||  !csvLine[ThunderbirdConstants.T_HOME_PSC].equals("") || 
				 !csvLine[ThunderbirdConstants.T_HOME_REGION].equals("") ||  !csvLine[ThunderbirdConstants.T_HOME_STREET].equals("") );
	}
	
	/**
	 * Checks postal work address items of thunderbird contact.
	 * @return true/false if there is at least one work address item. 
	 */
	public boolean checkPostalWorkThunderbird() {
		
		return (!csvLine[ThunderbirdConstants.T_WORK_CITY].equals("") || !csvLine[ThunderbirdConstants.T_WORK_COUNTRY].equals("") ||  !csvLine[ThunderbirdConstants.T_WORK_PSC].equals("") || 
				 !csvLine[ThunderbirdConstants.T_WORK_REGION].equals("") ||  !csvLine[ThunderbirdConstants.T_WORK_STREET].equals("") );
	}
	
	/**
	 * Checks structure name items of thunderbird contact.
	 * @return true/false if there is at least one name item. 
	 */
	public boolean checkStructureNameThunderbird() {
		
		return (!csvLine[ThunderbirdConstants.T_GIVEN_NAME].equals("") || !csvLine[ThunderbirdConstants.T_DISPLAY_NAME].equals("") ||  !csvLine[ThunderbirdConstants.T_FAMILY_NAME].equals(""));
	}
	
	/**
	 * Checks organization items of thunderbird contact.
	 * @return true/false if there is at least one organization item. 
	 */
	public boolean checkOrganizationThunderbird() {
		
		return (!csvLine[ThunderbirdConstants.T_DEPARTMENT].equals("") || !csvLine[ThunderbirdConstants.T_COMPANY].equals("") ||  !csvLine[ThunderbirdConstants.T_JOB_TITLE].equals(""));
	}
	
	/**
	 * Checks email address items of thunderbird contact.
	 * @return true/false if there is at least one email address item. 
	 */
	public boolean checkEmailThunderbird() {
		return (!csvLine[ThunderbirdConstants.T_EMAIL1].equals("") || !csvLine[ThunderbirdConstants.T_EMAIL2].equals(""));
	}
	
	/**
	 * Checks website items of thunderbird contact.
	 * @return true/false if there is at least one website item. 
	 */
	public boolean checkWebsiteThunderbird() {
		return (!csvLine[ThunderbirdConstants.T_WEB_ADDRESS].equals("") || !csvLine[ThunderbirdConstants.T_WEB_ADDRESS2].equals(""));
	}
	
	/**
	 * Checks postal home address items of outlook contact.
	 * @return true/false if there is at least one home address item. 
	 */
	public boolean checkPostalHomeOutlook() {
		return (!csvLine[OutlookConstants.O_HOME_CITY].equals("") || !csvLine[OutlookConstants.O_HOME_COUNTRY].equals("") ||  !csvLine[OutlookConstants.O_HOME_PSC].equals("") || 
				 !csvLine[OutlookConstants.O_HOME_REGION].equals("") ||  !csvLine[OutlookConstants.O_HOME_STREET].equals("") );
	}
	
	/**
	 * Checks postal work address items of outlook contact.
	 * @return true/false if there is at least one work address item. 
	 */
	public boolean checkPostalWorkOutlook() {
		return (!csvLine[OutlookConstants.O_WORK_CITY].equals("") || !csvLine[OutlookConstants.O_WORK_COUNTRY].equals("") ||  !csvLine[OutlookConstants.O_WORK_PSC].equals("") || 
				 !csvLine[OutlookConstants.O_WORK_REGION].equals("") ||  !csvLine[OutlookConstants.O_WORK_STREET].equals("") );
	}
	
	/**
	 * Checks postal other address items of outlook contact.
	 * @return true/false if there is at least one other address item. 
	 */
	public boolean checkPostalOtherOutlook() {
		return (!csvLine[OutlookConstants.O_OTHER_CITY].equals("") || !csvLine[OutlookConstants.O_OTHER_COUNTRY].equals("") ||  !csvLine[OutlookConstants.O_OTHER_PSC].equals("") || 
			 !csvLine[OutlookConstants.O_OTHER_REGION].equals("") ||  !csvLine[OutlookConstants.O_OTHER_STREET].equals("") );
	}
	
	/**
	 * Checks structure name items of outlook contact.
	 * @return true/false if there is at least one name item. 
	 */
	public boolean checkStructureNameOutlook() {
		return (!csvLine[OutlookConstants.O_GIVEN_NAME].equals("") || !csvLine[OutlookConstants.O_MIDDLE_NAME].equals("") ||  !csvLine[OutlookConstants.O_FAMILY_NAME].equals("") ||  !csvLine[OutlookConstants.O_TITLE].equals("") ||  !csvLine[OutlookConstants.O_SUFFIX].equals(""));
	}
	
	/**
	 * Checks organization items of outlook contact.
	 * @return true/false if there is at least one organization item. 
	 */
	public boolean checkOrganizationOutlook() {
		return (!csvLine[OutlookConstants.O_DEPARTMENT].equals("") || !csvLine[OutlookConstants.O_COMPANY].equals("") ||  !csvLine[OutlookConstants.O_JOB_TITLE].equals(""));
	}
	
	/**
	 * Checks email address items of outlook contact.
	 * @return true/false if there is at least one email address item. 
	 */
	public boolean checkEmailOutlook() {
		return (!csvLine[OutlookConstants.O_EMAIL1].equals("") || !csvLine[OutlookConstants.O_EMAIL2].equals("") || !csvLine[OutlookConstants.O_EMAIL3].equals(""));
	}
	
	/**
	 * Checks phones items of outlook contact.
	 * @return true/false if there is at least one phone item. 
	 */
	public boolean checkPhoneOutlook() {
		return (!csvLine[OutlookConstants.O_HOME_FAX].equals("") || !csvLine[OutlookConstants.O_HOME_PHONE].equals("") 
			|| !csvLine[OutlookConstants.O_HOME_PHONE2].equals("") || !csvLine[OutlookConstants.O_WORK_FAX].equals("") 
			|| !csvLine[OutlookConstants.O_WORK_PHONE].equals("") || !csvLine[OutlookConstants.O_WORK_PHONE2].equals("")
			|| !csvLine[OutlookConstants.O_MOBILE].equals("") || !csvLine[OutlookConstants.O_OTHER_PHONE].equals("") 
			|| !csvLine[OutlookConstants.O_PAGER].equals(""));
	}
	
	/**
	 * Method searches for phone number in thunderbird phone items.
	 * @param phoneNumber - string of phone number.
	 * @param matchedType - matchType.
	 * @return true/false if there is phone number.
	 */
	public boolean findPhoneThunderbird(String phoneNumber, MatchType matchedType) {
		if(checkPhoneThunderbird()) {
			return ( equal(ThunderbirdConstants.T_HOME_PHONE, phoneNumber, matchedType) 
					|| equal(ThunderbirdConstants.T_WORK_PHONE, phoneNumber, matchedType) 
					|| equal(ThunderbirdConstants.T_FAX, phoneNumber, matchedType) 
					|| equal(ThunderbirdConstants.T_PAGER, phoneNumber, matchedType) 
					|| equal(ThunderbirdConstants.T_MOBILE, phoneNumber, matchedType));
		}
		else 
			return false;
	}
	
	/**
	 * Method searches for phone number in Outlook phone items.
	 * @param phoneNumber - string of phone number.
	 * @param matchedType - matchType.
	 * @return true/false if there is phone number.
	 */
	public boolean findPhoneOutlook(String phoneNumber, MatchType matchedType) {
		if(checkPhoneOutlook()) {
			return (equal(OutlookConstants.O_HOME_FAX, phoneNumber, matchedType)  || equal(OutlookConstants.O_HOME_PHONE, phoneNumber, matchedType) 
					|| equal(OutlookConstants.O_HOME_PHONE2, phoneNumber, matchedType) || equal(OutlookConstants.O_WORK_FAX, phoneNumber, matchedType) 
					|| equal(OutlookConstants.O_WORK_PHONE, phoneNumber, matchedType) || equal(OutlookConstants.O_WORK_PHONE2, phoneNumber, matchedType)
					|| equal(OutlookConstants.O_MOBILE, phoneNumber, matchedType) || equal(OutlookConstants.O_OTHER_PHONE, phoneNumber, matchedType) 
					|| equal(OutlookConstants.O_PAGER, phoneNumber, matchedType));
		}
		else 
			return false;
	}
	
	/**
	 * Checks phones items of thunderbird contact.
	 * @return true/false if there is at least one phone item. 
	 */
	public boolean checkPhoneThunderbird() {
		return (!csvLine[ThunderbirdConstants.T_HOME_PHONE].equals("") || !csvLine[ThunderbirdConstants.T_WORK_PHONE].equals("") || !csvLine[ThunderbirdConstants.T_FAX].equals("") || !csvLine[ThunderbirdConstants.T_PAGER].equals("") || !csvLine[ThunderbirdConstants.T_MOBILE].equals("") );
	}
	
	/**
	 * Checks web site item of outlook contact.
	 * @return true/false if there is web page item. 
	 */ 
	public boolean checkWebsiteOutlook() {
		return (!csvLine[OutlookConstants.O_WEB_PAGE].equals(""));
	}
	
	public boolean isNull(int index) {
		return (csvLine[index] == "");
	}
	
	public String getString(int index) {
		if(csvLine.length > index)
			return (csvLine[index] == null) ? "" : csvLine[index];
		else 
			return null;
	}
	
	public void setString(int index, String string) {
		if(index < csvLine.length)
			csvLine[index]= string;
	}
	
	public String[] getArrayOfStrings() {
		return csvLine;
	}
	
	/**
	 * Method finds email in email items of csv contact.
	 * @param email searching email.
	 * @param matchedType type of match.
	 * @return true/false if it contains this email.
	 */
	public boolean findEmailOutlook(String email, MatchType matchedType) {
		if(!csvLine[OutlookConstants.O_EMAIL1].equals(""))
			if(equal(OutlookConstants.O_EMAIL1, email, matchedType))
				return true;
		if(!csvLine[OutlookConstants.O_EMAIL2].equals("")) 
			if(equal(OutlookConstants.O_EMAIL2, email, matchedType))
				return true;
		if(!csvLine[OutlookConstants.O_EMAIL3].equals("")) 
			if(equal(OutlookConstants.O_EMAIL3, email, matchedType))
				return true;
		return false;
	}
	
	/**
	 * Method finds email in email items of csv contact.
	 * @param email searching email.
	 * @param matchedType type of match.
	 * @return true/false if it contains this email.
	 */
	public boolean findEmailUserOutlook(String email, MatchType matchedType) {
		if(!csvLine[OutlookConstants.O_USER1].equals("")) {
			if(equal(OutlookConstants.O_USER1, email, matchedType))
				return true;
		}
		if(!csvLine[OutlookConstants.O_USER2].equals("")) { 
			if(equal(OutlookConstants.O_USER2, email, matchedType))
				return true;
		}
		if(!csvLine[OutlookConstants.O_USER3].equals("")) { 
			if(equal(OutlookConstants.O_USER3, email, matchedType))
				return true;
		}
		return false;
	}
	
	/**
	 * Method finds email in email items of csv contact.
	 * @param email searching email.
	 * @param matchedType type of match.
	 * @return true/false if it contains this email.
	 */
	public boolean findEmailCustomThunderbird(String email, MatchType matchedType) {
		if(!csvLine[ThunderbirdConstants.T_OTHER].equals(""))
			if(equal(ThunderbirdConstants.T_OTHER, email, matchedType))
				return true;
		if(!csvLine[ThunderbirdConstants.T_OTHER2].equals(""))	
			if(equal(ThunderbirdConstants.T_OTHER2, email, matchedType))
				return true;
		return false;
	}
	
	/**
	 * Method finds email in email items of csv contact.
	 * @param email searching email.
	 * @param matchedType type of match.
	 * @return true/false if it contains this email.
	 */
	public boolean findEmailThunderbird(String email, MatchType type) {
		if(!csvLine[ThunderbirdConstants.T_EMAIL1].equals(""))
			if( equal(ThunderbirdConstants.T_EMAIL1, email, type) )
				return true;
		if(!csvLine[ThunderbirdConstants.T_EMAIL2].equals(""))	
			if(equal(ThunderbirdConstants.T_EMAIL2, email, type))
				return true;
		return false;
	}
	
	/**
	 * Compares two string based ond type.
	 * @param position - position of first string.
	 * @param s2 - string of false.
	 * @param type - type of comparing.
	 * @return true/false if they are equal.
	 */
	public boolean equal(int position, String s2, MatchType type) {
		switch (type) {
			case CASE_SENSITIVE:
				return getString(position).equals(s2);
			case IGNORE_CASE:
				return getString(position).equalsIgnoreCase(s2);
			case IGNORE_ACCENTS_CASE_SENSITIVE:
				return StringUtils.convertNonAscii(getString(position)).equals(StringUtils.convertNonAscii(s2));
			case IGNORE_ACCENTS_AND_CASES:
				return StringUtils.convertNonAscii(getString(position)).equalsIgnoreCase(StringUtils.convertNonAscii(s2));
		}
		return false;
	}
	
	public static String removeAccents(String string) {
		return StringUtils.toUpperCaseSansAccent(string);
	}
}
