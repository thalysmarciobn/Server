package com.transformice.network.packet;

import com.transformice.network.events.*;
import com.transformice.server.users.Users;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public class PacketManage {

    public ConcurrentHashMap<Integer, Packet> packets = new ConcurrentHashMap();

    private Users users;

    public PacketManage(Users users) {
        this.users = users;
        this.registry(new Screen());
        this.registry(new Login());
        this.registry(new CreateAccount());
        this.registry(new Langue());
        this.registry(new Captcha());
    }

    public ByteArray decrypt(int packetID, ByteArray packet, int[] keys) {
        ByteArray data = new ByteArray();
        while (packet.bytesAvailable()) {
            packetID = ++packetID % keys.length;
            data.writeByte(packet.readByte() ^ keys[packetID]);
        }
        return data;
    }

    private void registry(Packet packet) {
        Class<? extends  Packet> _class = packet.getClass();
        if (_class.getAnnotations().length > 0) {
            PacketEvent event = _class.getAnnotation(PacketEvent.class);
            this.packets.put((event.C() << 8) | event.CC(), packet);
        }
    }
}
