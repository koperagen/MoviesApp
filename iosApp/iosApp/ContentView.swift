import SwiftUI
import shared

func greet() -> String {
    return Platform().platform
}

struct ContentView: View {
    var body: some View {
        MovieIndex(proxy: IndexViewProxy())
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

