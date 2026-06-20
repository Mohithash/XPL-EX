function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    local h = hook:getName()
    local key = 'fingerprint.' .. string.lower(h)

    local fake = param:getSetting(key)
    if fake == nil then
        return false
    end

    param:setResult(fake)
    return true, result, fake
end
