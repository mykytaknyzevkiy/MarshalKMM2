//
//  IGolfSocket.swift
//  Ios
//
//  Created by Nekbakht Zabirov on 21.12.2022.
//

import UIKit

@objc
public class IGolfSocket: NSObject {
    
    @objc
    public func v3() {
        
    }
    
    private let socket = SocketManager()
    private var delegate: SocketManagerDelegate!
    
    public override init() {
        super.init()
    }
    
    @objc
    public func setDelegate(delegate: SocketManagerDelegate) {
        self.delegate = delegate
        socket.setDelegate(delegate: self.delegate)
    }
    
    @objc
    public func connect(url: String, port: Int) {
        socket.connect(url: url, port: port)
    }
    
    @objc
    public func sendEvent(event: String, json: String) {
        socket.sendEvent(event: event, json: json)
    }
}
