package bp.iemanager.importer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import bp.iemanager.MessageObject;
import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.OutlookConstants;
import bp.iemanager.csvcontact.ThunderbirdConstants;

import sk.kasala.viliam.bakalarka.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Class reprezenting activity for importing CSV Contact into Android System.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class ImporterActivity extends Activity {
	/**
	 * String identifying from which Class debug :PRINTING" come from
	 */
	@SuppressWarnings("unused")
	private static String TAG = ImporterActivity.class.getSimpleName();
	
	/**
	 * File path for Application main folder
	 */
	private static final String INIT_FOLDER_IMPORT ="IE Manager" + java.io.File.separator;
	
	/**
	 * RESULT CODE FOR CATCHING DATA FROM ANOTHER ACTIVITY and appropriate action
	 */
	private static final int PICKFILE_RESULT_CODE = 1;
	
	/**
	 * RESULT CODE FOR CATCHING DATA FROM ANOTHER ACTIVITY and appropriate action
	 */
	private static final int SYNCHRONIZER_RESULT_CODE = 2;
	
	private static final String POSSIBLE_LIST_ID = "bp.iemanager.importContacts/SERIALIZABLE_ARRAY_LIST";
	private static final String MATCH_TYPE_STRING = "bp.iemanager.importContacts/MATCH$TYPE";
	private static final String ACCOUNT_TYPE_STRING = "bp.iemanager.importContacts/ACCOUNT$NEW";
	private static final String MATCH_ADDRESS_TYPE_STRING = "bp.iemanager.importContacts/ADDRESS%TYPE";
	
	/**
	 * DIALOG UNIQUE ID's
	 */
	private static final int DIALOG_PICK_ACCOUNT = 1;
	private static final int DIALOG_PICK_WITH_ACCOUNTS_TO_SYNCHRONIZE = 2;
	private static final int DIALOG_WANT_SYNCHRONIZE_CONTACTS = 3;
	
	private static final int DIALOG_PICK_FILE = 5;
	private static final int DIALOG_PROGRESS = 6;
	private static final int DIALOG_REALLY_IMPORT = 7;
	private static final int DIALOG_PICK_ADDRESS_OPTIONS = 8;
	private static final int DIALOG_PICK_MIN_MATCH_OPTIONS = 9;
	private static final int DIALOG_PICK_MATCH_TYPE = 10;
	private static final int DIALOG_PICK_CHARSET_TYPE = 11;
	
	
	/**
	 * Constant attributes for displaying different options for user
	 */
	private static final String[] ADDRESS_OPTIONS_NAMES = new String[] {"STREET", "POBOX", "CITY", "REGION", "POSTCODE", "COUNTRY"};
	
	/**
	 * Constant attributes for displaying different options for user
	 */
	private static final String[] MIN_MATCH_TO_SYNC_CONTACT = new String[] {"EMAIL", "PHONE"};
	
	/**
	 * Constant attributes for displaying different options for user
	 */
	private static final String[] MATCH_TYPE_LABEL = new String[] {"CASE SENSITIVE", "IGNORE CASES", "IGNORE ACCENTS CASE SENSITIVE" ,"IGNORE ACCENTS AND CASES"};
	
	/**
	 * Pattern for matching two address fields. Default options are set.
	 */
	private boolean[] minAddressFieldsToMatch = { false, false, true, false, true, true };
	
	/**
	 * Pattern for declaring contacts as the same. Default options are set.
	 */
	private boolean[] minFieldsForMatchToSync = { true, false};
	
	/**
	 * References to TextViews of Layout from import_layout.xml
	 */
	private Button exitButton;
	private Button startButton;
	private Button myManagerButton;
	private Button otherManagerButton;
	private TextView newContactAccountsTextView;
	private TextView synchronizeWithAccountsTextView;
	private TextView fileNameTextView;
	private TextView fileDirTextView;
	private TextView addressOptionsTextView;
	private TextView minMatchOptionsTextView;
	private TextView matchTypeTextView;
	private TextView importEncodingTypeTextView;
	
	/**
	 * Class providing calling of Sync
	 */
	private ProgressThread progressThread;
	
	/**
	 * Reference to ProgressDialog to be able show progress and message
	 */
	private ProgressDialog progressDialog;
	
	/**
	 * Attribute representing progress of ProgressDialog
	 */
	private int maxProgress;
	
	/**
	 * Account type to which new Contact should be joined
	 */
	private Account pickedAccountForNewContact = null;
	
	/**
	 * Accounts to be sync Contact from file with Android
	 */
	private HashSet<Account> pickedAccountsForSync;
	
	/**
	 * Reference where will be added Pair of contacts which are marked as Possible Match.
	 * After automatic sync we will be asked to sync contact where can't be decided if it is match
	 */
	protected ArrayList<PossibleEqualContacts> listOfPossibleEqualItems;
	
	/**
	 * User selected file representing by full file path
	 */
	private String pickedFileWithFullPath = "";
	
	/**
	 * User selected charset - Default CHARSET UTF-8
	 */
	Charset pickedCharset = Charset.forName("UTF-8");
	
	/**
	 * Match type for Sync of all fields
	 */
	private MatchType matchType = MatchType.CASE_SENSITIVE;
	//-----------------------------------------------------//
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_layout);
		
		// Ziskaj odkazy na prvky GUI z resources
		exitButton = (Button) findViewById(R.id.import_button_exit);
		startButton = (Button) findViewById(R.id.import_button_start);
		myManagerButton = (Button) findViewById(R.id.import_my_file_explorer_button);
		otherManagerButton = (Button) findViewById(R.id.import_different_file_explorer_button);
		newContactAccountsTextView = (TextView) findViewById(R.id.import_edit_account);
		synchronizeWithAccountsTextView = (TextView) findViewById(R.id.import_debug_textview);
		
		fileNameTextView = (TextView) findViewById(R.id.import_filename_textView);
		fileDirTextView = (TextView) findViewById(R.id.import_file_dir_textView);
		addressOptionsTextView = (TextView) findViewById(R.id.import_sync_addres_options_textView);
		minMatchOptionsTextView = (TextView) findViewById(R.id.min_match_fields_to_synch_contacts);
		matchTypeTextView = (TextView) findViewById(R.id.import_type_match);
		importEncodingTypeTextView = (TextView) findViewById(R.id.import_encoding_type_textView);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Zaregistruj listener-y
		newContactAccountsTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_ACCOUNT);
			}
		});
		
		synchronizeWithAccountsTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_WITH_ACCOUNTS_TO_SYNCHRONIZE);
			}
		});
		
		exitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onExitButtonClicked();
			}
		});
		
		startButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onStartButtonClicked();
			}
		});
		
		otherManagerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPickFileActivity();
			}
		});
		
		myManagerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_FILE);
			}
		});
		
		addressOptionsTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_ADDRESS_OPTIONS);
			}
		});
		
		minMatchOptionsTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_MIN_MATCH_OPTIONS);
			}
		});
		
		matchTypeTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_MATCH_TYPE);
			}
		});
		
		importEncodingTypeTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_CHARSET_TYPE);
			}
		});
		
		pickedAccountsForSync = new HashSet<Account>();
		populateAccounts();
		showFileNameText();
		populateAddressOptions();
		populateMinMatchOptions();
		populateMatchType();
		populateEncodingType();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case PICKFILE_RESULT_CODE:
				if(resultCode == RESULT_OK) {
					if(!(new File(data.getData().getPath())).exists())
						makeToast("Picked File doesn't exists. Please try another File Manager");
					else {
						if(data.getData().getPath().substring(data.getData().getPath().lastIndexOf(".")).equalsIgnoreCase(".csv")) {
							setFilePath(data.getData().getPath());
						}
						else {
							makeToast("You didn't pick CSV File.");
						}
					}
				}
				else if(requestCode == RESULT_CANCELED) {
					makeToast("No File Picked!");
				}
				break;
			case SYNCHRONIZER_RESULT_CODE:
				listOfPossibleEqualItems.clear();
		}
	}
	
	/**
	 * Method sets new file path to attribute
	 * @param filePath path that should be set
	 */
	private void setFilePath(String filePath) {
		pickedFileWithFullPath = filePath;
		showFileNameText();
	}
	
	/**
	 * Method shows picked file name
	 */
	private void showFileNameText() {
		if(pickedFileWithFullPath == "") {
			fileDirTextView.setText("");
			fileNameTextView.setText("");
		}
		else {
			fileDirTextView.setText(pickedFileWithFullPath.substring(0,pickedFileWithFullPath.lastIndexOf(java.io.File.separator)));
			fileNameTextView.setText(pickedFileWithFullPath.substring(pickedFileWithFullPath.lastIndexOf(java.io.File.separator)+1));
		}
	}
	
	/**
	 * Start another activity for Selecting a file if app is not found open Dialog Box pick file
	 */
	protected void showPickFileActivity() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	    intent.setType("text/csv");
	    intent.addCategory(Intent.CATEGORY_OPENABLE);
	    try {
	    	startActivityForResult(intent,PICKFILE_RESULT_CODE);	
	    } catch (ActivityNotFoundException e) {
	    	showDialog(DIALOG_PICK_FILE);
		}		
	}
	
	/**
	 * Function calls Synchronizer activity and pass him arguments such as 
	 * listOfPossibleEqualItems,
	 * matchType - to see how to compare it
	 * account to which 
	 */
	protected void callSynchronizer() {
		Intent i = new Intent(this, SynchronizerActivity.class);
		i.putExtra(POSSIBLE_LIST_ID, listOfPossibleEqualItems);
		int j = 0;
		switch(matchType) {
			case CASE_SENSITIVE:
				j = 0;
				break;
			case IGNORE_CASE:
				j = 1;
				break;
			case IGNORE_ACCENTS_AND_CASES:
				j = 2;
				break;
		}
		
		i.putExtra(MATCH_TYPE_STRING, j);
		i.putExtra(ACCOUNT_TYPE_STRING, pickedAccountForNewContact);
		i.putExtra(MATCH_ADDRESS_TYPE_STRING, minAddressFieldsToMatch);
		startActivityForResult(i, SYNCHRONIZER_RESULT_CODE);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_PICK_ACCOUNT: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.import_account_label);
				AccountManager manager = AccountManager.get(this);
			    final Account[] accountsForNewContact = manager.getAccounts();
			    
	     
			    final String[] namesForNewContact = new String[accountsForNewContact.length];
			    
			    
			    for (int i = 0; i < accountsForNewContact.length; i++) {
					namesForNewContact[i] = accountsForNewContact[i].name + "\n" + accountsForNewContact[i].type;
				}
			    
			    builder.setItems(namesForNewContact, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			            pickedAccountForNewContact = accountsForNewContact[which];
			            newContactAccountsTextView.setText(namesForNewContact[which]);
			        }
			    });
			    return builder.create();
			}
			case DIALOG_PICK_WITH_ACCOUNTS_TO_SYNCHRONIZE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.import_synchronize_account);
				AccountManager manager = AccountManager.get(this);
			    final Account[] accounts = manager.getAccounts();
			    
			    pickedAccountsForSync.clear();
			  
			    final String[] names = new String[accounts.length];
			    boolean[] checkedItems = new boolean[accounts.length];
			    
			    for (int i = 0; i < accounts.length; i++) {
					names[i] = accounts[i].name + "\n" + accounts[i].type;
					checkedItems[i] = false;
				}
			    builder.setMultiChoiceItems(names, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						if(isChecked) {
							pickedAccountsForSync.add(accounts[which]);
						}
						else {
							pickedAccountsForSync.remove(accounts[which]);
						}
					}});
			    
			    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						populateAccounts();
					}
				});
			    return builder.create();  
			}
			case DIALOG_WANT_SYNCHRONIZE_CONTACTS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("There was found " + listOfPossibleEqualItems.size() + " to synchronize !\n" + "Do you want synchronize this contacts?")
						.setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								callSynchronizer();
							}
						})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								listOfPossibleEqualItems.clear();
							}
						});
				return builder.create();
			}
			case DIALOG_PICK_FILE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick file");
				File file = new File(Environment.getExternalStorageDirectory().getPath() + java.io.File.separator + INIT_FOLDER_IMPORT);
				final String[] files = file.list(new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						if(filename.contains(".csv"))
							return true;
						else if(filename.contains(".CSV")) {
							return true;
						}
						return false;
					}
				});
			    
				builder.setItems(files, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			    		setFilePath(Environment.getExternalStorageDirectory().getPath() + java.io.File.separator + INIT_FOLDER_IMPORT + files[which]);
			            //dialog.cancel();
			        }
			    });
				
				return builder.create();
			}
			case DIALOG_REALLY_IMPORT: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Do you really want to import contacts?");
				builder.setCancelable(false);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						prepareContactsImporter();
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				return builder.create();
			}
			case DIALOG_PROGRESS: {
				progressDialog = new ProgressDialog(this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setCancelable(false);
				progressDialog.setMessage("Synchronizing...");
				return progressDialog;
			}
			case DIALOG_PICK_ADDRESS_OPTIONS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Pattern to Sync Address Fields")
				.setMultiChoiceItems(ADDRESS_OPTIONS_NAMES, minAddressFieldsToMatch, new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						minAddressFieldsToMatch[which] = isChecked;
					}
				 })
			    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						populateAddressOptions();
					}
				});
			    return builder.create();  
			}
			case DIALOG_PICK_MIN_MATCH_OPTIONS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Min Match to Sync Contacts")
				.setMultiChoiceItems(MIN_MATCH_TO_SYNC_CONTACT, minFieldsForMatchToSync, new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						minFieldsForMatchToSync[which] = isChecked;
					}
				 })
			    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						populateMinMatchOptions();
					}
				});
			    return builder.create();  
			}
			case DIALOG_PICK_MATCH_TYPE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Match type");
				builder.setItems(MATCH_TYPE_LABEL, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			            switch(which) {
			            case 0:
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
			            }
			            populateMatchType();
			        }
			    });
			    return builder.create();
			}
			case DIALOG_PICK_CHARSET_TYPE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Pick export file encoding");
				SortedMap<String, Charset> chmap = Charset.availableCharsets();
				final String[] charsetNames = new String[chmap.size()];
				int i = 0;
				for (Iterator<String> it = chmap.keySet().iterator(); it.hasNext();) {
					charsetNames[i++] = (String) it.next();
				}
				builder.setItems(charsetNames, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			            pickedCharset = Charset.forName(charsetNames[which]);
			            populateEncodingType();
			        }
			    });
			    return builder.create();
			}
		}
		return null;
	}
	
	/**
     * Populate Minimum Match Option TextView with picked options.
     */
    private void populateMinMatchOptions() {
    	minMatchOptionsTextView.setText("");
    	String s = "";
    	for (int i = 0; i < minFieldsForMatchToSync.length; i++) {
			if(minFieldsForMatchToSync[i]) {
				s += MIN_MATCH_TO_SYNC_CONTACT[i] + "\n";
			}
		}
    	s = s.trim();
    	s += "\nNAME";
    	minMatchOptionsTextView.setText(s);
    }
	
    /**
     * Populate Match type fields TextView with picked options.
     */
    private void populateMatchType() {
    	switch (matchType) {
			case CASE_SENSITIVE:
				matchTypeTextView.setText("CASE SENSITIVE");
				break;
			case IGNORE_CASE:
				matchTypeTextView.setText("IGNORE CASES");
				break;
			case IGNORE_ACCENTS_CASE_SENSITIVE:
				matchTypeTextView.setText("IGNORE ACCENTS CASE SENSITIVE");
				break;
			case IGNORE_ACCENTS_AND_CASES:
				matchTypeTextView.setText("IGNORE ACCENTS AND CASES");
				break;		
    	}
    }
    
	/**
     * Populate Encoding type TextView with picked encoding type.
     */
    private void populateEncodingType() {
    	importEncodingTypeTextView.setText(pickedCharset.name());
    }
    
    /**
     * Populate Encoding type textView wich picked Encoding type
     */
    private void populateAddressOptions() {
    	addressOptionsTextView.setText("");
    	String s = "";
    	for (int i = 0; i < minAddressFieldsToMatch.length; i++) {
    		if(minAddressFieldsToMatch[i])
    			s += ("\n"+ ADDRESS_OPTIONS_NAMES[i]);			
		}
    	addressOptionsTextView.append(s.trim());
    }
	
    /**
     * Exit activity button was clicked and we want exitAcitivty
     */
	private void onExitButtonClicked() {
		exitActivity();
	}
	
	/**
	 * Exit Activity
	 */
	private void exitActivity() {
		finish();
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
			case DIALOG_PROGRESS: 
				progressDialog.setMax(getMaxProgress());
				progressDialog.setProgress(0);
				progressThread.start();
				break;
		}	
	}
	
	/**
	 * Start button was clicked, also checks if all values are correct
	 */
	private void onStartButtonClicked() {
		if(pickedAccountForNewContact == null) {
			makeToast("Select account to which we will add new Contacts");
			return;
		}
		if(pickedAccountsForSync.isEmpty()) {
			makeToast("Select Accounts with which we will Sync Contacts");
			return;
		}
		if(pickedFileWithFullPath.equals("")) {
			makeToast("Select some file");
			return;
		}
		if(!new File(pickedFileWithFullPath).exists()) {
			makeToast("File doesn't exist");
			return;
		}
		if(!pickedFileWithFullPath.contains(".csv") && !pickedFileWithFullPath.contains(".CSV")) {
			makeToast("File is not CSV type");
			return;
		}
		showDialog(DIALOG_REALLY_IMPORT);
	}
	
	/**
	 * Method creates a string of selected accounts and displays it in appropriate TextView.
	 */
    private void populateAccounts() {
		if(pickedAccountsForSync.isEmpty()) {
			synchronizeWithAccountsTextView.setText(R.string.empty_label);
			return;
		}
		synchronizeWithAccountsTextView.setText("");
		int i = 1;
		for (Iterator<Account> iterator = pickedAccountsForSync.iterator(); iterator.hasNext();) {
			Account account = (Account) iterator.next();
			
			synchronizeWithAccountsTextView.append(" " + i++ + ". " + account.name + "\n" + account.type );
			if(iterator.hasNext())
				synchronizeWithAccountsTextView.append("\n-\t\t\t-\t\t\t-\t\t\t-\t\t\t-\t\t\t-\n");
		}
		return;
	}
	
    /**
     * Method reads all CSV rows, then set progressBar and inicialize all attributes for importing.
     * Also show dialog.
     */
	private void prepareContactsImporter() {
		try {
			
			CSVReader reader = null;
			
			reader = new CSVReader(new InputStreamReader(new FileInputStream(pickedFileWithFullPath), pickedCharset));
			
			// Ziskaj zoznam CSV Poloziek
			List<String []> csvEntries = reader.readAll();						
			reader.close();
			// Check types
			if(csvEntries.isEmpty()) {
				makeToast("Wrong Content of file!");
				return;
			}
			if((csvEntries.get(0).length != ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) && (csvEntries.get(0).length != OutlookConstants.OUTLOOK_ITEM_COUNT)) {
				makeToast("Header of csv file don't match with supported standards");
				return;
			}
			setMaxProgress(csvEntries.size() - 1);
			
			listOfPossibleEqualItems = new ArrayList<PossibleEqualContacts>();
			progressThread = new ProgressThread(handler, this, csvEntries);
			showDialog(DIALOG_PROGRESS);	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method showing Toast message on Screen with this message
	 * @param msg - message that prints on screen
	 */
	protected void makeToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Implemented handler interface which provide service for filling progressBar with message and progress value.
	 */
	public final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			MessageObject obj = (MessageObject) msg.obj;
			if(obj.getTotal() < 0) {
				message = "Synchronizing:\n" + obj.getName();
				runOnUiThread(changeMessage);
			}
			else {
				progressDialog.setProgress(obj.getTotal());
			}
			if(obj.getTotal() >= getMaxProgress()) {
				dismissDialog(DIALOG_PROGRESS);
				setProgress(0);
				if(listOfPossibleEqualItems.size() > 0) 
					showDialog(DIALOG_WANT_SYNCHRONIZE_CONTACTS);
			}
		}
	};
	
	/**
	 * Set new value to maxProgress attribute
	 * @param max - new value
	 */
	private void setMaxProgress(int max) {
		maxProgress = max;
	}
	
	/**
	 * Get value of maxProgress attribute
	 * @return value of attribute
	 */
	private int getMaxProgress() {
		return maxProgress;
	}	
	
	/**
	 * Attribute representing message which should be send to progressDialog.
	 */
	private String message = "";
	
	/**
	 * New thread which should update message of the progressDialog.
	 */
	private Runnable changeMessage = new Runnable() {
	    public void run() {
	        progressDialog.setMessage(message);
	    }
	};
		
	/**
	 * Inner class providing service for Synchronizing contacts and filling progress bar.
	 * It runs in new thread. 
	 */
	private class ProgressThread extends Thread {
		public Handler mHandler;
		private int total;
		private Context ctx;
		private List<String []> csvEntries;
				
		public ProgressThread(Handler _h, Context _ctx, List<String []> _csvEntries) {
			mHandler = _h;
			ctx = _ctx;
			csvEntries = _csvEntries;
		}

		public void run() {
			total = 0;
			for (Iterator<String[]> iterator = csvEntries.iterator(); iterator.hasNext();) {
				CsvContact csvLine = new CsvContact( (String[]) iterator.next());
				if(total != 0) {
					String s = "";
					if(csvLine.getType() == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
						if(csvLine.checkStructureNameThunderbird()) {
							if(!csvLine.getString(ThunderbirdConstants.T_DISPLAY_NAME).equals("")) {
								s = csvLine.getString(ThunderbirdConstants.T_DISPLAY_NAME).trim();
							}
							else {
								s = csvLine.getString(ThunderbirdConstants.T_GIVEN_NAME) + " " + csvLine.getString(ThunderbirdConstants.T_FAMILY_NAME);
							}
						}
						Message msg = mHandler.obtainMessage();
						msg.obj = new MessageObject(-1, s.trim());
						mHandler.sendMessage(msg);
						new ThunderbirdImporter(ctx, csvLine, listOfPossibleEqualItems, new UserSelectedOptions(pickedAccountForNewContact, pickedAccountsForSync, minAddressFieldsToMatch, minFieldsForMatchToSync, matchType)).startImport();
					}
					else {
						if(csvLine.checkStructureNameOutlook()) {
							s = (csvLine.getString(OutlookConstants.O_GIVEN_NAME) + " " 
									+ ( (csvLine.getString(OutlookConstants.O_MIDDLE_NAME).equals("")) ? csvLine.getString(OutlookConstants.O_MIDDLE_NAME) + " " : "") 
									+ csvLine.getString(OutlookConstants.O_FAMILY_NAME) 
									+ ( (csvLine.getString(OutlookConstants.O_SUFFIX).equals("")) ? " " + csvLine.getString(OutlookConstants.O_SUFFIX) : "" )
									);
						}
						Message msg = mHandler.obtainMessage();
						msg.obj = new MessageObject(-1, s.trim());
						mHandler.sendMessage(msg);
						
						new OutlookImporter(ctx, csvLine, listOfPossibleEqualItems, new UserSelectedOptions(pickedAccountForNewContact, pickedAccountsForSync, minAddressFieldsToMatch, minFieldsForMatchToSync, matchType)).startImport();
					}
					// Send message to Progress bar
					Message msg = mHandler.obtainMessage();
					msg.obj = new MessageObject(total, "bla");
					mHandler.sendMessage(msg);
					
				}
				total++;		
			}
		}
	}
}
