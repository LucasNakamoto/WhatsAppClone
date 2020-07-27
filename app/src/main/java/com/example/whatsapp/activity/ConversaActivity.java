package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.adapter.MensagemAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.Preferencias;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Mensagem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ConversaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editMensagem;
    private ImageButton btMensagem;
    private DatabaseReference firebase;
    private ListView listView;
    private ArrayList<Mensagem> mensagens;
    private ArrayAdapter<Mensagem> adapter;
    private ValueEventListener valueEventListenerMensagem;

    //Dados destinatario
    private String nomeUsuarioDestinatario;
    private String idUsuarioDestinatario;
    //dados do remetente
    private String idUsuarioRemetente;
    private String nomeUsuarioRemetente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversa);

        toolbar = (Toolbar) findViewById(R.id.tb_conversa);
        editMensagem = (EditText) findViewById(R.id.edit_mensagem);
        btMensagem = (ImageButton) findViewById(R.id.bt_enviar);
        listView = (ListView) findViewById(R.id.lv_conversas);

        Bundle extra = getIntent().getExtras();

        if(extra != null){
            nomeUsuarioDestinatario = extra.getString("nome");
            String emailDestinatario = extra.getString("email");

            idUsuarioDestinatario = Base64Custom.codificacaoBase64(emailDestinatario);
        }

        //Dados usuario logado
        final Preferencias preferencias = new Preferencias(ConversaActivity.this);
        nomeUsuarioRemetente = preferencias.getNome();
        idUsuarioRemetente = preferencias.getIdentificador();

        //Configura toolbar
        toolbar.setTitle(nomeUsuarioDestinatario);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        setSupportActionBar(toolbar);

        //Monta listview e adapter
        mensagens = new ArrayList<>();

        adapter = new MensagemAdapter(ConversaActivity.this, mensagens);

        listView.setAdapter(adapter);

        //Recuperar mensagens do firebase
        firebase = ConfiguracaoFirebase.getFirebase()
                .child("mensagens")
                .child(idUsuarioRemetente).child(idUsuarioDestinatario);

        //Criar listener mensagens
        valueEventListenerMensagem = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mensagens.clear();

                for( DataSnapshot dado: dataSnapshot.getChildren()){
                    Mensagem mensagem = dado.getValue(Mensagem.class);
                    mensagens.add(mensagem);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        firebase.addValueEventListener( valueEventListenerMensagem);



        //Enviar Mensagem
        btMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoMensagem = editMensagem.getText().toString();

                if(textoMensagem.isEmpty()){
                    Toast.makeText(ConversaActivity.this, "Digite uma mensagem", Toast.LENGTH_SHORT);
                }else{

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioRemetente);
                    mensagem.setMensagem(textoMensagem);

                    //Salvamos mensagem para o remetente
                    Boolean retornoMensagemRemetente = salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);
                    if(!retornoMensagemRemetente){
                        Toast.makeText(ConversaActivity.this, "Problema ao enviar mensagem, tente novamente", Toast.LENGTH_SHORT).show();
                    }else{
                        //Salvamos mensagem para o destinatario
                        Boolean retornoMensagemDestinatario = salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);
                        if(!retornoMensagemDestinatario){
                            Toast.makeText(ConversaActivity.this, "Problema ao enviar mensagem para o destinátario, tente novamente", Toast.LENGTH_SHORT).show();
                        }
                    }

                    //Salvamos a conversa para o remetente
                    Conversa conversa = new Conversa();
                    conversa.setIdUsuario( idUsuarioDestinatario );
                    conversa.setNome( nomeUsuarioDestinatario);
                    conversa.setMensagem(textoMensagem);
                    Boolean retornoConversa = salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, conversa);
                    if(!retornoConversa){
                        Toast.makeText(ConversaActivity.this,"problema ao salvar conversa", Toast.LENGTH_SHORT).show();
                    }else{
                        //Salvamos a conver para o destinatario
                        conversa = new Conversa();
                        conversa.setIdUsuario( idUsuarioRemetente );
                        conversa.setNome( nomeUsuarioDestinatario);
                        conversa.setMensagem(textoMensagem);
                        retornoConversa = salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, conversa);
                        if(!retornoConversa){

                        }else{

                        }
                    }



                    editMensagem.setText("");
                }

            }
        });
    }

    private boolean salvarMensagem(String idRemetente, String idDestinatario, Mensagem mensagem){

        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("mensagens");
            firebase.child(idRemetente)
                    .child(idDestinatario)
                    .push().setValue(mensagem);


            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private Boolean salvarConversa(String idUsuarioRemetente, String idUsuarioDestinatario, Conversa conversa){
        try {
            firebase = ConfiguracaoFirebase.getFirebase().child("conversas");
            firebase.child(idUsuarioRemetente).child(idUsuarioDestinatario).setValue(conversa);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebase.removeEventListener(valueEventListenerMensagem);
    }
}