package com.transformice.server;

import com.transformice.config.Config;
import com.transformice.database.Database;
import com.transformice.network.packet.PacketManage;
import com.transformice.server.helpers.Langues;
import com.transformice.server.helpers.Tribulle;
import com.transformice.server.users.Skills;
import com.transformice.server.users.Users;
import jdbchelper.QueryResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.netty.channel.Channel;
import com.transformice.network.Bootstrap;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Server {

    public Database database;
    public Langues langues;
    public PacketManage packetManage;
    public Users users;
    public Tribulle tribulle;

    public Long startServer;

    public List<Channel> channels = new ArrayList();

    private ArrayList ports = new ArrayList();

    private ScheduledExecutorService tasks = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public void start() {
        this.database = new Database();
        if (this.database.connect()) {
            this.startServer = System.nanoTime();
            this.langues = new Langues();
            this.users = new Users(this);
            this.users.skills = new Skills(this.users);
            this.tribulle = new Tribulle(this, this.users);
            this.packetManage = new PacketManage(this.users);
            this.println("Server loaded in: " + ((System.nanoTime() - this.startServer) / 1000000) + "ms", "info");
            for (int port : Config.transformice.ports) {
                this.channels.add(new Bootstrap(this).boot().bind(new InetSocketAddress(Config.transformice.ipAddress, port)));
                this.ports.add(port);
            }
            this.println("Server online on ports: " + this.ports.toString(), "info");
            this.scheduleTask(()-> this.database.freeIdleConnections(), 0, 30L, TimeUnit.SECONDS, true);
        }
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu) {
        return this.scheduleTask(task, delay, tu, false);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit tu, boolean repeat) {
        return repeat ? this.tasks.scheduleAtFixedRate(task, delay, delay, tu) : this.tasks.schedule(task, delay, tu);
    }

    public ScheduledFuture<?> scheduleTask(Runnable task, long start, long delay, TimeUnit tu, boolean repeat) {
        return repeat ? this.tasks.scheduleAtFixedRate(task, start, delay, tu) : this.tasks.schedule(task, delay, tu);
    }

    public void println(String message, String type) {
        System.out.println("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] [" + type + "] " + message);
    }

    public String getRandom(int size) {
        return RandomStringUtils.random(size, "ABCDE");
    }

    public boolean checkExistingUser(String playerName) {
        return this.database.jdbc.query("SELECT id FROM users WHERE Username = ?", playerName).next();
    }

    public int getTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public int getShamanLevelByExperience(int experience) {
        return (experience / Config.transformice.expBase) + 1;
    }

    public int getNextExperienceByShamanLevel(int level) {
        return level * Config.transformice.expBase;
    }
}
