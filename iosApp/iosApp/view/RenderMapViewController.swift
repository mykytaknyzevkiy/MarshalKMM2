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
import CoreLocation

class RenderMapViewController: UIViewController, CourseRenderViewDelegate, IgolfMapNativeRenderView {
    @IBOutlet var renderView: CourseRenderView?
    
    private let cartIds = [Int32]()
    
    func setHole(hole: Int32) {
        renderView?.currentHole = UInt(hole)
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
        
    }
    
    func addCart(id: Int32, name: String, lat: Double, lng: Double) {
        let latitudeString = String(format: "%f", lat)
        let longitudeString = String(format: "%f", lng)
        
        if (cartIds.contains(id)) {
            renderView?.updateCartMarker(
                withId: id,
                newLocation: CLLocation(latitude: latitudeString, longitude: longitudeString)
            )
        } else {
            renderView?.addCartMarker(withName: name, andLocation: CLLocation(latitude: latitudeString, longitude: longitudeString), andId: id)
        }
    }
    
    func removeCart(id: Int32) {
        if (cartIds.contains(id)) {
            renderView?.removeCartMarker(withId: id)
        }
    }


}

extension CLLocation {
    
    convenience init?(latitude: String, longitude: String) {
        
        if let latitudeValue = Double(latitude), let longitudeValue = Double(longitude) {
            let latitudeDD = Double(Int(latitudeValue / 100))
            let latitudeMM = ((latitudeValue / 100) - latitudeDD) * 100
            let latitudedd = latitudeMM / 60
            let latitudeRes = latitudeDD + latitudedd
            
            let longitudeDD = Double(Int(longitudeValue / 100))
            let longitudeMM = ((longitudeValue / 100) - longitudeDD) * 100
            let longitudedd = longitudeMM / 60
            let longitudeRes = longitudeDD + longitudedd
            
            if latitudeRes < -90.0 || latitudeRes > 90.0 || longitudeRes < -180.0 || longitudeRes > 180.0 {
                return nil
            } else {
                self.init(latitude: latitudeRes, longitude: longitudeRes)
            }
        } else {
           return nil
        }
    }
}

