package com.transformice.config;

public class Config {
    public static final class transformice {
        public static final String ipAddress = "127.0.0.1";
        public static final int captcha = 4;
        public static final int[] ports = {57};
        public static final int[] packetkeys = {55,62,55,25,29,50,5,10,38,32,109,100,105,71,71,104,99,108,76,74};
        public static final int[] loginkeys = {-2147483648,-2147483648,256,16777216,13326141,256,16777216,10915256};

        public static int expBase = 24;
        public static int maxLevel = 100;
    }

    public static final class MySQL {
        public static final String Host = "127.0.0.1";
        public static final String User = "thalys";
        public static final String Password = "123";
        public static final String DatabaseName = "thalys_transformice";
        public static final int Port = 3306;
        public static final int MaxConnections = 2;
    }
}
