package com.example.mongodbfirst;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import okhttp3.*;
import java.io.IOException;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private final String SERVER_URL = "http://10.0.2.2:3000";

    private EditText etName, etAge;
    private TextView tvResult;



    private RecyclerView rvUsers;
    private MainActivity.UserAdapter userAdapter;
    private List<MainActivity.User> userList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        //tvResult = findViewById(R.id.tvResult);

        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList);
        rvUsers.setAdapter(userAdapter);

        Button btnAddUser = findViewById(R.id.btnAddUser);
        Button btnFetchUsers = findViewById(R.id.btnFetchUsers);
        Button btnUpdateUser = findViewById(R.id.btnUpdateUser);
        Button btnDeleteUser = findViewById(R.id.btnDeleteUser);
        Button btnClear = findViewById(R.id.btnClear);


        btnAddUser.setOnClickListener(v -> addUser(etName.getText().toString(), Integer.parseInt(etAge.getText().toString())));
        btnFetchUsers.setOnClickListener(v -> fetchUsers());
        btnUpdateUser.setOnClickListener(v -> updateUser(etName.getText().toString(), Integer.parseInt(etAge.getText().toString())));
        btnDeleteUser.setOnClickListener(v -> deleteUser(etName.getText().toString()));
        btnClear.setOnClickListener(v -> clearRecyclerView());






        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private final List<User> userList;

        public UserAdapter(List<User> userList) {
            this.userList = userList;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = userList.get(position);
            holder.tvName.setText("Name: " + user.name);
            holder.tvAge.setText("Age: " + user.age);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAge;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvName);
                tvAge = itemView.findViewById(R.id.tvAge);
            }
        }
    }

    private void addUser(String name, int age) {



        //simple validation
       /* for (User u : userList) {
            if (u.name.equalsIgnoreCase(name)) { // Case-insensitive comparison
                Toast.makeText(MainActivity.this, "User with the same name already exists!", Toast.LENGTH_SHORT).show();
                return; // Stop further execution
            }
        }*/


        // If no duplicate is found, proceed with adding the user
        User user = new User(name, age);
        String json = new Gson().toJson(user);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SERVER_URL + "/addUser")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showError("Error adding user: " + e.getMessage()));

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        userList.add(user);
                        userAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "User added successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        showError("Error: " + response.message());
                        Toast.makeText(MainActivity.this, "Failed to add user. " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchUsers() {
        Request request = new Request.Builder()
                .url(SERVER_URL + "/users")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvResult.setText("Error: " + e.getMessage())); // Optional logging
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    Type userListType = new TypeToken<List<User>>() {}.getType();
                    List<User> fetchedUsers = gson.fromJson(responseBody, userListType);

                    runOnUiThread(() -> {
                        userList.clear();
                        userList.addAll(fetchedUsers);
                        userAdapter.notifyDataSetChanged();

                        Toast.makeText(MainActivity.this, "Users fetched successfully!", Toast.LENGTH_SHORT).show();

                    });
                }
            }
        });
    }


    private void updateUser(String name, int age) {
        User user = new User(name, age);
        String json = new Gson().toJson(user);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SERVER_URL + "/updateUser/" + name) // Ensure this matches your server's route
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showError("Error updating user: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        // Update the user in the list
                        for (User u : userList) {
                            if (u.name.equals(name)) {
                                u.age = age;
                                break;
                            }
                        }
                        userAdapter.notifyDataSetChanged();

                        // Show a success toast
                        Toast.makeText(MainActivity.this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> showError("Failed to update user: " + response.message()));
                }
            }
        });
    }


    private void deleteUser(String name) {
        Request request = new Request.Builder()
                .url(SERVER_URL + "/deleteUser/" + name)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showError("Error deleting user: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        // Remove the user from the list safely
                        for (int i = 0; i < userList.size(); i++) {
                            if (userList.get(i).name.equals(name)) {
                                userList.remove(i);
                                break;
                            }
                        }
                        userAdapter.notifyDataSetChanged();

                        Toast.makeText(MainActivity.this, "User deleted successfully!", Toast.LENGTH_SHORT).show();

                    });
                } else {
                    runOnUiThread(() -> showError("Failed to delete user: " + response.message()));
                }
            }
        });
    }


    private void showError(String errorMessage) {
        // Display an error message in the RecyclerView or via Toast
        Log.e("MainActivity", errorMessage);
    }

    private void updateUI(List<User> users) {
        runOnUiThread(() -> {
            userList.clear(); // Clear the current list
            userList.addAll(users); // Add the new list of users
            userAdapter.notifyDataSetChanged(); // Notify the adapter to update the RecyclerView
        });
    }

    private void clearRecyclerView() {
        runOnUiThread(() -> {
            userList.clear(); // Clear the data list
            userAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the view
        });
    }


    class User {
        String name;
        int age;

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}