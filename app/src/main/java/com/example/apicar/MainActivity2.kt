package com.example.apicar

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity2 : AppCompatActivity() {
    private lateinit var upName: EditText
    private lateinit var upPrice: EditText
    private lateinit var upImage: EditText
    private lateinit var upCheckbox: CheckBox
    private lateinit var upButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        upName = findViewById(R.id.name)
        upPrice = findViewById(R.id.price)
        upImage = findViewById(R.id.image)
        upCheckbox = findViewById(R.id.full)
        upButton = findViewById(R.id.up)
        deleteButton = findViewById(R.id.delete)

        // Retrieve car details from Intent
        val carId = intent.getIntExtra("id", 0)
        val name = intent.getStringExtra("name")
        val price = intent.getDoubleExtra("prix", 0.0)
        val isFullOptions = intent.getBooleanExtra("check", false)
        val image = intent.getStringExtra("image")

        // Set values in UI
        upName.setText(name)
        upPrice.setText(price.toString())
        upImage.setText(image)
        upCheckbox.isChecked = isFullOptions

        // Set up delete button listener
        deleteButton.setOnClickListener {
            deleteCar(carId)
        }

        // Set up update button listener
        upButton.setOnClickListener {
            updateCar(carId)
        }
    }

    private fun updateCar(carId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apiyes.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        // Gather updated values from the UI
        val updatedName = upName.text.toString().trim()
        val updatedPriceStr = upPrice.text.toString().trim()
        val updatedImageUrl = upImage.text.toString().trim()
        val isFullOption = upCheckbox.isChecked

        // Validate input fields
        if (updatedName.isEmpty() || updatedPriceStr.isEmpty() || updatedImageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert price to Double
        val updatedPrice = updatedPriceStr.toDoubleOrNull()
        if (updatedPrice == null) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the updated Car object
        val updatedCar = Car(carId, updatedName, updatedPrice, isFullOption, updatedImageUrl)

        // Make the API call to update the car
        apiService.updateCar(updatedCar).enqueue(object : Callback<AddResponse> {
            override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity2, "Car updated successfully", Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    // Log the error response
                    val errorBody = response.errorBody()?.string()
                    Log.e("UpdateCarError", "Error: $errorBody")
                    Toast.makeText(this@MainActivity2, "Failed to update car: $errorBody", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                Log.e("UpdateCarFailure", "Error: ${t.message}")
                Toast.makeText(this@MainActivity2, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteCar(carId: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apiyes.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        val carToDelete = Car(id = carId, name = "", price = 0.0, isFullOptions = false, image = "")

        apiService.deleteCar(carToDelete).enqueue(object : Callback<AddResponse> {
            override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                if (response.isSuccessful) {
                    val deleteResponse = response.body()
                    if (deleteResponse != null && deleteResponse.status == 1) {
                        Toast.makeText(applicationContext, "Car deleted successfully!", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Failed to delete car", Toast.LENGTH_LONG).show()
                    }
                } else {
                    response.errorBody()?.let { errorBody ->
                        val errorString = errorBody.string()
                        Log.e("DeleteCarError", "Error response: $errorString")
                        Toast.makeText(applicationContext, "Error deleting car: $errorString", Toast.LENGTH_LONG).show()
                    } ?: run {
                        Log.e("DeleteCarError", "Error response: Unknown error")
                        Toast.makeText(applicationContext, "Error deleting car: Unknown error", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
