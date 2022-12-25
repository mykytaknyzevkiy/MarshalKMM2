import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        IniterKt.doInitMe(bundle: Bundle.main)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
}
