package com.transformice.network.events;

import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.Server;
import com.transformice.server.helpers.Identifiers;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._8.C, CC = Identifiers.recv._8.langue)
public class Langue implements Packet {

    @Override
    public void parse(Server server, ConcurrentHashMap player, ByteArray packet, int packetID) {
        int langueID = packet.readByte();
        String langue = server.langues.getLangue(langueID);
        player.replace(Identifiers.player.languebyte, langueID);
        player.replace(Identifiers.player.langue, langue);
    }
}
