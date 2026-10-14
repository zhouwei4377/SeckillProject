-- KEYS[1] = stockKey
-- ARGV[1] = quantity
local stock = tonumber(redis.call('get', KEYS[1]) or '-1')
local qty = tonumber(ARGV[1])
if stock == -1 then
  return -2 -- stock key missing
end
if stock < qty then
  return -1 -- insufficient
end
local res = redis.call('decrby', KEYS[1], qty)
return res
