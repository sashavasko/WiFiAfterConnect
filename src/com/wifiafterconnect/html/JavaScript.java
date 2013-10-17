package com.wifiafterconnect.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

public class JavaScript {

	private String src;
	private String script = null;
	private List<String> cleanScript = new ArrayList<String>(); // comments removed, eol removed from inside strings
	private List<String> flatScript = new ArrayList<String>(); // eol removed from inside top level blocks and statements 
	private Map<String, String> functions = new HashMap<String,String>();// bodies of the functions
	private List<String> jqueries = new ArrayList<String>(); // top level JQueries without leading $ 
	
	
	public JavaScript (Element e){
		src = e.attr("src");
		if (src.isEmpty()) {
			parse (e.data());
		}
	}

	private void parse(String data) {
		script = data;
		makeCleanScript ();
		flatten();
		extractFunctions();
		extractJQueries();
	}
	
	private void flatten() {
		// TODO Auto-generated method stub
		
	}

	private void extractJQueries() {
		// TODO Auto-generated method stub
		
	}

	private void extractFunctions() {
		// TODO Auto-generated method stub
		
	}

	public static int skipStringLiteral (final String l, int pos) {
		char stringEnd = l.charAt(pos);
		char lastChar = ' ';
		try {
			for (++pos; l.charAt(pos) != stringEnd; ++pos) {
				lastChar = l.charAt(pos); 
				if (lastChar == '\\') 
					++pos;
			}
		}catch (IndexOutOfBoundsException e)
		{
			return lastChar == '\\' ? 0 : pos;
		}
		return pos;
	}

	private void makeCleanScript () {
		BufferedReader r = new BufferedReader(new StringReader (script));
		cleanScript.clear();
		try {
			String l;
			boolean insideMLComment = false;
			while ((l = r.readLine()) != null) {
				l = l.trim();
				if (insideMLComment) {
					int end = l.indexOf("*/");
					if (end == -1)
						continue;
					l = l.substring(end+2);
					insideMLComment = false;
				}

				int len = l.length();
				int i = 0;
				
				try {
					for(; i < len ; i++) { 
						char c = l.charAt(i);
						if (c == '/' && l.charAt(i+1) == '/') {// end of line comment
							if (i > 0)
								cleanScript.add(l.substring(0, i));
							break;
						}else if (c == '/' && l.charAt(i+1) == '*') {// multi-line comment
							int end = l.indexOf("*/", i+2);
							if (end == -1) {
								insideMLComment = true;
								if (i > 0)
									cleanScript.add(l.substring(0, i));
								break;
							}else {
								if (i > 0)
									l = l.substring(0, i) + l.substring(end+2);
								else 
									l = l.substring(end+2);
								len = l.length();
							}
							break;
						}else if (c == '\'' || c == '\"'){ // string literal
							int end = 0;
							while (end == 0) {
								end = skipStringLiteral (l, i);
								if (end == 0) { // handling multi-line strings
									String nextLine; 
									if ((nextLine = r.readLine()) == null)
										end = len;
									else {
										l = l.substring(0, len-1) + nextLine.trim();
										len = l.length();
									}
								}
							}
							i = end;
						}	
				    }
				}catch (IndexOutOfBoundsException e)
				{
					i = len;
				}
				if ( i == len)
					cleanScript.add(l);
			}
		} catch (IOException e) {
		}
	}

	public String getDocumentReadyFunc() {
		for (String s : cleanScript) {
			//if s.
		}
			
		return null;
	}
		
}
