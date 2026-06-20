function before(hook, param)
    local params = param:getArgument(0)
    if params == nil then
        return false
    end

    local clsKeyGenSpec = luajava.bindClass("android.security.keystore.KeyGenParameterSpec")
    if not luajava.instanceof(params, clsKeyGenSpec) then
        return false
    end

    local isAttestation = params:getAttestationChallenge()
    if isAttestation ~= nil then
        log("Blocking hardware key attestation request")
        local clsKeyStoreException = luajava.bindClass("java.security.ProviderException")
        local fake = luajava.new(clsKeyStoreException, "Failed to generate key pair")
        param:setException(fake)
        return true, "attestation_blocked"
    end

    return false
end
