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
import gt.com.ro.devumgapp.network.model.Nota;

public class NotaAdapter extends RecyclerView.Adapter<NotaAdapter.NotaViewHolder> {

    public interface OnNotaClickListener {
        void onNotaClick(Nota nota);
    }

    private final List<Nota> notas = new ArrayList<>();
    private final OnNotaClickListener listener;
    private final boolean studentView;

    public NotaAdapter(OnNotaClickListener listener) {
        this(listener, false);
    }

    public NotaAdapter(OnNotaClickListener listener, boolean studentView) {
        this.listener = listener;
        this.studentView = studentView;
    }

    public void submitList(List<Nota> nuevasNotas) {
        notas.clear();
        notas.addAll(nuevasNotas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nota, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {
        Nota nota = notas.get(position);

        if (studentView) {
            holder.estudiante.setText(
                    nota.getCursoNombre() != null ? nota.getCursoNombre() : "Curso no disponible");
            holder.curso.setText(
                    nota.getCicloAcademico() != null
                            ? "Ciclo: " + nota.getCicloAcademico()
                            : "Ciclo: No disponible");
            holder.valor.setText(
                    "Zona: " + valueOrUnavailable(nota.getZona())
                            + " | Examen final: " + valueOrUnavailable(nota.getExamenFinal())
                            + " | Nota final: " + valueOrUnavailable(nota.getNotaFinal()));
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        } else {
            holder.estudiante.setText(nota.getEstudianteNombre() != null ? nota.getEstudianteNombre() : "ID Estudiante: " + nota.getEstudianteId());
            holder.curso.setText(nota.getCursoNombre() != null ? nota.getCursoNombre() : "ID Curso: " + nota.getCursoId());
            holder.valor.setText("Nota: " + nota.getNota());
            holder.itemView.setOnClickListener(v -> listener.onNotaClick(nota));
        }
    }

    @Override
    public int getItemCount() {
        return notas.size();
    }

    private String valueOrUnavailable(Double value) {
        return value != null ? String.valueOf(value) : "No disponible";
    }

    static class NotaViewHolder extends RecyclerView.ViewHolder {
        final TextView estudiante;
        final TextView curso;
        final TextView valor;

        NotaViewHolder(@NonNull View itemView) {
            super(itemView);
            estudiante = itemView.findViewById(R.id.txtEstudianteNota);
            curso = itemView.findViewById(R.id.txtCursoNota);
            valor = itemView.findViewById(R.id.txtValorNota);
        }
    }
}
