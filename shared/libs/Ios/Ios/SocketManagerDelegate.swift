//
//  SocketManagerDelegate.swift
//  Ios
//
//  Created by Nekbakht Zabirov on 21.12.2022.
//

import Foundation

@objc
public protocol SocketManagerDelegate {
    @objc
    func didConnected()
    
    @objc
    func onError(error: String)
    
    @objc
    func onMessage(message: String)
}
