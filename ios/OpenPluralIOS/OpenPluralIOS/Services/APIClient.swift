import Foundation

final class APIClient {
    var baseURL: String
    var authToken: String?

    init(baseURL: String, authToken: String?) {
        self.baseURL = baseURL
        self.authToken = authToken
    }

    func register(name: String, password: String, system: Bool) async throws {
        let request = RegisterRequest(name: name, password: password, system: system)
        _ = try await send(method: "POST", path: "/auth/register", body: request, expectCode: 204) as EmptyResponse
    }

    func login(device: String, name: String, password: String) async throws -> UserResponse {
        let request = LoginRequest(device: device, name: name, password: password)
        let user: UserResponse = try await send(method: "POST", path: "/auth/login", body: request)
        authToken = user.session?.token
        return user
    }

    func getUser(full: Bool) async throws -> UserResponse {
        try await send(method: "GET", path: "/api/user/self?full=\(full)")
    }

    func getFolders(userId: UserID? = nil) async throws -> [Folder] {
        let suffix = userId.map { "?userId=\($0)" } ?? ""
        return try await send(method: "GET", path: "/api/folder/\(suffix)")
    }

    func createFolder(_ folder: Folder) async throws -> FolderID {
        let response: IDResponse<FolderID> = try await send(method: "PUT", path: "/api/folder/", body: folder)
        return response.id
    }

    func editFolder(_ folder: Folder) async throws {
        _ = try await send(method: "PATCH", path: "/api/folder/\(folder.id)", body: folder, expectCode: 204) as EmptyResponse
    }

    func deleteFolder(id: FolderID) async throws {
        _ = try await send(method: "DELETE", path: "/api/folder/\(id)", expectCode: 204) as EmptyResponse
    }

    func getMembers(userId: UserID? = nil) async throws -> [Member] {
        let suffix = userId.map { "?userId=\($0)" } ?? ""
        return try await send(method: "GET", path: "/api/member/\(suffix)")
    }

    func createMember(_ member: Member) async throws -> MemberID {
        let response: IDResponse<MemberID> = try await send(method: "PUT", path: "/api/member/", body: member)
        return response.id
    }

    func editMember(_ member: Member) async throws {
        _ = try await send(method: "PATCH", path: "/api/member/\(member.id)", body: member, expectCode: 204) as EmptyResponse
    }

    func editMemberFolders(id: MemberID, folders: [FolderID]) async throws {
        _ = try await send(method: "PATCH", path: "/api/member/\(id)/folders", body: folders, expectCode: 204) as EmptyResponse
    }

    func deleteMember(id: MemberID) async throws {
        _ = try await send(method: "DELETE", path: "/api/member/\(id)", expectCode: 204) as EmptyResponse
    }

    func getFront(userId: UserID? = nil) async throws -> [FrontEntry] {
        let suffix = userId.map { "?userId=\($0)" } ?? ""
        return try await send(method: "GET", path: "/api/user/front\(suffix)")
    }

    func addFrontEntry(_ entry: FrontEntry) async throws -> FrontEntryID {
        let response: IDResponse<FrontEntryID> = try await send(method: "PUT", path: "/api/front/", body: entry)
        return response.id
    }

    func front(memberId: MemberID) async throws -> FrontEntryID {
        let response: IDResponse<FrontEntryID> = try await send(method: "PUT", path: "/api/front/\(memberId)")
        return response.id
    }

    func unfront(memberId: MemberID, endTime: String? = nil) async throws {
        let suffix = endTime.map { "?endedAt=\($0.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? $0)" } ?? ""
        _ = try await send(method: "DELETE", path: "/api/front/member/\(memberId)\(suffix)", expectCode: 204) as EmptyResponse
    }

    func frontComment(entryId: FrontEntryID, comment: String?) async throws {
        _ = try await send(method: "PATCH", path: "/api/front/\(entryId)/comment", body: FrontCommentRequest(comment: comment), expectCode: 204) as EmptyResponse
    }

    func frontStartTime(entryId: FrontEntryID, startedAt: String) async throws {
        _ = try await send(method: "PATCH", path: "/api/front/\(entryId)/startTime", body: FrontStartTimeRequest(startedAt: startedAt), expectCode: 204) as EmptyResponse
    }

    func getFriends() async throws -> [UserResponse] {
        try await send(method: "GET", path: "/api/friend/")
    }

    func sendFriendRequest(friendCode: String) async throws {
        _ = try await send(method: "PUT", path: "/api/friend/requests/\(friendCode)", expectCode: 204) as EmptyResponse
    }

    private func send<Response: Decodable>(
        method: String,
        path: String,
        expectCode: Int = 200
    ) async throws -> Response {
        try await send(method: method, path: path, body: Optional<String>.none, expectCode: expectCode)
    }

    private func send<Response: Decodable, Body: Encodable>(
        method: String,
        path: String,
        body: Body? = nil,
        expectCode: Int = 200
    ) async throws -> Response {
        guard let url = URL(string: baseURL + path) else {
            throw APIError(code: -1, message: "Invalid server URL")
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("OpenPlural iOS App", forHTTPHeaderField: "User-Agent")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let authToken {
            request.setValue("Bearer \(authToken)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            request.httpBody = try JSONEncoder().encode(body)
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        } else if method == "POST" {
            request.httpBody = Data("{}".utf8)
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError(code: -1, message: "No HTTP response")
        }

        if httpResponse.statusCode == 401 {
            throw APIError(code: 401, message: "Session expired or invalid. Please log in again.")
        }
        if httpResponse.statusCode == 500 {
            throw APIError(code: 500, message: "Server error")
        }

        if httpResponse.statusCode != expectCode {
            throw mapKnownError(code: httpResponse.statusCode, path: path)
        }

        if Response.self == EmptyResponse.self {
            return EmptyResponse() as! Response
        }
        if Response.self == String.self {
            return String(decoding: data, as: UTF8.self) as! Response
        }
        return try JSONDecoder().decode(Response.self, from: data)
    }

    private func mapKnownError(code: Int, path: String) -> APIError {
        switch (path, code) {
        case ("/auth/register", 409):
            return APIError(code: code, message: "Username already exists")
        case ("/auth/login", 401):
            return APIError(code: code, message: "Invalid username or password")
        default:
            return APIError(code: code, message: "Unexpected HTTP response: \(code)")
        }
    }
}

private struct EmptyResponse: Decodable {}
