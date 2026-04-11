package gdgoc.everyclub.docs;

public final class ClubDocs {

    public static final String TAG_NAME = "Clubs";
    public static final String TAG_DESCRIPTION = "동아리 API";

    /** 여러 엔드포인트의 @PathVariable 설명에서 공통으로 사용 */
    public static final String PARAM_CLUB_ID = "동아리 id";

    /** getClubs, searchClubs 두 엔드포인트에서 공통으로 사용 */
    public static final String PARAM_NAME = "동아리 이름 검색어";
    public static final String PARAM_TAG = "태그 검색어";

    private ClubDocs() {}
}
