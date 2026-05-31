package com.neilan.control.repository;

import com.neilan.control.model.TipoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoServicoRepository extends JpaRepository<TipoServico, Long> {

    List<TipoServico> findByAtivoTrueOrderByCategoriaAscNomeAsc();

    List<TipoServico> findAllByOrderByCategoriaAscNomeAsc();
}
