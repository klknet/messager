package com.konglk.xmpp;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by konglk on 2019/8/9.
 */
public class OpenFileManager {

    public static AbstractXMPPConnection connect(String username, String password) throws Exception {
        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
        configBuilder.setUsernameAndPassword(username, password);
        configBuilder.setResource("xylink");
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//        configBuilder.enableDefaultDebugger();
        configBuilder.setXmppDomain("192.168.183.100");

        AbstractXMPPConnection connection = new XMPPTCPConnection(configBuilder.build());
// Connect to the server
        connection.connect();
// Log into the server
        connection.login();
        return connection;
    }

    public static void main(String[] args) {
        try {
            AbstractXMPPConnection connection = connect("konglk", "konglk");
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("在线");
            connection.sendStanza(presence);


            ChatManager chatManager= ChatManager.getInstanceFor(connection);//从连接中得到聊天管理器
            EntityBareJid jid = JidCreate.entityBareFrom("qintian@192.168.183.100");
            Chat chat = chatManager.chatWith(jid);
            chatManager.addIncomingListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                    System.out.println("New message from " + from + ": " + message.getBody());
//                    try {
//                        chat.send("hello u "+System.currentTimeMillis());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String val = in.readLine();
                if ("exit".equals(val)) {
                    break;
                }
                chat.send(val);

            }
            connection.disconnect(presence);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
