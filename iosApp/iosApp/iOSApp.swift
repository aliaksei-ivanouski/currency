import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        MainViewControllerKt.initializeKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}
