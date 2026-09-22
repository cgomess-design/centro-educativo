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

    public NotaAdapter(OnNotaClickListener listener) {
        this.listener = listener;
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

        holder.estudiante.setText(nota.getEstudianteNombre() != null ? nota.getEstudianteNombre() : "ID Estudiante: " + nota.getEstudianteId());
        holder.curso.setText(nota.getCursoNombre() != null ? nota.getCursoNombre() : "ID Curso: " + nota.getCursoId());
        holder.valor.setText("Nota: " + nota.getNota());

        holder.itemView.setOnClickListener(v -> listener.onNotaClick(nota));
    }

    @Override
    public int getItemCount() {
        return notas.size();
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
