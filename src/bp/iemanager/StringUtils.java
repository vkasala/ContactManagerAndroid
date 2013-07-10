package bp.iemanager;


/**
 * Method is used from following URL. It is also extended from slovak and czech diacritic marks.
 * http://www.rgagnon.com/javadetails/java-0456.html
 * @author  http://www.rgagnon.com/javadetails/java-0456.html
 *
 */
public class StringUtils {
	  private StringUtils() {}

	  private static final String PLAIN_ASCII =
		      "AaEeIiOoUu"    // grave
		    + "AaEeIiOoUuYy"  // acute
		    + "AaEeIiOoUuYy"  // circumflex
		    + "AaOoNn"        // tilde
		    + "AaEeIiOoUuYy"  // umlaut
		    + "AaUu"            // ring
		    + "Cc"            // cedilla
		    + "OoUu"          // double acute
		    + "CcDdEeLlNnSsTtZzRr"		      // Caron makcen
		    ;

	private static final String UNICODE =
		     "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
		    + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
		    + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
		    + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
		    + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
		    + "\u00C5\u00E5\u016E\u016F"
		    + "\u00C7\u00E7"
		    + "\u0150\u0151\u0170\u0171"
		    + "\u010C\u010D\u010E\u010F\u011A\u011B\u013D\u013E\u0147\u0148\u0160\u0161\u0164\u0165\u017D\u017E\u0158\u0159"
		    ;
	  
	  private static final String UPPERCASE_ASCII =
	    "AEIOU" // grave
	    + "AEIOUY" // acute
	    + "AEIOUY" // circumflex
	    + "AON" // tilde
	    + "AEIOUY" // umlaut
	    + "A" // ring
	    + "C" // cedilla
	    + "OU" // double acute
	    ;

	  private static final String UPPERCASE_UNICODE =
	    "\u00C0\u00C8\u00CC\u00D2\u00D9"
	    + "\u00C1\u00C9\u00CD\u00D3\u00DA\u00DD"
	    + "\u00C2\u00CA\u00CE\u00D4\u00DB\u0176"
	    + "\u00C3\u00D5\u00D1"
	    + "\u00C4\u00CB\u00CF\u00D6\u00DC\u0178"
	    + "\u00C5"
	    + "\u00C7"
	    + "\u0150\u0170"
	    ;
	  
	    // remove accentued from a string and replace with ascii equivalent
	    public static String convertNonAscii(String s) {
	       if (s == null) return null;
	       StringBuilder sb = new StringBuilder();
	       int n = s.length();
	       for (int i = 0; i < n; i++) {
	          char c = s.charAt(i);
	          int pos = UNICODE.indexOf(c);
	          if (pos > -1) {
	              sb.append(PLAIN_ASCII.charAt(pos));
	          }
	          else {
	              sb.append(c);
	          }
	       }
	       return sb.toString();
	    }
	  
	  public static String toUpperCaseSansAccent(String txt) {
	       if (txt == null) {
	          return null;
	       }
	       String txtUpper = txt.toUpperCase();
	       StringBuilder sb = new StringBuilder();
	       int n = txtUpper.length();
	       for (int i = 0; i < n; i++) {
	          char c = txtUpper.charAt(i);
	          int pos = UPPERCASE_UNICODE.indexOf(c);
	          if (pos > -1){
	            sb.append(UPPERCASE_ASCII.charAt(pos));
	          }
	          else {
	            sb.append(c);
	          }
	       }
	       return sb.toString();
	  }
	}
