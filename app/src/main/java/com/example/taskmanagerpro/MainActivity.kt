package com.example.taskmanagerpro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(),
    ListaTarefasFragment.OnTarefaSelecionadaListener,
    FormularioTarefaFragment.OnTarefaAdicionadaListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerFormulario, FormularioTarefaFragment())
                .replace(R.id.containerLista, ListaTarefasFragment())
                .commit()
        }
    }

    override fun onTarefaSelecionada(tarefa: Tarefa) {
        val intent = Intent(this, DetalheActivity::class.java).apply {
            putExtra("EXTRA_TITULO", tarefa.titulo)
            putExtra("EXTRA_DESCRICAO", tarefa.descricao)
            putExtra("EXTRA_PRIORIDADE", tarefa.prioridade.rotulo)
        }
        startActivity(intent)
    }

    override fun onTarefaAdicionada() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerLista, ListaTarefasFragment())
            .commit()
    }
}