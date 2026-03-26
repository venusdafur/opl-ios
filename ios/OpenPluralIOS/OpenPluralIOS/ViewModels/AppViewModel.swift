import Foundation

@MainActor
final class AppViewModel: ObservableObject {
    @Published var phase: AppPhase = .launching
    @Published var preferences = AppPreferences()
    @Published var selfUser: UserResponse?
    @Published var friends: [UserResponse] = []
    @Published var selectedRoute: DashboardRoute = .dashboard
    @Published var errorMessage: String?
    @Published var isWorking = false

    private let storage = AppStorage()
    private var apiClient: APIClient
    private var sync = SyncData(lastFullSync: 0, newestFolder: 0, newestMember: 0, newestFront: 0)
    private var dirty = DirtyData(folders: false, members: false, front: false)

    init() {
        let preferences = storage.loadPreferences()
        self.preferences = preferences
        self.selectedRoute = DashboardRoute(rawValue: preferences.defaultPage) ?? .dashboard
        self.apiClient = APIClient(baseURL: preferences.serverURL, authToken: storage.loadToken())
    }

    func bootstrap() async {
        preferences = storage.loadPreferences()
        selectedRoute = DashboardRoute(rawValue: preferences.defaultPage) ?? .dashboard
        apiClient.baseURL = preferences.serverURL
        apiClient.authToken = storage.loadToken()

        if let persisted = storage.loadPersistedState() {
            selfUser = persisted.selfUser
            sync = persisted.sync
            dirty = persisted.dirty
        }

        guard storage.isTokenValid(), storage.loadToken() != nil else {
            phase = .loggedOut
            return
        }

        do {
            try await syncFully()
            phase = .ready
        } catch {
            storage.clearToken()
            phase = .loggedOut
            errorMessage = error.localizedDescription
        }
    }

    func login(username: String, password: String, registering: Bool, system: Bool, serverURL: String) async {
        isWorking = true
        defer { isWorking = false }

        do {
            preferences.serverURL = serverURL
            savePreferences()
            apiClient.baseURL = serverURL

            if registering {
                try await apiClient.register(name: username, password: password, system: system)
            }

            let user = try await apiClient.login(device: currentDeviceName(), name: username, password: password)
            if let token = user.session?.token {
                storage.saveToken(token)
            }

            let folders = user.folders ?? []
            let members = user.members ?? []
            let front = user.front ?? []
            sync = SyncData(
                lastFullSync: 0,
                newestFolder: folders.map(\.id).max() ?? 0,
                newestMember: members.map(\.id).max() ?? 0,
                newestFront: front.map(\.id).max() ?? 0
            )
            dirty = DirtyData(folders: false, members: false, front: false)
            selfUser = user
            try saveState()
            try await syncFully()
            phase = .ready
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func logout() {
        storage.clearToken()
        selfUser = nil
        friends = []
        phase = .loggedOut
    }

    func savePreferences() {
        storage.savePreferences(preferences)
        apiClient.baseURL = preferences.serverURL
        selectedRoute = DashboardRoute(rawValue: preferences.defaultPage) ?? .dashboard
    }

    func member(id: MemberID) -> Member? {
        selfUser?.members?.first(where: { $0.id == id })
    }

    func folder(id: FolderID) -> Folder? {
        selfUser?.folders?.first(where: { $0.id == id })
    }

    func createMember(name: String, custom: Bool) {
        guard var user = selfUser else { return }
        let now = nowISOString()
        let member = Member(
            id: localTemporaryID(),
            userId: user.id,
            name: name,
            pronouns: nil,
            avatar: nil,
            description: nil,
            color: defaultResourceColor,
            custom: custom,
            createdAt: now,
            updatedAt: now,
            folders: []
        )
        user.members = (user.members ?? []) + [member]
        selfUser = user
        dirty.members = true
        persistIgnoringErrors()
    }

    func updateMember(_ member: Member) {
        guard var user = selfUser, let members = user.members else { return }
        user.members = members.map { $0.id == member.id ? member : $0 }
        selfUser = user
        dirty.members = true
        persistIgnoringErrors()
    }

    func deleteMember(id: MemberID) {
        guard var user = selfUser else { return }
        user.members = (user.members ?? []).filter { $0.id != id }
        user.front = (user.front ?? []).filter { $0.member != id }
        selfUser = user
        dirty.members = true
        persistIgnoringErrors()
    }

    func createFolder(name: String, parentId: FolderID?) {
        guard var user = selfUser else { return }
        let now = nowISOString()
        let folder = Folder(
            id: localTemporaryID(),
            parentId: parentId,
            name: name,
            description: nil,
            emoji: nil,
            color: defaultResourceColor,
            createdAt: now,
            updatedAt: now
        )
        user.folders = (user.folders ?? []) + [folder]
        selfUser = user
        dirty.folders = true
        persistIgnoringErrors()
    }

    func updateFolder(_ folder: Folder) {
        guard var user = selfUser, let folders = user.folders else { return }
        user.folders = folders.map { $0.id == folder.id ? folder : $0 }
        selfUser = user
        dirty.folders = true
        persistIgnoringErrors()
    }

    func deleteFolder(id: FolderID) {
        guard var user = selfUser else { return }
        user.folders = (user.folders ?? []).filter { $0.id != id }
        user.members = (user.members ?? []).map { member in
            var copy = member
            copy.folders.removeAll { $0 == id }
            return copy
        }
        selfUser = user
        dirty.folders = true
        persistIgnoringErrors()
    }

    func setFronting(memberId: MemberID, isFronting: Bool) {
        guard var user = selfUser else { return }
        var front = user.front ?? []
        if isFronting {
            front.append(
                FrontEntry(
                    id: Int64(localTemporaryID()),
                    member: memberId,
                    startedAt: nowISOString(),
                    endedAt: nil,
                    comment: nil
                )
            )
        } else {
            front = front.map { entry in
                guard entry.member == memberId, entry.endedAt == nil else { return entry }
                var copy = entry
                copy.endedAt = nowISOString()
                return copy
            }
        }
        user.front = front
        selfUser = user
        dirty.front = true
        persistIgnoringErrors()
    }

    func syncDirtyResources() async {
        guard phase == .ready, selfUser != nil else { return }
        isWorking = true
        defer { isWorking = false }

        do {
            if dirty.folders && dirty.members && dirty.front {
                try await syncFully()
                return
            }
            if Date().timeIntervalSince1970 - sync.lastFullSync > 24 * 60 * 60 {
                try await syncFully()
                return
            }
            if dirty.folders {
                let folders = try await apiClient.getFolders()
                try await syncFolders(serverFolders: folders)
            }
            if dirty.members {
                let members = try await apiClient.getMembers()
                try await syncMembers(serverMembers: members)
            }
            if dirty.front {
                let front = try await apiClient.getFront()
                try await syncFront(serverFront: front)
            }
            try saveState()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func syncFully() async throws {
        guard selfUser != nil || storage.loadToken() != nil else { return }

        var user = try await apiClient.getUser(full: true)
        user.session = nil

        try await syncFolders(serverFolders: user.folders ?? [])
        try await syncMembers(serverMembers: user.members ?? [])
        try await syncFront(serverFront: user.front ?? [])

        if let local = selfUser {
            user.folders = local.folders ?? user.folders
            user.members = local.members ?? user.members
            user.front = local.front ?? user.front
        }
        selfUser = user
        sync.lastFullSync = Date().timeIntervalSince1970
        try saveState()
    }

    func loadFriends() async {
        do {
            friends = try await apiClient.getFriends()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func sendFriendRequest(code: String) async {
        do {
            try await apiClient.sendFriendRequest(friendCode: code)
            await loadFriends()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func saveState() throws {
        guard let selfUser else { return }
        try storage.savePersistedState(PersistedState(selfUser: selfUser, sync: sync, dirty: dirty))
    }

    private func persistIgnoringErrors() {
        do {
            try saveState()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func syncFolders(serverFolders remote: [Folder]) async throws {
        guard var user = selfUser else { return }
        var localFolders = user.folders ?? []
        var localMembers = user.members ?? []
        var newestFolder = sync.newestFolder

        for local in localFolders {
            if let server = remote.first(where: { $0.id == local.id }) {
                let localUpdated = Date.fromISOString(local.updatedAt) ?? .distantPast
                let serverUpdated = Date.fromISOString(server.updatedAt) ?? .distantPast
                if localUpdated > serverUpdated {
                    try await apiClient.editFolder(local)
                } else if localUpdated < serverUpdated,
                          let index = localFolders.firstIndex(where: { $0.id == local.id }) {
                    localFolders[index] = server
                }
            } else if local.id < sync.newestFolder && local.id > 0 {
                localFolders.removeAll { $0.id == local.id }
                localMembers = localMembers.map { member in
                    var copy = member
                    copy.folders.removeAll { $0 == local.id }
                    return copy
                }
            } else {
                let newID = try await apiClient.createFolder(local)
                if let index = localFolders.firstIndex(where: { $0.id == local.id }) {
                    localFolders[index].name = local.name
                    localFolders[index] = Folder(
                        id: newID,
                        parentId: local.parentId,
                        name: local.name,
                        description: local.description,
                        emoji: local.emoji,
                        color: local.color,
                        createdAt: local.createdAt,
                        updatedAt: local.updatedAt
                    )
                }
                localMembers = localMembers.map { member in
                    var copy = member
                    copy.folders = copy.folders.map { $0 == local.id ? newID : $0 }
                    return copy
                }
                newestFolder = max(newestFolder, newID)
            }
        }

        for server in remote where !localFolders.contains(where: { $0.id == server.id }) {
            if server.id <= sync.newestFolder {
                try await apiClient.deleteFolder(id: server.id)
            } else {
                localFolders.append(server)
                newestFolder = max(newestFolder, server.id)
            }
        }

        user.folders = localFolders
        user.members = localMembers
        selfUser = user
        sync.newestFolder = newestFolder
        dirty.folders = false
    }

    private func syncMembers(serverMembers remote: [Member]) async throws {
        guard var user = selfUser else { return }
        var localMembers = user.members ?? []
        var localFront = user.front ?? []
        var newestMember = sync.newestMember

        for local in localMembers {
            if let server = remote.first(where: { $0.id == local.id }) {
                let localUpdated = Date.fromISOString(local.updatedAt) ?? .distantPast
                let serverUpdated = Date.fromISOString(server.updatedAt) ?? .distantPast
                if localUpdated > serverUpdated {
                    if !local.profilesMatch(server) {
                        try await apiClient.editMember(local)
                    }
                    if !local.foldersMatch(server) {
                        try await apiClient.editMemberFolders(id: local.id, folders: local.folders)
                    }
                } else if localUpdated < serverUpdated,
                          let index = localMembers.firstIndex(where: { $0.id == local.id }) {
                    localMembers[index] = server
                }
            } else if local.id < sync.newestMember && local.id > 0 {
                localMembers.removeAll { $0.id == local.id }
                localFront.removeAll { $0.member == local.id }
            } else {
                let newID = try await apiClient.createMember(local)
                if let index = localMembers.firstIndex(where: { $0.id == local.id }) {
                    var updated = local
                    updated = Member(
                        id: newID,
                        userId: local.userId,
                        name: local.name,
                        pronouns: local.pronouns,
                        avatar: local.avatar,
                        description: local.description,
                        color: local.color,
                        custom: local.custom,
                        createdAt: local.createdAt,
                        updatedAt: local.updatedAt,
                        folders: local.folders
                    )
                    localMembers[index] = updated
                }
                localFront = localFront.map { entry in
                    guard entry.member == local.id else { return entry }
                    var copy = entry
                    copy.member = newID
                    return copy
                }
                newestMember = max(newestMember, newID)
            }
        }

        for server in remote where !localMembers.contains(where: { $0.id == server.id }) {
            if server.id <= sync.newestMember {
                try await apiClient.deleteMember(id: server.id)
            } else {
                localMembers.append(server)
                newestMember = max(newestMember, server.id)
            }
        }

        user.members = localMembers
        user.front = localFront
        selfUser = user
        sync.newestMember = newestMember
        dirty.members = false
    }

    private func syncFront(serverFront remote: [FrontEntry]) async throws {
        guard var user = selfUser else { return }
        var localFront = user.front ?? []
        var newestFront = sync.newestFront

        for local in localFront {
            if let server = remote.first(where: { $0.id == local.id }) {
                if local.endedAt != nil {
                    try await apiClient.unfront(memberId: local.member, endTime: local.endedAt)
                } else {
                    let localTime = Date.fromISOString(local.startedAt)?.timeIntervalSince1970 ?? 0
                    let serverTime = Date.fromISOString(server.startedAt)?.timeIntervalSince1970 ?? 0
                    if localTime != serverTime {
                        try await apiClient.frontStartTime(entryId: local.id, startedAt: local.startedAt)
                    }
                    if local.comment != server.comment {
                        try await apiClient.frontComment(entryId: local.id, comment: local.comment)
                    }
                }
            } else if local.id < sync.newestFront && local.id > 0 {
                localFront.removeAll { $0.id == local.id }
            } else {
                let newID = try await apiClient.addFrontEntry(local)
                if let index = localFront.firstIndex(where: { $0.id == local.id }) {
                    localFront[index] = FrontEntry(
                        id: newID,
                        member: local.member,
                        startedAt: local.startedAt,
                        endedAt: local.endedAt,
                        comment: local.comment
                    )
                }
                newestFront = max(newestFront, newID)
            }
        }

        for server in remote where !localFront.contains(where: { $0.id == server.id }) {
            localFront.append(server)
            newestFront = max(newestFront, server.id)
        }

        localFront.removeAll { $0.id <= newestFront && $0.endedAt != nil }
        user.front = localFront
        selfUser = user
        sync.newestFront = newestFront
        dirty.front = false
    }
}
