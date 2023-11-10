package com.msik404.karmaappgateway.docs;

public class KarmaAppEndpointDocs {

    // OPERATIONS
    public static final String OP_SUM_FIND_PAGINATED_POSTS = "Get key-set (karma_score, post_id) paginated posts.";

    private static final String POST_PAGINATION_SUFFIX = """
             If two posts karma_scores are the same, post with greater post_id is first. If pagination is not set top
            posts are returned.
            """;

    private static final String OP_BASE_DESC_FIND_PAGINATED_POSTS = OP_SUM_FIND_PAGINATED_POSTS + POST_PAGINATION_SUFFIX;

    private static final String GUEST_ENDPOINT_SUFFIX = "User does not need to be logged-in to use this endpoint.";

    private static final String GUEST_ENDPOINT_POST_VISIBILITY_SUFFIX = " Active posts can be returned.";

    public static final String OP_DESC_FIND_PAGINATED_POSTS = OP_BASE_DESC_FIND_PAGINATED_POSTS + GUEST_ENDPOINT_SUFFIX +
            GUEST_ENDPOINT_POST_VISIBILITY_SUFFIX;

    private static final String MOD_ENDPOINT_SUFFIX = " This endpoint requires at least mod privilege.";

    private static final String MOD_ENDPOINT_POST_VISIBILITY_SUFFIX = " Active and hidden posts can be returned.";

    public static final String OP_SUM_FIND_PAGINATED_POSTS_FOR_MOD = OP_SUM_FIND_PAGINATED_POSTS + MOD_ENDPOINT_SUFFIX;

    public static final String OP_FIND_PAGINATED_POSTS_FOR_MOD = OP_SUM_FIND_PAGINATED_POSTS_FOR_MOD +
            MOD_ENDPOINT_POST_VISIBILITY_SUFFIX;

    private static final String ADMIN_ENDPOINT_SUFFIX = " This endpoint requires at least admin privilege.";

    private static final String ADMIN_ENDPOINT_POST_VISIBILITY_SUFFIX = " Active, hidden and deleted posts can be returned.";

    public static final String OP_SUM_FIND_PAGINATED_POSTS_FOR_ADMIN = OP_SUM_FIND_PAGINATED_POSTS +
            ADMIN_ENDPOINT_SUFFIX;

    public static final String OP_DESC_FIND_PAGINATED_POSTS_FOR_ADMIN = OP_SUM_FIND_PAGINATED_POSTS_FOR_ADMIN +
            ADMIN_ENDPOINT_POST_VISIBILITY_SUFFIX;

    public static final String OP_SUM_FIND_PAGINATED_OWNED_POSTS = OP_SUM_FIND_PAGINATED_POSTS +
            " Each post was created by currently logged-in user.";

    public static final String OP_DESC_FIND_PAGINATED_OWNED_POSTS = OP_SUM_FIND_PAGINATED_OWNED_POSTS +
            POST_PAGINATION_SUFFIX;

    public static final String OP_SUM_FIND_PAGINATED_POSTS_RATINGS =
            "Get key-set (karma_score, post_id) paginated posts ratings of currently logged-in user.";

    private static final String OP_BASE_DESC_FIND_PERSONAL_POSTS_RATINGS = OP_SUM_FIND_PAGINATED_POSTS_RATINGS + """
            Post ratings can be returned in the same way as posts. This endpoint can be used with endpoints for
            returning paginated posts, to show to the user which posts have been rated by him and in what way (positive/
            negative).
            """;

    private static final String POSTS_RATINGS_PAGINATION_SUFFIX = """
             If two posts karma_scores are the same, post rating of post with greater post_id is first. If pagination
            is not set post ratings of top posts are returned.
            """;

    public static final String OP_DESC_FIND_PERSONAL_POST_RATINGS = OP_BASE_DESC_FIND_PERSONAL_POSTS_RATINGS +
            POSTS_RATINGS_PAGINATION_SUFFIX;


    public static final String OP_SUM_FIND_PERSONAL_POSTS_RATINGS_FOR_MOD = OP_SUM_FIND_PAGINATED_POSTS_RATINGS +
            MOD_ENDPOINT_SUFFIX;

    private static final String MOD_ENDPOINT_POSTS_RATINGS_VISIBILITY_SUFFIX =
            " Posts ratings of active and hidden posts can be returned.";

    public static final String OP_DESC_FIND_PERSONAL_POST_RATINGS_FOR_MOD = OP_SUM_FIND_PERSONAL_POSTS_RATINGS_FOR_MOD +
            MOD_ENDPOINT_POSTS_RATINGS_VISIBILITY_SUFFIX;

    public static final String OP_SUM_FIND_PERSONAL_POST_RATINGS_FOR_ADMIN = OP_SUM_FIND_PAGINATED_POSTS_RATINGS +
            ADMIN_ENDPOINT_SUFFIX;

    private static final String ADMIN_ENDPOINT_POSTS_RATINGS_VISIBILITY_SUFFIX =
            " Posts ratings of active, hidden and deleted posts can be returned.";

    public static final String OP_DESC_FIND_PERSONAL_POST_RATINGS_FOR_ADMIN = OP_SUM_FIND_PERSONAL_POST_RATINGS_FOR_ADMIN +
            ADMIN_ENDPOINT_POSTS_RATINGS_VISIBILITY_SUFFIX;

    public static final String OP_SUM_FIND_IMAGE_BY_ID = "Get image by post id.";

    public static final String OP_DESC_FIND_IMAGE_BY_ID = OP_SUM_FIND_IMAGE_BY_ID + GUEST_ENDPOINT_SUFFIX;

    public static final String OP_SUM_CREATE_POST = "Create new post.";

    private static final String USER_ENDPOINT_SUFFIX = " User needs to be logged-in.";

    public static final String OP_DESC_CREATE_POST = OP_SUM_CREATE_POST + USER_ENDPOINT_SUFFIX;

    public static final String OP_SUM_RATE_POST = "Rate existing post.";

    private static final String IDEMPOTENT_OPERATION_SUFFIX = """ 
             This operation is idempotent. When client performs it for the first time it is persisted. Performing it
            consecutively for the second time does not do anything.
            """;

    public static final String OP_DESC_RATE_POST = OP_SUM_RATE_POST + USER_ENDPOINT_SUFFIX + IDEMPOTENT_OPERATION_SUFFIX + """
             User can change his rating decision from positive to negative or the other way around, and post's score
            will get updated accordingly.
            """;

    public static final String OP_SUM_UNRATE_POST = "Unrate existing post.";

    public static final String OP_DESC_UNRATE_POST = OP_SUM_UNRATE_POST + USER_ENDPOINT_SUFFIX + IDEMPOTENT_OPERATION_SUFFIX +
            " If post was not rated in the first place, this operation does not do anything.";

    public static final String OP_SUM_HIDE_POST_BY_USER =
            "Change owned post's visibility from active to hidden.";

    public static final String OP_DESC_HIDE_POST_BY_USER = OP_SUM_HIDE_POST_BY_USER + USER_ENDPOINT_SUFFIX +
            IDEMPOTENT_OPERATION_SUFFIX;

    public static final String OP_SUM_UNHIDE_POST_BY_USER = "Change owned post's visibility from hidden to active";

    public static final String OP_DESC_UNHIDE_POST_BY_USER = OP_SUM_UNHIDE_POST_BY_USER + USER_ENDPOINT_SUFFIX +
            IDEMPOTENT_OPERATION_SUFFIX;

    public static final String OP_SUM_HIDE_POST_BY_MOD = "Change post's visibility from active to hidden.";

    public static final String OP_DESC_HIDE_POST_BY_MOD = OP_SUM_HIDE_POST_BY_MOD + USER_ENDPOINT_SUFFIX +
            IDEMPOTENT_OPERATION_SUFFIX;

    public static final String OP_SUM_DELETE_POST_BY_USER = "Change owned post's visibility from any to deleted.";

    public static final String OP_DESC_DELETE_POST_BY_USER = OP_SUM_DELETE_POST_BY_USER + USER_ENDPOINT_SUFFIX +
            IDEMPOTENT_OPERATION_SUFFIX;

    public static final String OP_SUM_DELETE_POST_BY_ADMIN = "Change post's visibility from any to deleted.";

    public static final String OP_DESC_DELETE_POST_BY_ADMIN = OP_SUM_DELETE_POST_BY_ADMIN + ADMIN_ENDPOINT_SUFFIX +
            IDEMPOTENT_OPERATION_SUFFIX;

    public static final String OP_SUM_ACTIVATE_POST_BY_ADMIN = "Change post's visibility from any to active.";

    public static final String OP_DESC_ACTIVE_POST_BY_ADMIN = OP_SUM_ACTIVATE_POST_BY_ADMIN + ADMIN_ENDPOINT_SUFFIX +
            IDEMPOTENT_OPERATION_SUFFIX;

    public static final String OP_SUM_UPDATE_WITH_USER_PRIVILEGE = "Update user account of currently logged-in user.";

    public static final String OP_DESC_UPDATE_WITH_USER_PRIVILEGE = OP_SUM_UPDATE_WITH_USER_PRIVILEGE + """
             Only sub-set of all fields can be updated with this endpoint.
            These fields contain every field which is safe to be changed by regular user.
            """;

    public static final String OP_SUM_UPDATE_WITH_ADMIN_PRIVILEGE = "Update user account with admin privilege.";

    public static final String OP_DESC_UPDATE_WITH_ADMIN_PRIVILEGE = OP_SUM_UPDATE_WITH_ADMIN_PRIVILEGE +
            ADMIN_ENDPOINT_SUFFIX + " All users accounts and all fields can be modified with this endpoint.";

    public static final String OP_SUM_REGISTER = "Create new user account.";

    public static final String OP_DESC_REGISTER = OP_SUM_REGISTER;

    public static final String OP_SUM_LOGIN = "Login and get JWT.";

    public static final String OP_DESC_LOGIN = """
            Used for login. Returns JWT string which should be set in Authorization header in the form of bearer token.
            If Authorization header is not set, user is considered to not be logged-in.
            Bearer: JWT_STRING
            """;

    // PARAMETERS
    public static final String PARAM_DESC_SIZE = """
            Amount of posts to be returned. If there are less posts than requested, as many posts as possible are
            returned.
            """;

    private static final String PAGINATION_REQUIREMENT = """
             Used for pagination. Pagination to work requires both karma_score and post_id to be set, otherwise top
            posts are returned.
            """;

    public static final String PARAM_DESC_POST_ID = "Id of the last returned post." + PAGINATION_REQUIREMENT;

    public static final String PARAM_DESC_KARMA_SCORE = "Score of the last returned post." + PAGINATION_REQUIREMENT;

    public static final String PARAM_DESC_USERNAME = """
            Creator user's username, makes endpoint return only these posts whose creator has this username.
            Can be omitted, to not filter by username.
            """;

    private static final String OTHER_VISIBILITIES_CAN_BE_SELECTED_SUFFIX =
            " At the same time other visibilities can also be selected.";

    public static final String PARAM_DESC_ACTIVE = "Post visibility, makes returned post have active visibility." +
            OTHER_VISIBILITIES_CAN_BE_SELECTED_SUFFIX;

    public static final String PARAM_DESC_HIDDEN = "Post visibility, makes returned post have hidden visibility" +
            OTHER_VISIBILITIES_CAN_BE_SELECTED_SUFFIX;

    public static final String PARAM_DESC_DELETED = "Post visibility, makes returned post have deleted visibility" +
            OTHER_VISIBILITIES_CAN_BE_SELECTED_SUFFIX;

    public static final String PARAM_DESC_POST_CREATION_REQUEST = "Json with data for creating new post.";

    public static final String PARAM_DESC_IMAGE_DATA = "Binary data of an image. Should be uploaded as a file.";

    public static final String PARAM_DESC_IS_POSITIVE = "Indicates whether post rating is positive or negative.";

    public static final String PARAM_DESC_USER_UPDATE_REQUEST_WITH_USER_PRIVILEGE = """
            Json with data for performing user update. Each field is optional, only set fields will be used for update.
            This objects contains fields which are safe to be modified with user privilege.
            """;

    public static final String PARAM_DESC_USER_UPDATE_REQUEST_WITH_ADMIN_PRIVILEGE = """
            Json with data for performing user update. Each field is optional, only set fields will be used for update.
            This objects contains fields which can be modified with user and admin privilege, thus to use it, one must
            have admin privilege.
            """;

    public static final String PARAM_DESC_REGISTER_REQUEST = "Json with data for creating new user account.";

    public static final String PARAM_DESC_LOGIN_REQUEST = "Json with data for logging-in.";

    // PATH PARAMETERS
    public static final String PATH_DESC_POST_ID = "Id of the post on which specified operation is being performed.";

    public static final String PATH_DESC_USER_ID = "Id of the user on which specified operation is being performed.";

    // RESPONSES
    public static final String RESP_OK_DESC_PAGINATED_POSTS = "Returned paginated posts.";

    public static final String RESP_OK_DESC_PAGINATED_POSTS_RATINGS = "Returned paginated posts ratings.";

    public static final String RESP_INTERNAL_DESC_PAGINATED_POSTS =
            "Could not get posts from the database for some reason.";

    public static final String RESP_INTERNAL_DESC_PAGINATED_POSTS_RATINGS =
            "Could not get posts ratings from the database for some reason.";

    public static final String RESP_OK_DESC_FIND_IMAGE = "Returned post's image data";

    public static final String RESP_OK_CREATE_POST = "Post was created successfully.";

    public static final String RESP_OK_RATE_POST = "Post was rated successfully.";

    public static final String RESP_INTERNAL_DESC_RATE_POST = "Could not rate post for some reason.";

    public static final String RESP_OK_UNRATE_POST = "Post was unrated successfully.";

    public static final String RESP_INTERNAL_DESC_UNRATE_POST = "Could not unrate post for some reason.";

    public static final String RESP_OK_HIDE_POST = "Post was hidden successfully.";

    public static final String RESP_OK_DELETE_POST = "Post was deleted successfully.";

    public static final String RESP_OK_ACTIVATE_POST = "Post was activated successfully.";

    public static final String RESP_INTERNAL_DESC_CHANGE_POST_VISIBILITY =
            "Could not change visibility of the post for some reason";

    public static final String RESP_UNAUTH_DESC_CHANGE_POST_VISIBILITY =
            "Could not change visibility of the post because of not sufficient privilege or not being the owner.";

    public static final String RESP_OK_ACCOUNT_UPDATE = "User account was updated successfully.";

    public static final String RESP_CONF_NOT_UNIQUE_OBJECT_FIELD =
            "At least one set field which should be unique is not.";

    public static final String RESP_BAD_REQ_ACCOUNT_UPDATE =
            "Zero fields were set in update object. At least single filed should be set.";

    public static final String RESP_OK_REGISTER = "User account was registered successfully.";

    public static final String RESP_OK_LOGIN = """
            User logged-in successfully. JWT string is returned. Authorization header should be set with string:
            "Bearer JWT_STRING" for the user to be considered logged-in. JWT is signed with sha256 and is valid for one
            hour. JWT has user's id encoded as subject.
            """;

    public static final String USERNAME_FETCH_FAIL_DURING_CACHING_SUFFIX = """
             This exception might be thrown during caching rated post. This might happen when username
            for post to be cached is fetched and somehow user who created the post was deleted. This
            practically cannot happen, because there is no functionality for deleting users.
            """;

    public static final String USER_NOT_FOUND_SUFFIX = """
             This exception might be thrown during failed fetch for user's username, which is required for caching full
            post data.
            """;
}