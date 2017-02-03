package com.transformice.network.packet;

import com.transformice.server.Server;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public interface Packet {
    void parse(Server server, ConcurrentHashMap player, ByteArray packet, int packetID);
}
