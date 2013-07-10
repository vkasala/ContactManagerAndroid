package bp.iemanager;

/**
 * Class encapsulate attributes for updating ProgressDialog. 
 * Especially number and name.
 * @author Viliam Kasala, <xkasal01@stud.fit.vutbr.cz>
 *
 */

public class MessageObject {
	/**
	 * Number of imported/exported contact.
	 */
	private int total;
	/*
	 * Name of imported/exported contact.
	 */
	private String name;
	
	public MessageObject(int _total, String _name) {
		setTotal(_total);
		setName(_name);
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
