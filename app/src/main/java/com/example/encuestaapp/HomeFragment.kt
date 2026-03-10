package com.example.encuestaapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.encuestaapp.databinding.FragmentHomeBinding
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var database: DatabaseReference? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        try {
            database = FirebaseDatabase.getInstance().getReference("encuesta")
            observeResults()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al conectar con Firebase: ${e.message}", Toast.LENGTH_LONG).show()
        }

        verificarEstadoVotacion()

        // Configurar botones de votación
        binding.btnVotarA.setOnClickListener { registrarVoto("Candidato A") }
        binding.btnVotarB.setOnClickListener { registrarVoto("Candidato B") }
        binding.btnVotarC.setOnClickListener { registrarVoto("Candidato C") }

        binding.btnVolverInicio.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun verificarEstadoVotacion() {
        val sharedPref = activity?.getSharedPreferences("EncuestaPrefs", android.content.Context.MODE_PRIVATE) ?: return
        if (sharedPref.getBoolean("has_voted", false)) {
            deshabilitarBotones()
            Toast.makeText(requireContext(), "Ya has registrado tu voto.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deshabilitarBotones() {
        if (_binding == null) return
        binding.btnVotarA.isEnabled = false
        binding.btnVotarB.isEnabled = false
        binding.btnVotarC.isEnabled = false
    }

    private fun registrarVoto(candidato: String) {
        val db = database
        if (db == null) {
            Toast.makeText(requireContext(), "Error: Base de datos no inicializada", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            db.child("votos").child(candidato).runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val count = mutableData.getValue(Int::class.java) ?: 0
                    mutableData.value = count + 1
                    return Transaction.success(mutableData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    activity?.runOnUiThread {
                        if (committed) {
                            guardarEstadoVotacionLocal()
                            navegarADetalle(candidato)
                        } else if (error != null) {
                            Toast.makeText(requireContext(), "Error al votar: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarEstadoVotacionLocal() {
        val sharedPref = activity?.getSharedPreferences("EncuestaPrefs", android.content.Context.MODE_PRIVATE) ?: return
        sharedPref.edit().putBoolean("has_voted", true).apply()
    }

    private fun navegarADetalle(candidato: String) {
        if (_binding == null) return
        val fragment = CuestionarioVotoFragment.newInstance(candidato)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun observeResults() {
        database?.child("votos")?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                
                var maxVotos = -1
                var totalVotos = 0
                var lider = ""
                var empate = false
                
                val vA = snapshot.child("Candidato A").getValue(Int::class.java) ?: 0
                val vB = snapshot.child("Candidato B").getValue(Int::class.java) ?: 0
                val vC = snapshot.child("Candidato C").getValue(Int::class.java) ?: 0

                totalVotos = vA + vB + vC
                binding.tvTotalVotos.text = "Total de participación: $totalVotos votos"

                actualizarProgreso(binding.pbA, binding.tvPorcentajeA, vA)
                actualizarProgreso(binding.pbB, binding.tvPorcentajeB, vB)
                actualizarProgreso(binding.pbC, binding.tvPorcentajeC, vC)

                for (child in snapshot.children) {
                    val votos = child.getValue(Int::class.java) ?: 0
                    if (votos > maxVotos) {
                        maxVotos = votos
                        lider = child.key ?: ""
                        empate = false
                    } else if (votos == maxVotos && maxVotos > 0) {
                        empate = true
                    }
                }

                if (maxVotos > 0) {
                    binding.tvLiderActual.text = if (empate) 
                        "¡Empate técnico entre los punteros!" 
                    else 
                        "¡$lider lidera con $maxVotos votos!"
                } else {
                    binding.tvLiderActual.text = "Aún no hay votos registrados."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                activity?.runOnUiThread {
                    if (_binding != null) {
                        binding.tvLiderActual.text = "Error al cargar resultados"
                    }
                }
            }
        })
    }

    private fun actualizarProgreso(pb: android.widget.ProgressBar, tv: android.widget.TextView, votos: Int) {
        // Cada voto vale 2% (Meta de 50 votos para el 100%)
        val porcentaje = (votos * 2).coerceAtMost(100)
        
        // Animación suave del progreso
        android.animation.ObjectAnimator.ofInt(pb, "progress", porcentaje)
            .setDuration(800)
            .start()
            
        tv.text = "$porcentaje%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}