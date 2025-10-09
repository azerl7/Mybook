local key = KEYS[1] --操作的 redis key
local followUserId = ARGV[1] -- 关注的用户id
local timestamp = ARGV[2]    -- 时间戳
local expireSeconds = ARGV[3] --过期时间（秒）

-- ZADD
redis.call("ZADD", key, timestamp, followUserId)

redis.call("EXPIRE", key, expireSeconds)

return 0