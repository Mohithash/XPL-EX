function after(hook, param)
    local envName = param:getArgument(0)
    if envName == nil then
        return false
    end

    if envName == "PATH" then
        local result = param:getResult()
        if result == nil then
            return false
        end
        local cleaned = tostring(result)
        cleaned = cleaned:gsub("/sbin:?", "")
        cleaned = cleaned:gsub("/su/bin:?", "")
        cleaned = cleaned:gsub("/data/local/xbin:?", "")
        cleaned = cleaned:gsub("/data/local/bin:?", "")
        cleaned = cleaned:gsub("::+", ":")
        cleaned = cleaned:gsub("^:", "")
        cleaned = cleaned:gsub(":$", "")
        if cleaned ~= tostring(result) then
            param:setResult(cleaned)
            return true, tostring(result), cleaned
        end
    end

    return false
end
