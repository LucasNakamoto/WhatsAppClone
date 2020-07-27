package com.example.whatsapp.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissão {

    public static boolean validarPermissoes(int requestCode, Activity activity, String[] permissoes){

        if (Build.VERSION.SDK_INT >= 23) {

            List<String> listaPermissoes = new ArrayList<String>();

            /*Percorre as permissoes passadas uma a uma, verificando se elas foram liberadas*/
            for( String permissao: permissoes) {
                Boolean validarPermissao = ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;

                if (!validarPermissao) {
                    listaPermissoes.add(permissao);
                }
            }
            //Caso a lista esteja vazia não e nescesariosolicitar permissao
            if(listaPermissoes.isEmpty()) return  true;

            //converter array list para array string
            String[] novasPermissoes = new String[listaPermissoes.size()];
            listaPermissoes.toArray(novasPermissoes);

            //solicitar permissao
            ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);
        }


        return true;
    }
}
