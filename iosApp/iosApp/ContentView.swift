import SwiftUI
import Shared

struct ContentView: View {
    let greet = Greeting().greet()

    var body: some View {
        ComposeController()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
