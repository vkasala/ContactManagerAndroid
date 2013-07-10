package bp.iemanager.importer;

import bp.iemanager.StringUtils;


/**
 * Class compares pair of two arrays of string. The result of each pair is saved into array of fields, 
 * which reflect each pair of strings with the same index. We use widespread set of boolean values such as 
 * EQUAL , EMPTY, NONEQUAL, OMIT, LOAD. 
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class MoreFieldsComparator {
		public static int FLAG_CHANGE_CASE = 1;
		public static int FLAG_PICK_STRING_WITH_MORE_ACCENT_CHARS = 2;
		private String[] androidArray;
		private String[] fileArray;
		private MatchValues[] matchedFields;
			
		private int EQ_count;
		private int NEQ_count;
		@SuppressWarnings("unused")
		private int LOAD_count;
		@SuppressWarnings("unused")
		private int OMIT_count;
		@SuppressWarnings("unused")
		private int EMPTY_count;
		
		protected int mandatoryEQ;
		
		
		protected MatchType type;
						
		public MoreFieldsComparator(String[] _androidAddress, String[] _csvAddress, MatchType _type) {
			androidArray = _androidAddress;
			fileArray = _csvAddress;
			matchedFields = new MatchValues[_androidAddress.length];
			type = _type;
			eraseCounts();
						
			removeWhiteSpacesFromArray(androidArray);
			removeWhiteSpacesFromArray(fileArray);
			
			compareStrings();
		}
		private void eraseCounts() {
			EQ_count = 0;
			NEQ_count  = 0;
			LOAD_count  = 0;
			OMIT_count  = 0;
			EMPTY_count = 0;
		}
		
		public void compareStrings() {
			eraseCounts();
			for (int i = 0; i < androidArray.length; i++) {
				// SCHEME: ANDROID ADDRESS is EMPTY String and CSV ADDRESS is EMPTY String
				if(androidArray[i].equals("") && fileArray[i].equals("")) {
					matchedFields[i] = MatchValues.EMPTY;
				}
				// Scheme: EMPTY and NOT-EMPTY and NOT-EQUAL
				else if(!androidArray[i].equals("") && fileArray[i].equals("")) {
					matchedFields[i] = MatchValues.OMIT;
				}
				// Scheme: NOT-EMPTY and EMPTY and NOT-EQUAL
				else if(androidArray[i].equals("") && !fileArray[i].equals("")) {
					matchedFields[i] = MatchValues.LOAD;
				}
				// Scheme: ANDROID ADDRESS == CSV ADDRESS
				else 
					switch (type) {
						case CASE_SENSITIVE:
							if(androidArray[i].equals(fileArray[i]))
								matchedFields[i] = MatchValues.EQ;
							else 
								matchedFields[i] = MatchValues.NEQ;
							break;
						case IGNORE_CASE:
							if(androidArray[i].equalsIgnoreCase(fileArray[i]))
								matchedFields[i] = MatchValues.EQ;
							else 
								matchedFields[i] = MatchValues.NEQ;
							break;
						case IGNORE_ACCENTS_CASE_SENSITIVE:
							if(removeAccents(fileArray[i]).equals(removeAccents(androidArray[i])))
								matchedFields[i] = MatchValues.EQ;
							else 
								matchedFields[i] = MatchValues.NEQ;
							break;
						case IGNORE_ACCENTS_AND_CASES:
							if(removeAccents(fileArray[i]).equalsIgnoreCase(removeAccents(androidArray[i])))
								matchedFields[i] = MatchValues.EQ;
							else 
								matchedFields[i] = MatchValues.NEQ;
							break;
					}
			}
			computePercentage();
		}
		
		protected void computePercentage() {
			eraseCounts();
			for (int i = 0; i < matchedFields.length; i++) {
				checkField(matchedFields[i]);
			}
		}
		
		protected void checkField(MatchValues val) {
			switch(val) {
				case NEQ:
					NEQ_count++;
					break;
				case EQ:
					EQ_count++;
					break;
				case OMIT:
					OMIT_count++;
					break;
				case LOAD:
					LOAD_count++;
					break;
				case EMPTY:
					EMPTY_count++;
					break;
			}
		}
		
				
		public boolean areOrganisationsEqual() {
			// Return result for Organisation : Company, Job Title 
			if(androidArray.length == 2) {
				// At least one pair is Non-equal
				if(EQ_count > 1) {
					return true;
				}
				// Other combination of values are permitted
				else 
					return false;
			}
			else return false;
		}
		
		/**
		 * Method checks mandatory pair fields flags:  
		 * if they aren't NON EQUAL (return false) 
		 * if at least one mandatory pair flag is EQUAL (return true)
		 * @param fields
		 * @return
		 */
		private boolean checkEqualMandatoryFields(boolean[] fields) {
			boolean atLeastOneIsMandatoryIsEqual = false;
			mandatoryEQ = 0;
			for(int i = 0; i < fields.length; i++) {
				if(fields[i]) {
					switch (matchedFields[i]) {
						case NEQ:
							return false;
						case EQ:
							atLeastOneIsMandatoryIsEqual = true;
							mandatoryEQ++;
							break;
						default:
							break;
					}
				}
			}
			return atLeastOneIsMandatoryIsEqual;
		}
		
		private void swap(int i, int k) {
			String s1 = fileArray[i];
			fileArray[i] = fileArray[k];
			fileArray[k] = s1;
		}
		
		public boolean areAddressesEqual(boolean[] fields) {
			if(androidArray.length == 6) {
				// All fields are EQUAL
				if(NEQ_count > 0) {
					// All fields are NON EQAUL create new address in contact
					if(NEQ_count == 6)
						return false;
					// NO EQUAL with combinantion of other like empty,
					if(EQ_count == 0) {
						return false;
					}
					
					// Other options are only EQ with NON_EQUAL and Others
					// If all mandatory fields are not NON_EQUAL and at least one mandatory pair flag is EQ
					if(checkEqualMandatoryFields(fields)) {
						
						int countNEQ = getCountOfFalseFields(fields);
						int countMinEqualMandatory = (fields.length-countNEQ);
						// If all mandatory fields are not NON_EQUAL and all mandatory pair flags are EQUAL
						// others doesnt't matter
						if(mandatoryEQ == countMinEqualMandatory) {
							return true;
						}
						/** If at least one mandatory fields is EQ
						// and all non-mandatory are NEQ_EQUAL return false
						if((NEQ_count == countNEQ)) {
							return false;
						}
						// If at least one mandatory fields is EQ return false
						// All must be the same
						else */
						  return false;
					}
					else 
						return false;
					
				}
				// At least one pair is EQUAL and we can build new string
				else if(EQ_count > 0) {
					
					return true;
				}
				// No EQUAL and NONEQUAL FLAGS. It means there are only combinations of OMIT, LOAD, EMPTY
				else {
					// All can't be EMPTY, LOAD, OMIT
					// EMPTY + OMITS won't be
					return true;
				}
			}
			return false;
		}
		
		protected int getCountOfFalseFields(boolean[] fields) {
			int j = 0;
			for (int i = 0; i < fields.length; i++) {
				if(!fields[i]) {
					j++;
				}
			}
			return j;
		}
		
		public boolean areNamesEqual(boolean[] fields) {
			if(androidArray.length == 5) {
				if(checkEqualMandatoryFields(fields)) {
					return true;				
				}
				else {
					swap(1,3);
					compareStrings();
					if(checkEqualMandatoryFields(fields))
					{
						return true;
					}
					else 
						return false;
				}
				
			}
			return false;
			}
		
		public String[] createNewSyncData() {
			return createNewAddress(0, 0);
		}
		
		public String[] createNewAddress(int flag, int flag_pick_accent) {
			String[] newAddress = new String[matchedFields.length];
			for (int i = 0; i < matchedFields.length; i++) {
				MatchValues val = matchedFields[i];
				switch(val) {
					case NEQ:
						//System.out.print("NEQ\t");
						if(flag == FLAG_CHANGE_CASE)
							newAddress[i] = fileArray[i].substring(0,1).toUpperCase()+ fileArray[i].substring(1).toLowerCase();
						else 
							newAddress[i] = fileArray[i];
						break;
					case EQ:
						//System.out.print("EQ\t");
						if(flag_pick_accent == FLAG_PICK_STRING_WITH_MORE_ACCENT_CHARS) {
							if(fileArray[i].equalsIgnoreCase(androidArray[i])) {
								if(flag == FLAG_CHANGE_CASE)
									newAddress[i] = fileArray[i].substring(0,1).toUpperCase()+ fileArray[i].substring(1).toLowerCase();
								else 
									newAddress[i] = fileArray[i];
								break;
							}
							String s = fileArray[i].substring(0,1).toUpperCase() + fileArray[i].substring(1).toLowerCase();
							String a = androidArray[i].substring(0,1).toUpperCase() + androidArray[i].substring(1).toLowerCase();
							int before = countDifferentCharacter(s.toCharArray(), a.toCharArray());
							int csv = countDifferentCharacter(toApperCaseAccents(s).toCharArray(), a.toCharArray());
							int android = countDifferentCharacter(s.toCharArray(), toApperCaseAccents(a).toCharArray());
							System.out.println(before + " " + csv + " " + android + "\t");
							if(csv > android)
								if(flag == FLAG_CHANGE_CASE)
									newAddress[i] = a;
								else
									newAddress[i] = androidArray[i];
							else 
								if(flag == FLAG_CHANGE_CASE)
									newAddress[i] = s;
								else
									newAddress[i] = fileArray[i];
						}
						else {
							newAddress[i] = fileArray[i];
						}
						break;
					case LOAD:
						//System.out.print("LOAD\t");
						if(flag == FLAG_CHANGE_CASE)
							newAddress[i] = fileArray[i].substring(0,1).toUpperCase() + fileArray[i].substring(1).toLowerCase();
						else 
							newAddress[i] = fileArray[i];
						break;
					case EMPTY:
						//System.out.print("EMPTY\t");
						newAddress[i] = fileArray[i];
						break;
					case OMIT:
						//System.out.print("OMIT\t");
						if(flag == FLAG_CHANGE_CASE)
							newAddress[i] = androidArray[i].substring(0,1).toUpperCase() + androidArray[i].substring(1).toLowerCase();
						else
							newAddress[i] = androidArray[i];
						break;	
				}
			}
			//System.out.println();
			return newAddress;
		}
		
		/**
		 * Funkcia vymaze z pola retazcov biele znaky na zaciatku a na konci retazca.
		 * @param array - pole 
		 */
		private void removeWhiteSpacesFromArray(String[] array) {
			for (int i = 0; i < array.length; i++) {
				array[i] = array[i].trim();
				if(array[i].matches("\\s+")) {
					array[i] = "";
				}	
			} 
		}
		
		public static int countDifferentCharacter(char[] s1, char[] s2) {
			int j = 0;
			
			for (int i = 0; i < s1.length; i++) {
				if(s1[i] != s2[i])
					j++;
			}
			return j;
		}
	
	public static String removeAccents(String s) {
		return StringUtils.convertNonAscii(s);
	}
		
	public static String toApperCaseAccents(String string) {
		return StringUtils.toUpperCaseSansAccent(string);
	}

}