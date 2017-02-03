package com.transformice.server.users;

import com.transformice.network.packet.ByteArray;
import com.transformice.server.Server;
import com.transformice.server.helpers.Identifiers;
import jdbchelper.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;

public class Users {
    public ConcurrentHashMap players = new ConcurrentHashMap();

    private Server server;
    public Skills skills;

    private int lastPlayerCode = 0;

    public Users(Server server) {
        this.server = server;
    }

    public void sendPacket(Channel channel, int[] identifiers, byte... data) {
        ByteArray packet = new ByteArray();
        int length = data.length + 2;
        if (length <= 0xFF) {
            packet.writeByte(1).writeByte(length);
        } else if (length <= 0xFFFF) {
            packet.writeByte(2).writeShort(length);
        } else if (length <= 0xFFFFFF) {
            packet.writeByte(3).writeByte((length >> 16) & 0xFF).writeByte((length >> 8) & 0xFF).writeByte(length & 0xFF);
        }
        packet.writeByte(identifiers[0]).writeByte(identifiers[1]).writeBytes(data);
        channel.write(ChannelBuffers.wrappedBuffer(packet.toByteArray()));
    }

    public final void sendPacket(Channel channel, int[] identifiers, int... data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) data[i];
        }
        this.sendPacket(channel, identifiers, result);
    }

    public String parsePlayerName(String playerName) {
        return playerName.startsWith("*") ? "*" + StringUtils.capitalize(playerName.substring(1).toLowerCase()) : StringUtils.capitalize(playerName.toLowerCase());
    }

    public boolean login(ConcurrentHashMap player, String playerName, String password) {
        QueryResult result = this.server.database.jdbc.query("select * from users where Username = ? and Password = ?", playerName, password);
        if (result.next()) {
            player.replace(Identifiers.player.id, result.getInt("id"));
            player.replace(Identifiers.player.username, result.getString("Username"));
            player.replace(Identifiers.player.privilege, result.getInt("Privilege"));
            player.replace(Identifiers.player.experience, result.getInt("Experience"));
            result.close();
            return true;
        }
        result.close();
        return false;
    }

    public void enterPlayer(Channel channel, ConcurrentHashMap player) {
        player.replace(Identifiers.player.playerCode, this.lastPlayerCode++);
        this.skills.sendShamanSkills(channel, false);
        this.skills.sendExp(channel, this.server.getShamanLevelByExperience((Integer) player.get(Identifiers.player.experience)), 0, this.server.getNextExperienceByShamanLevel((Integer) player.get(Identifiers.player.experience)));
        if ((Boolean) player.get(Identifiers.player.isGuest)) {
            this.sendPacket(channel, Identifiers.send.login_souris, new ByteArray().writeByte(1).writeByte(10).toByteArray());
            this.sendPacket(channel, Identifiers.send.login_souris, new ByteArray().writeByte(2).writeByte(5).toByteArray());
            this.sendPacket(channel, Identifiers.send.login_souris, new ByteArray().writeByte(3).writeByte(15).toByteArray());
            this.sendPacket(channel, Identifiers.send.login_souris, new ByteArray().writeByte(4).writeByte(200).toByteArray());
        }
        this.sendPacket(channel, Identifiers.send.player_identification, new ByteArray().writeInt((Integer) player.get(Identifiers.player.id)).writeUTF((String) player.get(Identifiers.player.username)).writeInt(600000).writeByte((Integer) player.get(Identifiers.player.languebyte)).writeInt((Integer) player.get(Identifiers.player.playerCode)).writeByte((Integer) player.get(Identifiers.player.privilege)).writeByte(0).writeBoolean(false).toByteArray());
        this.sendShamanItems(channel, player);
        this.sendPacket(channel, Identifiers.send.time_stamp, new ByteArray().writeInt(this.server.getTime()).toByteArray());
        this.sendPacket(channel, Identifiers.send.email_confirmed, 1);
        this.server.tribulle.sendPlayerInfo(player);
        this.server.tribulle.sendFriendList(player, null);
        this.server.tribulle.sendIgnoredsList(player);
        this.server.tribulle.sendTribe(player, false);
        this.players.put((Integer) player.get(Identifiers.player.id), player);

    }

    public void sendShamanItems(Channel channel, ConcurrentHashMap player) {
        ByteArray packet = new ByteArray();
        packet.writeShort(0); // size
        this.sendPacket(channel, Identifiers.send.shaman_items, packet.toByteArray());
    }
}
