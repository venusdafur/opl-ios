import SwiftUI

struct DashboardView: View {
    @EnvironmentObject private var appModel: AppViewModel

    private let routes: [DashboardRoute] = [
        .members, .frontHistory, .analytics, .chat, .polls, .friends,
        .usefulLinks, .appReminders, .privacyBuckets, .tokens, .userReport,
        .notificationHistory, .howTo, .customFields, .accountSettings,
        .accessibility, .options
    ]

    private let columns = [GridItem(.adaptive(minimum: 110), spacing: 12)]

    var body: some View {
        ScrollView {
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(routes) { route in
                    Button {
                        appModel.selectedRoute = route
                    } label: {
                        VStack(spacing: 10) {
                            Image(systemName: symbol(for: route))
                                .font(.title2)
                            Text(route.title)
                                .font(.subheadline.weight(.medium))
                                .multilineTextAlignment(.center)
                        }
                        .frame(maxWidth: .infinity, minHeight: 110)
                        .padding(12)
                        .background(Color(uiColor: .secondarySystemBackground), in: RoundedRectangle(cornerRadius: 18))
                    }
                    .buttonStyle(.plain)
                }

                if appModel.preferences.developerMode {
                    Button {
                        appModel.selectedRoute = .developer
                    } label: {
                        VStack(spacing: 10) {
                            Image(systemName: "hammer")
                                .font(.title2)
                            Text("Developer")
                                .font(.subheadline.weight(.medium))
                        }
                        .frame(maxWidth: .infinity, minHeight: 110)
                        .padding(12)
                        .background(Color(uiColor: .secondarySystemBackground), in: RoundedRectangle(cornerRadius: 18))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding()
        }
    }

    private func symbol(for route: DashboardRoute) -> String {
        switch route {
        case .members: return "person.3"
        case .friends: return "person.2.badge.plus"
        case .accountSettings: return "gearshape.2"
        case .options: return "slider.horizontal.3"
        case .frontHistory: return "clock.arrow.trianglehead.counterclockwise.rotate.90"
        case .analytics: return "chart.bar"
        case .chat: return "bubble.left.and.bubble.right"
        case .polls: return "chart.pie"
        case .usefulLinks: return "link"
        case .appReminders: return "bell"
        case .privacyBuckets: return "lock.shield"
        case .tokens: return "key"
        case .userReport: return "doc.text.magnifyingglass"
        case .notificationHistory: return "tray.full"
        case .howTo: return "questionmark.circle"
        case .customFields: return "square.and.pencil"
        case .accessibility: return "figure.roll"
        case .dashboard, .developer: return "square.grid.2x2"
        }
    }
}
