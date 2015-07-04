package com.wifiafterconnect.html;

import java.util.List;

import com.wifiafterconnect.html.JavaScript.Token;
import com.wifiafterconnect.JavaScriptTests.JavaScriptTester;


public class SimpleJSTester implements JavaScriptTester {

	@Override
	public JavaScript getJS() {
		return new JavaScript ("function(){ var a_123 = 10;\n /*   blah\"*/\n}", true);
	}

	@Override
	public boolean getGoodTokens(List<Token> tokensClean, int testNo) {
		int t = 0;
		if (testNo == t++) {
			tokensClean.add(new JavaScript.Identifier("function"));
			tokensClean.add(new JavaScript.Punctuator("("));
			tokensClean.add(new JavaScript.Punctuator(")"));
			return true;
		}else if (testNo == t++) {
			tokensClean.add(new JavaScript.Identifier("function"));
			tokensClean.add(new JavaScript.Punctuator("("));
			tokensClean.add(new JavaScript.Punctuator(")"));
			tokensClean.add(new JavaScript.Punctuator("{"));
			tokensClean.add(new JavaScript.Identifier("var"));
			return true;
		}else if (testNo == t++) {
			tokensClean.add(new JavaScript.Identifier("a_123"));
			tokensClean.add(new JavaScript.Punctuator("="));
			return true;
		}else if (testNo == t++) {
			tokensClean.add(new JavaScript.Identifier("function"));
			tokensClean.add(new JavaScript.Punctuator("("));
			tokensClean.add(new JavaScript.Punctuator(")"));
			tokensClean.add(new JavaScript.Punctuator("{"));
			tokensClean.add(new JavaScript.Identifier("var"));
			tokensClean.add(new JavaScript.Identifier("a_123"));
			tokensClean.add(new JavaScript.Punctuator("="));
			tokensClean.add(new JavaScript.Number("10"));
			tokensClean.add(new JavaScript.Punctuator(";"));
			tokensClean.add(new JavaScript.LineTerminator());
			tokensClean.add(new JavaScript.LineTerminator());
			tokensClean.add(new JavaScript.Punctuator("}"));
			return true;
		}else if (testNo == t) {
			tokensClean.add(new JavaScript.Punctuator("{"));
			tokensClean.add(new JavaScript.Identifier("var"));
			tokensClean.add(new JavaScript.Identifier("a_123"));
			tokensClean.add(new JavaScript.Punctuator("="));
			tokensClean.add(new JavaScript.Number("10"));
			tokensClean.add(new JavaScript.Punctuator(";"));
			tokensClean.add(new JavaScript.Punctuator("}"));
			return true;
		}
		return false;
	}

	@Override
	public boolean getBadTokens(List<Token> tokensClean, int testNo) {
		int t = 0;
		if (testNo == t++) {
			tokensClean.add(new JavaScript.Identifier("function"));
			tokensClean.add(new JavaScript.Punctuator("{"));
			return true;
		}else if (testNo == t++) {
			tokensClean.add(new JavaScript.Identifier("a_123"));
			tokensClean.add(new JavaScript.Punctuator("="));
			tokensClean.add(new JavaScript.Number("10"));
			tokensClean.add(new JavaScript.Punctuator("}"));
			return true;
		}else if (testNo == t++) {
			tokensClean.add(new JavaScript.Identifier("a_12"));
			tokensClean.add(new JavaScript.Punctuator("="));
			return true;
		}else if (testNo == t) {
			tokensClean.add(new JavaScript.Identifier("a_123"));
			tokensClean.add(new JavaScript.Number("10"));
			return true;
		}
		return false;
	}

}
