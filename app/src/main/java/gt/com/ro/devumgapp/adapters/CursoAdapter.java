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
import gt.com.ro.devumgapp.network.model.Curso;

public class CursoAdapter extends RecyclerView.Adapter<CursoAdapter.CursoViewHolder> {

        public interface OnCursoClickListener {
            void onCursoClick(Curso curso);
        }

        private final List<Curso> cursos = new ArrayList<>();
        private final OnCursoClickListener listener;

        public CursoAdapter(OnCursoClickListener listener) {
            this.listener = listener;
        }

        public void submitList(List<Curso> nuevosCursos) {
            cursos.clear();
            cursos.addAll(nuevosCursos);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CursoViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_curso, parent, false);

            return new CursoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull CursoViewHolder holder,
                int position
        ) {
            Curso curso = cursos.get(position);

            holder.nombre.setText(curso.getNombre());
            holder.codigo.setText(curso.getCodigo());
            holder.creditos.setText(String.valueOf(curso.getCreditos()));

            if (listener == null) {
                holder.itemView.setOnClickListener(null);
                holder.itemView.setClickable(false);
            } else {
                holder.itemView.setOnClickListener(
                        v -> listener.onCursoClick(curso)
                );
                holder.itemView.setClickable(true);
            }
        }

        @Override
        public int getItemCount() {
            return cursos.size();
        }

        static class CursoViewHolder extends RecyclerView.ViewHolder {

            final TextView nombre;
            final TextView codigo;
            final TextView creditos;

            CursoViewHolder(@NonNull View itemView) {
                super(itemView);

                nombre = itemView.findViewById(R.id.txtNombreCurso);
                codigo = itemView.findViewById(R.id.txtCodigoCurso);
                creditos = itemView.findViewById(R.id.txtCreditosCurso);
            }
        }
}
