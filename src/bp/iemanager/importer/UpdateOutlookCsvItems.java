package bp.iemanager.importer;

import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.OutlookConstants;

/**
 * Class representing Outlook CSV items from file, which should be added to Android contact. 
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class UpdateOutlookCsvItems {
	/**
	 *  Konstanta pre rozoznanie duplicitnych kontaktov
	 */
	private final String NOTE_UNIQUE_NAME = "#NOTE@DUPLICATE@CONTACT#";
	
	private boolean newOrganization;
	private boolean newWorkAddress;
	private boolean newHomeAddress;
	private boolean newOtherAddress;
	private boolean newWorkFax;
	private boolean newWorkPhone;
	private boolean newWorkPhone2;
	private boolean newHomeFax;
	private boolean newHomePhone;
	private boolean newHomePhone2;
	private boolean newMobile;
	private boolean newOtherPhone;
	private boolean newPager;
	private boolean newEmail1;
	private boolean newEmail2;
	private boolean newEmail3;
	//private boolean newNickName;
	private boolean newNote;
	private boolean newWebPage;
	
	public UpdateOutlookCsvItems(CsvContact csvLine) {
		setNewOrganization(false);
		setNewWorkAddress(false);
		setNewHomeAddress(false);
		setNewOtherAddress(false);
		setNewWorkFax(false);
		setNewWorkPhone(false);
		setNewWorkPhone2(false);
		setNewHomeFax(false);
		setNewHomePhone(false);
		setNewHomePhone2(false);
		setNewMobile(false);
		setNewOtherPhone(false);
		setNewPager(false);
		setNewEmail1(false);
		setNewEmail2(false);
		setNewEmail3(false);
		//setNewNickName(false);
		setNewNote(false);
		setNewWebPage(false);
		
		if(csvLine.checkOrganizationOutlook())
			setNewOrganization(true);
		if(csvLine.checkPostalHomeOutlook()) 
			setNewHomeAddress(true);
		if(csvLine.checkPostalWorkOutlook())
			setNewWorkAddress(true);
		if(csvLine.checkPostalOtherOutlook())
			setNewWorkAddress(true);
		if(!csvLine.getString(OutlookConstants.O_WORK_FAX).equals(""))
			setNewWorkFax(true);
		if(!csvLine.getString(OutlookConstants.O_WORK_PHONE).equals(""))
			setNewWorkPhone(true);
		if(!csvLine.getString(OutlookConstants.O_WORK_PHONE2).equals(""))
			setNewWorkPhone2(true);
		if(!csvLine.getString(OutlookConstants.O_HOME_FAX).equals(""))
			setNewHomeFax(true);
		if(!csvLine.getString(OutlookConstants.O_HOME_PHONE).equals(""))
			setNewHomePhone(true);
		if(!csvLine.getString(OutlookConstants.O_HOME_PHONE2).equals(""))
			setNewHomePhone2(true);
		if(!csvLine.getString(OutlookConstants.O_MOBILE).equals(""))
			setNewMobile(true);
		if(!csvLine.getString(OutlookConstants.O_OTHER_PHONE).equals(""))
			setNewOtherPhone(true);
		if(!csvLine.getString(OutlookConstants.O_PAGER).equals(""))
			setNewPager(true);
		if(!csvLine.getString(OutlookConstants.O_EMAIL1).equals(""))
			setNewEmail1(true);
		if(!csvLine.getString(OutlookConstants.O_EMAIL2).equals(""))
			setNewEmail2(true);
		if(!csvLine.getString(OutlookConstants.O_EMAIL3).equals(""))
			setNewEmail3(true);
		// TODO nickName u Outlook
		//if(csvLine.getString(OutlookContants.O_) != "")
		//	setNewNickName(true);
		if(!csvLine.getString(OutlookConstants.O_NOTE).equals("") && !csvLine.getString(OutlookConstants.O_NOTE).equals(NOTE_UNIQUE_NAME))
			setNewNote(true);
		if(csvLine.getLength() > OutlookConstants.O_WEB_PAGE)
			if(!csvLine.getString(OutlookConstants.O_WEB_PAGE).equals(""))
				setNewWebPage(true);
	}

	public boolean isNewOrganization() {
		return newOrganization;
	}

	public void setNewOrganization(boolean newOrganization) {
		this.newOrganization = newOrganization;
	}

	public boolean isNewWorkAddress() {
		return newWorkAddress;
	}

	public void setNewWorkAddress(boolean newWorkAddress) {
		this.newWorkAddress = newWorkAddress;
	}

	protected boolean isNewHomeAddress() {
		return newHomeAddress;
	}

	protected void setNewHomeAddress(boolean newHomeAddress) {
		this.newHomeAddress = newHomeAddress;
	}

	public boolean isNewOtherAddress() {
		return newOtherAddress;
	}

	public void setNewOtherAddress(boolean newOtherAddress) {
		this.newOtherAddress = newOtherAddress;
	}

	protected boolean isNewWorkFax() {
		return newWorkFax;
	}

	protected void setNewWorkFax(boolean newWorkFax) {
		this.newWorkFax = newWorkFax;
	}

	protected boolean isNewWorkPhone() {
		return newWorkPhone;
	}

	protected void setNewWorkPhone(boolean newWorkPhone) {
		this.newWorkPhone = newWorkPhone;
	}

	public boolean isNewWorkPhone2() {
		return newWorkPhone2;
	}

	public void setNewWorkPhone2(boolean newWorkPhone2) {
		this.newWorkPhone2 = newWorkPhone2;
	}

	public boolean isNewHomeFax() {
		return newHomeFax;
	}

	public void setNewHomeFax(boolean newHomeFax) {
		this.newHomeFax = newHomeFax;
	}

	public boolean isNewHomePhone() {
		return newHomePhone;
	}

	public void setNewHomePhone(boolean newHomePhone) {
		this.newHomePhone = newHomePhone;
	}

	public boolean isNewHomePhone2() {
		return newHomePhone2;
	}

	public void setNewHomePhone2(boolean newHomePhone2) {
		this.newHomePhone2 = newHomePhone2;
	}

	public boolean isNewMobile() {
		return newMobile;
	}

	public void setNewMobile(boolean newMobile) {
		this.newMobile = newMobile;
	}

	public boolean isNewOtherPhone() {
		return newOtherPhone;
	}

	public void setNewOtherPhone(boolean newOtherPhone) {
		this.newOtherPhone = newOtherPhone;
	}

	public boolean isNewPager() {
		return newPager;
	}

	public void setNewPager(boolean newPager) {
		this.newPager = newPager;
	}

	public boolean isNewEmail1() {
		return newEmail1;
	}

	public void setNewEmail1(boolean newEmail1) {
		this.newEmail1 = newEmail1;
	}

	public boolean isNewEmail2() {
		return newEmail2;
	}

	public void setNewEmail2(boolean newEmail2) {
		this.newEmail2 = newEmail2;
	}

	public boolean isNewEmail3() {
		return newEmail3;
	}

	public void setNewEmail3(boolean newEmail3) {
		this.newEmail3 = newEmail3;
	}

	/*	public boolean isNewNickName() {
		return newNickName;
	}

	public void setNewNickName(boolean newNickName) {
		this.newNickName = newNickName;
	}
	*/

	public boolean isNewNote() {
		return newNote;
	}

	public void setNewNote(boolean newNote) {
		this.newNote = newNote;
	}

	public boolean isNewWebPage() {
		return newWebPage;
	}

	public void setNewWebPage(boolean newWebPage) {
		this.newWebPage = newWebPage;
	}
		
		
}
