import SwiftUI
import Shared

struct ComposeController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        ComposeRootControllerKt.getRootController()
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        uiViewController.view.setNeedsLayout()
    }
}
