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
import gt.com.ro.devumgapp.network.model.Estudiante;

public class EstudianteAdapter extends RecyclerView.Adapter<EstudianteAdapter.EstudianteViewHolder> {
    public interface OnEstudianteClickListener { void onEstudianteClick(Estudiante estudiante); }

    private final List<Estudiante> estudiantes = new ArrayList<>();
    private final OnEstudianteClickListener listener;

    public EstudianteAdapter(OnEstudianteClickListener listener) { this.listener = listener; }

    public void submitList(List<Estudiante> nuevosEstudiantes) {
        estudiantes.clear();
        estudiantes.addAll(nuevosEstudiantes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EstudianteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_estudiante, parent, false);
        return new EstudianteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EstudianteViewHolder holder, int position) {
        Estudiante estudiante = estudiantes.get(position);
        holder.nombre.setText(estudiante.getNombreCompleto());
        holder.codigo.setText(estudiante.getCodigoEstudiante());
        holder.carnet.setText(estudiante.getCarnet());
        holder.itemView.setOnClickListener(v -> listener.onEstudianteClick(estudiante));
    }

    @Override
    public int getItemCount() { return estudiantes.size(); }

    static class EstudianteViewHolder extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView codigo;
        final TextView carnet;

        EstudianteViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtNombreEstudiante);
            codigo = itemView.findViewById(R.id.txtCodigoEstudiante);
            carnet = itemView.findViewById(R.id.txtCarnetEstudiante);
        }
    }
}
