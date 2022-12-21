//
//  IGolfSocket.swift
//  Ios
//
//  Created by Nekbakht Zabirov on 21.12.2022.
//

import UIKit

@objc
public class IGolfSocket: NSObject {
    private let socket = SocketManager()
    
    @objc
    public func connect(url: String, port: Int) {
        socket.connect(url: url, port: port)
    }
}
