package com.example.taskmanagerpro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import androidx.fragment.app.Fragment

// Repositório em memória utilizando os seus modelos (Tarefa e Prioridade)
object RepositorioTarefas {
    private var contadorId = 1
    val lista = mutableListOf<Tarefa>()

    fun gerarId(): Int = contadorId++
}

// FRAGMENT DE FORMULÁRIO

class FormularioTarefaFragment : Fragment() {

    interface OnTarefaAdicionadaListener {
        fun onTarefaAdicionada()
    }

    private var listener: OnTarefaAdicionadaListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnTarefaAdicionadaListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_formulario_tarefa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAbrirFormulario = view.findViewById<Button>(R.id.btnAbrirFormulario)
        val layoutCampos = view.findViewById<LinearLayout>(R.id.layoutCamposFormulario)
        val editTitulo = view.findViewById<EditText>(R.id.editTitulo)
        val editDescricao = view.findViewById<EditText>(R.id.editDescricao)
        val spinnerPrioridade = view.findViewById<Spinner>(R.id.spinnerPrioridadeForm)
        val btnSalvar = view.findViewById<Button>(R.id.btnSalvar)
        val btnCancelar = view.findViewById<Button>(R.id.btnCancelar)

        // Opções mapeadas usando o Enum Prioridade
        val opcoesForm = listOf("Selecione a prioridade...", Prioridade.ALTA.rotulo, Prioridade.MEDIA.rotulo, Prioridade.BAIXA.rotulo)
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            opcoesForm
        )
        spinnerPrioridade.adapter = spinnerAdapter

        btnAbrirFormulario.setOnClickListener {
            layoutCampos.visibility = View.VISIBLE
            btnAbrirFormulario.visibility = View.GONE
        }

        btnCancelar.setOnClickListener {
            esconderFormulario(layoutCampos, btnAbrirFormulario, editTitulo, editDescricao, spinnerPrioridade)
        }

        btnSalvar.setOnClickListener {
            val titulo = editTitulo.text.toString().trim()
            val descricao = editDescricao.text.toString().trim()
            val prioridadeTexto = spinnerPrioridade.selectedItem.toString()

            // Regra: Se não selecionar nada, vira Prioridade.BAIXA por padrão
            val prioridadeEnum = when (prioridadeTexto) {
                Prioridade.ALTA.rotulo -> Prioridade.ALTA
                Prioridade.MEDIA.rotulo -> Prioridade.MEDIA
                else -> Prioridade.BAIXA
            }

            if (titulo.isNotBlank()) {
                val novaTarefa = Tarefa(
                    id = RepositorioTarefas.gerarId(),
                    titulo = titulo,
                    descricao = if (descricao.isBlank()) "Sem detalhes" else descricao,
                    prioridade = prioridadeEnum
                )

                RepositorioTarefas.lista.add(novaTarefa)

                esconderFormulario(layoutCampos, btnAbrirFormulario, editTitulo, editDescricao, spinnerPrioridade)
                listener?.onTarefaAdicionada()
            } else {
                editTitulo.error = "Digite o título da tarefa"
            }
        }
    }

    private fun esconderFormulario(
        layoutCampos: LinearLayout,
        btnAbrir: Button,
        editTitulo: EditText,
        editDescricao: EditText,
        spinnerPrioridade: Spinner
    ) {
        editTitulo.text.clear()
        editDescricao.text.clear()
        spinnerPrioridade.setSelection(0)
        layoutCampos.visibility = View.GONE
        btnAbrir.visibility = View.VISIBLE
    }
}

//  FRAGMENT DE LISTA
class ListaTarefasFragment : Fragment() {

    interface OnTarefaSelecionadaListener {
        fun onTarefaSelecionada(tarefa: Tarefa)
    }

    private var listener: OnTarefaSelecionadaListener? = null
    private var tarefasExibidas: List<Tarefa> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnTarefaSelecionadaListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lista_tarefas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<ListView>(R.id.listViewTarefas)
        val spinnerFiltro = view.findViewById<Spinner>(R.id.spinnerFiltroPrioridade)

        val opcoesFiltro = listOf("Todas", Prioridade.ALTA.rotulo, Prioridade.MEDIA.rotulo, Prioridade.BAIXA.rotulo)
        val filtroAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            opcoesFiltro
        )
        spinnerFiltro.adapter = filtroAdapter

        fun atualizarLista(filtro: String) {
            tarefasExibidas = if (filtro == "Todas") {
                RepositorioTarefas.lista
            } else {
                RepositorioTarefas.lista.filter { it.prioridade.rotulo.equals(filtro, ignoreCase = true) }
            }

            val listAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                tarefasExibidas.map { "[${it.prioridade.rotulo}] ${it.titulo}" }
            )
            listView.adapter = listAdapter
        }

        spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                atualizarLista(opcoesFiltro[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val tarefa = tarefasExibidas[position]
            listener?.onTarefaSelecionada(tarefa)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}