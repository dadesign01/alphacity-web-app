var kakaoMapOnItemClick = null;
var kakaoMapOnLocation = null;
var kakaoMapOnLocationError = null;
var kakaoMapOnCameraChanged = null;

window.kakaoMapOnItemClick = kakaoMapOnItemClick;
window.kakaoMapOnLocation = kakaoMapOnLocation;
window.kakaoMapOnLocationError = kakaoMapOnLocationError;
window.kakaoMapOnCameraChanged = kakaoMapOnCameraChanged;

var map = null;

var programMarkers = [];
var missionMarkers = [];
var storeMarkers = [];
var clusterMarkers = [];

var infoOverlays = [];
var clusterListOverlay = null;

var userLocationMarker = null;

var currentItems = [];

var lastZoom = null;

var currentLocationLat = null;
var currentLocationLng = null;

var resizeHandler = null;

/*
 * 줌 변경 후 마커 재계산 debounce timer
 */
var zoomRebuildTimer = null;

var DEFAULT_LAT = 35.835478081566905;
var DEFAULT_LNG = 128.68178640679784;

/*
 * Kakao 지도는 숫자가 작을수록 확대다.
 *
 * 대한민국 전체가 나오는 문제가 있으므로
 * 초기 화면은 13으로 고정한다.
 *
 * 중요:
 * 이 값은 초기 진입시에만 사용한다.
 * 사용자가 이후 줌을 변경해도 다시 13으로
 * 강제하지 않는다.
 */
var DEFAULT_LEVEL = 4;


/* =========================================================
 * DEBUG
 * ========================================================= */

function getMapDebugState() {

    if (!map) {

        return {
            exists: false
        };
    }

    var center = null;

    try {

        center =
            map.getCenter();

    } catch (e) {

        center = null;
    }

    var bounds = null;

    try {

        bounds =
            map.getBounds();

    } catch (e) {

        bounds = null;
    }

    var result = {
        exists: true,

        level:
            map.getLevel(),

        lastZoom:
            lastZoom,

        center: center
            ? {
                lat: center.getLat(),
                lng: center.getLng()
            }
            : null,

        bounds: null
    };

    if (bounds) {

        try {

            var sw =
                bounds.getSouthWest();

            var ne =
                bounds.getNorthEast();

            result.bounds = {

                sw: {
                    lat: sw.getLat(),
                    lng: sw.getLng()
                },

                ne: {
                    lat: ne.getLat(),
                    lng: ne.getLng()
                }
            };

        } catch (e) {

            result.bounds = null;
        }
    }

    return result;
}


function logMapState(
    label
) {

    var state =
        getMapDebugState();

    console.log(
        "[KakaoMap][STATE]",
        label,
        state
    );
}


function logContainerState(
    label
) {

    var container =
        document.getElementById(
            "kakao-map-root"
        );

    if (!container) {

        console.warn(
            "[KakaoMap][CONTAINER][STATE]",
            label,
            "container 없음"
        );

        return;
    }

    var rect =
        container.getBoundingClientRect();

    console.log(
        "[KakaoMap][CONTAINER][STATE]",
        label,
        {
            rect: {
                left: rect.left,
                top: rect.top,
                right: rect.right,
                bottom: rect.bottom,
                width: rect.width,
                height: rect.height
            },

            clientWidth:
                container.clientWidth,

            clientHeight:
                container.clientHeight,

            offsetWidth:
                container.offsetWidth,

            offsetHeight:
                container.offsetHeight,

            scrollWidth:
                container.scrollWidth,

            scrollHeight:
                container.scrollHeight,

            styleWidth:
                container.style.width,

            styleHeight:
                container.style.height,

            styleTop:
                container.style.top,

            stylePosition:
                container.style.position
        }
    );
}


/* =========================================================
 * MARKER IMAGE
 * ========================================================= */

var RESOURCE_BASE =
    window.location.origin +
    "/composeResources/composewebtest.generated.resources/drawable/";

var SINGLE_MARKER_IMAGE_URL =
    RESOURCE_BASE +
    "single_marker.png";

var CLUSTER_MARKER_IMAGE_URL =
    RESOURCE_BASE +
    "cluster_marker.png";


/* =========================================================
 * COLORS
 * ========================================================= */

var PRIMARY_COLOR = "#02CDF8";
var MISSION_COLOR = "#4C27D0";
var CLUSTER_COLOR = "#2563EB";
var TEXT_COLOR = "#121212";


/* =========================================================
 * CLUSTER SETTINGS
 * ========================================================= */

var CLUSTER_DISTANCE = 60;

var SINGLE_MARKER_WIDTH = 42;
var SINGLE_MARKER_HEIGHT = 50;

var CLUSTER_MARKER_WIDTH = 42;
var CLUSTER_MARKER_HEIGHT = 50;


/* =========================================================
 * MAP CONTAINER
 * ========================================================= */

function updateMapContainerBounds() {

    console.log(
        "[KakaoMap][CONTAINER] ===== updateMapContainerBounds START ====="
    );

    var container =
        document.getElementById("kakao-map-root");

    if (!container) {

        console.error(
            "[KakaoMap] #kakao-map-root 없음"
        );

        return;
    }

    var composeRoot =
        document.getElementById("compose-root");

    if (composeRoot) {

        composeRoot.style.position =
            "relative";

        composeRoot.style.zIndex =
            "0";
    }

    var headerHeight = 57;
    var bottomNavHeight = 76;

    var viewportWidth =
        window.innerWidth;

    var viewportHeight =
        window.innerHeight;

    var mapHeight =
        viewportHeight -
        headerHeight -
        bottomNavHeight;

    if (mapHeight < 0) {
        mapHeight = viewportHeight;
    }

    console.log(
        "[KakaoMap][CONTAINER] 계산:",
        {
            viewportWidth:
                viewportWidth,

            viewportHeight:
                viewportHeight,

            headerHeight:
                headerHeight,

            bottomNavHeight:
                bottomNavHeight,

            calculatedMapHeight:
                mapHeight
        }
    );

    container.style.position =
        "fixed";

    container.style.left =
        "0px";

    container.style.top =
        headerHeight + "px";

    container.style.width =
        viewportWidth + "px";

    container.style.height =
        mapHeight + "px";

    container.style.minWidth =
        "0";

    container.style.minHeight =
        "0";

    container.style.display =
        "block";

    container.style.visibility =
        "visible";

    container.style.opacity =
        "1";

    container.style.overflow =
        "hidden";

    container.style.zIndex =
        "1";

    container.style.pointerEvents =
        "auto";

    console.log(
        "[KakaoMap][CONTAINER] bounds:",
        {
            width:
                viewportWidth,

            height:
                mapHeight,

            top:
                headerHeight,

            bottom:
                bottomNavHeight
        }
    );

    logContainerState(
        "updateMapContainerBounds AFTER"
    );

    console.log(
        "[KakaoMap][CONTAINER] ===== updateMapContainerBounds END ====="
    );
}


/* =========================================================
 * MAP RELAYOUT
 * ========================================================= */

function relayoutMap() {

    console.log(
        "[KakaoMap][RELAYOUT] 호출"
    );

    logMapState(
        "relayoutMap BEFORE"
    );

    logContainerState(
        "relayoutMap BEFORE"
    );

    if (!map) {

        console.warn(
            "[KakaoMap][RELAYOUT] map 없음 -> container만 업데이트"
        );

        updateMapContainerBounds();

        return;
    }

    updateMapContainerBounds();

    console.log(
        "[KakaoMap][RELAYOUT] map.relayout() 실행"
    );

    setTimeout(
        function() {

            if (!map) {

                console.warn(
                    "[KakaoMap][RELAYOUT] setTimeout 내부 map 없음"
                );

                return;
            }

            logMapState(
                "relayoutMap setTimeout BEFORE relayout"
            );

            map.relayout();

            logMapState(
                "relayoutMap AFTER relayout"
            );

            logContainerState(
                "relayoutMap AFTER relayout"
            );

        },
        0
    );
}


/* =========================================================
 * CAMERA
 * ========================================================= */

function applyDefaultCamera() {

    console.log(
        "[KakaoMap][CAMERA] ===== applyDefaultCamera START ====="
    );

    if (!map) {

        console.warn(
            "[KakaoMap][CAMERA] map 없음"
        );

        return;
    }

    logMapState(
        "applyDefaultCamera BEFORE"
    );

    var center =
        new kakao.maps.LatLng(
            DEFAULT_LAT,
            DEFAULT_LNG
        );

    console.log(
        "[KakaoMap][CAMERA] DEFAULT:",
        {
            lat:
                DEFAULT_LAT,

            lng:
                DEFAULT_LNG,

            level:
                DEFAULT_LEVEL
        }
    );


    /*
     * 첫 번째 setCenter
     */

    console.log(
        "[KakaoMap][CAMERA] map.setCenter() 실행 직전"
    );

    logMapState(
        "setCenter BEFORE"
    );

    map.setCenter(
        center
    );

    logMapState(
        "setCenter AFTER"
    );


    /*
     * setLevel
     */

    console.log(
        "[KakaoMap][CAMERA] map.setLevel() 실행:",
        DEFAULT_LEVEL
    );

    logMapState(
        "setLevel BEFORE"
    );

    map.setLevel(
        DEFAULT_LEVEL
    );

    logMapState(
        "setLevel AFTER"
    );


    /*
     * 두 번째 setCenter
     */

    console.log(
        "[KakaoMap][CAMERA] 두 번째 map.setCenter() 실행 직전"
    );

    logMapState(
        "second setCenter BEFORE"
    );

    map.setCenter(
        center
    );

    logMapState(
        "second setCenter AFTER"
    );


    console.log(
        "[KakaoMap][CAMERA] DEFAULT와 실제 상태 비교:",
        {
            expected: {
                lat:
                    DEFAULT_LAT,

                lng:
                    DEFAULT_LNG,

                level:
                    DEFAULT_LEVEL
            },

            actual:
                getMapDebugState()
        }
    );

    console.log(
        "[KakaoMap][CAMERA] ===== applyDefaultCamera END ====="
    );
}


function notifyCameraChanged() {

    if (!map) {
        return;
    }

    var center =
        map.getCenter();

    var level =
        map.getLevel();

    console.log(
        "[KakaoMap][CAMERA][NOTIFY]",
        {
            lat:
                center.getLat(),

            lng:
                center.getLng(),

            level:
                level
        }
    );

    if (
        typeof window.kakaoMapOnCameraChanged ===
        "function"
    ) {

        window.kakaoMapOnCameraChanged(
            center.getLat(),
            center.getLng(),
            level
        );
    }
}


/* =========================================================
 * MAP INITIALIZE
 * ========================================================= */

window.kakaoMapInitialize =
    function() {

        console.log(
            "[KakaoMap][INIT] ========================================"
        );

        console.log(
            "[KakaoMap][INIT] initialize 시작"
        );

        console.log(
            "[KakaoMap][INIT] DEFAULT CAMERA:",
            {
                lat:
                    DEFAULT_LAT,

                lng:
                    DEFAULT_LNG,

                level:
                    DEFAULT_LEVEL
            }
        );

        var container =
            document.getElementById(
                "kakao-map-root"
            );

        if (!container) {

            console.error(
                "[KakaoMap] #kakao-map-root 없음"
            );

            return;
        }

        console.log(
            "[KakaoMap][INIT] container 발견"
        );

        logContainerState(
            "INIT 시작"
        );

        if (
            typeof kakao === "undefined" ||
            !kakao.maps
        ) {

            console.error(
                "[KakaoMap] Kakao Maps SDK가 로드되지 않았습니다."
            );

            return;
        }

        console.log(
            "[KakaoMap][INIT] Kakao Maps SDK 확인 완료"
        );

        if (map) {

            console.log(
                "[KakaoMap][INIT] 기존 map 존재"
            );

            logMapState(
                "기존 map"
            );

            updateMapContainerBounds();

            console.log(
                "[KakaoMap][INIT] 기존 map.relayout() 실행"
            );

            map.relayout();

            logMapState(
                "기존 map.relayout AFTER"
            );

            /*
             * 기존 지도 재초기화 시에도
             * 기본 카메라를 적용한다.
             *
             * 단, 일반적인 줌 변경에서는
             * 이 코드가 호출되지 않는다.
             */
            console.log(
                "[KakaoMap][INIT] 기존 map -> applyDefaultCamera()"
            );

            applyDefaultCamera();

            logMapState(
                "기존 map applyDefaultCamera AFTER"
            );

            rebuildMarkers();

            return;
        }

        updateMapContainerBounds();

        var defaultCenter =
            new kakao.maps.LatLng(
                DEFAULT_LAT,
                DEFAULT_LNG
            );

        console.log(
            "[KakaoMap][INIT] 지도 생성 직전:",
            {
                centerLat:
                    DEFAULT_LAT,

                centerLng:
                    DEFAULT_LNG,

                level:
                    DEFAULT_LEVEL
            }
        );

        logContainerState(
            "지도 생성 직전"
        );

        map =
            new kakao.maps.Map(
                container,
                {
                    center: defaultCenter,
                    level: DEFAULT_LEVEL
                }
            );

        console.log(
            "[KakaoMap][INIT] 지도 생성 완료:",
            {
                level:
                    map.getLevel(),

                expectedLevel:
                    DEFAULT_LEVEL,

                lat:
                    map.getCenter().getLat(),

                lng:
                    map.getCenter().getLng()
            }
        );

        logMapState(
            "지도 생성 직후"
        );

        logContainerState(
            "지도 생성 직후"
        );


        /* =====================================================
         * ZOOM
         * ===================================================== */

        kakao.maps.event.addListener(
            map,
            "zoom_changed",
            function() {

                console.log(
                    "[KakaoMap][ZOOM] ===== zoom_changed ====="
                );

                if (!map) {
                    return;
                }

                var currentZoom =
                    map.getLevel();

                lastZoom =
                    currentZoom;

                console.log(
                    "[KakaoMap][ZOOM] 변경 감지:",
                    {
                        currentZoom:
                            currentZoom,

                        expectedDefaultLevel:
                            DEFAULT_LEVEL,

                        lastZoom:
                            lastZoom
                    }
                );

                logMapState(
                    "zoom_changed"
                );

                /*
                 * 줌 변경 시
                 * 현재 열려 있는 클러스터 목록을 닫는다.
                 */
                hideClusterList();

                /*
                 * 카메라 변경 상태 전달.
                 *
                 * 중요:
                 * 여기서 map.setLevel()을 호출하지 않는다.
                 *
                 * 사용자가 직접 조작한 줌 레벨을
                 * 그대로 유지한다.
                 */
                notifyCameraChanged();

                /*
                 * 줌 이벤트가 발생할 때마다
                 * marker를 즉시 전부 제거/생성하면
                 * 줌 동작 자체가 끊길 수 있다.
                 *
                 * 따라서 마지막 줌 변경 후 200ms 뒤에
                 * 한 번만 클러스터를 다시 계산한다.
                 */

                if (zoomRebuildTimer) {

                    console.log(
                        "[KakaoMap][ZOOM] 기존 rebuild timer 제거"
                    );

                    clearTimeout(
                        zoomRebuildTimer
                    );

                    zoomRebuildTimer =
                        null;
                }

                zoomRebuildTimer =
                    setTimeout(
                        function() {

                            console.log(
                                "[KakaoMap][ZOOM] debounce 200ms 완료"
                            );

                            zoomRebuildTimer =
                                null;

                            if (!map) {
                                return;
                            }

                            logMapState(
                                "ZOOM debounce BEFORE rebuild"
                            );

                            rebuildMarkers();

                            logMapState(
                                "ZOOM debounce AFTER rebuild"
                            );

                        },
                        200
                    );
            }
        );


        /* =====================================================
         * CENTER
         * ===================================================== */

        kakao.maps.event.addListener(
            map,
            "center_changed",
            function() {

                console.log(
                    "[KakaoMap][CENTER] ===== center_changed ====="
                );

                if (!map) {
                    return;
                }

                logMapState(
                    "center_changed"
                );

                /*
                 * 이동할 때 마커를 다시 만들지 않는다.
                 *
                 * CustomOverlay는 지도를 자동으로 따라간다.
                 */
                notifyCameraChanged();
            }
        );


        /* =====================================================
         * RESIZE
         * ===================================================== */

        resizeHandler =
            function() {

                console.log(
                    "[KakaoMap][RESIZE] ===== resize ====="
                );

                if (!map) {
                    return;
                }

                console.log(
                    "[KakaoMap][RESIZE] BEFORE"
                );

                logMapState(
                    "resize BEFORE"
                );

                logContainerState(
                    "resize BEFORE"
                );

                updateMapContainerBounds();

                console.log(
                    "[KakaoMap][RESIZE] map.relayout() 실행"
                );

                map.relayout();

                logMapState(
                    "resize AFTER relayout"
                );

                logContainerState(
                    "resize AFTER relayout"
                );

                /*
                 * 리사이즈 때만
                 * 클러스터 위치를 다시 계산한다.
                 */
                rebuildMarkers();

                logMapState(
                    "resize AFTER rebuild"
                );

            };

        window.addEventListener(
            "resize",
            resizeHandler
        );


        /* =====================================================
         * INITIAL RELAYOUT
         * ===================================================== */

        /*
         * 초기 100ms:
         *
         * relayout
         * default camera
         * marker 생성
         *
         * 여기서만 기본 카메라를 강제한다.
         */
        setTimeout(
            function() {

                console.log(
                    "[KakaoMap][INIT][100ms] =================="
                );

                if (!map) {

                    console.warn(
                        "[KakaoMap][INIT][100ms] map 없음"
                    );

                    return;
                }

                console.log(
                    "[KakaoMap][INIT][100ms] BEFORE"
                );

                logMapState(
                    "INIT 100ms BEFORE"
                );

                logContainerState(
                    "INIT 100ms BEFORE"
                );

                updateMapContainerBounds();

                console.log(
                    "[KakaoMap][INIT][100ms] map.relayout() 실행"
                );

                map.relayout();

                logMapState(
                    "INIT 100ms relayout AFTER"
                );

                logContainerState(
                    "INIT 100ms relayout AFTER"
                );

                console.log(
                    "[KakaoMap][INIT][100ms] applyDefaultCamera() 실행"
                );

                applyDefaultCamera();

                logMapState(
                    "INIT 100ms applyDefaultCamera AFTER"
                );

                rebuildMarkers();

                logMapState(
                    "INIT 100ms rebuild AFTER"
                );

                console.log(
                    "[KakaoMap][INIT][100ms] END:",
                    getMapDebugState()
                );

            },
            100
        );


        /*
         * 500ms:
         *
         * relayout + marker 재계산만 한다.
         *
         * 중요:
         * applyDefaultCamera()를 호출하지 않는다.
         *
         * 그래야 사용자가 초기 100ms 이후
         * 줌을 조작했을 때 다시 level 13으로
         * 돌아가지 않는다.
         */
        setTimeout(
            function() {

                console.log(
                    "[KakaoMap][INIT][500ms] =================="
                );

                if (!map) {

                    console.warn(
                        "[KakaoMap][INIT][500ms] map 없음"
                    );

                    return;
                }

                console.log(
                    "[KakaoMap][INIT][500ms] BEFORE"
                );

                logMapState(
                    "INIT 500ms BEFORE"
                );

                logContainerState(
                    "INIT 500ms BEFORE"
                );

                updateMapContainerBounds();

                console.log(
                    "[KakaoMap][INIT][500ms] map.relayout() 실행"
                );

                map.relayout();

                logMapState(
                    "INIT 500ms AFTER relayout"
                );

                logContainerState(
                    "INIT 500ms AFTER relayout"
                );

                rebuildMarkers();

                logMapState(
                    "INIT 500ms AFTER rebuild"
                );

                console.log(
                    "[KakaoMap][INIT][500ms] END:",
                    getMapDebugState()
                );

            },
            500
        );


        /*
         * 1000ms:
         *
         * 마지막 relayout + marker 재계산만 한다.
         *
         * 기본 카메라는 다시 적용하지 않는다.
         */
        setTimeout(
            function() {

                console.log(
                    "[KakaoMap][INIT][1000ms] =================="
                );

                if (!map) {

                    console.warn(
                        "[KakaoMap][INIT][1000ms] map 없음"
                    );

                    return;
                }

                console.log(
                    "[KakaoMap][INIT][1000ms] BEFORE"
                );

                logMapState(
                    "INIT 1000ms BEFORE"
                );

                logContainerState(
                    "INIT 1000ms BEFORE"
                );

                updateMapContainerBounds();

                console.log(
                    "[KakaoMap][INIT][1000ms] map.relayout() 실행"
                );

                map.relayout();

                logMapState(
                    "INIT 1000ms AFTER relayout"
                );

                logContainerState(
                    "INIT 1000ms AFTER relayout"
                );

                rebuildMarkers();

                logMapState(
                    "INIT 1000ms AFTER rebuild"
                );

                console.log(
                    "[KakaoMap][INIT][1000ms] END:",
                    getMapDebugState()
                );

            },
            1000
        );
    };


/* =========================================================
 * SET ITEMS
 * ========================================================= */

window.kakaoMapSetItems =
    function(json) {

        console.log(
            "[KakaoMap][SET_ITEMS] 호출"
        );

        try {

            var parsed =
                JSON.parse(json);

            console.log(
                "[KakaoMap][SET_ITEMS] JSON 파싱 완료:",
                {
                    isArray:
                        Array.isArray(parsed),

                    length:
                        Array.isArray(parsed)
                            ? parsed.length
                            : 0
                }
            );

            if (!Array.isArray(parsed)) {

                currentItems =
                    [];

            } else {

                currentItems =
                    parsed.filter(
                        function(item) {

                            return (
                                item &&
                                Number.isFinite(
                                    Number(
                                        item.latitude
                                    )
                                ) &&
                                Number.isFinite(
                                    Number(
                                        item.longitude
                                    )
                                )
                            );
                        }
                    );
            }

        } catch (e) {

            console.error(
                "[KakaoMap] 지도 데이터 파싱 실패:",
                e
            );

            currentItems =
                [];
        }

        console.log(
            "[KakaoMap][SET_ITEMS] 지도 데이터:",
            currentItems.length,
            "개"
        );

        if (map) {

            console.log(
                "[KakaoMap][SET_ITEMS] 현재 지도 level:",
                map.getLevel()
            );

            logMapState(
                "SET_ITEMS BEFORE rebuild"
            );
        }

        rebuildMarkers();

        if (map) {

            logMapState(
                "SET_ITEMS AFTER rebuild"
            );
        }
    };


/* =========================================================
 * MOVE TO
 * ========================================================= */

window.kakaoMapMoveTo =
    function(
        latitude,
        longitude,
        level
    ) {

        console.log(
            "[KakaoMap][MOVE_TO] 호출:",
            {
                latitude:
                    latitude,

                longitude:
                    longitude,

                level:
                    level
            }
        );

        if (!map) {

            console.warn(
                "[KakaoMap][MOVE_TO] map 없음"
            );

            return;
        }

        logMapState(
            "MOVE_TO BEFORE"
        );

        var lat =
            Number(latitude);

        var lng =
            Number(longitude);

        if (
            !Number.isFinite(lat) ||
            !Number.isFinite(lng)
        ) {

            console.error(
                "[KakaoMap][MOVE_TO] 잘못된 좌표:",
                {
                    lat:
                        lat,

                    lng:
                        lng
                }
            );

            return;
        }

        updateMapContainerBounds();

        console.log(
            "[KakaoMap][MOVE_TO] relayout 실행"
        );

        map.relayout();

        logMapState(
            "MOVE_TO AFTER relayout"
        );

        var position =
            new kakao.maps.LatLng(
                lat,
                lng
            );

        var targetLevel =
            DEFAULT_LEVEL;

        if (
            level !== undefined &&
            level !== null &&
            Number.isFinite(
                Number(level)
            )
        ) {

            targetLevel =
                Number(level);
        }

        /*
         * 너무 멀리 빠지는 레벨 방지.
         */
        if (targetLevel > 15) {
            targetLevel = 15;
        }

        if (targetLevel < 1) {
            targetLevel = 1;
        }

        console.log(
            "[KakaoMap][MOVE_TO] 실제 적용값:",
            {
                targetLat:
                    lat,

                targetLng:
                    lng,

                targetLevel:
                    targetLevel
            }
        );

        console.log(
            "[KakaoMap][MOVE_TO] map.setLevel() 실행 직전"
        );

        map.setLevel(
            targetLevel
        );

        logMapState(
            "MOVE_TO AFTER setLevel"
        );

        console.log(
            "[KakaoMap][MOVE_TO] map.setCenter() 실행 직전"
        );

        map.setCenter(
            position
        );

        logMapState(
            "MOVE_TO AFTER setCenter"
        );

        console.log(
            "[KakaoMap][MOVE_TO] 실제 CAMERA:",
            {
                requested: {
                    lat:
                        lat,

                    lng:
                        lng,

                    level:
                        targetLevel
                },

                actual:
                    getMapDebugState()
            }
        );
    };


/* =========================================================
 * LOCATION
 * ========================================================= */

window.kakaoMapRequestLocation =
    function() {

        console.log(
            "[KakaoMap][LOCATION] 위치 요청"
        );

        if (!navigator.geolocation) {

            console.error(
                "[KakaoMap] GPS를 사용할 수 없습니다."
            );

            if (
                typeof window.kakaoMapOnLocationError ===
                "function"
            ) {

                window.kakaoMapOnLocationError(
                    "GPS를 사용할 수 없습니다."
                );
            }

            return;
        }

        navigator.geolocation.getCurrentPosition(

            function(position) {

                var lat =
                    position.coords.latitude;

                var lng =
                    position.coords.longitude;

                currentLocationLat =
                    lat;

                currentLocationLng =
                    lng;

                console.log(
                    "[KakaoMap][LOCATION] GPS 위치:",
                    lat,
                    lng
                );

                updateUserLocationMarker(
                    lat,
                    lng
                );

                if (
                    typeof window.kakaoMapOnLocation ===
                    "function"
                ) {

                    window.kakaoMapOnLocation(
                        lat,
                        lng
                    );
                }

            },

            function(error) {

                console.error(
                    "[KakaoMap][LOCATION] GPS 오류:",
                    error
                );

                if (
                    typeof window.kakaoMapOnLocationError ===
                    "function"
                ) {

                    window.kakaoMapOnLocationError(
                        "위치 정보를 가져올 수 없습니다."
                    );
                }
            },

            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }
        );
    };


/* =========================================================
 * USER LOCATION MARKER
 * ========================================================= */

function updateUserLocationMarker(
    latitude,
    longitude
) {

    console.log(
        "[KakaoMap][LOCATION_MARKER] 생성:",
        {
            latitude:
                latitude,

            longitude:
                longitude
        }
    );

    if (!map) {
        return;
    }

    var position =
        new kakao.maps.LatLng(
            latitude,
            longitude
        );

    if (userLocationMarker) {

        userLocationMarker.setMap(
            null
        );

        userLocationMarker =
            null;
    }

    var content =
        document.createElement(
            "div"
        );

    content.style.width =
        "18px";

    content.style.height =
        "18px";

    content.style.borderRadius =
        "50%";

    content.style.background =
        "rgba(37, 99, 235, 0.25)";

    content.style.display =
        "flex";

    content.style.alignItems =
        "center";

    content.style.justifyContent =
        "center";

    content.style.pointerEvents =
        "none";

    var inner =
        document.createElement(
            "div"
        );

    inner.style.width =
        "10px";

    inner.style.height =
        "10px";

    inner.style.borderRadius =
        "50%";

    inner.style.background =
        "#2563EB";

    inner.style.border =
        "2px solid white";

    inner.style.boxSizing =
        "border-box";

    content.appendChild(
        inner
    );

    userLocationMarker =
        new kakao.maps.CustomOverlay(
            {
                position: position,
                content: content,
                yAnchor: 0.5,
                xAnchor: 0.5,
                zIndex: 20
            }
        );

    userLocationMarker.setMap(
        map
    );
}


/* =========================================================
 * INFO WINDOW TEXT
 * ========================================================= */

function truncateInfoText(
    text
) {

    if (
        text === undefined ||
        text === null
    ) {
        return "";
    }

    var value =
        String(text);

    var characters =
        Array.from(value);

    if (
        characters.length >= 5
    ) {

        return (
            characters
                .slice(0, 4)
                .join("") +
            "..."
        );
    }

    return value;
}


/* =========================================================
 * CLEAR INFO WINDOWS
 * ========================================================= */

function clearInfoOverlays() {

    for (
        var i = 0;
        i < infoOverlays.length;
        i++
    ) {

        if (infoOverlays[i]) {

            infoOverlays[i].setMap(
                null
            );
        }
    }

    infoOverlays.length = 0;
}


/* =========================================================
 * CLUSTER LIST HIDE
 * ========================================================= */

function hideClusterList() {

    if (clusterListOverlay) {

        console.log(
            "[KakaoMap][CLUSTER] 목록 닫기"
        );

        clusterListOverlay.setMap(
            null
        );

        clusterListOverlay =
            null;
    }
}


/* =========================================================
 * HIDE INFO WINDOWS
 *
 * 기존 선택형 구조와 호환용.
 * 현재는 개별 마커의 info가 항상 표시되므로
 * 전체 info를 닫는 용도로 사용하지 않는다.
 * ========================================================= */

function hideInfoWindow() {

    /*
     * 클러스터 목록만 닫는다.
     *
     * 개별 인포윈도우는 항상 표시되어야 하므로
     * 여기서 제거하지 않는다.
     */

    hideClusterList();
}


/* =========================================================
 * INFO WINDOW CLICK
 * ========================================================= */

function handleItemClick(
    item
) {

    if (
        !item ||
        typeof window.kakaoMapOnItemClick !==
        "function"
    ) {
        return;
    }

    var targetId =
        item.targetProgramId;

    if (
        targetId === undefined ||
        targetId === null ||
        Number(targetId) <= 0
    ) {

        targetId =
            item.id;
    }

    console.log(
        "[KakaoMap] ITEM CLICK:",
        item.type,
        item.id,
        targetId
    );

    window.kakaoMapOnItemClick(
        String(item.type || ""),
        Number(item.id),
        Number(targetId || -1)
    );
}


/* =========================================================
 * CREATE INFO WINDOW
 *
 * 개별 마커마다 항상 표시한다.
 *
 * 좌표:
 *
 *      marker
 *        ↓
 *      [INFO]
 *
 * marker는 yAnchor=1
 * info는 yAnchor=0
 *
 * 따라서 marker 하단과 info 상단이
 * 동일한 좌표에 위치한다.
 * ========================================================= */

function createInfoMarkerOverlay(
    item
) {

    if (!map || !item) {
        return null;
    }

    var latitude =
        Number(item.latitude);

    var longitude =
        Number(item.longitude);

    if (
        !Number.isFinite(latitude) ||
        !Number.isFinite(longitude)
    ) {
        return null;
    }

    var position =
        new kakao.maps.LatLng(
            latitude,
            longitude
        );

    var info =
        document.createElement(
            "div"
        );

    info.style.display =
        "flex";

    info.style.alignItems =
        "center";

    info.style.justifyContent =
        "center";

    info.style.width =
        "max-content";

    info.style.minWidth =
        "30px";

    info.style.height =
        "30px";

    info.style.padding =
        "0 12px";

    info.style.margin =
        "0";

    info.style.background =
        "#2563EB";

    info.style.borderRadius =
        "15px";

    info.style.boxSizing =
        "border-box";

    info.style.color =
        "#ffffff";

    info.style.fontSize =
        "13px";

    info.style.fontWeight =
        "600";

    info.style.fontFamily =
        "Arial, sans-serif";

    info.style.lineHeight =
        "30px";

    info.style.whiteSpace =
        "nowrap";

    info.style.cursor =
        "pointer";

    info.style.pointerEvents =
        "auto";

    info.style.userSelect =
        "none";

    info.style.boxShadow =
        "0 2px 6px rgba(0,0,0,0.18)";

    info.textContent =
        truncateInfoText(
            item.name
        );


    info.addEventListener(
        "click",
        function(event) {

            event.stopPropagation();

            handleItemClick(
                item
            );
        }
    );


    var overlay =
        new kakao.maps.CustomOverlay(
            {
                position: position,
                content: info,
                xAnchor: 0.5,
                yAnchor: 0,
                zIndex: 30
            }
        );

    return overlay;
}


/* =========================================================
 * CREATE SINGLE MARKER
 * ========================================================= */

function createSingleMarkerContent(
    item
) {

    var wrapper =
        document.createElement(
            "div"
        );

    wrapper.style.position =
        "relative";

    wrapper.style.width =
        SINGLE_MARKER_WIDTH + "px";

    wrapper.style.height =
        SINGLE_MARKER_HEIGHT + "px";

    wrapper.style.cursor =
        "pointer";

    wrapper.style.userSelect =
        "none";

    wrapper.style.pointerEvents =
        "auto";


    var image =
        document.createElement(
            "img"
        );

    image.src =
        SINGLE_MARKER_IMAGE_URL;

    image.style.display =
        "block";

    image.style.position =
        "absolute";

    image.style.left =
        "0px";

    image.style.top =
        "0px";

    image.style.width =
        SINGLE_MARKER_WIDTH + "px";

    image.style.height =
        SINGLE_MARKER_HEIGHT + "px";

    image.style.objectFit =
        "contain";

    image.style.margin =
        "0";

    image.style.padding =
        "0";

    image.style.pointerEvents =
        "none";

    image.draggable =
        false;

    image.onload =
        function() {

            console.log(
                "[KakaoMap] single_marker.png 로딩 완료:",
                image.src
            );
        };

    image.onerror =
        function() {

            console.error(
                "[KakaoMap] single_marker.png 로딩 실패:",
                image.src
            );
        };

    wrapper.appendChild(
        image
    );


    /* =====================================================
     * CENTER DOT
     * ===================================================== */

    var dot =
        document.createElement(
            "div"
        );

    dot.style.position =
        "absolute";

    dot.style.left =
        "50%";

    dot.style.top =
        "39%";

    dot.style.transform =
        "translate(-50%, -50%)";

    dot.style.width =
        "10px";

    dot.style.height =
        "10px";

    dot.style.borderRadius =
        "50%";

    dot.style.background =
        getMarkerColor(item);

    dot.style.border =
        "2px solid white";

    dot.style.boxSizing =
        "border-box";

    dot.style.pointerEvents =
        "none";

    wrapper.appendChild(
        dot
    );

    return wrapper;
}


/* =========================================================
 * SINGLE MARKER
 * ========================================================= */

function createSingleMarker(
    item
) {

    var latitude =
        Number(item.latitude);

    var longitude =
        Number(item.longitude);

    if (
        !Number.isFinite(latitude) ||
        !Number.isFinite(longitude)
    ) {

        return null;
    }

    var position =
        new kakao.maps.LatLng(
            latitude,
            longitude
        );

    var content =
        createSingleMarkerContent(
            item
        );

    var markerOverlay =
        new kakao.maps.CustomOverlay(
            {
                position: position,
                content: content,

                /*
                 * marker의 아래쪽 끝이
                 * 좌표에 위치한다.
                 */
                yAnchor: 1,
                xAnchor: 0.5,

                zIndex: 10
            }
        );


    /*
     * marker 클릭도 기존 상세 이동을 유지한다.
     */
    content.addEventListener(
        "click",
        function(event) {

            event.stopPropagation();

            console.log(
                "[KakaoMap] MARKER CLICK:",
                item.name,
                item.id
            );

            handleItemClick(
                item
            );
        }
    );


    markerOverlay.setMap(
        map
    );


    /*
     * 중요:
     *
     * 이제 info window는 클릭해야 생성하지 않는다.
     *
     * 개별 marker가 만들어지는 순간
     * 바로 info도 만든다.
     */

    var infoOverlay =
        createInfoMarkerOverlay(
            item
        );

    if (infoOverlay) {

        infoOverlay.setMap(
            map
        );

        infoOverlays.push(
            infoOverlay
        );
    }


    return markerOverlay;
}


/* =========================================================
 * CLUSTER CONTENT
 * ========================================================= */

function createClusterContent(
    count
) {

    var wrapper =
        document.createElement(
            "div"
        );

    wrapper.style.position =
        "relative";

    wrapper.style.width =
        CLUSTER_MARKER_WIDTH + "px";

    wrapper.style.height =
        CLUSTER_MARKER_HEIGHT + "px";

    wrapper.style.cursor =
        "pointer";

    wrapper.style.userSelect =
        "none";

    wrapper.style.pointerEvents =
        "auto";


    var image =
        document.createElement(
            "img"
        );

    image.src =
        CLUSTER_MARKER_IMAGE_URL;

    image.style.display =
        "block";

    image.style.position =
        "absolute";

    image.style.left =
        "0px";

    image.style.top =
        "0px";

    image.style.width =
        CLUSTER_MARKER_WIDTH + "px";

    image.style.height =
        CLUSTER_MARKER_HEIGHT + "px";

    image.style.objectFit =
        "contain";

    image.style.margin =
        "0";

    image.style.padding =
        "0";

    image.style.pointerEvents =
        "none";

    image.draggable =
        false;

    image.onload =
        function() {

            console.log(
                "[KakaoMap] cluster_marker.png 로딩 완료:",
                image.src
            );
        };

    image.onerror =
        function() {

            console.error(
                "[KakaoMap] cluster_marker.png 로딩 실패:",
                image.src
            );
        };

    wrapper.appendChild(
        image
    );


    /* =====================================================
     * CLUSTER BADGE
     * ===================================================== */

    var badge =
        document.createElement(
            "div"
        );

    badge.style.position =
        "absolute";

    badge.style.right =
        "-7px";

    badge.style.bottom =
        "-5px";

    badge.style.minWidth =
        "27px";

    badge.style.height =
        "27px";

    badge.style.padding =
        "0 5px";

    badge.style.borderRadius =
        "50%";

    badge.style.background =
        "#2563EB";

    badge.style.color =
        "#ffffff";

    badge.style.fontSize =
        "16px";

    badge.style.fontWeight =
        "600";

    badge.style.fontFamily =
        "Arial, sans-serif";

    badge.style.display =
        "flex";

    badge.style.alignItems =
        "center";

    badge.style.justifyContent =
        "center";

    badge.style.boxSizing =
        "border-box";

    badge.style.lineHeight =
        "27px";

    badge.style.whiteSpace =
        "nowrap";

    badge.style.pointerEvents =
        "none";

    badge.textContent =
        String(count);

    wrapper.appendChild(
        badge
    );

    return wrapper;
}


/* =========================================================
 * CLUSTER LIST ITEM TEXT
 * ========================================================= */

function getClusterItemTitle(
    item
) {

    if (!item) {
        return "";
    }

    if (
        item.name !== undefined &&
        item.name !== null &&
        String(item.name).trim() !== ""
    ) {

        return String(item.name);
    }

    if (
        item.title !== undefined &&
        item.title !== null &&
        String(item.title).trim() !== ""
    ) {

        return String(item.title);
    }

    return "장소";
}


/* =========================================================
 * SHOW CLUSTER LIST
 *
 * 카카오/네이버 지도처럼
 * 같은 위치에 겹친 장소를 목록으로 보여준다.
 * ========================================================= */

function showClusterList(
    items,
    center
) {

    if (
        !map ||
        !Array.isArray(items) ||
        items.length === 0 ||
        !center
    ) {
        return;
    }

    console.log(
        "[KakaoMap][CLUSTER] 목록 표시:",
        {
            count:
                items.length,

            centerLat:
                center.getLat(),

            centerLng:
                center.getLng()
        }
    );

    hideClusterList();

    var container =
        document.createElement(
            "div"
        );

    container.style.width =
        "230px";

    container.style.maxWidth =
        "calc(100vw - 40px)";

    container.style.maxHeight =
        "260px";

    container.style.overflowY =
        "auto";

    container.style.background =
        "#ffffff";

    container.style.borderRadius =
        "14px";

    container.style.boxShadow =
        "0 4px 18px rgba(0,0,0,0.22)";

    container.style.border =
        "1px solid rgba(0,0,0,0.08)";

    container.style.boxSizing =
        "border-box";

    container.style.pointerEvents =
        "auto";

    container.style.fontFamily =
        "Arial, sans-serif";


    /* =====================================================
     * TITLE
     * ===================================================== */

    var header =
        document.createElement(
            "div"
        );

    header.style.padding =
        "12px 14px 10px";

    header.style.fontSize =
        "14px";

    header.style.fontWeight =
        "700";

    header.style.color =
        "#121212";

    header.style.borderBottom =
        "1px solid #eeeeee";

    header.textContent =
        items.length +
        "개의 장소";

    container.appendChild(
        header
    );


    /* =====================================================
     * ITEMS
     * ===================================================== */

    for (
        var i = 0;
        i < items.length;
        i++
    ) {

        (function(item) {

            var row =
                document.createElement(
                    "div"
                );

            row.style.minHeight =
                "46px";

            row.style.padding =
                "0 14px";

            row.style.display =
                "flex";

            row.style.alignItems =
                "center";

            row.style.boxSizing =
                "border-box";

            row.style.cursor =
                "pointer";

            row.style.pointerEvents =
                "auto";

            row.style.background =
                "#ffffff";

            row.style.borderBottom =
                "1px solid #eeeeee";

            row.style.fontSize =
                "13px";

            row.style.fontWeight =
                "600";

            row.style.color =
                "#121212";


            var text =
                document.createElement(
                    "div"
                );

            text.style.flex =
                "1";

            text.style.minWidth =
                "0";

            text.style.overflow =
                "hidden";

            text.style.textOverflow =
                "ellipsis";

            text.style.whiteSpace =
                "nowrap";

            text.textContent =
                getClusterItemTitle(
                    item
                );


            row.appendChild(
                text
            );


            row.addEventListener(
                "mouseenter",
                function() {

                    row.style.background =
                        "#f5f5f5";
                }
            );


            row.addEventListener(
                "mouseleave",
                function() {

                    row.style.background =
                        "#ffffff";
                }
            );


            row.addEventListener(
                "click",
                function(event) {

                    event.stopPropagation();

                    console.log(
                        "[KakaoMap] CLUSTER ITEM CLICK:",
                        item.name,
                        item.id
                    );

                    hideClusterList();

                    handleItemClick(
                        item
                    );
                }
            );


            container.appendChild(
                row
            );

        })(items[i]);
    }


    /*
     * 마지막 border 제거
     */

    if (
        container.lastChild
    ) {

        container.lastChild.style.borderBottom =
            "0";
    }


    clusterListOverlay =
        new kakao.maps.CustomOverlay(
            {
                position: center,
                content: container,

                /*
                 * 클러스터 좌표의 아래쪽에
                 * 목록을 표시한다.
                 */
                xAnchor: 0.5,
                yAnchor: 0,

                zIndex: 100
            }
        );

    clusterListOverlay.setMap(
        map
    );


    console.log(
        "[KakaoMap] CLUSTER LIST 표시:",
        items.length,
        "개"
    );
}


/* =========================================================
 * CLUSTER
 * ========================================================= */

function createCluster(
    items
) {

    if (
        !items ||
        items.length < 2
    ) {
        return null;
    }

    var latitudeTotal =
        0;

    var longitudeTotal =
        0;

    for (
        var i = 0;
        i < items.length;
        i++
    ) {

        latitudeTotal +=
            Number(
                items[i].latitude
            );

        longitudeTotal +=
            Number(
                items[i].longitude
            );
    }

    var center =
        new kakao.maps.LatLng(
            latitudeTotal / items.length,
            longitudeTotal / items.length
        );

    var content =
        createClusterContent(
            items.length
        );

    var overlay =
        new kakao.maps.CustomOverlay(
            {
                position: center,
                content: content,
                xAnchor: 0.5,
                yAnchor: 0.5,
                zIndex: 15
            }
        );


    content.addEventListener(
        "click",
        function(event) {

            event.stopPropagation();

            if (!map) {
                return;
            }

            console.log(
                "[KakaoMap] CLUSTER CLICK:",
                items.length,
                "개"
            );

            /*
             * 기존처럼 바로 줌인하지 않는다.
             *
             * 같은 좌표에 겹친 장소를
             * 목록으로 보여준다.
             */

            showClusterList(
                items,
                center
            );
        }
    );


    overlay.setMap(
        map
    );

    return overlay;
}


/* =========================================================
 * CLEAR MARKERS
 * ========================================================= */

function clearMarkerArray(
    markers
) {

    for (
        var i = 0;
        i < markers.length;
        i++
    ) {

        if (markers[i]) {

            markers[i].setMap(
                null
            );
        }
    }

    markers.length = 0;
}


function clearAllMarkers() {

    console.log(
        "[KakaoMap][MARKER] clearAllMarkers:",
        {
            program:
                programMarkers.length,

            mission:
                missionMarkers.length,

            store:
                storeMarkers.length,

            cluster:
                clusterMarkers.length,

            info:
                infoOverlays.length
        }
    );

    clearInfoOverlays();

    hideClusterList();

    clearMarkerArray(
        programMarkers
    );

    clearMarkerArray(
        missionMarkers
    );

    clearMarkerArray(
        storeMarkers
    );

    clearMarkerArray(
        clusterMarkers
    );
}


/* =========================================================
 * BUILD CLUSTERS
 * ========================================================= */

function buildClusters(
    items
) {

    if (
        !map ||
        !Array.isArray(items) ||
        items.length === 0
    ) {

        return [];
    }

    console.log(
        "[KakaoMap][CLUSTER_BUILD] 시작:",
        {
            itemCount:
                items.length,

            level:
                map.getLevel()
        }
    );

    var projection =
        map.getProjection();

    if (!projection) {

        console.warn(
            "[KakaoMap][CLUSTER_BUILD] projection 없음"
        );

        return [];
    }

    var nodes =
        [];

    for (
        var i = 0;
        i < items.length;
        i++
    ) {

        var item =
            items[i];

        var position =
            new kakao.maps.LatLng(
                Number(item.latitude),
                Number(item.longitude)
            );

        var point =
            projection.containerPointFromCoords(
                position
            );

        nodes.push(
            {
                item: item,
                point: point,
                used: false
            }
        );
    }

    var groups =
        [];

    for (
        var i = 0;
        i < nodes.length;
        i++
    ) {

        if (
            nodes[i].used
        ) {
            continue;
        }

        var group =
            [nodes[i]];

        nodes[i].used =
            true;

        var changed =
            true;

        while (changed) {

            changed =
                false;

            var centerX =
                0;

            var centerY =
                0;

            for (
                var g = 0;
                g < group.length;
                g++
            ) {

                centerX +=
                    group[g].point.x;

                centerY +=
                    group[g].point.y;
            }

            centerX /=
                group.length;

            centerY /=
                group.length;

            for (
                var j = 0;
                j < nodes.length;
                j++
            ) {

                if (
                    nodes[j].used
                ) {
                    continue;
                }

                var dx =
                    nodes[j].point.x -
                    centerX;

                var dy =
                    nodes[j].point.y -
                    centerY;

                var distance =
                    Math.sqrt(
                        dx * dx +
                        dy * dy
                    );

                if (
                    distance <=
                    CLUSTER_DISTANCE
                ) {

                    nodes[j].used =
                        true;

                    group.push(
                        nodes[j]
                    );

                    changed =
                        true;
                }
            }
        }

        groups.push(
            group.map(
                function(node) {
                    return node.item;
                }
            )
        );
    }

    console.log(
        "[KakaoMap][CLUSTER_BUILD] 완료:",
        {
            groupCount:
                groups.length,

            groups:
                groups.map(
                    function(group) {
                        return group.length;
                    }
                )
        }
    );

    return groups;
}


/* =========================================================
 * ADD SINGLE MARKER
 * ========================================================= */

function addSingleMarkerToCategory(
    marker,
    item
) {

    if (!marker) {
        return;
    }

    var category =
        item.category ||
        item.type ||
        "";

    if (
        category === "mission" ||
        item.type === "mission"
    ) {

        missionMarkers.push(
            marker
        );

    } else if (
        category === "food" ||
        category === "store" ||
        item.type === "store"
    ) {

        storeMarkers.push(
            marker
        );

    } else {

        programMarkers.push(
            marker
        );
    }
}


/* =========================================================
 * REBUILD MARKERS
 * ========================================================= */

function rebuildMarkers() {

    if (!map) {

        console.warn(
            "[KakaoMap][REBUILD] map 없음"
        );

        return;
    }

    console.log(
        "[KakaoMap][REBUILD] 시작:",
        {
            level:
                map.getLevel(),

            itemCount:
                currentItems.length
        }
    );

    logMapState(
        "REBUILD BEFORE clear"
    );

    /*
     * 기존 marker + info + cluster list 전부 제거
     */
    clearAllMarkers();

    if (
        !Array.isArray(currentItems) ||
        currentItems.length === 0
    ) {

        console.log(
            "[KakaoMap] 표시할 마커 없음"
        );

        return;
    }

    var groups =
        buildClusters(
            currentItems
        );

    var singleCount =
        0;

    var clusterCount =
        0;

    for (
        var i = 0;
        i < groups.length;
        i++
    ) {

        var group =
            groups[i];

        if (
            group.length === 1
        ) {

            var item =
                group[0];

            /*
             * createSingleMarker 내부에서
             * marker + 항상 표시되는 info까지 생성한다.
             */
            var marker =
                createSingleMarker(
                    item
                );

            if (!marker) {
                continue;
            }

            addSingleMarkerToCategory(
                marker,
                item
            );

            singleCount++;

        } else {

            var cluster =
                createCluster(
                    group
                );

            if (cluster) {

                clusterMarkers.push(
                    cluster
                );

                clusterCount++;
            }
        }
    }

    console.log(
        "[KakaoMap][REBUILD] 완료:",
        {
            level:
                map.getLevel(),

            single:
                singleCount,

            cluster:
                clusterCount,

            program:
                programMarkers.length,

            mission:
                missionMarkers.length,

            store:
                storeMarkers.length,

            info:
                infoOverlays.length
        }
    );

    logMapState(
        "REBUILD AFTER"
    );
}


/* =========================================================
 * MARKER COLOR
 * ========================================================= */

function getMarkerColor(
    item
) {

    if (
        item.category === "mission" ||
        item.type === "mission"
    ) {

        return MISSION_COLOR;
    }

    if (
        item.category === "store" ||
        item.type === "store" ||
        item.category === "food"
    ) {

        return PRIMARY_COLOR;
    }

    return PRIMARY_COLOR;
}


/* =========================================================
 * DESTROY
 * ========================================================= */

window.kakaoMapDestroy =
    function() {

        console.log(
            "[KakaoMap][DESTROY] ===== START ====="
        );

        if (map) {

            logMapState(
                "DESTROY BEFORE"
            );
        }

        /*
         * 예약되어 있는 줌 debounce 제거
         */
        if (zoomRebuildTimer) {

            console.log(
                "[KakaoMap][DESTROY] zoom timer 제거"
            );

            clearTimeout(
                zoomRebuildTimer
            );

            zoomRebuildTimer =
                null;
        }

        clearAllMarkers();


        if (userLocationMarker) {

            userLocationMarker.setMap(
                null
            );

            userLocationMarker =
                null;
        }


        if (
            resizeHandler
        ) {

            window.removeEventListener(
                "resize",
                resizeHandler
            );

            resizeHandler =
                null;
        }


        map =
            null;


        currentItems =
            [];

        lastZoom =
            null;

        currentLocationLat =
            null;

        currentLocationLng =
            null;


        var container =
            document.getElementById(
                "kakao-map-root"
            );


        if (container) {

            container.innerHTML =
                "";

            container.style.position =
                "relative";

            container.style.width =
                "100%";

            container.style.height =
                "100%";

            container.style.display =
                "none";

            container.style.visibility =
                "hidden";

            logContainerState(
                "DESTROY AFTER"
            );
        }


        console.log(
            "[KakaoMap] 지도 제거 완료"
        );

        console.log(
            "[KakaoMap][DESTROY] ===== END ====="
        );
    };