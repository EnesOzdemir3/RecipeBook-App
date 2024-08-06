package com.enesozdemir.tarifkitabiuygulamasi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.enesozdemir.tarifkitabiuygulamasi.databinding.FragmentTarifBinding
import com.enesozdemir.tarifkitabiuygulamasi.databinding.RecyclerRowBinding
import com.enesozdemir.tarifkitabiuygulamasi.model.Tarif
import com.enesozdemir.tarifkitabiuygulamasi.view.ListeFragment
import com.enesozdemir.tarifkitabiuygulamasi.view.ListeFragmentDirections

class tarifAdapter (val tarifListesi: List<Tarif>): RecyclerView.Adapter<tarifAdapter.tarifHolder>() {
    class tarifHolder(val binding: RecyclerRowBinding):RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): tarifHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return tarifHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return tarifListesi.size
    }

    override fun onBindViewHolder(holder: tarifHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = tarifListesi[position].isim
        holder.itemView.setOnClickListener(){
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "eski", id = tarifListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}