import SwiftUI

struct MembersView: View {
    @EnvironmentObject private var appModel: AppViewModel

    @State private var showFolders = true
    @State private var currentRoot: FolderID?
    @State private var showingCreateMember = false
    @State private var showingCreateFolder = false
    @State private var creatingCustomMember = false
    @State private var editingMemberID: MemberID?
    @State private var editingFolderID: FolderID?

    var body: some View {
        List {
            frontSection
            treeSection(custom: false, title: "Members")
            treeSection(custom: true, title: "Custom Fronts")
        }
        .listStyle(.insetGrouped)
        .toolbar {
            ToolbarItemGroup(placement: .topBarTrailing) {
                Button {
                    showingCreateFolder = true
                } label: {
                    Image(systemName: "folder.badge.plus")
                }

                Button {
                    creatingCustomMember = false
                    showingCreateMember = true
                } label: {
                    Image(systemName: "person.badge.plus")
                }
            }
        }
        .sheet(item: Binding(
            get: { editingMemberID.map(MemberSheetID.init) },
            set: { editingMemberID = $0?.value }
        )) { item in
            MemberEditorView(memberID: item.value)
                .environmentObject(appModel)
        }
        .sheet(item: Binding(
            get: { editingFolderID.map(FolderSheetID.init) },
            set: { editingFolderID = $0?.value }
        )) { item in
            FolderEditorView(folderID: item.value)
                .environmentObject(appModel)
        }
        .sheet(isPresented: $showingCreateMember) {
            NamePromptSheet(
                title: "Create Member",
                message: "Enter a name",
                onSubmit: { name in
                    appModel.createMember(name: name, custom: creatingCustomMember)
                }
            )
        }
        .sheet(isPresented: $showingCreateFolder) {
            NamePromptSheet(
                title: "Create Folder",
                message: "Enter a name",
                onSubmit: { name in
                    appModel.createFolder(name: name, parentId: currentRoot)
                }
            )
        }
    }

    private var frontSection: some View {
        Section("Fronters") {
            let activeFront = (appModel.selfUser?.front ?? []).filter { $0.endedAt == nil }
            if activeFront.isEmpty {
                Text("There is currently no member at front")
                    .foregroundStyle(.secondary)
            } else {
                ForEach(activeFront) { entry in
                    if let member = appModel.member(id: entry.member) {
                        MemberRow(
                            member: member,
                            isFronting: true,
                            onToggleFront: { _ in
                                appModel.setFronting(memberId: member.id, isFronting: false)
                            },
                            onTap: { editingMemberID = member.id }
                        )
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func treeSection(custom: Bool, title: String) -> some View {
        let allFolders = appModel.selfUser?.folders ?? []
        let allMembers = (appModel.selfUser?.members ?? []).filter { $0.custom == custom }
        let folderMembers = allMembers.filter { member in
            guard let currentRoot else { return member.folders.isEmpty }
            return member.folders.contains(currentRoot)
        }
        let visibleFolders = allFolders.filter { folder in
            if let currentRoot {
                return folder.parentId == currentRoot
            }
            return folder.parentId == nil
        }

        Section(title) {
            if !custom {
                Toggle("View folders", isOn: $showFolders)
                if showFolders {
                    HStack {
                        Text(currentRoot.flatMap { appModel.folder(id: $0)?.resolvePath(in: allFolders) } ?? "Root")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                        Spacer()
                        if currentRoot != nil {
                            Button("Up") {
                                currentRoot = appModel.folder(id: currentRoot!)?.parentId
                            }
                            .buttonStyle(.borderless)
                        }
                    }

                    ForEach(visibleFolders) { folder in
                        Button {
                            currentRoot = folder.id
                        } label: {
                            HStack {
                                Text(folder.emoji ?? "📁")
                                Text(folder.name)
                                Spacer()
                                Button {
                                    editingFolderID = folder.id
                                } label: {
                                    Image(systemName: "slider.horizontal.3")
                                }
                                .buttonStyle(.borderless)
                            }
                        }
                        .buttonStyle(.plain)
                    }
                }
            }

            ForEach(folderMembers) { member in
                MemberRow(
                    member: member,
                    isFronting: (appModel.selfUser?.front ?? []).contains(where: { $0.member == member.id && $0.endedAt == nil }),
                    onToggleFront: { enabled in
                        appModel.setFronting(memberId: member.id, isFronting: enabled)
                        Task {
                            await appModel.syncDirtyResources()
                        }
                    },
                    onTap: { editingMemberID = member.id }
                )
            }

            if custom {
                Button("Create Custom Front") {
                    creatingCustomMember = true
                    showingCreateMember = true
                }
            }
        }
    }
}

private struct MemberRow: View {
    let member: Member
    let isFronting: Bool
    let onToggleFront: (Bool) -> Void
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                AsyncImage(url: URL(string: member.avatar ?? "")) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Image(systemName: "person.crop.square")
                        .foregroundStyle(.secondary)
                }
                .frame(width: 42, height: 42)
                .clipShape(RoundedRectangle(cornerRadius: 10))

                VStack(alignment: .leading, spacing: 2) {
                    Text(member.name)
                    if let pronouns = member.pronouns, !pronouns.isEmpty {
                        Text(pronouns)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                Spacer()

                Button(isFronting ? "Remove" : "Front") {
                    onToggleFront(!isFronting)
                }
                .buttonStyle(.bordered)
            }
        }
        .buttonStyle(.plain)
    }
}

private struct MemberSheetID: Identifiable {
    let value: MemberID
    var id: Int { value }
}

private struct FolderSheetID: Identifiable {
    let value: FolderID
    var id: Int { value }
}
