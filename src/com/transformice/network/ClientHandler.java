package com.transformice.network;

import com.transformice.config.Config;
import com.transformice.network.packet.ByteArray;
import com.transformice.server.Server;
import com.transformice.server.helpers.Identifiers;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

public class ClientHandler extends SimpleChannelHandler {
    private final Server server;

    public ClientHandler(Server server) {
        this.server = server;
    }

    @Override
    public void channelOpen(ChannelHandlerContext context, ChannelStateEvent e) {
        ConcurrentHashMap player = new ConcurrentHashMap();
        player.put(Identifiers.player.id, 0);
        player.put(Identifiers.player.playerCode, 0);
        player.put(Identifiers.player.username, "");
        player.put(Identifiers.player.channel, context.getChannel());
        player.put(Identifiers.player.lastpacket, ThreadLocalRandom.current().nextInt(0,99));
        player.put(Identifiers.player.authkey, ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE));
        player.put(Identifiers.player.langue, "BR");
        player.put(Identifiers.player.languebyte, 3);
        player.put(Identifiers.player.isNew, false);
        player.put(Identifiers.player.isGuest, true);
        player.put(Identifiers.player.privilege, 0);
        player.put(Identifiers.player.experience, 0);
        player.put(Identifiers.player.captcha, this.server.getRandom(Config.transformice.captcha));
        player.put(Identifiers.player.ipaddress, ((InetSocketAddress) context.getChannel().getRemoteAddress()).getAddress().getHostAddress());
        context.getChannel().setAttachment(player);
    }

    @Override
    public void channelClosed(ChannelHandlerContext context, ChannelStateEvent e) {
        ((ConcurrentHashMap) context.getChannel().getAttachment()).clear();
    }

    @Override
    public void messageReceived(ChannelHandlerContext context, MessageEvent e) {
        if ((e.getMessage() instanceof byte[])) {
            byte[] buff = (byte[]) e.getMessage();
            if (buff != null && buff.length > 2) {
                this.parse(context, new ByteArray(buff), buff);
            }
        }
    }

    public void parse(ChannelHandlerContext context, ByteArray packet, byte[] buff) {
        if (packet.size() > 2) {
            byte sizeBytes = packet.readByte();
            int length = sizeBytes == 1 ? packet.readUnsignedByte() : sizeBytes == 2 ? packet.readUnsignedShort() : sizeBytes == 3 ? ((packet.readUnsignedByte() & 0xFF) << 16) | ((packet.readUnsignedByte() & 0xFF) << 8) | (packet.readUnsignedByte() & 0xFF) : 0;
            if (length != 0) {
                byte packetID = packet.readByte();
                if (packet.size() == length) {
                    if (packet.size() >= 2) {
                        this.parse(context.getChannel(), packet, packetID);
                    }
                } else if (packet.size() > length) {
                    byte[] data = packet.read(new byte[length]);
                    if (length >= 2) {
                        this.parse(context.getChannel(), new ByteArray(data), packetID);
                    }
                }
            }
        }
    }

    public void parse(Channel channel, ByteArray packet, int packetID) {
        byte[] token = {packet.readByte(), packet.readByte()};
        ConcurrentHashMap player = (ConcurrentHashMap) channel.getAttachment();
        this.parse(player, token, packet, packetID);
    }

    public void parse(ConcurrentHashMap player, byte[] tokens, ByteArray packet, int packetID) {
        int header = (tokens[0] << 8) | tokens[1];
        if (this.server.packetManage.packets.containsKey(header)) {
            this.server.packetManage.packets.get(header).parse(this.server, player, packet, packetID);
        }
    }
}
