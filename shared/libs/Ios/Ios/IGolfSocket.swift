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
    public func v8() {
        
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
    
    @objc
    public func sendEvent(event: String, json: String) {
        socket.sendEvent(event: event, json: json)
    }
    
    func didConnected() {
        
    }
    
    func onError(error: String) {
        
    }
    
    func onMessage(message: String) {
        
    }
}
