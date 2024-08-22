package com.nexusdev.beautywaveadmin.view.products

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nexusdev.beautywaveadmin.R
import com.nexusdev.beautywaveadmin.databinding.ActivityAddProductsBinding
import com.nexusdev.beautywaveadmin.entities.Constants
import com.nexusdev.beautywaveadmin.model.EventPost
import com.nexusdev.beautywaveadmin.model.ProductsModel

class AddProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductsBinding

    private var producto: ProductsModel? = null
    private var estado: String? = null
    private var categoria: String? = null

    private var photoSelectedUri: Uri? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                photoSelectedUri = it.data?.data

                binding.let {
                    Glide.with(this)
                        .load(photoSelectedUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(it.imgProductPrevie)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        click()
        setSpinnerData()
    }

    private fun click() {
        binding.let {
            it.btnSave.setOnClickListener {
                uploadImage(producto?.id) { eventPost ->
                    disableUI()
                    if (producto == null) {
                        val pd = ProductsModel(
                            name = binding.name.text.toString().trim(),
                            description = binding.desctipcion.text.toString().trim(),
                            price = binding.precio.text.toString().toDouble(),
                            imgUrl = eventPost.photoUrl,
                            status = estado.toString(),
                            backOn = binding.backOn.toString(),
                            category = categoria.toString(),
                        )
                        save(pd)
                        enableUI()
                    } else {
                        producto?.apply {
                            name = binding.name.text.toString().trim()
                            description = binding.desctipcion.text.toString().trim()
                            price = binding.precio.text.toString().toDouble()
                            imgUrl = eventPost.photoUrl
                            status = estado.toString()
                            backOn = binding.backOn.toString()
                            category = binding.categoria.toString()
                        }
                        enableUI()
                    }
                }
            }
            it.ibProduct.setOnClickListener {
                openGallery()
            }
            it.btnCancel.setOnClickListener {
                onBackPressed()
                this@AddProductsActivity.finish()
            }
            it.btnUpdate.setOnClickListener {
                producto?.let {
                    it.price = binding.precio.text.toString().toDouble()
                    it.status = estado
                    update(it)
                }
            }
        }
    }

    private fun getData() {
        this.producto = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("producto")
        } else {
            intent.getParcelableExtra("producto")
        }

        if (producto != null) {
            binding.name.setText(producto!!.name)
            binding.desctipcion.setText(producto!!.description)
            binding.precio.setText(producto!!.price.toString())
            estado = (producto!!.status)
            categoria = (producto!!.category)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun uploadImage(productId: String?, callback: (EventPost) -> Unit) {
        val eventPost = EventPost()
        eventPost.documentId = productId ?: FirebaseFirestore.getInstance()
            .collection(Constants.PATH_PRODUCTS)
            .document().id
        val storageRef =
            FirebaseStorage.getInstance().reference.child(Constants.PATH_IMG)

        photoSelectedUri?.let { uri ->
            binding.let { binding ->

                binding.progressBar.visibility = View.VISIBLE

                val photoRef = storageRef.child(eventPost.documentId!!)

                photoRef.putFile(uri)
                    .addOnProgressListener {
                        val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                        it.run {
                            binding.progressBar.progress = progress
                            binding.tvProgress.text = String.format("%s%%", progress)
                        }
                    }
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                            Log.i("url", downloadUrl.toString())
                            eventPost.isSuccess = true
                            eventPost.photoUrl =
                                downloadUrl.toString() // Guardar el enlace de descarga en el objeto eventPost
                            callback(eventPost)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                        enableUI()
                        eventPost.isSuccess = false
                        callback(eventPost)
                    }
            }
        }
    }

    private fun save(dataP: ProductsModel) {
        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.PATH_PRODUCTS)
            .document()
            .set(dataP)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Agregando... Ok!", Toast.LENGTH_SHORT).show()
                    clearData()
                    enableUI()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Ah ocurrido un error ${task.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                    enableUI()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ah ocurrido un error $it", Toast.LENGTH_SHORT).show()
                enableUI()
            }
    }

    private fun update(pedido: ProductsModel) {
        val db = FirebaseFirestore.getInstance()
        pedido.id?.let { id ->
            db.collection(Constants.PATH_PRODUCTS)
                .document(id)
                .set(pedido).addOnSuccessListener {
                    Toast.makeText(this, "Producto Actualizado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al modificar", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                }
        }
    }

    private fun setSpinnerData() {
        val estadosList = resources.getStringArray(R.array.estado)

        //spinner for estado
        val snippetsEstado = binding.spinnerEstado
        val adapterV = ArrayAdapter(this, android.R.layout.simple_spinner_item, estadosList)
        snippetsEstado.adapter = adapterV

        snippetsEstado.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                estado = snippetsEstado.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun enableUI() {
        with(binding) {
            name.isEnabled = true
            desctipcion.isEnabled = true
            precio.isEnabled = true
            btnSave.isEnabled = true
            btnUpdate.isEnabled = true
            btnCancel.isEnabled = true
        }
    }

    private fun disableUI() {
        with(binding) {
            name.isEnabled = false
            desctipcion.isEnabled = false
            precio.isEnabled = false
            btnSave.isEnabled = false
            btnUpdate.isEnabled = false
            btnCancel.isEnabled = false
        }
    }

    private fun clearData() {
        with(binding) {
            name.setText("")
            desctipcion.setText("")
            precio.setText("")
        }
    }

}