//
//  SocketManager.swift
//  Ios
//
//  Created by Nekbakht Zabirov on 21.12.2022.
//

import Foundation
import socket_IO

class SocketManager: NSObject, SocketIODelegate {
    private var socketIO: SocketIO!
    private var delegate: SocketManagerDelegate!
    
    func setDelegate(delegate: SocketManagerDelegate) {
        self.delegate = delegate
    }
    
    func connect(url: String, port: Int) {
        self.socketIO = nil
        
        print("socketIO onConnect " + url + ":" + String(port))
        
        self.socketIO = SocketIO(delegate: self)
        self.socketIO.useSecure = true
        self.socketIO.connect(toHost: url, onPort: port)
    }
    
    func socketIODidConnect(_ socket: SocketIO!) {
        print("socket did connect")
        delegate.didConnected()
    }
    
    func socketIO(_ socket: SocketIO!, didReceiveMessage packet: SocketIOPacket!) {
        print("socket didReceiveMessage")
        
        if let data = packet.packetData {
                    if let jsonObject = try? JSONSerialization.jsonObject(with: data, options: []) {
                        if let jsonData = try? JSONSerialization.data(
                            withJSONObject: jsonObject,
                            options: JSONSerialization.WritingOptions.prettyPrinted
                        ) as NSData {
                            let body = NSString(data: jsonData as Data, encoding: NSUTF8StringEncoding)! as String
                            
                            delegate.onMessage(message: body)
                        }
                    }
                }
    }
    
    func socketIO(_ socket: SocketIO!, onError error: Error!) {
        print(error.localizedDescription)
        delegate.onError(error: error.localizedDescription)
    }
    
    func sendEvent(event: String, json: String) {
        let data = json.data(using: .utf8)!
            
        if let nsDir = try? JSONSerialization.jsonObject(with: data, options: []) as? NSDictionary {
            self.socketIO.sendEvent(event, withData: nsDir)
        }
    }
}
