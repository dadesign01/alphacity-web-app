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

    var container =
        document.getElementById("kakao-map-root");

    if (!container) {
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
}


/* =========================================================
 * MAP RELAYOUT
 * ========================================================= */

function relayoutMap() {

    if (!map) {

        updateMapContainerBounds();

        return;
    }

    updateMapContainerBounds();

    setTimeout(
        function() {

            if (!map) {
                return;
            }

            map.relayout();

        },
        0
    );
}


/* =========================================================
 * CAMERA
 * ========================================================= */

function applyDefaultCamera() {

    if (!map) {
        return;
    }

    var center =
        new kakao.maps.LatLng(
            DEFAULT_LAT,
            DEFAULT_LNG
        );

    /*
     * 첫 번째 setCenter
     */

    map.setCenter(
        center
    );


    /*
     * setLevel
     */

    map.setLevel(
        DEFAULT_LEVEL
    );


    /*
     * 두 번째 setCenter
     */

    map.setCenter(
        center
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

        var container =
            document.getElementById(
                "kakao-map-root"
            );

        if (!container) {
            return;
        }

        if (
            typeof kakao === "undefined" ||
            !kakao.maps
        ) {
            return;
        }

        if (map) {

            updateMapContainerBounds();

            map.relayout();

            /*
             * 기존 지도 재초기화 시에도
             * 기본 카메라를 적용한다.
             *
             * 단, 일반적인 줌 변경에서는
             * 이 코드가 호출되지 않는다.
             */
            applyDefaultCamera();

            rebuildMarkers();

            return;
        }

        updateMapContainerBounds();

        var defaultCenter =
            new kakao.maps.LatLng(
                DEFAULT_LAT,
                DEFAULT_LNG
            );

        map =
            new kakao.maps.Map(
                container,
                {
                    center: defaultCenter,
                    level: DEFAULT_LEVEL
                }
            );


        /* =====================================================
         * ZOOM
         * ===================================================== */

        kakao.maps.event.addListener(
            map,
            "zoom_changed",
            function() {

                if (!map) {
                    return;
                }

                var currentZoom =
                    map.getLevel();

                lastZoom =
                    currentZoom;

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

                    clearTimeout(
                        zoomRebuildTimer
                    );

                    zoomRebuildTimer =
                        null;
                }

                zoomRebuildTimer =
                    setTimeout(
                        function() {

                            zoomRebuildTimer =
                                null;

                            if (!map) {
                                return;
                            }

                            rebuildMarkers();

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

                if (!map) {
                    return;
                }

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

                if (!map) {
                    return;
                }

                updateMapContainerBounds();

                map.relayout();

                /*
                 * 리사이즈 때만
                 * 클러스터 위치를 다시 계산한다.
                 */
                rebuildMarkers();

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

                if (!map) {
                    return;
                }

                updateMapContainerBounds();

                map.relayout();

                applyDefaultCamera();

                rebuildMarkers();

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

                if (!map) {
                    return;
                }

                updateMapContainerBounds();

                map.relayout();

                rebuildMarkers();

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

                if (!map) {
                    return;
                }

                updateMapContainerBounds();

                map.relayout();

                rebuildMarkers();

            },
            1000
        );
    };


/* =========================================================
 * SET ITEMS
 * ========================================================= */

window.kakaoMapSetItems =
    function(json) {

        try {

            var parsed =
                JSON.parse(json);

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

            currentItems =
                [];
        }

        rebuildMarkers();
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

        if (!map) {
            return;
        }

        var lat =
            Number(latitude);

        var lng =
            Number(longitude);

        if (
            !Number.isFinite(lat) ||
            !Number.isFinite(lng)
        ) {
            return;
        }

        updateMapContainerBounds();

        map.relayout();

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

        map.setLevel(
            targetLevel
        );

        map.setCenter(
            position
        );
    };


/* =========================================================
 * LOCATION
 * ========================================================= */

window.kakaoMapRequestLocation =
    function() {

        if (!navigator.geolocation) {

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

    var projection =
        map.getProjection();

    if (!projection) {
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
        return;
    }

    /*
     * 기존 marker + info + cluster list 전부 제거
     */
    clearAllMarkers();

    if (
        !Array.isArray(currentItems) ||
        currentItems.length === 0
    ) {

        return;
    }

    var groups =
        buildClusters(
            currentItems
        );

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

        } else {

            var cluster =
                createCluster(
                    group
                );

            if (cluster) {

                clusterMarkers.push(
                    cluster
                );
            }
        }
    }
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

        /*
         * 예약되어 있는 줌 debounce 제거
         */
        if (zoomRebuildTimer) {

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
        }
    };