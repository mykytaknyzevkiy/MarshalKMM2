import SwiftUI

struct ComposeController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> some UIViewController {
        BackgroundCrashWorkaroundController()
    }

    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
        uiViewController.view.setNeedsLayout()
    }
}
