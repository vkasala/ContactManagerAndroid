package bp.iemanager.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bp.iemanager.StringUtils;
import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.OutlookConstants;
import bp.iemanager.csvcontact.ThunderbirdConstants;


import sk.kasala.viliam.bakalarka.R;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class representing activity for manual synchronization of pair of contacts.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class SynchronizerActivity extends Activity {
	/**
	 * Name of class from which printing Originates
	 */
	private static final String TAG = "SynchronizerActivity";
	/**
	 * Flag for enable debug printing
	 */
	private static final boolean debug = true;
	/**
	 * ID for extracting list of possible match pairs from the Bundle
	 */
	private static final String POSSIBLE_LIST_ID = "bp.iemanager.importContacts/SERIALIZABLE_ARRAY_LIST";
	/**
	 * ID for extracting matchType from the Bundle
	 */
	private static final String MATCH_TYPE_STRING = "bp.iemanager.importContacts/MATCH$TYPE";
	
	/**
	 * ID for extracting OPTIONS from from the Bundle
	 */
	private static final String MATCH_ADDRESS_TYPE_STRING = "bp.iemanager.importContacts/ADDRESS%TYPE";
	
	/**
	 * ID for extracting Account TYPE from from the Bundle
	 */
	private static final String ACCOUNT_TYPE_STRING = "bp.iemanager.importContacts/ACCOUNT$NEW";
	
	/**
	 * Pattern for dividing more same items in textView and fill if some text view has more lines than other
	 */
	private static final String lineBreakPattern = "\t\t-\t\t-\t\t-\t\t";
	
	/**
	 * Unique ID for DIALOG
	 */
	private static final int DIALOG_REALLY_EXIT = 1;
	
	/**
	 * Pattern for matching two address fields
	 */
	private boolean[] MIN_MATCH_FIELDS = { false, false, true, false, true, true };
	
	/**
	 * Account type to which new Contact should be joined
	 */
	private Account pickedAccount;
	
	/** 
	 * List of possible pair contacts matches through which will be iterate 
	 */
	private List<PossibleEqualContacts> listOfPossibleEqualContacts;
	
	/**
	 * Match type for Sync of all fields
	 */
	private MatchType matchType;
	
	/** GUI References */
	private TextView phoneNameTextView;
	private TextView phoneOrganizationTextView;
	private TextView phonePhoneNumbersTextView;
	private TextView phoneEmailAddressTextView;
	private TextView phoneAddressTextView;
	private TextView phoneNotesTextView;
	private TextView phoneWebPagesTextView;
	private TextView phoneNicknameTextView;
	private TextView phoneImTextView;
	
	/** GUI REFERENCES on TEXTVIEWS */
	private TextView csvNameTextView;
	private TextView csvOrganizationTextView;
	private TextView csvPhoneNumbersTextView;
	private TextView csvEmailAddressTextView;
	private TextView csvAddressTextView;
	private TextView csvNotesTextView;
	private TextView csvWebPagesTextView;
	private TextView csvNicknameTextView;
	private TextView csvImTextView ;
	private Button synchronizeButton;
	private Button nextButton;
	private Button exitButton;
	private Button newContactButton;
	
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.synchronizer_layout);
		getExtrasFromBundle();
		//if(debug) Log.v(TAG, String.valueOf(listOfPossibleEqualContacts.size()));
		
		// Register GUI items
		synchronizeButton = (Button) findViewById(R.id.synchronize_button);
		nextButton = (Button) findViewById(R.id.synchronize_nextButton);
		exitButton = (Button) findViewById(R.id.exit_synchronization);
		newContactButton = (Button) findViewById(R.id.newContactButtonSynchronizer);
		
		phoneNameTextView = (TextView) findViewById(R.id.phone_name_textview);
		phoneOrganizationTextView = (TextView) findViewById(R.id.phone_organization_textview);
		phonePhoneNumbersTextView = (TextView) findViewById(R.id.phone_phone_textview);
		phoneEmailAddressTextView = (TextView) findViewById(R.id.phone_email_textview);
		phoneAddressTextView = (TextView) findViewById(R.id.phone_address_textview);
		phoneNotesTextView = (TextView) findViewById(R.id.phone_notes_textview);
		phoneWebPagesTextView = (TextView) findViewById(R.id.phone_webpage_textview);
		phoneNicknameTextView = (TextView) findViewById(R.id.phone_nickname_textview);
		phoneImTextView = (TextView) findViewById(R.id.phone_im_textview);
		
		csvNameTextView = (TextView) findViewById(R.id.csv_name_textview);
		csvOrganizationTextView = (TextView) findViewById(R.id.csv_organization_textview);
		csvPhoneNumbersTextView = (TextView) findViewById(R.id.csv_phone_textview);
		csvEmailAddressTextView = (TextView) findViewById(R.id.csv_email_textview);
		csvAddressTextView = (TextView) findViewById(R.id.csv_address_textview);
		csvNotesTextView = (TextView) findViewById(R.id.csv_notes_textview);
		csvWebPagesTextView = (TextView) findViewById(R.id.csv_webpage_textview);
		csvNicknameTextView = (TextView) findViewById(R.id.csv_nickname_textview);
		csvImTextView = (TextView) findViewById(R.id.csv_im_textview);
		index = 0;
		
		synchronizeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/**if(index-1 >= 0) {
					index--;
					populateTextViews();
				}*/
				synchronizeContact();
				listOfPossibleEqualContacts.remove(0);
				if(!listOfPossibleEqualContacts.isEmpty())	{
					populateTextViews();
				}else {
					exitDialog();
				}
			}
		});
		
		newContactButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/**if(index-1 >= 0) {
					index--;
					populateTextViews();
				}*/
				createNewContact();
				listOfPossibleEqualContacts.remove(0);
				if(!listOfPossibleEqualContacts.isEmpty())	{
					populateTextViews();
				}else {
					exitDialog();
				}
			}
		});
		
		nextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/*if(index + 1 <= listOfPossibleEqualItems.size() - 1 ) {
					index++;
					populateTextViews();
				}*/
				listOfPossibleEqualContacts.remove(0);
				if(!listOfPossibleEqualContacts.isEmpty())	{
					populateTextViews();
				}
				else {
					exitDialog();
				}
			}
		});
		
		exitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_REALLY_EXIT);
			}
		});
		populateTextViews();
	}
	
	@Override
	public void onBackPressed() {
		showDialog(DIALOG_REALLY_EXIT);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_REALLY_EXIT: 
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage("Are you sure you want to exit?")
		           .setCancelable(false)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    SynchronizerActivity.this.finish();
		               }
		           })
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		                    dialog.dismiss();
		               }
		           });
		    return builder.create();
		default:
			return null;
		}
	}
	
	/**
	 * Finish activity with result OK
	 */
	protected void exitDialog() {
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	/**
	 * Method inicialize all textView with appropriate data and show matches
	 */
	protected void populateTextViews() {
		phoneNameTextView.setText("");
		phoneOrganizationTextView.setText(""); 
		phonePhoneNumbersTextView.setText(""); 
		phoneEmailAddressTextView.setText(""); 
		phoneAddressTextView.setText(""); 
		phoneNotesTextView.setText(""); 
		phoneWebPagesTextView.setText(""); 
		phoneNicknameTextView.setText(""); 
		phoneImTextView.setText("");
		csvNameTextView.setText(""); 
		csvOrganizationTextView.setText(""); 
		csvPhoneNumbersTextView.setText(""); 
		csvEmailAddressTextView.setText(""); 
		csvAddressTextView.setText(""); 
		csvNotesTextView.setText(""); 
		csvWebPagesTextView.setText(""); 
		csvNicknameTextView.setText("");
		csvImTextView.setText("");
		
		CsvContact fileContact = listOfPossibleEqualContacts.get(0).fileContact;
		
		fileContact.callTrimOn();
		
		ArrayList<CsvContact> phoneContact =  listOfPossibleEqualContacts.get(index).
				getListOfCsvLinesFromRawContactID(this);
		
		for (CsvContact csvLine : phoneContact) {
			csvLine.emptyStringsIfNull();
			csvLine.callTrimOn();
		}
		if(fileContact.getType() == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
			populateWithThunderbirdName(fileContact, phoneContact);
			populateOrganization(fileContact, phoneContact, csvOrganizationTextView, phoneOrganizationTextView, ThunderbirdConstants.T_COMPANY, ThunderbirdConstants.T_JOB_TITLE, ThunderbirdConstants.THUDERBIRD_ITEM_COUNT);
			populateWithIndex(fileContact, phoneContact, csvNotesTextView, phoneNotesTextView, ThunderbirdConstants.T_NOTE);
			populateWithThunderbirdWebPages(fileContact, phoneContact);
			populateWithThunderbirdEmails(fileContact, phoneContact);
			
			populateWithIndex(fileContact, phoneContact, csvNicknameTextView, phoneNicknameTextView, ThunderbirdConstants.T_NICKNAME);
			populateWithThunderbirdPhone(fileContact, phoneContact);
			populateThunderbirdAddress(fileContact, phoneContact, csvAddressTextView, phoneAddressTextView);
		}
		else if(fileContact.getType() == OutlookConstants.OUTLOOK_ITEM_COUNT) {
			populateWithOutlookName(fileContact, phoneContact);
			populateOrganization(fileContact, phoneContact, csvOrganizationTextView, phoneOrganizationTextView, OutlookConstants.O_COMPANY, OutlookConstants.O_JOB_TITLE, OutlookConstants.OUTLOOK_ITEM_COUNT);
			populateWithIndex(fileContact, phoneContact, csvNotesTextView, phoneNotesTextView, OutlookConstants.O_NOTE);
			populateWithIndex(fileContact, phoneContact, csvWebPagesTextView, phoneWebPagesTextView, OutlookConstants.O_WEB_PAGE);
			populateWithOutlookEmails(fileContact, phoneContact);
			populateWithOutlookPhone(fileContact, phoneContact);
			populateOutlookAddress(fileContact, phoneContact, csvAddressTextView, phoneAddressTextView);	
		}
	}
	
	/**
	 * Method synchronizing contacts from CSV file.
	 */
	protected void synchronizeContact() {
		PossibleEqualContacts pos = listOfPossibleEqualContacts.get(0);
		if(pos.fileContact.getType() == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
			(new ThunderbirdImporter(this.getBaseContext(), pos.getFromContact(), matchType, MIN_MATCH_FIELDS)).updateRawContact(pos.getRawContactID());
		}
		else if(pos.fileContact.getType() == OutlookConstants.OUTLOOK_ITEM_COUNT) {
			(new OutlookImporter(this.getBaseContext(), pos.getFromContact(), matchType, MIN_MATCH_FIELDS)).updateRawContact(pos.getRawContactID());
		}
		return;
	}
	
	/**
	 * Method creates new contact from CSV file contact.
	 */
	protected void createNewContact() {
		PossibleEqualContacts pos = listOfPossibleEqualContacts.get(0);
		if(pos.fileContact.getType() == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
			(new ThunderbirdImporter(this.getBaseContext(), pos.getFromContact(), matchType, MIN_MATCH_FIELDS)).createNewContact(pos.fileContact, pickedAccount.name, pickedAccount.type);
		}
		else if(pos.fileContact.getType() == OutlookConstants.OUTLOOK_ITEM_COUNT) {
			(new OutlookImporter(this.getBaseContext(), pos.getFromContact(), matchType, MIN_MATCH_FIELDS)).createNewContact(pos.fileContact, pickedAccount.name, pickedAccount.type);
		}
		return;
	}
	
	/**
	 * Method fills Name TextView 
	 * @param fileContact
	 * @param phoneContact
	 */
	protected void populateWithThunderbirdName(CsvContact fileContact, ArrayList<CsvContact> phoneContact) {
		String phoneNameFullText = phoneContact.get(0).getString(ThunderbirdConstants.T_DISPLAY_NAME).trim().replaceAll("\\s+", " ");
		
		String fileNameFullText = (fileContact.getString(ThunderbirdConstants.T_GIVEN_NAME) + 
									" " + fileContact.getString(ThunderbirdConstants.T_FAMILY_NAME)).trim().replaceAll("\\s+", " ");
		
		phoneNameTextView.setText(phoneNameFullText, TextView.BufferType.SPANNABLE);
		csvNameTextView.setText(fileNameFullText, 
			TextView.BufferType.SPANNABLE);
		
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_GIVEN_NAME, "")) {
			setColor(phoneNameTextView, phoneNameFullText, fileContact.getString(ThunderbirdConstants.T_GIVEN_NAME).trim(), Color.RED);
		}
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_FAMILY_NAME, "")) {
			setColor(phoneNameTextView, phoneNameFullText, fileContact.getString(ThunderbirdConstants.T_FAMILY_NAME).trim(), Color.RED);
		}
		if(!phoneContact.get(0).equalTwoStrings(ThunderbirdConstants.T_FAMILY_NAME, "")) {
			setColor(csvNameTextView, fileNameFullText, phoneContact.get(0).getString(ThunderbirdConstants.T_FAMILY_NAME).trim(), Color.RED);
		}
		if(!phoneContact.get(0).equalTwoStrings(ThunderbirdConstants.T_GIVEN_NAME, "")) {
			setColor(csvNameTextView, fileNameFullText, phoneContact.get(0).getString(ThunderbirdConstants.T_GIVEN_NAME).trim(), Color.RED);
		}
	}
	
	/**
	 * Method fills Name TextView 
	 * @param fileContact
	 * @param phoneContact
	 */
	protected void populateWithOutlookName(CsvContact fileContact, ArrayList<CsvContact> phoneContact) {
		CsvContact csvLineContacts = phoneContact.get(0);
		String phoneNameFullText = (csvLineContacts.getString(OutlookConstants.O_GIVEN_NAME) + " " 
				+ ( (csvLineContacts.getString(OutlookConstants.O_MIDDLE_NAME).equals("")) ? csvLineContacts.getString(OutlookConstants.O_MIDDLE_NAME) + " " : "") 
				+ csvLineContacts.getString(OutlookConstants.O_FAMILY_NAME) 
				+ ( (csvLineContacts.getString(OutlookConstants.O_SUFFIX).equals(" ")) ? ", " + csvLineContacts.getString(OutlookConstants.O_SUFFIX) : "" )
				).trim();
		
		String fileNameFullText = (fileContact.getString(OutlookConstants.O_GIVEN_NAME) + " " 
				+ ( (fileContact.getString(OutlookConstants.O_MIDDLE_NAME).equals(" ")) ? fileContact.getString(OutlookConstants.O_MIDDLE_NAME) + " " : "") 
				+ fileContact.getString(OutlookConstants.O_FAMILY_NAME) 
				+ ( (fileContact.getString(OutlookConstants.O_SUFFIX).equals(" ")) ? ", " + fileContact.getString(OutlookConstants.O_SUFFIX) : "" )
				).trim();
		
		phoneNameTextView.setText(phoneNameFullText, TextView.BufferType.SPANNABLE);
		csvNameTextView.setText(fileNameFullText, 
			TextView.BufferType.SPANNABLE);
		
		String help = fileContact.getString(OutlookConstants.O_GIVEN_NAME);
		if(!help.equals("")) {
			setColors(phoneNameFullText, fileNameFullText, help);
		}
		help = fileContact.getString(OutlookConstants.O_MIDDLE_NAME);
		if(!help.equals("")) {
			setColors(phoneNameFullText, fileNameFullText, help);
		}
		help = fileContact.getString(OutlookConstants.O_FAMILY_NAME);
		if(!help.equals("")) {
			setColors(phoneNameFullText, fileNameFullText, help);
		}
		help = fileContact.getString(OutlookConstants.O_SUFFIX);
		if(!help.equals("")) {
			setColors(phoneNameFullText, fileNameFullText, help);
		}		
	}
	
	/**
	 * Method fills textViews with strings and also changes color of same strings.
	 * @param fileContact list of CSV Lines
	 * @param phoneContact CSV line contact
	 * @param csvFile - textView for CSV Contact
	 * @param phone - textView for Phone Contact
	 * @param index - index from CSV Line
	 */
	protected void populateWithIndex(CsvContact fileContact, ArrayList<CsvContact> phoneContact, TextView csvFile, TextView phone, int index) {
		String phoneFullText = "";
		String fileFullText = fileContact.getString(index).trim(); 
		int i = 0;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();		
			phoneFullText += (csvLine.getString(index).trim()); 	
			if(iterator.hasNext()) {
				if(!phoneContact.get(++i).equalTwoStrings(index, "")) {
					phoneFullText += ("\n"+ lineBreakPattern +"\n");
				}
			}
		} 
		
		String[] pole = addNewLines(fileFullText.replaceAll("\\n+", "\n").trim(), phoneFullText.replaceAll("\\n+", "\n").trim());
		fileFullText = pole[0];
		phoneFullText = pole[1];
		
		phone.setText(phoneFullText, TextView.BufferType.SPANNABLE);
		csvFile.setText(fileFullText, TextView.BufferType.SPANNABLE);
		
		// Find all matches in string
		if(!fileContact.equalTwoStrings(index, ""))
			for (int j = -1; (j = getIndex(phoneFullText, fileContact.getString(index), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvFile, new CopyIndex(0, fileContact.getString(index).trim().length()) , Color.RED);
				setColor(phone, new CopyIndex(j, j+fileContact.getString(index).length()), Color.RED);
			} 
	}
	
	/**
	 * Method fills textViews with WebAddresses and changes color of the same
	 * @param fileContact
	 * @param phoneContact
	 */
	protected void populateWithThunderbirdWebPages(CsvContact fileContact, ArrayList<CsvContact> phoneContact) {
		String phoneWebPageFullText = "";
		
		String fileWebPageFullText = (fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS) + "\n"+ fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS2)).trim(); 
									
		int i = 0;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			phoneWebPageFullText += (csvLine.getString(ThunderbirdConstants.T_WEB_ADDRESS) + "\n" + (csvLine.getString(ThunderbirdConstants.T_WEB_ADDRESS2))).trim(); 
			if(iterator.hasNext()) {
				if(phoneContact.get(++i).checkWebsiteThunderbird()) {
					phoneWebPageFullText += ("\n"+ lineBreakPattern +"\n");
				}
			}
		} 
		String[] pole = addNewLines(fileWebPageFullText, phoneWebPageFullText);
		fileWebPageFullText	= pole[0];
		phoneWebPageFullText = pole[1];
		
		phoneWebPagesTextView.setText(phoneWebPageFullText, TextView.BufferType.SPANNABLE);
		csvWebPagesTextView.setText(fileWebPageFullText, 
			TextView.BufferType.SPANNABLE);
		
		// If there were found some equal  fields
		// Find all matches in string
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_WEB_ADDRESS, ""))
			for (int j = -1; (j = getIndex(phoneWebPageFullText,fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvWebPagesTextView, fileWebPageFullText, fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS) , Color.RED);
				setColor(phoneWebPagesTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_WEB_ADDRESS2, ""))
			for (int j = -1; (j = getIndex(phoneWebPageFullText, fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS2), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvWebPagesTextView, fileWebPageFullText, fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS2) , Color.RED);
				setColor(phoneWebPagesTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_WEB_ADDRESS2).length()), Color.RED);
			}
	}
	
	/**
	 * Method fills textViews with Emails and changes color of the same
	 * @param fileContact
	 * @param phoneContact
	 */
	protected void populateWithThunderbirdEmails(CsvContact fileContact, ArrayList<CsvContact> phoneContact) {
		String phoneEmailsFullText = "";
		
		String fileEmailsFullText = (fileContact.getString(ThunderbirdConstants.T_EMAIL1) + "\n"+ fileContact.getString(ThunderbirdConstants.T_EMAIL2)).trim(); 
									
		int i = 0;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			phoneEmailsFullText += (csvLine.getString(ThunderbirdConstants.T_EMAIL1) + "\n" + (csvLine.getString(ThunderbirdConstants.T_EMAIL2))).trim(); 
			if(iterator.hasNext()) {
				if(phoneContact.get(++i).checkEmailThunderbird()) {
					phoneEmailsFullText += ("\n"+ lineBreakPattern +"\n");
				}
			}
		} 
		String[] pole = addNewLines(fileEmailsFullText.replaceAll("\\n+", "\n").trim(), phoneEmailsFullText.replaceAll("\\n+", "\n").trim());
		fileEmailsFullText	= pole[0];
		phoneEmailsFullText = pole[1];
		
		phoneEmailAddressTextView.setText(phoneEmailsFullText, TextView.BufferType.SPANNABLE);
		csvEmailAddressTextView.setText(fileEmailsFullText, 
			TextView.BufferType.SPANNABLE);
		
		// If there were found some equal  fields
		// Find all matches in string
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_EMAIL1, ""))
			for (int j = -1; (j = getIndex(phoneEmailsFullText, fileContact.getString(ThunderbirdConstants.T_EMAIL1), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvEmailAddressTextView, fileEmailsFullText, fileContact.getString(ThunderbirdConstants.T_EMAIL1) , Color.RED);
				setColor(phoneEmailAddressTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_EMAIL1).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_EMAIL2, ""))
			for (int j = -1; (j = getIndex(phoneEmailsFullText, fileContact.getString(ThunderbirdConstants.T_EMAIL2), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvEmailAddressTextView, fileEmailsFullText, fileContact.getString(ThunderbirdConstants.T_EMAIL2) , Color.RED);
				setColor(phoneEmailAddressTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_EMAIL2).length()), Color.RED);
			}
	}
	
	/**
	 * Method fills textViews with WebAddresses and changes color of the same
	 * @param fileContact
	 * @param phoneContact
	 */
	protected void populateWithOutlookEmails(CsvContact fileContact, ArrayList<CsvContact> phoneContact) {
		String phoneEmailsFullText = "";
		String fileEmailsFullText = (fileContact.getString(OutlookConstants.O_EMAIL1) + "\n"+ fileContact.getString(OutlookConstants.O_EMAIL2) + "\n"+ fileContact.getString(OutlookConstants.O_EMAIL3)).trim(); 						
		int i = 0;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			phoneEmailsFullText += (csvLine.getString(OutlookConstants.O_EMAIL1) + "\n" + (csvLine.getString(OutlookConstants.O_EMAIL2)) + "\n" + (csvLine.getString(OutlookConstants.O_EMAIL3))).trim(); 
			if(iterator.hasNext()) {
				if(phoneContact.get(++i).checkEmailOutlook()) {
					phoneEmailsFullText += ("\n"+ lineBreakPattern +"\n");
				}
			}
		} 
		String[] pole = addNewLines(fileEmailsFullText.replaceAll("\\n+", "\n"), phoneEmailsFullText.replaceAll("\\n+", "\n"));
		fileEmailsFullText	= pole[0];
		phoneEmailsFullText = pole[1];
		
		phoneEmailAddressTextView.setText(phoneEmailsFullText, TextView.BufferType.SPANNABLE);
		csvEmailAddressTextView.setText(fileEmailsFullText, 
			TextView.BufferType.SPANNABLE);
		
		// If there were found some equal  fields
		// Find all matches in string
		if(!fileContact.equalTwoStrings(OutlookConstants.O_EMAIL1, ""))
			for (int j = -1; (j = getIndex(phoneEmailsFullText, fileContact.getString(OutlookConstants.O_EMAIL1), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvEmailAddressTextView, fileEmailsFullText, fileContact.getString(OutlookConstants.O_EMAIL1) , Color.RED);
				setColor(phoneEmailAddressTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_EMAIL1).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_EMAIL2, ""))
			for (int j = -1; (j = getIndex(phoneEmailsFullText, fileContact.getString(OutlookConstants.O_EMAIL2), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvEmailAddressTextView, fileEmailsFullText, fileContact.getString(OutlookConstants.O_EMAIL2) , Color.RED);
				setColor(phoneEmailAddressTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_EMAIL2).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_EMAIL3, ""))
			for (int j = -1; (j = getIndex(phoneEmailsFullText, fileContact.getString(OutlookConstants.O_EMAIL3), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvEmailAddressTextView, fileEmailsFullText, fileContact.getString(OutlookConstants.O_EMAIL3) , Color.RED);
				setColor(phoneEmailAddressTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_EMAIL3).length()), Color.RED);
			}
	}
	
	/**
	 * Method fills TextViews with phones and change colors of the same
	 * @param fileContact
	 * @param phoneContact
	 */
	protected void populateWithThunderbirdPhone(CsvContact fileContact, ArrayList<CsvContact> phoneContact) {
		String phonePhoneFullText = "";
		
		String filePhoneFullText = "";
		if(fileContact.checkPhoneThunderbird()) {
			filePhoneFullText = (fileContact.getString(ThunderbirdConstants.T_WORK_PHONE) + 
					"\n" + fileContact.getString(ThunderbirdConstants.T_HOME_PHONE) + "\n" + fileContact.getString(ThunderbirdConstants.T_FAX) + 
					"\n" + fileContact.getString(ThunderbirdConstants.T_PAGER) + "\n" + fileContact.getString(ThunderbirdConstants.T_MOBILE)).trim();
			
			filePhoneFullText= filePhoneFullText.replaceAll("\\n+", "\n");				
		}						
		int i = 0;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			if(csvLine.checkPhoneThunderbird()) {
				phonePhoneFullText += (csvLine.getString(ThunderbirdConstants.T_WORK_PHONE) + 
						"\n" + csvLine.getString(ThunderbirdConstants.T_HOME_PHONE) + "\n" +csvLine.getString(ThunderbirdConstants.T_FAX) + 
						"\n" + csvLine.getString(ThunderbirdConstants.T_PAGER) + "\n" + csvLine.getString(ThunderbirdConstants.T_MOBILE)).trim(); 
				phonePhoneFullText = phonePhoneFullText.replaceAll("\\n+", "\n");
			}
			if(iterator.hasNext()) {
				if(phoneContact.get(++i).checkPhoneThunderbird()) {
					phonePhoneFullText += ("\n"+ lineBreakPattern +"\n");
				}
			}
		} 
		String[] pole = addNewLines(filePhoneFullText, phonePhoneFullText);
		filePhoneFullText = pole[0];
		phonePhoneFullText = pole[1];
		
		phonePhoneNumbersTextView.setText(phonePhoneFullText, TextView.BufferType.SPANNABLE);
		csvPhoneNumbersTextView.setText(filePhoneFullText, 
			TextView.BufferType.SPANNABLE);
		
		// If there were found some equal  fields
		// Find all matches in string
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_WORK_PHONE, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(ThunderbirdConstants.T_WORK_PHONE), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(ThunderbirdConstants.T_WORK_PHONE) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_WORK_PHONE).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_HOME_PHONE, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(ThunderbirdConstants.T_HOME_PHONE), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(ThunderbirdConstants.T_HOME_PHONE) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_HOME_PHONE).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_FAX, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(ThunderbirdConstants.T_FAX), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(ThunderbirdConstants.T_FAX) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_FAX).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_PAGER, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(ThunderbirdConstants.T_PAGER), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(ThunderbirdConstants.T_PAGER) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_PAGER).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(ThunderbirdConstants.T_MOBILE, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(ThunderbirdConstants.T_MOBILE), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(ThunderbirdConstants.T_MOBILE) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(ThunderbirdConstants.T_MOBILE).length()), Color.RED);
			}
	}
	
	/**
	 * Method fills TextViews with phones and change colors of the same
	 * @param fileContact
	 * @param phoneContact
	 */
	protected void populateWithOutlookPhone(CsvContact fileContact, ArrayList<CsvContact> phoneContact) {
		String phonePhoneFullText = "";
		
		String filePhoneFullText = "";
		if(fileContact.checkPhoneOutlook()) {
			filePhoneFullText = (fileContact.getString(OutlookConstants.O_HOME_FAX) + "\n" + fileContact.getString(OutlookConstants.O_HOME_PHONE) 
					+ "\n" + fileContact.getString(OutlookConstants.O_HOME_PHONE2) + "\n" + fileContact.getString(OutlookConstants.O_WORK_FAX) 
					+ "\n" + fileContact.getString(OutlookConstants.O_WORK_PHONE) + "\n" + fileContact.getString(OutlookConstants.O_WORK_PHONE2)
					+ "\n" + fileContact.getString(OutlookConstants.O_MOBILE) + "\n" + fileContact.getString(OutlookConstants.O_OTHER_PHONE) 
					+ "\n" + fileContact.getString(OutlookConstants.O_PAGER)).trim();
			filePhoneFullText= filePhoneFullText.replaceAll("\\n+", "\n");
		}
									
		int i = 0;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			if(csvLine.checkPhoneOutlook()) {
				phonePhoneFullText += (csvLine.getString(OutlookConstants.O_HOME_FAX) + "\n" + csvLine.getString(OutlookConstants.O_HOME_PHONE) 
						+ "\n" + csvLine.getString(OutlookConstants.O_HOME_PHONE2) + "\n" + csvLine.getString(OutlookConstants.O_WORK_FAX) 
						+ "\n" + csvLine.getString(OutlookConstants.O_WORK_PHONE) + "\n" + csvLine.getString(OutlookConstants.O_WORK_PHONE2)
						+ "\n" + csvLine.getString(OutlookConstants.O_MOBILE) + "\n" + csvLine.getString(OutlookConstants.O_OTHER_PHONE) 
						+ "\n" + csvLine.getString(OutlookConstants.O_PAGER)).trim(); 
				phonePhoneFullText = phonePhoneFullText.replaceAll("\\n+", "\n");
			}
			if(iterator.hasNext()) {
				if(phoneContact.get(++i).checkPhoneThunderbird()) {
					phonePhoneFullText += ("\n"+ lineBreakPattern +"\n");
				}
			}
		} 
		String[] pole = addNewLines(filePhoneFullText.trim(), phonePhoneFullText.trim());
		filePhoneFullText	= pole[0];
		phonePhoneFullText = pole[1];
		
		phonePhoneNumbersTextView.setText(phonePhoneFullText, TextView.BufferType.SPANNABLE);
		csvPhoneNumbersTextView.setText(filePhoneFullText, TextView.BufferType.SPANNABLE);
		// If there were found some equal  fields
		// Find all matches in string
		if(!fileContact.equalTwoStrings(OutlookConstants.O_WORK_PHONE, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_WORK_PHONE), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_WORK_PHONE) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_WORK_PHONE).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_WORK_PHONE2, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_WORK_PHONE2), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_WORK_PHONE2) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_WORK_PHONE2).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_HOME_PHONE, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_HOME_PHONE), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_HOME_PHONE) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_HOME_PHONE).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_HOME_PHONE2, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_HOME_PHONE2), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_HOME_PHONE2) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_HOME_PHONE2).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_HOME_FAX, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_HOME_FAX), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_HOME_FAX) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_HOME_FAX).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_WORK_FAX, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_WORK_FAX), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_WORK_FAX) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_WORK_FAX).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_PAGER, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_PAGER), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_PAGER) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_PAGER).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_MOBILE, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_MOBILE), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_MOBILE) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_MOBILE).length()), Color.RED);
			}
		if(!fileContact.equalTwoStrings(OutlookConstants.O_OTHER_PHONE, ""))
			for (int j = -1; (j = getIndex(phonePhoneFullText, fileContact.getString(OutlookConstants.O_OTHER_PHONE), j + 1)) != -1; ) {
				// If string was found color both
				setColor(csvPhoneNumbersTextView, filePhoneFullText, fileContact.getString(OutlookConstants.O_OTHER_PHONE) , Color.RED);
				setColor(phonePhoneNumbersTextView, new CopyIndex(j, j+fileContact.getString(OutlookConstants.O_OTHER_PHONE).length()), Color.RED);
			}
	}
	
	/**
	 * Method fills TextViews with Organisations and change colors of the same
	 * @param fileContact
	 * @param phoneContact
	 * @param csv
	 * @param phone
	 * @param index1
	 * @param index2
	 */
	protected void populateOrganization(CsvContact fileContact, ArrayList<CsvContact> phoneContact, TextView csv, TextView phone, int index1, int index2, int type) {
		String phoneOrganizationFullText = ""; 
		List<CopyIndex> newMatches = new ArrayList<CopyIndex>();
		int i = 0;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			int startIndex = phoneOrganizationFullText.length();
			
			MoreFieldsComparator org = new MoreFieldsComparator(new String[] { csvLine.getString(index1), 
					csvLine.getString(index2)}, new String[] {fileContact.getString(index1), fileContact.getString(index2)}, matchType);			
			
			boolean shouldMatch = org.areOrganisationsEqual();
			
			phoneOrganizationFullText += (csvLine.getString(index1).trim() + 
					"\n" + csvLine.getString(index2).trim()).trim();
			if(shouldMatch) {
				newMatches.add(new CopyIndex(startIndex, startIndex + (csvLine.getString(index1).trim() + 
					"\n" + csvLine.getString(index2).trim()).trim().length()));
			}
			
			if(iterator.hasNext()) {
				if(type == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
					if(phoneContact.get(++i).checkOrganizationThunderbird()) {
						phoneOrganizationFullText += ("\n"+ lineBreakPattern +"\n");
					}
				}
				else {
					if(phoneContact.get(++i).checkOrganizationOutlook()) {
						phoneOrganizationFullText += ("\n"+ lineBreakPattern +"\n");
					}
				}
					
			}
		} 
			
		String fileOrganizationFullText = (fileContact.getString(index1).trim() + 
									"\n" + fileContact.getString(index2).trim()).trim();
		int lengthBefore = fileOrganizationFullText.length();
		
		String[] pole = addNewLines(fileOrganizationFullText.replaceAll("\\n+", "\n").trim(), phoneOrganizationFullText.replaceAll("\\n+", "\n").trim());
		fileOrganizationFullText = pole[0];
		phoneOrganizationFullText = pole[1];
		
		phone.setText(phoneOrganizationFullText, TextView.BufferType.SPANNABLE);
		csv.setText(fileOrganizationFullText, TextView.BufferType.SPANNABLE);
		
		// If there were found some equal organisations fields
		if(!newMatches.isEmpty()) {
			setColor(csv, new CopyIndex(0, 0+lengthBefore) , Color.RED);
			for (CopyIndex copyIndex : newMatches) {
				setColor(phone, copyIndex, Color.RED);
			}
		}
	}

	/**
	 * Method fills TextViews with Addresses and change colors of the same
	 * @param fileContact
	 * @param phoneContact
	 * @param csv
	 * @param phone
	 */
	protected void populateOutlookAddress(CsvContact fileContact, ArrayList<CsvContact> phoneContact, TextView csv, TextView phone) {
		String phoneAddressFullText = ""; 
		
		List<CopyIndex> newMatches = new ArrayList<CopyIndex>();
		//int i = 0;
		//boolean lineBreaker = false;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			
			if(csvLine.checkPostalHomeOutlook()) {
				int startIndex = phoneAddressFullText.length();
				phoneAddressFullText += (csvLine.getString(OutlookConstants.O_HOME_STREET) + "\n" + csvLine.getString(OutlookConstants.O_HOME_POBOX) + csvLine.getString(OutlookConstants.O_HOME_CITY) + "\n"  + 
						 csvLine.getString(OutlookConstants.O_HOME_REGION) + "\n" +   csvLine.getString(OutlookConstants.O_HOME_PSC) + "\n" + csvLine.getString(OutlookConstants.O_HOME_COUNTRY)).trim().replaceAll("\\n+", "\n");
				if(fileContact.checkPostalHomeOutlook()) {	
					MoreFieldsComparator org = new MoreFieldsComparator(new String[] { csvLine.getString(OutlookConstants.O_HOME_STREET), csvLine.getString(OutlookConstants.O_HOME_POBOX), csvLine.getString(OutlookConstants.O_HOME_CITY) , csvLine.getString(OutlookConstants.O_HOME_REGION), csvLine.getString(OutlookConstants.O_HOME_PSC), csvLine.getString(OutlookConstants.O_HOME_COUNTRY)}, 
							new String[] {fileContact.getString(OutlookConstants.O_HOME_STREET), csvLine.getString(OutlookConstants.O_HOME_POBOX), fileContact.getString(OutlookConstants.O_HOME_CITY) , fileContact.getString(OutlookConstants.O_HOME_REGION), fileContact.getString(OutlookConstants.O_HOME_PSC), fileContact.getString(OutlookConstants.O_HOME_COUNTRY)}, matchType);
					
					boolean shouldMatch = org.areAddressesEqual(MIN_MATCH_FIELDS);
					if(shouldMatch) {
						String help = (csvLine.getString(OutlookConstants.O_HOME_STREET) + "\n" + csvLine.getString(OutlookConstants.O_HOME_POBOX) + csvLine.getString(OutlookConstants.O_HOME_CITY) + "\n"  + 
								 csvLine.getString(OutlookConstants.O_HOME_REGION) + "\n" +   csvLine.getString(OutlookConstants.O_HOME_PSC) + "\n" + csvLine.getString(OutlookConstants.O_HOME_COUNTRY)).trim().replaceAll("\\n+", "\n");
						newMatches.add(new CopyIndex(startIndex, startIndex + help.length(), 1));
						
					}
				}
				phoneAddressFullText += ("\n"+ lineBreakPattern + "\n");
				//lineBreaker = true;
			}
			if(csvLine.checkPostalWorkOutlook()) {
				int startIndex = phoneAddressFullText.length();
				phoneAddressFullText += (csvLine.getString(OutlookConstants.O_WORK_STREET) + "\n" + csvLine.getString(OutlookConstants.O_WORK_POBOX) + csvLine.getString(OutlookConstants.O_WORK_CITY) + "\n"  + 
						 csvLine.getString(OutlookConstants.O_WORK_REGION) + "\n" +   csvLine.getString(OutlookConstants.O_WORK_PSC) + "\n" + csvLine.getString(OutlookConstants.O_WORK_COUNTRY)).trim().replaceAll("\\n+", "\n");
				
				// Check Work addresses and save int with which address they are similar
				if(fileContact.checkPostalWorkOutlook()) {
					MoreFieldsComparator org = new MoreFieldsComparator(new String[] { csvLine.getString(OutlookConstants.O_WORK_STREET), "", csvLine.getString(OutlookConstants.O_WORK_CITY) , csvLine.getString(OutlookConstants.O_WORK_REGION), csvLine.getString(OutlookConstants.O_WORK_PSC), csvLine.getString(OutlookConstants.O_WORK_COUNTRY)}, 
							new String[] {fileContact.getString(OutlookConstants.O_WORK_STREET), "", fileContact.getString(OutlookConstants.O_WORK_CITY) , fileContact.getString(OutlookConstants.O_WORK_REGION), fileContact.getString(OutlookConstants.O_WORK_PSC), fileContact.getString(OutlookConstants.O_WORK_COUNTRY)}, matchType);
					boolean shouldMatch = org.areAddressesEqual(MIN_MATCH_FIELDS);
					
					if(shouldMatch) {
						String help = (csvLine.getString(OutlookConstants.O_WORK_STREET) + "\n" + csvLine.getString(OutlookConstants.O_WORK_POBOX) + csvLine.getString(OutlookConstants.O_WORK_CITY) + "\n"  + 
								 csvLine.getString(OutlookConstants.O_WORK_REGION) + "\n" +   csvLine.getString(OutlookConstants.O_WORK_PSC) + "\n" + csvLine.getString(OutlookConstants.O_WORK_COUNTRY)).trim().replaceAll("\\n+", "\n");
							newMatches.add(new CopyIndex(startIndex, startIndex + help.length(), 2));
					}
				}
				phoneAddressFullText += ("\n"+ lineBreakPattern +"\n");
			}
			if(csvLine.checkPostalOtherOutlook()) {
				int startIndex = phoneAddressFullText.length();
				phoneAddressFullText += csvLine.getString(OutlookConstants.O_OTHER_STREET) + "\n" + csvLine.getString(OutlookConstants.O_OTHER_POBOX) + (csvLine.getString(OutlookConstants.O_OTHER_CITY) + "\n"  + 
						 csvLine.getString(OutlookConstants.O_OTHER_REGION) + "\n" +   csvLine.getString(OutlookConstants.O_OTHER_PSC) + "\n" + csvLine.getString(OutlookConstants.O_OTHER_COUNTRY)).trim().replaceAll("\\n+", "\n");
				
				// Check OTHER addresses and save int with which address they are similar
				if(fileContact.checkPostalOtherOutlook()) {
					MoreFieldsComparator org = new MoreFieldsComparator(new String[] { csvLine.getString(OutlookConstants.O_OTHER_STREET), "", csvLine.getString(OutlookConstants.O_OTHER_CITY) , csvLine.getString(OutlookConstants.O_OTHER_REGION), csvLine.getString(OutlookConstants.O_OTHER_PSC), csvLine.getString(OutlookConstants.O_OTHER_COUNTRY)}, 
							new String[] {fileContact.getString(OutlookConstants.O_OTHER_STREET), "", fileContact.getString(OutlookConstants.O_OTHER_CITY) , fileContact.getString(OutlookConstants.O_OTHER_REGION), fileContact.getString(OutlookConstants.O_OTHER_PSC), fileContact.getString(OutlookConstants.O_OTHER_COUNTRY)}, matchType);
					boolean shouldMatch = org.areAddressesEqual(MIN_MATCH_FIELDS);
					
					if(shouldMatch) {
						String help = (csvLine.getString(OutlookConstants.O_OTHER_STREET) + "\n" + csvLine.getString(OutlookConstants.O_OTHER_POBOX) + csvLine.getString(OutlookConstants.O_OTHER_CITY) + "\n"  + 
								 csvLine.getString(OutlookConstants.O_OTHER_REGION) + "\n" +   csvLine.getString(OutlookConstants.O_OTHER_PSC) + "\n" + csvLine.getString(OutlookConstants.O_OTHER_COUNTRY)).trim().replaceAll("\\n+", "\n");
						newMatches.add(new CopyIndex(startIndex, startIndex + help.length(), 3));
					}
				}
				phoneAddressFullText += ("\n" + lineBreakPattern + "\n");
			}
		} 
		
		String fileAddressFullText = "";
		
		if(fileContact.checkPostalHomeOutlook()) {
			fileAddressFullText = (fileContact.getString(OutlookConstants.O_HOME_STREET) + "\n" + fileContact.getString(OutlookConstants.O_HOME_POBOX) + fileContact.getString(OutlookConstants.O_HOME_CITY) + "\n"  + 
					 fileContact.getString(OutlookConstants.O_HOME_REGION) + "\n" +   fileContact.getString(OutlookConstants.O_HOME_PSC) + "\n" + fileContact.getString(OutlookConstants.O_HOME_COUNTRY)).trim().replaceAll("\\n+", "\n");
		}
		if(fileContact.checkPostalWorkOutlook()) {
			if(!fileAddressFullText.equals("")) {
				fileAddressFullText += "\n" + lineBreakPattern + "\n"; 
			}
			fileAddressFullText += (fileContact.getString(OutlookConstants.O_WORK_STREET) + "\n" + fileContact.getString(OutlookConstants.O_WORK_POBOX) + fileContact.getString(OutlookConstants.O_WORK_CITY) + "\n"  + 
					 fileContact.getString(OutlookConstants.O_WORK_REGION) + "\n" +   fileContact.getString(OutlookConstants.O_WORK_PSC) + "\n" + fileContact.getString(OutlookConstants.O_WORK_COUNTRY)).trim().replaceAll("\\n+", "\n");
		}
		if(fileContact.checkPostalOtherOutlook()) {
			if(!fileAddressFullText.equals("")) {
				fileAddressFullText += "\n" + lineBreakPattern + "\n"; 
			}
			fileAddressFullText += (fileContact.getString(OutlookConstants.O_OTHER_STREET) + "\n" + fileContact.getString(OutlookConstants.O_OTHER_POBOX) + fileContact.getString(OutlookConstants.O_OTHER_CITY) + "\n"  + 
					 fileContact.getString(OutlookConstants.O_OTHER_REGION) + "\n" +   fileContact.getString(OutlookConstants.O_OTHER_PSC) + "\n" + fileContact.getString(OutlookConstants.O_OTHER_COUNTRY)).trim().replaceAll("\\n+", "\n");
		}
		
		String[] pole = addNewLines(fileAddressFullText.replaceAll("\\n+", "\n").trim(), phoneAddressFullText.replaceAll("\\n+", "\n").trim());
		fileAddressFullText = pole[0];
		phoneAddressFullText = pole[1];		
		
		phone.setText(phoneAddressFullText, TextView.BufferType.SPANNABLE);
		csv.setText(fileAddressFullText, TextView.BufferType.SPANNABLE);
		
		// If there were found some equal organisations fields
		if(!newMatches.isEmpty()) {
			for (CopyIndex copyIndex : newMatches) {
				if(copyIndex.who == 1) {
					setColor(csv, fileAddressFullText, (fileContact.getString(OutlookConstants.O_HOME_STREET) + "\n" + fileContact.getString(OutlookConstants.O_HOME_POBOX) + fileContact.getString(OutlookConstants.O_HOME_CITY) + "\n"  + 
							 fileContact.getString(OutlookConstants.O_HOME_REGION) + "\n" +   fileContact.getString(OutlookConstants.O_HOME_PSC) + "\n" + fileContact.getString(OutlookConstants.O_HOME_COUNTRY)).trim().replaceAll("\\n+", "\n")
							 , Color.RED);
				}
				else if(copyIndex.who == 2) {
					setColor(csv, fileAddressFullText, (fileContact.getString(OutlookConstants.O_WORK_STREET) + "\n" + fileContact.getString(OutlookConstants.O_WORK_POBOX) + fileContact.getString(OutlookConstants.O_WORK_CITY) + "\n"  + 
							 fileContact.getString(OutlookConstants.O_WORK_REGION) + "\n" +   fileContact.getString(OutlookConstants.O_WORK_PSC) + "\n" + fileContact.getString(OutlookConstants.O_WORK_COUNTRY)).trim().replaceAll("\\n+", "\n") 
							, Color.RED);
				}
				else if(copyIndex.who == 3) {
					setColor(csv, fileAddressFullText, (fileContact.getString(OutlookConstants.O_OTHER_STREET) + "\n" + fileContact.getString(OutlookConstants.O_OTHER_POBOX) + fileContact.getString(OutlookConstants.O_OTHER_CITY) + "\n"  + 
							 fileContact.getString(OutlookConstants.O_OTHER_REGION) + "\n" +   fileContact.getString(OutlookConstants.O_OTHER_PSC) + "\n" + fileContact.getString(OutlookConstants.O_OTHER_COUNTRY)).trim().replaceAll("\\n+", "\n") 
							, Color.RED);
				}
				setColor(phone, copyIndex, Color.RED);
			}
		}
	}
	
	protected void populateThunderbirdAddress(CsvContact fileContact, ArrayList<CsvContact> phoneContact, TextView csv, TextView phone) {
		String phoneAddressFullText = ""; 
		
		List<CopyIndex> newMatches = new ArrayList<CopyIndex>();
		//int i = 0;
		//boolean lineBreaker = false;
		for (Iterator<CsvContact> iterator = phoneContact.iterator(); iterator.hasNext();) {
			CsvContact csvLine = (CsvContact) iterator.next();
			
			if(csvLine.checkPostalHomeThunderbird()) {
				int startIndex = phoneAddressFullText.length();
				phoneAddressFullText += (csvLine.getString(ThunderbirdConstants.T_HOME_STREET) + "\n" + csvLine.getString(ThunderbirdConstants.T_HOME_CITY) + "\n" +  csvLine.getString(ThunderbirdConstants.T_HOME_REGION) + "\n" +  
										csvLine.getString(ThunderbirdConstants.T_HOME_PSC) + "\n" + csvLine.getString(ThunderbirdConstants.T_HOME_COUNTRY) ).trim().replaceAll("\\n+", "\n");
				if(fileContact.checkPostalHomeThunderbird()) {	
					MoreFieldsComparator org = new MoreFieldsComparator(new String[] { csvLine.getString(ThunderbirdConstants.T_HOME_STREET), "", csvLine.getString(ThunderbirdConstants.T_HOME_CITY) , csvLine.getString(ThunderbirdConstants.T_HOME_REGION), csvLine.getString(ThunderbirdConstants.T_HOME_PSC), csvLine.getString(ThunderbirdConstants.T_HOME_COUNTRY)}, 
							new String[] {fileContact.getString(ThunderbirdConstants.T_HOME_STREET), "", fileContact.getString(ThunderbirdConstants.T_HOME_CITY) , fileContact.getString(ThunderbirdConstants.T_HOME_REGION), fileContact.getString(ThunderbirdConstants.T_HOME_PSC), fileContact.getString(ThunderbirdConstants.T_HOME_COUNTRY)}, matchType);
					
					boolean shouldMatch = org.areAddressesEqual(MIN_MATCH_FIELDS);
					if(shouldMatch) {
						String help = (csvLine.getString(ThunderbirdConstants.T_HOME_STREET) + "\n" + csvLine.getString(ThunderbirdConstants.T_HOME_CITY) + "\n" +  csvLine.getString(ThunderbirdConstants.T_HOME_REGION) + "\n" +  
								csvLine.getString(ThunderbirdConstants.T_HOME_PSC) + "\n" + csvLine.getString(ThunderbirdConstants.T_HOME_COUNTRY) ).trim().replaceAll("\\n+", "\n");
						newMatches.add(new CopyIndex(startIndex, startIndex + help.length(), 1));
						
					}
				}
				phoneAddressFullText += ("\n" + lineBreakPattern + "\n");
				//lineBreaker = true;
			}
			if(csvLine.checkPostalWorkThunderbird()) {
				int startIndex = phoneAddressFullText.length();
				phoneAddressFullText += (csvLine.getString(ThunderbirdConstants.T_WORK_STREET) + "\n" + csvLine.getString(ThunderbirdConstants.T_WORK_CITY) + "\n" +  csvLine.getString(ThunderbirdConstants.T_WORK_REGION) + "\n" +  
						csvLine.getString(ThunderbirdConstants.T_WORK_PSC) + "\n" + csvLine.getString(ThunderbirdConstants.T_WORK_COUNTRY) ).trim().replaceAll("\\n+", "\n");
				
				// Check Work addresses and save int with which address they are similar
				if(fileContact.checkPostalWorkThunderbird()) {
					MoreFieldsComparator org = new MoreFieldsComparator(new String[] { csvLine.getString(ThunderbirdConstants.T_WORK_STREET), "", csvLine.getString(ThunderbirdConstants.T_WORK_CITY) , csvLine.getString(ThunderbirdConstants.T_WORK_REGION), csvLine.getString(ThunderbirdConstants.T_WORK_PSC), csvLine.getString(ThunderbirdConstants.T_WORK_COUNTRY)}, 
							new String[] {fileContact.getString(ThunderbirdConstants.T_WORK_STREET), "", fileContact.getString(ThunderbirdConstants.T_WORK_CITY) , fileContact.getString(ThunderbirdConstants.T_WORK_REGION), fileContact.getString(ThunderbirdConstants.T_WORK_PSC), fileContact.getString(ThunderbirdConstants.T_WORK_COUNTRY)}, matchType);
					boolean shouldMatch = org.areAddressesEqual(MIN_MATCH_FIELDS);
					
					if(shouldMatch) {
						String help = (csvLine.getString(ThunderbirdConstants.T_WORK_STREET) + "\n" + csvLine.getString(ThunderbirdConstants.T_WORK_CITY) + "\n" +  csvLine.getString(ThunderbirdConstants.T_WORK_REGION) + "\n" +  
								csvLine.getString(ThunderbirdConstants.T_WORK_PSC) + "\n" + csvLine.getString(ThunderbirdConstants.T_WORK_COUNTRY) ).trim().replaceAll("\\n+", "\n");
						newMatches.add(new CopyIndex(startIndex, startIndex + help.length(), 2));
					}
				}
				phoneAddressFullText += ("\n"+ lineBreakPattern + "\n");
			}
		} 
		
		String fileAddressFullText = "";
		
		if(fileContact.checkPostalHomeThunderbird()) {
			fileAddressFullText = (fileContact.getString(ThunderbirdConstants.T_HOME_STREET) + "\n" + fileContact.getString(ThunderbirdConstants.T_HOME_CITY) + "\n" +  fileContact.getString(ThunderbirdConstants.T_HOME_REGION) + "\n" +  
					fileContact.getString(ThunderbirdConstants.T_HOME_PSC) + "\n" + fileContact.getString(ThunderbirdConstants.T_HOME_COUNTRY)).trim().replaceAll("\\n+", "\n");
		}
		if(fileContact.checkPostalWorkThunderbird()) {
			if(!fileAddressFullText.equals("")) {
				fileAddressFullText += "\n" + lineBreakPattern + "\n"; 
			}
			fileAddressFullText += (fileContact.getString(ThunderbirdConstants.T_WORK_STREET) + "\n" + fileContact.getString(ThunderbirdConstants.T_WORK_CITY) + "\n" +  fileContact.getString(ThunderbirdConstants.T_WORK_REGION) + "\n" +  
					fileContact.getString(ThunderbirdConstants.T_WORK_PSC) + "\n" + fileContact.getString(ThunderbirdConstants.T_WORK_COUNTRY)).trim().replaceAll("\\n+", "\n");
		}
		String[] pole = addNewLines(fileAddressFullText.trim().replaceAll("\\n+", "\n").trim(), phoneAddressFullText.trim().replaceAll("\\n+", "\n").trim());
		fileAddressFullText = pole[0];
		phoneAddressFullText = pole[1];		
		
		//if()
		
		phone.setText(phoneAddressFullText, TextView.BufferType.SPANNABLE);
		csv.setText(fileAddressFullText, TextView.BufferType.SPANNABLE);
		
		// If there were found some equal organisations fields
		if(!newMatches.isEmpty()) {
			for (CopyIndex copyIndex : newMatches) {
				if(copyIndex.who == 1) {
					setColor(csv, fileAddressFullText, (fileContact.getString(ThunderbirdConstants.T_HOME_STREET) + "\n" + fileContact.getString(ThunderbirdConstants.T_HOME_CITY) + "\n" +  fileContact.getString(ThunderbirdConstants.T_HOME_REGION) + "\n" +  
							fileContact.getString(ThunderbirdConstants.T_HOME_PSC) + "\n" + fileContact.getString(ThunderbirdConstants.T_HOME_COUNTRY) ).trim().replaceAll("\\n+", "\n")
							 , Color.RED);
				}
				else if(copyIndex.who == 2) {
					setColor(csv, fileAddressFullText, (fileContact.getString(ThunderbirdConstants.T_WORK_STREET) + "\n" + fileContact.getString(ThunderbirdConstants.T_WORK_CITY) + "\n" +  fileContact.getString(ThunderbirdConstants.T_WORK_REGION) + "\n" +  
							fileContact.getString(ThunderbirdConstants.T_WORK_PSC) + "\n" + fileContact.getString(ThunderbirdConstants.T_WORK_COUNTRY)).trim().replaceAll("\\n+", "\n") 
							, Color.RED);
				}
				setColor(phone, copyIndex, Color.RED);
			}
		}
	}
	
	/**
	 * Method extract data's from bundle and sets them to appropriate variable
	 */
	private void getExtrasFromBundle() {
		Intent i = getIntent();
		if(i.getSerializableExtra(POSSIBLE_LIST_ID) != null) {
			@SuppressWarnings("unchecked")
			ArrayList<PossibleEqualContacts> serializableExtra = (ArrayList<PossibleEqualContacts>) i.getSerializableExtra(POSSIBLE_LIST_ID);
			listOfPossibleEqualContacts = serializableExtra;
			if(debug) Log.v(TAG, String.valueOf(listOfPossibleEqualContacts.size()));
		} else { 
			// TODO ZLE zatvorenie
			exitDialog();
		}
		switch(i.getIntExtra(MATCH_TYPE_STRING, 4)) {
			case 0 :
				matchType = MatchType.CASE_SENSITIVE;
				break;
			case 1:
				matchType = MatchType.IGNORE_CASE;
				break;
			case 2:
				matchType = MatchType.IGNORE_ACCENTS_CASE_SENSITIVE;
				break;
			case 3:
				matchType = MatchType.IGNORE_ACCENTS_AND_CASES;
				break;
				default:
					finish();
				break;
			}
		if(i.getParcelableExtra(ACCOUNT_TYPE_STRING) != null) {
			
			Account parc = (Account) i.getParcelableExtra(ACCOUNT_TYPE_STRING);
			pickedAccount = parc;
			if(debug) 
				Log.v(TAG, String.valueOf(pickedAccount.toString()));
		} else { 
			// TODO ZLE zatvorenie
			exitDialog();
		}
		if(i.getBooleanArrayExtra(MATCH_ADDRESS_TYPE_STRING)!= null)  {
			MIN_MATCH_FIELDS = i.getBooleanArrayExtra(MATCH_ADDRESS_TYPE_STRING);
			if(debug) 
				Log.v(TAG, String.valueOf(i.getBooleanArrayExtra(MATCH_ADDRESS_TYPE_STRING).toString()));
		}
		else { 
				// TODO ZLE zatvorenie
				exitDialog();
		}
	}
	
	protected void setColors(String phoneNameFullText, String fileNameFullText , String help) {
		int i = 0;
		if((i = getIndex(phoneNameFullText, help)) != -1) {
			setColor(phoneNameTextView, 
					new CopyIndex(i, i+help.length()), 
					Color.RED);
			setColor(csvNameTextView, fileNameFullText, help, Color.RED);
		}
	}
	
	private void setColor(TextView view, String fulltext , String subtext, int color) {
		  Spannable str = (Spannable) view.getText();
		  int i = -1;
		  i = getIndex(fulltext, subtext);
		  if(i != -1)
			  str.setSpan(new ForegroundColorSpan(color), i, i+subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	private void setColor(TextView view, CopyIndex c, int color) {
	      Spannable str = (Spannable) view.getText();
	      str.setSpan(new ForegroundColorSpan(color), c.from, c.to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
	
	private int getIndex(String fulltext, String subtext) {
		switch (matchType) {
			case CASE_SENSITIVE:
				// Case Sensitive
				return fulltext.indexOf(subtext);
			case IGNORE_CASE:
				// CASE_INSENSITIVE
				return fulltext.toLowerCase().indexOf(subtext.toLowerCase());
			case IGNORE_ACCENTS_CASE_SENSITIVE:
				// CASE SENSITIVYE IGNORING ONLY ACCENT CHARS
				return StringUtils.convertNonAscii(fulltext).indexOf(StringUtils.convertNonAscii(subtext));
			case IGNORE_ACCENTS_AND_CASES:
				// Case INSENSITIVE IGNORING ACCENTS
				return StringUtils.toUpperCaseSansAccent(fulltext).indexOf(StringUtils.toUpperCaseSansAccent(subtext));
		}  
		return -1;
	}
	
	private int getIndex(String fulltext, String subtext, int startIndex) {
		switch (matchType) {
			case CASE_SENSITIVE:
				// Case Sensitive
				return fulltext.indexOf(subtext, startIndex);
			case IGNORE_CASE:
				// CASE_INSENSITIVE
				return fulltext.toLowerCase().indexOf(subtext.toLowerCase(), startIndex);
			case IGNORE_ACCENTS_CASE_SENSITIVE:
				// CASE SENSITIVYE IGNORING ONLY ACCENT CHARS
				return StringUtils.convertNonAscii(fulltext).indexOf(StringUtils.convertNonAscii(subtext), startIndex);
			case IGNORE_ACCENTS_AND_CASES:
				// Case INSENSITIVE IGNORING ACCENTS
				return StringUtils.toUpperCaseSansAccent(fulltext).indexOf(StringUtils.toUpperCaseSansAccent(subtext), startIndex);
		}  
		return -1;
	}
	
	private class CopyIndex {
		public int from;
		public int to;
		public int who;
		
		public CopyIndex(int i, int t) {
			from = i;
			to = t;
			who = -1;
		}
		
		public CopyIndex(int i, int t, int w) {
			this(i,t);
			who = w;
		}
	}
	
	
	private static int countLines(String str, String findStr) {
		int lastIndex = 0;
		int count = 0;

		if(str.equals(""))
			return 0;
		
		while(lastIndex != -1){
		       lastIndex = str.indexOf(findStr, lastIndex);
		       if(lastIndex != -1 && lastIndex + 1 != str.length()) {
		    	   lastIndex+= findStr.length();
		       }
		       if(lastIndex + 1 == str.length()) {
		    	   count++;
		       		break;
		       }
		       if( lastIndex != -1) {
		             count++;
		      }
		}
		return count;
	}
	
	private static String[] addNewLines(String s1, String s2) {
		int s1LinesCount = countLines(s1, "\n");
		int s2LinesCount = countLines(s2, "\n");
		
		int s1LinesCr = countLines(s1, "\r\n");
		int s2LinesCr = countLines(s2, "\r\n");
		
		if(s1LinesCr > 0 || s2LinesCr > 0) {
			s1LinesCount = s1LinesCr;
			s2LinesCount = s2LinesCr;
		}
		
		if(s1LinesCount == s2LinesCount)
			return new String[] {s1, s2};
		else if(s1LinesCount > s2LinesCount) {
			for(int j = 0; j < s1LinesCount - s2LinesCount; j++) {
				if(j == 0 && s2.equals("")) {
					s2 += lineBreakPattern;
				}
				s2 += "\n" + lineBreakPattern;
			}
			return new String[] {s1, s2};
		}
		else {
			for(int j = 0; j < s2LinesCount - s1LinesCount; j++) {
				if(j == 0 && s1.equals("")) {
					s1 += lineBreakPattern;
				} 
				s1 += "\n" + lineBreakPattern;
			}
			return new String[] {s1, s2};
		}
	}
	
	/**
	 * Method showing Toast message on Screen with this message
	 * @param msg - message that prints on screen
	 */
	protected void makeToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
}
