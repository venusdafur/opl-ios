import SwiftUI

struct MemberEditorView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appModel: AppViewModel

    let memberID: MemberID

    @State private var name = ""
    @State private var pronouns = ""
    @State private var avatar = ""
    @State private var details = ""
    @State private var color = Color.white
    @State private var selectedFolders: Set<FolderID> = []

    var body: some View {
        NavigationStack {
            Form {
                Section("Profile") {
                    TextField("Name", text: $name)
                    TextField("Pronouns", text: $pronouns)
                    TextField("Avatar URL", text: $avatar)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                    TextField("Description", text: $details, axis: .vertical)
                        .lineLimit(4, reservesSpace: true)
                    ColorPicker("Color", selection: $color, supportsOpacity: false)
                }

                Section("Groups") {
                    ForEach(appModel.selfUser?.folders ?? []) { folder in
                        Toggle(
                            folder.resolvePath(in: appModel.selfUser?.folders ?? []),
                            isOn: Binding(
                                get: { selectedFolders.contains(folder.id) },
                                set: { enabled in
                                    if enabled {
                                        selectedFolders.insert(folder.id)
                                    } else {
                                        selectedFolders.remove(folder.id)
                                    }
                                }
                            )
                        )
                    }
                }

                Section {
                    Button("Delete Member", role: .destructive) {
                        appModel.deleteMember(id: memberID)
                        dismiss()
                    }
                }
            }
            .navigationTitle("Edit Member")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Save") {
                        save()
                    }
                }
            }
            .onAppear {
                guard let member = appModel.member(id: memberID) else { return }
                name = member.name
                pronouns = member.pronouns ?? ""
                avatar = member.avatar ?? ""
                details = member.description ?? ""
                color = Color(rgb: member.color)
                selectedFolders = Set(member.folders)
            }
        }
    }

    private func save() {
        guard var member = appModel.member(id: memberID) else { return }
        member.name = name
        member.pronouns = pronouns.isEmpty ? nil : pronouns
        member.avatar = avatar.isEmpty ? nil : avatar
        member.description = details.isEmpty ? nil : details
        member.color = color.rgbValue
        member.updatedAt = nowISOString()
        member.folders = Array(selectedFolders).sorted()
        appModel.updateMember(member)
        Task {
            await appModel.syncDirtyResources()
        }
        dismiss()
    }
}

struct FolderEditorView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var appModel: AppViewModel

    let folderID: FolderID

    @State private var name = ""
    @State private var emoji = ""
    @State private var details = ""
    @State private var color = Color.white

    var body: some View {
        NavigationStack {
            Form {
                Section("Folder") {
                    TextField("Name", text: $name)
                    TextField("Emoji", text: $emoji)
                    TextField("Description", text: $details, axis: .vertical)
                        .lineLimit(4, reservesSpace: true)
                    ColorPicker("Color", selection: $color, supportsOpacity: false)
                }

                Section {
                    Button("Delete Folder", role: .destructive) {
                        appModel.deleteFolder(id: folderID)
                        dismiss()
                    }
                }
            }
            .navigationTitle("Edit Folder")
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Save") {
                        save()
                    }
                }
            }
            .onAppear {
                guard let folder = appModel.folder(id: folderID) else { return }
                name = folder.name
                emoji = folder.emoji ?? ""
                details = folder.description ?? ""
                color = Color(rgb: folder.color)
            }
        }
    }

    private func save() {
        guard var folder = appModel.folder(id: folderID) else { return }
        folder.name = name
        folder.emoji = emoji.isEmpty ? nil : emoji
        folder.description = details.isEmpty ? nil : details
        folder.color = color.rgbValue
        folder.updatedAt = nowISOString()
        appModel.updateFolder(folder)
        Task {
            await appModel.syncDirtyResources()
        }
        dismiss()
    }
}

private extension Color {
    init(rgb: Int) {
        let red = Double((rgb >> 16) & 0xFF) / 255
        let green = Double((rgb >> 8) & 0xFF) / 255
        let blue = Double(rgb & 0xFF) / 255
        self.init(red: red, green: green, blue: blue)
    }

    var rgbValue: Int {
        #if canImport(UIKit)
        let components = UIColor(self).cgColor.components ?? [1, 1, 1]
        let red = Int((components[safe: 0] ?? 1) * 255)
        let green = Int((components[safe: 1] ?? 1) * 255)
        let blue = Int((components[safe: 2] ?? 1) * 255)
        return (red << 16) | (green << 8) | blue
        #else
        return defaultResourceColor
        #endif
    }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        guard indices.contains(index) else { return nil }
        return self[index]
    }
}
