//
//  MapView.swift
//  AlphaCityStampTour
//

import SwiftUI
import KakaoMapsSDK

// MARK: - Category

private struct MapCategory: Identifiable {
    let id: String
    let label: String
}

private let categories = [
    MapCategory(id: "all", label: "전체"),
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

// MARK: - MapView

struct MapContentView: View {
    @StateObject private var viewModel = MapViewModel()
    var onProgramTapped: ((ProgramData) -> Void)? = nil
    var onProfileTap: (() -> Void)? = nil
    @Binding var focusLat: Double?
    @Binding var focusLng: Double?
    @State private var storeDetailProgram: ProgramData? = nil

    var body: some View {
        VStack(spacing: 0) {
            // Header
            MapHeaderView(onProfileTap: { onProfileTap?() })

            Divider()
                .foregroundColor(Color(hex: "E2E2E2"))

            // Map + Category overlay
            ZStack(alignment: .top) {
                KakaoMapRepresentable(
                    programs: viewModel.filteredPrograms,
                    onMarkerTapped: { programId in
                        if let program = viewModel.filteredPrograms.first(where: { $0.id == programId }) {
                            if program.category == "food" {
                                storeDetailProgram = program
                            } else {
                                onProgramTapped?(program)
                            }
                        }
                    },
                    focusLat: $focusLat,
                    focusLng: $focusLng
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
        }
        .sheet(item: $storeDetailProgram) { program in
            StoreDetailSheet(
                program: program,
                onDismiss: { storeDetailProgram = nil }
            )
            .presentationDetents([.large])
        }
    }
}

// MARK: - KakaoMapRepresentable

struct KakaoMapRepresentable: UIViewRepresentable {
    let programs: [ProgramData]
    var onMarkerTapped: ((Int) -> Void)?
    @Binding var focusLat: Double?
    @Binding var focusLng: Double?

    func makeCoordinator() -> Coordinator {
        Coordinator(programs: programs, onMarkerTapped: onMarkerTapped)
    }

    func makeUIView(context: Context) -> UIView {
        let wrapper = MapWrapperView()
        wrapper.coordinator = context.coordinator
        return wrapper
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.programs = programs
        context.coordinator.onMarkerTapped = onMarkerTapped
        context.coordinator.updateMarkers()

        if let lat = focusLat, let lng = focusLng {
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

    class Coordinator: NSObject, MapControllerDelegate, KakaoMapEventDelegate {
        var container: KMViewContainer?
        var controller: KMController?
        var kakaoMap: KakaoMap?
        var programs: [ProgramData]
        var onMarkerTapped: ((Int) -> Void)?
        private var isMapReady = false
        private var currentZoomLevel: Int = 15
        private var clusters: [MapCluster] = []
        private var zoomCheckTimer: Timer?

        deinit {
            zoomCheckTimer?.invalidate()
        }

        init(programs: [ProgramData], onMarkerTapped: ((Int) -> Void)?) {
            self.programs = programs
            self.onMarkerTapped = onMarkerTapped
        }

        func createController() {
            guard let container else { return }
            controller = KMController(viewContainer: container)
            controller?.delegate = self
            controller?.prepareEngine()
        }

        // KMControllerDelegate
        func addViews() {
            let defaultPosition = MapPoint(longitude: 128.690, latitude: 35.842)
            let mapviewInfo = MapviewInfo(
                viewName: "mapview",
                viewInfoName: "map",
                defaultPosition: defaultPosition,
                defaultLevel: 15
            )
            controller?.addView(mapviewInfo)
        }

        func addViewSucceeded(_ viewName: String, viewInfoName: String) {
            guard let mapView = controller?.getView("mapview") as? KakaoMap else { return }
            kakaoMap = mapView
            isMapReady = true
            mapView.eventDelegate = self

            currentZoomLevel = Int(mapView.zoomLevel)
            updateMarkers()

            // 줌 변경 감지 타이머 (0.3초 간격)
            zoomCheckTimer = Timer.scheduledTimer(withTimeInterval: 0.3, repeats: true) { [weak self] _ in
                guard let self, let map = self.kakaoMap else { return }
                let newZoom = Int(map.zoomLevel)
                if newZoom != self.currentZoomLevel {
                    self.currentZoomLevel = newZoom
                    self.updateMarkers()
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
            guard layerID == "programMarkers" else { return }
            let idx = poiID.replacingOccurrences(of: "poi_", with: "")
            guard let index = Int(idx), index < clusters.count else { return }
            let cluster = clusters[index]

            if cluster.isSingle, let program = cluster.programs.first {
                onMarkerTapped?(program.id)
            } else {
                // 클러스터 탭 → 줌 인
                let pos = MapPoint(longitude: cluster.center.lng, latitude: cluster.center.lat)
                let newZoom = min(currentZoomLevel + 2, 17)
                let cameraUpdate = CameraUpdate.make(target: pos, zoomLevel: newZoom, mapView: kakaoMap)
                kakaoMap.moveCamera(cameraUpdate, callback: nil)
            }
        }

        func terrainDidTapped(kakaoMap: KakaoMap, position: MapPoint) {
            // 빈 영역 탭 - 무시
        }

        func moveCameraTo(lat: Double, lng: Double) {
            guard let map = kakaoMap else { return }
            let pos = MapPoint(longitude: lng, latitude: lat)
            let cameraUpdate = CameraUpdate.make(target: pos, zoomLevel: 17, mapView: map)
            map.moveCamera(cameraUpdate, callback: nil)
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
                    markerImage = createNameBubbleImage(name: cluster.programs.first?.name ?? "")
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
        }

        // MARK: - Marker Images

        private func createClusterImage(count: Int) -> UIImage {
            let radius: CGFloat = 22
            let size = CGSize(width: radius * 2, height: radius * 2)

            let renderer = UIGraphicsImageRenderer(size: size)
            return renderer.image { _ in
                // 외곽 반투명 원
                UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 0.3).setFill()
                UIBezierPath(
                    arcCenter: CGPoint(x: radius, y: radius),
                    radius: radius,
                    startAngle: 0, endAngle: .pi * 2, clockwise: true
                ).fill()

                // 내부 원
                UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1).setFill()
                UIBezierPath(
                    arcCenter: CGPoint(x: radius, y: radius),
                    radius: 16,
                    startAngle: 0, endAngle: .pi * 2, clockwise: true
                ).fill()

                // 숫자
                let text = "\(count)" as NSString
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 14),
                    .foregroundColor: UIColor.white,
                ]
                let textSize = text.size(withAttributes: attrs)
                text.draw(
                    at: CGPoint(x: radius - textSize.width / 2, y: radius - textSize.height / 2),
                    withAttributes: attrs
                )
            }
        }

        private func createNameBubbleImage(name: String) -> UIImage {
            let primaryColor = UIColor(red: 37/255, green: 99/255, blue: 235/255, alpha: 1)
            let pointerHeight: CGFloat = 6

            let textAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 10),
                .foregroundColor: UIColor.white,
            ]
            let textNS = name as NSString
            let textSize = textNS.size(withAttributes: textAttrs)
            let paddingH: CGFloat = 10
            let paddingV: CGFloat = 5
            let boxWidth = textSize.width + paddingH * 2
            let boxHeight = textSize.height + paddingV * 2
            let cornerRadius = boxHeight / 2
            let totalHeight = boxHeight + pointerHeight

            let renderer = UIGraphicsImageRenderer(size: CGSize(width: boxWidth, height: totalHeight))
            return renderer.image { _ in
                let cx = boxWidth / 2

                // 배경 라운드 박스
                let boxRect = CGRect(x: 0, y: 0, width: boxWidth, height: boxHeight)
                primaryColor.setFill()
                UIBezierPath(roundedRect: boxRect, cornerRadius: cornerRadius).fill()

                // 아래 삼각형 포인터
                let pointer = UIBezierPath()
                pointer.move(to: CGPoint(x: cx - 4, y: boxHeight - 1))
                pointer.addLine(to: CGPoint(x: cx, y: totalHeight))
                pointer.addLine(to: CGPoint(x: cx + 4, y: boxHeight - 1))
                pointer.close()
                pointer.fill()

                // 텍스트
                textNS.draw(
                    at: CGPoint(x: paddingH, y: paddingV),
                    withAttributes: textAttrs
                )
            }
        }
    }
}

// MARK: - MapHeaderView

private struct MapHeaderView: View {
    var onProfileTap: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            Image("HeaderLogo")
                .resizable()
                .scaledToFill()
                .frame(width: 40, height: 40)
                .clipShape(RoundedRectangle(cornerRadius: 10))

            Text("알파스탬프")
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
        .padding(.leading, 15)
        .padding(.trailing, 20)
        .padding(.vertical, 12)
        .background(Color.white)
    }
}
