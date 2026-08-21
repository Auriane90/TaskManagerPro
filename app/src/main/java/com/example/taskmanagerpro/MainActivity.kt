package com.example.taskmanagerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacaoApp()
                }
            }
        }
    }
}

// --- BANCO DE DADOS EM MEMÓRIA ---
val listaDeTarefas = mutableStateListOf(
    Tarefa(1, "Estudar Null Safety e Exceções", "Praticar try/catch, Elvis e Safe Calls.", Prioridade.ALTA),
    Tarefa(2, "Tratar erros na busca", null, Prioridade.MEDIA), // Exemplo de descrição null
    Tarefa(3, "Revisar Stack Unwinding", "Entender como erros sobem na pilha.", Prioridade.BAIXA)
)

// --- SISTEMA DE NAVEGAÇÃO ---
@Composable
fun NavegacaoApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "tela_lista") {
        composable("tela_lista") {
            TelaLista(
                onTarefaSelecionada = { idTarefa ->
                    navController.navigate("tela_detalhes/$idTarefa")
                }
            )
        }

        composable("tela_detalhes/{tarefaId}") { backStackEntry ->
            val idParametro = backStackEntry.arguments?.getString("tarefaId")

            // USO DO 'try / catch' COMO EXPRESSÃO + Safe Cast (as?)
            val idValido: Int? = try {
                idParametro?.toInt()
            } catch (e: NumberFormatException) {
                null
            }

            TelaDetalhes(
                tarefaId = idValido,
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}

// --- TELA 1: LISTA DE TAREFAS + CRIAÇÃO COM VALIDAÇÃO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLista(onTarefaSelecionada: (Int) -> Unit) {
    val filtroApenasAlta = remember { mutableStateOf(false) }

    // Estados para formulário de nova tarefa
    var novoTitulo by remember { mutableStateOf("") }
    var mensagemErroFormulario by remember { mutableStateOf<String?>(null) }

    val tarefasExibidas = if (filtroApenasAlta.value) {
        listaDeTarefas.filter { it.prioridade == Prioridade.ALTA }
    } else {
        listaDeTarefas
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("TaskMaster - Tarefas") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // FORMULÁRIO COM TRATAMENTO DE EXCEÇÃO (try / catch)
            Text("Adicionar Nova Tarefa", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = novoTitulo,
                onValueChange = { novoTitulo = it },
                label = { Text("Título da tarefa") },
                modifier = Modifier.fillMaxWidth()
            )

            // OPERADOR ELVIS (?:) E SAFE CALL (?.) PARA EXIBIR MENSAGEM DE ERRO
            mensagemErroFormulario?.let { erro ->
                Text(text = erro, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    // TRY / CATCH CAPTURANDO A EXCEÇÃO LANÇADA PELO REQUIRE DA DATA CLASS
                    try {
                        val nova = Tarefa(
                            id = listaDeTarefas.size + 1,
                            titulo = novoTitulo,
                            descricao = "Criada manualmente no app",
                            prioridade = Prioridade.MEDIA
                        )
                        listaDeTarefas.add(nova)
                        novoTitulo = ""
                        mensagemErroFormulario = null // Limpa o erro se deu certo
                    } catch (e: IllegalArgumentException) {
                        // Captura e armazena a mensagem da exceção
                        mensagemErroFormulario = e.message ?: "Erro desconhecido ao criar tarefa."
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Adicionar")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // FILTRO DE PRIORIDADE
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = filtroApenasAlta.value,
                    onCheckedChange = { filtroApenasAlta.value = it }
                )
                Text("Exibir apenas prioridade ALTA")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // LISTA
            LazyColumn {
                items(tarefasExibidas) { tarefa ->
                    ItemTarefa(tarefa = tarefa, onClick = { onTarefaSelecionada(tarefa.id) })
                }
            }
        }
    }
}

@Composable
fun ItemTarefa(tarefa: Tarefa, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = tarefa.titulo, style = MaterialTheme.typography.titleMedium)

            // SAFE CALL (?.) + OPERADOR ELVIS (?:)
            // Se a descrição for null, usa a String padrão "Sem descrição cadastrada"
            val descricaoTexto = tarefa.descricao ?: "Sem descrição cadastrada."
            Text(text = descricaoTexto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// --- TELA 2: DETALHES DA TAREFA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhes(tarefaId: Int?, onVoltar: () -> Unit) {

    // TRATAMENTO COM NULL SAFETY: Busca a tarefa apenas se o ID não for null
    val tarefaEncontrada = tarefaId?.let { id ->
        listaDeTarefas.find { it.id == id }
    }

    // AVALIAÇÃO DO ESTADO DA TELA (Sealed Class)
    val estadoTela: EstadoTela = if (tarefaEncontrada != null) {
        EstadoTela.Sucesso(listOf(tarefaEncontrada))
    } else {
        EstadoTela.Erro("Erro: Tarefa não encontrada ou ID inválido!")
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalhes da Tarefa") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (estadoTela) {
                is EstadoTela.Carregando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EstadoTela.Sucesso -> {
                    val tarefa = estadoTela.tarefas.first()

                    Column {
                        Text(text = tarefa.titulo, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Prioridade: ${tarefa.prioridade.rotulo}", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        // SAFE CALL + LET COM VALOR DEFAULT USANDO ELVIS
                        // Exibe a descrição com formatação extra SOMENTE se ela existir
                        tarefa.descricao?.let { desc ->
                            Text(text = "Descrição:", style = MaterialTheme.typography.titleSmall)
                            Text(text = desc, style = MaterialTheme.typography.bodyLarge)
                        } ?: Text(
                            text = "Nenhuma descrição detalhada foi fornecida para esta tarefa.",
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = onVoltar) { Text("Voltar para Lista") }
                    }
                }
                is EstadoTela.Erro -> {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        Text(text = estadoTela.mensagem, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onVoltar) { Text("Voltar") }
                    }
                }
            }
        }
    }
}