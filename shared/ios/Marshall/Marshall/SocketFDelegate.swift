//
//  SocketFDelegate.swift
//  Marshall
//
//  Created by Nekbakht Zabirov on 19.12.2022.
//

import UIKit

public protocol SocketFDelegate: NSObject {
    func onConnected()
    
    func onError(error: String)
    
    func onMessage(json: String)
}
