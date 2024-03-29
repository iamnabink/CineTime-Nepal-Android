package com.whoamie.cinetime_nepal.common.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.whoamie.cinetime_nepal.R;
import com.whoamie.cinetime_nepal.common.interfaces.AdapterClickListener;
import com.whoamie.cinetime_nepal.common.models.Movie;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MovieActivityAdapter extends RecyclerView.Adapter<MovieActivityAdapter.MovieViewHolder> {
    Context context;
    ArrayList<Movie> umovies;
    AdapterClickListener listener;
    public MovieActivityAdapter(Context context, ArrayList<Movie> umovies, AdapterClickListener listener) {
        this.context = context;
        this.umovies = umovies;
        this.listener = listener;
    }
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_activity_movie,parent,false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = umovies.get(position);
        Picasso.get().load(movie.getPoster_url()).into(holder.imgv);
        holder.genreTv.setText(movie.getGenre());
        holder.nameTv.setText(movie.getName());
    }

    @Override
    public int getItemCount() {
        return umovies.size();
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder{
        TextView nameTv,genreTv;
        ImageView imgv;
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv=itemView.findViewById(R.id.s_movie_name_tv);
            genreTv=itemView.findViewById(R.id.s_movie_genre_tv);
            imgv=itemView.findViewById(R.id.s_movie_imgv);
            imgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition(),v);
                }
            });
        }
    }
}
