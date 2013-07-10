package bp.iemanager.exporter;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;

import bp.iemanager.MessageObject;

import sk.kasala.viliam.bakalarka.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.provider.ContactsContract.RawContacts;
import android.text.format.Time;

import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

/**
 * Class represents activity for exporting contacts.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class ExporterActivity extends Activity {
    
	private static final String INIT_FOLDER ="IE Manager" + java.io.File.separator;
	
	/**
	 * Attribute for building new name of Thunderbird export file
	 */
	private static final String FILE_LABEL_THD = "Thd";
	/**
	 * Attribute for building new name of Outlook export file
	 */
	private static final String FILE_LABEL_OUTLOOK = "Otk";
	
	private static final int THUNDERBIRD_TYPE = 0;
	private static final int OUTLOOK_TYPE = 1;
	
	private static final int DEFAULT_EXPORT_TYPE  = THUNDERBIRD_TYPE;
	
	/**
	 * Constant attributes for displaying different options for user
	 */
	private static final String[] exportTypes = new String[] {"Thunderbird CSV", "Outlook CSV"};
	private static final String[] options = new String[] {"Phone", "Email", "Address", "WebPage", "Im", "Note", "NickName",  "Organization"};
		
	// Dialog constant indentifiers
	private static final int DIALOG_PICK_EXPORT_TYPE = 1;
	private static final int DIALOG_PICK_ACCOUNTS = 2;
	private static final int DIALOG_PICK_OPTIONS = 3;
	private static final int DIALOG_PROGRESS = 4;
	private static final int DIALOG_REALLY_IMPORT = 5;
	private static final int DIALOG_PICK_CHARSET_TYPE = 6;
	
	/** GUI Components references */
	private Button okButton;
	private Button backButton;
	private TextView accoutsPickTextView;
	private TextView accountsLabelTextView;
	private TextView exportTypeTextView;
	private TextView exportEncodingTypeTextView;
	
	private TextView exportTypeLabelTextView;
	private TextView optionsTypeTextView;
	private TextView optionsLabelTextView;
	
	private EditText fileNameEditText;
	private TextView dirTextView;
	private CheckBox removeAccentsCheckBox;
	private CheckBox onlyFieldsWhichFitsAccentsCheckBox;
	
	/*
	 * Thread realizing all exporting options
	 */
	private ProgressThread progressThread;
	
	/*
	 * User selected charset - Default CHARSET UTF-8
	 */
	private Charset pickedCharset = Charset.forName("UTF-8"); 
	
	private ProgressDialog progressDialog;
	
	
	private String selection = "";
	private String[] selectionArgs = null;
	private int maxProgress;
	
	/*
	 *  Attributes saving picked Options by USER 
	 */
	private HashSet<Account> pickedAccounts;
	private int pickedExportType = DEFAULT_EXPORT_TYPE;
	/*
	 * Default options for Exported items with each CSV item
	 */
	private boolean[] checkedOptionsItems = {true, true, true, true, true,true,true,true};
	
	/*
	 * Flag which determines if  
	 */
	private boolean removeAccentsFlag = false;
	
	/*
	 * Flag which determines if  
	 */
	private boolean fitFields = false;
	
	private String fileName = "";
		
	
	/*
	 * Called when the activity is first created. 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_layout);

        okButton = (Button) findViewById(R.id.export_Start_button);
        backButton = (Button) findViewById(R.id.export_back_button);
        
        accoutsPickTextView = (TextView) findViewById(R.id.export_accounts_textView3);
        accountsLabelTextView = (TextView) findViewById(R.id.export_accounts_label_textView2);
        
        exportTypeTextView = (TextView) findViewById(R.id.export_type_textView5);
        exportTypeLabelTextView = (TextView) findViewById(R.id.export_type_label_textView4);
        
        optionsTypeTextView = (TextView) findViewById(R.id.export_options_textView7);
        optionsLabelTextView = (TextView) findViewById(R.id.export_options_label_textView6);
        
        fileNameEditText = (EditText) findViewById(R.id.file_name_edit_text);
        
        dirTextView = (TextView) findViewById(R.id.export_dir_textView);
        dirTextView.setText("/sdcard/\n" + INIT_FOLDER);
       
        exportEncodingTypeTextView = (TextView) findViewById(R.id.export_encoding_type_textview);

        removeAccentsCheckBox = (CheckBox) findViewById(R.id.export_checkBox);
        
        onlyFieldsWhichFitsAccentsCheckBox = (CheckBox) findViewById(R.id.export_only_which_fits);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        	
        // Register the GUI event handlers
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	onStartExportButtonClicked();
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        
        accoutsPickTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_ACCOUNTS);
			}
		});
        accountsLabelTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_ACCOUNTS);
			}
		});
        
        exportTypeTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_EXPORT_TYPE);
			}
		});
        
        exportTypeLabelTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_EXPORT_TYPE);
			}
		});
        
        optionsLabelTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_OPTIONS);
			}
		});
        
        optionsTypeTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_OPTIONS);
			}
		});
        
        removeAccentsCheckBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				removeAccentsFlag = ((CheckBox) v).isChecked(); 
				makeToast(String.valueOf(((CheckBox) v).isChecked()), Toast.LENGTH_SHORT);
			}
		});
        
        exportEncodingTypeTextView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PICK_CHARSET_TYPE);
				
			}
		});
        
        onlyFieldsWhichFitsAccentsCheckBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fitFields = ((CheckBox) v).isChecked(); 
				makeToast(String.valueOf(((CheckBox) v).isChecked()), Toast.LENGTH_SHORT);
			}
		});
        
        inicializeAttributes();  
    	populateExportType();
    	populateOptions();
    	populateAccounts();
    	populateEncodingType();
    }
    
    /**
     * Method inicialize all non-inicialized attributes.
     */
    protected final void inicializeAttributes() {
    	pickedAccounts = new HashSet<Account>();
    	setMaxProgress(0);
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DIALOG_PICK_EXPORT_TYPE: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.export_type_label);
				builder.setItems( exportTypes, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int which) {
			            pickedExportType = which;
			            populateExportType();
			        }
			    });
			    return builder.create();
			}
			case DIALOG_PICK_ACCOUNTS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.import_synchronize_account);
				AccountManager manager = AccountManager.get(this);
			    
				final Account[] accounts = manager.getAccounts();
			    			    
			    final String[] namesOfAccounts = new String[accounts.length];
			    boolean[] checkedItems = new boolean[accounts.length];
			    
			    for (int i = 0; i < accounts.length; i++) {
					namesOfAccounts[i] = accounts[i].name + "\n" + accounts[i].type;
					checkedItems[i] = false;
				}
					
			    builder.setMultiChoiceItems(namesOfAccounts, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						if(isChecked) {
							pickedAccounts.add(accounts[which]);
						}
						else {
							pickedAccounts.remove(accounts[which]);
						}
					}
				});
			    
			    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						populateAccounts();
					}
				});
				return builder.create();  
			}
			case DIALOG_PICK_OPTIONS: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Fields to Export")
				.setMultiChoiceItems(options, checkedOptionsItems, new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						return;
					}
				 })
			    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						populateOptions();
					}
				});
			    return builder.create();  
			}
			case DIALOG_REALLY_IMPORT: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Do you really want to Export contacts?");
				builder.setCancelable(false);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showDialog(DIALOG_PROGRESS);
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
				progressDialog.setMessage("Exporting Contacts...");
				progressDialog.setCancelable(false);
				return progressDialog;
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
    
    @Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch(id) {
			// Start background exporting contacts.
			case DIALOG_PROGRESS: 
				progressDialog.setProgress(0);
				progressDialog.setMessage("Exporting...");
				progressDialog.setMax(getMaxProgress());
				progressThread.start();
				break;
		}	
	}
    
    /**
     * Set maximum value of progressBar.
     * @param max maximum value which should be set.
     */
    private void setMaxProgress(int max) {
		maxProgress = max;
	}
	
    /**
     * Get value of maxProgress.
     * @return the obtained value
     */
	private int getMaxProgress() {
		return maxProgress;
	}	
    
	/**
	 * Method creates a string of selected accounts and displays it in appropriate TextView.
	 */
    private void populateAccounts() {
		if(pickedAccounts.isEmpty()) {
			accoutsPickTextView.setText(R.string.empty_label);
			return;
		}
		accoutsPickTextView.setText("");
		int i = 1;
		for (Iterator<Account> iterator = pickedAccounts.iterator(); iterator.hasNext();) {
			Account account = (Account) iterator.next();
			accoutsPickTextView.append(" " + i++ + "." + account.name + "\n\t " + account.type + "\n-\t\t\t-\t\t\t-\t\t\t-\t\t\t-\t\t\t-");
			if(iterator.hasNext())
				accoutsPickTextView.append("\n");
		}
		return;
	}
    
    /**
     * Populate export type textView.
     */
    private void populateExportType() {
    	exportTypeTextView.setText("  " + exportTypes[pickedExportType]);
    	createNewFileName();
    }
     
    /**
     * Populate Options TextView with picked options.
     */
    private void populateOptions() {
    	optionsTypeTextView.setText("");
    	int k = 0;
    	for (int i = 0; i < checkedOptionsItems.length; i++) {
			if(checkedOptionsItems[i]) {
				optionsTypeTextView.append(options[i] + ";");
				if(k % 4 == 0 && k != 0)
					optionsTypeTextView.append("\n");
				k++;
			}
		}
    }
        
    /**
     * Sets new file name.
     * @param newFileName the name of the new file name.
     */
    private void setFileName(String newFileName) {
    	fileName = newFileName;
    	populateFileName();
    }
    /**
     * Gets new file name from EditText and sets it into variable reprezenting the reference to the file. 
     */
    private void getFileName() {
    	setFileName(fileNameEditText.getText().toString());
    }
    
    /**
     * Populate EditText with new file name.
     */
    private void populateFileName() {
    	fileNameEditText.setText(fileName);
    }
    
    /**
     * Creates new file from actual time with pattern:
     * Outlook/Thunderbird_export_YYYY_M_D_M.csv and set new file name.
     */
    private void createNewFileName() {
    	// Get actual time
    	Time now = new Time(Time.getCurrentTimezone());
    	now.setToNow();
    	// Create unique file name		
    	setFileName(((pickedExportType == THUNDERBIRD_TYPE) ? FILE_LABEL_THD : FILE_LABEL_OUTLOOK) + "_export"  + "_" + (now.year) + "_" + (now.month + 1) + "_"  + now.monthDay + "_"  + now.minute + ".csv"); 
    }
     
    /**
     * Checks if all mandatory parameters have been set and according to this
     * parameters make actions. If parameters weren't set correctly, then
     * display suitable message, otherwise show dialog for exporting.
     */
    protected void onStartExportButtonClicked() {
    	if(pickedAccounts.isEmpty()) {
    		makeToast("Please choose some accounts!", Toast.LENGTH_SHORT);
    		return;
    	}
    	getFileName();
    	MyFileWriter fileWriter = new MyFileWriter(new File(Environment.getExternalStorageDirectory().getPath() + java.io.File.separator + INIT_FOLDER + fileName), pickedCharset);
    	if(!fileWriter.createNewFile()) {
    		makeToast("Please rename export file!", Toast.LENGTH_LONG);
    		return;
    	}
    	makeSelectionParameters(pickedAccounts);
    	progressThread = new ProgressThread(handler, this, fileWriter);
    	showDialog(DIALOG_REALLY_IMPORT);
    }

    /**
     * Creates new string arguments for query from the picked accounts and
     * set them to the to attributes of this class selectionArgs, selection.
     * @param synchronizeAccounts selected set of accounts, from which the selection arguments should be constructed 
     */
	protected void makeSelectionParameters(HashSet<Account> synchronizeAccounts) {
		selection = " ";
		int i = 0, j = 0;
		selectionArgs = new String[synchronizeAccounts.size()*2];
		
		for (Iterator<Account> iterator = synchronizeAccounts.iterator(); iterator
				.hasNext();) {
			Account account = (Account) iterator.next();
			selectionArgs[j++] = account.name;
			selectionArgs[j++] = account.type;
			
			if(i == 0)
				selection +=  " ( " + RawContacts.ACCOUNT_NAME + " = ? AND " + RawContacts.ACCOUNT_TYPE + " = ? ) ";
			else 
				selection +=   " OR "+ " ( " + RawContacts.ACCOUNT_NAME + " = ? AND " + RawContacts.ACCOUNT_TYPE + " = ? ) ";
			i++;
		}
		return;
	}
	
	/**
	 * Method showing Toast message on Screen with this message
	 * @param msg - message that prints on screen
	 */
	protected void makeToast(String msg) {
		makeToast(msg, Toast.LENGTH_SHORT);
	}
	
	/**
     * Populate Encoding type TextView with picked encoding type.
     */
    private void populateEncodingType() {
    	exportEncodingTypeTextView.setText(pickedCharset.name());
    }
	
	/**
	 * Method showing Toast message on Screen with this message
	 * @param msg - message that prints on screen
	 * @param length - toast length
	 */
	protected void makeToast(String msg, int length) {
		Toast.makeText(this, msg, length).show();
	}
	
	/**
	 * Object that handles ProgressDialog. In right time 
	 * it closes the ProgressDialog.
	 */
	public final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			MessageObject obj =(MessageObject) msg.obj;
			if(!obj.getName().equals("UNIQUE^@&!EXIT_MEASSAGE") &&  obj.getTotal() != -1) {
				message = obj.getName();
				progressDialog.setProgress(obj.getTotal());
				runOnUiThread(changeMessage);
				if(obj.getTotal() >= getMaxProgress() ) {
					dismissDialog(DIALOG_PROGRESS);
					makeToast("Contacts Exported!", Toast.LENGTH_LONG);
				}
			}
		}
	};
	
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
	 * 
	 * Nested class exports contacts and handle ProgressDialog in different thread.
	 */
	private class ProgressThread extends Thread {
		private MyFileWriter fileWriter;
		private Context ctx;
		private OutlookExporter expOutlook;
		private ThunderbirdExporter expThunderbird;
		
		public ProgressThread(Handler _h, Context _ctx, MyFileWriter _fileWriter) {
			ctx = _ctx;
			fileWriter = _fileWriter;
			if(pickedExportType == THUNDERBIRD_TYPE) {
	    		expThunderbird = new ThunderbirdExporter(fileWriter, ctx, selection, selectionArgs, checkedOptionsItems, _h, removeAccentsFlag, fitFields);
	    		setMaxProgress(expThunderbird.getExportedContactNumber());
			}
	    	else if(pickedExportType == OUTLOOK_TYPE) {
	    		expOutlook = new OutlookExporter(fileWriter, ctx, selection, selectionArgs, checkedOptionsItems, _h, removeAccentsFlag, fitFields);
	    		setMaxProgress(expOutlook.getExportedContactNumber());
	    	}
		}

		public void run() {
			if(pickedExportType == THUNDERBIRD_TYPE)
	    		expThunderbird.startExport();
	    	else if(pickedExportType == OUTLOOK_TYPE)
	    		expOutlook.startExport();	
		}
	}
	
	
}