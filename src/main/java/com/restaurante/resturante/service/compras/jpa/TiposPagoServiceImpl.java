package com.restaurante.resturante.service.compras.jpa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurante.resturante.dto.compras.TiposPagoDto;
import com.restaurante.resturante.repository.compras.TiposPagoRepository;
import com.restaurante.resturante.service.compras.ITiposPagoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TiposPagoServiceImpl implements ITiposPagoService {

    private final TiposPagoRepository tiposPagoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TiposPagoDto> findAll() {
        return tiposPagoRepository.findAll().stream()
                .map(entity -> new TiposPagoDto(entity.getIdTipoPago(), entity.getTipoPago()))
                .collect(Collectors.toList());
    }
}
