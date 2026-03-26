import SwiftUI

@main
struct OpenPluralIOSApp: App {
    @StateObject private var appModel = AppViewModel()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(appModel)
                .task {
                    await appModel.bootstrap()
                }
        }
    }
}
