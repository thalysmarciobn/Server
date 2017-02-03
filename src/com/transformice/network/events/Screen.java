package com.transformice.network.events;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.Server;
import com.transformice.server.helpers.Identifiers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._28.C, CC = Identifiers.recv._28.screen)
public class Screen implements Packet {

    @Override
    public void parse(Server server, ConcurrentHashMap player, ByteArray packet, int packetID) {
        Channel channel = (Channel) player.get(Identifiers.player.channel);
        server.users.sendPacket(channel, Identifiers.send.version, new ByteArray().writeInt(server.users.players.size()).writeByte((Integer) player.get(Identifiers.player.lastpacket)).writeUTF(player.get(Identifiers.player.langue).toString().toLowerCase()).writeUTF(player.get(Identifiers.player.langue).toString().toLowerCase()).writeInt((Integer) player.get(Identifiers.player.authkey)).toByteArray());
        server.users.sendPacket(channel, Identifiers.send.banner, 52, 0);
        server.users.sendPacket(channel, Identifiers.send.image, new ByteArray().writeUTF("x_noel2014.jpg").toByteArray());
    }
}
