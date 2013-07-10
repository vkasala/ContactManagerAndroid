package bp.iemanager.importer;

import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.ThunderbirdConstants;

/**
 * Class representing Thunderbird CSV items from file, which should be added to Android contact. 
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class UpdateThunderbirdCsvItems {
	protected boolean newNickName;
	protected boolean newEmail1;
	protected boolean newEmail2;
	
	protected boolean newWorkPhone; 
	protected boolean newHomePhone;
	protected boolean newFax;
	protected boolean newPager;
	protected boolean newMobile;
	protected boolean newHomeAddress;
	protected boolean newWorkAddress;
	protected boolean newOrganization;
	private boolean newWebPage1;
	private boolean newWebPage2;
	private boolean newNote;
	
	public UpdateThunderbirdCsvItems(CsvContact csvLine) {
		newEmail1 = false;
		newEmail2 = false;
		newWorkAddress = false;
		newWorkPhone = false;
		newFax = false;
		newHomeAddress = false;
		newHomePhone = false;
		newPager = false;
		newMobile = false;
		newOrganization = false;
		newNickName = false;
		newWebPage1 = false;
		newWebPage2 = false;
		newNote = false;
		
		if(!csvLine.getString(ThunderbirdConstants.T_HOME_PHONE).equals(""))
			setNewHomePhone(true);
		if(!csvLine.getString(ThunderbirdConstants.T_WORK_PHONE).equals(""))
			setNewWorkPhone(true);
		if(!csvLine.getString(ThunderbirdConstants.T_FAX).equals(""))
			setNewFax(true);
		if(!csvLine.getString(ThunderbirdConstants.T_PAGER).equals(""))
			setNewPager(true);
		if(!csvLine.getString(ThunderbirdConstants.T_MOBILE).equals(""))
			setNewMobile(true);
		if(!csvLine.getString(ThunderbirdConstants.T_WEB_ADDRESS).equals(""))
			setNewWebPage1(true);
		if(!csvLine.getString(ThunderbirdConstants.T_WEB_ADDRESS2).equals(""))
			setNewWebPage2(true);
		if(!csvLine.getString(ThunderbirdConstants.T_EMAIL1).equals(""))
			setNewEmail1(true);
		if(!csvLine.getString(ThunderbirdConstants.T_EMAIL2).equals(""))
			setNewEmail2(true);
		if(csvLine.checkPostalHomeThunderbird()) 
			setNewHomeAddress(true);
		if(csvLine.checkPostalWorkThunderbird())
			setNewWorkAddress(true);
		if(!csvLine.getString(ThunderbirdConstants.T_NICKNAME).equals(""))
			setNewNickName(true);
		if(csvLine.checkOrganizationThunderbird())
			setNewOrganization(true);
		if(!csvLine.getString(ThunderbirdConstants.T_NOTE).equals(""))
			setNewNote(true);
	}
	
	public void setNewWorkPhone(boolean newWorkPhone) {
		this.newWorkPhone = newWorkPhone;
	}
	
	public void setNewPager(boolean newPager) {
		this.newPager = newPager;
	}
	
	public void setNewWorkAddress(boolean newWorkAddress) {
		this.newWorkAddress = newWorkAddress;
	}
	
	public void setNewOrganization(boolean newOrganization) {
		this.newOrganization = newOrganization;
	}
	
	public void setNewNickName(boolean newNickName) {
		this.newNickName = newNickName;
	}
	
	public void setNewMobile(boolean newMobile) {
		this.newMobile = newMobile;
	}
	
	public void setNewHomePhone(boolean newHomePhone) {
		this.newHomePhone = newHomePhone;
	}
	
	public void setNewHomeAddress(boolean newHomeAddress) {
		this.newHomeAddress = newHomeAddress;
	}
	
	public void setNewFax(boolean newFax) {
		this.newFax = newFax;
	}
	
	public void setNewEmail2(boolean newEmail2) {
		this.newEmail2 = newEmail2;
	}
	
	public void setNewEmail1(boolean newEmail1) {
		this.newEmail1 = newEmail1;
	}
	
	public boolean isNewWorkPhone() {
		return newWorkPhone;
	}
	
	public boolean isNewWorkAddress() {
		return newWorkAddress;
	}
	public boolean isNewPager() {
		return newPager;
	}
	
	public boolean isNewOrganization() {
		return newOrganization;
	}
	
	public boolean isNewNickName() {
		return newNickName;
	}
	
	public boolean isNewMobile() {
		return newMobile;
	}
	
	public boolean isNewHomePhone() {
		return newHomePhone;
	}
	
	public boolean isNewHomeAddress() {
		return newHomeAddress;
	}
	
	public boolean isNewFax() {
		return newFax;
	}

	public boolean isNewEmail2() {
		return newEmail2;
	}
	
	public boolean isNewEmail1() {
		return newEmail1;
	}

	public boolean isNewWebPage1() {
		return newWebPage1;
	}

	public void setNewWebPage1(boolean newWebPage1) {
		this.newWebPage1 = newWebPage1;
	}

	public boolean isNewWebPage2() {
		return newWebPage2;
	}

	public void setNewWebPage2(boolean newWebPage2) {
		this.newWebPage2 = newWebPage2;
	}

	public boolean isNewNote() {
		return newNote;
	}

	public void setNewNote(boolean newNote) {
		this.newNote = newNote;
	}
}
