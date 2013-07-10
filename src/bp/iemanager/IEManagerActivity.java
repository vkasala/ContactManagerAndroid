package bp.iemanager;

import java.io.File;

import bp.iemanager.exporter.ExporterActivity;
import bp.iemanager.importer.ImporterActivity;

import sk.kasala.viliam.bakalarka.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

/**
 * Main activity of application, where user can choose whether he wants to 
 * import or export contact.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class IEManagerActivity extends Activity {
	private static final String INIT_FOLDER ="IE Manager" + java.io.File.separator;
	
	/*
	 * DIALOG ID		
	 */
	private static final int DIALOG_NO_SDCARD = 1;
	
	/**
	 * References on buttons
	 */
	private Button exportButton;
	private Button importButton;
	private Button exitButton;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		exportButton = (Button) findViewById(R.id.main_export_button);
		importButton = (Button) findViewById(R.id.main_import_button);
		exitButton = (Button) findViewById(R.id.main_exit_button);
		
		exportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onExportButtonClicked();
			}
		});
		
		importButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onImportButtonClicked();
			}
		});
		
		exitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		if(createInitFolderIfNotExists(INIT_FOLDER) == false) {
			showDialog(DIALOG_NO_SDCARD);
		}		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_NO_SDCARD: {
				Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("SD Card isn't mounted!");
				builder.setMessage("Exit application!");
				builder.setCancelable(false);
				builder.setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				return builder.create();
			}
			default:
				return null;
		}
	}
	
    /**
     * Method creates new default folder for application on SD Card, if it doesn't exist.
     * @param initFolder - name of init folder.
     * @return treu/false
     */
    protected boolean createInitFolderIfNotExists(String initFolder) {
    	File file = new File(Environment.getExternalStorageDirectory().getPath() + java.io.File.separator + initFolder);
    	//Log.v(TAG, Environment.getExternalStorageDirectory().getPath() + "/" + initFolder + "/");
    	    	
    	if(file.exists() == false) {
    		if(file.mkdirs() == false) {
    			return false;
    		}
    		else {
    			//Log.v(TAG, "Podarilo sa vytvorit subor");
    			return true;
    		}
    	}
    	else {
    		return true;
    	}
    }
	
    /**
     * Method runs new Activity for importing contacts.
     */
	protected void onImportButtonClicked() {
		Intent i = new Intent( IEManagerActivity.this, ImporterActivity.class);
        startActivity(i);
	}
	
	/**
	 * Method runs new Activity for exporting contacts.
	 */
	protected void onExportButtonClicked() {
		Intent i = new Intent( IEManagerActivity.this, ExporterActivity.class);
        startActivity(i);
	}
}
