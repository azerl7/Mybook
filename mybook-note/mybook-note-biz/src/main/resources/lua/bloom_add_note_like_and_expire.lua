---
--- Created by 16017.
--- DateTime: 2025/9/15 10:59
---

-- 初始化布隆过滤器，并设置过期时间
-- 操作的 Key
local key = KEYS[1]
local noteId = ARGV[1] -- 笔记ID
local expireSeconds = ARGV[2] -- 过期时间（秒）

redis.call("BF.ADD", key, noteId)
-- 设置过期时间
redis.call("EXPIRE", key, expireSeconds)
return 0
