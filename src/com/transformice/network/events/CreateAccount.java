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

@PacketEvent(C = Identifiers.recv._26.C, CC = Identifiers.recv._26.create_account)
public class CreateAccount implements Packet {

    @Override
    public void parse(Server server, ConcurrentHashMap player, ByteArray packet, int packetID) {
        packet = server.packetManage.decrypt(packetID, packet, Config.transformice.packetkeys);
        String playerName = server.users.parsePlayerName(packet.readUTF());
        String password = packet.readUTF();
        String captcha = packet.readUTF();
        player.replace(Identifiers.player.link, packet.readUTF());
        Channel channel = (Channel) player.get(Identifiers.player.channel);
        if (!playerName.matches("^[A-Za-z][A-Za-z0-9_]{2,11}$")) {
            channel.close();
        } else if (server.checkExistingUser(playerName)) {
            server.users.sendPacket(channel, Identifiers.send.login_result, 3);
        } else if (this.function(playerName)) {
            server.users.sendPacket(channel, Identifiers.send.login_result, 4);
        } else if (!captcha.equals(player.get(Identifiers.player.captcha))) {
            server.users.sendPacket(channel, Identifiers.send.login_result, 7);
        } else {
            server.database.jdbc.beginTransaction();
            try {
                server.database.jdbc.run("INSERT INTO users (Username, Password, DateCreated, Address) VALUES (?, ?, NOW(), ?)", playerName, password, player.get(Identifiers.player.ipaddress));
                if (server.users.login(player, playerName, password)) {
                    player.replace(Identifiers.player.username, playerName);
                    player.replace(Identifiers.player.isNew, true);
                    player.replace(Identifiers.player.isGuest, false);
                    server.users.enterPlayer(channel, player);
                } else {
                    server.users.sendPacket(channel, Identifiers.send.login_result, 6);
                }
            } catch(JdbcException error){
                server.users.sendPacket(channel, Identifiers.send.login_result, 6);
                if (server.database.jdbc.isInTransaction()) {
                    server.database.jdbc.rollbackTransaction();
                }
            } finally{
                if (server.database.jdbc.isInTransaction()) {
                    server.database.jdbc.commitTransaction();
                }
            }
        }
    }

    private boolean function(String playerName) {
        if (playerName.charAt(0) == 0 || playerName.charAt(0) == 1 || playerName.charAt(0) == 2 || playerName.charAt(0) == 3 || playerName.charAt(0) == 4 || playerName.charAt(0) == 5 || playerName.charAt(0) == 6 || playerName.charAt(0) == 7 || playerName.charAt(0) == 8 || playerName.charAt(0) == 9) {
            return true;
        }
        return false;
    }
}
