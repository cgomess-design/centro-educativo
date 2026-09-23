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
import gt.com.ro.devumgapp.network.model.Inscripcion;

public class InscripcionAdapter
        extends RecyclerView.Adapter<InscripcionAdapter.InscripcionViewHolder> {

    public interface OnInscripcionClickListener {
        void onInscripcionClick(Inscripcion inscripcion);
    }

    private final List<Inscripcion> inscripciones = new ArrayList<>();
    private final OnInscripcionClickListener listener;

    public InscripcionAdapter(
            OnInscripcionClickListener listener
    ) {
        this.listener = listener;
    }

    public void submitList(
            List<Inscripcion> nuevasInscripciones
    ) {
        inscripciones.clear();
        inscripciones.addAll(nuevasInscripciones);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InscripcionViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_inscripcion,
                        parent,
                        false
                );

        return new InscripcionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull InscripcionViewHolder holder,
            int position
    ) {
        Inscripcion inscripcion =
                inscripciones.get(position);

        holder.estudiante.setText(
                inscripcion.getNombreEstudiante() != null
                        ? inscripcion.getNombreEstudiante()
                        : "Estudiante no disponible"
        );

        holder.curso.setText(
                inscripcion.getNombreCurso() != null
                        ? inscripcion.getNombreCurso()
                        : "Curso no disponible"
        );

        holder.fecha.setText(
                inscripcion.getFechaInscripcion() != null
                        ? "Fecha: " + inscripcion.getFechaInscripcion()
                        : "Fecha: No disponible"
        );

        holder.itemView.setOnClickListener(
                v -> listener.onInscripcionClick(inscripcion)
        );
    }

    @Override
    public int getItemCount() {
        return inscripciones.size();
    }

    static class InscripcionViewHolder
            extends RecyclerView.ViewHolder {

        final TextView estudiante;
        final TextView curso;
        final TextView fecha;

        InscripcionViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            estudiante = itemView.findViewById(
                    R.id.txtNombreEstudianteInscripcion
            );

            curso = itemView.findViewById(
                    R.id.txtNombreCursoInscripcion
            );

            fecha = itemView.findViewById(
                    R.id.txtFechaInscripcion
            );
        }
    }
}