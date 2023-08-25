package com.example.yemektariflerikitabi

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yemektariflerikitabi.databinding.FragmentListBinding
import java.lang.Exception


class FragmentList : Fragment() {

    var mealNameList = ArrayList<String>()
    var mealIdList = ArrayList<Int>()

    private lateinit var listAdapter: ListRecyclerAdapter
    private lateinit var binding: FragmentListBinding


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

            binding = FragmentListBinding.inflate(inflater,container,false)

            return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        listAdapter = ListRecyclerAdapter(mealNameList,mealIdList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = listAdapter

        sqlTakeData()

    }

    @SuppressLint("NotifyDataSetChanged")
    fun sqlTakeData(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("Meals", Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM meals",null)

                val mealNameIndex = cursor.getColumnIndex("mealName")
                val mealIdIndex = cursor.getColumnIndex("id")

                mealNameList.clear()
                mealIdList.clear()

                while (cursor.moveToNext()){
                    mealNameList.add(cursor.getString(mealNameIndex))
                    mealIdList.add(cursor.getInt(mealIdIndex))
                }
                listAdapter.notifyDataSetChanged()

                cursor.close()
            }
        }

        catch (e:Exception){
            e.printStackTrace()
        }
    }




}