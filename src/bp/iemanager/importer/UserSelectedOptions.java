package bp.iemanager.importer;

import java.util.HashSet;

import android.accounts.Account;

/**
 * Class encapsulating some user selected options into one Object.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */
public class UserSelectedOptions {
	public Account newContactType;
	public HashSet<Account> chooseAccounts;
	public boolean[] minMatchAddressFieldds;
	public boolean[] minMatchContactFields;
	public MatchType type;
	
	public UserSelectedOptions(Account _newContactType, 
			HashSet<Account> _chooseAccounts, 
			boolean[] _minMatchAddressFieldds, 
			boolean[] _minMatchContactFields, MatchType _type) {
		chooseAccounts = _chooseAccounts;
		newContactType = _newContactType;
		minMatchAddressFieldds = _minMatchAddressFieldds;
		minMatchContactFields = _minMatchContactFields;
		type = _type;
	}
}
