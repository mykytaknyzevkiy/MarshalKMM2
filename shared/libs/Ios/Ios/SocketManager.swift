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
    
    func connect(url: String, port: Int) {
        self.socketIO = nil
        
        self.socketIO = SocketIO(delegate: self)
        self.socketIO.connect(toHost: url, onPort: port)
    }
    
    func socketIODidConnect(_ socket: SocketIO!) {
        print("socket did connect")
    }
    
    func socketIO(_ socket: SocketIO!, didReceiveMessage packet: SocketIOPacket!) {
        print("socket didReceiveMessage")
    }
    
    func socketIO(_ socket: SocketIO!, onError error: Error!) {
        print(error.localizedDescription)
    }
}
