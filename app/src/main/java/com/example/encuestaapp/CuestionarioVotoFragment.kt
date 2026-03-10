package com.example.encuestaapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.encuestaapp.databinding.FragmentCuestionarioVotoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import kotlinx.coroutines.*

class CuestionarioVotoFragment : Fragment() {
    private var _binding: FragmentCuestionarioVotoBinding? = null
    private val binding get() = _binding!!

    private var database: DatabaseReference? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var candidatoSeleccionado: String? = null

    companion object {
        private const val ARG_CANDIDATO = "arg_candidato"
        fun newInstance(candidato: String): CuestionarioVotoFragment {
            val fragment = CuestionarioVotoFragment()
            val args = Bundle()
            args.putString(ARG_CANDIDATO, candidato)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { candidatoSeleccionado = it.getString(ARG_CANDIDATO) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCuestionarioVotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            database = FirebaseDatabase.getInstance().getReference("encuesta")
            setupUI()
            setupSpinners()
            setupListeners()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al inicializar cuestionario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.tvGraciasVoto.text = "Has votado por: $candidatoSeleccionado\nAyúdanos con estas preguntas:"
    }

    private fun setupSpinners() {
        val configSpinner = { spinnerId: android.widget.Spinner, arrayId: Int ->
            try {
                ArrayAdapter.createFromResource(requireContext(), arrayId, android.R.layout.simple_spinner_item).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerId.adapter = adapter
                }
            } catch (e: Exception) { }
        }

        configSpinner(binding.spinnerEdad, R.array.edades_array)
        configSpinner(binding.spinnerZona, R.array.zonas_array)
        configSpinner(binding.spinnerPropuesta, R.array.propuestas_array)
        configSpinner(binding.spinnerFuente, R.array.fuentes_array)
    }

    private fun setupListeners() {
        binding.btnEnviarCuestionario.setOnClickListener { enviarDatos() }
        binding.btnVolverInicio.setOnClickListener { 
            parentFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    private fun enviarDatos() {
        val db = database ?: return
        
        // Obtener valores
        val edad = binding.spinnerEdad.selectedItem?.toString() ?: ""
        val zona = binding.spinnerZona.selectedItem?.toString() ?: ""
        val genero = binding.etGenero.text.toString()
        val propuesta = binding.spinnerPropuesta.selectedItem?.toString() ?: ""
        val motivo = binding.etMotivoEleccion.text.toString()
        val confianza = binding.rbConfianza.rating
        val conoceTrayectoria = binding.swTrayectoria.isChecked
        val fuente = binding.spinnerFuente.selectedItem?.toString() ?: ""

        // Validaciones
        if (edad.contains("Selecciona") || zona.contains("Selecciona") || 
            propuesta.contains("Selecciona") || motivo.isEmpty() || confianza == 0f) {
            Toast.makeText(requireContext(), "Por favor, completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnEnviarCuestionario.isEnabled = false

        val respuestaMap = mapOf(
            "candidato" to candidatoSeleccionado,
            "perfil" to mapOf("edad" to edad, "zona" to zona, "genero" to genero),
            "motivacion" to mapOf("propuestaFavorita" to propuesta, "motivoTexto" to motivo, "nivelConfianza" to confianza),
            "conocimiento" to mapOf("conoceTrayectoria" to conoceTrayectoria, "fuenteInformacion" to fuente),
            "timestamp" to ServerValue.TIMESTAMP
        )

        db.child("respuestas_detalladas").push().setValue(respuestaMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mostrarExito()
            } else {
                binding.btnEnviarCuestionario.isEnabled = true
                Toast.makeText(requireContext(), "Error al enviar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarExito() {
        // 1. Detener cualquier interacción para evitar clics dobles
        binding.btnEnviarCuestionario.isEnabled = false
        binding.btnVolverInicio.visibility = View.GONE

        // 2. Intento ultra-seguro de Lottie
        try {
            // Solo intentamos cargar si el ID existe (esto evita el 90% de los crashes por recursos)
            val resId = resources.getIdentifier("lottie_success", "raw", requireContext().packageName)
            if (resId != 0) {
                binding.lottieCheck.visibility = View.VISIBLE
                binding.lottieCheck.setAnimation(resId)
                binding.lottieCheck.playAnimation()
            }
        } catch (e: Exception) {
            binding.lottieCheck.visibility = View.GONE
        }

        // 3. Usar el Lifecycle de la vista para que si la app se cierra, la corrutina muera y no cause crash
        scope.launch {
            try {
                delay(1000)
                // Verificamos que el fragmento siga vivo y activo antes de mostrar el diálogo
                if (_binding != null && isAdded && context != null) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("¡Gracias por tu participación!")
                        .setMessage("Tus respuestas han sido enviadas. Ahora verás los resultados actualizados.")
                        .setCancelable(false)
                        .setPositiveButton("VER RESULTADOS") { _, _ ->
                            // Regreso seguro: Intentamos volver atrás, si falla, cerramos el fragmento manualmente
                            if (isAdded) {
                                parentFragmentManager.popBackStack()
                            }
                        }
                        .show()
                }
            } catch (e: Exception) {
                // Si algo falla en el proceso del diálogo, intentamos al menos volver al inicio
                if (isAdded) parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }
}