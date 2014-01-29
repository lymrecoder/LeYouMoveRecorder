package org.testxxx.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public abstract class AbstractComm {
	
	abstract public String sendMessage(String MsgContent) throws UnsupportedEncodingException, IOException;
	
}
