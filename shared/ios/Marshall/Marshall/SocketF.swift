//
//  SocketF.swift
//  Marshall
//
//  Created by Nekbakht Zabirov on 19.12.2022.
//

import UIKit
import FSocket

public class SocketF: NSObject {
    private var delegate: SocketFDelegate!
    
    private let nSocket = NSocket()

    public func setDelegate(delegate: SocketFDelegate) {
        self.delegate = delegate
    }
    
    public func connect(url: String, port: Int) {
        nSocket.connect(url: url, port: port, delegate: self)
    }
    
    public func disconnect() {
        nSocket.disconnect()
    }
    
    public func sendEvent(event: String, json: String) {
        let data = json.data(using: .utf8)!
        
        if let nsDir = try? JSONSerialization.jsonObject(with: data, options: []) as? NSDictionary {
            nSocket.sendEvent(event: event, data: nsDir)
        }
    }
}

extension SocketF: NSocketDeleagte {
    public func onConnected() {
        delegate.onConnected()
        
    }
    
    public func onError(error: String) {
        delegate.onError(error: error)
    }
    
    public func onMessage(message: Data) {
        if let jsonObject = try? JSONSerialization.jsonObject(with: message, options: []) {
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
