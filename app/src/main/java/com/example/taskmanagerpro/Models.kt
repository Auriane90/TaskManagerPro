package com.example.taskmanagerpro

enum class Prioridade(val rotulo: String) {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta")
}

data class Tarefa(
    val id: Int,
    val titulo: String,
    val descricao: String,
    val prioridade: Prioridade
)

sealed class EstadoTela {
    object Carregando : EstadoTela()
    data class Sucesso(val tarefas: List<Tarefa>) : EstadoTela()
    data class Erro(val mensagem: String) : EstadoTela()
}

