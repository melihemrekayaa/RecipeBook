package com.example.yemektariflerikitabi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView

class ListRecyclerAdapter(val mealList: ArrayList<String>,val idList: ArrayList<Int>) : RecyclerView.Adapter<ListRecyclerAdapter.MealHolder>() {


    class MealHolder(itemView: View) : RecyclerView.ViewHolder(itemView){


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealHolder {

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_row,parent,false)
        return MealHolder(view)
    }

    override fun getItemCount(): Int {

        return mealList.size
    }

    override fun onBindViewHolder(holder: MealHolder, position: Int) {

            holder.itemView.findViewById<TextView>(R.id.recycler_row_text).text = mealList[position]
            holder.itemView.setOnClickListener {
                val action = FragmentListDirections.actionFragmentList2ToFragmentRecipe("recycler",idList[position])
                Navigation.findNavController(it).navigate(action)
            }
    }
}