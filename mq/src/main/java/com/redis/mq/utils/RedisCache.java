package com.redis.mq.utils;

import com.alibaba.fastjson.JSON;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;


@Configuration
public class RedisCache {
    private static final int LOCK_DEFAULT_EXPIRE_TIME = 3;
    private static Logger log = LoggerFactory.getLogger(RedisCache.class);
    private static String DEFAULT_KEY_PREFIX = "";
    private static int DEFAULT_EXPIRE_TIME = 0;
    private static String ADDR = null;
    private static int PORT = 6379;
    private static String AUTH = null;
    private static int MAX_ACTIVE = 300;
    private static int MAX_IDLE = 200;
    private static int MAX_WAIT = 10000;
    private static int TIMEOUT = 10000;
    private static boolean TEST_ON_BORROW = true;
    private static JedisPool jedisPool = null;
    private static Jedis jedis = null;

    public RedisCache() {
    }

    public static String getDefaultKeyPrefix() {
        return DEFAULT_KEY_PREFIX;
    }

    @Value("${redis.key.prefix:prefix}")
    public void setDefaultKeyPrefix(String redisKeyPrefix) {
        DEFAULT_KEY_PREFIX = redisKeyPrefix;
    }

    private static synchronized void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis((long)MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        config.setMaxTotal(MAX_ACTIVE);
        jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT, AUTH);
    }

    public static String set(String cacheType, String key, String value) {
        return set(generateFullKey(cacheType, key), value, DEFAULT_EXPIRE_TIME);
    }

    public static String set(String cacheType, String key, String value, int expireSeconds) {
        return set(generateFullKey(cacheType, key), value, expireSeconds);
    }

    public static String set(String prefix, String cacheType, String key, String value) {
        return set(generateFullKey(prefix, cacheType, key), value, DEFAULT_EXPIRE_TIME);
    }

    public static String set(String prefix, String cacheType, String key, String value, int expireSeconds) {
        return set(generateFullKey(prefix, cacheType, key), value, expireSeconds);
    }

    public static String setByExpireAt(String prefix, String cacheType, String key, String value, int expireSeconds) {
        return setByExpireAt(generateFullKey(prefix, cacheType, key), value, expireSeconds);
    }

    private static String set(String fullKey, String value, int expireSeconds) {
        String result = "FAIL";
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (jedis != null) {
                result = jedis.set(fullKey, value);
                if (expireSeconds > 0) {
                    jedis.expire(fullKey, expireSeconds);
                }
            }
        } catch (Exception var9) {
            log.error("Redis设置单个值异常：{}", var9);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static String setByExpireAt(String fullKey, String value, int expireSeconds) {
        String result = "FAIL";
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (jedis != null) {
                result = jedis.set(fullKey, value);
                if (expireSeconds > 0) {
                    jedis.expireAt(fullKey, (long)expireSeconds);
                }
            }
        } catch (Exception var9) {
            log.error("Redis设置单个值异常：{}", var9);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static String setObj(String cacheType, String key, Object value) {
        return setObj(generateFullKey(cacheType, key), value, DEFAULT_EXPIRE_TIME);
    }

    public static String setObj(String cacheType, String key, Object value, int expireSeconds) {
        return setObj(generateFullKey(cacheType, key), value, expireSeconds);
    }

    public static String setObj(String prefix, String cacheType, String key, Object value, int expireSeconds) {
        return setObj(generateFullKey(prefix, cacheType, key), value, expireSeconds);
    }

    public static String setObjByExpireAt(String cacheType, String key, Object value, long expireSeconds) {
        return setObjByExpireAt(generateFullKey(cacheType, key), value, expireSeconds);
    }

    private static String setObj(String fullKey, Object value, int expireTime) {
        String result = "FAIL";
        Jedis jedis = null;

        try {
            jedis = getJedis();
            byte[] bs = UtilSerialize.serialize(value);
            if (bs != null) {
                result = jedis.set(fullKey.getBytes(), bs);
                if (expireTime > 0) {
                    jedis.expire(fullKey, expireTime);
                }
            }
        } catch (Exception var9) {
            log.error("Redis设置Object值异常：{}", var9);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return result;
    }

    private static String setObjByExpireAt(String fullKey, Object value, long expireTime) {
        String result = "FAIL";
        Jedis jedis = null;

        try {
            jedis = getJedis();
            byte[] bs = UtilSerialize.serialize(value);
            if (bs != null) {
                result = jedis.set(fullKey.getBytes(), bs);
                if (expireTime > 0L) {
                    jedis.expireAt(fullKey, expireTime);
                }
            }
        } catch (Exception var10) {
            log.error("Redis设置Object值异常：{}", var10);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return result;
    }

    public static String get(String cacheType, String key) {
        return get(generateFullKey(cacheType, key));
    }

    public static String get(String prefix, String cacheType, String key) {
        return get(generateFullKey(prefix, cacheType, key));
    }

    private static String get(String fullKey) {
        Jedis jedis = null;

        String var2;
        try {
            jedis = getJedis();
            if (jedis == null) {
                return null;
            }

            var2 = jedis.get(fullKey);
        } catch (Exception var6) {
            log.error("Redis获取单个值数据异常：{}", var6);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return var2;
    }

    public static Object getObj(String cacheType, String key) {
        return getObj(generateFullKey(cacheType, key));
    }

    public static Object getObj(String prefix, String cacheType, String key) {
        return getObj(generateFullKey(prefix, cacheType, key));
    }

    private static Object getObj(String fullKey) {
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (jedis != null) {
                byte[] value = jedis.get(fullKey.getBytes());
                if (value != null) {
                    Object var3 = UtilSerialize.unSerialize(value);
                    return var3;
                }
            }
        } catch (Exception var7) {
            log.error("Redis获取Object数据异常：{}", var7);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return null;
    }

    public static boolean del(String cacheType, String key) {
        return del(generateFullKey(cacheType, key));
    }

    public static boolean del(String prefix, String cacheType, String key) {
        return del(generateFullKey(prefix, cacheType, key));
    }

    private static boolean del(String fullKey) {
        Boolean result = Boolean.FALSE;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (jedis != null) {
                jedis.del(fullKey);
                result = Boolean.TRUE;
            }
        } catch (Exception var7) {
            log.error("删除Redis数据异常：{}", var7);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long append(String cacheType, String key, String value) {
        return append(generateFullKey(cacheType, key), value);
    }

    public static Long append(String prefix, String cacheType, String key, String value) {
        return append(generateFullKey(prefix, cacheType, key), value);
    }

    private static Long append(String fullKey, String value) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (jedis != null) {
                result = jedis.append(fullKey, value);
            }
        } catch (Exception var8) {
            log.error("追加Redis数据异常:{}", var8);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return result;
    }

    public static Boolean exists(String cacheType, String key) {
        return exists(generateFullKey(cacheType, key));
    }

    public static Boolean exists(String prefix, String cacheType, String key) {
        return exists(generateFullKey(prefix, cacheType, key));
    }

    private static Boolean exists(String fullKey) {
        Boolean result = Boolean.FALSE;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (jedis != null) {
                result = jedis.exists(fullKey);
            }
        } catch (Exception var7) {
            log.error("检查key是否存在异常：{}", var7);
        } finally {
            if (jedis != null) {
                jedis.close();
            }

        }

        return result;
    }

    public static void subscribe(JedisPubSub subscriber, String channelName) {
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                jedis.subscribe(subscriber, new String[]{channelName});
            }
        } catch (Exception var7) {
            log.error("订阅频道失败", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

    }

    public static Long publish(String channel, String msg) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.publish(channel, msg);
            }
        } catch (Exception var8) {
            log.error("向频道发送消息失败", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static String hget(String prefix, String cacheType, String key, String field) {
        return hget(generateFullKey(prefix, cacheType, key), field);
    }

    public static String hget(String cacheType, String key, String field) {
        return hget(generateFullKey(cacheType, key), field);
    }

    private static String hget(String key, String field) {
        String result = null;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.hget(key, field);
            }
        } catch (Exception var8) {
            log.error("redis获取hash值异常", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long hset(String prefix, String cacheType, String key, String field, String value, int expireTime) {
        return hset(generateFullKey(prefix, cacheType, key), field, value, expireTime);
    }

    public static Long hset(String cacheType, String key, String field, String value, int expireTime) {
        return hset(generateFullKey(cacheType, key), field, value, expireTime);
    }

    public static Long hset(String cacheType, String key, String field, String value) {
        return hset(generateFullKey(cacheType, key), field, value, DEFAULT_EXPIRE_TIME);
    }

    public static Long hset(String cacheType, String key, String field, Object value) {
        return hset(generateFullKey(cacheType, key), field, value, DEFAULT_EXPIRE_TIME);
    }

    private static Long hset(String key, String field, String value, int expireTime) {
        Long result = -1L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.hset(key, field, value);
                if (expireTime > 0) {
                    jedis.expire(key, expireTime);
                }
            }
        } catch (Exception var10) {
            log.error("redis设置hash值异常", var10);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static Long hset(String key, String field, Object value, int expireTime) {
        Long result = -1L;
        Jedis jedis = null;
        String str = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                if (value instanceof String) {
                    str = (String)value;
                } else {
                    str = JSON.toJSONString(value);
                }

                result = jedis.hset(key, field, str);
                if (expireTime > 0) {
                    jedis.expire(key, expireTime);
                }
            }
        } catch (Exception var11) {
            log.error("redis设置hash值异常", var11);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long hsetnx(String cacheType, String key, String field, String value) {
        return hsetnx(generateFullKey(cacheType, key), field, value, DEFAULT_EXPIRE_TIME);
    }

    private static Long hsetnx(String key, String field, String value, int expireTime) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.hsetnx(key, field, value);
                if (expireTime > 0) {
                    jedis.expire(key, expireTime);
                }
            }
        } catch (Exception var10) {
            log.error("redis设置hash值异常", var10);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long hsetnx(String cacheType, String key, String field, String value, int expireTime) {
        return hsetnx(generateFullKey(cacheType, key), field, value, expireTime);
    }

    public static Set<String> hkeys(String cacheType, String key) {
        return hkeys(generateFullKey(cacheType, key));
    }

    private static Set<String> hkeys(String key) {
        Set<String> result = null;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.hkeys(key);
            }
        } catch (Exception var7) {
            log.error("redis获取keys异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Map<String, String> hgetAll(String prefix, String cacheType, String key) {
        return hgetAll(generateFullKey(prefix, cacheType, key));
    }

    public static Map<String, String> hgetAll(String cacheType, String key) {
        return hgetAll(generateFullKey(cacheType, key));
    }

    private static Map<String, String> hgetAll(String key) {
        Map<String, String> result = null;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.hgetAll(key);
            }
        } catch (Exception var7) {
            log.error("redis获取key的所有键值对异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long hdel(String cacheType, String key, String field) {
        return hdel(generateFullKey(cacheType, key), field);
    }

    private static Long hdel(String key, String field) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.hdel(key, new String[]{field});
            }
        } catch (Exception var8) {
            log.error("redis删除hash值异常", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Boolean lock(String cacheType, String key, int expireTime) {
        return lock(generateFullKey(cacheType, key), expireTime);
    }

    public static Boolean lock(String cacheType, String key) {
        return lock(generateFullKey(cacheType, key), 3);
    }

    public static Boolean unlock(String cacheType, String key) {
        return unlock(generateFullKey(cacheType, key));
    }

    private static Boolean lock(String key, int expireTime) {
        Boolean result = false;
        Jedis jedis = null;

        try {
            jedis = getJedis();

            for(int times = expireTime * 100; times > 0; --times) {
                Long status = jedis.setnx(key, "lock");
                if (1L == status) {
                    jedis.expire(key, expireTime);
                    result = true;
                    break;
                }

                Thread.sleep(10L);
            }
        } catch (Exception var9) {
            UtilLogger.errorByFormat(log, "redis加锁失败", new Object[]{var9});
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static Boolean unlock(String key) {
        Boolean result = false;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            jedis.del(key);
            result = true;
        } catch (Exception var7) {
            UtilLogger.errorByFormat(log, "redis解锁失败", new Object[]{var7});
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Boolean hexists(String cacheType, String key, String field) {
        return hexists(generateFullKey(cacheType, key), field);
    }

    private static Boolean hexists(String key, String field) {
        Jedis jedis = null;
        Boolean result = false;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.hexists(key, field);
            }
        } catch (Exception var8) {
            log.error("redis判断hash值是否存在异常", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long lpush(String cacheType, String key, String... items) {
        return lpush(generateFullKey(cacheType, key), items);
    }

    public static Long lpush(String cacheType, String key, Object... items) {
        String[] strings = new String[items.length];

        for(int i = 0; i < items.length; ++i) {
            if (items[i] instanceof String) {
                strings[i] = (String)items[i];
            } else {
                strings[i] = JSON.toJSONString(items[i]);
            }
        }

        return lpush(generateFullKey(cacheType, key), strings);
    }

    public static String lpop(String cacheType, String key) {
        return lpop(generateFullKey(cacheType, key));
    }

    public static Long rpush(String cacheType, String key, String... items) {
        return rpush(generateFullKey(cacheType, key), items);
    }

    public static Long rpush(String cacheType, String key, Object... items) {
        String[] strings = new String[items.length];

        for(int i = 0; i < items.length; ++i) {
            if (items[i] instanceof String) {
                strings[i] = (String)items[i];
            } else {
                strings[i] = JSON.toJSONString(items[i]);
            }
        }

        return lpush(generateFullKey(cacheType, key), strings);
    }

    public static String rpop(String cacheType, String key) {
        return rpop(generateFullKey(cacheType, key));
    }

    public static List<String> lrange(String cacheType, String key, long start, long end) {
        return lrange(generateFullKey(cacheType, key), start, end);
    }

    public static List<String> lrangeAll(String cacheType, String key) {
        return lrange(generateFullKey(cacheType, key), 0L, -1L);
    }

    private static Long lpush(String key, String... items) {
        Jedis jedis = null;
        Long result = 0L;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.lpush(key, items);
            }
        } catch (Exception var8) {
            log.error("redis lpush操作异常", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static String lpop(String key) {
        Jedis jedis = null;
        String result = "";

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.lpop(key);
            }
        } catch (Exception var7) {
            log.error("redis lpop操作异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static Long rpush(String key, String... items) {
        Jedis jedis = null;
        Long result = 0L;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.rpush(key, items);
            }
        } catch (Exception var8) {
            log.error("redis rpush操作异常", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static String rpop(String key) {
        Jedis jedis = null;
        String result = "";

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.rpop(key);
            }
        } catch (Exception var7) {
            log.error("redis rpop操作异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static List<String> lrange(String key, long start, long end) {
        Jedis jedis = null;
        List result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.lrange(key, start, end);
                jedis.ltrim(key, start, end);
            }
        } catch (Exception var11) {
            log.error("redis lrange操作异常", var11);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Double zincrby(String prefix, String cacheType, String key, double score, String member) {
        return zincrby(generateFullKey(prefix, cacheType, key), score, member);
    }

    private static Double zincrby(String key, double score, String member) {
        Jedis jedis = null;
        Double result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zincrby(key, score, member);
            }
        } catch (Exception var10) {
            log.error("redis执行zincrby存在异常", var10);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Double zscore(String prefix, String cacheType, String key, String member) {
        return zscore(generateFullKey(prefix, cacheType, key), member);
    }

    private static Double zscore(String key, String member) {
        Jedis jedis = null;
        Double result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zscore(key, member);
            }
        } catch (Exception var8) {
            log.error("redis执行zscore存在异常", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Set<Tuple> zrangeWithScores(String cacheType, String key, int start, long end) {
        return zrangeWithScores(generateFullKey(cacheType, key), start, end);
    }

    private static Set<Tuple> zrangeWithScores(String key, int start, long end) {
        Jedis jedis = null;
        Set result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zrangeWithScores(key, (long)start, end);
            }
        } catch (Exception var10) {
            log.error("redis执行zrangeWithScores存在异常", var10);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Set<String> zrangeByScore(String cacheType, String key, double min, double max) {
        return zrangeWithScore(generateFullKey(cacheType, key), min, max);
    }

    public static Set<String> zrangeWithScore(String key, double min, double max) {
        Jedis jedis = null;
        Set result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zrangeByScore(key, min, max);
            }
        } catch (Exception var11) {
            log.error("redis执行zrangeWithScores存在异常", var11);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Set<String> zrangeByScore(String cacheType, String key, String min, String max) {
        return zrangeWithScore(generateFullKey(cacheType, key), min, max);
    }

    public static String zGetFirstMember(String prefix, String cacheType1, String cacheType2, String key) {
        return zGetFirstMember(generateFullKey(prefix, cacheType1, cacheType2, key));
    }

    public static String zGetFirstMember(String key) {
        Jedis jedis = null;
        String result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                Set<String> set = jedis.zrange(key, 0L, 0L);
                if (set != null && !set.isEmpty()) {
                    result = (String)set.iterator().next();
                }
            }
        } catch (Exception var7) {
            log.error("redis执行zrangeWithScores存在异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Set<String> zrangeWithScore(String key, String min, String max) {
        Jedis jedis = null;
        Set result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zrangeByScore(key, min, max);
            }
        } catch (Exception var9) {
            log.error("redis执行zrangeWithScores存在异常", var9);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zadd(String prefix, String cacheType, String key, double score, Object member) {
        return zadd(generateFullKey(prefix, cacheType, key), score, member);
    }

    public static Long zadd(String prefix, String cacheType1, String cacheType2, String key, double score, String member) {
        return zadd(generateFullKey(prefix, cacheType1, cacheType2, key), score, member);
    }

    public static Long zadd(String cacheType, String key, double score, Object member) {
        return zadd(generateFullKey(cacheType, key), score, member);
    }

    private static Long zadd(String key, double score, Object member) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                String str;
                if (member instanceof String) {
                    str = (String)member;
                } else {
                    str = JSON.toJSONString(member);
                }

                result = jedis.zadd(key, score, str);
            }
        } catch (Exception var11) {
            log.error("redis执行zadd存在异常", var11);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    private static Long zadd(String key, double score, String member) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zadd(key, score, member);
            }
        } catch (Exception var10) {
            log.error("redis执行zadd存在异常", var10);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zcard(String prefix, String cacheType1, String cacheType2, String key) {
        return zcard(generateFullKey(prefix, cacheType1, cacheType2, key));
    }

    public static Long zcard(String cacheType, String key) {
        return zcard(generateFullKey(cacheType, key));
    }

    public static Set<String> getAllKeys(String prefix, String cacheType1, String cacheType2) {
        return getAllKeys(generateFullKey(prefix, cacheType1, cacheType2));
    }

    public static Set<String> getAllKeys(String key) {
        Jedis jedis = null;
        Set result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.keys(key + ":*");
            }
        } catch (Exception var7) {
            log.error("redis模糊查询所有key时发生异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zcard(String key) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zcard(key);
            }
        } catch (Exception var7) {
            log.error("redis执行zcard存在异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zrem(String prefix, String cacheType1, String cacheType2, String key, String member) {
        return zrem(generateFullKey(prefix, cacheType1, cacheType2, key), member);
    }

    public static Long zrem(String prefix, String cacheType, String key, Object member) {
        return zrem(generateFullKey(prefix, cacheType, key), member);
    }

    public static Long zrem(String prefix, String cacheType, String key, String member) {
        return zrem(generateFullKey(prefix, cacheType, key), member);
    }

    public static Long zrem(String cacheType, String key, String member) {
        return zrem(generateFullKey(cacheType, key), member);
    }

    private static Long zrem(String key, Object member) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                String str;
                if (member instanceof String) {
                    str = (String)member;
                } else {
                    str = JSON.toJSONString(member);
                }

                result = jedis.zrem(key, new String[]{str});
            }
        } catch (Exception var9) {
            log.error("redis删除zset值异常", var9);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zrem(String key, String member) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zrem(key, new String[]{member});
            }
        } catch (Exception var8) {
            log.error("redis删除zset值异常", var8);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zremrangeByRank(String key, long start, long end) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.zremrangeByRank(key, start, end);
            }
        } catch (Exception var11) {
            log.error("redis删除zset值异常", var11);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zdiffstore(String dstkey, String... keys) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                String[] var4 = keys;
                int var5 = keys.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    String key = var4[var6];
                    Set<Tuple> setA = jedis.zrangeWithScores(key, 0L, -1L);
                    if (setA != null && !setA.isEmpty()) {
                        result = jedis.zrem(dstkey, (String[])setA.stream().map(Tuple::getElement).toArray((x$0) -> {
                            return new String[x$0];
                        }));
                    }
                }
            }
        } catch (Exception var12) {
            log.error("redis zdiffstore操作异常", var12);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long zunionstore(String dstkey, String... keys) {
        Long result = 0L;
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                String[] var4 = keys;
                int var5 = keys.length;

                for(int var6 = 0; var6 < var5; ++var6) {
                    String key = var4[var6];
                    Set<Tuple> setA = jedis.zrangeWithScores(key, 0L, -1L);
                    if (setA != null && !setA.isEmpty()) {
                        result = jedis.zadd(dstkey, (Map)setA.stream().collect(Collectors.toMap(Tuple::getElement, Tuple::getScore)));
                    }
                }
            }
        } catch (Exception var12) {
            log.error("redis sunionstore", var12);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long incr(String prefix, String cacheType, String key) {
        return incr(generateFullKey(prefix, cacheType, key));
    }

    private static Long incr(String key) {
        Jedis jedis = null;
        Long result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = jedis.incr(key);
            }
        } catch (Exception var7) {
            log.error("redis执行incr存在异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static <R> R execOnSameRedisConn(Function<RedisCache.RedisTransaction, R> function) {
        Jedis jedis = null;
        R result = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                result = function.apply(new RedisCache.RedisTransaction(jedis));
            }
        } catch (Exception var7) {
            log.error("redis执行execOnSameRedisConn存在异常", var7);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return result;
    }

    public static Long getCurrentTimeMillisFromRedis() {
        Jedis jedis = null;

        try {
            jedis = getJedis();
            if (null != jedis) {
                List<String> time = jedis.time();
                if (UtilCompare.isNotEmpty(time) && time.size() >= 2) {
                    Long second = Long.parseLong((String)time.get(0));
                    Long microSecond = Long.parseLong((String)time.get(1));
                    Long currentTimeMillis = second * 1000L + microSecond / 1000L;
                    Long var5 = currentTimeMillis;
                    return var5;
                }
            }
        } catch (Exception var9) {
            log.error("redis获取时间戳异常", var9);
        } finally {
            if (null != jedis) {
                jedis.close();
            }

        }

        return null;
    }

    public static String generateFullKey(String prefix, String cacheType, String key) {
        return MessageFormat.format("{0}:{1}:{2}", prefix, cacheType, key);
    }

    public static String generateFullKey(String prefix, String cacheType1, String cacheType2, String key) {
        return MessageFormat.format("{0}:{1}:{2}:{3}", prefix, cacheType1, cacheType2, key);
    }

    public static String generateFullKey(String cacheType, String key) {
        return MessageFormat.format("{0}:{1}:{2}", DEFAULT_KEY_PREFIX, cacheType, key);
    }

    private static Jedis getJedis() {
        try {
            if (jedisPool == null) {
                init();
            }

            jedis = jedisPool.getResource();
        } catch (Exception var1) {
            log.error("获取Redis实例异常:{}", var1);
        }

        return jedis;
    }

    @Value("${redis.expire.time:0}")
    public void setDefaultExpireTime(int defaultExpireTime) {
        DEFAULT_EXPIRE_TIME = defaultExpireTime;
    }

    @Value("${redis.addr:127.0.0.1}")
    public void setADDR(String ADDR) {
        this.ADDR = ADDR;
    }

    @Value("${redis.port}")
    public void setPORT(int PORT) {
        PORT = PORT;
    }

    @Value("${redis.auth:nullValue}")
    public void setAUTH(String AUTH) {
        if ("nullValue".equals(AUTH)) {
            AUTH = null;
        } else {
            AUTH = AUTH;
        }

    }

    @Value("${redis.pool.max_active:300}")
    public void setMaxActive(int maxActive) {
        MAX_ACTIVE = maxActive;
    }

    @Value("${redis.pool.max_idle:200}")
    public void setMaxIdle(int maxIdle) {
        MAX_IDLE = maxIdle;
    }

    @Value("${redis.pool.max_wait:10000}")
    public void setMaxWait(int maxWait) {
        MAX_WAIT = maxWait;
    }

    @Value("${redis.pool.timeout:10000}")
    public void setTIMEOUT(int TIMEOUT) {
        TIMEOUT = TIMEOUT;
    }

    public static class RedisTransaction {
        private Transaction transaction;
        private Jedis tranJedis;

        private RedisTransaction(Jedis jedis) {
            this.tranJedis = jedis;
        }

        public String watch(String prefix, String cacheType, String key) {
            return this.watch(RedisCache.generateFullKey(prefix, cacheType, key));
        }

        private String watch(String key) {
            String result = null;

            try {
                if (null != this.tranJedis) {
                    result = this.tranJedis.watch(new String[]{key});
                }
            } catch (Exception var4) {
                RedisCache.log.error("redis监控key存在异常", var4);
            }

            return result;
        }

        public Response<String> set(String prefix, String cacheType, String key, String value) {
            return this.set(RedisCache.generateFullKey(prefix, cacheType, key), value, RedisCache.DEFAULT_EXPIRE_TIME);
        }

        public Response<String> set(String prefix, String cacheType, String key, String value, int expireSeconds) {
            return this.set(RedisCache.generateFullKey(prefix, cacheType, key), value, expireSeconds);
        }

        private Response<String> set(String key, String value, int expireSeconds) {
            Response result = null;

            try {
                result = this.transaction.set(key, value);
                if (expireSeconds > 0) {
                    this.transaction.expire(key, expireSeconds);
                }
            } catch (Exception var6) {
                RedisCache.log.error("redis事务中设置单个值失败", var6);
            }

            return result;
        }

        public Response<Long> incr(String prefix, String cacheType, String key) {
            return this.incr(RedisCache.generateFullKey(prefix, cacheType, key));
        }

        private Response<Long> incr(String key) {
            Response result = null;

            try {
                result = this.transaction.incr(key);
            } catch (Exception var4) {
                RedisCache.log.error("redis事务中执行incr存在异常", var4);
            }

            return result;
        }

        public void multi() {
            try {
                this.transaction = this.tranJedis.multi();
            } catch (Exception var2) {
                RedisCache.log.error("redis开启事务失败", var2);
            }

        }

        public List<Object> exec() {
            List result = null;

            try {
                result = this.transaction.exec();
            } catch (Exception var3) {
                RedisCache.log.error("redis事务执行失败", var3);
            }

            return result;
        }
    }
}
