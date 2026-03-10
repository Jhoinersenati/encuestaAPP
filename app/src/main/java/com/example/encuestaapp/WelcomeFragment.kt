package com.example.encuestaapp

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.encuestaapp.databinding.FragmentWelcomeBinding
import com.google.firebase.database.FirebaseDatabase

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnEmpezar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnReset.setOnClickListener {
            mostrarDialogoSeguridad()
        }
    }

    private fun mostrarDialogoSeguridad() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Ingresa la clave de administrador"

        AlertDialog.Builder(requireContext())
            .setTitle("Acceso Restringido")
            .setMessage("Para resetear los votos de Punchana 2026, ingresa la contraseña:")
            .setView(input)
            .setPositiveButton("VERIFICAR") { _, _ ->
                val clave = input.text.toString()
                if (clave == "Punchana2026") {
                    ejecutarResetTotal()
                } else {
                    Toast.makeText(requireContext(), "Acceso Denegado: Clave Incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun ejecutarResetTotal() {
        // 1. Limpiar memoria local (Permitir volver a votar)
        val sharedPref = activity?.getSharedPreferences("EncuestaPrefs", android.content.Context.MODE_PRIVATE)
        sharedPref?.edit()?.clear()?.apply()
        
        // 2. Resetear Firebase (Poner todos los contadores en 0)
        // Usamos un mapa con 0 para que las barras de progreso se animen bajando suavemente a 0%
        val database = FirebaseDatabase.getInstance().getReference("encuesta/votos")
        val resetMap = mapOf(
            "Candidato A" to 0,
            "Candidato B" to 0,
            "Candidato C" to 0
        )
        
        database.setValue(resetMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "¡Base de datos reseteada con éxito!", Toast.LENGTH_LONG).show()
            }
        }
        
        // También limpiamos las respuestas detalladas para un reset total
        FirebaseDatabase.getInstance().getReference("encuesta/respuestas_detalladas").setValue(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}