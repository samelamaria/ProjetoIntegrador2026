(() => {
    const search = document.getElementById("busca");
    const rows = Array.from(document.querySelectorAll("[data-user-row]"));
    if (!search || rows.length === 0) return;
    document.getElementById("busca-container").hidden = false;
    const normalize = (value) => value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLocaleLowerCase("pt-BR").trim();
    search.addEventListener("input", () => {
        const query = normalize(search.value);
        const cpfQuery = /^[\d.\-\s]+$/.test(query) ? query.replace(/\D/g, "") : "";
        let visible = 0;
        rows.forEach((row) => {
            const name = row.querySelector(".person-name").textContent;
            const id = row.querySelector(".user-id").textContent;
            const cpf = row.querySelector(".person-cpf")?.dataset.cpf || "";
            row.hidden = !(normalize(name + " " + id).includes(query)
                || (cpfQuery.length > 0 && cpf.includes(cpfQuery)));
            if (!row.hidden) visible++;
        });
        document.getElementById("sem-resultados").hidden = visible !== 0;
        document.getElementById("resultado-contagem").textContent =
            query ? visible + " de " + rows.length + " usuário(s)" : rows.length + " usuário(s) cadastrado(s)";
    });
})();
