package com.example.bit242713_foodmanager

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DishAdapter(
    private var dishList: List<Dish>,
    private val onItemClick: (Dish) -> Unit
) : RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    class DishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDishName: TextView = itemView.findViewById(R.id.tvDishName)
        val tvDishPrice: TextView = itemView.findViewById(R.id.tvDishPrice)
        val tvPrepTag: TextView = itemView.findViewById(R.id.tvPrepTag)
        val tvStatusTag: TextView = itemView.findViewById(R.id.tvStatusTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishList[position]

        holder.tvDishName.text = dish.name
        holder.tvDishPrice.text = "Giá: ${dish.price} VNĐ"

        if (dish.preparationTime <= 15) {
            holder.tvPrepTag.text = "Quick / Nhanh"
            holder.tvPrepTag.setBackgroundColor(Color.parseColor("#E0E0E0"))
        } else {
            holder.tvPrepTag.text = "Regular / Thông thường"
            holder.tvPrepTag.setBackgroundColor(Color.parseColor("#FFF9C4")) // Màu vàng nhạt
        }

        if (dish.isAvailable) {
            holder.tvStatusTag.text = "Đang bán"
            holder.tvStatusTag.setBackgroundColor(Color.parseColor("#4CAF50")) // Xanh lá
        } else {
            holder.tvStatusTag.text = "Ngừng bán"
            holder.tvStatusTag.setBackgroundColor(Color.parseColor("#F44336")) // Đỏ
        }

        holder.itemView.setOnClickListener {
            onItemClick(dish)
        }
    }

    override fun getItemCount(): Int = dishList.size

    fun updateData(newList: List<Dish>) {
        dishList = newList
        notifyDataSetChanged()
    }
}