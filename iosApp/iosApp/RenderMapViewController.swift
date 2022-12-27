//
//  RenderMapViewController.swift
//  iosApp
//
//  Created by Nekbakht Zabirov on 27.12.2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import Shared

class RenderMapViewController: UIViewController, CourseRenderViewDelegate, IgolfMapNativeRenderView {
    
    @IBOutlet var renderView: CourseRenderView?
    
    func setHole(hole: Int32) {
        //renderView?.currentHole = UInt(hole)
    }
    
    func setVectors(json: String) {
        renderView?.delegate = self

        let data = json.data(using: .utf8)!
                    
        if let vectors = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
            renderView?.viewCart(withGpsVectorData: vectors)
        }
    }
    
    func renderNUIViewController() -> UIViewController {
        return self
    }
    
    func courseRenderViewDidLoadCourseData() {
        print("Neka courseRenderViewDidLoadCourseData")
        renderView?.currentHole = UInt(3)
    }


}
