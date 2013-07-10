package bp.iemanager.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


import au.com.bytecode.opencsv.CSVWriter;

/**
 * Class representing all neccessary atributes for writing CsvContact to file.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class MyFileWriter {
	/*
	 * File where we will write CsvContact.
	 */
	private File file;
	/*
	 * Charset encoding of file.
	 */
	private Charset charset;
	
	public MyFileWriter(File f, Charset ch) {
		setFile(f);
		charset = ch;
	}
	
	/**
	 * Method writes one row to file.
	 * @param list - list of items of CsvContact.
	 */
	public void writeStrings(String[] list) {
		try {
			CSVWriter writer = new CSVWriter(new OutputStreamWriter(
													new FileOutputStream(file, true),
													charset),
											',');		     
			writer.writeNext(list);
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public File getFile() {
		return file;
	}
	
	public boolean createNewFile() {
		try {
			if(!file.exists()) {
				file.createNewFile();
				return true;
			}
			else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	private void setFile(File _file) {
		file = _file;
	}
	
	public void copyRawFileToNewFile(InputStream raw) {
		try {
			/**
			CSVReader reader = null;
			
			reader = new CSVReader(new InputStreamReader(raw, charset));
			writeStrings(reader.readNext());
			reader.close();**/
			
			OutputStream out = new FileOutputStream(file);
			
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = raw.read(buf)) >= 0) {
		    	out.write(buf, 0, len);
			}
			
		    out.close();
			raw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}	
}
