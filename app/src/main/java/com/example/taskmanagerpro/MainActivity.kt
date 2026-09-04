package com.example.taskmanagerpro

import android.os.Bundle
import android.util.Log
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

val listaDeTarefas = mutableStateListOf(
    Tarefa(1, "Estudar Genéricos e out/in", "Praticar Type Safety e Containers Genéricos.", Prioridade.ALTA),
    Tarefa(2, "Criar Anotações Personalizadas", "Entender @Target e @Retention.", Prioridade.MEDIA),
    Tarefa(3, "Substituir Any por <T>", "Evitar casts em tempo de execução.", Prioridade.BAIXA)
)

fun RegistrarAuditoria(funcaoReferencia: Function<*>){
    val anotacao = funcaoReferencia::class.annotations.filterIsInstance<Auditavel>().firstOrNull()

    anotacao?.let{
        Log.i("AUDITORIA_APP", "Ação auditada execultada: ${it.acao}")
    }
}

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

// --- TELA 1: LISTA DE TAREFA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaLista(onTarefaSelecionada: (Int) -> Unit) {
    val filtroApenasAlta = remember { mutableStateOf(false) }
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
            Text("Adicionar Nova Tarefa", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = novoTitulo,
                onValueChange = { novoTitulo = it },
                label = { Text("Título da tarefa") },
                modifier = Modifier.fillMaxWidth()
            )

            mensagemErroFormulario?.let { erro ->
                Text(text = erro, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    try {
                        adicionarNovaTarefa(novoTitulo)
                        novoTitulo = ""
                        mensagemErroFormulario = null
                    } catch (e: IllegalArgumentException) {
                        mensagemErroFormulario = e.message ?: "Erro desconhecido ao criar tarefa."
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Adicionar")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))


            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = filtroApenasAlta.value,
                    onCheckedChange = { filtroApenasAlta.value = it }
                )
                Text("Exibir apenas prioridade ALTA")
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(tarefasExibidas) { tarefa ->
                    ItemTarefa(tarefa = tarefa, onClick = { onTarefaSelecionada(tarefa.id) })
                }
            }
        }
    }
}

@Auditavel(acao = "Cadastro_De_Tarefa")
fun adicionarNovaTarefa(titulo: String){
    val nova = Tarefa(
        id = listaDeTarefas.size + 1,
        titulo = titulo,
        descricao = "Criada manualmente no app",
        prioridade = Prioridade.BAIXA
    )
    listaDeTarefas.add(nova)
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


            val descricaoTexto = tarefa.descricao ?: "Sem descrição cadastrada."
            Text(text = descricaoTexto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// --- TELA 2: DETALHES DA TAREFA ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhes(tarefaId: Int?, onVoltar: () -> Unit) {

    val tarefaEncontrada = tarefaId?.let { id -> listaDeTarefas.find { it.id == id } }

    val estadoTela: Resultado<Tarefa> = if (tarefaEncontrada != null){
        Resultado.Sucesso(dados = tarefaEncontrada)
    }else{
        Resultado.Erro("A tarefa solicitada não foi encontrada no repositorio")
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
                is Resultado.Carregado -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resultado.Sucesso -> {
                    val tarefa = estadoTela.dados

                    Column {
                        Text(text = tarefa.titulo, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Prioridade: ${tarefa.prioridade.rotulo}", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(16.dp))


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
                is Resultado.Erro -> {
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