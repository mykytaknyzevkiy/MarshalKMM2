//
//  SocketManagerDelegate.swift
//  Ios
//
//  Created by Nekbakht Zabirov on 21.12.2022.
//

import Foundation

protocol SocketManagerDelegate {
    func didConnected()
    
    func onError(error: String)
    
    func onMessage(message: String)
}
