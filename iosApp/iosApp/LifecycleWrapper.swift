import Foundation
import shared

class LifecycleWrapper {

    let lifecycle = LifecycleRegistry()
    
    init() {
        self.lifecycle.onCreate()
    }
    
    deinit {
        self.lifecycle.destroy()
    }
    
    func start() {
        lifecycle.resume()
    }
    
    func stop() {
        lifecycle.stop()
    }
}
