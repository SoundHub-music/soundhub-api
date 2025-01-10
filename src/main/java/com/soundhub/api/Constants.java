package com.soundhub.api;

public class Constants {
    public static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    public static final String HOST_REGEX = "https?:\\/\\/(?:[a-zA-Z0-9.-]+|\\d{1,3}(?:\\.\\d{1,3}){3})(:\\d+)?(?:\\/[^\\s]*)?";

    public static final String[] ENDPOINT_WHITELIST = {
            "/api/v1/files/**",
            "/api/v1/auth/**",
            "/api/v1/users/checkUser/**",
            "/api/v1/genres",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/ws/**"
    };

    public static final String USER_RESOURCE_NAME = "User";
    public static final String POST_RESOURCE_NAME = "Post";
    public static final String CHAT_RESOURCE_NAME = "Chat";
    public static final String MESSAGE_RESOURCE_NAME = "Message";
    public static final String INVITE_RESOURCE_NAME = "Invite";

    public static final String EMAIL_FIELD = "email";
    public static final String ID_FIELD = "id";

    // error messages
    public static final String USER_EMAIL_EXISTS_MSG = "This email is already taken";
    public static final String INVALID_EMAIL = "Invalid email";
    public static final String USERNAME_NOT_FOUND = "User not found with this username or email: %s";
    public static final String SUCCESSFUL_LOGOUT = "You have successfully logged out";
    public static final String INVALID_EMAIL_PASSWORD = "Invalid email or password";
    public static final String INVALID_MULTIPART = "Invalid multipart file";
    public static final String CREATE_STATIC_DIR_ERROR = "Couldn't create folder %s";
    public static final String SAVE_STATIC_ERROR = "Error during saving file";
    public static final String FILE_NOT_FOUND = "File %s";
    public static final String RESOURCE_NOT_EXIST = "Resource %s not found";
    public static final String PERMISSION_MESSAGE = "You don't have permissions for this operation.";
    public static final String CHAT_DELETE_SUCCESS_MSG = "Chat with id: %s was successfully deleted";
    public static final String CHAT_NOT_CONTAINS_USER = "This user is not related to this chat";
    public static final String MESSAGE_PAGE_NOT_FOUND = "This page is not found. Total page count is %d";
    public static final String UNAUTHORIZED_ERROR_MESSAGE = "Sorry, You're not authorized to access this resource.";
    public static final String INVITE_ALREADY_SENT_MSG = "Invitation for this user has already sent";
    public static final String USER_ALREADY_FRIEND = "This user is already a friend";
    public static final String USER_CANNOT_ADD_HIMSELF = "User can't add himself as a friend!";
    public static final String NO_INVITE_STRATEGY_MESSAGE = "No strategy found for name: %s";

    public static final String DEFAULT_MESSAGE_PAGE = "0";
    public static final String DEFAULT_MESSAGE_PAGE_SIZE = "50";

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String DELETER_ID_HEADER = "DeleterId";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String BLACKLIST_CACHE_NAME = "jwt-blacklist";
    public static final String FILE_PATH_PART = "/api/v1/files/";


    public static final String LOCAL_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    // request part ids
    public static final String USER_DTO_SIGN_UP_ID = "userData";
    public static final String FILE_REQUEST_PART_ID = "files";
    public static final String DELETE_FILE_LIST_REQUEST_PART_ID = "deleteFiles";
}
