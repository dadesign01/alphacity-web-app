//
//  MapView.swift
//  AlphaCityStampTour
//

import SwiftUI
import KakaoMapsSDK
import CoreLocation

// MARK: - Category

private struct MapCategory: Identifiable {
    let id: String
    let label: String
}

private let categories = [
    MapCategory(id: "all", label: "전체"),
    MapCategory(id: "mission", label: "미션"),
    MapCategory(id: "food", label: "맛집"),
    MapCategory(id: "exhibition", label: "전시"),
    MapCategory(id: "seminar", label: "세미나"),
    MapCategory(id: "event", label: "이벤트"),
]

// MARK: - Clustering

private struct MapCluster {
    let center: (lat: Double, lng: Double)
    let programs: [ProgramData]
    var count: Int { programs.count }
    var isSingle: Bool { programs.count == 1 }
}

private func clusterPrograms(_ programs: [ProgramData], zoomLevel: Int) -> [MapCluster] {
    let valid = programs.filter { $0.latitude != nil && $0.longitude != nil }
    guard !valid.isEmpty else { return [] }

    // 줌 18 이상이면 클러스터링 안함
    guard zoomLevel < 18 else {
        return valid.map { MapCluster(center: ($0.latitude!, $0.longitude!), programs: [$0]) }
    }

    let gridSize: Double
    switch zoomLevel {
    case 0...10: gridSize = 0.05
    case 11...12: gridSize = 0.02
    case 13: gridSize = 0.01
    case 14: gridSize = 0.005
    case 15: gridSize = 0.003
    case 16: gridSize = 0.0015
    default: gridSize = 0.0008
    }

    var grid: [String: [ProgramData]] = [:]
    for p in valid {
        let key = "\(Int(floor(p.latitude! / gridSize)))_\(Int(floor(p.longitude! / gridSize)))"
        grid[key, default: []].append(p)
    }

    return grid.values.map { items in
        let avgLat = items.compactMap(\.latitude).reduce(0, +) / Double(items.count)
        let avgLng = items.compactMap(\.longitude).reduce(0, +) / Double(items.count)
        return MapCluster(center: (avgLat, avgLng), programs: items)
    }
}

private struct MapMissionCluster {
    let center: (lat: Double, lng: Double)
    let missions: [MissionData]
    var count: Int { missions.count }
    var isSingle: Bool { missions.count == 1 }
}

private func clusterMissions(_ missions: [MissionData], zoomLevel: Int) -> [MapMissionCluster] {
    let valid = missions.filter { $0.place?.latitude != nil && $0.place?.longitude != nil }
    guard !valid.isEmpty else { return [] }

    guard zoomLevel < 18 else {
        return valid.map { MapMissionCluster(center: ($0.place!.latitude!, $0.place!.longitude!), missions: [$0]) }
    }

    let gridSize: Double
    switch zoomLevel {
    case 0...10: gridSize = 0.05
    case 11...12: gridSize = 0.02
    case 13: gridSize = 0.01
    case 14: gridSize = 0.005
    case 15: gridSize = 0.003
    case 16: gridSize = 0.0015
    default: gridSize = 0.0008
    }

    var grid: [String: [MissionData]] = [:]
    for m in valid {
        let lat = m.place!.latitude!
        let lng = m.place!.longitude!
        let key = "\(Int(floor(lat / gridSize)))_\(Int(floor(lng / gridSize)))"
        grid[key, default: []].append(m)
    }

    return grid.values.map { items in
        let avgLat = items.compactMap { $0.place?.latitude }.reduce(0, +) / Double(items.count)
        let avgLng = items.compactMap { $0.place?.longitude }.reduce(0, +) / Double(items.count)
        return MapMissionCluster(center: (avgLat, avgLng), missions: items)
    }
}

// 클러스터 탭 시 실제로 클러스터가 풀리는 최소 줌 (해제 기준인 18을 넘지 않음)
private func splitZoomForPrograms(_ programs: [ProgramData], from currentZoom: Int) -> Int {
    guard currentZoom < 18 else { return 18 }
    for z in (currentZoom + 1)...18 where clusterPrograms(programs, zoomLevel: z).count > 1 {
        return z
    }
    return 18
}

private func splitZoomForMissions(_ missions: [MissionData], from currentZoom: Int) -> Int {
    guard currentZoom < 18 else { return 18 }
    for z in (currentZoom + 1)...18 where clusterMissions(missions, zoomLevel: z).count > 1 {
        return z
    }
    return 18
}

// MARK: - MapView

struct MapContentView: View {
    @StateObject private var viewModel = MapViewModel()
    var onProgramTapped: ((ProgramData) -> Void)? = nil
    var onStoreTapped: ((StoreData) -> Void)? = nil
    var onProfileTap: (() -> Void)? = nil
    @Binding var focusLat: Double?
    @Binding var focusLng: Double?
    var showBack: Bool = false
    var onBack: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Header
            MapHeaderView(onProfileTap: { onProfileTap?() }, showBack: showBack, onBack: onBack)

            Divider()
                .foregroundColor(Color(hex: "E2E2E2"))

            // Map + Category overlay
            ZStack(alignment: .top) {
                KakaoMapRepresentable(
                    programs: viewModel.filteredPrograms,
                    stores: viewModel.filteredStores,
                    missions: viewModel.filteredMissions,
                    onMarkerTapped: { programId in
                        if let program = viewModel.programs.first(where: { $0.id == programId }) {
                            onProgramTapped?(program)
                        }
                    },
                    onStoreTapped: { storeId in
                        if let store = viewModel.filteredStores.first(where: { $0.id == storeId }) {
                            onStoreTapped?(store)
                        }
                    },
                    focusLat: $focusLat,
                    focusLng: $focusLng,
                    viewModel: viewModel
                )

                // Category filter tabs
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(categories) { cat in
                            let isSelected = viewModel.selectedCategory == cat.id
                            Button {
                                viewModel.selectCategory(cat.id)
                            } label: {
                                Text(cat.label)
                                    .font(AppFont.semibold(14))
                                    .foregroundColor(isSelected ? .white : AppColor.primary)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(
                                        isSelected
                                            ? AnyShapeStyle(AppColor.primary)
                                            : AnyShapeStyle(Color(hex: "FBFCFF"))
                                    )
                                    .clipShape(Capsule())
                                    .overlay(
                                        Capsule()
                                            .stroke(isSelected ? Color.clear : AppColor.primary, lineWidth: 1)
                                    )
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 2)
                }
                .padding(.top, 15)
            }
        }
        .onAppear {
            viewModel.fetchPrograms()
            viewModel.fetchStores()
            viewModel.fetchMissions()
        }
    }
}

// MARK: - KakaoMapRepresentable

struct KakaoMapRepresentable: UIViewRepresentable {
    let programs: [ProgramData]
    let stores: [StoreData]
    let missions: [MissionData]
    var onMarkerTapped: ((Int) -> Void)?
    var onStoreTapped: ((Int) -> Void)?
    @Binding var focusLat: Double?
    @Binding var focusLng: Double?
    var viewModel: MapViewModel?

    func makeCoordinator() -> Coordinator {
        Coordinator(programs: programs, stores: stores, missions: missions, onMarkerTapped: onMarkerTapped, onStoreTapped: onStoreTapped, viewModel: viewModel)
    }

    func makeUIView(context: Context) -> UIView {
        let wrapper = MapWrapperView()
        wrapper.coordinator = context.coordinator
        return wrapper
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.programs = programs
        context.coordinator.stores = stores
        context.coordinator.missions = missions
        context.coordinator.onMarkerTapped = onMarkerTapped
        context.coordinator.onStoreTapped = onStoreTapped
        context.coordinator.updateMarkers()
        context.coordinator.updateStoreMarkers()
        context.coordinator.updateMissionMarkers()

        if let lat = focusLat, let lng = focusLng {
            context.coordinator.markCenteredByFocus()
            context.coordinator.moveCameraTo(lat: lat, lng: lng)
            DispatchQueue.main.async {
                focusLat = nil
                focusLng = nil
            }
        }

    }

    // MARK: - MapWrapperView

    class MapWrapperView: UIView {
        weak var coordinator: Coordinator?
        private var mapContainer: KMViewContainer?

        override func didMoveToWindow() {
            super.didMoveToWindow()
            guard window != nil, mapContainer == nil else { return }
            setupMap()
        }

        private func setupMap() {
            let container = KMViewContainer(frame: bounds)
            container.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            addSubview(container)
            mapContainer = container
            coordinator?.container = container
            coordinator?.createController()
        }

        override func layoutSubviews() {
            super.layoutSubviews()
            mapContainer?.frame = bounds
        }
    }

    // MARK: - Coordinator

    class Coordinator: NSObject, MapControllerDelegate, KakaoMapEventDelegate, CLLocationManagerDelegate {
        var container: KMViewContainer?
        var controller: KMController?
        var kakaoMap: KakaoMap?
        var programs: [ProgramData]
        var stores: [StoreData]
        var missions: [MissionData]
        var onMarkerTapped: ((Int) -> Void)?
        var onStoreTapped: ((Int) -> Void)?
        weak var viewModel: MapViewModel?
        private var isMapReady = false
        private var currentZoomLevel: Int = 15
        private var clusters: [MapCluster] = []
        private var missionClusters: [MapMissionCluster] = []
        private var zoomCheckTimer: Timer?
        // 현재 위치(GPS)
        private let locationManager = CLLocationManager()
        private var lastUserLocation: (lat: Double, lng: Double)?
        private var didCenterOnUser = false
        // 포커스/GPS/저장 카메라가 없을 때 데이터 위치로 1회 자동 센터링했는지
        private var didCenterOnData = false
        private var didRestoreSavedCamera = false

        deinit {
            zoomCheckTimer?.invalidate()
        }

        init(programs: [ProgramData], stores: [StoreData], missions: [MissionData], onMarkerTapped: ((Int) -> Void)?, onStoreTapped: ((Int) -> Void)?, viewModel: MapViewModel?) {
            self.programs = programs
            self.stores = stores
            self.missions = missions
            self.onMarkerTapped = onMarkerTapped
            self.onStoreTapped = onStoreTapped
            self.viewModel = viewModel
        }

        func createController() {
            guard let container else { return }
            controller = KMController(viewContainer: container)
            controller?.delegate = self
            controller?.prepareEngine()
            // 현재 위치 권한 요청 시작
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
            locationManager.requestWhenInUseAuthorization()
        }

        // KMControllerDelegate
        func addViews() {
            // 저장된 카메라 위치가 있으면 복원, 없으면 기본 위치
            let defaultPosition: MapPoint
            let defaultLevel: Int
            // KakaoMap 델리게이트 콜백은 메인 스레드에서 호출되므로 MainActor 격리를 가정해 viewModel 접근
            let savedCamera: (lat: Double, lng: Double, zoom: Int)? = MainActor.assumeIsolated {
                guard let vm = viewModel, vm.hasSavedCameraPosition,
                      let lat = vm.savedCameraLat, let lng = vm.savedCameraLng, let zoom = vm.savedCameraZoom else {
                    return nil
                }
                return (lat, lng, zoom)
            }
            if let savedCamera {
                defaultPosition = MapPoint(longitude: savedCamera.lng, latitude: savedCamera.lat)
                defaultLevel = savedCamera.zoom
                currentZoomLevel = savedCamera.zoom
                didRestoreSavedCamera = true
            } else {
                defaultPosition = MapPoint(longitude: 128.690, latitude: 35.842)
                defaultLevel = 15
            }
            let mapviewInfo = MapviewInfo(
                viewName: "mapview",
                viewInfoName: "map",
                defaultPosition: defaultPosition,
                defaultLevel: defaultLevel
            )
            controller?.addView(mapviewInfo)
        }

        func addViewSucceeded(_ viewName: String, viewInfoName: String) {
            guard let mapView = controller?.getView("mapview") as? KakaoMap else { return }
            kakaoMap = mapView
            isMapReady = true
            mapView.eventDelegate = self

            // 카카오 기본 POI(식당/상호명 등) 라벨 숨김 — 우리 마커만 표시되도록.
            // (확대 시 주변 상호명이 계속 바뀌어 마커 라벨처럼 보이는 문제 방지)
            mapView.setPoiEnabled(false)

            currentZoomLevel = Int(mapView.zoomLevel)
            updateMarkers()
            updateStoreMarkers()
            updateMissionMarkers()
            centerOnUserIfNeeded()

            // 줌 변경 감지 타이머 (0.3초 간격) + 카메라 위치 저장
            zoomCheckTimer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { [weak self] _ in
                guard let self, let map = self.kakaoMap else { return }
                let newZoom = Int(map.zoomLevel)

                // 카메라 중심 좌표를 ViewModel에 저장 (네비게이션 복귀 시 복원용)
                let viewRect = map.viewRect
                let centerPoint = CGPoint(x: viewRect.midX, y: viewRect.midY)
                let centerMapPoint = map.getPosition(centerPoint)
                let coord = centerMapPoint.wgsCoord
                Task { @MainActor [weak self] in
                    self?.viewModel?.saveCameraPosition(
                        lat: coord.latitude,
                        lng: coord.longitude,
                        zoom: newZoom
                    )
                }

                if newZoom != self.currentZoomLevel {
                    self.currentZoomLevel = newZoom
                    self.updateMarkers()
                    self.updateStoreMarkers()
                    self.updateMissionMarkers()
                }
            }
        }

        func addViewFailed(_ viewName: String, viewInfoName: String) {
            print("[MapView] addView 실패: \(viewName)")
        }

        func containerDidResized(_ size: CGSize) {
            controller?.getView("mapview")?.viewRect = CGRect(origin: .zero, size: size)
            if controller?.isEnginePrepared == false {
                controller?.prepareEngine()
            } else if controller?.isEngineActive == false {
                controller?.activateEngine()
            }
        }

        func authenticationSucceeded() {
            print("[MapView] 카카오맵 인증 성공")
            controller?.activateEngine()
        }

        func authenticationFailed(_ errorCode: Int, desc: String) {
            print("[MapView] 카카오맵 인증 실패: \(errorCode) - \(desc)")
        }

        // MARK: - KakaoMapEventDelegate (Poi 탭)

        func poiDidTapped(kakaoMap: KakaoMap, layerID: String, poiID: String, position: MapPoint) {
            // 상점 마커 클릭
            if layerID == "storeMarkers" {
                let idx = poiID.replacingOccurrences(of: "store_", with: "")
                let validStores = stores.filter { $0.latitude != nil && $0.longitude != nil }
                guard let index = Int(idx), index < validStores.count else { return }
                let store = validStores[index]
                onStoreTapped?(store.id)
                return
            }

            // 미션 마커 클릭
            if layerID == "missionMarkers" {
                let idx = poiID.replacingOccurrences(of: "mission_", with: "")
                guard let index = Int(idx), index < missionClusters.count else { return }
                let cluster = missionClusters[index]
                if cluster.isSingle, let mission = cluster.missions.first {
                    // 미션이 속한 프로그램 상세로 이동 (없으면 해당 위치 줌인)
                    if let programId = mission.programId,
                       programs.contains(where: { $0.id == programId }) {
                        onMarkerTapped?(programId)
                    } else {
                        let pos = MapPoint(longitude: cluster.center.lng, latitude: cluster.center.lat)
                        let cameraUpdate = CameraUpdate.make(target: pos, zoomLevel: 18, mapView: kakaoMap)
                        kakaoMap.moveCamera(cameraUpdate, callback: nil)
                    }
                } else {
                    let pos = MapPoint(longitude: cluster.center.lng, latitude: cluster.center.lat)
                    let newZoom = splitZoomForMissions(cluster.missions, from: currentZoomLevel)
                    let cameraUpdate = CameraUpdate.make(target: pos, zoomLevel: newZoom, mapView: kakaoMap)
                    kakaoMap.moveCamera(cameraUpdate, callback: nil)
                }
                return
            }

            // 프로그램 마커 클릭
            guard layerID == "programMarkers" else { return }
            let idx = poiID.replacingOccurrences(of: "poi_", with: "")
            guard let index = Int(idx), index < clusters.count else { return }
            let cluster = clusters[index]

            if cluster.isSingle, let program = cluster.programs.first {
                onMarkerTapped?(program.id)
            } else {
                // 클러스터 탭 -> 클러스터가 풀리는 줌으로 이동
                let pos = MapPoint(longitude: cluster.center.lng, latitude: cluster.center.lat)
                let newZoom = splitZoomForPrograms(cluster.programs, from: currentZoomLevel)
                let cameraUpdate = CameraUpdate.make(target: pos, zoomLevel: newZoom, mapView: kakaoMap)
                kakaoMap.moveCamera(cameraUpdate, callback: nil)
            }
        }

        func terrainDidTapped(kakaoMap: KakaoMap, position: MapPoint) {
            // 빈 영역 탭 - 무시
        }

        func moveCameraTo(lat: Double, lng: Double, zoom: Int = 17) {
            guard let map = kakaoMap else { return }
            let pos = MapPoint(longitude: lng, latitude: lat)
            let cameraUpdate = CameraUpdate.make(target: pos, zoomLevel: zoom, mapView: map)
            map.moveCamera(cameraUpdate, callback: nil)
        }

        // focus 좌표로 이동했으면 현재위치 자동이동 생략 표시
        func markCenteredByFocus() {
            didCenterOnUser = true
        }

        // MARK: - 현재 위치 (CLLocationManagerDelegate)

        func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
            switch manager.authorizationStatus {
            case .authorizedWhenInUse, .authorizedAlways:
                manager.requestLocation()
            default:
                break
            }
        }

        func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
            guard let loc = locations.last else { return }
            lastUserLocation = (loc.coordinate.latitude, loc.coordinate.longitude)
            centerOnUserIfNeeded()
        }

        func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
            // 위치 획득 실패 시 기본 위치 유지
            print("[MapView] 위치 획득 실패: \(error.localizedDescription)")
        }

        // 맵·위치 준비되면 내 위치 점 갱신 + (focus 없을 때) 최초 1회 현재 위치 중심으로
        func centerOnUserIfNeeded() {
            guard isMapReady, let loc = lastUserLocation else { return }
            updateLocationMarker(lat: loc.lat, lng: loc.lng)
            if !didCenterOnUser {
                didCenterOnUser = true
                moveCameraTo(lat: loc.lat, lng: loc.lng, zoom: 16)
            }
        }

        // 현재 위치 파란 점 마커 (Android와 동일 디자인)
        func updateLocationMarker(lat: Double, lng: Double) {
            guard isMapReady, let map = kakaoMap else { return }
            let manager = map.getLabelManager()
            manager.removeLabelLayer(layerID: "userLocation")
            let layerOption = LabelLayerOptions(
                layerID: "userLocation",
                competitionType: .none,
                competitionUnit: .symbolFirst,
                orderType: .rank,
                zOrder: 10003
            )
            guard let layer = manager.addLabelLayer(option: layerOption) else { return }
            let styleID = "userLocationStyle"
            let iconStyle = PoiIconStyle(symbol: createLocationMarkerImage(), anchorPoint: CGPoint(x: 0.5, y: 0.5))
            let poiStyle = PoiStyle(styleID: styleID, styles: [PerLevelPoiStyle(iconStyle: iconStyle, level: 0)])
            manager.addPoiStyle(poiStyle)
            let options = PoiOptions(styleID: styleID, poiID: "user_location")
            if let poi = layer.addPoi(option: options, at: MapPoint(longitude: lng, latitude: lat)) {
                poi.show()
            }
        }

        // 내 위치 점 이미지 (반투명 외곽 14 + 흰 테두리 10 + 파란 점 8)
        private func createLocationMarkerImage() -> UIImage {
            let size: CGFloat = 28
            let renderer = UIGraphicsImageRenderer(size: CGSize(width: size, height: size))
            return renderer.image { _ in
                let center = CGPoint(x: size / 2, y: size / 2)
                let blue = UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1)
                blue.withAlphaComponent(0.25).setFill()
                UIBezierPath(arcCenter: center, radius: 14, startAngle: 0, endAngle: .pi * 2, clockwise: true).fill()
                UIColor.white.setFill()
                UIBezierPath(arcCenter: center, radius: 10, startAngle: 0, endAngle: .pi * 2, clockwise: true).fill()
                blue.setFill()
                UIBezierPath(arcCenter: center, radius: 8, startAngle: 0, endAngle: .pi * 2, clockwise: true).fill()
            }
        }

        func updateMarkers() {
            guard isMapReady, let map = kakaoMap else { return }

            let manager = map.getLabelManager()
            manager.removeLabelLayer(layerID: "programMarkers")

            clusters = clusterPrograms(programs, zoomLevel: currentZoomLevel)
            guard !clusters.isEmpty else { return }

            let layerOption = LabelLayerOptions(
                layerID: "programMarkers",
                competitionType: .none,
                competitionUnit: .symbolFirst,
                orderType: .rank,
                zOrder: 10001
            )
            guard let layer = manager.addLabelLayer(option: layerOption) else { return }

            for (index, cluster) in clusters.enumerated() {
                let position = MapPoint(longitude: cluster.center.lng, latitude: cluster.center.lat)
                let styleID: String
                let markerImage: UIImage

                if cluster.isSingle {
                    styleID = "single_\(index)"
                    let program = cluster.programs.first
                    let markerName: String
                    if program?.category == "food", let storeName = program?.stores?.first?.name {
                        markerName = storeName
                    } else {
                        markerName = program?.name ?? ""
                    }
                    markerImage = createNameBubbleImage(name: markerName)
                } else {
                    styleID = "cluster_\(index)"
                    markerImage = createClusterImage(count: cluster.count)
                }

                let iconStyle = PoiIconStyle(symbol: markerImage, anchorPoint: CGPoint(x: 0.5, y: 1.0))
                let poiStyle = PoiStyle(styleID: styleID, styles: [
                    PerLevelPoiStyle(iconStyle: iconStyle, level: 0)
                ])
                manager.addPoiStyle(poiStyle)

                let options = PoiOptions(styleID: styleID, poiID: "poi_\(index)")
                options.rank = clusters.count - index

                if let poi = layer.addPoi(option: options, at: position) {
                    poi.clickable = true
                    poi.show()
                }
            }

            // 포커스/GPS/저장 카메라가 없으면 기본 좌표 대신 등록된 프로그램 위치로 1회 이동
            if !didCenterOnData && !didCenterOnUser && !didRestoreSavedCamera && lastUserLocation == nil {
                let valid = programs.filter { $0.latitude != nil && $0.longitude != nil }
                if !valid.isEmpty {
                    let avgLat = valid.compactMap(\.latitude).reduce(0, +) / Double(valid.count)
                    let avgLng = valid.compactMap(\.longitude).reduce(0, +) / Double(valid.count)
                    let pos = MapPoint(longitude: avgLng, latitude: avgLat)
                    let cameraUpdate = CameraUpdate.make(target: pos, zoomLevel: 15, mapView: map)
                    map.moveCamera(cameraUpdate, callback: nil)
                    currentZoomLevel = 15
                    didCenterOnData = true
                }
            }
        }

        func updateStoreMarkers() {
            guard isMapReady, let map = kakaoMap else { return }

            let manager = map.getLabelManager()
            manager.removeLabelLayer(layerID: "storeMarkers")

            let validStores = stores.filter { $0.latitude != nil && $0.longitude != nil }
            guard !validStores.isEmpty else { return }

            let layerOption = LabelLayerOptions(
                layerID: "storeMarkers",
                competitionType: .none,
                competitionUnit: .symbolFirst,
                orderType: .rank,
                zOrder: 10002
            )
            guard let layer = manager.addLabelLayer(option: layerOption) else { return }

            for (index, store) in validStores.enumerated() {
                let position = MapPoint(longitude: store.longitude!, latitude: store.latitude!)
                let styleID = "store_style_\(index)"
                let markerImage = createNameBubbleImage(name: store.name)

                let iconStyle = PoiIconStyle(symbol: markerImage, anchorPoint: CGPoint(x: 0.5, y: 1.0))
                let poiStyle = PoiStyle(styleID: styleID, styles: [
                    PerLevelPoiStyle(iconStyle: iconStyle, level: 0)
                ])
                manager.addPoiStyle(poiStyle)

                let options = PoiOptions(styleID: styleID, poiID: "store_\(index)")
                options.rank = validStores.count - index

                if let poi = layer.addPoi(option: options, at: position) {
                    poi.clickable = true
                    poi.show()
                }
            }
        }

        func updateMissionMarkers() {
            guard isMapReady, let map = kakaoMap else { return }

            let manager = map.getLabelManager()
            manager.removeLabelLayer(layerID: "missionMarkers")

            missionClusters = clusterMissions(missions, zoomLevel: currentZoomLevel)
            guard !missionClusters.isEmpty else { return }

            let layerOption = LabelLayerOptions(
                layerID: "missionMarkers",
                competitionType: .none,
                competitionUnit: .symbolFirst,
                orderType: .rank,
                zOrder: 10000
            )
            guard let layer = manager.addLabelLayer(option: layerOption) else { return }

            for (index, cluster) in missionClusters.enumerated() {
                let position = MapPoint(longitude: cluster.center.lng, latitude: cluster.center.lat)
                let styleID: String
                let markerImage: UIImage
                let anchor: CGPoint

                if cluster.isSingle {
                    styleID = "mission_single_\(index)"
                    markerImage = createMissionBubbleImage(name: cluster.missions.first?.name ?? "")
                    anchor = CGPoint(x: 0.5, y: 1.0)
                } else {
                    styleID = "mission_cluster_\(index)"
                    markerImage = createMissionClusterImage(count: cluster.count)
                    anchor = CGPoint(x: 0.5, y: 0.5)
                }

                let iconStyle = PoiIconStyle(symbol: markerImage, anchorPoint: anchor)
                let poiStyle = PoiStyle(styleID: styleID, styles: [
                    PerLevelPoiStyle(iconStyle: iconStyle, level: 0)
                ])
                manager.addPoiStyle(poiStyle)

                let options = PoiOptions(styleID: styleID, poiID: "mission_\(index)")
                options.rank = missionClusters.count - index

                if let poi = layer.addPoi(option: options, at: position) {
                    poi.clickable = true
                    poi.show()
                }
            }
        }

        // MARK: - Marker Images

        // 원본 비율: 1443x1152 (가로:세로 = 5:4)
        private let duckW: CGFloat = 36
        private let duckH: CGFloat = 29  // 36 * 1152 / 1443
        private var cachedDuckImage: UIImage?

        private func getDuckImage() -> UIImage? {
            if let cached = cachedDuckImage { return cached }
            guard let src = UIImage(named: "MapMarker") else { return nil }
            let size = CGSize(width: duckW, height: duckH)
            let renderer = UIGraphicsImageRenderer(size: size)
            let scaled = renderer.image { _ in
                src.draw(in: CGRect(origin: .zero, size: size))
            }
            cachedDuckImage = scaled
            return scaled
        }

        private func createClusterImage(count: Int) -> UIImage {
            guard let duck = getDuckImage() else {
                // 폴백: 파란 원
                return UIGraphicsImageRenderer(size: CGSize(width: 36, height: 36)).image { _ in
                    UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1).setFill()
                    UIBezierPath(ovalIn: CGRect(x: 0, y: 0, width: 36, height: 36)).fill()
                }
            }

            let badgeRadius: CGFloat = 9
            let badgeBorder: CGFloat = 2
            let extra = (badgeRadius + badgeBorder) * 0.7
            let totalW = duckW + extra
            let totalH = duckH + extra

            let renderer = UIGraphicsImageRenderer(size: CGSize(width: totalW, height: totalH))
            return renderer.image { _ in
                // 오리
                duck.draw(in: CGRect(x: 0, y: 0, width: duckW, height: duckH))

                // 배지 위치: 오른쪽 아래
                let badgeCx = duckW - badgeRadius * 0.2
                let badgeCy = duckH - badgeRadius * 0.2

                // 흰 테두리
                UIColor.white.setFill()
                UIBezierPath(
                    arcCenter: CGPoint(x: badgeCx, y: badgeCy),
                    radius: badgeRadius + badgeBorder,
                    startAngle: 0, endAngle: .pi * 2, clockwise: true
                ).fill()

                // 파란 배지
                UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1).setFill()
                UIBezierPath(
                    arcCenter: CGPoint(x: badgeCx, y: badgeCy),
                    radius: badgeRadius,
                    startAngle: 0, endAngle: .pi * 2, clockwise: true
                ).fill()

                // 숫자
                let text = "\(count)" as NSString
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 10),
                    .foregroundColor: UIColor.white,
                ]
                let textSize = text.size(withAttributes: attrs)
                text.draw(at: CGPoint(
                    x: badgeCx - textSize.width / 2,
                    y: badgeCy - textSize.height / 2
                ), withAttributes: attrs)
            }
        }

        private func createNameBubbleImage(name: String) -> UIImage {
            guard let duck = getDuckImage() else { return UIImage() }

            let textAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 11, weight: .medium),
                .foregroundColor: UIColor(red: 18/255, green: 18/255, blue: 18/255, alpha: 1),
            ]
            let textNS = name as NSString
            let textSize = textNS.size(withAttributes: textAttrs)
            let paddingH: CGFloat = 10
            let paddingV: CGFloat = 6
            let boxWidth = textSize.width + paddingH * 2
            let boxHeight = textSize.height + paddingV * 2
            let cornerRadius: CGFloat = 7
            let pointerH: CGFloat = 5
            let shadowPad: CGFloat = 5

            let totalW = max(boxWidth + shadowPad * 2, duckW)
            let totalH = shadowPad + boxHeight + pointerH + duckH

            let renderer = UIGraphicsImageRenderer(size: CGSize(width: totalW, height: totalH))
            return renderer.image { ctx in
                let cgCtx = ctx.cgContext
                let cx = totalW / 2

                let boxLeft = cx - boxWidth / 2
                let boxTop = shadowPad
                let boxRect = CGRect(x: boxLeft, y: boxTop, width: boxWidth, height: boxHeight)

                // 그림자 설정
                cgCtx.setShadow(offset: CGSize(width: 0, height: 2), blur: 4,
                                color: UIColor.black.withAlphaComponent(0.2).cgColor)

                // 흰 배경
                UIColor.white.setFill()
                UIBezierPath(roundedRect: boxRect, cornerRadius: cornerRadius).fill()

                // 그림자 해제 후 포인터
                cgCtx.setShadow(offset: .zero, blur: 0, color: nil)
                let pointerY = boxTop + boxHeight
                let pointer = UIBezierPath()
                pointer.move(to: CGPoint(x: cx - 4, y: pointerY))
                pointer.addLine(to: CGPoint(x: cx, y: pointerY + pointerH))
                pointer.addLine(to: CGPoint(x: cx + 4, y: pointerY))
                pointer.close()
                UIColor.white.setFill()
                pointer.fill()

                // 텍스트
                textNS.draw(at: CGPoint(x: boxLeft + paddingH, y: boxTop + paddingV),
                            withAttributes: textAttrs)

                // 오리 이미지
                let duckLeft = (totalW - duckW) / 2
                let duckTop = boxTop + boxHeight + pointerH
                duck.draw(in: CGRect(x: duckLeft, y: duckTop, width: duckW, height: duckH))
            }
        }

        private func createStoreNameBubbleImage(name: String) -> UIImage {
            guard let duck = getDuckImage() else { return UIImage() }

            let textAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 11, weight: .medium),
                .foregroundColor: UIColor.white,
            ]
            let textNS = name as NSString
            let textSize = textNS.size(withAttributes: textAttrs)
            let paddingH: CGFloat = 10
            let paddingV: CGFloat = 6
            let boxWidth = textSize.width + paddingH * 2
            let boxHeight = textSize.height + paddingV * 2
            let cornerRadius: CGFloat = 7
            let pointerH: CGFloat = 5
            let shadowPad: CGFloat = 5

            let totalW = max(boxWidth + shadowPad * 2, duckW)
            let totalH = shadowPad + boxHeight + pointerH + duckH

            let storeGreen = UIColor(red: 22/255, green: 163/255, blue: 74/255, alpha: 1)

            let renderer = UIGraphicsImageRenderer(size: CGSize(width: totalW, height: totalH))
            return renderer.image { ctx in
                let cgCtx = ctx.cgContext
                let cx = totalW / 2

                let boxLeft = cx - boxWidth / 2
                let boxTop = shadowPad
                let boxRect = CGRect(x: boxLeft, y: boxTop, width: boxWidth, height: boxHeight)

                // 그림자 설정
                cgCtx.setShadow(offset: CGSize(width: 0, height: 2), blur: 4,
                                color: UIColor.black.withAlphaComponent(0.2).cgColor)

                // 녹색 배경 (상점 구분)
                storeGreen.setFill()
                UIBezierPath(roundedRect: boxRect, cornerRadius: cornerRadius).fill()

                // 그림자 해제 후 포인터
                cgCtx.setShadow(offset: .zero, blur: 0, color: nil)
                let pointerY = boxTop + boxHeight
                let pointer = UIBezierPath()
                pointer.move(to: CGPoint(x: cx - 4, y: pointerY))
                pointer.addLine(to: CGPoint(x: cx, y: pointerY + pointerH))
                pointer.addLine(to: CGPoint(x: cx + 4, y: pointerY))
                pointer.close()
                storeGreen.setFill()
                pointer.fill()

                // 텍스트 (흰색)
                textNS.draw(at: CGPoint(x: boxLeft + paddingH, y: boxTop + paddingV),
                            withAttributes: textAttrs)

                // 오리 이미지
                let duckLeft = (totalW - duckW) / 2
                let duckTop = boxTop + boxHeight + pointerH
                duck.draw(in: CGRect(x: duckLeft, y: duckTop, width: duckW, height: duckH))
            }
        }

        // 미션 이름 말풍선 (보라 #4C27D0, Android와 동일)
        private func createMissionBubbleImage(name: String) -> UIImage {
            let textAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 11, weight: .semibold),
                .foregroundColor: UIColor.white,
            ]
            let textNS = name as NSString
            let textSize = textNS.size(withAttributes: textAttrs)
            let paddingH: CGFloat = 10
            let paddingV: CGFloat = 6
            let boxWidth = textSize.width + paddingH * 2
            let boxHeight = textSize.height + paddingV * 2
            let cornerRadius: CGFloat = 7
            let pointerH: CGFloat = 5
            let shadowPad: CGFloat = 5

            let totalW = boxWidth + shadowPad * 2
            let totalH = shadowPad + boxHeight + pointerH

            let missionPurple = UIColor(red: 76/255, green: 39/255, blue: 208/255, alpha: 1)

            let renderer = UIGraphicsImageRenderer(size: CGSize(width: totalW, height: totalH))
            return renderer.image { ctx in
                let cgCtx = ctx.cgContext
                let cx = totalW / 2

                let boxLeft = cx - boxWidth / 2
                let boxTop = shadowPad
                let boxRect = CGRect(x: boxLeft, y: boxTop, width: boxWidth, height: boxHeight)

                cgCtx.setShadow(offset: CGSize(width: 0, height: 2), blur: 4,
                                color: UIColor.black.withAlphaComponent(0.2).cgColor)
                missionPurple.setFill()
                UIBezierPath(roundedRect: boxRect, cornerRadius: cornerRadius).fill()

                cgCtx.setShadow(offset: .zero, blur: 0, color: nil)
                let pointerY = boxTop + boxHeight
                let pointer = UIBezierPath()
                pointer.move(to: CGPoint(x: cx - 4, y: pointerY))
                pointer.addLine(to: CGPoint(x: cx, y: pointerY + pointerH))
                pointer.addLine(to: CGPoint(x: cx + 4, y: pointerY))
                pointer.close()
                missionPurple.setFill()
                pointer.fill()

                textNS.draw(at: CGPoint(x: boxLeft + paddingH, y: boxTop + paddingV),
                            withAttributes: textAttrs)
            }
        }

        // 미션 클러스터 배지 (보라 원 + 카운트)
        private func createMissionClusterImage(count: Int) -> UIImage {
            let radius: CGFloat = 15
            let border: CGFloat = 2
            let size = (radius + border) * 2
            let missionPurple = UIColor(red: 76/255, green: 39/255, blue: 208/255, alpha: 1)

            let renderer = UIGraphicsImageRenderer(size: CGSize(width: size, height: size))
            return renderer.image { _ in
                let center = CGPoint(x: size / 2, y: size / 2)
                UIColor.white.setFill()
                UIBezierPath(arcCenter: center, radius: radius + border, startAngle: 0, endAngle: .pi * 2, clockwise: true).fill()
                missionPurple.setFill()
                UIBezierPath(arcCenter: center, radius: radius, startAngle: 0, endAngle: .pi * 2, clockwise: true).fill()

                let text = "\(count)" as NSString
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 12),
                    .foregroundColor: UIColor.white,
                ]
                let textSize = text.size(withAttributes: attrs)
                text.draw(at: CGPoint(x: center.x - textSize.width / 2, y: center.y - textSize.height / 2), withAttributes: attrs)
            }
        }
    }
}

// MARK: - MapHeaderView

private struct MapHeaderView: View {
    var onProfileTap: () -> Void
    var showBack: Bool = false
    var onBack: (() -> Void)? = nil

    var body: some View {
        HStack(spacing: 8) {
            if showBack {
                // 축제/행사 상세에서 '지도에서 보기'로 진입: 로고 대신 뒤로가기 + 제목
                Button(action: { onBack?() }) {
                    Image("IconBackArrow")
                        .renderingMode(.original)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 13, height: 26)
                }
                .padding(.trailing, 6)

                Text("지도 상세보기")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                    .tracking(-0.36)

                Spacer()
            } else {
                // 하단 탭 '지도'로 진입: 로고 + 프로필
                Image("HeaderLogo")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 40, height: 40)
                    .clipShape(RoundedRectangle(cornerRadius: 10))

                Text("올리모아")
                    .font(AppFont.semibold(18))
                    .foregroundColor(Color(hex: "121212"))
                    .tracking(-0.36)

                Spacer()

                Button(action: onProfileTap) {
                    Image("IconProfile")
                        .resizable()
                        .scaledToFill()
                        .frame(width: 40, height: 40)
                        .clipShape(Circle())
                        .overlay(
                            Circle()
                                .stroke(Color(hex: "EBEBEB"), lineWidth: 1)
                        )
                }
            }
        }
        .padding(.leading, 15)
        .padding(.trailing, 20)
        .padding(.vertical, 12)
        .background(Color.white)
    }
}
