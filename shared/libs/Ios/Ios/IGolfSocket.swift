//
//  IGolfSocket.swift
//  Ios
//
//  Created by Nekbakht Zabirov on 21.12.2022.
//

import UIKit

@objc
public class IGolfSocket: NSObject, SocketManagerDelegate {
    
    @objc
    public func v1() {
        
    }
    
    private let socket = SocketManager()
    
    public override init() {
        super.init()
        socket.setDelegate(delegate: self)
    }
    
    @objc
    public func connect(url: String, port: Int) {
        socket.connect(url: url, port: port)
    }
    
    public func didConnected() {
        
    }
    
    public func onError(error: String) {
        
    }
    
    public func onMessage(message: String) {
        
    }
}
