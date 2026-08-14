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
import androidx.navigation.NavController
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
                    // Inicializa o sistema de navegação do App
                    NavegacaoApp()
                }
            }
        }
    }
}

// --- BANCO DE DADOS EM MEMÓRIA (List/Coleção) ---
val listaDeTarefas = listOf(
    Tarefa(1, "Estudar Lambdas e Altas Ordem", "Praticar a passagem de funções como parâmetros.", Prioridade.ALTA),
    Tarefa(2, "Criar Coleções em Kotlin", "Entender a diferença entre List, Set e Map.", Prioridade.MEDIA),
    Tarefa(3, "Revisar Data Classes", "Aprender a usar copy(), toString() e desestruturação.", Prioridade.BAIXA)
)

// --- SISTEMA DE NAVEGAÇÃO ---
@Composable
fun NavegacaoApp() {
    val navController = rememberNavController()

    // NavHost gerencia as telas e rotas de navegação
    NavHost(navController = navController, startDestination = "tela_lista") {

        // Rota da Tela 1: Lista
        composable("tela_lista") {
            TelaLista(
                // Passando uma Lambda como Callback de clique (Função de Alta Ordem)
                onTarefaSelecionada = { idTarefa ->
                    navController.navigate("tela_detalhes/$idTarefa")
                }
            )
        }

        // Rota da Tela 2: Detalhes da Tarefa (Recebe ID por parâmetro)
        composable("tela_detalhes/{tarefaId}") { backStackEntry ->
            val idString = backStackEntry.arguments?.getString("tarefaId")
            val id = idString?.toIntOrNull() ?: 0

            TelaDetalhes(
                tarefaId = id,
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}

// TELA 1: LISTA DE TAREFAS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLista(onTarefaSelecionada: (Int) -> Unit) { // Função de Alta Ordem: recebe uma lambda (Int) -> Unit
    val (filtroApenasAlta, setFiltroApenasAlta) = remember { mutableStateOf(false) }

    // Aplicação das Funções Funcionais (filter)
    val tarefasExibidas = if (filtroApenasAlta) {
        listaDeTarefas.filter { it.prioridade == Prioridade.ALTA }
    } else {
        listaDeTarefas
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("TaskMaster - Minhas Tarefas") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Controles de Filtro
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = filtroApenasAlta,
                    onCheckedChange = { setFiltroApenasAlta(it)}
                )
                Text("Exibir apenas prioridade ALTA")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Coleção renderizada em lista rolável
            LazyColumn {
                items(tarefasExibidas) { tarefa ->
                    ItemTarefa(
                        tarefa = tarefa,
                        onClick = { onTarefaSelecionada(tarefa.id) } // Execução do Callback Lambda
                    )
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
            Text(text = "Prioridade: ${tarefa.prioridade.rotulo}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

//  TELA 2: DETALHES DA TAREFA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhes(tarefaId: Int, onVoltar: () -> Unit) {
    // Busca na coleção
    val tarefa = listaDeTarefas.find { it.id == tarefaId }

    // Uso de Sealed Class para controlar a exibição do estado
    val estadoTela: EstadoTela = if (tarefa != null) {
        EstadoTela.Sucesso(listOf(tarefa))
    } else {
        EstadoTela.Erro("Tarefa não encontrada!")
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
            // Tratamento das variantes do Sealed Class
            when (estadoTela) {
                is EstadoTela.Carregando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EstadoTela.Sucesso -> {
                    val t = estadoTela.tarefas.first()

                    // Exemplo de Desestruturação da Data Class
                    val (id, titulo, descricao, prioridade) = t

                    Column {
                        Text(text = titulo, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Prioridade: ${prioridade.rotulo}", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = descricao, style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = onVoltar) {
                            Text("Voltar para Lista")
                        }
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