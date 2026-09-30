package gt.com.ro.devumgapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.model.Carrera;

public class CarreraAdapter extends RecyclerView.Adapter<CarreraAdapter.CarreraViewHolder> {
    public interface OnCarreraClickListener {
        void onCarreraClick(Carrera carrera);
    }

    private final List<Carrera> carreras = new ArrayList<>();
    private final OnCarreraClickListener listener;

    public CarreraAdapter(OnCarreraClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Carrera> nuevasCarreras) {
        carreras.clear();
        carreras.addAll(nuevasCarreras);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CarreraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_carrera, parent, false);
        return new CarreraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarreraViewHolder holder, int position) {
        Carrera carrera = carreras.get(position);
        holder.nombre.setText(carrera.getNombre());
        holder.codigo.setText(carrera.getCodigo());
        holder.itemView.setOnClickListener(v -> listener.onCarreraClick(carrera));
    }

    @Override
    public int getItemCount() {
        return carreras.size();
    }

    static class CarreraViewHolder extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView codigo;

        CarreraViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtNombreCarrera);
            codigo = itemView.findViewById(R.id.txtCodigoCarrera);
        }
    }
}
