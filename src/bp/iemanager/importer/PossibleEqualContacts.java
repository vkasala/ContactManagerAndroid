package bp.iemanager.importer;

import java.io.Serializable;
import java.util.ArrayList;

import bp.iemanager.csvcontact.CsvContact;
import bp.iemanager.csvcontact.ThunderbirdConstants;
import bp.iemanager.exporter.OutlookExporter;
import bp.iemanager.exporter.ThunderbirdExporter;

import android.content.Context;

/**
 * Class representing pair of possible equal contacts.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class PossibleEqualContacts implements Serializable {
	/**
	 * Generated serial version ID due to interface Serializable
	 */
	private static final long serialVersionUID = -47653413677706605L;
	/*
	 * ID of Raw Contact from Android System.
	 */
	protected long rawContactID;
	
	/*
	 * CSV Contact from file.
	 */
	protected CsvContact fileContact;
		
	public PossibleEqualContacts(long rawId, CsvContact which) {
		rawContactID = rawId;
		fileContact = which;
	}
		
	public CsvContact getFromContact() {
		return fileContact;
	}
	
	public long getRawContactID() {
		return rawContactID;
	}
	public CsvContact getCsvLineFromRawContactID(Context context) {
		if(fileContact.getType() == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
			ThunderbirdExporter exp = new ThunderbirdExporter(context, new boolean[]{true, true,true,true,true,true,true,true}, true);
			exp.queryAllDetailedInformation(rawContactID);
			return exp.getFirstCsvLine();
		}
		else {
			OutlookExporter exp = new OutlookExporter(context, new boolean[]{true, true,true,true,true,true,true,true}, true);
			exp.queryAllDetailedInformation(rawContactID);
			return exp.getFirstCsvLine();
		}
	}
	
	public ArrayList<CsvContact> getListOfCsvLinesFromRawContactID(Context context) {
		if(fileContact.getType() == ThunderbirdConstants.THUDERBIRD_ITEM_COUNT) {
			ThunderbirdExporter exp = new ThunderbirdExporter(context, new boolean[]{true, true,true,true,true,true,true,true}, true);
			exp.queryAllDetailedInformation(rawContactID);
			return exp.getArrayList();
		}
		else {
			OutlookExporter exp = new OutlookExporter(context, new boolean[]{true, true,true,true,true,true,true,true}, true);
			exp.queryAllDetailedInformation(rawContactID);
			return exp.getArrayList();
		}
	}
	

}
