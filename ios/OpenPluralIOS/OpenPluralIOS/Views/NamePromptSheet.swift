import SwiftUI
#if canImport(UIKit)
import UIKit
#endif

struct NamePromptSheet: View {
    @Environment(\.dismiss) private var dismiss

    let title: String
    let message: String
    #if canImport(UIKit)
    var keyboardType: UIKeyboardType = .default
    #endif
    var onSubmit: (String) async -> Void

    @State private var value = ""
    @State private var submitting = false

    var body: some View {
        NavigationStack {
            Form {
                TextField(message, text: $value)
                    #if canImport(UIKit)
                    .keyboardType(keyboardType)
                    #endif
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
            }
            .navigationTitle(title)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Confirm") {
                        Task {
                            submitting = true
                            await onSubmit(value)
                            submitting = false
                            dismiss()
                        }
                    }
                    .disabled(value.isEmpty || submitting)
                }
            }
        }
    }
}
