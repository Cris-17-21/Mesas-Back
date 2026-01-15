package com.restaurante.resturante.config.jackson;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.restaurante.resturante.service.security.IdEncryptionService;

@Component
public class ObfuscatedIdSerializer extends JsonSerializer<Long>{

    private static IdEncryptionService idEncryptionService;

    @Autowired
    public void setIdEncryptionService(IdEncryptionService service) {
        ObfuscatedIdSerializer.idEncryptionService = service;
    }

    @Override
    public void serialize(Long id, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (id != null) {
            gen.writeString(idEncryptionService.encrypt(id));
        } else {
            gen.writeNull();
        }
    }
}
