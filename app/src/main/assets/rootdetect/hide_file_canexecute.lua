function before(hook, param)
    local ths = param:getThis()
    if ths == nil then
        return false
    end

    local path = ths:getAbsolutePath()
    if path == nil then
        return false
    end

    if param:fileIsEvidence(path, 3) then
        param:setResult(false)
        return true, path, "false"
    end

    return false
end
