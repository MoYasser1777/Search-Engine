package engine;


import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class utils {
    public static void main (String  args[]){
        String input = "Hello, World!!!!!";
        String hashedString = hashString(input);
        System.out.println("Hashed string: " + hashedString);
    }

    public static String normalizeURL(String urlString) {
        try {
            urlString = urlString.trim();
            urlString = lowercaseHostName(urlString);
            urlString = replaceProtocolToLowercase(urlString);
            urlString = decodePercentEncodedTriplets(urlString);
            urlString = removeDefaultPort(urlString);
            urlString = removeDotSegments(urlString);

            return urlString;
        } catch (Exception e) {
            // Handle any exceptions
//            e.printStackTrace();
            return null;
        }
    }
    public static String replaceProtocolToLowercase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        Pattern pattern = Pattern.compile("^(.*?:)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String matchedString = matcher.group();
            return input.replace(matchedString, matchedString.toLowerCase());
        }
        return input;
    }
    public static String lowercaseHostName(String urlString) {
        try {
            URL url = new URL(urlString);
            return urlString.replaceFirst(url.getHost(), url.getHost().toLowerCase());
        } catch (MalformedURLException e) {
//            System.out.println(urlString);
//            e.printStackTrace();
            return null;
        }
    }
    private static String replaceEncodedOctets(String urlString) {
        Pattern pattern = Pattern.compile("%[0-9A-Fa-f]{2}");
        Matcher matcher = pattern.matcher(urlString);
        while (matcher.find()) {
            String octet = matcher.group();
            String decoded = octet.toUpperCase();
            urlString = urlString.replace(octet, decoded);
        }
        return urlString;
    }

    private static String removeDefaultPort(String urlString) {
        Pattern pattern = Pattern.compile("(:80|:443|:\\d+)");
        Matcher matcher = pattern.matcher(urlString);
        if (matcher.find()) {
            String port = matcher.group();
            urlString = urlString.replace(port, "");
        }
        return urlString;
    }

    public static String removeDotSegments(String input) {
        // Pattern to match and remove dot-segments
        String dotSegmentsPattern = "/\\./|/[^/]+/\\.\\./|/\\.$|^\\.$";

        // Remove dot-segments from the input string
        Pattern pattern = Pattern.compile(dotSegmentsPattern);
        Matcher matcher = pattern.matcher(input);

        return matcher.replaceAll("/");
    }


    private static String removeFragment(String urlString) {
        return urlString.split("#")[0];
    }

    private static String removeQuery(String urlString) {
        return urlString.split("\\?")[0];
    }
    public static String decodePercentEncodedTriplets(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        try {
            // Decode percent-encoded triplets
            String decodedInput = URLDecoder.decode(input, StandardCharsets.UTF_8.toString());

            // Decode specific unreserved characters
            Pattern pattern = Pattern.compile("%[A-Za-z0-9]{2}");
            Matcher matcher = pattern.matcher(decodedInput);
            while (matcher.find()) {
                String triplet = matcher.group();
                String decodedTriplet = URLDecoder.decode(triplet, StandardCharsets.UTF_8.toString());
                decodedInput = decodedInput.replace(triplet, decodedTriplet);
            }

            return decodedInput;
        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
            return input;
        }
    }
    public static String hashString(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        }catch (NoSuchAlgorithmException e){
            return null;
        }
    }


}


