package com.example.taskmanagerpro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalheActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe)

        // Suporte ao botão "Voltar" nativo da ActionBar (caso o tema exiba a barra)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val titulo = intent.getStringExtra("EXTRA_TITULO") ?: "Sem título"
        val descricao = intent.getStringExtra("EXTRA_DESCRICAO") ?: "Sem descrição"
        val prioridade = intent.getStringExtra("EXTRA_PRIORIDADE") ?: "Baixa"
        val dataLimite = intent.getStringExtra("EXTRA_DATA") ?: "Sem data"

        val txtTitulo = findViewById<TextView>(R.id.txtTituloDetalhe)
        val txtPrioridade = findViewById<TextView>(R.id.txtPrioridadeDetalhe)
        val txtData = findViewById<TextView>(R.id.txtDataDetalhe)
        val txtDescricao = findViewById<TextView>(R.id.txtDescricaoDetalhe)
        val btnCompartilhar = findViewById<Button>(R.id.btnCompartilhar)
        val btnVoltar = findViewById<Button>(R.id.btnVoltar)

        txtTitulo.text = titulo
        txtPrioridade.text = "Prioridade: $prioridade"
        txtData.text = "Data limite: $dataLimite"
        txtDescricao.text = descricao

        // Ação do botão Voltar personalizado no layout
        btnVoltar.setOnClickListener {
            finish() // Encerra a Activity atual e retorna para a MainActivity
        }

        btnCompartilhar.setOnClickListener {
            val textoCompartilhado = """
                📋 Tarefa: $titulo
                ⭐ Prioridade: $prioridade
                📅 Data Limite: $dataLimite
                📝 Descrição: $descricao
            """.trimIndent()

            val intentImplicit = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textoCompartilhado)
            }
            startActivity(Intent.createChooser(intentImplicit, "Compartilhar tarefa via:"))
        }
    }

    // Trata o clique no botão de voltar da ActionBar superior (caso habilitada)
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}