/**
 * 
 */
package com.wifiafterconnect;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

import com.wifiafterconnect.html.JavaScript;
import com.wifiafterconnect.html.SimpleJSTester;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * @author sasha
 *
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class JavaScriptTests extends TestCase {
	public interface JavaScriptTester {
		JavaScript getJS();
		boolean getGoodTokens(List<JavaScript.Token> tokensClean, int testNo);
		boolean getBadTokens(List<JavaScript.Token> tokensClean, int testNo);
	}

	public void executeTester (JavaScriptTester tester) {
		JavaScript js = tester.getJS();
		
		List<JavaScript.Token> tokens = new ArrayList<>();
		for (int i = 0 ; tester.getGoodTokens(tokens, i) ; ++i) {
			if (tokens.size() > 0) {
				Log.d("WifiAfterConnect", "Start good test #" + i);
				String script = JavaScript.scriptFromTokens(tokens);
				assertTrue ("Good Test #" + i + " <script>" + script + "</script>", js.matchCode(tokens)>= 0);
				tokens.clear();
				Log.d("WifiAfterConnect", "Done good test #" + i);
			}
		}
		for (int i = 0 ; tester.getBadTokens(tokens, i) ; ++i) {
			if (tokens.size() > 0) {
				Log.d("WifiAfterConnect", "Start bad test #" + i);
				String script = JavaScript.scriptFromTokens(tokens);
				assertFalse ("Bad Test #" + i + " <script>" + script + "</script>", js.matchCode(tokens)>= 0);
				tokens.clear();
				Log.d("WifiAfterConnect", "Done bad test #" + i);
			}
		}
	}

    @Test
	public void testSimpleJS (){
		executeTester (new SimpleJSTester());
	}
}
