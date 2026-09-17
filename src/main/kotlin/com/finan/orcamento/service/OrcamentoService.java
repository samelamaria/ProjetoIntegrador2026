package com.finan.orcamento.service;

import com.finan.orcamento.model.OrcamentoModel;
import com.finan.orcamento.repositories.OrcamentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrcamentoService {
    private final OrcamentoRepository orcamentoRepository;
    private final UsuarioService usuarioService;

    public OrcamentoService(OrcamentoRepository orcamentoRepository, UsuarioService usuarioService) {
        this.orcamentoRepository = orcamentoRepository;
        this.usuarioService = usuarioService;
    }

    public List<OrcamentoModel> buscarCadastro() {
        return orcamentoRepository.findAll();
    }

    public OrcamentoModel buscaId(Long id) {
        return orcamentoRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado"));
    }

    @Transactional
    public OrcamentoModel cadastrarOrcamento(OrcamentoModel orcamento) {
        orcamento.setId(null);
        prepararOrcamento(orcamento);
        return orcamentoRepository.save(orcamento);
    }

    @Transactional
    public OrcamentoModel atualizaCadastro(OrcamentoModel orcamento, Long id) {
        OrcamentoModel existente = buscaId(id);
        existente.setValorOrcamento(orcamento.getValorOrcamento());
        existente.setIcmsEstados(orcamento.getIcmsEstados());
        existente.setUsuario(orcamento.getUsuario());
        prepararOrcamento(existente);
        return orcamentoRepository.save(existente);
    }

    private void prepararOrcamento(OrcamentoModel orcamento) {
        if (orcamento.getUsuario() != null) {
            if (orcamento.getUsuario().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID do usuário");
            }
            orcamento.setUsuario(usuarioService.buscaId(orcamento.getUsuario().getId()));
        }
        orcamento.calcularIcms();
    }

    @Transactional
    public void deletaOrcamento(Long id) {
        orcamentoRepository.delete(buscaId(id));
    }
}
