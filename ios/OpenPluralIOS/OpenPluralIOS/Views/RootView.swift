import SwiftUI

struct RootView: View {
    @EnvironmentObject private var appModel: AppViewModel

    var body: some View {
        Group {
            switch appModel.phase {
            case .launching:
                ProgressView("Starting OpenPlural…")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .loggedOut:
                LoginView()
            case .ready:
                MainShellView()
            }
        }
        .alert("OpenPlural", isPresented: Binding(
            get: { appModel.errorMessage != nil },
            set: { if !$0 { appModel.errorMessage = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(appModel.errorMessage ?? "")
        }
    }
}

struct MainShellView: View {
    @EnvironmentObject private var appModel: AppViewModel

    var body: some View {
        NavigationStack {
            content(for: appModel.selectedRoute)
                .navigationTitle(appModel.selectedRoute.title)
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Menu {
                            ForEach(navigationRoutes) { route in
                                Button(route.title) {
                                    appModel.selectedRoute = route
                                }
                            }
                        } label: {
                            Label("Navigate", systemImage: "line.3.horizontal")
                        }
                    }
                    ToolbarItem(placement: .topBarTrailing) {
                        if appModel.isWorking {
                            ProgressView()
                        } else {
                            Button("Sync") {
                                Task {
                                    await appModel.syncDirtyResources()
                                }
                            }
                        }
                    }
                }
        }
    }

    private var navigationRoutes: [DashboardRoute] {
        var routes: [DashboardRoute] = [.dashboard, .members, .friends, .accountSettings, .options]
        if appModel.preferences.developerMode {
            routes.append(.developer)
        }
        return routes
    }

    @ViewBuilder
    private func content(for route: DashboardRoute) -> some View {
        switch route {
        case .dashboard:
            DashboardView()
        case .members:
            MembersView()
        case .friends:
            FriendsView()
        case .accountSettings:
            AccountSettingsView()
        case .options:
            OptionsView()
        case .developer:
            PlaceholderScreen(
                title: "Developer",
                detail: "Developer mode is carried over from Android. Add one-off server tools here as the backend evolves."
            )
        default:
            PlaceholderScreen(
                title: route.title,
                detail: "This section was also unfinished in the Android codebase. The iOS port keeps the route visible, but does not invent missing server or UI behavior."
            )
        }
    }
}
