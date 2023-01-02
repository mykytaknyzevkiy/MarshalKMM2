//
//  AppDelegate.swift
//  iosApp
//
//  Created by Nekbakht Zabirov on 02.01.2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import UIKit
import Shared

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        IniterKt.doInitMe(bundle: Bundle.main)
        IniterKt.setCourseRenderView(igolfMapNativeRenderViewN: RenderMapViewController())
        
        return true
    }
    
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }
}
