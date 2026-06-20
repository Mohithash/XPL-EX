function after(hook, param)
    local result = param:getResult()
    if result == nil or result == false then
        return false
    end

    param:setResult(false)
    return true, "true", "false"
end
