package com.fitnexus.server.util;

import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.repository.publicuser.PublicUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

import static com.fitnexus.server.constant.FitNexusConstants.PatternConstants.TIME_ZONE;
import static org.springframework.security.oauth2.common.exceptions.OAuth2Exception.INVALID_TOKEN;



@Slf4j
@RequiredArgsConstructor
@Component
public class CustomGenerator {

    @Value("${base_mail_url}")
    private String baseUrl;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${spring.mvc.servlet.path}")
    private String servletPath;

    public static double round(Double value, int places) {
        if (value == null) value = 0.0;
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return this function will return a five digit random number as String
     */
    public static String generateOTP() {
        Random r = new Random();
        int low = 1000;
        int high = 10000;
        int code = r.nextInt(high - low) + low;
        return String.valueOf(code);
    }

    /**
     * This function can use to generate a JsonObject from a given JWT string.
     *
     * @param jwtToken the JWT which needs to parse
     * @return JsonObject of the token is valid and can be parsed.
     * @throws CustomServiceException if the token is not valid.
     */
    public static JSONObject getJsonObjectFromJwt(String jwtToken) {
        try {
            log.info("\nGet JSON from JWT : ");

            jwtToken = jwtToken.split(" ")[1];
//        ------------ Decode JWT ------------
            String[] split_string = jwtToken.split("\\.");
            String base64EncodedHeader = split_string[0];
            String base64EncodedBody = split_string[1];

//      ~~~~~~~~~ JWT Header ~~~~~~~
            Base64 base64Url = new Base64(true);

//      ~~~~~~~~~ JWT Body ~~~~~~~~~
            String body = new String(base64Url.decode(base64EncodedBody));

            JSONObject jsonObject = new JSONObject(body);
            log.info("\nGot JSON from JWT : {}", jsonObject.getString("user_name"));
            return jsonObject;
        } catch (IndexOutOfBoundsException | IllegalArgumentException |
                IllegalStateException | JSONException | NullPointerException n) {
            // token is invalid or user is not found if hits here.
            log.error("Failed to get JSON from JWT: {}\tError: {} ", jwtToken, n);
            throw new CustomServiceException(400, INVALID_TOKEN);
        }
    }

    /**
     * This function can use to generate a json header from a given JWT string.
     *
     * @param jwtToken the JWT which needs to parse
     * @return JsonObject header of the token is valid and can be parsed.
     * @throws CustomServiceException if the token is not valid.
     */
    public static JSONObject getJsonHeaderJwt(String jwtToken) {
        try {
            log.info("\nToken check header");

            jwtToken = jwtToken.split(" ")[1];
//        ------------ Decode JWT ------------
            String[] split_string = jwtToken.split("\\.");
            String base64EncodedHeader = split_string[0];

            log.info("token header : " + base64EncodedHeader);
//      ~~~~~~~~~ JWT Header ~~~~~~~
            Base64 base64Url = new Base64(true);

//      ~~~~~~~~~ JWT Body ~~~~~~~~~
            String body = new String(base64Url.decode(base64EncodedHeader));
            log.info("token string header : " + body);

            return new JSONObject(body);
        } catch (IndexOutOfBoundsException | IllegalArgumentException |
                IllegalStateException | JSONException | NullPointerException n) {
            // token is invalid if hits here.
            log.error("Failed to get header from JWT: {}\tError: {} ", jwtToken, n);
            throw new CustomServiceException(400, INVALID_TOKEN);
        }
    }

    /**
     * This function can use to get the time difference of end time and start time in long
     *
     * @param startDate start date time
     * @param endDate   end date time
     * @return the difference in a long value
     */
    public static long getDateTimeDifferenceInHours(Date startDate, Date endDate) {
        // d1, d2 are dates
        long diff = endDate.getTime() - startDate.getTime();

        return diff / (60 * 60 * 1000);
    }

    /**
     * This function can use to get the time difference of end time and start time in long in minutes
     *
     * @param startDate start date time
     * @param endDate   end date time
     * @return the difference in a long value
     */
    public static String getTimeDifferenceInHoursAndMinutes(Date startDate, Date endDate) {
        // d1, d2 are dates
        long diff = endDate.getTime() - startDate.getTime();

        long hourDiff = diff / (60 * 60 * 1000);
        long minutesDiff = (diff / (60 * 1000) % 60) % 60;
        return hourDiff + ":" + minutesDiff;
    }

    public static double getFormattedDouble(double amount) {
        return Double.parseDouble(new DecimalFormat().format(amount));
    }

    public String getPageUrlWithToken(String endPointPage, String token) {
        String confirmationUrl = endPointPage + "?token=" + token;
        return baseUrl + servletPath + confirmationUrl;
    }

    public static String generatePassword() {
        int length = 10;
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        char[] password = new char[length];

        password[0] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
        password[1] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
        password[2] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        password[3] = numbers.charAt(random.nextInt(numbers.length()));

        for (int i = 4; i < length; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }
        return String.valueOf(password);
    }

    public String generateInviteCode(String name, PublicUserRepository userRepository) {
        if (name == null) return null;
        int minLength = 4;
        if (name.length() < minLength) {
            for (int i = 0; i < minLength; i++) {
                if (name.length() == i) name = name.concat(getAlphaNumericString(minLength - i));
            }
        }
        String formattedName = name.trim().toLowerCase().replaceAll("\\s+", "").substring(0, 4);
        String username = formattedName + String.format("%04d", new Random().nextInt(10000));
        return userRepository.findByReferralCode(username).isPresent() ? generateInviteCode(name, userRepository) : username;
    }

    public static LocalDateTime getCurrentDateTimeForQuery() {
        return LocalDateTime.now().minusHours(6);
    }

    public static String getDateTimeToGivenZone(LocalDateTime dateTime, String timeZone) {
        try {
            log.info("\nConverting time to zone: {}", timeZone);
            LocalDateTime newDateTime = getDateTimeByZone(dateTime, timeZone);
            return newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")) + (timeZone != null ? " (" + timeZone + ")" : "(GMT)");
        } catch (Exception e) {
            log.error("\nFailed to convert time zone: {}", e);
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
        }
    }

    public static LocalDateTime getDateTimeByZone(LocalDateTime dateTime, String timeZone) {
        ZoneId oldZone = ZoneId.of(TIME_ZONE);
        ZoneId newZone = ZoneId.of(timeZone != null ? timeZone : TIME_ZONE);
        return dateTime.atZone(oldZone).withZoneSameInstant(newZone).toLocalDateTime();
    }

    private static String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
