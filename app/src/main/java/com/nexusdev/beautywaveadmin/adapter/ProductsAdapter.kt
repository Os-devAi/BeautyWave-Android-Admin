package com.nexusdev.beautywaveadmin.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexusdev.beautywaveadmin.R
import com.nexusdev.beautywaveadmin.databinding.ProductsViewBinding
import com.nexusdev.beautywaveadmin.model.ProductsModel

class ProductsAdapter(private val originalProductsList: List<ProductsModel>) :
    RecyclerView.Adapter<ProductsAdapter.ProductsHolder>() {

    var onItemClick: ((ProductsModel) -> Unit)? = null

    // Lista filtrada
    private var filteredProductsList: List<ProductsModel> = originalProductsList.toMutableList()

    class ProductsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun render(product: ProductsModel) {
            val binding = ProductsViewBinding.bind(itemView)
            binding.productName.text = product.name
            binding.currentPrice.text = "Q." + product.price.toString() + "0"
            val imgUrl = product.imgUrl
            val img = binding.productImage
            Glide
                .with(img)
                .load(imgUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(img)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductsHolder(layoutInflater.inflate(R.layout.products_view, parent, false))
    }

    override fun getItemCount(): Int = filteredProductsList.size

    override fun onBindViewHolder(holder: ProductsHolder, position: Int) {
        val item = filteredProductsList[position]
        holder.render(item)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    fun filter(query: String) {
        filteredProductsList = if (query.isEmpty()) {
            originalProductsList
        } else {
            originalProductsList.filter {
                it.name!!.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}