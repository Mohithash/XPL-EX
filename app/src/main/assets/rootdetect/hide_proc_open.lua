function before(hook, param)
    local path = param:getArgument(0)
    if path == nil then
        return false
    end

    local pathStr = tostring(path)
    if param:isProcSelfPath(pathStr) then
        log("Proc file access detected: " .. pathStr)
    end

    return false
end
