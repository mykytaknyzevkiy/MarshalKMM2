//
//  NUtils.swift
//  Ios
//
//  Created by Nekbakht Zabirov on 26.12.2022.
//

import UIKit

@objc
public class NUtils: NSObject {
    
    @objc
    public class func toNSDirictory(json: String) -> NSDictionary? {
        let data = json.data(using: .utf8)!
            
        return try? JSONSerialization.jsonObject(with: data, options: []) as? NSDictionary
    }
    
}
