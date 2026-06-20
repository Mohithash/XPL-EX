function after(hook, param)
    local propName = param:getArgument(0)
    if propName == nil then
        return false
    end

    local spoofed = param:spoofRootProperty(propName)
    if spoofed ~= nil then
        local old = param:getResult()
        param:setResult(spoofed)
        return true, propName .. "=" .. tostring(old), propName .. "=" .. spoofed
    end

    if param:isRootProperty(propName) then
        param:setResult("")
        return true, propName, "hidden"
    end

    if param:isEmulatorProperty(propName) then
        param:setResult("")
        return true, propName, "hidden"
    end

    return false
end
