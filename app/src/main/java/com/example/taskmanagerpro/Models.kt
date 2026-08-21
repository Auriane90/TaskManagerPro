package com.example.taskmanagerpro

enum class Prioridade(val rotulo: String) {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta")
}

// Data Class utilizando Null Safety
data class Tarefa(
    val id: Int,
    val titulo: String,
    val descricao: String?,
    val prioridade: Prioridade
) {
    init {
        // Validação usando a função utilitária require() do Kotlin
        // Lança IllegalArgumentException se o título for vazio
        require(titulo.isNotBlank()) { "O título da tarefa não pode estar em branco!" }
    }
}

sealed class EstadoTela {
    object Carregando : EstadoTela()
    data class Sucesso(val tarefas: List<Tarefa>) : EstadoTela()
    data class Erro(val mensagem: String) : EstadoTela()
}

