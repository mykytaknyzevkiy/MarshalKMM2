//
//  Scene.swift
//  iosApp
//
//  Created by Nekbakht Zabirov on 02.01.2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import UIKit
import Shared

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    private let composeViewController = ComposeRootControllerKt.getRootController()
    
    var window: UIWindow?
    
    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        guard let winScene = (scene as? UIWindowScene) else { return }
        window = UIWindow(frame: winScene.coordinateSpace.bounds)
        window?.windowScene = winScene
        window?.rootViewController = composeViewController
        window?.makeKeyAndVisible()
    }
    
}
