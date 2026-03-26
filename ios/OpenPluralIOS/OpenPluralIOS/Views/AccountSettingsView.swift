import SwiftUI

struct AccountSettingsView: View {
    @EnvironmentObject private var appModel: AppViewModel

    var body: some View {
        Form {
            if let user = appModel.selfUser {
                Section("Profile") {
                    LabeledContent("Name", value: user.name)
                    LabeledContent("Description", value: user.description ?? "")
                    LabeledContent("Friend Code", value: user.friendCode ?? "")
                }

                Section("Avatar") {
                    HStack {
                        Spacer()
                        AsyncImage(url: URL(string: user.avatar ?? "")) { image in
                            image.resizable().scaledToFill()
                        } placeholder: {
                            Image(systemName: "person.crop.square")
                                .resizable()
                                .scaledToFit()
                                .foregroundStyle(.secondary)
                                .padding(24)
                        }
                        .frame(width: 140, height: 140)
                        .background(Color(uiColor: .secondarySystemBackground), in: RoundedRectangle(cornerRadius: 20))
                        Spacer()
                    }
                }

                Section {
                    Button("Log Out", role: .destructive) {
                        appModel.logout()
                    }
                }
            }
        }
    }
}
