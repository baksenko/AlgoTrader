package algotrader.ingestor;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisPublisher {
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> syncCommands;

    public RedisPublisher(String redisUrl) {
        this.redisClient = RedisClient.create(redisUrl);
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
    }

    public void publish(String channel, String message) {
        syncCommands.publish(channel, message);
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}
