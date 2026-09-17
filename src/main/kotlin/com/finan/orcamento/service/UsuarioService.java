package com.finan.orcamento.service;

import com.finan.orcamento.model.UsuarioModel;
import com.finan.orcamento.repositories.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioModel> buscarUsuario() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public UsuarioModel buscaId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    @Transactional
    public UsuarioModel cadastrarUsuario(UsuarioModel usuario) {
        usuario.setId(null);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioModel atualizaUsuario(UsuarioModel usuario, Long id) {
        UsuarioModel existente = buscaId(id);
        existente.setNomeUsuario(usuario.getNomeUsuario());
        existente.setCpf(usuario.getCpf());
        existente.setDataNascimento(usuario.getDataNascimento());
        return usuarioRepository.save(existente);
    }

    @Transactional
    public void deletaUsuario(Long id) {
        usuarioRepository.delete(buscaId(id));
    }
}
