package com.example.taskmanagerpro

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auditavel(val acao: String)

enum class Prioridade(val rotulo: String) {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta")
}

data class Tarefa(
    val id: Int,
    val titulo: String,
    val descricao: String?,
    val prioridade: Prioridade
) {
    init {
        require(titulo.isNotBlank()) { "O título da tarefa não pode estar em branco!" }
    }
}

/*sealed class EstadoTela {
    object Carregando : EstadoTela()
    data class Sucesso(val tarefas: List<Tarefa>) : EstadoTela()
    data class Erro(val mensagem: String) : EstadoTela()
}*/

sealed class Resultado<out T>{
    object Carregado : Resultado<Nothing>()
    data class Sucesso<out T>(val dados: T) : Resultado<T>()
    data class Erro(val mensagem: String): Resultado<Nothing>()
}

