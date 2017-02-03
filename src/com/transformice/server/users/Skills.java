package com.transformice.server.users;

import com.transformice.network.packet.ByteArray;
import com.transformice.server.helpers.Identifiers;
import org.jboss.netty.channel.Channel;

public class Skills {
    private Users users;

    public Skills(Users users) {
        this.users = users;
    }

    public void sendExp(Channel channel, int level, int exp, int nextLevel) {
        this.users.sendPacket(channel, Identifiers.send.shaman_exp, new ByteArray().writeShort(level - 1).writeInt(exp).writeInt(nextLevel).toByteArray());
    }

    public void sendShamanSkills(Channel channel, boolean type) {
        ByteArray packet = new ByteArray().writeByte(0); // size
        this.users.sendPacket(channel, Identifiers.send.shaman_skills, packet.writeBoolean(type).toByteArray());
    }
}
