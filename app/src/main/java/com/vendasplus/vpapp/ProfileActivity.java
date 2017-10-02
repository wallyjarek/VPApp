package com.vendasplus.vpapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends Activity implements View.OnClickListener {

    private Button logout;
    private TextView userData;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logout = (Button) findViewById(R.id.logout);
        userData = (TextView) findViewById(R.id.dadosUsuario);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        if(firebaseAuth.getCurrentUser() == null) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        user = firebaseAuth.getCurrentUser();
        logout.setOnClickListener(this);

        progressDialog.setMessage("Buscando informações...");
        progressDialog.show();

        showUserData();
    }

    @Override
    public void onClick(View v) {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void showUserData() {
        new getDadosVendedor().execute();
    }

    public void editUserData(Vendedor vendedor) {
        String info = "Nome: " + vendedor.getNome();
        info += "\nEmail: " + vendedor.getEmail();
        info += "\nCidade: " + vendedor.getCidade();
        info += "\nEstado: " + vendedor.getEstado();
        info += "\nTelefone: " + vendedor.getTelefone();
        info += "\n\nPontos: " + vendedor.getPontos();

        userData.setText(info);
    }

    private class getDadosVendedor extends AsyncTask<Void,Void,Vendedor> {

        @Override
        public void onPreExecute(){
        }

        @Override
        public Vendedor doInBackground(Void... params){
            HttpURLConnection con = null;
            try {

                Uri.Builder postBody = new Uri.Builder();
                postBody.appendQueryParameter("email", user.getEmail());
                String query = postBody.build().toString();

                URL url = new URL("http://vendasplus.com.br/r/vendedor/getInfoVendedorByEmail" + query);
                con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(100000);
                con.setConnectTimeout(150000);
                con.setRequestMethod("GET");
                con.setDoInput(true);

                String resultado = Util.streamToString(con.getInputStream());
                Vendedor vendedor = Util.JSONToVendedor(resultado);
                return vendedor;
            }
            catch (Exception e){
                e.printStackTrace();
            }finally {
                con.disconnect();
            }
            return null;
        }

        @Override
        public void onPostExecute(Vendedor vendedor){
            progressDialog.dismiss();
            editUserData(vendedor);
        }
    }
}