package org.smartplatforms.demo.interceptor;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scope {


	public final Context context; // patient | user | system
	public final String resource; // Alert | AllergyIntolerance | ... | ValueSet | *
	public final boolean read; 
	public final boolean write;
	public final boolean valid;
	
	private static final Set<String> readScopes = new HashSet<String>(Arrays.asList(
		     new String[] {"read", "*"}
		));
	

	private static final Set<String> writeScopes = new HashSet<String>(Arrays.asList(
		     new String[] {"read", "*"}
		));
	
	static Scope parse(String wireFormat) {
		return new Scope(wireFormat);
	}
	
	private Scope (String wireFormat) {

		Pattern pattern = Pattern.compile("(user|patient|system)/(.*?)\\.(read|write|\\*)");
		Matcher matcher = pattern.matcher(wireFormat);
		if (!matcher.matches()) {
			valid = false;
			read = false;
			write = false;
			resource = null;
			context = null;
			return;
		}
		
		context = Context.valueOf(matcher.group(1));
		resource = matcher.group(2);
		read = (readScopes.contains(matcher.group(3)));
		write = (writeScopes.contains(matcher.group(3)));	
		valid = true;
	}
	
	public enum Context {
		PATIENT("patient"), USER("user"), SYSTEM("system");
		private String value;

		private Context(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

}
