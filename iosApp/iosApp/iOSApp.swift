import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Starts the same Koin graph Android starts in FocusLockApplication.onCreate.
        // `doInitKoin` is Kotlin's `initKoin()` - the ObjC bridge prefixes `do` to avoid
        // colliding with Objective-C's `init` family.
        SharedModuleKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            FocusView()
        }
    }
}
