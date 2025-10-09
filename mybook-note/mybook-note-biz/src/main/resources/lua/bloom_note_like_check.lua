---
--- Created by 16017.
--- DateTime: 2025/9/15 10:32
---

local key = KEYS[1] -- 操作的rediskey
local noteId = ARGV[1] --笔记id
local exists = redis.call("EXISTS", key);

if exists == 0 then
    return -1
end

-- 笔记是否被点赞过

local isLiked = redis.call("BF.EXISTS", key, noteId);
if isLiked==1 then
    return 1
end

-- 笔记没有点赞过就添加，布隆过滤器没有假阴性
redis.call("BF.ADD",key,noteId)
return 0
