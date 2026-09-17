const { test } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");

test("busca por CPF com e sem pontuacao, parcial, nome e ID", () => {
    let onInput;
    const elements = {
        busca: { value: "", addEventListener: (_, callback) => { onInput = callback; } },
        "busca-container": { hidden: true },
        "sem-resultados": { hidden: true },
        "resultado-contagem": { textContent: "" }
    };
    const row = (name, id, cpf) => ({
        hidden: false,
        querySelector: (selector) => ({
            ".person-name": { textContent: name },
            ".user-id": { textContent: id },
            ".person-cpf": { dataset: { cpf } }
        })[selector]
    });
    const rows = [row("João", "#1", "52998224725"), row("Ana", "#2", "")];
    vm.runInNewContext(fs.readFileSync("src/main/resources/static/js/usuarios.js", "utf8"), {
        document: {
            getElementById: (id) => elements[id],
            querySelectorAll: () => rows
        }
    });
    assert.equal(elements["busca-container"].hidden, false);
    for (const query of ["52998224725", "529.982.247-25", "982.247", "joao", "#1"]) {
        elements.busca.value = query;
        onInput();
        assert.deepEqual(rows.map((item) => item.hidden), [false, true], query);
        assert.equal(elements["resultado-contagem"].textContent, "1 de 2 usuário(s)");
    }
    elements.busca.value = "inexistente";
    onInput();
    assert.equal(elements["sem-resultados"].hidden, false);
    assert.ok(rows.every((item) => item.hidden));
    elements.busca.value = "";
    onInput();
    assert.ok(rows.every((item) => !item.hidden));
    assert.equal(elements["sem-resultados"].hidden, true);
});
