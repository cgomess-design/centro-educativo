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
import gt.com.ro.devumgapp.network.model.Docente;

public class DocenteAdapter extends RecyclerView.Adapter<DocenteAdapter.DocenteViewHolder> {

    public interface OnDocenteClickListener {
        void onDocenteClick(Docente docente);
    }

    private final List<Docente> docentes = new ArrayList<>();
    private final OnDocenteClickListener listener;

    public DocenteAdapter(OnDocenteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Docente> nuevosDocentes) {
        docentes.clear();
        docentes.addAll(nuevosDocentes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocenteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_docente, parent, false);
        return new DocenteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocenteViewHolder holder, int position) {
        Docente docente = docentes.get(position);

        String nombreCompleto = docente.getNombre() + " " + docente.getApellido();
        holder.nombre.setText(nombreCompleto);
        holder.emailInstitucional.setText(docente.getemailInstitucional());
        holder.emailPersonal.setText(docente.getEmailPersonal());
        holder.dpi.setText(docente.getDpi());
        holder.telefono.setText(docente.getTelefono());
        holder.especialidad.setText(docente.getEspecialidad());
        holder.fechaContratacion.setText(docente.getFechaContratacion());

        holder.itemView.setOnClickListener(v -> listener.onDocenteClick(docente));
    }

    @Override
    public int getItemCount() {
        return docentes.size();
    }

    static class DocenteViewHolder extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView emailInstitucional;
        final TextView emailPersonal;

        final TextView dpi;
        final TextView telefono;
        final TextView especialidad;

        final TextView fechaContratacion;

        DocenteViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtNombreDocente);
            emailInstitucional = itemView.findViewById(R.id.txtEmailInstitucionalDocente);
            emailPersonal = itemView.findViewById(R.id.txtEmailPersonalDocente);
            dpi = itemView.findViewById(R.id.txtDpiDocente);
            telefono = itemView.findViewById(R.id.txtTelefonoDocente);
            especialidad = itemView.findViewById(R.id.txtEspecialidadDocente);
            fechaContratacion = itemView.findViewById(R.id.txtFechaContratacionDocente);

        }
    }
}

