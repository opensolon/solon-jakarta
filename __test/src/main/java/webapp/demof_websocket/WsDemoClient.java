/*
 * Copyright 2017-2025 noear.org and authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package webapp.demof_websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Iterator;

public class WsDemoClient extends WebSocketClient {
    public WsDemoClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        System.out.println("握手...");
        for(Iterator<String> it = shake.iterateHttpFields(); it.hasNext();) {
            String key = it.next();
            System.out.println(key+":"+shake.getFieldValue(key));
        }
    }

    @Override
    public void onMessage(String s) {
        System.out.println("接收到消息："+s);

        send("close");
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("关闭...");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("异常"+e);
    }
}
