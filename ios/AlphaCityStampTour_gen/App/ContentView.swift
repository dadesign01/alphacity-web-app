import SwiftUI

struct ContentView: View {
    @State private var showSplash = true

    var body: some View {
        NavigationStack {
            if showSplash {
                SplashView(onStartTapped: {
                    showSplash = false
                })
            } else {
                // TODO: LoginView()
                Text("로그인 화면")
            }
        }
    }
}

#Preview {
    ContentView()
}
