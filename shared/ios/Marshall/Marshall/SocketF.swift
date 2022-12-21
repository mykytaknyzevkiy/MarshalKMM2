//
//  SocketF.swift
//  Marshall
//
//  Created by Nekbakht Zabirov on 19.12.2022.
//

import UIKit
import socket_IO

public class SocketF: NSObject {
    private var delegate: SocketFDelegate!
    
    private var socket: SocketIO!
    
    public func setDelegate(delegate: SocketFDelegate) {
        self.delegate = delegate
    }
    
    public func connect(url: String, port: Int) {
        socket = nil
        
        socket = SocketIO(delegate: self)
        
        socket.useSecure = true
        socket.connect(toHost: url, onPort: port)
    }
    
    public func disconnect() {
        socket.disconnect()
    }
    
    public func sendEvent(event: String, json: String) {
        let data = json.data(using: .utf8)!
        
        if let nsDir = try? JSONSerialization.jsonObject(with: data, options: []) as? NSDictionary {
            socket.sendEvent(event, withData: nsDir)
        }
    }
}

extension SocketF: SocketIODelegate {
    public func socketIODidConnect(_ socket: SocketIO!) {
        delegate.onConnected()
    }
    
    public func socketIO(_ socket: SocketIO!, onError error: Error!) {
        delegate.onError(error: error.localizedDescription)
    }
    
    public func socketIO(_ socket: SocketIO!, didReceiveMessage packet: SocketIOPacket!) {
        if let data = packet.packetData {
            if let jsonObject = try? JSONSerialization.jsonObject(with: data, options: []) {
                if let jsonData = try? JSONSerialization.data(
                    withJSONObject: jsonObject,
                    options: JSONSerialization.WritingOptions.prettyPrinted
                ) as NSData {
                    let body = NSString(data: jsonData as Data, encoding: NSUTF8StringEncoding)! as String
                    
                    delegate.onMessage(json: body)
                }
            }
        }
    }
    
    public func socketIODidDisconnect(_ socket: SocketIO!, disconnectedWithError error: Error!) {
    }
}
