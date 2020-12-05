import SwiftUI
import Foundation
import shared
import Combine

struct MovieIndex: View {
    
    let lifecycle: LifecycleWrapper = LifecycleWrapper()
    @State var viewLifecycle: LifecycleRegistry?
    @ObservedObject var proxy: IndexViewProxy
    @State var controller: IndexController?
    
    var body: some View {
        VStack {
            List() {
                ForEach(proxy.model?.movies ?? [], id: \.movie.id) { item in
                    Button(action: { self.proxy.dispatch(event: IndexViewEvent.MovieClick(movie: item.movie)) }) {
                        MovieItem(item: item)
                    }
                }
            }
        }.onAppear(perform: {
            if (self.controller == nil) {
                self.controller = IndexControllerFactory(appDatabase: MovieCacheKt.createDatabase(driverFactory: DriverFactory()), lifecycle: self.lifecycle.lifecycle).create()
            }
            let viewLifecycle = LifecycleRegistry()
            self.viewLifecycle = viewLifecycle
            self.controller?.onViewCreated(view: proxy, viewLifecycle: viewLifecycle)
            viewLifecycle.onCreate()
            viewLifecycle.onStart()
            self.lifecycle.start()
        })
    }
}

struct MovieItem : View {
    
    let item: FavouriteMovie
    
    var body: some View {
        VStack {
            let text = item.isFavourite ? "*" + item.movie.title : item.movie.title
            
            Text(text).bold()
            Text(item.movie.voteAverage.description + " / 10")
            Text(item.movie.overview)
        }
    }
    
}

