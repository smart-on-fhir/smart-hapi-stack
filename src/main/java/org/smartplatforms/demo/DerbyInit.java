package org.smartplatforms.demo;

public class DerbyInit {

	public DerbyInit() throws ClassNotFoundException {
		Class.forName("org.apache.derby.jdbc.ClientDriver");
	}
	
}
