import SwiftUI

struct OptionsView: View {
    @EnvironmentObject private var appModel: AppViewModel

    var body: some View {
        Form {
            Section("Defaults") {
                Picker("Default Page", selection: Binding(
                    get: { DashboardRoute(rawValue: appModel.preferences.defaultPage) ?? .dashboard },
                    set: {
                        appModel.preferences.defaultPage = $0.rawValue
                        appModel.savePreferences()
                    }
                )) {
                    ForEach(DashboardRoute.allCases.filter { $0 != .developer }) { route in
                        Text(route.title).tag(route)
                    }
                }

                Toggle("Color Wheel", isOn: Binding(
                    get: { appModel.preferences.colorWheel },
                    set: {
                        appModel.preferences.colorWheel = $0
                        appModel.savePreferences()
                    }
                ))

                Toggle("Developer Mode", isOn: Binding(
                    get: { appModel.preferences.developerMode },
                    set: {
                        appModel.preferences.developerMode = $0
                        appModel.savePreferences()
                    }
                ))
            }

            Section("Server") {
                TextField("Server URL", text: Binding(
                    get: { appModel.preferences.serverURL },
                    set: {
                        appModel.preferences.serverURL = $0
                        appModel.savePreferences()
                    }
                ))
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
            }
        }
    }
}
