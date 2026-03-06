// swift-tools-version: 5.9
// This is a reference file. Open the project in Xcode by creating a new iOS App project
// and adding existing files from the AlphaCityStampTour directory.
//
// Xcode Project Setup:
// 1. File > New > Project > iOS > App
// 2. Product Name: AlphaCityStampTour
// 3. Interface: SwiftUI
// 4. Language: Swift
// 5. Bundle Identifier: com.alphacity.stamptour
// 6. Minimum Deployment: iOS 17.0
// 7. Add existing files from this directory

import PackageDescription

let package = Package(
    name: "AlphaCityStampTour",
    platforms: [.iOS(.v17)],
    dependencies: [
        .package(url: "https://github.com/Alamofire/Alamofire.git", from: "5.9.0"),
    ],
    targets: [
        .executableTarget(
            name: "AlphaCityStampTour",
            dependencies: ["Alamofire"],
            path: "AlphaCityStampTour"
        ),
    ]
)
