function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    local pkg = param:getArgument(0)
    if pkg == nil then
        return false
    end

    local pkgStr = tostring(pkg)
    if pkgStr == "com.google.android.gms" or pkgStr == "com.android.vending" then
        return false
    end

    return false
end
