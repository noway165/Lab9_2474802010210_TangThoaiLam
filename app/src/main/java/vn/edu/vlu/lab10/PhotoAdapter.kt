package vn.edu.vlu.lab10

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import vn.edu.vlu.lab10.databinding.ItemPhotoBinding

/** PhotoAdapter: bridge between the PhotoItem list and RecyclerView, built on ItemPhotoBinding. */
class PhotoAdapter(
    private val photos: List<PhotoItem>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    /** Holds one row's binding so views are not looked up again on every scroll. */
    class PhotoViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photos[position]

        holder.binding.tvTitle.text = photo.title
        holder.binding.tvAuthor.text = "Author: ${photo.author}"

        // Async load on a background thread; UI thread is never blocked
        Glide.with(holder.itemView.context)
            .load(photo.imageUrl)
            .placeholder(R.drawable.ic_placeholder)                // while loading
            .error(R.drawable.ic_broken_image)                     // invalid URL / no network
            .transition(DrawableTransitionOptions.withCrossFade()) // smooth fade-in
            .diskCacheStrategy(DiskCacheStrategy.ALL)              // cache original + transformed
            .into(holder.binding.ivPhoto)
    }

    override fun getItemCount(): Int = photos.size

    /** Cancel any pending load for a recycled row: no wrong image in reused rows, less memory. */
    override fun onViewRecycled(holder: PhotoViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.itemView.context).clear(holder.binding.ivPhoto)
    }
}
