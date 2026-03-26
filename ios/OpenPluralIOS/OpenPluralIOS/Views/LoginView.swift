import SwiftUI

struct LoginView: View {
    @EnvironmentObject private var appModel: AppViewModel

    @State private var username = ""
    @State private var password = ""
    @State private var registering = false
    @State private var systemAccount = false
    @State private var serverURL = defaultBaseURL
    @State private var developerClicks = 0

    var body: some View {
        NavigationStack {
            VStack(spacing: 18) {
                Spacer()

                Text("Please log into your account")
                    .font(.headline)
                    .onTapGesture {
                        developerClicks += 1
                    }

                VStack(spacing: 12) {
                    TextField("Username", text: $username)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled()
                        .textFieldStyle(.roundedBorder)

                    SecureField("Password", text: $password)
                        .textFieldStyle(.roundedBorder)

                    if registering {
                        Toggle("System account", isOn: $systemAccount)
                    }

                    if developerClicks >= 10 {
                        TextField("Server URL", text: $serverURL)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                            .textFieldStyle(.roundedBorder)
                    }
                }

                Button(registering ? "Register" : "Login") {
                    Task {
                        await appModel.login(
                            username: username,
                            password: password,
                            registering: registering,
                            system: systemAccount,
                            serverURL: developerClicks >= 10 ? serverURL : defaultBaseURL
                        )
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(username.isEmpty || password.isEmpty || appModel.isWorking)

                Button(registering ? "Switch to Login" : "Register instead") {
                    registering.toggle()
                }
                .buttonStyle(.plain)

                Spacer()
            }
            .padding(24)
            .navigationTitle("OpenPlural")
        }
    }
}
