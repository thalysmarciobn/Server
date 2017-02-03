package com.transformice.network.events;

import com.transformice.config.Config;
import com.transformice.network.packet.ByteArray;
import com.transformice.network.packet.Packet;
import com.transformice.network.packet.PacketEvent;
import com.transformice.server.Server;
import com.transformice.server.helpers.Identifiers;
import jdbchelper.JdbcException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

@PacketEvent(C = Identifiers.recv._26.C, CC = Identifiers.recv._26.login)
public class Login implements Packet {

    @Override
    public void parse(Server server, ConcurrentHashMap player, ByteArray packet, int packetID) {
        packet = server.packetManage.decrypt(packetID, packet, Config.transformice.packetkeys);
        String playerName = server.users.parsePlayerName(packet.readUTF());
        String password = packet.readUTF();
        player.put(Identifiers.player.link, packet.readUTF());
        String startRoom = packet.readUTF();
        int resultKey = packet.readInt();
        int authKey = (Integer) player.get(Identifiers.player.authkey);
        Channel channel = (Channel) player.get(Identifiers.player.channel);
        boolean canLogin = true;
        for (int key : Config.transformice.loginkeys) {
            authKey ^= key;
        }
        if (!playerName.matches("^[A-Za-z][A-Za-z0-9_]{2,11}$") || playerName.length() > 25 || (playerName.length() >= 1 && playerName.substring(1).contains("+"))) {
            channel.close();
        } else if (authKey == resultKey && player.get(Identifiers.player.username).toString().isEmpty()) {
            playerName = playerName.equals("") ? "Souris" : playerName;
            if (server.users.players.containsKey(playerName)) {
                server.users.sendPacket(channel, Identifiers.send.login_result, 1);
            } else {
                if (!password.equals("")) {
                    server.database.jdbc.beginTransaction();
                    try {
                        if (server.users.login(player, playerName, password)) {
                            player.replace(Identifiers.player.isGuest, false);
                        } else {
                            server.users.sendPacket(channel, Identifiers.send.login_result, 6);
                            canLogin = false;
                        }
                    } catch (JdbcException error) {
                        server.users.sendPacket(channel, Identifiers.send.login_result, 6);
                        System.out.println(error.getCause());
                        if (server.database.jdbc.isInTransaction()) {
                            server.database.jdbc.rollbackTransaction();
                        }
                        canLogin = false;
                    } finally {
                        if (server.database.jdbc.isInTransaction()) {
                            server.database.jdbc.commitTransaction();
                        }
                    }
                }
            }
            if (canLogin) {
                server.users.enterPlayer(channel, player);
            }
        }
    }
}
