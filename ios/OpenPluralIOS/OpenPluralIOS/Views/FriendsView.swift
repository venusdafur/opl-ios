import SwiftUI

struct FriendsView: View {
    @EnvironmentObject private var appModel: AppViewModel
    @State private var showPrompt = false

    var body: some View {
        List {
            if appModel.friends.isEmpty {
                ContentUnavailableView(
                    "No friends yet",
                    systemImage: "person.2.slash",
                    description: Text("Send a friend request using their friend code.")
                )
            } else {
                ForEach(appModel.friends) { friend in
                    HStack(spacing: 12) {
                        AsyncImage(url: URL(string: friend.avatar ?? "")) { image in
                            image.resizable().scaledToFill()
                        } placeholder: {
                            Image(systemName: "person.crop.square")
                                .foregroundStyle(.secondary)
                        }
                        .frame(width: 42, height: 42)
                        .clipShape(RoundedRectangle(cornerRadius: 10))

                        VStack(alignment: .leading, spacing: 2) {
                            Text(friend.name)
                            if let description = friend.description, !description.isEmpty {
                                Text(description)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                    .lineLimit(2)
                            }
                        }
                    }
                }
            }
        }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showPrompt = true
                } label: {
                    Image(systemName: "person.badge.plus")
                }
            }
        }
        .task {
            await appModel.loadFriends()
        }
        .sheet(isPresented: $showPrompt) {
            NamePromptSheet(
                title: "Add Friend",
                message: "Enter a friend code",
                keyboardType: .asciiCapable,
                onSubmit: { code in
                    await appModel.sendFriendRequest(code: code)
                }
            )
        }
    }
}
