-- KEYS[1] = stockKey
-- ARGV[1] = quantity
local qty = tonumber(ARGV[1])
local res = redis.call('incrby', KEYS[1], qty)
return res
