package com.example.whatsapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.whatsapp.R;
import com.example.whatsapp.helper.Preferencias;
import com.example.whatsapp.model.Mensagem;

import java.util.ArrayList;
import java.util.List;

public class MensagemAdapter extends ArrayAdapter<Mensagem> {

    private Context context;
    private ArrayList<Mensagem> mensagens;

    public MensagemAdapter(@NonNull Context c, @NonNull ArrayList<Mensagem> objects) {
        super(c, 0, objects);
        this.context = c;
        this.mensagens = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;

        //Verifica se a lista esta preenchida
        if( mensagens != null){

            //Recupera dados do usuario remetente
            Preferencias preferencias = new Preferencias(context);
            String idUsuarioRemetente = preferencias.getIdentificador();

            //Inicializa objeto para montagem do layout
            LayoutInflater Inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            //Recupera mensagem
            Mensagem mensagem = mensagens.get(position);

            //Monta view a partir de xml
            if( idUsuarioRemetente.equals(mensagem.getIdUsuario()) ){
                view = Inflater.inflate(R.layout.item_mensagem_direita, parent, false);
            }else{
                view = Inflater.inflate(R.layout.item_mensagem_esquerda, parent, false);
            }

            //Recupera elemento para a exibição
            TextView textoMensagem = (TextView) view.findViewById(R.id.tv_mensagem);
            textoMensagem.setText(mensagem.getMensagem());

        }


        return view;

    }
}
