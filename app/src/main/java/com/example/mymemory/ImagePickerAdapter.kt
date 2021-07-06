package com.example.mymemory

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemory.models.BoardSize
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val choosenImageUris: List<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener
    ): RecyclerView.Adapter<ImagePickerAdapter.ViewHolder> () {

    interface ImageClickListener {
        fun onPlaceholderClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width /  boardSize.getWidth()
        val cardHeight = parent.width / boardSize.getHeigth()
        val cardSize = min(cardHeight, cardWidth)
        val layoutParams = view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams
        layoutParams.width = cardSize
        layoutParams.height = cardSize
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = boardSize.getNumPairs()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if( position < choosenImageUris.size ) {
            holder.bind(choosenImageUris[position])
        } else {
            holder.bind()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        fun bind(uri: Uri) {
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }

        fun bind() {
            imageClickListener.onPlaceholderClicked()
        }
    }

}
