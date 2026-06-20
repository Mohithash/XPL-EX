function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    local ths = param:getThis()
    if ths == nil then
        return false
    end

    if param:isProcMapsLine(result) then
        local nextLine = ths:readLine()
        while nextLine ~= nil do
            if not param:isProcMapsLine(nextLine) then
                param:setResult(nextLine)
                return true, result, nextLine
            end
            nextLine = ths:readLine()
        end
        param:setResult(nil)
        return true, result, "null"
    end

    if param:isProcMountLine(result) then
        local nextLine = ths:readLine()
        while nextLine ~= nil do
            if not param:isProcMountLine(nextLine) then
                param:setResult(nextLine)
                return true, result, nextLine
            end
            nextLine = ths:readLine()
        end
        param:setResult(nil)
        return true, result, "null"
    end

    return false
end
