import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        IniterKt.doInitMe(bundle: Bundle.main)
        IniterKt.setCourseRenderView(igolfMapNativeRenderViewN: RenderMapViewController())
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
}
