package com.fitnexus.server.config.security.custom;

import com.fitnexus.server.exception.CustomAccessDeniedException;
import com.fitnexus.server.util.CustomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import static com.fitnexus.server.constant.FitzkyConstants.ErrorConstants.FORBIDDEN_RESOURCE;


@Slf4j
public class CustomUserAuthenticator {

    /**
     * This can use to validate the given user id is belong to the given token.
     * @param id the user's id to check
     * @param jwtToken the user's access token.
     * @throws CustomAccessDeniedException if not authorized by any means.
     */
    public static void checkPublicUserIdWithToken(long id, String jwtToken) {
        try {
            log.info("\nChecking id with token: {}", id);
            if (id != getPublicUserFromToken(jwtToken).getLong("id")) throw new CustomAccessDeniedException(403, FORBIDDEN_RESOURCE);
            log.info("\nuser id matches with id: {}", id);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("\nUnauthorized token: {}\n", e.getMessage());
            throw new CustomAccessDeniedException(403, FORBIDDEN_RESOURCE);
        }
    }

    /**
     * This can use to validate the given user mobile is belong to the given token.
     * @param mobile the user's mobile to check
     * @param jwtToken the user's access token.
     * @throws CustomAccessDeniedException if not authorized by any means.
     */
    public static void checkPublicUserMobileWithToken(String mobile, String jwtToken) {
        try {
            log.info("\nChecking mobile with token: {}", mobile);
            if (!mobile.equals(getPublicUserFromToken(jwtToken).getString("mobile"))) throw new CustomAccessDeniedException(403, FORBIDDEN_RESOURCE);
            log.info("\nuser id matches with mobile: {}", mobile);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("\nUnauthorized token: {}\n", e.getMessage());
            throw new CustomAccessDeniedException(403, FORBIDDEN_RESOURCE);
        }
    }

    /**
     * this can use to get public user id from th given JWT.
     * @param jwtToken the user token
     * @return the user's id
     */
    public static long getPublicUserIdFromToken(String jwtToken) {
        return getPublicUserFromToken(jwtToken).getLong("id");
    }

    public static String getPublicUserMobileFromToken(String jwtToken) {
        return getPublicUserFromToken(jwtToken).getString("mobile");
    }

    private static JSONObject getPublicUserFromToken(String jwtToken) {
        JSONObject tokenJson = CustomGenerator.getJsonObjectFromJwt(jwtToken);
        JSONObject tokenUserJson = tokenJson.getJSONObject("user");
        return tokenUserJson.getJSONObject("userDetails");
    }

    /**
     * this can use to get auth user id from th given JWT.
     * @param jwtToken the user token
     * @return the user's id
     */
    public static long getAuthUserIdFromToken(String jwtToken) {
        JSONObject tokenJson = CustomGenerator.getJsonObjectFromJwt(jwtToken);
        JSONObject tokenUserJson = tokenJson.getJSONObject("user");
        return tokenUserJson.getLong("userId");
    }
}
