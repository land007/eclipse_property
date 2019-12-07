

import com.google.api.GoogleAPI;
import com.google.api.GoogleAPIException;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

public class Main {

	/**
	 * @param args
	 * @throws GoogleAPIException 
	 */
	public static void main(String[] args) throws GoogleAPIException {
		// Set the HTTP referrer to your website address.
	    GoogleAPI.setHttpReferrer("http://jiayq007.appspot.com/test"/* Enter the URL of your site here */);

	    // Set the Google Translate API key
	    // See: http://code.google.com/apis/language/translate/v2/getting_started.html
	    GoogleAPI.setKey("AIzaSyDfkN-DqqhoHtqGZy-N565kAquv5QN-1jc"/* Enter your API key here */);

//	    String translatedText = Translate.DEFAULT.execute("Bonjour le monde", Language.FRENCH, Language.ENGLISH);
//	    System.out.println(translatedText);
	    
	    String tran = Translate.DEFAULT.execute("Bonjour le monde", Language.CHINESE, Language.ENGLISH);
	    System.out.println(tran);

	}

}
