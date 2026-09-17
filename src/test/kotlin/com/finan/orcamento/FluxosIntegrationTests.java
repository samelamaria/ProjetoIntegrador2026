package com.finan.orcamento;

import com.finan.orcamento.model.UsuarioModel;
import com.finan.orcamento.repositories.UsuarioRepository;
import com.finan.orcamento.repositories.OrcamentoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FluxosIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired OrcamentoRepository orcamentos;
    @Autowired ObjectMapper mapper;

    @BeforeEach
    void limparBanco() {
        orcamentos.deleteAll();
        usuarios.deleteAll();
    }

    @Test
    void cpfInvalidoOuAusenteNaoSalva() throws Exception {
        for (String cpf : new String[]{"", "11111111111", "52998224724", "abc52998224725"}) {
            mvc.perform(post("/usuarios").param("nomeUsuario", "Ana")
                            .param("cpf", cpf).param("dataNascimento", "1995-04-23"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("usuarioModel", "cpf"));
        }
        assertThat(usuarios.count()).isZero();
    }

    @Test
    void nascimentoAusenteInvalidoOuFuturoNaoSalva() throws Exception {
        for (String nascimento : new String[]{"", "2020-02-30", "nao-e-data",
                java.time.LocalDate.now().plusDays(1).toString()}) {
            mvc.perform(post("/usuarios").param("nomeUsuario", "Ana")
                            .param("cpf", "52998224725").param("dataNascimento", nascimento))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("usuarioModel", "dataNascimento"));
        }
        assertThat(usuarios.count()).isZero();
    }

    @Test
    void cpfSemMascaraEDataSaoExibidosNaLista() throws Exception {
        mvc.perform(post("/usuarios").param("nomeUsuario", "Ana")
                        .param("cpf", "52998224725").param("dataNascimento", "1995-04-23"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/usuarios")).andExpect(status().isOk())
                .andExpect(content().string(containsString("529.982.247-25")))
                .andExpect(content().string(containsString("23/04/1995")))
                .andExpect(content().string(containsString("data-cpf=\"52998224725\"")));
    }

    @Test
    void paginaInicialERecursosDisponiveis() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/usuarios"));
        mvc.perform(get("/usuarios")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Adicionar usuário")));
        mvc.perform(get("/css/style.css")).andExpect(status().isOk());
        mvc.perform(get("/js/usuarios.js")).andExpect(status().isOk());
    }

    @Test
    void cadastroNormalizaNomeIgnoraIdEListaUsuario() throws Exception {
        mvc.perform(post("/usuarios").param("cpf", "529.982.247-25").param("dataNascimento", "1995-04-23").param("nomeUsuario", "  Ana Oliveira  ").param("id", "999"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("sucesso", "Usuário cadastrado com sucesso!"));
        assertThat(usuarios.findAll()).singleElement().satisfies(usuario -> {
            assertThat(usuario.getNomeUsuario()).isEqualTo("Ana Oliveira");
            assertThat(usuario.getCpf()).isEqualTo("52998224725");
            assertThat(usuario.getDataNascimento()).isEqualTo(java.time.LocalDate.of(1995, 4, 23));
            assertThat(usuario.getId()).isNotEqualTo(999L);
        });
        mvc.perform(get("/usuarios")).andExpect(content().string(containsString("Ana Oliveira")));
        mvc.perform(get("/usuarios/pesquisa")).andExpect(content().string(containsString("Ana Oliveira")));
    }

    @Test
    void nomeVazioOuLongoNaoSalva() throws Exception {
        for (String nome : new String[]{"   ", "a".repeat(101)}) {
            mvc.perform(post("/usuarios").param("cpf", "529.982.247-25").param("dataNascimento", "1995-04-23").param("nomeUsuario", nome))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeHasFieldErrors("usuarioModel", "nomeUsuario"));
        }
        assertThat(usuarios.count()).isZero();
    }

    @Test
    void nomesSaoEscapadosNoHtml() throws Exception {
        mvc.perform(post("/usuarios").param("cpf", "529.982.247-25").param("dataNascimento", "1995-04-23").param("nomeUsuario", "<script>alert(1)</script>"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/usuarios"))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    void criaAtualizaRecalculaEExcluiOrcamento() throws Exception {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setNomeUsuario("Ana");
        usuario.setCpf("52998224725");
        usuario.setDataNascimento(java.time.LocalDate.of(1995, 4, 23));
        Long usuarioId = usuarios.save(usuario).getId();
        String resposta = mvc.perform(post("/orcamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"icmsEstados\":\"ICMS_SP\",\"valorOrcamento\":100,\"valorICMS\":999,\"usuario\":{\"id\":" + usuarioId + "}}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.valorICMS").value(12.0))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(resposta).get("id").asLong();
        mvc.perform(post("/orcamentos/put/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"icmsEstados\":\"ICMS_MG\",\"valorOrcamento\":200.01,\"valorICMS\":999}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorICMS").value(36.0))
                .andExpect(jsonPath("$.icmsEstados").value("ICMS_MG"));
        mvc.perform(delete("/orcamentos/delete/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/orcamentos/pesquisaid/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void orcamentosInvalidosRetornam400() throws Exception {
        for (String payload : new String[]{"{}", "{\"icmsEstados\":\"ICMS_SP\",\"valorOrcamento\":-1}",
                "{\"icmsEstados\":\"ICMS_SP\",\"valorOrcamento\":1.999}",
                "{\"icmsEstados\":\"ICMS_SP\",\"valorOrcamento\":100,\"usuario\":{}}"}) {
            mvc.perform(post("/orcamentos").contentType(MediaType.APPLICATION_JSON).content(payload))
                    .andExpect(status().isBadRequest());
        }
        assertThat(orcamentos.count()).isZero();
    }

    @Test
    void usuarioInexistenteRetorna404() throws Exception {
        mvc.perform(post("/orcamentos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"icmsEstados\":\"ICMS_RJ\",\"valorOrcamento\":100,\"usuario\":{\"id\":999999}}"))
                .andExpect(status().isNotFound());
    }
}
