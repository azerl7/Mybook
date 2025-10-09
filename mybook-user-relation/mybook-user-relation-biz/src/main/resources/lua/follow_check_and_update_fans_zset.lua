local key = KEYS[1] -- 操作的redis key
local fansUserId = ARGV[1] -- 粉丝id
local timestamp = ARGV[2]  -- 时间戳

local exists = redis.call('EXISTS', key); -- 判断是否存在粉丝 zset
if exists == 0 then
    return -1
end

-- 获取粉丝列表大小
local size = redis.call("ZCARD", key)
if size >= 5000 then -- 若超过5000个粉丝，则移除最早的那一批
    redis.call("ZPOPMIN", key)
end

-- 添加新的粉丝关系
redis.call("ZADD", key, timestamp, fansUserId) -- 为什么不是先添加了之后再pop
return 0;