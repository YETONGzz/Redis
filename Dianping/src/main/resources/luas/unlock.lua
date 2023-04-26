--判断线程标识 与要释放的锁的线程标识是否一致
if (redis.call('hget', KEYS[1], ARGV[1]) == 0) then
    --一致释放
    return redis.call('del', KEYS[1])
end
redis.call('hincrby', KEYS[1], ARGV[1], -1);

