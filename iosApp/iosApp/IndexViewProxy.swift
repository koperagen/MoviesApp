import Foundation
import shared

class IndexViewProxy: BaseMviView<IndexViewModel, IndexViewEvent>, IndexView, ObservableObject {
    
    @Published var model: IndexViewModel?
    
    override func render(model: IndexViewModel) {
        self.model = model
    }
    
}
