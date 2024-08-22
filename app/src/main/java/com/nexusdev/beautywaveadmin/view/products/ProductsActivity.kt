package com.nexusdev.beautywaveadmin.view.products

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nexusdev.beautywaveadmin.R
import com.nexusdev.beautywaveadmin.adapter.ProductsAdapter
import com.nexusdev.beautywaveadmin.databinding.ActivityProductsBinding
import com.nexusdev.beautywaveadmin.entities.Constants
import com.nexusdev.beautywaveadmin.model.ProductsModel

class ProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductsBinding
    private lateinit var adapter: ProductsAdapter
    private lateinit var firestoreListener: ListenerRegistration


    private val handler = Handler(Looper.getMainLooper())
    private var index = 0
    private val delay: Long = 150

    private lateinit var typewriterTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)


        typewriterTextView = binding.txtTitle
        val textToDisplay = "BeautyWave"
        typewriterEffect(textToDisplay)

        getAllActiveData()
        setupSearchView()
        clicks()
    }

    override fun onPause() {
        super.onPause()
        firestoreListener.remove()
    }

    override fun onResume() {
        super.onResume()
        getAllActiveData()
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty())
                return true
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun clicks() {
        binding.let {
            it.btnIn.setOnClickListener {
                getAllActiveData()
            }
            it.btnLowPrice.setOnClickListener {
                getAllActiveDataBy()
            }
            it.btnAll.setOnClickListener {
                getAllData()
            }
        }
    }

    //animation for text like write machine
    @SuppressLint("SetTextI18n")
    private fun typewriterEffect(text: String) {
        if (index < text.length) {
            typewriterTextView.text = typewriterTextView.text.toString() + text[index]
            index++
            handler.postDelayed({ typewriterEffect(text) }, delay)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getAllActiveData() {
        binding.txtSubMenuTitle.text = "Productos Disponibles"
        val db = FirebaseFirestore.getInstance()

        val productRef = db.collection(Constants.PATH_PRODUCTS)
        firestoreListener =
            productRef.whereEqualTo("status", "Disponible").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al consultar datos, verifica tu conexión a internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                val prodList = mutableListOf<ProductsModel>()
                for (snapshot in snapshot!!.documentChanges) {
                    val product = snapshot.document.toObject(ProductsModel::class.java)
                    product.id = snapshot.document.id

                    when (snapshot.type) {
                        DocumentChange.Type.ADDED -> prodList.add(product)
                        DocumentChange.Type.MODIFIED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList[index] = product
                                adapter.notifyItemChanged(index)
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList.removeAt(index)
                                adapter.notifyItemRemoved(index)
                            }
                        }
                    }
                }

                configRecyclerView(prodList)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getAllActiveDataBy() {
        binding.txtSubMenuTitle.text = "Ordenados por Precio"
        val db = FirebaseFirestore.getInstance()

        val productRef = db.collection(Constants.PATH_PRODUCTS)
        firestoreListener = productRef.whereEqualTo("status", "Disponible").orderBy("price")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al consultar datos, verifica tu conexión a internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                val prodList = mutableListOf<ProductsModel>()
                for (snapshot in snapshot!!.documentChanges) {
                    val product = snapshot.document.toObject(ProductsModel::class.java)
                    product.id = snapshot.document.id

                    when (snapshot.type) {
                        DocumentChange.Type.ADDED -> prodList.add(product)
                        DocumentChange.Type.MODIFIED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList[index] = product
                                adapter.notifyItemChanged(index)
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            val index = prodList.indexOfFirst { it.id == product.id }
                            if (index != -1) {
                                prodList.removeAt(index)
                                adapter.notifyItemRemoved(index)
                            }
                        }
                    }
                }

                configRecyclerView(prodList)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getAllData() {
        binding.txtSubMenuTitle.text = "Todos los Productos"
        val db = FirebaseFirestore.getInstance()

        val productRef = db.collection(Constants.PATH_PRODUCTS)
        firestoreListener = productRef.orderBy("price").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Toast.makeText(
                    this,
                    "Error al consultar datos, verifica tu conexión a internet.",
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }

            val prodList = mutableListOf<ProductsModel>()
            for (snapshot in snapshot!!.documentChanges) {
                val product = snapshot.document.toObject(ProductsModel::class.java)
                product.id = snapshot.document.id

                when (snapshot.type) {
                    DocumentChange.Type.ADDED -> prodList.add(product)
                    DocumentChange.Type.MODIFIED -> {
                        val index = prodList.indexOfFirst { it.id == product.id }
                        if (index != -1) {
                            prodList[index] = product
                            adapter.notifyItemChanged(index)
                        }
                    }

                    DocumentChange.Type.REMOVED -> {
                        val index = prodList.indexOfFirst { it.id == product.id }
                        if (index != -1) {
                            prodList.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }
                }
            }

            configRecyclerView(prodList)
        }
    }

    private fun configRecyclerView(itemList: List<ProductsModel>) {
        adapter = ProductsAdapter(itemList.toMutableList())
        binding.recyclerViewProducts.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@ProductsActivity.adapter
        }

        adapter.onItemClick = {
            val i = Intent(this@ProductsActivity, AddProductsActivity::class.java)
            i.putExtra("producto", it)
            startActivity(i)
        }
    }
}