package com.restaurante.resturante.dto.venta.apisperu;

import lombok.Data;

@Data
public class ApisPeruDocumentResponse {
    private boolean success;
    private String message;
    private String xml;
    private String pdf;
    private String cdr;
    private String hash;
    private SunatResponse sunatResponse;

    @Data
    public static class SunatResponse {
        private boolean success;
        private String code;
        private String description;
        private String notes;
    }
}
