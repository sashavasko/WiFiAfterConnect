/*
 * Copyright (C) 2013 Sasha Vasko <sasha at aftercode dot net> 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wifiafterconnect.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

import com.wifiafterconnect.Constants;

import android.util.Log;

public class JavaScript {

	private boolean enableDebugOuput = false;
	private String src;
	private String script = null;
	private List<Token> tokens;
	private List<Token> tokensClean;
	@SuppressWarnings("unused") // TODO :
	private Map<String, String> functions = new HashMap<String,String>();// bodies of the functions
	@SuppressWarnings("unused") // TODO :
	private List<String> jqueries = new ArrayList<String>(); // top level JQueries without leading $

	public static boolean isEOLChar(int c) {
		return c == '\n' || c == '\r' || c == 2028 || c == 2029;
	}
	
	/*
	 * JavaScript could be Unicode/UTF-8, hence the mess with CodePoints
	 */
	
	public static abstract class Token {
		protected int start;  //index
		protected int startCP;  //codepoints
		protected int count;  	//codepoints
		protected String token = " ";
		
		public Token (int start, int startCodePoints) {
			this.start = start;
			this.startCP = startCodePoints;
			this.count = 0;
		}

		public Token (String token) {
			this.start = 0;
			this.token = token;
			this.count = token.codePointCount(0, token.length());
		}
		/* 
		 * length may differ, 
		 * for example whitespace, comments and eol's all have effective length of 1
		 * so this return count of codepoints in the source
		 */
		public int getCodePointsCount() {
			return count;
		}
		
		protected void saveToken (String source) {
			if (count > 0)
				token = source.substring(start, source.offsetByCodePoints(start, count));
		}
		
		public int parse(String source, int codePointCount) {
			int maxPos = codePointCount - startCP;
			
			while (count < maxPos && checkChar(source.codePointAt(startCP + count))) ++count;

			if (count == 0) 
				return start;

			saveToken(source);
			return source.offsetByCodePoints(start, count);
		}
		
		public abstract boolean checkChar(int c);
		
		@Override
		public String toString() {
			return token;
		}

		public String toDiagString() {
			return "Token class ["+getClass().getSimpleName() + "]{ start = " + start + "; count = " + count + "; token = [" + toString() + "]}";
		}

		@Override
		public boolean equals(Object o) {
			
			return o.getClass().equals(this.getClass()) && this.toString().equals(((Token)o).toString());
		}
	}
	
	public static class WhiteSpace extends Token {

		public WhiteSpace(int start, int startCodePoints) {
			super(start, startCodePoints);
		}
		
		public WhiteSpace() {
			super(" ");
		}

		@Override
		protected void saveToken (String source) {}

		@Override
		public boolean checkChar(int c) {
			return !isEOLChar(c) && Character.isWhitespace(c);
		}
	} 
	public static class LineTerminator  extends Token {

		public LineTerminator(int start, int startCodePoints) {
			super(start, startCodePoints);
		}

		public LineTerminator() {
			super(" ");
		}
		
		@Override
		protected void saveToken (String source) {}

		@Override
		public boolean checkChar(int c) {
			return isEOLChar(c);
		}
	}
	public static class Comment extends Token {
		
		private boolean multiline = false;
		private int maybeEnd = 0;

		public Comment(int start, int startCodePoints) {
			super(start, startCodePoints);
		}
		
		public Comment() {
			super(" ");
		}
		
		@Override
		protected void saveToken (String source) {}

		@Override
		public boolean checkChar(int c) {
			switch (count) {
				case 0 : 	return c== '/';
				case 1 : 	multiline = (c == '*');
							return c== '/' || c == '*';
			}
			if (!multiline)
				return !isEOLChar(c);
			switch (maybeEnd) {
				case 0: maybeEnd = (c == '*') ? 1 : 0; break;
				case 1: maybeEnd = (c == '/') ? 2 : 0; break;
				case 2: return false;
			}
			return true;
		}
	}
	public static class StringLiteral extends Token {
		
		private int startChar = ' ';
		private boolean escaped = false;
		private boolean complete = false;

		public StringLiteral(int start, int startCodePoints) {
			super(start, startCodePoints);
		}
		
		public StringLiteral(String sl) {
			super(sl);
			startChar = sl.codePointAt(0);
			complete = sl.codePointBefore(sl.length()) == startChar;
		}


		@Override
		public boolean checkChar(int c) {
			if (complete || isEOLChar(c))
				return false;
			if (startChar == ' '){
				startChar = c;
				return c == '\'' || c == '"';
			}
			if (!escaped && c == startChar) {
				return (complete = true);
			}
			escaped = (!escaped && c == '\\');
			return true;
		}
	}

	public static class Number extends Token {

		private boolean isHex = false;
		private boolean hasDecimalPoint = false;
		
		public Number(int start, int startCodePoints) {
			super(start, startCodePoints);
		}

		public Number(String token) {
			super(token);
			isHex = (token.startsWith ("0x") || token.startsWith ("0X")); 
			hasDecimalPoint = (token.indexOf('.') >= 0);
		}

		@Override
		public boolean checkChar(int c) {
			switch (count) {
				case 0 : isHex = (c == '0'); break;
				case 1 : 
					if (c == 'x' || c == 'X') return isHex;
					else isHex = false; 
			}
			if (c== '.') {
				if (hasDecimalPoint) return false;
				return (hasDecimalPoint = true);
			}
			// TODO need to handle exponent somehow
			return Character.isDigit(c) || (isHex && "abcdefABCDEF".indexOf(c) >= 0);
		}
	}

	public static class RegularExpression extends Token {

		private boolean escaped = false;
		private int complete = 0;

		public RegularExpression(int start, int startCodePoints) {
			super(start, startCodePoints);
		}

		public RegularExpression(String token) {
			super(token);
			complete = 1;
		}

		@Override
		public boolean checkChar(int c) {
			if (complete > 0 || isEOLChar(c))
				return false;
			if (count == 0)
				return c == '/';

			if (!escaped) {
				if (c == ']' && complete < 0) {
					++complete;
				}else if (c == '[') {
					--complete;
				}else if (c == '/' && complete == 0) {
					++complete;
				}else 
					escaped = c == '\\';
			}else 
				escaped = false;
			return true;
		}
	} 
	public static class Identifier extends Token {

		public Identifier(int start, int startCodePoints) {
			super(start, startCodePoints);
		}

		public Identifier(String token) {
			super(token);
		}

		@Override
		public boolean checkChar(int c) {
			if (count == 0) 
				return Character.isJavaIdentifierStart(c);
			else
				return Character.isJavaIdentifierPart(c);
		}
	} 
	

	public static final String PUNCTUATORS_SINGLE_STR = "><&|+-=!.;(),{}:[]?*/%^~";
	public static final String PUNCTUATORS_SINGLE[] = {">","<","&","|","+","-","=","!",".",";","(",")",",","{","}",":","[","]","?","*","/","%","^","~"};
	public static final String PUNCTUATORS_DOUBLE[] = {	">>","<<","==","!=","+=",">=","<=","-=","|=","*=","/=","&=","^=","%=", "&&", "||","++","--"};
	public static final String PUNCTUATORS_TRIPPLE[] = {">>=", "<<=", "===", "!==",">>>"};
	public static final String PUNCTUATORS_QUAD[] = {">>>="};	
	
	public static boolean isPunct (int c) {
		return PUNCTUATORS_SINGLE_STR.indexOf(c) >= 0;
	}
	
	public static class Punctuator extends Token {
		int singleIdx = -1;
		int doubleIdx = -1;
		int trippleIdx = -1;
		int quadIdx = -1;
		
		public Punctuator(int start, int startCodePoints) {
			super(start, startCodePoints);
		}

		public Punctuator(String token) {
			super(token);
			int maxCount = token.codePointCount(0, token.length());
			for (count = 0 ; count < maxCount ; ++count)
				checkChar (token.codePointAt(count));
		}

		public Punctuator(char c) {
			super(Character.toString(c));
			singleIdx = PUNCTUATORS_SINGLE_STR.indexOf(c);
			count = 1;
		}

		@Override
		protected void saveToken (String source) {}

		@Override
		public boolean checkChar(int c) {
			
			switch (count) {
				case 0 : return (singleIdx = PUNCTUATORS_SINGLE_STR.indexOf(c)) >= 0;
				case 1 : 
					if (singleIdx <= 5 || c == '=') {
						char c1 =  PUNCTUATORS_SINGLE_STR.charAt(singleIdx);
						for (int i = 0 ; i < PUNCTUATORS_DOUBLE.length ; ++i){
							if (PUNCTUATORS_DOUBLE[i].charAt(1) == c && 
									PUNCTUATORS_DOUBLE[i].charAt(0) == c1 ){
								doubleIdx = i;
								break;
							}
						}
					}
					return doubleIdx >= 0;
				case 2 : 
					if (c == '>' && doubleIdx == 0)	trippleIdx = 4;
					else if (c == '=' && doubleIdx < 4) trippleIdx = doubleIdx;
					return trippleIdx >= 0;
				case 3 : 
					if (c == '=' && trippleIdx == 4)
						quadIdx = 0;
					return trippleIdx >= 0;
			}
			return false;	
		}

		@Override
		public String toString() {
			if (quadIdx >= 0) return PUNCTUATORS_QUAD[quadIdx];
			if (trippleIdx >= 0) return PUNCTUATORS_TRIPPLE[trippleIdx];
			if (doubleIdx >= 0) return PUNCTUATORS_DOUBLE[doubleIdx];
			if (singleIdx >= 0) return PUNCTUATORS_SINGLE[singleIdx];
			return " ";
		}
		
	}
	
	public JavaScript (Element e){
		src = e.attr("src");
		if (src.isEmpty()) {
			parse (e.data());
		}
	}

	public JavaScript (String script){
		src = "";
		parse (script);
	}

	public JavaScript (String script, boolean enableDebugOuput){
		this.enableDebugOuput = enableDebugOuput;
		src = "";
		parse (script);
	}
	
	private void parse(String data) {
		script = data;
		tokens = tokenize (script);
		tokensClean = clean(tokens);
		if (enableDebugOuput) {
			Log.d(Constants.TAG, tokens.size() + "of parsed tokens: ");
			for (Token t : tokens)
				Log.d(Constants.TAG, t.toDiagString());
			Log.d(Constants.TAG, tokensClean.size() + "of clean tokens: ");
			for (Token t : tokensClean)
				Log.d(Constants.TAG, t.toDiagString());
		}
		extractFunctions();
		extractJQueries();
	}
	
	public static List<Token> clean(List<Token> tokensAll) {
		List<Token> result = new ArrayList <Token>();
		if (tokensAll != null) {
			for (Token t1 : tokensAll) {
				if (t1 instanceof WhiteSpace || t1 instanceof Comment)
					continue;
				result.add(t1);
			}
		}
		return result;
	}
	
	public static List<Token> tokenize(String code) {
		int lengthCP = code.codePointCount(0, code.length());
		int curr = 0;
		int currCP = 0;
		
		List<Token> result = new ArrayList <Token>();
		
		while (currCP < lengthCP) {
			int c1 = code.codePointAt(currCP);
			int c2 = currCP+1 < lengthCP ? code.codePointAt(currCP+1) : 0;
			Token t = null;
			if (isEOLChar (c1))
				t = new LineTerminator(curr, currCP);
			else if (Character.isWhitespace(c1))
				t = new WhiteSpace (curr, currCP);
			else if (c1 == '/' && (c2 == '/' || c2 == '*'))
				t = new Comment(curr, currCP);
			else if (c1 == '\'' || c1 == '"')
				t = new StringLiteral(curr, currCP);
			else if ((c1 >= '0' && c1 <= '9') || (c1 == '.' && c2 >= '0' && c2 <= '9'))
				t = new Number(curr, currCP);
			else if (/*TODO parseRegex &&*/ c1 == '/')
				t = new RegularExpression(curr, currCP);
			else if (isPunct(c1))
				t = new Punctuator(curr, currCP);
			else
				t = new Identifier(curr, currCP);
			
			curr = t.parse(code, lengthCP);
			int count = t.getCodePointsCount();
			if (count == 0) {
				Log.e(Constants.TAG, "unrecognized JavaScript token encountered");
				break;
			}
			
			currCP += count;
			result.add(t);
		}
		
		return result;
	}

	private void extractJQueries() {
		// TODO Auto-generated method stub
		
	}

	private void extractFunctions() {
		// TODO Auto-generated method stub
		
	}


	public String getDocumentReadyFunc() {
			
		return null;
	}
	
	public int matchCode (String code) {
		List<Token> tokenizedCode = clean(tokenize (code));
		return matchCode (tokenizedCode);
	}
	
	public int matchCode (List<Token> tokenizedCode) {
		if (tokensClean == null || tokenizedCode == null)
			return -1;
		for (int i = 0 ; i < tokensClean.size() ; ++i) {
			
			if (tokensClean.get(i) instanceof LineTerminator)
				continue;
			
			int curr_t1 = i;
			int curr_t2 = 0 ;
			boolean match = true;
			
			while (match && curr_t2 < tokenizedCode.size() && curr_t1 < tokensClean.size()) {
				Token t2 = tokenizedCode.get(curr_t2++);
				if (!(t2 instanceof LineTerminator)) { 
					Token t1 = tokensClean.get(curr_t1++);
					if (t1 instanceof LineTerminator){ 
						curr_t2--;
					}else{
						match = t1.equals(t2);
						if (enableDebugOuput && match) {
							Log.d(Constants.TAG, "tok#" + curr_t1 + " " + t1.toDiagString() + " matches tok#" + curr_t2 + " " + t2.toDiagString());
						}
					}
				}
			}
			
			if (match)
				return i;
		}
		
		return -1;
	}
	
	public String findAssignedValue (String varName) {
		List<Token> tokenizedCode = new ArrayList <Token>();
		tokenizedCode.add (new Identifier(varName));
		tokenizedCode.add (new Punctuator("=")); 

		int assignmentIdx = matchCode (tokenizedCode);
		
		// TODO implement proper evaluation of expressions :
		return (assignmentIdx >= 0 && assignmentIdx+2 < tokensClean.size()) ? tokensClean.get(assignmentIdx+2).toString() : "";
	}
		
	public static String scriptFromTokens (List<Token> tokens) {
		StringBuilder sb = new StringBuilder();
		if (tokens != null) {
			for (Token t : tokens) {
				if (t instanceof LineTerminator)
					sb.append('\n');
				else
					sb.append(t.toString()).append(' ');
			}
		}
		return sb.toString();
	}
	
	public String getClean() {
		return scriptFromTokens (tokensClean);
	}

	public void setEnableDebugOuput(boolean enableDebugOuput) {
		this.enableDebugOuput = enableDebugOuput;
	}
	
	private static final Punctuator punctSemicolon = new Punctuator (';');
	private static final Punctuator punctAdd = new Punctuator ('+');
	private static final Punctuator punctAssign = new Punctuator ('=');
	
	public String eval (int idx) {
		String result = "";
		if (tokensClean != null) {
			while (idx  < tokensClean.size()) {
				Token tok = tokensClean.get(idx++);
				Log.d(Constants.TAG, "eval(" + idx + "): tok = " + tok.toString());
				if (tok.equals(punctSemicolon))
					break;
				if (tok instanceof Punctuator && idx < tokensClean.size()) {
					Token tokOp = tokensClean.get(idx++);
					Log.d(Constants.TAG, "eval(" + idx + "): tok = " + tok.toString());
					String strOp = "";
					if (tokOp instanceof StringLiteral) {
						strOp = tokOp.toString();
						strOp = strOp.substring(1, strOp.length()-1);
					}else if (tokOp instanceof Identifier) {
						strOp = evalStringVar(tokOp.toString());
					}
					Log.d(Constants.TAG, "eval(" + idx + "): strOp = " + strOp);
					// only support addition for now
					if (tok.equals(punctAssign))
						result += strOp;
					else if (tok.equals(punctAdd))
						result += strOp;
				}
			}
		}
		return result;
	}

	public String evalStringVar(String varName) {
		List<Token> tokenizedCode = new ArrayList <Token>();
		tokenizedCode.add (new Identifier(varName));
		tokenizedCode.add (new Punctuator("=")); 

		int idx = matchCode (tokenizedCode);
		Log.d(Constants.TAG, "evalStringVar(" + varName + "): idx = " + idx);
		if (idx < 0)
			return "";
		return eval (idx+1);
	}
	
}
