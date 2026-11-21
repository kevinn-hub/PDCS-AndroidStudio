package com.a2511500012.Mahasiswa.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.a2511500012.Mahasiswa.databinding.ItemMahasiswaBinding
import com.a2511500012.Mahasiswa.model.Mahasiswa

class MahasiswaAdapter(
    private var items: List<Mahasiswa> = emptyList(),
    private val onItemClick: (Mahasiswa) -> Unit = {}
) : RecyclerView.Adapter<MahasiswaAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemMahasiswaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(m: Mahasiswa) {
            binding.tvNama.text = m.nama
            binding.tvNim.text = "NIM: ${m.nim}"
            binding.tvJurusan.text = m.jurusan

            binding.root.setOnClickListener {
                onItemClick(m)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMahasiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newData: List<Mahasiswa>) {
        items = newData
        notifyDataSetChanged()
    }
}