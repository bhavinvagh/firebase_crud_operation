package com.example.db;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText categoryEditText;
    private Button addCategoryButton;
    private Button updateCategoryButton;
    private Button deleteCategoryButton;
    private ListView categoryListView;

    private DatabaseReference databaseReference;
    private ArrayList<String> categoryList;
    private ArrayAdapter<String> adapter;
    private String selectedCategoryKey; // To store the key of the selected category

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        categoryEditText = findViewById(R.id.categoryEditText);
        addCategoryButton = findViewById(R.id.addCategoryButton);
        updateCategoryButton = findViewById(R.id.updateCategoryButton);
        deleteCategoryButton = findViewById(R.id.deleteCategoryButton);
        categoryListView = findViewById(R.id.categoryListView);

        databaseReference = FirebaseDatabase.getInstance().getReference("categories");
        categoryList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        categoryListView.setAdapter(adapter);

        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
            }
        });

        updateCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCategory();
            }
        });

        deleteCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCategory();
            }
        });

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectCategory(position);
            }
        });

        loadCategories();
    }

    private void loadCategories() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String category = dataSnapshot.getValue(String.class);
                    categoryList.add(category);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addCategory() {
        String categoryName = categoryEditText.getText().toString();
        if (!categoryName.isEmpty()) {
            String id = databaseReference.push().getKey();
            databaseReference.child(id).setValue(categoryName);
            categoryEditText.setText("");
            Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Enter a category name", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectCategory(int position) {
        String categoryName = categoryList.get(position);
        categoryEditText.setText(categoryName);
        selectedCategoryKey = categoryName; // Store the selected category name
        updateCategoryButton.setVisibility(View.VISIBLE); // Show update button
        deleteCategoryButton.setVisibility(View.VISIBLE); // Show delete button
        addCategoryButton.setVisibility(View.GONE); // Hide add button
    }

    private void updateCategory() {
        String newCategoryName = categoryEditText.getText().toString();
        if (selectedCategoryKey != null && !newCategoryName.isEmpty()) {
            // Find the category key and update it
            databaseReference.orderByValue().equalTo(selectedCategoryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        categorySnapshot.getRef().setValue(newCategoryName); // Update the value
                        Toast.makeText(MainActivity.this, "Category updated", Toast.LENGTH_SHORT).show();
                        break; // Break after updating the first match
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error updating category", Toast.LENGTH_SHORT).show();
                }
            });
            // Reset the view
            categoryEditText.setText("");
            selectedCategoryKey = null;
            updateCategoryButton.setVisibility(View.GONE);
            deleteCategoryButton.setVisibility(View.GONE);
            addCategoryButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Enter a new category name", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCategory() {
        if (selectedCategoryKey != null) {
            // Find the category key and delete it
            databaseReference.orderByValue().equalTo(selectedCategoryKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                        categorySnapshot.getRef().removeValue(); // Delete the category
                        Toast.makeText(MainActivity.this, "Category deleted", Toast.LENGTH_SHORT).show();
                        break; // Break after deleting the first match
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error deleting category", Toast.LENGTH_SHORT).show();
                }
            });
            // Reset the view
            categoryEditText.setText("");
            selectedCategoryKey = null;
            updateCategoryButton.setVisibility(View.GONE);
            deleteCategoryButton.setVisibility(View.GONE);
            addCategoryButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Select a category to delete", Toast.LENGTH_SHORT).show();
        }
    }
}
