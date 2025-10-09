
-- 操作的key
local key = KEYS[1]

-- 准备批量添加的参数
local zaddArgs = {}

-- 遍历 ARGV 参数，将分数（时间戳）和值按顺序插入到 zaddArgs中

for i = 1, #ARGV - 1, 2 do
    table.insert(zaddArgs, ARGV[i])  --分数（关注时间）
    table.insert(zaddArgs, ARGV[i + 1]) --值（关注用户id）
end

-- 调用ZADD批量插入数据
redis.call("ZADD", key, unpack(zaddArgs))
local expireTime = ARGV[#ARGV] --最后一个参数为过期时间
redis.call("EXPIRE", key, expireTime)

return 0