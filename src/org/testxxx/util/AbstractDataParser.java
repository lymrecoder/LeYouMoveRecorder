package org.testxxx.util;

import java.util.Hashtable;

public abstract class AbstractDataParser {

	abstract public Hashtable<String, Object> decode(String recvdata);
	
	abstract public String encode(Hashtable<String, Object> senddata);
}
