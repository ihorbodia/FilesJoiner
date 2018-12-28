package filesjoiner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataHelper {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static final Pattern VALID_WEBSITE_ADDRESS_REGEX
            = Pattern.compile("^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$", Pattern.CASE_INSENSITIVE);


    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean validateURLs(String url) {
        Matcher matcher = VALID_WEBSITE_ADDRESS_REGEX.matcher(url);
        return matcher.find();
    }

}

