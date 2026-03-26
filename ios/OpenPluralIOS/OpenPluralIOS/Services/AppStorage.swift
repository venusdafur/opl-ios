import Foundation

final class AppStorage {
    private let userDefaults: UserDefaults
    private let stateURL: URL
    private let tokenKey = "openplural.web.token"
    private let tokenValidityKey = "openplural.web.validity"
    private let preferencesKey = "openplural.app.preferences"

    init(userDefaults: UserDefaults = .standard) {
        self.userDefaults = userDefaults

        let baseDirectory = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("OpenPluralIOS", isDirectory: true)
        try? FileManager.default.createDirectory(at: baseDirectory, withIntermediateDirectories: true, attributes: nil)
        self.stateURL = baseDirectory.appendingPathComponent("state.json")
    }

    func loadPreferences() -> AppPreferences {
        guard let data = userDefaults.data(forKey: preferencesKey),
              let preferences = try? JSONDecoder().decode(AppPreferences.self, from: data) else {
            return AppPreferences()
        }
        return preferences
    }

    func savePreferences(_ preferences: AppPreferences) {
        guard let data = try? JSONEncoder().encode(preferences) else { return }
        userDefaults.set(data, forKey: preferencesKey)
    }

    func loadPersistedState() -> PersistedState? {
        guard let data = try? Data(contentsOf: stateURL) else { return nil }
        return try? JSONDecoder().decode(PersistedState.self, from: data)
    }

    func savePersistedState(_ state: PersistedState) throws {
        let data = try JSONEncoder().encode(state)
        try data.write(to: stateURL, options: .atomic)
    }

    func saveToken(_ token: String) {
        userDefaults.set(token, forKey: tokenKey)
        userDefaults.set(Date().timeIntervalSince1970 + tokenValidityDuration, forKey: tokenValidityKey)
    }

    func loadToken() -> String? {
        userDefaults.string(forKey: tokenKey)
    }

    func isTokenValid() -> Bool {
        Date().timeIntervalSince1970 < userDefaults.double(forKey: tokenValidityKey)
    }

    func clearToken() {
        userDefaults.removeObject(forKey: tokenKey)
        userDefaults.removeObject(forKey: tokenValidityKey)
    }
}
