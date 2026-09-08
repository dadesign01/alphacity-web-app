package web

/*
 * Kotlin/Wasm ↔ JavaScript Kakao Map bridge
 *
 * kakao-map.js에서 window 전역으로 제공하는 함수/콜백을
 * Kotlin/Wasm에서 external 선언으로 연결한다.
 */


/*
 * ============================================================
 * JS 함수
 * ============================================================
 */

external fun kakaoMapInitialize()

external fun kakaoMapSetItems(
    json: String
)

external fun kakaoMapMoveTo(
    latitude: Double,
    longitude: Double,
    level: Int
)

external fun kakaoMapRequestLocation()

external fun kakaoMapDestroy()


/*
 * ============================================================
 * JS → Kotlin callback
 *
 * kakao-map.js:
 *
 * window.kakaoMapOnItemClick = function (...) {}
 * window.kakaoMapOnLocation = function (...) {}
 * window.kakaoMapOnLocationError = function (...) {}
 * window.kakaoMapOnCameraChanged = function (...) {}
 * ============================================================
 */

external var kakaoMapOnItemClick:
        ((String, Int, Int) -> Unit)?

external var kakaoMapOnLocation:
        ((Double, Double) -> Unit)?

external var kakaoMapOnLocationError:
        ((String) -> Unit)?

external var kakaoMapOnCameraChanged:
        ((Double, Double, Int) -> Unit)?


/*
 * ============================================================
 * Kakao Map Bridge
 * ============================================================
 */

object KakaoMapBridge {

    /*
     * ========================================================
     * 지도 생성
     * ========================================================
     */

    fun createMap(
        onItemClick: (String, Int, Int) -> Unit = { _, _, _ -> },
        onLocation: (Double, Double) -> Unit = { _, _ -> },
        onLocationError: (String) -> Unit = {},
        onCameraChanged: (Double, Double, Int) -> Unit = { _, _, _ -> },
    ) {

        /*
         * JS → Kotlin callback 등록
         */
        kakaoMapOnItemClick =
            onItemClick

        kakaoMapOnLocation =
            onLocation

        kakaoMapOnLocationError =
            onLocationError

        kakaoMapOnCameraChanged =
            onCameraChanged

        /*
         * 실제 Kakao Map 생성
         */
        kakaoMapInitialize()
    }


    /*
     * ========================================================
     * 지도 데이터 전달
     * ========================================================
     */

    fun setItems(
        json: String
    ) {
        kakaoMapSetItems(
            json
        )
    }


    /*
     * ========================================================
     * 지도 이동
     * ========================================================
     */

    fun moveTo(
        latitude: Double,
        longitude: Double,
        level: Int = 17,
    ) {

        kakaoMapMoveTo(
            latitude,
            longitude,
            level
        )
    }


    /*
     * ========================================================
     * 현재 위치 요청
     * ========================================================
     */

    fun requestLocation() {
        kakaoMapRequestLocation()
    }


    /*
     * ========================================================
     * 지도 제거
     *
     * MapScreen이 Composition에서 제거될 때 호출한다.
     *
     * kakao-map.js:
     * window.kakaoMapDestroy()
     * ========================================================
     */

    fun destroyMap() {

        kakaoMapDestroy()

        /*
         * 페이지가 완전히 종료되었으므로
         * 이전 Kotlin callback도 제거한다.
         */
        kakaoMapOnItemClick =
            null

        kakaoMapOnLocation =
            null

        kakaoMapOnLocationError =
            null

        kakaoMapOnCameraChanged =
            null
    }
}