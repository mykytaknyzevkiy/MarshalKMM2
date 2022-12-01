import SwiftUI
import shared

class BackgroundCrashWorkaroundController: UIViewController {
    let composeController: UIViewController

    init() {
        IniterKt.doInitMe(bundle: Bundle.main)

        composeController = ComposeRootControllerKt.getRootController()

        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        if composeController.parent == nil {
            addChild(composeController)
            composeController.view.frame = view.bounds
            view.addSubview(composeController.view)
            composeController.didMove(toParent: self)
        }
    }
}
