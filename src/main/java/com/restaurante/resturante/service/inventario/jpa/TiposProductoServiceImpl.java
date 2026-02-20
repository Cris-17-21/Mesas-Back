package com.restaurante.resturante.service.inventario.jpa;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.restaurante.resturante.domain.inventario.TiposProducto;
import com.restaurante.resturante.repository.inventario.TiposProductoRepository;
import com.restaurante.resturante.service.inventario.ITiposProductoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TiposProductoServiceImpl implements ITiposProductoService {

    private final TiposProductoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TiposProducto> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public TiposProducto findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public TiposProducto save(TiposProducto tipo) {
        return repository.save(tipo);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiposProducto> findByCategoryId(Integer idCategoria) {
        return repository.findByCategoria_IdCategoria(idCategoria);
    }
}
