import Foundation
#if canImport(UIKit)
import UIKit
#endif

typealias UserID = Int
typealias MemberID = Int
typealias FolderID = Int
typealias FrontEntryID = Int64

let defaultResourceColor = 0xFFFFFF
let defaultBaseURL = "http://127.0.0.1:11675"
let tokenValidityDuration: TimeInterval = 7 * 24 * 60 * 60
let fullSyncInterval: TimeInterval = 18 * 60 * 60

struct RegisterRequest: Codable {
    let name: String
    let password: String
    let system: Bool
}

struct LoginRequest: Codable {
    let device: String
    let name: String
    let password: String
}

struct SessionResponse: Codable, Equatable, Hashable {
    let token: String
}

struct IDResponse<Value: Codable>: Codable {
    let id: Value
}

struct Folder: Codable, Equatable, Identifiable, Hashable {
    let id: FolderID
    let parentId: FolderID?
    var name: String
    var description: String?
    var emoji: String?
    var color: Int
    let createdAt: String
    var updatedAt: String

    func resolvePath(in folders: [Folder], basePath: String = "Root") -> String {
        let displayName = [emoji, name].compactMap { $0 }.joined(separator: emoji == nil ? "" : " ")
        guard let parentId else {
            return "\(basePath) / \(displayName)"
        }
        guard let parent = folders.first(where: { $0.id == parentId }) else {
            return "\(basePath) / ???"
        }
        return "\(parent.resolvePath(in: folders, basePath: basePath)) / \(displayName)"
    }
}

struct Member: Codable, Equatable, Identifiable, Hashable {
    let id: MemberID
    let userId: UserID
    var name: String
    var pronouns: String?
    var avatar: String?
    var description: String?
    var color: Int
    let custom: Bool
    let createdAt: String
    var updatedAt: String
    var folders: [FolderID]

    func profilesMatch(_ other: Member) -> Bool {
        name == other.name &&
        pronouns == other.pronouns &&
        avatar == other.avatar &&
        description == other.description &&
        color == other.color
    }

    func foldersMatch(_ other: Member) -> Bool {
        Set(folders) == Set(other.folders)
    }
}

struct FrontEntry: Codable, Equatable, Identifiable, Hashable {
    let id: FrontEntryID
    var member: MemberID
    var startedAt: String
    var endedAt: String?
    var comment: String?
}

struct FrontCommentRequest: Codable {
    let comment: String?
}

struct FrontStartTimeRequest: Codable {
    let startedAt: String
}

struct FriendRequest: Codable, Equatable, Identifiable, Hashable {
    let code: String
    let name: String

    var id: String { code }
}

struct UserResponse: Codable, Equatable, Identifiable, Hashable {
    var session: SessionResponse?
    let id: UserID
    var name: String
    var avatar: String?
    var description: String?
    var color: Int
    let system: Bool
    var friendCode: String?
    var folders: [Folder]?
    var members: [Member]?
    var front: [FrontEntry]?
}

struct SyncData: Codable, Equatable {
    var lastFullSync: TimeInterval
    var newestFolder: FolderID
    var newestMember: MemberID
    var newestFront: FrontEntryID
}

struct DirtyData: Codable, Equatable {
    var folders: Bool
    var members: Bool
    var front: Bool
}

struct PersistedState: Codable {
    var selfUser: UserResponse
    var sync: SyncData
    var dirty: DirtyData
}

struct AppPreferences: Codable, Equatable {
    var serverURL: String = defaultBaseURL
    var developerMode = false
    var defaultPage = DashboardRoute.dashboard.rawValue
    var colorWheel = false
}

enum DashboardRoute: String, CaseIterable, Identifiable, Codable {
    case dashboard
    case members
    case frontHistory = "front_history"
    case analytics
    case chat
    case polls
    case friends
    case usefulLinks = "useful_links"
    case appReminders = "app_reminders"
    case privacyBuckets = "privacy_buckets"
    case tokens
    case userReport = "user_report"
    case notificationHistory = "notification_history"
    case howTo = "how_to"
    case customFields = "custom_fields"
    case accountSettings = "account_settings"
    case accessibility
    case options
    case developer

    var id: String { rawValue }

    var title: String {
        switch self {
        case .dashboard: return "Dashboard"
        case .members: return "Members"
        case .frontHistory: return "Front History"
        case .analytics: return "Analytics"
        case .chat: return "Chat"
        case .polls: return "Polls"
        case .friends: return "Friends"
        case .usefulLinks: return "Useful Links"
        case .appReminders: return "App Reminders"
        case .privacyBuckets: return "Privacy Buckets"
        case .tokens: return "Tokens"
        case .userReport: return "User Report"
        case .notificationHistory: return "Notification History"
        case .howTo: return "How-To's"
        case .customFields: return "Custom Fields"
        case .accountSettings: return "Account Settings"
        case .accessibility: return "Accessibility"
        case .options: return "Options"
        case .developer: return "Developer"
        }
    }

    var isImplemented: Bool {
        switch self {
        case .dashboard, .members, .friends, .accountSettings, .options, .developer:
            return true
        default:
            return false
        }
    }
}

enum AppPhase: Equatable {
    case launching
    case loggedOut
    case ready
}

struct APIError: LocalizedError, Equatable {
    let code: Int
    let message: String

    var errorDescription: String? { message }
}

extension Date {
    static func fromISOString(_ value: String) -> Date? {
        ISO8601DateFormatter.withFractionalSeconds.date(from: value) ??
        ISO8601DateFormatter().date(from: value)
    }
}

extension ISO8601DateFormatter {
    static let withFractionalSeconds: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()
}

func nowISOString() -> String {
    ISO8601DateFormatter.withFractionalSeconds.string(from: Date())
}

func localTemporaryID() -> Int {
    -abs(nowISOString().hashValue)
}

func currentDeviceName() -> String {
    #if canImport(UIKit)
    return UIDevice.current.name
    #else
    return "iPhone"
    #endif
}
